package dujiacun.skuservice.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static dujiacun.common.constant.RabbitMQConstant.*;

@Configuration
public class RabbitMQConfig {

    @Bean
    public Queue orderQueue() {
        return QueueBuilder.durable(ORDER_QUEUE)
                //指定死信队列
                .deadLetterExchange(DEAD_ORDER_EXCHANGE)
                .deadLetterRoutingKey(DEAD_ORDER_ROUTING_KEY)
//                .ttl(5000)
                .build();
    }

    @Bean
    public Exchange orderExchange() {
        return new DirectExchange(ORDER_EXCHANGE);
    }

    @Bean
    public Binding orderBinding() {
        return BindingBuilder.bind(orderQueue())
                .to(orderExchange())
                .with(ROUTING_KEY).noargs();
    }


    // 死信队列
    @Bean
    public Queue deadQueue() {
        return new Queue(DEAD_ORDER_QUEUE, true); // true 持久化
    }

    @Bean
    public Exchange deadExchange() {
        return new DirectExchange(DEAD_ORDER_EXCHANGE);
    }

    @Bean
    public Binding deadBinding() {
        return BindingBuilder.bind(deadQueue())
                .to(deadExchange())
                .with(DEAD_ORDER_ROUTING_KEY).noargs();
    }
}
