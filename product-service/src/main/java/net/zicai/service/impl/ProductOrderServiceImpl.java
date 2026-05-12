package net.zicai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wechat.pay.java.service.payments.model.Transaction;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.zicai.config.BenefitGrantMQConfig;
import net.zicai.config.BenefitGrantMessage;
import net.zicai.config.ProductOrderMQConfig;
import net.zicai.controller.req.BenefitOrderCreateReq;
import net.zicai.controller.req.PackageOrderCreateReq;
import net.zicai.controller.req.ProductOrderPageReq;
import net.zicai.dto.AccountDTO;
import net.zicai.dto.PayInfoDTO;
import net.zicai.dto.ProductOrderDTO;
import net.zicai.enums.BizCodeEnum;
import net.zicai.enums.OrderStateEnum;
import net.zicai.enums.OrderTypeEnum;
import net.zicai.enums.PaymentEnum;
import net.zicai.enums.StatusEnum;
import net.zicai.exception.BizException;
import net.zicai.interceptor.AccountLoginInterceptor;
import net.zicai.mapper.BenefitMapper;
import net.zicai.mapper.PackageBenefitMapper;
import net.zicai.mapper.ProductOrderMapper;
import net.zicai.mapper.ProductPackageMapper;
import net.zicai.model.BenefitDO;
import net.zicai.model.PackageBenefitDO;
import net.zicai.model.ProductOrderDO;
import net.zicai.model.ProductPackageDO;
import net.zicai.service.ProductOrderService;
import net.zicai.util.CommonUtil;
import net.zicai.util.JsonData;
import net.zicai.util.SpringBeanUtil;
import net.zicai.util.WechatPayUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.alibaba.nacos.api.config.remote.response.ConfigPublishResponse.buildFailResponse;

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

    private final ProductOrderMQConfig productOrderMQConfig;

    private final RabbitTemplate rabbitTemplate;

    private final PackageBenefitMapper packageBenefitMapper;

    private static final String WECHAT_CALLBACK_SUCCESS = "{\"code\":\"SUCCESS\",\"message\":\"成功\"}";

    /**
     * 根据支付方式路由到对应的支付逻辑
     */
    private JsonData handlePayment(ProductOrderDO productOrderDO, String payType, String description) {

        ProductOrderDTO productOrderDTO = SpringBeanUtil.copyProperties(productOrderDO, ProductOrderDTO.class);
        rabbitTemplate.convertAndSend(productOrderMQConfig.getOrderEventExchange(),
                productOrderMQConfig.getOrderCloseDelayRoutingKey(), productOrderDTO);

        if(PaymentEnum.WECHAT_PAY.name().equals(payType)){
            return doWechatNativePay(productOrderDO, description);
        }else if(PaymentEnum.ALI_PAY.name().equals(payType)){
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

    @Override
    public void handleTimeOutOrder(ProductOrderDTO productOrderDTO) {
        String outTradeNo = productOrderDTO.getOutTradeNo();
        log.info("处理订单超时：{}", outTradeNo);
        try {
            ProductOrderDO productOrderDO = productOrderMapper.selectById(productOrderDTO.getId());
            if(productOrderDO == null){
                log.error("订单不存在：{}", outTradeNo);
                return;
            }
            //判断状态是否已经支付
            if(OrderStateEnum.PAY.name().equals(productOrderDO.getOrderState())){
                log.info("订单已支付：{}", outTradeNo);
                return;
            }
            if (OrderStateEnum.CANCEL.name().equals(productOrderDO.getOrderState())){
                log.info("订单已取消：{}", outTradeNo);
                return;
            }
            //查询订单状态
            if(PaymentEnum.WECHAT_PAY.name().equals(productOrderDO.getPayType())){
                //调用微信支付的方法解决问题
                handleWechatTimeOutOrder(productOrderDO);
            }

        }catch (Exception e){
            log.error("处理订单超时异常：{}", outTradeNo, e);
        }


    }

    private void handleWechatTimeOutOrder(ProductOrderDO productOrderDO) {

        Transaction transaction = wechatPayUtil.queryOrderByOutTradeNo(productOrderDO.getOutTradeNo());

        if(transaction == null){
            log.error("订单不存在：{}", productOrderDO.getOutTradeNo());
            return;
        }
        if(transaction.getTradeState().equals(Transaction.TradeStateEnum.SUCCESS)){
            log.warn("回调异常补偿机制，订单已支付，但是数据库未更新，订单号：{}", productOrderDO.getOutTradeNo());
            int updated = updateOrderState(productOrderDO.getOutTradeNo(), transaction.getTransactionId(), OrderStateEnum.PAY.name());
            if(updated > 0){
                log.info("订单已支付，更新数据库成功，订单号：{}", productOrderDO.getOutTradeNo());
            }else {
                log.error("订单已支付，更新数据库失败，订单号：{}", productOrderDO.getOutTradeNo());
            }
        }else if(Transaction.TradeStateEnum.USERPAYING.equals(transaction.getTradeState())){
            log.warn("回调异常补偿机制，订单正在支付，但是数据库未更新，订单号：{}", productOrderDO.getOutTradeNo());
        }else {
            log.warn("订单已关闭，更新订单状态为已取消，订单号：{},订单状态：{}", productOrderDO.getOutTradeNo(), transaction.getTradeState());
            cancleOrder(productOrderDO);
        }

    }

    private void cancleOrder(ProductOrderDO productOrderDO) {
        wechatPayUtil.closeOrder(productOrderDO.getOutTradeNo());
        productOrderDO.setOrderState(OrderStateEnum.CANCEL.name());
        productOrderMapper.updateById(productOrderDO);
        log.info("订单已取消，更新数据库成功，订单号：{}", productOrderDO.getOutTradeNo());
    }

    @Override
    public JsonData createBenefitOrder(BenefitOrderCreateReq req) {

    //查询对应群益是否存在
        BenefitDO benefitDO = benefitMapper.selectById(req.getBenefitId());
        if(benefitDO == null){
            return JsonData.buildError("权益不存在");
        }
        if (!benefitDO.getStatus().equals(StatusEnum.ON.name())){
            return JsonData.buildError("权益已下架");
        }

        //构建价格
        BigDecimal payAmount = benefitDO.getUnitPrice()
                .multiply(new BigDecimal(req.getPorchaseCount()))
                .multiply(req.getDiscount());

        ProductOrderDO build = ProductOrderDO.builder()
                .outTradeNo("BFT" + CommonUtil.getCurrentTimestamp() + CommonUtil.generateStringNumRandom(6))
                .accountId(getCurrentAccountId())
                .orderType(OrderTypeEnum.BENEFIT_ORDER.name())
                .orderState(OrderStateEnum.NEW.name())
                .title(benefitDO.getName())
                .benefitId(req.getBenefitId())
                .purchaseCount(req.getPorchaseCount())
                .unitPrice(benefitDO.getUnitPrice())
                .discount(req.getDiscount())
                .payAmount(payAmount)
                .payType(PaymentEnum.valueOf(req.getPayType()).name())
                .build();

        productOrderMapper.insert(build);
        log.info("订单创建成功: {}", build);
        return handlePayment(build, req.getPayType(), benefitDO.getName());

    }

    @Override
    public JsonData createPackageOrder(PackageOrderCreateReq req) {
        //查询对应群益是否存在
        ProductPackageDO productPackageDO = productPackageMapper.selectById(req.getPackageId());
        if(productPackageDO == null){
            return JsonData.buildError("权益不存在");
        }
        if (!productPackageDO.getStatus().equals(StatusEnum.ON.name())){
            return JsonData.buildError("权益已下架");
        }

        //构建价格
        BigDecimal payAmount = productPackageDO.getPrice()
                .multiply(new BigDecimal(1))
                .multiply(req.getDiscount());

        ProductOrderDO build = ProductOrderDO.builder()
                .outTradeNo("BFT" + CommonUtil.getCurrentTimestamp() + CommonUtil.generateStringNumRandom(6))
                .accountId(getCurrentAccountId())
                .orderType(OrderTypeEnum.BENEFIT_ORDER.name())
                .orderState(OrderStateEnum.NEW.name())
                .title(productPackageDO.getName())
                .benefitId(req.getPackageId())
                .purchaseCount(1)
                .unitPrice(productPackageDO.getPrice())
                .discount(req.getDiscount())
                .payAmount(payAmount)
                .payType(PaymentEnum.valueOf(req.getPayType()).name())
                .build();

        productOrderMapper.insert(build);
        log.info("订单创建成功: {}", build);
        return handlePayment(build, req.getPayType(), productPackageDO.getName());

    }

    private Long getCurrentAccountId() {

        AccountDTO accountDTO = AccountLoginInterceptor.threadLocal.get();
        return accountDTO.getId();
    }

    @Override
    public ProductOrderDTO queryOrderStatus(String outTradeNo) {
        log.info("查询订单支付状态, outTradeNo:{}", outTradeNo);

        // 仅查询当前登录账号的订单，防止越权
        Long accountId = getCurrentAccountId();
        LambdaQueryWrapper<ProductOrderDO> queryWrapper = new LambdaQueryWrapper<ProductOrderDO>()
                .eq(ProductOrderDO::getOutTradeNo, outTradeNo)
                .eq(ProductOrderDO::getAccountId, accountId);

        ProductOrderDO productOrderDO = productOrderMapper.selectOne(queryWrapper);
        if (productOrderDO == null) {
            log.warn("订单不存在, outTradeNo:{}, accountId:{}", outTradeNo, accountId);
            throw new BizException(BizCodeEnum.ORDER_NO_EXIST);
        }

        log.info("查询订单成功, outTradeNo:{}, orderState:{}", outTradeNo, productOrderDO.getOrderState());
        return SpringBeanUtil.copyProperties(productOrderDO, ProductOrderDTO.class);
    }

    @Override
    public Map<String, Object> page(ProductOrderPageReq req) {
        // 1. 获取当前登录用户ID，强制按用户维度过滤，防止越权
        Long accountId = getCurrentAccountId();
        log.info("分页查询用户订单, accountId:{}, page:{}, size:{}, orderState:{}, orderType:{}",
                accountId, req.getPage(), req.getSize(), req.getOrderState(), req.getOrderType());

        // 2. 构建查询条件（动态条件 + 固定按当前用户过滤）
        LambdaQueryWrapper<ProductOrderDO> queryWrapper = new LambdaQueryWrapper<ProductOrderDO>()
                .eq(ProductOrderDO::getAccountId, accountId)
                .eq(StringUtils.isNotBlank(req.getOrderState()),
                        ProductOrderDO::getOrderState, req.getOrderState())
                .eq(StringUtils.isNotBlank(req.getOrderType()),
                        ProductOrderDO::getOrderType, req.getOrderType())
                .orderByDesc(ProductOrderDO::getGmtCreate);

        // 3. 分页查询
        Page<ProductOrderDO> page = new Page<>(req.getPage(), req.getSize());
        Page<ProductOrderDO> resultPage = productOrderMapper.selectPage(page, queryWrapper);

        // 4. DO转DTO
        List<ProductOrderDTO> dtoList = resultPage.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        log.info("分页查询用户订单完成, accountId:{}, total:{}", accountId, resultPage.getTotal());
        return CommonUtil.convertToPageMap(resultPage, dtoList);
    }

    /**
     * 微信支付回调
     * @param timestamp
     * @param nonce
     * @param signature
     * @param signType
     * @param serialNumber
     * @param body
     * @return
     */
    @Override
    public ResponseEntity<String> handleWechatPayCallback(String timestamp, String nonce, String signature, String signType, String serialNumber, String body) {

        Transaction transaction = wechatPayUtil.parseCallbackNotification(
                timestamp, nonce, signature, signType, serialNumber, body);
        if (transaction == null) {
            log.error("解析微信支付回调失败");
            return ResponseEntity.status(500).body("解析回调失败");
        }
        log.info("解析微信支付回调成功，订单号：{}，状态：{}",
                transaction.getOutTradeNo(), transaction.getTradeState());
        //查询订单
        ProductOrderDO productOrderDO = productOrderMapper.selectOne(
                new LambdaQueryWrapper<ProductOrderDO>()
                        .eq(ProductOrderDO::getOutTradeNo, transaction.getOutTradeNo())
        );
        if (productOrderDO == null) {
            log.error("订单不存在，订单号：{}", transaction.getOutTradeNo());
            return ResponseEntity.ok().body("订单不存在");
        }
        if(Transaction.TradeStateEnum.SUCCESS.equals(transaction.getTradeState())){

            //检查数据库是否更新
            if (productOrderDO.getOrderState().equals(OrderStateEnum.PAY.name())){
                log.info("订单已支付，订单号：{}", transaction.getOutTradeNo());
                return ResponseEntity.ok().body("订单已支付");
            }
            //更新数据库
            Integer update = updateOrderState(transaction.getOutTradeNo(), transaction.getTransactionId(), OrderStateEnum.PAY.name());
            if (update == null || update == 0){
                log.error("更新订单状态失败，订单号：{}", transaction.getOutTradeNo());
                return ResponseEntity.status(500).body("更新订单状态失败");
            }else {
                log.info("更新订单状态成功，订单号：{}", transaction.getOutTradeNo());
                return ResponseEntity.ok().body("更新订单状态成功");
            }
        }else if(!OrderStateEnum.PAY.name().equals(productOrderDO.getOrderState())){
            //更新订单为CANCLE
            Integer update = updateOrderState(transaction.getOutTradeNo(), transaction.getTransactionId(), OrderStateEnum.CANCEL.name());
            if (update == null || update == 0){
                log.error("更新订单状态失败，订单号：{}", transaction.getOutTradeNo());
                return ResponseEntity.status(500).body("更新订单状态失败");
            }else {
                log.info("订单未支付成功，订单号：{}", transaction.getOutTradeNo());
                return ResponseEntity.ok().body("订单未支付成功");
            }
        }
        return ResponseEntity.ok().body("处理成功");
    }

    private Integer updateOrderState(String outTradeNo, String transactionId, String newState) {
        //查询订单状态
        ProductOrderDO productOrderDO = productOrderMapper.selectOne(
                new LambdaQueryWrapper<ProductOrderDO>()
                        .eq(ProductOrderDO::getOutTradeNo, outTradeNo)
        );
        if (productOrderDO == null) {
            log.error("订单不存在，订单号：{}", outTradeNo);
            return 0;
        }
        if(newState.equals(productOrderDO.getOrderState())){
            log.info("订单{}已经是{}，跳过", outTradeNo, newState);
            return 0;
        }
        int updated = productOrderMapper.update(null,
                new LambdaUpdateWrapper<ProductOrderDO>()
                        .set(ProductOrderDO::getTransactionNo, transactionId)
                        .set(ProductOrderDO::getOrderState, newState)
                        .set(OrderStateEnum.PAY.name().equals(newState), ProductOrderDO::getNotifyTime, new Date())
                        .eq(ProductOrderDO::getOutTradeNo, outTradeNo)
                        .notIn(ProductOrderDO::getOrderState, newState)); // 排除目标状态，防止重复更新

        if (updated > 0) {
            log.info("订单状态已更新，订单号：{}，新状态：{}", outTradeNo, newState);
            return updated;
        } else {
            log.warn("订单状态更新失败（可能已被其他线程更新），订单号：{}，目标状态：{}", outTradeNo, newState);
            return 0;
        }
    }

    /**
     * DO转DTO
     *
     * @param productOrderDO 订单实体
     * @return ProductOrderDTO
     */
    private ProductOrderDTO convertToDTO(ProductOrderDO productOrderDO) {
        return SpringBeanUtil.copyProperties(productOrderDO, ProductOrderDTO.class);
    }

    /**
     * 处理权益订单发放
     * @param orderDO
     */
    private void processSingleBenefit(ProductOrderDO orderDO) {
        // 1. 根据 benefitId 查询权益基础信息（validDays 等）
        BenefitDO benefitDO = benefitMapper.selectById(orderDO.getBenefitId());
        if (benefitDO == null) {
            log.error("权益不存在，发放失败, benefitId:{}", orderDO.getBenefitId());
            return;
        }

        // 2. 构建单条 BenefitItem
        BenefitGrantMessage.BenefitItem item = BenefitGrantMessage.BenefitItem.builder()
                .benefitId(benefitDO.getId())
                .benefitCode(benefitDO.getBenefitCode())
                .count(orderDO.getPurchaseCount())      // 购买数量
                .validDays(benefitDO.getValidDays())    // 单次有效天数
                .build();

        // 3. 构建消息并发送
        BenefitGrantMessage message = BenefitGrantMessage.builder()
                .outTradeNo(orderDO.getOutTradeNo())
                .accountId(orderDO.getAccountId())
                .orderId(orderDO.getId())
                .orderType(orderDO.getOrderType())
                .benefitItems(List.of(item))
                .build();

        rabbitTemplate.convertAndSend(
                BenefitGrantMQConfig.BENEFIT_GRANT_EXCHANGE,
                BenefitGrantMQConfig.BENEFIT_GRANT_ROUTING_KEY,
                message
        );
        log.info("单权益发放消息已发送, outTradeNo:{}, benefitId:{}", orderDO.getOutTradeNo(), benefitDO.getId());
    }

    /**
     * 处理套餐订单发放
     * @param orderDO
     */
    private void processPackageBenefits(ProductOrderDO orderDO) {
        // 1. 查询套餐下的所有权益配置
        List<PackageBenefitDO> packageBenefitList = packageBenefitMapper.selectList(
                new LambdaQueryWrapper<PackageBenefitDO>()
                        .eq(PackageBenefitDO::getProductPackageId, orderDO.getProductPackageId())
        );

        if (packageBenefitList == null || packageBenefitList.isEmpty()) {
            log.error("套餐下无权益配置，发放失败, productPackageId:{}", orderDO.getProductPackageId());
            return;
        }

        // 2. 批量查出所有权益的详细信息（validDays 等）
        List<String> benefitCodes = packageBenefitList.stream()
                .map(PackageBenefitDO::getBenefitCode)
                .collect(Collectors.toList());
        Map<String, BenefitDO> benefitMap = benefitMapper.selectList(
                        new LambdaQueryWrapper<BenefitDO>().in(BenefitDO::getBenefitCode, benefitCodes))
                .stream()
                .collect(Collectors.toMap(BenefitDO::getBenefitCode, b -> b));

        // 3. 构建 BenefitItem 列表（每个套餐权益对应一条）
        List<BenefitGrantMessage.BenefitItem> benefitItems = packageBenefitList.stream()
                .map(packageBenefit -> {
                    BenefitDO benefitDO = benefitMap.get(packageBenefit.getBenefitCode());
                    return BenefitGrantMessage.BenefitItem.builder()
                            .benefitId(benefitDO != null ? benefitDO.getId() : null)
                            .benefitCode(packageBenefit.getBenefitCode())
                            .count(packageBenefit.getCount())
                            .validDays(benefitDO != null ? benefitDO.getValidDays() : null)
                            .build();
                })
                .collect(Collectors.toList());

        // 4. 只发一条消息（携带完整权益列表）
        BenefitGrantMessage message = BenefitGrantMessage.builder()
                .outTradeNo(orderDO.getOutTradeNo())
                .accountId(orderDO.getAccountId())
                .orderId(orderDO.getId())
                .orderType(orderDO.getOrderType())
                .benefitItems(benefitItems)
                .build();

        rabbitTemplate.convertAndSend(
                BenefitGrantMQConfig.BENEFIT_GRANT_EXCHANGE,
                BenefitGrantMQConfig.BENEFIT_GRANT_ROUTING_KEY,
                message
        );
        log.info("套餐权益发放消息已发送, outTradeNo:{}, 权益数量:{}", orderDO.getOutTradeNo(), benefitItems.size());
    }

    /**
     * 处理订单权益发放
     * @param outTradeNo
     */
    private void processOrderBenefits(String outTradeNo) {
        try {
            // 1. 查询订单
            ProductOrderDO productOrderDO = productOrderMapper.selectOne(
                    new LambdaQueryWrapper<ProductOrderDO>()
                            .eq(ProductOrderDO::getOutTradeNo, outTradeNo)
            );

            // 2. 订单校验：不存在或未支付则跳过
            if (productOrderDO == null || !OrderStateEnum.PAY.name().equals(productOrderDO.getOrderState())) {
                log.warn("权益发放跳过，订单不存在或未支付, outTradeNo:{}", outTradeNo);
                return;
            }

            // 3. 按订单类型分发
            if (OrderTypeEnum.PACKAGE_ORDER.name().equals(productOrderDO.getOrderType())) {
                processPackageBenefits(productOrderDO);
            } else if (OrderTypeEnum.BENEFIT_ORDER.name().equals(productOrderDO.getOrderType())) {
                processSingleBenefit(productOrderDO);
            }
        } catch (Exception e) {
            log.error("权益发放消息发送失败, outTradeNo:{}", outTradeNo, e);
        }
    }
}
