package dujiacun.skuservice.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
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
    public PageInfo<SkuResponseDto> getSkuInfo(Integer pageNum, Integer pageSize, SkuParamBo skuParamBo) {
        // 必须在执行 Mapper 查询前开启分页，PageHelper 才能拦截并生成分页 SQL。
        PageHelper.startPage(pageNum, pageSize);
        List<SkuEntity> skuEntityList = skuMapper.getSkuInfo(skuParamBo);
        PageInfo<SkuEntity> skuEntityPageInfo = new PageInfo<>(skuEntityList);

        List<SkuResponseDto> list = new ArrayList<>();
        for (SkuEntity skuEntity : skuEntityList){
            list.add(BeanConvertUtil.convert(skuEntity, SkuResponseDto.class));
        }

        // DTO 转换会创建新的列表，需要显式保留数据库查询得到的分页元数据。
        PageInfo<SkuResponseDto> responsePageInfo = new PageInfo<>(list);
        responsePageInfo.setTotal(skuEntityPageInfo.getTotal());
        responsePageInfo.setPageNum(skuEntityPageInfo.getPageNum());
        responsePageInfo.setPageSize(skuEntityPageInfo.getPageSize());
        return responsePageInfo;
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
        int detailResult = skuMapper.saveSkuDetail(orderId,skuStockList,ORDER_STATUS_WAIT_FOR_PAY);
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
    @Transactional(rollbackFor = Exception.class)
    public CommonResult<String> saleSkuInfo(Long orderId) {
        // MQ消费传入的是订单ID，库存扣减必须基于该订单下的全部待支付明细执行。
        int pendingSkuCount = skuMapper.countPendingSkuDetail(orderId);
        if (pendingSkuCount <= 0) {
            throw new BusinessException("库存扣减失败");
        }

        int masterUpdated = skuMapper.deductSkuMasterByOrderId(orderId);
        if (masterUpdated != pendingSkuCount) {
            throw new BusinessException("库存不足或扣减不完整");
        }

        int detailUpdated = skuMapper.updateSkuDetailStatus(orderId, IS_SALE);
        if (detailUpdated <= 0) {
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
