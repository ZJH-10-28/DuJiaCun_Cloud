package dujiacun.orderservice.entity.bo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class OrderInfoBo {

    private Long orderId;

    private Long userId;

    private Double orderPrice;

    private LocalDateTime createTime;
}