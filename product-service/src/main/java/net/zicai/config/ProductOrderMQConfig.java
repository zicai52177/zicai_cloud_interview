package net.zicai.config;

/**
 * @author wangdi
 * @date 2026/5/11 20:30
 * @description
 */
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@Slf4j
@Data
public class ProductOrderMQConfig {

    // 订单交换机
    private String orderEventExchange = "order.event.exchange";

    // 延迟队列（不能被消费者监听）
    private String orderCloseDelayQueue = "order.close.delay.queue";

    // 死信队列（被消费者监听）
    public static final String orderCloseQueue = "order.close.queue";

    // 延迟队列路由key
    private String orderCloseDelayRoutingKey = "order.close.delay.routing.key";

    // 死信队列路由key
    private String orderCloseRoutingKey = "order.close.delay.key";

    // 过期时间：1分钟
    private Integer ttl = 1000 * 60 ;

    /**
     * 配置 JSON 消息转换器，替代默认的 Java 原生序列化
     * SpringAMQP3.x默认使用 Java 原生序列化时，会校验反序列化白名单，ProductOrderDTO 不在白名单中导致反序列化失败。
     * 最佳解决方案是配置 Jackson2JsonMessageConverter，改用 JSON 序列化替代 Java 原生序列化。
     */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * 配置 RabbitTemplate，使用 JSON 消息转换器
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter());
        return rabbitTemplate;
    }

    /**
     * 创建交换机
     */
    @Bean
    public TopicExchange orderEventExchange() {
        return new TopicExchange(orderEventExchange, true, false);
    }

    /**
     * 延迟队列
     */
    @Bean
    public Queue orderCloseDelayQueue() {
        Map<String, Object> args = new HashMap<>(3);
        // 消息过期后转发的交换机
        args.put("x-dead-letter-exchange", orderEventExchange);
        // 消息过期后转发的路由key
        args.put("x-dead-letter-routing-key", orderCloseRoutingKey);
        // 消息过期时间
        args.put("x-message-ttl", ttl);
        return new Queue(orderCloseDelayQueue, true, false, false, args);
    }

    /**
     * 死信队列（普通队列，被监听）
     */
    @Bean
    public Queue orderCloseQueue() {
        return new Queue(orderCloseQueue, true, false, false);
    }

    /**
     * 延迟队列绑定
     */
    @Bean
    public Binding orderCloseDelayBinding() {
        return BindingBuilder.bind(orderCloseDelayQueue())
                .to(orderEventExchange())
                .with(orderCloseDelayRoutingKey);
    }

    /**
     * 死信队列绑定
     */
    @Bean
    public Binding orderCloseBinding() {
        return BindingBuilder.bind(orderCloseQueue())
                .to(orderEventExchange())
                .with(orderCloseRoutingKey);
    }
}