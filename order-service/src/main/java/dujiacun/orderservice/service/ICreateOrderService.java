package dujiacun.orderservice.service;

import dujiacun.common.CommonResult;
import dujiacun.orderservice.entity.bo.OrderParamBo;

public interface ICreateOrderService {
    CommonResult<Long> createOrder(Long userId , OrderParamBo orderParamBo) throws InterruptedException;

    CommonResult<Long> createOrderWithTransaction(Long userId , OrderParamBo orderParamBo);
}
