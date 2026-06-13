package dujiacun.skuservice.service;

import dujiacun.common.CommonResult;
import dujiacun.skuservice.entity.*;

import java.util.List;

public interface ISkuService {

    CommonResult<List<SkuResponseDto>> getSkuInfo(SkuParamBo skuParamBo);

    CommonResult<SkuInfoBo> getSkuInfoById(Long skuId);

    CommonResult<Integer> getSkuStockCountById(Long skuId);

    CommonResult<String> insertSkuInfo(SkuParamBo skuParamBo);

    CommonResult<String> saveSkuDetail(Long orderId,List<SkuStock> skuStockList);

    CommonResult<String> updateSkuInfo(SkuParamBo skuParamBo);

    CommonResult<String> saleSkuInfo(Long skuId, Integer saleCount);

    CommonResult<String> deleteSkuInfo(Long skuId);
}
