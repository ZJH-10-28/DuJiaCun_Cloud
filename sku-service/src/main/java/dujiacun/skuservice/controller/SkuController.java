package dujiacun.skuservice.controller;

import dujiacun.common.CommonResult;
import dujiacun.common.util.BeanConvertUtil;
import dujiacun.skuservice.entity.SkuParamBo;
import dujiacun.skuservice.entity.SkuRequestDto;
import dujiacun.skuservice.entity.SkuResponseDto;
import dujiacun.skuservice.service.ISkuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/skus")
public class SkuController {

    @Autowired
    private ISkuService skuService;

    @GetMapping("/skuList")
    public CommonResult<List<SkuResponseDto>> getSkuInfo(SkuRequestDto skuRequestDto) {
        SkuParamBo skuParamBo = BeanConvertUtil.convert(skuRequestDto, SkuParamBo.class);
        List<SkuResponseDto> list = skuService.getSkuInfo(skuParamBo).getData();
        return CommonResult.success(list);
    }

    @PostMapping("/newSkuInfo")
    public CommonResult<String> insertSkuInfo(SkuRequestDto skuRequestDto) {
        SkuParamBo skuParamBo = BeanConvertUtil.convert(skuRequestDto, SkuParamBo.class);
        return skuService.insertSkuInfo(skuParamBo);
    }

    @PostMapping("/updateSkuInfo")
    public CommonResult<String> updateSkuInfo(SkuRequestDto skuRequestDto) {
        SkuParamBo skuParamBo = BeanConvertUtil.convert(skuRequestDto, SkuParamBo.class);
        return skuService.updateSkuInfo(skuParamBo);
    }

    @PostMapping("/skuOrder")
    public CommonResult<String> saleSkuInfo(@RequestParam("skuId") Long skuId,@RequestParam("saleCount")  Integer saleCount) {
        return skuService.saleSkuInfo(skuId,saleCount);
    }

    @PostMapping("/skuInfoDelete")
    public CommonResult<String> deleteSkuInfo(Long skuId) {
        return skuService.deleteSkuInfo(skuId);
    }
}
