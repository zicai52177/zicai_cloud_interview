package net.zicai.controller;

import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.zicai.DTO.AccountLoginResultDTO;
import net.zicai.controller.req.AccountLoginReq;
import net.zicai.controller.req.SendCheckCodeReq;
import net.zicai.dto.AccountDTO;
import net.zicai.enums.BizCodeEnum;
import net.zicai.exception.BizException;
import net.zicai.service.AccountService;
import net.zicai.util.CaptchaUtil;
import net.zicai.util.JsonData;
import net.zicai.util.JwtUtil;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    private final AccountService accountService;
    /**
     * 获取图形验证码
     */
    @GetMapping(value = "/captcha" , produces = MediaType.IMAGE_JPEG_VALUE)
    public byte[] captcha(@RequestParam(value = "identifier",required = true) String identifier,
                          @RequestParam(value = "type",required = true) String type) {
        return captchaUtil.generateCaptchaImage(identifier, type);
    }

    /**
     * 发送短信验证码
     */
    @PostMapping("send_check_code")
    @Operation(summary = "发送短信验证码",
            description = "需要先输入图形验证码,然后发送短信验证码用于登录或注册")
    public JsonData sendCheckCode(
            @Parameter(description = "发送短信请求参数", required = true)
            @Valid @RequestBody SendCheckCodeReq req) {
        return accountService.sendCheckCode(req);
    }

    /**
     *  登录接口
     */
    @PostMapping("login")
    @Operation(
            summary = "租户账号登录",
            description = "通过手机号和短信验证码登录，返回JWT Token和用户信息"
    )
    public JsonData login(
            @Parameter(description = "登录请求参数", required = true)
            @RequestBody AccountLoginReq req) {
        AccountLoginResultDTO result = accountService.login(req);
        return JsonData.buildSuccess(result);
    }

    /**
     * 查询个人信息
     */
    @GetMapping("detail")
    @Operation(summary = "查询个人信息", description = "根据Token查询当前登录用户信息")
    public JsonData detail() {
        AccountDTO accountDTO = accountService.findById();
        return JsonData.buildSuccess(accountDTO);
    }

    /**
     * 上传用户头像
     */
    @PostMapping(value = "/avatar",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "上传头像",
            description = "上传头像接口，返回可访问Url"
    )
    public JsonData avatar(
            @Parameter(description = "上传文件", required = true)
            @RequestPart("file") MultipartFile file) {
        return accountService.uploadAvatar(file);
    }

}

