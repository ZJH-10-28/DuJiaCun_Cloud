package dujiacun.orderservice.entity;

import lombok.Data;

import java.util.Date;

@Data
public class OrderEntity {

    private Long orderId;

    private Long userId;

    private Double orderPrice;

    /**
     * 订单状态: 0-待付款 1-待发货 2-待收货 3-待评价 4-已完成 5-已取消
     */
    private Integer orderStatus;

    private Date createTime;
}
