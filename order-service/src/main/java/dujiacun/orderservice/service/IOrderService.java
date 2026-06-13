package dujiacun.orderservice.service;

import dujiacun.common.CommonResult;
import dujiacun.orderservice.entity.SkuStock;
import dujiacun.orderservice.entity.bo.OrderInfoBo;
import dujiacun.orderservice.entity.bo.OrderParamBo;

import java.util.List;

public interface IOrderService {

    OrderInfoBo getOrderInfo(OrderParamBo orderParamBo);

    boolean checkStock(List<SkuStock> skuStockList);

    CommonResult<Long> createOrder(Long orderId, OrderParamBo orderParamBo);

    CommonResult<Long> afterCreateOrder(Long orderId);
}
