package dujiacun.orderservice.config;

import dujiacun.orderservice.mapper.MqMessageMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static dujiacun.common.constant.RabbitMQConstant.*;

@Slf4j
@Configuration
public class RabbitTemplateConfig {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private MqMessageMapper mqMessageMapper;

    @PostConstruct
    public void init() {
        rabbitTemplate.setConfirmCallback(this::confirm);
        rabbitTemplate.setReturnsCallback(this::returned);
    }

    @Bean
    public Queue orderRollbackQueue() {
        // Redis回滚补偿消息需要持久化队列,避免服务重启造成补偿消息丢失。
        return new Queue(ORDER_ROLLBACK_QUEUE, true);
    }

    @Bean
    public Exchange orderRollbackExchange() {
        // 回滚补偿使用独立交换机,避免和订单库存扣减消息混用路由。
        return new DirectExchange(ORDER_ROLLBACK_EXCHANGE, true, false);
    }

    @Bean
    public Binding orderRollbackBinding() {
        // 将回滚队列绑定到回滚路由键,消费者只处理Redis库存回滚消息。
        return BindingBuilder.bind(orderRollbackQueue())
                .to(orderRollbackExchange())
                .with(ROLLBACK_ROUTING_KEY)
                .noargs();
    }

    private void confirm(CorrelationData correlationData, boolean ack, String cause) {
        String messageId = correlationData == null ? "unknown" : correlationData.getId();
        if (ack) {
            log.info("RabbitMQ消息发送到交换机成功,messageId={}", messageId);
            mqMessageMapper.updateStatus(messageId, MQ_STATUS_SENT, null, MQ_STATUS_INIT);
            return;
        }
        log.error("RabbitMQ消息发送到交换机失败,messageId={},cause={}", messageId, cause);
        mqMessageMapper.updateStatus(messageId, MQ_STATUS_FAILED, cause, null);
    }

    private void returned(ReturnedMessage returnedMessage) {
        String messageId = returnedMessage.getMessage().getMessageProperties().getMessageId();
        if (messageId == null || messageId.isBlank()) {
            messageId = MQ_MESSAGE_ID_ORDER_PREFIX + new String(returnedMessage.getMessage().getBody());
        }
        log.error(
                "RabbitMQ消息路由失败,exchange={},routingKey={},replyCode={},replyText={},body={}",
                returnedMessage.getExchange(),
                returnedMessage.getRoutingKey(),
                returnedMessage.getReplyCode(),
                returnedMessage.getReplyText(),
                new String(returnedMessage.getMessage().getBody())
        );
        mqMessageMapper.updateStatus(messageId, MQ_STATUS_FAILED, returnedMessage.getReplyText(), null);
    }
}
