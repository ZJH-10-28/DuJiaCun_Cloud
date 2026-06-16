package dujiacun.skuservice.listener;

import com.rabbitmq.client.Channel;
import dujiacun.common.CommonResult;
import dujiacun.common.error.ErrorCode;
import dujiacun.skuservice.service.ISkuService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static dujiacun.common.constant.RabbitMQConstant.*;

@Slf4j
@Component
public class RabbitListenerService {

    @Autowired
    private ISkuService skuService;

    @RabbitListener(queues = ORDER_QUEUE)
    public void receive(Message message, Channel channel) throws IOException {
        log.info("接收到消息：{}",message.getBody());
        log.info("开始扣减库存");
        try {
            String messageBody = new String(message.getBody());
            CommonResult<String> result = skuService.saleSkuInfo(Long.valueOf(messageBody));
            if (result.getCode() == ErrorCode.SUCCESS.getCode()){
                log.info("SKU服务扣减库存成功");
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            }
        } catch (NumberFormatException e) {
            log.error("消费失败: {}", e.getMessage());
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), false);
            throw new RuntimeException(e);
        }
        log.info("结束扣减库存");
    }
}
