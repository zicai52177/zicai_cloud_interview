package net.zicai.util;
/**
 * @author wangdi
 * @date 2026/5/10 17:43
 * @description
 */
import com.wechat.pay.java.core.RSAPublicKeyConfig;
import com.wechat.pay.java.core.notification.NotificationParser;
import com.wechat.pay.java.core.notification.RequestParam;
import com.wechat.pay.java.service.payments.model.Transaction;
import com.wechat.pay.java.service.payments.nativepay.NativePayService;
import com.wechat.pay.java.service.payments.nativepay.model.Amount;
import com.wechat.pay.java.service.payments.nativepay.model.CloseOrderRequest;
import com.wechat.pay.java.service.payments.nativepay.model.PrepayRequest;
import com.wechat.pay.java.service.payments.nativepay.model.PrepayResponse;
import com.wechat.pay.java.service.payments.nativepay.model.QueryOrderByOutTradeNoRequest;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import net.zicai.config.WechatPayConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 微信支付工具类
 * 采用微信支付公钥验签模式（RSAPublicKeyConfig），基于 wechatpay-java SDK
 * <p>
 * config / nativePayService / notificationParser 在 Bean 初始化时构建一次，常驻内存复用，
 * 避免每次请求重复解析 RSA 密钥文件带来的性能开销。
 * </p>
 */
@Slf4j
@Component
public class WechatPayUtil {

    @Autowired
    private WechatPayConfig wechatPayConfig;

    /** 微信支付公钥模式配置，初始化后常驻 */
    private RSAPublicKeyConfig config;

    /** Native支付服务，线程安全可复用 */
    private NativePayService nativePayService;

    /** 回调通知解析器，线程安全可复用 */
    private NotificationParser notificationParser;

    /**
     * Bean 初始化完成后构建微信支付所需对象
     * WechatPayConfig#init() 已提前将 classpath 证书复制到临时目录并设置绝对路径
     * 如果微信支付未配置（isConfigured返回false），则跳过初始化
     */
    @PostConstruct
    public void init() {
        // 检查微信支付是否已配置
        if (!wechatPayConfig.isConfigured()) {
            log.warn("微信支付未配置或配置不完整，WechatPayUtil 跳过初始化");
            return;
        }

        try {
            config = new RSAPublicKeyConfig.Builder()
                    .merchantId(wechatPayConfig.getMchId())
                    .privateKeyFromPath(wechatPayConfig.getPrivateKeyAbsolutePath())
                    .publicKeyFromPath(wechatPayConfig.getPublicKeyAbsolutePath())
                    .publicKeyId(wechatPayConfig.getPublicKeyId())
                    .merchantSerialNumber(wechatPayConfig.getCertSerialNo())
                    .apiV3Key(wechatPayConfig.getApiV3Key())
                    .build();

            nativePayService = new NativePayService.Builder().config(config).build();
            notificationParser = new NotificationParser(config);

            log.info("WechatPayUtil 初始化完成，merchantId：{}", wechatPayConfig.getMchId());
        } catch (Exception e) {
            log.error("WechatPayUtil 初始化失败，merchantId：{}", wechatPayConfig.getMchId(), e);
            config = null;
            nativePayService = null;
            notificationParser = null;
        }
    }

    /**
     * 检查微信支付是否可用
     * @return true表示可用，false表示未配置或初始化失败
     */
    public boolean isAvailable() {
        return nativePayService != null && notificationParser != null;
    }

    /**
     * Native支付下单，获取二维码URL
     *
     * @param outTradeNo  商户订单号
     * @param amount      支付金额（单位：分）
     * @param description 商品描述
     * @return 二维码URL（code_url），失败返回null
     */
    public String createNativePayOrder(String outTradeNo, Long amount, String description) {
        // 检查微信支付是否可用
        if (!isAvailable()) {
            log.warn("微信支付未启用或未配置，Native支付下单失败，订单号：{}", outTradeNo);
            return null;
        }

        try {
            PrepayRequest request = new PrepayRequest();
            request.setAppid(wechatPayConfig.getAppId());
            request.setMchid(wechatPayConfig.getMchId());
            request.setDescription(description);
            request.setNotifyUrl(wechatPayConfig.getNotifyUrl());
            request.setOutTradeNo(outTradeNo);
            request.setTimeExpire(OffsetDateTime.now().plusMinutes(5).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX")));

            Amount reqAmount = new Amount();
            reqAmount.setTotal(amount.intValue());
            request.setAmount(reqAmount);

            PrepayResponse response = nativePayService.prepay(request);
            String codeUrl = response.getCodeUrl();

            log.info("Native支付下单成功，订单号：{}，codeUrl：{}", outTradeNo, codeUrl);
            return codeUrl;

        } catch (Exception e) {
            log.error("Native支付下单失败，订单号：{}", outTradeNo, e);
            return null;
        }
    }
    /**
     * 查询订单支付状态
     *
     * @param outTradeNo 商户订单号
     * @return 微信支付交易对象，查询失败返回null
     */
    public Transaction queryOrderByOutTradeNo(String outTradeNo) {
        // 检查微信支付是否可用
        if (!isAvailable()) {
            log.warn("微信支付未启用或未配置，查询订单失败，订单号：{}", outTradeNo);
            return null;
        }

        try {
            QueryOrderByOutTradeNoRequest request = new QueryOrderByOutTradeNoRequest();
            request.setOutTradeNo(outTradeNo);
            request.setMchid(wechatPayConfig.getMchId());

            Transaction transaction = nativePayService.queryOrderByOutTradeNo(request);
            log.info("查询微信支付订单成功，订单号：{}，状态：{}", outTradeNo, transaction.getTradeState());
            return transaction;

        } catch (Exception e) {
            log.error("查询微信支付订单失败，订单号：{}", outTradeNo, e);
            return null;
        }
    }



    /**
     * 关闭微信支付订单
     *
     * @param outTradeNo 商户订单号
     * @return 关闭成功返回true，失败返回false
     */
    public boolean closeOrder(String outTradeNo) {
        // 检查微信支付是否可用
        if (!isAvailable()) {
            log.warn("微信支付未启用或未配置，关闭订单失败，订单号：{}", outTradeNo);
            return false;
        }

        try {
            CloseOrderRequest request = new CloseOrderRequest();
            request.setMchid(wechatPayConfig.getMchId());
            request.setOutTradeNo(outTradeNo);

            nativePayService.closeOrder(request);
            log.info("关闭微信支付订单成功，订单号：{}", outTradeNo);
            return true;

        } catch (Exception e) {
            log.error("关闭微信支付订单失败，订单号：{}", outTradeNo, e);
            return false;
        }
    }


    /**
     * 解析微信支付回调通知
     * 使用公钥模式的 NotificationParser 验签并解密回调数据
     *
     * @param timestamp    回调请求头 Wechatpay-Timestamp
     * @param nonce        回调请求头 Wechatpay-Nonce
     * @param signature    回调请求头 Wechatpay-Signature
     * @param signType     回调请求头 Wechatpay-Signature-Type
     * @param serialNumber 回调请求头 Wechatpay-Serial
     * @param body         回调请求体
     * @return 解析后的 Transaction 交易对象，解析失败返回null
     */
    public Transaction parseCallbackNotification(String timestamp, String nonce, String signature,
                                                 String signType, String serialNumber, String body) {
        // 检查微信支付是否可用
        if (!isAvailable()) {
            log.warn("微信支付未启用或未配置，无法解析回调通知");
            return null;
        }

        try {
            RequestParam requestParam = new RequestParam.Builder()
                    .serialNumber(serialNumber)
                    .nonce(nonce)
                    .signature(signature)
                    .timestamp(timestamp)
                    .signType(signType)
                    .body(body)
                    .build();

            Transaction transaction = notificationParser.parse(requestParam, Transaction.class);
            log.info("解析微信支付回调成功，订单号：{}，状态：{}",
                    transaction.getOutTradeNo(), transaction.getTradeState());
            return transaction;

        } catch (Exception e) {
            log.error("解析微信支付回调失败", e);
            return null;
        }
    }
}