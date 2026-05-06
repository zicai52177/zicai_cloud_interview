package net.zicai.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.zicai.config.RedisKeyManager;
import net.zicai.config.WechatLoginConfig;
import org.apache.poi.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.TimeUnit;

/**
 * @author wangdi
 * @date 2026/5/5 20:56
 * @description
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WechatServiceImpl {

    @Autowired
    private WechatLoginConfig wechatLoginConfig;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;


    public String getAccessToken() {
        try {
            // 从缓存中获取
            String cacheKey = (String) redisTemplate.opsForValue().get(String.format(RedisKeyManager.WECHAT_ACCESS_TOKEN, wechatLoginConfig.getAppId()));
            if (StringUtil.isNotBlank(cacheKey)) {
                log.info("从缓存中获取到微信访问令牌：{}", cacheKey);
                return cacheKey;
            }
            // 2. 从微信API获取
            String url = String.format("%s?grant_type=client_credential&appid=%s&secret=%s",
                    wechatLoginConfig.getAccessTokenUrl(),
                    wechatLoginConfig.getAppId(),
                    wechatLoginConfig.getAppSecret());

            log.info("请求微信access_token，URL：{}", url);
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            String responseBody = response.getBody();
            log.info("微信access_token响应：{}", responseBody);

            JsonNode jsonNode = objectMapper.readTree(responseBody);

            // 3. 处理错误响应
            if (jsonNode.has("errcode")) {
                log.error("获取微信access_token失败：{}", jsonNode.get("errmsg").asText());
                return null;
            }

            String accessToken = jsonNode.get("access_token").asText();
            int expiresIn = jsonNode.get("expires_in").asInt();

            log.info("获取access_token成功，过期时间：{}秒", expiresIn);

            // 4. 存储到Redis缓存，提前5分钟过期
            redisTemplate.opsForValue().set(RedisKeyManager.WECHAT_ACCESS_TOKEN, accessToken,
                    expiresIn - 300, TimeUnit.SECONDS);

            return accessToken;

        } catch (Exception e) {
            log.error("获取微信access_token失败", e);
            return null;
        }


    }
}
