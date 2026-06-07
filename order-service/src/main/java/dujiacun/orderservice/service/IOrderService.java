package dujiacun.orderservice.service;

import dujiacun.orderservice.entity.bo.OrderInfoBo;
import dujiacun.orderservice.entity.bo.OrderParamBo;

import java.util.List;

public interface IOrderService {
    OrderInfoBo getOrderInfo(OrderParamBo orderParamBo);

    List<Long> createOrder(Long number, OrderParamBo orderParamBo);
}
