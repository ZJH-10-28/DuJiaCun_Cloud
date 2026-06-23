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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static dujiacun.common.constant.OrderConstant.*;
import static dujiacun.common.constant.SkuConstant.*;

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
        if(list.isEmpty()){
            return CommonResult.error(ErrorCode.FAILED.getCode(), "检索不到商品");
        }
        return CommonResult.success("检索完成",list);
    }

    @Override
    public CommonResult<List<SkuResponseDto>> getSkuInfoById(List<Long> skuIds) {
        List<SkuResponseDto> skuResponseDtoList = new ArrayList<>();
        for (SkuEntity skuEntity : skuMapper.getSkuInfoById(skuIds)){
            SkuResponseDto skuResponseDto = BeanConvertUtil.convert(skuEntity, SkuResponseDto.class);
            skuResponseDtoList.add(skuResponseDto);
        }
        if(skuResponseDtoList.isEmpty()){
            return CommonResult.error(ErrorCode.FAILED.getCode(), "检索不到商品");
        }
        return CommonResult.success("检索完成",skuResponseDtoList);
    }
    @Override
    public Map<Long, Integer> getSkuStocksByIds(List<Long> skuIds) {
        Map<Long, Integer> skuStockCountMap = new HashMap<>();
        List<SkuEntity> skuEntityList = skuMapper.getSkuStocksByIds(skuIds);
        for (SkuEntity skuEntity : skuEntityList){
            skuStockCountMap.put(skuEntity.getSkuId(),skuEntity.getSkuStockCount());
        }
        return skuStockCountMap;
    }

    @Override
    public CommonResult<Map<Long,List<SkuResponseDto>>> getSkuDetailByOrderId(List<Long> orderIds) {
        Map<Long,List<SkuResponseDto>> skuMap = new HashMap<>();

        List<SkuEntity> skuEntityResult = skuMapper.getSkuDetailByOrderId(orderIds);
        if (skuEntityResult == null || skuEntityResult.isEmpty()){
            return CommonResult.error(ErrorCode.FAILED.getCode(), "无订单信息");
        }

        for (SkuEntity skuEntity : skuEntityResult){
            List<SkuResponseDto> list = new ArrayList<>();
            SkuResponseDto skuResponseDto = BeanConvertUtil.convert(skuEntity, SkuResponseDto.class);

            if (skuMap.containsKey(skuResponseDto.getOrderId())){
                list = skuMap.get(skuResponseDto.getOrderId());
            }

            list.add(skuResponseDto);
            skuMap.put(skuResponseDto.getOrderId(),list);

        }
        return CommonResult.success(skuMap);
    }

    @Override
    public CommonResult<String> insertSkuInfo(SkuParamBo skuParamBo) {
        int result = skuMapper.insertSkuInfo(skuParamBo,NOT_DELETED);
        if(result <= 0){
            return CommonResult.error(ErrorCode.FAILED.getCode(), "新增失败");
        }
        return CommonResult.success("新增成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommonResult<String> saveSkuDetail(Long orderId, List<SkuStock> skuStockList) {
        log.info("开始保存订单明细");
        Integer detailResult = skuMapper.saveSkuDetail(orderId,skuStockList,ORDER_STATUS_WAIT_FOR_PAY);
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
    public CommonResult<String> saleSkuInfo(Long skuId) {
        int result = skuMapper.saleSkuInfo(skuId,IS_SALE);
        if(result <= 0){
            throw new BusinessException("库存扣减失败");
        }
        return CommonResult.success("库存扣减成功");
    }

    @Override
    public CommonResult<String> deleteSkuInfo(List<Long> skuIds) {
        int result = skuMapper.deleteSkuInfo(skuIds,IS_DELETED);
        if(result <= 0){
            return CommonResult.error(ErrorCode.FAILED.getCode(), "商品删除失败");
        }
        return CommonResult.success("商品删除成功");
    }
}
