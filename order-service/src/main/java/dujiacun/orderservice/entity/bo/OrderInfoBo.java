package dujiacun.orderservice.entity.bo;

import dujiacun.feignclient.entity.UserEntity;
import lombok.Data;

@Data
public class OrderInfoBo {
    private Long orderId;
    private String orderName;
    private Double orderPrice;
    private Integer orderNum;
    private Long userId;
    private UserEntity userEntity;

    public OrderInfoBo(){};

    public OrderInfoBo(Long orderId, String orderName, Double orderPrice,Integer orderNum,Long userId,UserEntity userEntity) {
        this.orderId = orderId;
        this.orderName = orderName;
        this.orderPrice = orderPrice;
        this.orderNum = orderNum;
        this.userId = userId;
        this.userEntity = userEntity;
    }
}