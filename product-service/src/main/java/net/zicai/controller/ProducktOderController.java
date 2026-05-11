package net.zicai.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.zicai.controller.req.BenefitOrderCreateReq;
import net.zicai.controller.req.PackageOrderCreateReq;
import net.zicai.controller.req.ProductOrderPageReq;
import net.zicai.dto.ProductOrderDTO;
import net.zicai.service.ProductOrderService;
import net.zicai.util.JsonData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @author wangdi
 * @date 2026/5/10 23:52
 * @description
 */
@RestController
@Slf4j
@RequestMapping("/api/v1/order")
public class ProducktOderController {

    @Autowired
    private ProductOrderService productOrderService;

    /**
     * 创建权益订单接口
     */
    @Operation(summary = "创建权益订单", description = "创建权益购买订单并返回支付信息（含微信支付二维码URL）")
    @PostMapping("/benefit/create")
    public JsonData createBenefitOrder(@Valid @RequestBody BenefitOrderCreateReq req) {
        log.info("创建权益订单");
        return productOrderService.createBenefitOrder(req);
    }

    /**
     * 创建套餐订单接口
     * @param req
     * @return
     */
    @Operation(summary = "创建套餐订单", description = "创建套餐购买订单并返回支付信息（含微信支付二维码URL）")
    @PostMapping("/package/create")
    public JsonData createPackageOrder(@Valid @RequestBody PackageOrderCreateReq req) {
        log.info("创建套餐订单");
        return productOrderService.createPackageOrder(req);
    }

    /**
     * 查询订单支付状态接口
     *
     * @param outTradeNo 商户订单号
     * @return 订单DTO（含支付状态、金额、回调时间等完整信息）
     */
    @Operation(summary = "查询订单支付状态", description = "根据商户订单号查询订单的完整信息（含支付状态orderState）")
    @GetMapping("/status")
    public JsonData queryOrderStatus(
            @Parameter(description = "商户订单号", required = true)
            @RequestParam("outTradeNo") String outTradeNo) {
        log.info("查询订单支付状态请求, outTradeNo:{}", outTradeNo);
        ProductOrderDTO orderDTO = productOrderService.queryOrderStatus(outTradeNo);
        return JsonData.buildSuccess(orderDTO);
    }

    /**
     * 分页查询当前用户订单列表
     *
     * @param req 分页请求参数
     * @return 分页结果（records, totalRecord, totalPage, currentPage, pageSize）
     */
    @Operation(summary = "分页查询当前用户订单列表", description = "根据登录态获取当前用户ID，按订单状态、类型进行分页查询")
    @PostMapping("/page")
    public JsonData page(@Valid @RequestBody ProductOrderPageReq req) {
        log.info("分页查询当前用户订单请求, page:{}, size:{}", req.getPage(), req.getSize());
        Map<String, Object> pageResult = productOrderService.page(req);
        return JsonData.buildSuccess(pageResult);
    }

    /**
     * 微信支付回调通知
     * 微信支付成功后回调此接口，更新订单状态
     *
     * <p>注意：此接口需在拦截器中配置为白名单，不需要登录验证</p>
     *
     * <p>微信支付回调规范：
     * <ul>
     *   <li>验签通过：返回 HTTP 200/204，无需返回应答报文</li>
     *   <li>验签不通过：返回 HTTP 5XX/4XX，并返回应答报文 {"code":"FAIL","message":"..."}</li>
     * </ul>
     */
    @Operation(summary = "微信支付回调", description = "微信支付Native支付结果通知，自动验签并更新订单状态")
    @PostMapping("/callback/wechat_pay")
    public ResponseEntity<String> wechatPayCallback(
            @RequestHeader(value = "Wechatpay-Timestamp", required = false) String timestamp,
            @RequestHeader(value = "Wechatpay-Nonce", required = false) String nonce,
            @RequestHeader(value = "Wechatpay-Signature", required = false) String signature,
            @RequestHeader(value = "Wechatpay-Signature-Type", required = false) String signType,
            @RequestHeader(value = "Wechatpay-Serial", required = false) String serialNumber,
            @RequestBody String body) {

        log.info("收到微信支付回调，timestamp：{}，serial：{}", timestamp, serialNumber);
        log.info("收到微信支付回调，body：{}", body);

        return productOrderService.handleWechatPayCallback(
                timestamp, nonce, signature, signType, serialNumber, body);
    }
}
