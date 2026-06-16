package dujiacun.orderservice.entity.dto;

import dujiacun.orderservice.entity.SkuStock;
import lombok.Data;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Data
public class OrderRequestDto {

    private List<SkuStock> skuStockList;

}
