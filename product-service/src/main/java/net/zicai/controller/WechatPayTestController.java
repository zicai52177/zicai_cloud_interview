package net.zicai.controller;

import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.RSAPublicKeyConfig;

import com.wechat.pay.java.service.payments.nativepay.NativePayService;
import com.wechat.pay.java.service.payments.nativepay.model.Amount;
import com.wechat.pay.java.service.payments.nativepay.model.PrepayRequest;
import com.wechat.pay.java.service.payments.nativepay.model.PrepayResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import net.zicai.config.WechatPayConfig;
import net.zicai.util.JsonData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @author wangdi
 * @date 2026/5/7 20:29
 * @description
 */
@RestController
@Slf4j
@RequestMapping("/api/v1/pay/test/")
public class WechatPayTestController {

    @Autowired
    private WechatPayConfig wechatPayConfig;

    /**
     * 微信支付测试接口
     */
    /**
     * 测试Native支付下单
     * 使用微信支付v3公钥验签模式
     */
    @Operation(summary = "测试Native支付", description = "测试微信支付Native支付下单功能")
    @GetMapping("/native")
    public JsonData testNativePay(
            @RequestParam(value = "description", defaultValue = "测试商品") String description,
            @RequestParam(value = "amount", defaultValue = "1") int amount) {

        log.info("测试Native支付, description:{}, amount:{}", description, amount);

        try {
            // 构建微信支付v3公钥验签模式的配置
            Config config = new RSAPublicKeyConfig.Builder()
                    .merchantId(wechatPayConfig.getMchId())
                    .privateKeyFromPath(wechatPayConfig.getPrivateKeyAbsolutePath())
                    .publicKeyFromPath(wechatPayConfig.getPublicKeyAbsolutePath())
                    .publicKeyId(wechatPayConfig.getPublicKeyId())
                    .merchantSerialNumber(wechatPayConfig.getCertSerialNo())
                    .apiV3Key(wechatPayConfig.getApiV3Key())
                    .build();

            // 创建NativePayService
            NativePayService service = new NativePayService.Builder().config(config).build();

            // 构建预支付请求
            PrepayRequest request = new PrepayRequest();
            request.setAppid(wechatPayConfig.getAppId());
            request.setMchid(wechatPayConfig.getMchId());
            request.setDescription(description);
            request.setNotifyUrl(wechatPayConfig.getNotifyUrl());
            // 生成商户订单号（实际项目中应使用更完善的生成策略）
            String outTradeNo = "TEST" + System.currentTimeMillis();
            request.setOutTradeNo(outTradeNo);

            // 设置金额（单位为分）
            Amount reqAmount = new Amount();
            reqAmount.setTotal(amount);
            request.setAmount(reqAmount);

            // 调用预支付接口
            PrepayResponse response = service.prepay(request);

            log.info("Native支付下单成功, outTradeNo:{}, codeUrl:{}", outTradeNo, response.getCodeUrl());

            // 返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("outTradeNo", outTradeNo);
            result.put("codeUrl", response.getCodeUrl());
            result.put("description", description);
            result.put("amount", amount);

            return JsonData.buildSuccess(result);

        } catch (Exception e) {
            log.error("Native支付测试失败", e);
            return JsonData.buildError("支付测试失败: " + e.getMessage());
        }
    }

}
