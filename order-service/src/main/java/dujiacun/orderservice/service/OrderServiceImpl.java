package dujiacun.orderservice.service;

import dujiacun.orderservice.entity.bo.OrderInfoBo;
import dujiacun.orderservice.entity.bo.OrderParamBo;
import dujiacun.orderservice.entity.dto.OrderResponseDto;
import dujiacun.orderservice.mapper.OrderMapper;
import dujiacun.orderservice.util.BeanConvertUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderServiceImpl implements IOrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Override
    public OrderInfoBo getOrderInfo(OrderParamBo orderParamBo) {
        return BeanConvertUtil.convert(orderMapper.getOrderInfo(orderParamBo), OrderInfoBo.class);
    }
}
