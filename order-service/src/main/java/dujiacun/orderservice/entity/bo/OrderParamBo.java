package dujiacun.orderservice.entity.bo;

import dujiacun.orderservice.entity.SkuStock;
import lombok.Data;
import java.util.List;

@Data
public class OrderParamBo {

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 商品列表List
     */
    private List<SkuStock> skuStockList;

    /**
     * 订单总价
     */
    private Double orderPrice;

    /**
     * 订单状态
     */
    private Integer orderStatus;

    /**
     * 客户端下单幂等标识
     */
    private String idempotencyKey;

    /**
     * 下单请求体摘要
     */
    private String requestHash;

    /**
     * Redis预扣减回滚幂等标识
     */
    private String rollbackId;
}


