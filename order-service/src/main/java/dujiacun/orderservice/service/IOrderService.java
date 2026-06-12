package dujiacun.orderservice.service;

import dujiacun.common.CommonResult;
import dujiacun.orderservice.entity.bo.OrderInfoBo;
import dujiacun.orderservice.entity.bo.OrderParamBo;

import java.util.List;

public interface IOrderService {
    OrderInfoBo getOrderInfo(OrderParamBo orderParamBo);

    CommonResult<Long> createOrder(Long number, OrderParamBo orderParamBo);
}
