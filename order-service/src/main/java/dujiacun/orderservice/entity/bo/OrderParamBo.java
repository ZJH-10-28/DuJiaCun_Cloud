package dujiacun.orderservice.entity.bo;

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
     * 优惠券ID
     */
    private Long couponId;

    /**
     * 商品列表ID
     */
    private List<Long> skuIdList;

    /**
     * 商品数量
     */
    private Integer skuNum;

    /**
     * 订单总价
     */
    private Double orderPrice;
}


