package dujiacun.orderservice.entity;

import lombok.Data;

@Data
public class MqMessage {
    private String messageId;
    private String bizType;
    private String bizId;
    private String exchangeName;
    private String routingKey;
    private String payload;
    private String status;
    private Integer retryCount;
    private Integer maxRetryCount;
    private String failReason;
}
