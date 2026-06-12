package dujiacun.orderservice.service.feignClient;

import dujiacun.common.CommonResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;


@FeignClient(name = "sku-service")
public interface FeignSkuClient {

    @GetMapping("/skus/skuStockById")
    CommonResult<Integer> getSkuStockCountById(@RequestParam("skuId") Long skuId);

    @PostMapping("/skus/skuOrder")
    CommonResult<String> saleSkuInfo(
            @RequestParam("skuId") Long skuId,
            @RequestParam("saleCount") Integer saleCount
    );
}
