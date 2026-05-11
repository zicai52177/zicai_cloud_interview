package net.zicai.service;

import jakarta.validation.Valid;
import net.zicai.controller.req.BenefitOrderCreateReq;
import net.zicai.controller.req.PackageOrderCreateReq;
import net.zicai.controller.req.ProductOrderPageReq;
import net.zicai.dto.ProductOrderDTO;
import net.zicai.util.JsonData;
import org.springframework.http.ResponseEntity;

import java.util.Map;

/**
 * @author wangdi
 * @date 2026/5/10 23:54
 * @description
 */
public interface ProductOrderService {
    JsonData createBenefitOrder(@Valid BenefitOrderCreateReq req);

    JsonData createPackageOrder(@Valid PackageOrderCreateReq req);

    /**
     * 根据商户订单号查询订单支付状态
     *
     * @param outTradeNo 商户订单号
     * @return 订单DTO（含支付状态等完整信息）
     */
    ProductOrderDTO queryOrderStatus(String outTradeNo);

    /**
     * 分页查询当前用户的订单列表
     *
     * @param req 分页请求参数
     * @return 分页结果Map（records, totalRecord, totalPage, currentPage, pageSize）
     */
    Map<String, Object> page(@Valid ProductOrderPageReq req);

    ResponseEntity<String> handleWechatPayCallback(String timestamp, String nonce, String signature, String signType, String serialNumber, String body);
}
