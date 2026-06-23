package dujiacun.skuservice.listener;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static dujiacun.common.constant.RabbitMQConstant.*;

@Slf4j
@Component
public class deadConsumer {

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = DEAD_ORDER_QUEUE),
            exchange = @Exchange(name = DEAD_ORDER_EXCHANGE,type = ExchangeTypes.DIRECT),
            key = DEAD_ORDER_ROUTING_KEY
    ))
    public void receive(Message message, Channel  channel) throws IOException {
        log.info("死信队列接收到消息：{}",message);
        //TODO 消息存到数据库中
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }
}
