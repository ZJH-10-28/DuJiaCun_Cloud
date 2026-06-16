package dujiacun.common.util;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;

import java.io.IOException;

import static dujiacun.common.constant.RabbitMQConstant.*;

public class RabbitRetryUtil {

    public static int getRetryCount(Message message) {
        Integer retryCount = 0;
        if (message.getMessageProperties().getHeaders().containsKey(STR_RETRY_COUNT)) {
            retryCount = (Integer) message.getMessageProperties().getHeaders().get(STR_RETRY_COUNT);
        }
        return retryCount == null ? 0 : retryCount;
    }

    public static void retryMessage(Message message, Channel channel, int retryCount) throws IOException {

        message.getMessageProperties().getHeaders().put(STR_RETRY_COUNT, retryCount);

        AMQP.BasicProperties props = new AMQP.BasicProperties.Builder()
                .deliveryMode(2) //消息持久化 1:存内存 2:存磁盘
                .expiration("60000") // 过期时间 ms
                .headers(message.getMessageProperties().getHeaders())
                .build();

        //发送新消息
        channel.basicPublish("", //不使用交换机,避免交换机故障丢失消息
                message.getMessageProperties().getConsumerQueue(),
                props,
                message.getBody());

        //确认原消息
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }
}
