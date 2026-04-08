package dujiacun.orderservice.entity;

import dujiacun.feignclient.entity.UserEntity;
import lombok.Data;

import java.util.Date;

@Data
public class OrderEntity {
    private Long orderId;
    private String orderName;
    private Double orderPrice;
    private Integer orderNum;
    private Long userId;
    private UserEntity userEntity;

    public OrderEntity(){};

    public OrderEntity(Long orderId, String orderName, Double orderPrice,Integer orderNum,Long userId,UserEntity userEntity) {
        this.orderId = orderId;
        this.orderName = orderName;
        this.orderPrice = orderPrice;
        this.orderNum = orderNum;
        this.userId = userId;
        this.userEntity = userEntity;
    }
}
