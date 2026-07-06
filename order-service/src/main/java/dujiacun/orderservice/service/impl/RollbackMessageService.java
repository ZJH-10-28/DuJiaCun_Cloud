package dujiacun.orderservice.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dujiacun.common.exception.BusinessException;
import dujiacun.orderservice.entity.MqMessage;
import dujiacun.orderservice.entity.SkuStock;
import dujiacun.orderservice.entity.bo.OrderParamBo;
import dujiacun.orderservice.entity.dto.StockRollbackPayload;
import dujiacun.orderservice.mapper.MqMessageMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

import static dujiacun.common.constant.RabbitMQConstant.*;

@Slf4j
@Service
public class RollbackMessageService {

    private static final int DEFAULT_MAX_RETRY_COUNT = 5;

    private static final int MAX_FAIL_REASON_LENGTH = 1000;

    private static final Set<String> CAN_RESEND_STATUS = Set.of(MQ_STATUS_INIT, MQ_STATUS_FAILED);

    private final MqMessageMapper mqMessageMapper;

    private final RabbitTemplate rabbitTemplate;

    private final ObjectMapper objectMapper;

    public RollbackMessageService(MqMessageMapper mqMessageMapper, RabbitTemplate rabbitTemplate) {
        this.mqMessageMapper = mqMessageMapper;
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = new ObjectMapper();
    }

    public void saveAndSendRollbackMessage(OrderParamBo orderParamBo, int retryCount, Exception rollbackException) {
        String rollbackId = orderParamBo.getRollbackId();
        String messageId = MQ_MESSAGE_ID_ROLLBACK_PREFIX + rollbackId;

        // 回滚消息发送前必须先落库,保证RabbitMQ发送失败后仍有补偿依据。
        MqMessage rollbackMessage = buildRollbackMessage(orderParamBo, retryCount, rollbackException, messageId);
        int inserted = mqMessageMapper.insertMessage(rollbackMessage);
        if (inserted == 0 && !shouldResendExistingMessage(messageId)) {
            return;
        }

        // 数据库中存在INIT或FAILED记录后再发送MQ,由ConfirmCallback继续更新发送状态。
        rabbitTemplate.convertAndSend(
                ORDER_ROLLBACK_EXCHANGE,
                ROLLBACK_ROUTING_KEY,
                rollbackMessage.getPayload(),
                message -> {
                    message.getMessageProperties().setMessageId(messageId);
                    message.getMessageProperties().setHeader(MQ_HEADER_BIZ_TYPE, MQ_BIZ_TYPE_ORDER_STOCK_ROLLBACK);
                    message.getMessageProperties().setHeader(MQ_HEADER_BIZ_ID, rollbackId);
                    return message;
                },
                new CorrelationData(messageId)
        );
    }

    private MqMessage buildRollbackMessage(OrderParamBo orderParamBo, int retryCount, Exception rollbackException, String messageId) {
        MqMessage rollbackMessage = new MqMessage();
        rollbackMessage.setMessageId(messageId);
        rollbackMessage.setBizType(MQ_BIZ_TYPE_ORDER_STOCK_ROLLBACK);
        rollbackMessage.setBizId(orderParamBo.getRollbackId());
        rollbackMessage.setExchangeName(ORDER_ROLLBACK_EXCHANGE);
        rollbackMessage.setRoutingKey(ROLLBACK_ROUTING_KEY);
        rollbackMessage.setPayload(buildPayload(orderParamBo.getRollbackId(), orderParamBo.getSkuStockList()));
        rollbackMessage.setStatus(MQ_STATUS_INIT);
        rollbackMessage.setRetryCount(retryCount);
        rollbackMessage.setMaxRetryCount(DEFAULT_MAX_RETRY_COUNT);
        rollbackMessage.setFailReason(resolveFailReason(rollbackException));
        return rollbackMessage;
    }

    private String buildPayload(String rollbackId, List<SkuStock> skuStockList) {
        StockRollbackPayload payload = new StockRollbackPayload();
        payload.setRollbackId(rollbackId);
        payload.setSkuStockList(skuStockList.stream().map(this::convertRollbackSkuStock).toList());
        try {
            // JSON消息体只保留回滚必需字段,避免价格和优惠券等无关字段影响消费者。
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new BusinessException("Redis回滚补偿消息序列化失败");
        }
    }

    private StockRollbackPayload.RollbackSkuStock convertRollbackSkuStock(SkuStock skuStock) {
        StockRollbackPayload.RollbackSkuStock rollbackSkuStock = new StockRollbackPayload.RollbackSkuStock();
        rollbackSkuStock.setSkuId(skuStock.getSkuId());
        rollbackSkuStock.setSaleCount(skuStock.getSaleCount());
        return rollbackSkuStock;
    }

    private boolean shouldResendExistingMessage(String messageId) {
        MqMessage existingMessage = mqMessageMapper.selectByMessageId(messageId);
        if (existingMessage == null) {
            log.info("Redis回滚补偿消息已被并发删除或不可见,messageId={}", messageId);
            return false;
        }
        if (CAN_RESEND_STATUS.contains(existingMessage.getStatus())) {
            log.info("Redis回滚补偿消息已存在,允许补发MQ,messageId={},status={}", messageId, existingMessage.getStatus());
            return true;
        }
        log.info("Redis回滚补偿消息已存在且无需补发,messageId={},status={}", messageId, existingMessage.getStatus());
        return false;
    }

    private String resolveFailReason(Exception rollbackException) {
        String failReason = rollbackException.getMessage() == null ? rollbackException.getClass().getSimpleName() : rollbackException.getMessage();
        if (failReason.length() <= MAX_FAIL_REASON_LENGTH) {
            return failReason;
        }
        // 异常信息过长时进行截断,避免超过数据库字段或影响日志检索。
        return failReason.substring(0, MAX_FAIL_REASON_LENGTH);
    }
}
