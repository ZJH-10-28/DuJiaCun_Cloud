package dujiacun.skuservice.entity;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SkuStock {

    /**
     * 商品列表ID
     */
    @NotNull(message = "请选择商品")
    private Long skuId;

    /**
     * 商品数量
     */
    @Min(value = 1, message = "数量最小值为 {value}")
    private Integer saleCount;

    /**
     * 商品价格
     */
    @NotNull(message = "请选择商品")
    @Min(value = 0, message = "价格最小值为 {value}")
    private Double skuPrice;

    /**
     * 使用的优惠券ID
     */
    private Long couponId;
}
