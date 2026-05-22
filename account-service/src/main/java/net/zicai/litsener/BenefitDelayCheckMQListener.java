package net.zicai.litsener;

/**
 * @author wangdi
 * @date 2026/5/3 19:58
 * @description
 */


import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.zicai.config.BenefitDelayCheckMQConfig;
import net.zicai.dto.BenefitDelayCheckMessageDTO;
import net.zicai.service.BenefitTaskService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;


/**
 * 权益延迟检查MQ消费者
 * 监听延迟检查队列，检查业务是否完成，必要时回滚权益
 *
 * 设计说明：
 * - 由account-service负责延迟检查
 * - 检查业务是否完成（通过Feign调用ai-service）
 * - 如果业务未完成，回滚权益
 *
 * @author 小滴课堂
 */
@Component
@Slf4j
@RequiredArgsConstructor
@RabbitListener(queues = BenefitDelayCheckMQConfig.BENEFIT_DELAY_CHECK_CONSUME_QUEUE)
public class BenefitDelayCheckMQListener {

    private final BenefitTaskService benefitTaskService;

    @RabbitHandler
    public void handleDelayCheck(BenefitDelayCheckMessageDTO message, Message amqpMessage, Channel channel) {
        log.info("监听到延迟检查消息, messageId={}, businessId={}, checkLevel={}",
                message.getMessageId(), message.getBusinessId(), message.getCheckLevel());

        try {
            // 执行延迟检查（检查业务状态，必要时回滚权益）
            benefitTaskService.checkAndCompensate(message.getMessageId(), message.getCheckLevel());

            // 手动ACK
            channel.basicAck(amqpMessage.getMessageProperties().getDeliveryTag(), false);
            log.info("延迟检查消费成功, messageId={}", message.getMessageId());
        } catch (Exception e) {
            log.error("延迟检查消费失败, messageId={}", message.getMessageId(), e);
            try {
                // 拒绝消息，不重新入队（避免无限循环）
                channel.basicNack(amqpMessage.getMessageProperties().getDeliveryTag(), false, false);
            } catch (Exception ex) {
                log.error("消息拒绝失败", ex);
            }
        }
    }

    /**
     * 兜底处理：丢弃无法被Jackson反序列化的旧消息（Java原生序列化格式）
     * 当队列中残留application/x-java-serialized-object格式的消息时，
     * converter无法将其转为DTO，会降级为byte[]传入此方法
     */
    @RabbitHandler(isDefault = true)
    public void handleUnknownMessage(byte[] body, Message amqpMessage, Channel channel) {
        log.warn("收到无法解析的消息，丢弃处理, contentType={}, deliveryTag={}",
                amqpMessage.getMessageProperties().getContentType(),
                amqpMessage.getMessageProperties().getDeliveryTag());
        try {
            channel.basicAck(amqpMessage.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            log.error("丢弃消息ACK失败", e);
        }
    }
}