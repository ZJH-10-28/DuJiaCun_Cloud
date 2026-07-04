package dujiacun.orderservice.config;

import dujiacun.orderservice.mapper.MqMessageMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import static dujiacun.common.constant.RabbitMQConstant.MQ_MESSAGE_ID_ORDER_PREFIX;
import static dujiacun.common.constant.RabbitMQConstant.MQ_STATUS_FAILED;
import static dujiacun.common.constant.RabbitMQConstant.MQ_STATUS_SENT;

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

    private void confirm(CorrelationData correlationData, boolean ack, String cause) {
        String messageId = correlationData == null ? "unknown" : correlationData.getId();
        if (ack) {
            log.info("RabbitMQ消息发送到交换机成功,messageId={}", messageId);
            mqMessageMapper.updateStatus(messageId, MQ_STATUS_SENT, null);
            return;
        }
        log.error("RabbitMQ消息发送到交换机失败,messageId={},cause={}", messageId, cause);
        mqMessageMapper.updateStatus(messageId, MQ_STATUS_FAILED, cause);
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
        mqMessageMapper.updateStatus(messageId, MQ_STATUS_FAILED, returnedMessage.getReplyText());
    }
}
