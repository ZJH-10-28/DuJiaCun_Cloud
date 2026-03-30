package dujiacun.orderservice.service;

import dujiacun.orderservice.entity.bo.OrderInfoBo;
import dujiacun.orderservice.entity.bo.OrderParamBo;
import dujiacun.orderservice.mapper.OrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderServiceImpl implements IOrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Override
    public OrderInfoBo getUserInfo(OrderParamBo orderParamBo) {
        return orderMapper.getOrderInfo(orderParamBo);
    }
}
