package dujiacun.orderservice.entity.dto;

import dujiacun.orderservice.entity.UserEntity;
import lombok.Data;
import jakarta.validation.constraints.NotNull;

@Data
public class OrderRequestDto {

    @NotNull
    private Long orderId;
    private String orderName;
    private Double orderPrice;
    private Integer orderNum;
    private Long userId;
    private UserEntity userEntity;

    public OrderRequestDto(){};

    public OrderRequestDto(Long orderId, String orderName, Double orderPrice,Integer orderNum,Long userId,UserEntity userEntity) {
        this.orderId = orderId;
        this.orderName = orderName;
        this.orderPrice = orderPrice;
        this.orderNum = orderNum;
        this.userId = userId;
        this.userEntity = userEntity;
    }
}
