package dujiacun.orderservice.entity.bo;

import dujiacun.orderservice.entity.UserEntity;
import lombok.Data;

import java.util.Date;

@Data
public class OrderInfoBo {

    private Long orderId;

    private Long userId;

    private Double orderPrice;

    private Date createTime;
}