package dujiacun.orderservice.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class RabbitMQConfig {

    public static final String ORDER_EXCHANGE = "order.exchange";
    public static final String ORDER_QUEUE = "order.queue";
    public static final String ROUTING_KEY = "order.routingKey.success";

    @Bean
    public Queue orderQueue() {
        return new Queue(ORDER_QUEUE, true); // true 表示持久化
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
}
