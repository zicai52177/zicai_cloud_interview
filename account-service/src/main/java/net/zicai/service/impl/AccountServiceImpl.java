package net.zicai.service.impl;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import net.zicai.DTO.AccountLoginResultDTO;
import net.zicai.config.RedisKeyManager;
import net.zicai.config.SmsClient;
import net.zicai.controller.req.AccountLoginReq;
import net.zicai.controller.req.SendCheckCodeReq;
import net.zicai.dto.AccountDTO;
import net.zicai.dto.SmsDTO;
import net.zicai.enums.AccountRoleEnum;
import net.zicai.enums.BizCodeEnum;
import net.zicai.enums.StatusEnum;
import net.zicai.exception.BizException;
import net.zicai.mapper.AccountMapper;
import net.zicai.model.AccountDO;
import net.zicai.service.AccountService;
import net.zicai.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author 王镝
 * @date 20260416
 **/

@Service
@Slf4j
public class AccountServiceImpl implements AccountService {

    private final static int CHECK_CODE_EXPIRED_TIME = 10;

    @Autowired
    private CaptchaUtil captchaUtil;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private SmsClient smsClient;
    @Autowired
    private AccountMapper accountMapper;

    @Override
    public JsonData sendCheckCode(SendCheckCodeReq req) {
        log.info("发送验证码请求, identifier:{}, type:{}", req.getIdentifier(), req.getType());

        // 1. 校验图形验证码
        captchaUtil.verifyCaptcha(req.getIdentifier(), req.getCaptcha(), req.getType());

        // 2. 判断是否60秒内重复发送
        String cacheKey = String.format(RedisKeyManager.CHECK_CODE_KEY, req.getType(),req.getIdentifier());
        String cacheValue = stringRedisTemplate.opsForValue().get(cacheKey);
        if (StringUtils.isNotBlank(cacheValue)) {
            String[] parts = cacheValue.split("_");
            if (parts.length >= 2) {
                long sendTime = Long.parseLong(parts[1]);
                // 当前时间 - 验证码发送时间小于60秒不能发送
                if ((CommonUtil.getCurrentTimestamp() - sendTime) < 60 * 1000) {
                    log.warn("验证码发送过于频繁, identifier:{}", req.getIdentifier());
                    return JsonData.buildResult(BizCodeEnum.CODE_LIMITED);
                }
            }
        }

        // 3. 生成6位数字验证码
        String smsCode = CommonUtil.generateNumberCode();
        String value = smsCode + "_" + CommonUtil.getCurrentTimestamp();

        // 4. 根据标识类型发送验证码
        if (CommonUtil.isEmail(req.getIdentifier())) {
            return sendEmailCheckCode(req.getIdentifier(), smsCode, cacheKey, value);
        } else if (CommonUtil.isPhone(req.getIdentifier())) {
            return sendSmsCheckCode(req.getIdentifier(), smsCode, cacheKey, value);
        }

        log.error("接收号码格式不正确, identifier:{}", req.getIdentifier());
        return JsonData.buildResult(BizCodeEnum.CODE_TO_ERROR);
    }

    /**
     * 1 验证短信验证码
     * 2 查询用户唯一标识是否存在数据库
     * 生成token
     * @param req
     * @return
     */
    @Override
    public AccountLoginResultDTO login(AccountLoginReq req) {

        //校验短信验证码
        boolean verifyResult = verifyIdentifierCode(req.getIdentifier(),req.getCheckCode(),req.getType());
        if (!verifyResult) {
            throw new BizException(BizCodeEnum.SMS_CODE_ERROR);

        }

        //查询数据库
        AccountDO accountDO = accountMapper.selectOne(new LambdaQueryWrapper<AccountDO>()
                .eq(AccountDO::getStatus, StatusEnum.ON)
                .eq(AccountDO::getEmail,req.getIdentifier()).or().eq(AccountDO::getPhone,req.getIdentifier()));
        if (accountDO == null) {
            //用户不存在，注册
            boolean isPhone = CommonUtil.isPhone(req.getIdentifier());
            AccountDO build = AccountDO.builder().role(AccountRoleEnum.COMMON.name()).username("user_"+req.getIdentifier()).build();
            if (isPhone) {
                build.setPhone(req.getIdentifier());
            }else{
                build.setEmail(req.getIdentifier());
            }
            accountMapper.insert(build);
            log.info("用户注册成功：{}", build);
        }

        AccountDTO accountDTO = SpringBeanUtil.copyProperties(accountDO, AccountDTO.class);
        //生成token
        String loginJWT = JwtUtil.geneTenantAccountLoginJWT(accountDTO);

        return AccountLoginResultDTO.builder().success(true).token(loginJWT).accountDTO(accountDTO).build();
    }

    private boolean verifyIdentifierCode(String identifier, String checkCode, String type) {
        String cacheKey = String.format(RedisKeyManager.CHECK_CODE_KEY, type, identifier);
        String cacheValue = stringRedisTemplate.opsForValue().get(cacheKey);
        if (StringUtils.isNotBlank(cacheValue)) {
            String[] parts = cacheValue.split("_");
            if (parts.length >= 2) {
                String cacheCode = parts[0];
                if (cacheCode.equals(checkCode)) {
                    stringRedisTemplate.delete(cacheKey);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 发送短信验证码
     */
    private JsonData sendSmsCheckCode(String phone, String smsCode, String cacheKey, String cacheValue) {
        try {
            log.info("发送短信验证码, phone:{}", phone);
            SmsDTO smsDTO = SmsDTO.builder()
                    .mobile(phone)
                    .param("**code**:" + smsCode + ",**minute**:" + CHECK_CODE_EXPIRED_TIME)
                    .build();

            String sendSms = smsClient.sendSms(smsDTO);
            log.info("[发送短信] phone: {}, resp: {}", phone, sendSms);

            // 解析短信接口响应
            Map<String, Object> responseMap = JsonUtil.json2Obj(sendSms, Map.class);
            String code = (String) responseMap.get("code");
            String msg = (String) responseMap.get("msg");

            // 处理不同业务状态码
            if ("0".equals(code)) {
                // 发送成功，存储验证码到Redis
                stringRedisTemplate.opsForValue().set(cacheKey, cacheValue, CHECK_CODE_EXPIRED_TIME, TimeUnit.MINUTES);
                log.info("短信验证码发送成功, phone:{}", phone);
                return JsonData.buildSuccess("短信验证码已发送，请查收");
            }

            // 根据短信服务商返回的状态码返回相应错误
            return handleSmsErrorCode(code, msg);

        } catch (Exception e) {
            log.error("短信发送异常, phone:{}", phone, e);
            return JsonData.buildResult(BizCodeEnum.SMS_SEND_ERROR);
        }
    }

    /**
     * 处理短信服务商错误码
     */
    private JsonData handleSmsErrorCode(String code, String msg) {
        log.error("短信发送失败, code:{}, msg:{}", code, msg);

        switch (code) {
            case "-1":
                // 参数错误
                return JsonData.buildResult(BizCodeEnum.SMS_PARAM_ERROR);
            case "-2":
                // 账号密码错误
                return JsonData.buildResult(BizCodeEnum.SMS_ACCOUNT_ERROR);
            case "-3":
                // 余额不足
                return JsonData.buildResult(BizCodeEnum.SMS_BALANCE_NOT_ENOUGH);
            case "-4":
                // 手机号格式错误
                return JsonData.buildResult(BizCodeEnum.CODE_TO_ERROR);
            case "-5":
                // 内容含有敏感词
                return JsonData.buildResult(BizCodeEnum.SMS_CONTENT_SENSITIVE);
            case "-6":
                // 内容过长
                return JsonData.buildResult(BizCodeEnum.SMS_CONTENT_TOO_LONG);
            default:
                return JsonData.buildResult(BizCodeEnum.SMS_SEND_ERROR);
        }
    }

    /**
     * 发送邮箱验证码（预留方法）
     */
    private JsonData sendEmailCheckCode(String email, String emailCode, String cacheKey, String cacheValue) {
        // TODO: 实现邮箱验证码发送逻辑
        log.warn("邮箱验证码功能暂未实现, email:{}", email);
        return JsonData.buildResult(BizCodeEnum.SYSTEM_ERROR);
    }
}
