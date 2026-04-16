package net.zicai.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import net.zicai.util.CaptchaUtil;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author 王镝
 * @date 2026 04 15
 **/

@RestController
@RequestMapping("/api/v1/account")
@RequiredArgsConstructor
@Tag(name = "账号服务" ,description = "账号服务接口")
public class AccountController {

    private final CaptchaUtil captchaUtil;

    /**
     * 获取图形验证码
     */
    @GetMapping(value = "/captcha" , produces = MediaType.IMAGE_JPEG_VALUE)
    public byte[] captcha(@RequestParam(value = "identifier",required = true) String identifier,
                          @RequestParam(value = "type",required = true) String type) {
        return captchaUtil.generateCaptchaImage(identifier, type);
    }

}

