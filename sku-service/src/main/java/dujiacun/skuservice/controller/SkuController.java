package dujiacun.skuservice.controller;

import com.github.pagehelper.PageInfo;
import dujiacun.common.CommonResult;
import dujiacun.common.util.BeanConvertUtil;
import dujiacun.skuservice.entity.SkuParamBo;
import dujiacun.skuservice.entity.SkuRequestDto;
import dujiacun.skuservice.entity.SkuResponseDto;
import dujiacun.skuservice.entity.SkuStock;
import dujiacun.skuservice.service.ISkuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/skus")
public class SkuController {

    @Autowired
    private ISkuService skuService;

    @PostMapping("/skuList")
    public CommonResult<PageInfo<SkuResponseDto>> getSkuInfo(@RequestBody SkuRequestDto skuRequestDto) {
        SkuParamBo skuParamBo = BeanConvertUtil.convert(skuRequestDto, SkuParamBo.class);
        PageInfo<SkuResponseDto> pageInfo = skuService.getSkuInfo(
                skuRequestDto.getPageNum(),
                skuRequestDto.getPageSize(),
                skuParamBo
        );
        if (pageInfo == null || pageInfo.getList().isEmpty()) {
            return CommonResult.error("检索不到商品");
        }
        return CommonResult.success("检索完成", pageInfo);
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
        return skuService.saveSkuDetail(orderId,skuStockList);
    }

    @PostMapping("/skuDetailByOrderId")
    public CommonResult<Map<Long,List<SkuResponseDto>>> getSkuDetailByOrderId(@RequestBody List<Long> orderId) {
        return skuService.getSkuDetailByOrderId(orderId);
    }

    @PostMapping("/updateSkuInfo")
    public CommonResult<String> updateSkuInfo(SkuRequestDto skuRequestDto) {
        SkuParamBo skuParamBo = BeanConvertUtil.convert(skuRequestDto, SkuParamBo.class);
        return skuService.updateSkuInfo(skuParamBo);
    }

    @PostMapping("/skuInfoDelete")
    public CommonResult<String> deleteSkuInfo(List<Long> skuIds) {
        return skuService.deleteSkuInfo(skuIds);
    }
}
