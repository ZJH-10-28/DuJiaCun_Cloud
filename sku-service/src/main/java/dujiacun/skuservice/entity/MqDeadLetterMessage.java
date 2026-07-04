package dujiacun.skuservice.entity;

import lombok.Data;

@Data
public class MqDeadLetterMessage {
    private String messageId;
    private String bizType;
    private String bizId;
    private String exchangeName;
    private String routingKey;
    private String queueName;
    private String payload;
    private String headers;
    private String failReason;
}
