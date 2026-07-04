package dujiacun.skuservice.listener;

import com.rabbitmq.client.Channel;
import dujiacun.common.util.RabbitRetryUtil;
import dujiacun.skuservice.service.MqOrderMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static dujiacun.common.constant.RabbitMQConstant.*;

@Slf4j
@Component
public class skuConsumer {

    @Autowired
    private MqOrderMessageService mqOrderMessageService;

    @RabbitListener(queues = ORDER_QUEUE)
    public void receive(Message message, Channel channel) throws IOException {
        log.info("接收到消息：{}",message.getBody());
        log.info("开始扣减库存");
        int retryCount = RabbitRetryUtil.getRetryCount(message);
        String messageBody = new String(message.getBody());
        String messageId = message.getMessageProperties().getMessageId();
        if (messageId == null || messageId.isBlank()) {
            messageId = MQ_MESSAGE_ID_ORDER_PREFIX + messageBody;
        }

        try {
            Long orderId = Long.valueOf(messageBody);
            boolean consumed = mqOrderMessageService.consumeOrderStockDeduct(messageId, orderId);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            if (consumed) {
                log.info("SKU库存扣减成功,orderId={},messageId={}", orderId, messageId);
            } else {
                log.info("重复消息已忽略,orderId={},messageId={}", orderId, messageId);
            }
        } catch (Exception e) {
            if (retryCount < ORDER_MAX_RETRY_COUNT) {
                // 重试：更新重试次数，延迟后重新入队
                log.info("SKU库存扣减失败,准备第{}次重试,messageId={}", retryCount + 1, messageId, e);
                RabbitRetryUtil.retryMessage(message, channel, retryCount + 1);
            } else {
                log.error("SKU库存扣减最终失败,messageId={},body={}", messageId, messageBody, e);
                channel.basicReject(message.getMessageProperties().getDeliveryTag(), false);
            }
        }
        log.info("结束扣减库存");
    }
}
