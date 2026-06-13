package dujiacun.orderservice.service.feignClient;

import dujiacun.common.CommonResult;
import dujiacun.orderservice.entity.SkuStock;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;


@FeignClient(name = "sku-service")
public interface FeignSkuClient {

    @GetMapping("/skus/skuStockById")
    CommonResult<Integer> getSkuStockCountById(@RequestParam("skuId") Long skuId);

    @PostMapping("/skus/skuOrder")
    CommonResult<String> saleSkuInfo(
            @RequestParam("skuId") Long skuId,
            @RequestParam("saleCount") Integer saleCount
    );

    @PostMapping("/skus/skuDetail")
    CommonResult<String> saveSkuDetail(
            @RequestParam("orderId") Long orderId,
            @RequestBody List<SkuStock> skuStockList
    );
}
