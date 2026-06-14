package dujiacun.orderservice.entity.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrderResponseDto {

    private Long orderId;

    private Long userId;

    private Double orderPrice;

    private LocalDateTime createTime;
}
