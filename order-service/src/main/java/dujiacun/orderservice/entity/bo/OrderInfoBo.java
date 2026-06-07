package dujiacun.orderservice.entity.bo;

import dujiacun.orderservice.entity.UserEntity;
import lombok.Data;

@Data
public class OrderInfoBo {
    private Long orderId;
    private String orderName;
    private Double orderPrice;
    private Integer orderNum;
}