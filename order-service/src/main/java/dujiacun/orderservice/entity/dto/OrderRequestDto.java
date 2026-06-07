package dujiacun.orderservice.entity.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Data
public class OrderRequestDto {

    @NotNull
    private Long userId;

    private Long couponId;

    @NotEmpty(message = "请选择商品")
    private List<Long> skuIdList;

    @Min(value = 1, message = "数量最小值为 {value}")
    private Integer skuNum;

}
