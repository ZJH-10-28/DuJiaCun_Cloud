package dujiacun.orderservice.service;

import dujiacun.common.CommonResult;
import dujiacun.orderservice.entity.SkuStock;
import dujiacun.orderservice.entity.bo.OrderInfoBo;
import dujiacun.orderservice.entity.bo.OrderParamBo;
import dujiacun.orderservice.entity.dto.OrderResponseDto;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface IOrderService {

    OrderInfoBo getOrderInfo(OrderParamBo orderParamBo);

    List<OrderResponseDto> getOrderInfoByUserId(Long userId);

    CommonResult checkStock(List<SkuStock> skuStockList) throws InterruptedException;

    CommonResult<Long> afterCreateOrder(Long orderId);

    void rollbackStock(List<SkuStock> skuStockList) throws InterruptedException;

    void saveOrderInfo(OrderParamBo orderParamBo);
}
