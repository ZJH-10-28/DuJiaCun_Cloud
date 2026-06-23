package dujiacun.orderservice.entity.dto;

import dujiacun.orderservice.entity.SkuResponseDto;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderInfoListDto {

    private Long orderId;

    private Double orderPrice;

    private Integer orderStatus;

    private LocalDateTime createTime;

    List<SkuResponseDto> skuList;
}
