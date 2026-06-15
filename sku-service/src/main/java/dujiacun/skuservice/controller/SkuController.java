package dujiacun.skuservice.controller;

import dujiacun.common.CommonResult;
import dujiacun.common.error.ErrorCode;
import dujiacun.common.util.BeanConvertUtil;
import dujiacun.skuservice.entity.*;
import dujiacun.skuservice.service.ISkuService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/skus")
public class SkuController {

    @Autowired
    private ISkuService skuService;

    @Autowired
    private RedissonClient redissonClient;

    @GetMapping("/skuList")
    public CommonResult<List<SkuResponseDto>> getSkuInfo(SkuRequestDto skuRequestDto) {
        SkuParamBo skuParamBo = BeanConvertUtil.convert(skuRequestDto, SkuParamBo.class);
        List<SkuResponseDto> list = skuService.getSkuInfo(skuParamBo).getData();
        return CommonResult.success(list);
    }

    @GetMapping("/skuInfoById")
    public CommonResult<List<SkuResponseDto>> getSkuInfoById(List<Long> skuIds) {
        return skuService.getSkuInfoById(skuIds);
    }

    @PostMapping("/skuStocksByIds")
    public Map<Long, Integer> getStocksBySkuIds(@RequestBody List<Long> skuIds) {
        return skuService.getSkuStocksByIds(skuIds);
    }

    @PostMapping("/newSkuInfo")
    public CommonResult<String> insertSkuInfo(SkuRequestDto skuRequestDto) {
        SkuParamBo skuParamBo = BeanConvertUtil.convert(skuRequestDto, SkuParamBo.class);
        return skuService.insertSkuInfo(skuParamBo);
    }

    @PostMapping("/skuDetail")
    public CommonResult<String> saveSkuDetail(@RequestParam Long orderId,@RequestBody List<SkuStock> skuStockList) {
        RLock lock = redissonClient.getLock(orderId + "saveSkuDetail");
        if (lock.tryLock()){
            CommonResult<String> result = skuService.saveSkuDetail(orderId,skuStockList);
            lock.unlock();
            return result;
        }
        return CommonResult.error(ErrorCode.FAILED.getCode(), "请勿重复提交");
    }

    @PostMapping("/updateSkuInfo")
    public CommonResult<String> updateSkuInfo(SkuRequestDto skuRequestDto) {
        SkuParamBo skuParamBo = BeanConvertUtil.convert(skuRequestDto, SkuParamBo.class);
        return skuService.updateSkuInfo(skuParamBo);
    }

    @PostMapping("/skuOrder")
    public CommonResult<String> saleSkuInfo(@RequestParam("skuId") Long skuId) {
        return skuService.saleSkuInfo(skuId);
    }

    @PostMapping("/skuInfoDelete")
    public CommonResult<String> deleteSkuInfo(List<Long> skuIds) {
        return skuService.deleteSkuInfo(skuIds);
    }
}
