package dujiacun.orderservice.entity.dto;

import dujiacun.orderservice.entity.UserEntity;
import lombok.Data;

@Data
public class OrderResponseDto {
    private Long orderId;
    private String orderName;
    private Double orderPrice;
    private Integer orderNum;
}
