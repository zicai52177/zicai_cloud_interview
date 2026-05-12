package net.zicai.listener;

import com.rabbitmq.client.Channel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.zicai.config.ProductOrderMQConfig;
import net.zicai.dto.ProductOrderDTO;
import net.zicai.service.ProductOrderService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;


/**
 * @author wangdi
 * @date 2026/5/12 17:30
 * @description
 */
@Component
@Slf4j
@RabbitListener(queues = ProductOrderMQConfig.orderCloseQueue)
@AllArgsConstructor
public class ProductOrderMQListener {

    private final ProductOrderService productOrderService;


    @RabbitHandler
    public void productOrderHandler(ProductOrderDTO productOrderDTO, Message message, Channel channel){

        log.info("订单关闭监听：{}", productOrderDTO.getProductPackageId());

        try {
            productOrderService.handleTimeOutOrder(productOrderDTO);

            log.info("订单关闭处理成功：{}", productOrderDTO.getProductPackageId());

            channel.basicAck(message.getMessageProperties().getDeliveryTag(),  false);
        }catch (Exception e){
            log.error("订单关闭处理失败：{}", productOrderDTO.getProductPackageId(), e);
            try {
                //拒绝消息，不重新入队
                channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);

            }catch (Exception e1){

                log.error("拒绝消息处理失败：{}", productOrderDTO.getProductPackageId(), e1);
            }

        }

    }



}
