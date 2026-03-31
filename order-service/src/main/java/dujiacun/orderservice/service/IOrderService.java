package dujiacun.orderservice.service;

import dujiacun.orderservice.entity.bo.OrderInfoBo;
import dujiacun.orderservice.entity.bo.OrderParamBo;

public interface IOrderService {
    OrderInfoBo getOrderInfo(OrderParamBo orderParamBo);

}
