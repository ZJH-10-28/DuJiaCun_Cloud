package dujiacun.orderservice.service.impl;

import dujiacun.common.util.BeanConvertUtil;
import dujiacun.orderservice.entity.UserEntity;
import dujiacun.orderservice.entity.bo.OrderInfoBo;
import dujiacun.orderservice.entity.bo.OrderParamBo;
import dujiacun.orderservice.mapper.OrderMapper;
import dujiacun.orderservice.service.IOrderService;
import dujiacun.orderservice.service.feignClient.FeignSkuClient;
import dujiacun.orderservice.service.feignClient.FeignUserClient;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class OrderServiceImpl implements IOrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    //使用openFeign
    @Autowired
    private FeignUserClient userClient;

    @Autowired
    private FeignSkuClient skuClient;

    @GlobalTransactional
    public List<Long> createOrder(Long userId , OrderParamBo orderParamBo) {

        //锁库存
        for (Long skuId : orderParamBo.getSkuIdList()) {
            //TODO 锁库存
            skuClient.saleSkuInfo(skuId,orderParamBo.getSkuNum());
        }

        //创建订单
        List<Long> orderIds = new ArrayList<>() ;
        orderIds.add(123456L);

        //保存订单
        UserEntity userEntity = userClient.getByUserId(userId);

        //释放库存锁

        return orderIds;
    }
    @Override
//    @Async("orderTaskExecutor") // 指定线程池
    public OrderInfoBo getOrderInfo(OrderParamBo orderParamBo) {
        return  BeanConvertUtil.convert(orderMapper.getOrderInfo(orderParamBo),OrderInfoBo.class);
    }

}
