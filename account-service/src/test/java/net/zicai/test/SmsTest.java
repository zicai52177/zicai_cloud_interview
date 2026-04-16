package net.zicai.test;

import net.zicai.util.HttpUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * 短信发送测试类
 * 使用阿里云市场 - 国阳云短信服务
 *
 * @author 王镝
 * @date 2026-04-15
 */
@Slf4j
public class SmsTest {

    /**
     * 短信服务域名
     */
    private static final String HOST = "https://gyytz.market.alicloudapi.com";

    /**
     * 短信发送接口路径
     */
    private static final String PATH = "/sms/smsSend";

    /**
     * 阿里云 AppCode
     * 请替换为你的真实 AppCode
     */
    private static final String APPCODE = "551504ffe65b492cadbe7297a9cc669e";

    /**
     * 短信签名ID
     * 可登录国阳云控制台自助申请
     * 参考文档：http://help.guoyangyun.com/Problem/Qm.html
     */
    private static final String SMS_SIGN_ID = "2e65b1bb3d054466b82f0c9d125465e2";

    /**
     * 短信模板ID
     * 可登录国阳云控制台自助申请
     */
    private static final String TEMPLATE_ID = "908e94ccf08b4476ba6c876d13f084ad";

    public static void main(String[] args) {
        // 目标手机号
        String mobile = "18241862212";

        // 短信模板参数
        // **code**: 验证码, **minute**: 有效期（分钟）
        String param = "**code**:521,**minute**:5";

        // 发送短信
        String result = sendSms(mobile, param);

        // 输出结果
        if (result != null) {
            log.info("短信发送成功，响应结果: {}", result);
        } else {
            log.error("短信发送失败");
        }
    }

    /**
     * 发送短信
     *
     * @param mobile 目标手机号
     * @param param  模板参数
     * @return 响应内容
     */
    public static String sendSms(String mobile, String param) {
        // 1. 构建请求头
        Map<String, String> headers = new HashMap<>();
        // Authorization 格式：APPCODE + 空格 + AppCode
        headers.put("Authorization", "APPCODE " + APPCODE);

        // 2. 构建请求参数
        Map<String, Object> params = new HashMap<>();
        params.put("mobile", mobile);
        params.put("param", param);
        params.put("smsSignId", SMS_SIGN_ID);
        params.put("templateId", TEMPLATE_ID);

        // 3. 构建完整URL
        String url = HOST + PATH;

        // 4. 发送 POST 表单请求
        log.info("开始发送短信，手机号: {}", mobile);
        String response = HttpUtil.postForm(url, headers, params);

        return response;
    }

    /**
     * 发送验证码短信（便捷方法）
     *
     * @param mobile 目标手机号
     * @param code   验证码
     * @param minute 有效期（分钟）
     * @return 响应内容
     */
    public static String sendVerifyCode(String mobile, String code, int minute) {
        String param = String.format("**code**:%s,**minute**:%d", code, minute);
        return sendSms(mobile, param);
    }
}
