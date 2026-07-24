package dujiacun.orderservice.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrderEntity {

    private Long orderId;

    private Long userId;

    /**
     * 客户端下单幂等标识
     */
    private String idempotencyKey;

    /**
     * 下单请求体摘要
     */
    private String requestHash;

    private Double orderPrice;

    /**
     * 订单状态 0:待支付 1:已完成 2:已取消
     */
    private Integer orderStatus;

    private LocalDateTime createTime;

    /**
     * 订单更新时间
     */
    private LocalDateTime updateTime;
}
