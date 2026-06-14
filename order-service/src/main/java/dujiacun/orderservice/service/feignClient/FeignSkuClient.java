package dujiacun.orderservice.service.feignClient;

import dujiacun.common.CommonResult;
import dujiacun.orderservice.entity.SkuStock;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;


@FeignClient(name = "sku-service")
public interface FeignSkuClient {

    @PostMapping("/skus/skuStocksByIds")
    Map<Long, Integer> getStocksBySkuIds(@RequestBody List<Long> skuIds);

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
