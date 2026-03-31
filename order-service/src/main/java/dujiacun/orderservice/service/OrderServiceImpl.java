package dujiacun.orderservice.service;

import dujiacun.orderservice.entity.UserEntity;
import dujiacun.orderservice.entity.bo.OrderInfoBo;
import dujiacun.orderservice.entity.bo.OrderParamBo;
import dujiacun.orderservice.entity.dto.OrderResponseDto;
import dujiacun.orderservice.mapper.OrderMapper;
import dujiacun.orderservice.util.BeanConvertUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class OrderServiceImpl implements IOrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    RestTemplate restTemplate;

    @Override
    public OrderInfoBo getOrderInfo(OrderParamBo orderParamBo) {

        OrderInfoBo orderInfoBo = BeanConvertUtil.convert(orderMapper.getOrderInfo(orderParamBo),OrderInfoBo.class);

        String url = "http://user-service/user/" + orderInfoBo.getUserId();

        orderInfoBo.setUserEntity(restTemplate.getForObject(url, UserEntity.class));
        return orderInfoBo;
    }
}
