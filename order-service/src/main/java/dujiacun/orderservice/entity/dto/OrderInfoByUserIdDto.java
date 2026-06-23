package dujiacun.orderservice.entity.dto;

import lombok.Data;

@Data
public class OrderInfoByUserIdDto {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private Long userId;
}
