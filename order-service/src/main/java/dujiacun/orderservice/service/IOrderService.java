package dujiacun.orderservice.service;

import dujiacun.common.CommonResult;
import dujiacun.orderservice.entity.SkuStock;
import dujiacun.orderservice.entity.bo.OrderInfoBo;
import dujiacun.orderservice.entity.bo.OrderParamBo;

import java.util.List;

public interface IOrderService {

    OrderInfoBo getOrderInfo(OrderParamBo orderParamBo);

    CommonResult checkStock(List<SkuStock> skuStockList) throws InterruptedException;

    CommonResult<Long> afterCreateOrder(Long orderId);

    void rollbackStock(List<SkuStock> skuStockList) throws InterruptedException;

    void saveOrderInfo(OrderParamBo orderParamBo);
}
