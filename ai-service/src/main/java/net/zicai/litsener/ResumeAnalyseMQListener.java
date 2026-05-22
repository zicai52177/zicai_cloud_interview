package net.zicai.litsener;

import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.zicai.config.InterviewEventMQConfig;
import net.zicai.dto.InterviewCreateMessageDTO;
import net.zicai.dto.ResumeAnalyseMessageDTO;
import net.zicai.service.ResumeAnalyseService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author wangdi
 * @date 2026/5/4 11:30
 * @description
 */

@Component
@Slf4j
@RequiredArgsConstructor
@RabbitListener(queues = InterviewEventMQConfig.RESUME_ANALYSE_QUEUE)
public class ResumeAnalyseMQListener {
    private final ResumeAnalyseService resumeAnalyseService;

    /**
     * 处理简历分析任务
     */
    @RabbitHandler
    public void handleResumeAnalyseTask(ResumeAnalyseMessageDTO messageDTO, Message message, Channel channel) {

        Long resumeId = messageDTO.getResumeId();
        Long deliveryTag = message.getMessageProperties().getDeliveryTag();
        log.info("开始处理简历分析任务，简历ID：{}", resumeId);
        try {
            boolean success = resumeAnalyseService.processResumeAnalyse(messageDTO);
            if (success) {
                channel.basicAck(deliveryTag, false);
                log.info("简历分析完成, resumeId={}", resumeId);
            } else {
                channel.basicNack(deliveryTag, false, false);
                log.warn("简历分析失败, resumeId={}", resumeId);
            }
        } catch (Exception e) {
            log.error("简历分析异常, resumeId={}", resumeId, e);
            try {
                channel.basicNack(deliveryTag, false, false);
            } catch (Exception ignored) {
            }
        }
    }
    }


