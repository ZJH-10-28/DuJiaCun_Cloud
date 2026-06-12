package dujiacun.skuservice.service;

import dujiacun.common.CommonResult;
import dujiacun.skuservice.entity.SkuEntity;
import dujiacun.skuservice.entity.SkuInfoBo;
import dujiacun.skuservice.entity.SkuParamBo;
import dujiacun.skuservice.entity.SkuResponseDto;

import java.util.List;

public interface ISkuService {

    CommonResult<List<SkuResponseDto>> getSkuInfo(SkuParamBo skuParamBo);

    CommonResult<SkuInfoBo> getSkuInfoById(Long skuId);

    CommonResult<Integer> getSkuStockCountById(Long skuId);

    CommonResult<String> insertSkuInfo(SkuParamBo skuParamBo);

    CommonResult<String> updateSkuInfo(SkuParamBo skuParamBo);

    CommonResult<String> saleSkuInfo(Long skuId, Integer saleCount);

    CommonResult<String> deleteSkuInfo(Long skuId);
}
