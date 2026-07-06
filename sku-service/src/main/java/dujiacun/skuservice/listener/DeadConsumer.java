package dujiacun.skuservice.listener;

import com.rabbitmq.client.Channel;
import dujiacun.skuservice.entity.MqDeadLetterMessage;
import dujiacun.skuservice.mapper.MqDeadLetterMessageMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

import static dujiacun.common.constant.RabbitMQConstant.*;

@Slf4j
@Component
public class DeadConsumer {

    @Autowired
    private MqDeadLetterMessageMapper mqDeadLetterMessageMapper;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = DEAD_ORDER_QUEUE),
            exchange = @Exchange(name = DEAD_ORDER_EXCHANGE,type = ExchangeTypes.DIRECT),
            key = DEAD_ORDER_ROUTING_KEY
    ))
    public void receive(Message message, Channel channel) throws IOException {
        String payload = new String(message.getBody());
        Map<String, Object> headers = message.getMessageProperties().getHeaders();
        Object bizType = headers.get(MQ_HEADER_BIZ_TYPE);
        Object bizId = headers.get(MQ_HEADER_BIZ_ID);

        MqDeadLetterMessage deadLetterMessage = new MqDeadLetterMessage();
        deadLetterMessage.setMessageId(message.getMessageProperties().getMessageId());
        deadLetterMessage.setBizType(bizType == null ? MQ_BIZ_TYPE_ORDER_STOCK_DEDUCT : bizType.toString());
        deadLetterMessage.setBizId(bizId == null ? payload : bizId.toString());
        deadLetterMessage.setExchangeName(message.getMessageProperties().getReceivedExchange());
        deadLetterMessage.setRoutingKey(message.getMessageProperties().getReceivedRoutingKey());
        deadLetterMessage.setQueueName(DEAD_ORDER_QUEUE);
        deadLetterMessage.setPayload(payload);
        deadLetterMessage.setHeaders(headers.toString());
        deadLetterMessage.setFailReason(MQ_DEAD_LETTER_REASON);

        mqDeadLetterMessageMapper.insertMessage(deadLetterMessage);
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        log.info("死信消息已保存,messageId={},payload={}", deadLetterMessage.getMessageId(), payload);
    }
}
