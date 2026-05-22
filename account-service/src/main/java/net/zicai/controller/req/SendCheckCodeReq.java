package net.zicai.controller.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 发送验证码请求参数
 */
@Data
@Schema(description = "发送验证码请求参数")
public class SendCheckCodeReq {

    @NotBlank(message = "手机号或邮箱不能为空")
    @Schema(description = "手机号或邮箱", example = "13800138000")
    private String identifier;

    @NotBlank(message = "图形验证码不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9]{4}$", message = "图形验证码格式不正确")
    @Schema(description = "图形验证码", example = "8264")
    private String captcha;

    @NotBlank(message = "验证码类型不能为空")
    @Pattern(regexp = "^(LOGIN|REGISTER|RESET_PASSWORD)$", message = "验证码类型只能是 LOGIN、REGISTER 或 RESET_PASSWORD")
    @Schema(description = "验证码类型: LOGIN-登录, REGISTER-注册, RESET_PASSWORD-重置密码", example = "LOGIN")
    private String type;
}
