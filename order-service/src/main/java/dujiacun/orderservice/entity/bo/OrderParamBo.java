package dujiacun.orderservice.entity.bo;

import dujiacun.orderservice.entity.UserEntity;
import lombok.Data;

import java.util.Date;
@Data

public class OrderParamBo {
    private Long orderId;
    private String orderName;
    private Double orderPrice;
    private Integer orderNum;
    private Long userId;
    private UserEntity userEntity;

    public OrderParamBo(){};

    public OrderParamBo(Long orderId, String orderName, Double orderPrice,Integer orderNum,Long userId,UserEntity userEntity) {
        this.orderId = orderId;
        this.orderName = orderName;
        this.orderPrice = orderPrice;
        this.orderNum = orderNum;
        this.userId = userId;
        this.userEntity = userEntity;
    }
}


