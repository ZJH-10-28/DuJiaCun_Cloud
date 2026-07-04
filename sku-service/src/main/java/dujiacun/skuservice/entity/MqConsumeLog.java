package dujiacun.skuservice.entity;

import lombok.Data;

@Data
public class MqConsumeLog {
    private String messageId;
    private String bizType;
    private String bizId;
    private String consumeStatus;
}
