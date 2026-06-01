package dujiacun.orderservice.service.impl;

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
//
//    @Autowired
//    RestTemplate restTemplate;

//    @Override
//    public OrderInfoBo getOrderInfo(OrderParamBo orderParamBo) {
//        OrderInfoBo orderInfoBo = BeanConvertUtil.convert(orderMapper.getOrderInfo(orderParamBo),OrderInfoBo.class);
//        String url = "http://user-service/user/" + orderInfoBo.getUserId();
//        orderInfoBo.setUserEntity(restTemplate.getForObject(url, UserEntity.class));
//        return orderInfoBo;
//    }


    //使用openFeign
    @Autowired
    private FeignUserClient userClient;

    @Override
    public OrderInfoBo getOrderInfo(OrderParamBo orderParamBo) {
        OrderInfoBo orderInfoBo = BeanConvertUtil.convert(orderMapper.getOrderInfo(orderParamBo),OrderInfoBo.class);

        orderInfoBo.setUserEntity(userClient.getUser(orderInfoBo.getUserId()));
        return orderInfoBo;
    }



}
