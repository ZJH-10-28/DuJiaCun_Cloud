package dujiacun.orderservice.entity.dto;

import lombok.Data;

import java.util.List;

@Data
public class StockRollbackPayload {

    /**
     * Redis预扣减回滚幂等标识
     */
    private String rollbackId;

    /**
     * 需要回滚的商品库存列表
     */
    private List<RollbackSkuStock> skuStockList;

    @Data
    public static class RollbackSkuStock {

        /**
         * 商品ID
         */
        private Long skuId;

        /**
         * 需要回滚的商品数量
         */
        private Integer saleCount;
    }
}
