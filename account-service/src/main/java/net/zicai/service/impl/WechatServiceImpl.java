package net.zicai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.zicai.DTO.AccountLoginResultDTO;
import net.zicai.DTO.WechatAccountDTO;
import net.zicai.DTO.WechatQrCodeDTO;
import net.zicai.config.RedisKeyManager;
import net.zicai.config.WechatLoginConfig;
import net.zicai.dto.AccountDTO;
import net.zicai.enums.AccountRoleEnum;
import net.zicai.enums.StatusEnum;
import net.zicai.enums.WechatSceneQrCodeStatus;
import net.zicai.mapper.AccountMapper;
import net.zicai.model.AccountDO;
import net.zicai.service.WechatService;
import net.zicai.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author wangdi
 * @date 2026/5/5 20:56
 * @description
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WechatServiceImpl implements WechatService {

    @Autowired
    private WechatLoginConfig wechatLoginConfig;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AccountMapper accountMapper;


    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 分布式锁过期时间（秒）
     */
    private static final long LOCK_EXPIRE_SECONDS = 10;

    /**
     * 获取锁最大重试次数
     */
    private static final int MAX_RETRY_TIMES = 10;

    /**
     * 释放锁的Lua脚本（保证原子性）
     */
    private static final String RELEASE_LOCK_SCRIPT =
            "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";

    @Override
    public String getAccessToken() {
        String cacheKey = String.format(RedisKeyManager.WECHAT_ACCESS_TOKEN, wechatLoginConfig.getAppId());

        // 1. 先查缓存
        String cachedToken = (String) redisTemplate.opsForValue().get(cacheKey);
        if (StringUtils.hasText(cachedToken)) {
            log.debug("从缓存中获取到微信access_token");
            return cachedToken;
        }

        // 2. 缓存未命中，尝试获取分布式锁
        for (int i = 0; i < MAX_RETRY_TIMES; i++) {
            String lockKey = String.format(RedisKeyManager.REDIS_WECHAT_ACCESS_TOKEN_LOCK, wechatLoginConfig.getAppId());
            Boolean locked = redisTemplate.opsForValue()
                    .setIfAbsent(lockKey,
                            "lock", LOCK_EXPIRE_SECONDS, TimeUnit.SECONDS);

            if (Boolean.TRUE.equals(locked)) {
                try {
                    // 3. 双重检查：加锁后再次查缓存（其他线程可能已写入）
                    cachedToken = (String) redisTemplate.opsForValue().get(cacheKey);
                    if (StringUtils.hasText(cachedToken)) {
                        log.debug("双重检查命中缓存，直接返回");
                        return cachedToken;
                    }

                    // 4. 调用微信API获取并缓存
                    return fetchAndCacheAccessToken(cacheKey);

                } finally {
                    // 5. 释放锁（Lua脚本保证只释放自己持有的锁）
                    releaseLock(RedisKeyManager.REDIS_WECHAT_ACCESS_TOKEN_LOCK, "lock");
                }
            }

            // 6. 未获取到锁，短暂休眠后重试
            try {
                TimeUnit.MILLISECONDS.sleep(100 + (long) (Math.random() * 100));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("获取微信access_token等待被中断", e);
                return null;
            }

            // 重试前再查一次缓存（可能其他线程已更新）
            cachedToken = (String) redisTemplate.opsForValue().get(cacheKey);
            if (StringUtils.hasText(cachedToken)) {
                return cachedToken;
            }
        }

        log.error("获取微信access_token分布式锁重试耗尽，可能锁竞争过于激烈");
        return null;
    }

    @Override
    public JsonData generateQrcode() {

        try {
            //1 生成Scene Id
            String sceneId = UUID.randomUUID().toString();
            //2 获取 access_token
            String accessToken = getAccessToken();
            if (accessToken==null || accessToken.isEmpty()) {
                log.error("获取微信access_token失败");
                return JsonData.buildError("获取微信access_token失败");
            }
            //3创建ticket
            String qrCodeUrlTicket = createQrcode(accessToken, sceneId);
            if (qrCodeUrlTicket==null){
                log.error("创建微信ticket失败");
                return JsonData.buildError("创建微信ticket失败");
            }
            //4 构建二维码
            WechatQrCodeDTO qrCodeDTO = WechatQrCodeDTO.builder()
                    .sceneId(sceneId)
                    .qrCodeUrl(qrCodeUrlTicket)
                    .expireTime(wechatLoginConfig.getQrCodeExpireTime())
                    .status("WAITING")
                    .createTime(System.currentTimeMillis())
                    .build();
            //5 缓存二维码信息
            redisTemplate.opsForValue().set(String.format(RedisKeyManager.WECHAT_QR_CODE_SCENE, sceneId), qrCodeDTO,
                    wechatLoginConfig.getQrCodeExpireTime(), TimeUnit.SECONDS);
            log.info("生成二维码成功，二维码信息：{}", qrCodeDTO);
            return JsonData.buildSuccess(qrCodeDTO);
        } catch (Exception e) {
            log.error("生成微信二维码失败", e);
            return JsonData.buildError("生成微信二维码失败");
        }

    }

    @Override
    public JsonData getLoginResult(String sceneId) {
        //从缓存中获取二维码信息
        String key = String.format(RedisKeyManager.WECHAT_QR_CODE_SCENE, sceneId);
        WechatQrCodeDTO qrCodeDTO = (WechatQrCodeDTO) redisTemplate.opsForValue().get(key);
        if (qrCodeDTO==null){
            log.error("二维码信息不存在");
            return JsonData.buildError("二维码信息不存在");
        }
        //如果不是CONFIDED状态，则返回对应的状态
        if (!WechatSceneQrCodeStatus.CONFIRMED.name().equals(qrCodeDTO.getStatus())) {
            return JsonData.buildSuccess(qrCodeDTO);
        }
        //如果是CONFIRMED状态，则返回用户信息
        String userKey = String.format(RedisKeyManager.WECHAT_USER_LOGIN_RESULT, sceneId);
        AccountLoginResultDTO loginResultDTO = (AccountLoginResultDTO) redisTemplate.opsForValue().get(userKey);

        return JsonData.buildSuccess(loginResultDTO);
    }

    @Override
    public JsonData handleScanCallback(String sceneId, String openid) {
        log.info("开始处理微信扫码回调：sceneId={}, openid={}", sceneId, openid);

        // 1. 检查二维码是否存在
        String redisKey = String.format(RedisKeyManager.WECHAT_QR_CODE_SCENE, sceneId);
        WechatQrCodeDTO qrCodeDTO = (WechatQrCodeDTO) redisTemplate.opsForValue().get(redisKey);
        if (qrCodeDTO == null) {
            log.error("二维码不存在或已过期：sceneId={}", sceneId);
            return JsonData.buildError("二维码不存在或已过期");
        }

        // 2. 更新二维码状态为已扫码
        updateQrCodeStatus(sceneId, WechatSceneQrCodeStatus.SCANNED);
        log.info("更新二维码状态为已扫码：sceneId={}, openid={}", sceneId, openid);

        // 3. 获取用户信息
        log.info("开始获取微信用户信息：openid={}", openid);
        WechatAccountDTO userInfo = getWechatAccountInfo(openid);
        if (userInfo == null) {
            log.error("获取用户信息失败：openid={}", openid);
            return JsonData.buildError("获取用户信息失败");
        }
        log.info("获取用户信息成功：openid={}", openid);

        // 4. 登录或注册用户
        log.info("开始登录或注册用户：openid={}", openid);
        AccountLoginResultDTO loginResult = loginOrRegister(userInfo);

        if (loginResult.getSuccess()) {
            // 5. 更新二维码状态为已确认
            updateQrCodeStatus(sceneId, WechatSceneQrCodeStatus.CONFIRMED);
            log.info("更新二维码状态为已确认：sceneId={}, openid={}", sceneId, openid);

            // 6. 将登录结果存储到Redis，供前端轮询获取
            String loginResultKey = String.format(RedisKeyManager.WECHAT_USER_LOGIN_RESULT, sceneId);
            redisTemplate.opsForValue().set(loginResultKey, loginResult,
                    wechatLoginConfig.getLoginStateExpireTime(), TimeUnit.SECONDS);
            log.info("存储登录结果到Redis：sceneId={}, token={}", sceneId, loginResult.getToken());
        } else {
            log.error("用户登录或注册失败：openid={}, error={}", openid, loginResult.getErrorMessage());
            return JsonData.buildError("登录失败：" + loginResult.getErrorMessage());
        }

        log.info("处理微信扫码回调成功，sceneId：{}，openid：{}", sceneId, openid);
        return JsonData.buildSuccess(loginResult);
    }

    private AccountLoginResultDTO loginOrRegister(WechatAccountDTO userInfo) {
        //查询账号
        AccountDO accountDO = accountMapper.selectOne(new LambdaQueryWrapper<AccountDO>()
                .eq(AccountDO::getOpenid, userInfo.getOpenid()));
        if(accountDO == null){
            accountDO = AccountDO.builder()
                    .username("wx_"+ CommonUtil.generateUUID())
                    .openid(userInfo.getOpenid())
                    .unionid(userInfo.getUnionid())
                    .status(StatusEnum.ON.name())
                    .role(AccountRoleEnum.COMMON.name())
                    .build();
            accountMapper.insert(accountDO);
            log.info("新用户注册成功：{}", accountDO);
        }
        //生成jwt
        AccountDTO accountDTO = SpringBeanUtil.copyProperties(accountDO, AccountDTO.class);
        String token = JwtUtil.geneTenantAccountLoginJWT(accountDTO);
        log.info("生成jwt成功：{}", token);

        return AccountLoginResultDTO.builder().success(true).token(token).accountDTO(accountDTO).build();
    }

    private WechatAccountDTO getWechatAccountInfo(String openid) {
        try {
            String accessToken = getAccessToken();

            String url = String.format("%s?access_token=%s&openid=%s", wechatLoginConfig.getUserInfoUrl(), accessToken, openid);

            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            if(jsonNode.has("errcode")){
                log.error("获取微信用户信息失败：{}", jsonNode.get("errmsg"));
                return null;
            }
            WechatAccountDTO wechatAccountDTO = WechatAccountDTO.builder()
                    .openid(jsonNode.get("openid").asText())
                    .unionid(jsonNode.has("unionid")?jsonNode.get("unionid").asText():"")
                    .subscribe(jsonNode.get("subscribe").asInt())
                    .subscribeTime(jsonNode.get("subscribe_time").asLong())
                    .remark(jsonNode.get("remark").asText())
                    .groupid(jsonNode.get("groupid").asInt()).build();

            return wechatAccountDTO;
        }catch (Exception e){
            log.error("获取微信用户信息异常", e);
            return null;
        }
    }

    private void updateQrCodeStatus(String sceneId, WechatSceneQrCodeStatus status) {

        String redisKey = String.format(RedisKeyManager.WECHAT_QR_CODE_SCENE, sceneId);
        WechatQrCodeDTO qrCodeDTO = (WechatQrCodeDTO)redisTemplate.opsForValue().get(redisKey);
        if(qrCodeDTO!=null){
            qrCodeDTO.setStatus(status.name());
            redisTemplate.opsForValue().set(redisKey, qrCodeDTO,wechatLoginConfig.getQrCodeExpireTime(), TimeUnit.SECONDS);
        }

    }

    /**
     * 创建二维码ticket - 使用场景二维码方式
     */
    private String createQrcode(String accessToken, String sceneId) {

        //构建请求URL
        try {
            String url = String.format("%s?access_token=%s", wechatLoginConfig.getQrCodeUrl(), accessToken);

            //构建请求体
            Map<String, Object> body = Map.of(
                    "action_name", "QR_STR_SCENE",
                    "expire_seconds", wechatLoginConfig.getQrCodeExpireTime(),
                    "action_info", Map.of("scene", Map.of("scene_str", sceneId))
            );

            String jsonBody = JsonUtil.obj2Json(body);
            log.info("请求微信服务器, url:{}, body:{}", url, jsonBody);
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, jsonBody, String.class);
            String responseBody = responseEntity.getBody();
            log.info("微信服务器返回:{}", responseBody);

            //解析结果
            JsonNode jsonNode = objectMapper.readTree(responseBody);

            if (jsonNode.has("ticket")) {
                String ticket = jsonNode.get("ticket").asText();
                log.info("创建二维码成功：{}", ticket);
                String qrCodeUrl = String.format("%s?ticket=%s", wechatLoginConfig.getShowQrCodeUrl(), ticket);
                log.info("二维码地址：{}", qrCodeUrl);
                return qrCodeUrl;
            } else {
                log.error("创建二维码失败：{}", jsonNode.get("errmsg"));
                return null;
            }

        } catch (Exception e) {
            log.error("创建二维码异常", e);
        }
        return null;
    }

    /**
     * 从微信API获取access_token并写入Redis缓存
     */
    private String fetchAndCacheAccessToken(String cacheKey) {
        try {
            String url = String.format("%s?grant_type=client_credential&appid=%s&secret=%s",
                    wechatLoginConfig.getAccessTokenUrl(),
                    wechatLoginConfig.getAppId(),
                    wechatLoginConfig.getAppSecret());

            log.info("请求微信access_token，URL：{}", url);
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            String responseBody = response.getBody();
            log.info("微信access_token响应：{}", responseBody);

            JsonNode jsonNode = objectMapper.readTree(responseBody);

            // 处理错误响应
            if (jsonNode.has("errcode")) {
                log.error("获取微信access_token失败：{}", jsonNode.get("errmsg").asText());
                return null;
            }

            String accessToken = jsonNode.get("access_token").asText();
            int expiresIn = jsonNode.get("expires_in").asInt();

            log.info("获取access_token成功，过期时间：{}秒", expiresIn);

            // 存储到Redis缓存，提前5分钟过期
            redisTemplate.opsForValue().set(cacheKey, accessToken,
                    expiresIn - 300, TimeUnit.SECONDS);

            return accessToken;

        } catch (Exception e) {
            log.error("获取微信access_token失败", e);
            return null;
        }
    }

    /**
     * 使用Lua脚本释放分布式锁
     */
    private void releaseLock(String lockKey, String lockValue) {
        try {
            DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
            redisScript.setScriptText(RELEASE_LOCK_SCRIPT);
            redisScript.setResultType(Long.class);
            Long result = redisTemplate.execute(redisScript, Collections.singletonList(lockKey), lockValue);
            if (result != null && result > 0) {
                log.debug("分布式锁释放成功, lockKey:{}", lockKey);
            } else {
                log.warn("分布式锁释放失败或已过期, lockKey:{}", lockKey);
            }
        } catch (Exception e) {
            log.error("释放分布式锁异常, lockKey:{}", lockKey, e);
        }
    }
}
