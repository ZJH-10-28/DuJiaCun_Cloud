package dujiacun.orderservice.entity.bo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class OrderInfoBo {

    private Long orderId;

    private Long userId;

    /**
     * 客户端下单幂等标识
     */
    private String idempotencyKey;

    /**
     * 下单请求体摘要
     */
    private String requestHash;

    private Double orderPrice;

    private LocalDateTime createTime;
}
