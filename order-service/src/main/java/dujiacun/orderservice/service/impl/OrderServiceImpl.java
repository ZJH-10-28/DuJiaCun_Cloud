package dujiacun.orderservice.service.impl;

import dujiacun.common.exception.BusinessException;
import dujiacun.orderservice.entity.bo.OrderInfoBo;
import dujiacun.orderservice.entity.bo.OrderParamBo;
import dujiacun.orderservice.mapper.OrderMapper;
import dujiacun.orderservice.service.IOrderService;
import dujiacun.orderservice.service.feignClient.FeignUserClient;
import dujiacun.orderservice.util.BeanConvertUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderServiceImpl implements IOrderService {

    @Autowired
    private OrderMapper orderMapper;

    //使用openFeign
    @Autowired
    private FeignUserClient userClient;

    @Override
    public OrderInfoBo getOrderInfo(OrderParamBo orderParamBo) {
        OrderInfoBo orderInfoBo = BeanConvertUtil.convert(orderMapper.getOrderInfo(orderParamBo),OrderInfoBo.class);

        try {
            orderInfoBo.setUserEntity(userClient.getUser(orderInfoBo.getUserId()));
        } catch (Exception e) {
            throw new BusinessException("超时");
        }
        return orderInfoBo;
    }



}
