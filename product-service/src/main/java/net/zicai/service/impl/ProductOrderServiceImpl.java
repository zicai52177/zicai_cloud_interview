package net.zicai.service.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.zicai.dto.PayInfoDTO;
import net.zicai.enums.PaymentEnum;
import net.zicai.mapper.BenefitMapper;
import net.zicai.mapper.ProductOrderMapper;
import net.zicai.mapper.ProductPackageMapper;
import net.zicai.model.ProductOrderDO;
import net.zicai.service.ProductOrderService;
import net.zicai.util.JsonData;
import net.zicai.util.WechatPayUtil;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * @author wangdi
 * @date 2026/5/10 23:54
 * @description
 */
@Service
@Slf4j
@AllArgsConstructor
public class ProductOrderServiceImpl implements ProductOrderService {

    private final ProductOrderMapper productOrderMapper;

    private final ProductPackageMapper productPackageMapper;

    private final BenefitMapper benefitMapper;

    private final WechatPayUtil wechatPayUtil;

    /**
     * 根据支付方式路由到对应的支付逻辑
     */
    private JsonData handlePayment(ProductOrderDO productOrderDO, String payType, String description) {

        if(PaymentEnum.WECHAT_PAY.equals(payType)){

            return doWechatNativePay(productOrderDO, description);
        }else if(PaymentEnum.ALI_PAY.equals(payType)){
            return JsonData.buildError("不支持的支付方式：支付宝");
        }

        return JsonData.buildError("不支持的支付方式");

    }

    /**
     * 发起微信native支付，构建支付消息
     * @param productOrderDO
     * @param description
     * @return
     */
    private JsonData doWechatNativePay(ProductOrderDO productOrderDO, String description) {

        Long payAmountFen = productOrderDO.getPayAmount().multiply(new BigDecimal(100)).longValue();

        String codeUrl = wechatPayUtil.createNativePayOrder(productOrderDO.getOutTradeNo(), payAmountFen, description);

        if(codeUrl == null){

            return JsonData.buildError("微信支付异常");
        }
        PayInfoDTO build = PayInfoDTO.builder()
                .outTradeNo(productOrderDO.getOutTradeNo())
                .payAmount(payAmountFen)
                .payType(PaymentEnum.WECHAT_PAY.name())
                .codeUrl(codeUrl)
                .build();
        return JsonData.buildSuccess(build);
    }
}
