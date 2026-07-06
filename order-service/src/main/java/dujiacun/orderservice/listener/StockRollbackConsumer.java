package dujiacun.orderservice.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import dujiacun.common.util.RabbitRetryUtil;
import dujiacun.orderservice.entity.SkuStock;
import dujiacun.orderservice.entity.dto.StockRollbackPayload;
import dujiacun.orderservice.mapper.MqMessageMapper;
import dujiacun.orderservice.service.IOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

import static dujiacun.common.constant.RabbitMQConstant.*;

@Slf4j
@Component
public class StockRollbackConsumer {

    private final IOrderService orderService;

    private final MqMessageMapper mqMessageMapper;

    private final ObjectMapper objectMapper;

    public StockRollbackConsumer(IOrderService orderService, MqMessageMapper mqMessageMapper) {
        this.orderService = orderService;
        this.mqMessageMapper = mqMessageMapper;
        this.objectMapper = new ObjectMapper();
    }

    @RabbitListener(queues = ORDER_ROLLBACK_QUEUE)
    public void receive(Message message, Channel channel) throws IOException {
        String messageBody = new String(message.getBody());
        int retryCount = RabbitRetryUtil.getRetryCount(message);
        String messageId = message.getMessageProperties().getMessageId();

        try {
            StockRollbackPayload payload = objectMapper.readValue(messageBody, StockRollbackPayload.class);
            messageId = resolveMessageId(message, payload.getRollbackId());
            // MQ补偿和同步回滚共用同一个rollbackId,避免重复投递导致Redis库存重复加回。
            orderService.rollbackStock(convertSkuStockList(payload), payload.getRollbackId());
            mqMessageMapper.updateStatus(messageId, MQ_STATUS_CONSUMED, null, null);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            log.info("Redis库存MQ回滚成功,messageId={},rollbackId={}", messageId, payload.getRollbackId());
        } catch (Exception e) {
            if (messageId != null && !messageId.isBlank()) {
                mqMessageMapper.updateStatus(messageId, MQ_STATUS_FAILED, e.getMessage(), null);
            }
            if (retryCount < ORDER_MAX_RETRY_COUNT) {
                // 回滚失败时按现有RabbitMQ工具进行有限次数重试。
                log.info("Redis库存MQ回滚失败,准备第{}次重试,messageId={}", retryCount + 1, messageId, e);
                RabbitRetryUtil.retryMessage(message, channel, retryCount + 1);
            } else {
                log.error("Redis库存MQ回滚最终失败,messageId={},body={}", messageId, messageBody, e);
                channel.basicReject(message.getMessageProperties().getDeliveryTag(), false);
            }
        }
    }

    private String resolveMessageId(Message message, String rollbackId) {
        String messageId = message.getMessageProperties().getMessageId();
        if (messageId == null || messageId.isBlank()) {
            // 重试消息如果缺少messageId,使用rollbackId恢复同一条可靠消息ID。
            return MQ_MESSAGE_ID_ROLLBACK_PREFIX + rollbackId;
        }
        return messageId;
    }

    private List<SkuStock> convertSkuStockList(StockRollbackPayload payload) {
        return payload.getSkuStockList().stream().map(this::convertSkuStock).toList();
    }

    private SkuStock convertSkuStock(StockRollbackPayload.RollbackSkuStock rollbackSkuStock) {
        SkuStock skuStock = new SkuStock();
        skuStock.setSkuId(rollbackSkuStock.getSkuId());
        skuStock.setSaleCount(rollbackSkuStock.getSaleCount());
        return skuStock;
    }
}
