package dujiacun.orderservice.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class RabbitTemplateConfig {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @PostConstruct
    public void init() {
        rabbitTemplate.setConfirmCallback(this::confirm);
        rabbitTemplate.setReturnsCallback(this::returned);
    }

    private void confirm(CorrelationData correlationData, boolean ack, String cause) {
        String messageId = correlationData == null ? "unknown" : correlationData.getId();
        if (ack) {
            log.info("RabbitMQ消息发送到交换机成功,messageId={}", messageId);
            return;
        }
        log.error("RabbitMQ消息发送到交换机失败,messageId={},cause={}", messageId, cause);
    }

    private void returned(ReturnedMessage returnedMessage) {
        log.error(
                "RabbitMQ消息路由失败,exchange={},routingKey={},replyCode={},replyText={},body={}",
                returnedMessage.getExchange(),
                returnedMessage.getRoutingKey(),
                returnedMessage.getReplyCode(),
                returnedMessage.getReplyText(),
                new String(returnedMessage.getMessage().getBody())
        );
    }
}
