package dujiacun.orderservice.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrderEntity {

    private Long orderId;

    private Long userId;

    private Double orderPrice;

    /**
     * 订单状态 0:待支付 1:已完成 2:已取消
     */
    private Integer orderStatus;

    private LocalDateTime createTime;
}
