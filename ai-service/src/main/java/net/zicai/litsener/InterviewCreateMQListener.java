package net.zicai.litsener;

import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.zicai.config.InterviewEventMQConfig;
import net.zicai.dto.InterviewCreateMessageDTO;
import net.zicai.service.InterviewService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author wangdi
 * @date 2026/5/16 15:17
 * @description
 */
@Component
@Slf4j
@RequiredArgsConstructor
@RabbitListener(queues = InterviewEventMQConfig.INTERVIEW_CREATE_QUEUE)
public class InterviewCreateMQListener {

    @Autowired
    private final InterviewService interviewService;

    @RabbitHandler
    public void handleMessage(InterviewCreateMessageDTO messageDTO,
                              Message amqpMessage, Channel channel) {
        long deliveryTag = amqpMessage.getMessageProperties().getDeliveryTag();
        try {
            log.info("收到⾯试创建消息，⾯试ID：{}", messageDTO.getInterviewId());
            // 调⽤Service层⽣成⾯试内容
            interviewService.generateInterview(messageDTO);
            // ⼿动确认消息
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("处理⾯试创建消息失败", e);
            try {
                // 不重回队列，避免死循环
                channel.basicNack(deliveryTag, false, false);
            } catch (Exception ex) {
                log.error("消息NACK失败", ex);

            }
        }
    }
}