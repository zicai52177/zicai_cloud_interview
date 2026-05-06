package net.zicai.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.zicai.config.WechatLoginConfig;
import net.zicai.service.WechatService;
import net.zicai.util.JsonData;
import net.zicai.util.WechatUtil;
import org.springframework.web.bind.annotation.*;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Map;

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

    private final WechatService wechatService;

    @GetMapping("qrcode")
    @Operation(
            summary = "获取微信登录二维码",
            description = "获取微信登录二维码"
    )
    public JsonData getQrcode() {
        JsonData result = JsonData.buildSuccess(wechatService.generateQrcode());
        log.info("微信登录二维码URL：{}", result);
        return result;
    }

    /**
     * 微信事件推送回调
     * 处理用户关注、取消关注、扫码等事件
     */
    @PostMapping("/callback/verify")
    @Operation(summary = "微信事件推送回调", description = "处理微信用户事件推送")
    @ResponseBody //将返回值转换成JSON，如果返回值是String或者其他基本数据类型则不满足key-value形式，不能转换成json类型，则返回字符串
    public String handleEventCallback(HttpServletRequest request, HttpServletResponse response) {
        try {
            // 读取请求体
            String requestBody = WechatUtil.readRequestBody(request);
            log.info("收到微信事件推送：{}", requestBody);

            // 解析XML数据
            Map<String, String> eventData = WechatUtil.parseXmlData(requestBody);

            String eventType = eventData.get("Event");
            String openid = eventData.get("FromUserName");
            String sceneId = eventData.get("EventKey");

            log.info("事件类型：{}，用户openid：{}，场景ID：{}", eventType, openid, sceneId);

            switch (eventType) {
                case "SCAN":
                    // 用户扫码事件（已关注用户扫码）
                    log.info("处理已关注用户扫码事件：sceneId={}, openid={}", sceneId, openid);
                    wechatService.handleScanCallback(sceneId, openid);
                    break;
                case "subscribe":
                    // 用户关注事件
                    log.info("处理用户关注事件：sceneId={}, openid={}", sceneId, openid);
                    if (sceneId != null && sceneId.startsWith("qrscene_")) {
                        // 扫码关注，去掉"qrscene_"前缀 <EventKey><![CDATA[qrscene_8bcd7eee2dc54bd3b65ce88b4d1fc551]]></EventKey>
                        sceneId = sceneId.substring(8);
                        log.info("扫码关注，处理后sceneId：{}", sceneId);
                        wechatService.handleScanCallback(sceneId, openid);
                    } else {
                        log.info("普通关注事件，无场景ID：openid={}", openid);
                    }
                    break;
                case "unsubscribe":
                    // 用户取消关注事件
                    log.info("用户取消关注：{}", openid);
                    break;
                default:
                    log.info("未处理的事件类型：{}，openid：{}", eventType, openid);
                    break;
            }

            return "success";

        } catch (Exception e) {
            log.error("处理微信事件推送失败", e);
            return "error";
        }
    }
    @GetMapping("login/result")
    @Operation(
            summary = "微信登录结果",
            description = "微信登录结果"
    )
    public JsonData loginResult(@RequestParam("sceneId") String sceneId) {
        log.info("微信登录结果：sceneId={}", sceneId);
        return JsonData.buildSuccess(wechatService.getLoginResult(sceneId));
    }


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
