package dujiacun.orderservice.entity;

import dujiacun.orderservice.entity.UserEntity;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
public class OrderEntity {
    private Long orderId;

    private Long userId;

    private Long couponId;

    private Long skuId;

    private Integer skuNum;

    private Double orderPrice;
}
