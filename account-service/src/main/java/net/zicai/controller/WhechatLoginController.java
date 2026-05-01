package net.zicai.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.zicai.config.WechatLoginConfig;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * @author 王镝
 * @date @date 20260427 21:42
 **/

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/wechat")
public class WhechatLoginController {

    private final WechatLoginConfig wechatLoginConfig;

    @GetMapping("callback/verify")
    @Operation(
            summary = "微信服务器验证接口",
            description = "微信服务器验证接口"
    )
    public String callbackVerify(@RequestParam("signature") String signature,
                                 @RequestParam("timestamp") String timestamp,
                                 @RequestParam("nonce") String nonce,
                                 @RequestParam("echostr") String echostr){
        log.info("微信服务器验证请求：signature={}, timestamp={}, nonce={}, echostr={}",
                signature, timestamp, nonce, echostr);

        // 验证签名
        if (verifySignature(signature, timestamp, nonce)) {
            log.info("微信服务器验证成功");
            return echostr;
        } else {
            log.error("微信服务器验证失败");
            return "error";
        }
    }

    /**
     * 验证微信服务器签名
     */
    private boolean verifySignature(String signature, String timestamp, String nonce) {
        try {
            // 1. 将token、timestamp、nonce三个参数进行字典序排序
            String[] arr = {wechatLoginConfig.getToken(), timestamp, nonce};
            Arrays.sort(arr);

            // 2. 将三个参数字符串拼接成一个字符串进行sha1加密
            StringBuilder sb = new StringBuilder();
            for (String s : arr) {
                sb.append(s);
            }

            // 3. 进行SHA1加密
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] digest = md.digest(sb.toString().getBytes());

            // 4. 将加密后的字节数组转换为16进制字符串
            StringBuilder hexString = new StringBuilder();
            for (byte b : digest) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            // 5. 将计算出的签名与微信发送的signature对比
            String calculatedSignature = hexString.toString();
            boolean isValid = calculatedSignature.equals(signature);

            log.info("签名验证：计算签名={}, 接收签名={}, 验证结果={}",
                    calculatedSignature, signature, isValid);

            return isValid;

        } catch (NoSuchAlgorithmException e) {
            log.error("SHA-1算法不支持", e);
            return false;
        } catch (Exception e) {
            log.error("签名验证失败", e);
            return false;
        }
    }




}
