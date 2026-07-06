package dujiacun.skuservice.service;

import dujiacun.common.CommonResult;
import dujiacun.skuservice.entity.*;

import java.util.List;
import java.util.Map;

public interface ISkuService {

    CommonResult<List<SkuResponseDto>> getSkuInfo(SkuParamBo skuParamBo);

    CommonResult<List<SkuResponseDto>> getSkuInfoById(List<Long> skuIds);

    Map<Long, Integer> getSkuStocksByIds(List<Long> skuIds);

    CommonResult<Map<Long,List<SkuResponseDto>>> getSkuDetailByOrderId(List<Long> orderIds);

    CommonResult<String> insertSkuInfo(SkuParamBo skuParamBo);

    CommonResult<String> saveSkuDetail(Long orderId,List<SkuStock> skuStockList);

    CommonResult<String> updateSkuInfo(SkuParamBo skuParamBo);

    CommonResult<String> saleSkuInfo(Long orderId);

    CommonResult<String> deleteSkuInfo(List<Long> skuIds);
}
