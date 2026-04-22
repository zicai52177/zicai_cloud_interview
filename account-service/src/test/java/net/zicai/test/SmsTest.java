package net.zicai.test;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.zicai.config.SmsClient;
import net.zicai.dto.SmsDTO;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 短信发送测试类
 * 使用 common 模块封装的 SmsClient 发送短信
 *
 * @author 王镝
 * @date 2026-04-15
 */
@Slf4j
@SpringBootTest
public class SmsTest {

    @Resource
    private SmsClient smsClient;

    /**
     * 测试发送短信
     */
    @Test
    public void testSendSms() {
        // 目标手机号
        String mobile = "18241862212";

        // 短信模板参数
        // **code**: 验证码, **minute**: 有效期（分钟）
        String param = "**code**:521,**minute**:5";

        // 构建短信DTO
        SmsDTO smsDTO = SmsDTO.builder()
                .mobile(mobile)
                .param(param)
                .build();

        // 发送短信
        String result = smsClient.sendSms(smsDTO);

        // 输出结果
        if (result != null) {
            log.info("短信发送成功，响应结果: {}", result);
        } else {
            log.error("短信发送失败");
        }
    }

    /**
     * 测试发送验证码短信（便捷方法）
     */
    @Test
    public void testSendVerifyCode() {
        String mobile = "18241862212";
        String code = "123456";
        int minute = 5;

        String param = String.format("**code**:%s,**minute**:%d", code, minute);
        SmsDTO smsDTO = SmsDTO.builder()
                .mobile(mobile)
                .param(param)
                .build();

        String result = smsClient.sendSms(smsDTO);
        log.info("验证码短信发送结果: {}", result);
    }
}
