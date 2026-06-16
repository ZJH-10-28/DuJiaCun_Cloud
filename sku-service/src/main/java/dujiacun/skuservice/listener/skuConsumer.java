package dujiacun.skuservice.listener;

import com.rabbitmq.client.Channel;
import dujiacun.common.CommonResult;
import dujiacun.common.error.ErrorCode;
import dujiacun.common.exception.BusinessException;
import dujiacun.common.util.RabbitRetryUtil;
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
public class skuConsumer {

    @Autowired
    private ISkuService skuService;

    @RabbitListener(queues = ORDER_QUEUE)
    public void receive(Message message, Channel channel) throws IOException {
        log.info("接收到消息：{}",message.getBody());
        log.info("开始扣减库存");
        int retryCount = RabbitRetryUtil.getRetryCount(message);
        try {
            String messageBody = new String(message.getBody());
            CommonResult<String> result = skuService.saleSkuInfo(Long.valueOf(messageBody));
            if (result.getCode() != ErrorCode.SUCCESS.getCode()){
                throw new BusinessException("扣减库存失败");
            }
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            log.info("SKU服务扣减库存成功");
        } catch (Exception e) {
            if (retryCount < ORDER_MAX_RETRY_COUNT) {
                // 重试：更新重试次数，延迟后重新入队
                log.info("第{}次重试", retryCount + 1);
                RabbitRetryUtil.retryMessage(message, channel, retryCount + 1);
            } else {
                log.error("消费失败: {}", e.getMessage());
                channel.basicReject(message.getMessageProperties().getDeliveryTag(), false);
            }
        }
        log.info("结束扣减库存");
    }
}
