package dujiacun.skuservice.service;

import dujiacun.common.CommonResult;
import dujiacun.common.error.ErrorCode;
import dujiacun.common.exception.BusinessException;
import dujiacun.common.util.BeanConvertUtil;
import dujiacun.skuservice.entity.*;
import dujiacun.skuservice.mapper.SkuMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class SkuServiceImpl implements ISkuService{

    @Autowired
    private SkuMapper skuMapper;
    @Override
    public CommonResult<List<SkuResponseDto>> getSkuInfo(SkuParamBo skuParamBo) {
        List<SkuResponseDto> list = new ArrayList<>();
        for (SkuEntity skuEntity : skuMapper.getSkuInfo(skuParamBo)){
            list.add(BeanConvertUtil.convert(skuEntity, SkuResponseDto.class));
        }

        if(list.size() <= 0){
            return CommonResult.error(ErrorCode.FAILED.getCode(), "检索不到商品");
        }
        return CommonResult.success("检索完成",list);
    }

    @Override
    public CommonResult<SkuInfoBo> getSkuInfoById(Long skuId) {
        SkuInfoBo skuInfoBo = BeanConvertUtil.convert(skuMapper.getSkuInfoById(skuId),SkuInfoBo.class);
        return CommonResult.success(skuInfoBo);
    }
    @Override
    public CommonResult<Integer> getSkuStockCountById(Long skuId) {
        Integer skuStockCount = skuMapper.getSkuStockCountById(skuId);
        return CommonResult.success(skuStockCount);
    }

    @Override
    public CommonResult<String> insertSkuInfo(SkuParamBo skuParamBo) {
        int result = skuMapper.insertSkuInfo(skuParamBo);
        if(result <= 0){
            return CommonResult.error(ErrorCode.FAILED.getCode(), "新增失败");
        }
        return CommonResult.success("新增成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommonResult<String> saveSkuDetail(Long orderId, List<SkuStock> skuStockList) {
        log.info("开始保存订单明细");
        Integer detailResult = skuMapper.saveSkuDetail(orderId,skuStockList);
        if (detailResult <= 0){
            throw new BusinessException("订单明细保存失败");
        }
        log.info("结束保存订单明细");
        return CommonResult.success("订单明细保存成功");
    }

    @Override
    public CommonResult<String> updateSkuInfo(SkuParamBo skuParamBo) {
        int result = skuMapper.updateSkuInfo(skuParamBo);
        if(result <= 0){
            return CommonResult.error(ErrorCode.FAILED.getCode(), "更新失败");
        }
        return CommonResult.success("更新成功");
    }

    @Override
    @Transactional
    public CommonResult<String> saleSkuInfo(Long skuId, Integer saleCount) {
        int result = skuMapper.saleSkuInfo(skuId,saleCount);
        if(result <= 0){
            return CommonResult.error(ErrorCode.FAILED.getCode(), "库存扣减失败");
        }
        return CommonResult.success("库存扣减成功");
    }

    @Override
    public CommonResult<String> deleteSkuInfo(Long skuId) {
        int result = skuMapper.deleteSkuInfo(skuId);
        if(result <= 0){
            return CommonResult.error(ErrorCode.FAILED.getCode(), "商品删除失败");
        }
        return CommonResult.success("商品删除成功");
    }
}
