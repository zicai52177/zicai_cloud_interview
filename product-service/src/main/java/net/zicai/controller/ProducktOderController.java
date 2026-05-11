package net.zicai.controller;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.zicai.service.ProductOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    @RequestMapping("/benefit/create")

}
