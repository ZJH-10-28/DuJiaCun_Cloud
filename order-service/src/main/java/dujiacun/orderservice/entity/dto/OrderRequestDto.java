package dujiacun.orderservice.entity.dto;

import dujiacun.orderservice.entity.SkuStock;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;

@Data
public class OrderRequestDto {

    /**
     * 下单商品列表,不能为空且需要继续校验列表内商品字段。
     */
    @Valid
    @NotEmpty(message = "商品信息不能为空")
    private List<SkuStock> skuStockList;

}
