package net.zicai.biz;

/**
 * @author wangdi
 * @date 2026/5/10 22:31
 * @description
 */

import com.wechat.pay.java.service.payments.model.Transaction;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.slf4j.Slf4j;
import net.zicai.ProductServiceApplication;
import net.zicai.config.WechatPayConfig;
import net.zicai.util.WechatPayUtil;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * WechatPayUtil 集成测试（@SpringBootTest 完整上下文）
 * <p>
 * 使用 @SpringBootTest 启动完整 Spring Boot 应用上下文，
 * 通过 @BeforeAll 在上下文启动前加载 .env 文件解析占位符，
 * 激活 dev profile 使用 application-dev.yml 配置。
 * </p>
 * 测试顺序：先下单 → 查询 → 关闭订单，复用同一 outTradeNo
 */
@Slf4j
@SpringBootTest(classes = ProductServiceApplication.class)
@ActiveProfiles("dev")
//控制测试方法的执行顺序，测试方法将按照它们身上 @Order(n) 注解中定义的数值（n）从小到大依次执行
//测试逻辑存在明显的先后依赖关系（例如：必须先下单才能查询，必须先生成订单才能关闭）
//如果没有这个类级别的注解，JUnit 5 默认不会保证方法的执行顺
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("WechatPayUtil 集成测试（真实请求）")
class WechatPayUtilTest {

    /**
     * @SpringBootTest 的上下文初始化由 SpringExtension 在测试实例创建前触发，
     * 但 @BeforeAll 是静态方法，JUnit 在任何 Extension 回调之前就会先执行它，
     * 因此可以在这里提前将 .env 注入系统属性供 Spring 占位符解析使用。
     */
    @BeforeAll
    static void loadEnv() {
        Dotenv dotenv = Dotenv.configure()
                .directory("src/main/resources")
                .ignoreIfMissing()
                .load();
        dotenv.entries().forEach(entry -> {
            if (System.getProperty(entry.getKey()) == null) {
                System.setProperty(entry.getKey(), entry.getValue());
            }
        });
        log.info("=== .env 加载完成，注入 {} 个环境变量 ===", dotenv.entries().size());
    }

    @Autowired
    private WechatPayUtil wechatPayUtil;

    @Autowired
    private WechatPayConfig wechatPayConfig;

    /** 同一批测试共用同一个订单号，便于下单→查询→关闭流转 */
    private static final String OUT_TRADE_NO =
            "TEST1772519429100"  ;

    /** 测试金额：1 分（最小单位），避免真实扣款 */
    private static final Long AMOUNT_FEN = 1L;

    private static final String DESCRIPTION = "WechatPayUtil集成测试订单";

    // ==================== 基础配置验证 ====================

    @Test
    @Order(1)
    @DisplayName("配置加载验证：关键配置项不为空")
    void testConfigLoaded() {
        log.info("商户号：{}", wechatPayConfig.getMchId());
        log.info("AppId：{}", wechatPayConfig.getAppId());
        log.info("公钥ID：{}", wechatPayConfig.getPublicKeyId());
        log.info("私钥绝对路径：{}", wechatPayConfig.getPrivateKeyAbsolutePath());
        log.info("公钥绝对路径：{}", wechatPayConfig.getPublicKeyAbsolutePath());

        assertThat(wechatPayConfig.getMchId()).isNotBlank();
        assertThat(wechatPayConfig.getAppId()).isNotBlank();
        assertThat(wechatPayConfig.getCertSerialNo()).isNotBlank();
        assertThat(wechatPayConfig.getApiV3Key()).isNotBlank();
        assertThat(wechatPayConfig.getPublicKeyId()).isNotBlank();
        assertThat(wechatPayConfig.getPrivateKeyAbsolutePath()).isNotBlank();
        assertThat(wechatPayConfig.getPublicKeyAbsolutePath()).isNotBlank();
    }

    // ==================== Native 下单 ====================

    @Test
    @Order(2)
    @DisplayName("Native下单：向微信支付发起真实下单请求，返回 codeUrl")
    void testNativePayOrder() {
        log.info("发起 Native 下单，订单号：{}，金额：{} 分", OUT_TRADE_NO, AMOUNT_FEN);

        String codeUrl = wechatPayUtil.createNativePayOrder(OUT_TRADE_NO, AMOUNT_FEN, DESCRIPTION);

        log.info("Native下单结果，codeUrl：{}", codeUrl);

        // 微信 Native 支付的 codeUrl 以 weixin:// 开头
        assertThat(codeUrl)
                .as("下单成功，codeUrl 不应为空且以 weixin:// 开头")
                .isNotNull()
                .startsWith("weixin://");
    }

    // ==================== 查询订单 ====================

    @Test
    @Order(3)
    @DisplayName("查询订单：查询刚创建的订单，状态应为 NOTPAY（尚未扫码支付）")
    void testQueryOrder() {
        // 先确保订单存在（若 Order(2) 先执行则已创建；否则此处补充创建）
        String existingCodeUrl = wechatPayUtil.createNativePayOrder(
                OUT_TRADE_NO, AMOUNT_FEN, DESCRIPTION + "_query");

        log.info("查询订单，订单号：{}", OUT_TRADE_NO);
        Transaction transaction = wechatPayUtil.queryOrderByOutTradeNo(OUT_TRADE_NO);

        log.info("查询结果：outTradeNo={}, tradeState={}",
                transaction != null ? transaction.getOutTradeNo() : "null",
                transaction != null ? transaction.getTradeState() : "null");

        assertThat(transaction).as("查询结果不应为 null").isNotNull();
        assertThat(transaction.getOutTradeNo()).isEqualTo(OUT_TRADE_NO);
        // 刚创建的订单未支付，状态应为 NOTPAY
        assertThat(transaction.getTradeState())
                .as("未扫码支付的订单状态应为 NOTPAY")
                .isEqualTo(Transaction.TradeStateEnum.NOTPAY);
    }

    // ==================== 关闭订单 ====================

    @Test
    @Order(4)
    @DisplayName("关闭订单：关闭已创建但未支付的订单，应返回 true")
    void testCloseOrder() {
        // 若前序测试已创建订单则直接关闭；否则先创建再关闭
        //wechatPayUtil.createNativePayOrder(OUT_TRADE_NO, AMOUNT_FEN, DESCRIPTION + "_close");

        log.info("关闭订单，订单号：{}", OUT_TRADE_NO);
        boolean closed = wechatPayUtil.closeOrder(OUT_TRADE_NO);

        log.info("关闭订单结果：{}", closed);
        assertThat(closed).as("关闭未支付订单应成功，返回 true").isTrue();
    }

    @Test
    @Order(5)
    @DisplayName("关闭后查询：订单关闭后状态应变为 CLOSED")
    void testQueryOrderAfterClose() {
        // 确保订单已关闭（Order(4) 先执行的情况下已关闭）
        wechatPayUtil.closeOrder(OUT_TRADE_NO);

        log.info("关闭后查询，订单号：{}", OUT_TRADE_NO);
        Transaction transaction = wechatPayUtil.queryOrderByOutTradeNo(OUT_TRADE_NO);

        log.info("关闭后查询结果：tradeState={}",
                transaction != null ? transaction.getTradeState() : "null");

        assertThat(transaction).isNotNull();
        assertThat(transaction.getTradeState())
                .as("已关闭订单状态应为 CLOSED")
                .isEqualTo(Transaction.TradeStateEnum.CLOSED);
    }
}