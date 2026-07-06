package dujiacun.orderservice.service.feignClient;

import dujiacun.common.CommonResult;
import dujiacun.common.exception.BusinessException;
import dujiacun.orderservice.entity.SkuResponseDto;
import dujiacun.orderservice.entity.SkuStock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class FeignSkuClientFallbackFactory implements FallbackFactory<FeignSkuClient> {

    @Override
    public FeignSkuClient create(Throwable cause) {
        return new FeignSkuClient() {
            @Override
            public Map<Long, Integer> getStocksBySkuIds(List<Long> skuIds) {
                log.warn("库存服务查询库存降级,skuIds={}", skuIds, cause);
                throw new BusinessException("库存服务暂不可用，请稍后再试");
            }

            @Override
            public CommonResult<String> saveSkuDetail(Long orderId, List<SkuStock> skuStockList) {
                log.warn("库存服务保存订单明细降级,orderId={}", orderId, cause);
                return CommonResult.error("订单明细保存失败，请稍后再试");
            }

            @Override
            public CommonResult<Map<Long, List<SkuResponseDto>>> getSkuDetailByOrderId(List<Long> orderId) {
                log.warn("库存服务查询订单明细降级,orderId={}", orderId, cause);
                return CommonResult.error("订单明细查询失败，请稍后再试");
            }
        };
    }
}
