package dujiacun.orderservice.entity.dto;

import dujiacun.orderservice.entity.UserEntity;
import lombok.Data;

import java.util.Date;

@Data
public class OrderResponseDto {

    private Long orderId;

    private Long userId;

    private Double orderPrice;

    private Date createTime;
}
