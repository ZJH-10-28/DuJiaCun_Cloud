package dujiacun.orderservice.service.impl;

import dujiacun.common.CommonResult;
import dujiacun.common.exception.BusinessException;
import dujiacun.common.util.BeanConvertUtil;
import dujiacun.orderservice.entity.OrderEntity;
import dujiacun.orderservice.entity.SkuStock;
import dujiacun.orderservice.entity.bo.OrderInfoBo;
import dujiacun.orderservice.entity.bo.OrderParamBo;
import dujiacun.orderservice.mapper.OrderMapper;
import dujiacun.orderservice.service.IOrderService;
import dujiacun.orderservice.service.feignClient.FeignSkuClient;
import dujiacun.orderservice.service.feignClient.FeignUserClient;
import io.seata.spring.annotation.GlobalTransactional;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static dujiacun.common.constant.SysConstant.*;
import static dujiacun.orderservice.constant.OrderStatusConstant.*;

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

    @Autowired
    private RedissonClient redissonClient;

    @GlobalTransactional(rollbackFor= Exception.class)
    public CommonResult<Long> createOrder(Long userId , OrderParamBo orderParamBo) {

        List<SkuStock> skuStockList = orderParamBo.getSkuStockList();
//        for (SkuStock skuStock : skuStockList) {
//            boolean isStock = checkStock(skuStock.getSkuId(), skuStock.getSaleCount());
//        }

        //检查库存
        boolean isStock = true;
        if (!isStock){
            throw new BusinessException("库存不足");
        }

        //创建订单
        Long orderId =
                ((OrderServiceImpl) AopContext.currentProxy()) //代理对象
                        .submit(userId, orderParamBo);
        orderParamBo.setOrderId(orderId);


        //保存订单信息到数据库
        ((OrderServiceImpl) AopContext.currentProxy())
                .saveOrderInfo(orderParamBo);

        ((OrderServiceImpl) AopContext.currentProxy())
                .saveOrderDetail(orderParamBo.getOrderId(),skuStockList);

        //向RabbitMQ发送消息使sku-service扣减库存

        return CommonResult.success("订单创建成功",orderId);
    }

    @Transactional(rollbackFor= Exception.class)
    public Long submit(Long userId , OrderParamBo orderParamBo) {

        //创建主订单
        Long orderId = Long.valueOf("123");

        //创建商品订单
        Long orderIds = Long.valueOf("456");

        //计算订单总金额
        orderParamBo.setOrderPrice(99999.12);
        //向RabbitMQ发送消息扣减库存

        // TODO
//        if (!checkStock()){
//            throw new BusinessException("订单创建失败");
//        }
        return orderId;
    }

    @Transactional(rollbackFor= Exception.class)
    public void saveOrderInfo(OrderParamBo orderParamBo) {
        OrderEntity orderEntity = BeanConvertUtil.convert(orderParamBo, OrderEntity.class);
        orderEntity.setOrderStatus(ORDER_STATUS_WAIT_FOR_PAY);
        Integer result = orderMapper.saveOrderInfo(orderEntity);
        if (result <= 0){
            throw new BusinessException("保存订单信息失败");
        }
    }

    @Transactional(rollbackFor= Exception.class)
    public void saveOrderDetail(Long orderId,List<SkuStock> skuStockList) {

        Integer detailResult = orderMapper.saveOrderDetail(orderId,skuStockList);
        if (detailResult <= 0){
            throw new BusinessException("保存订单明细失败");
        }
    }

    @Override
//    @Async("orderTaskExecutor") // 指定线程池
    public OrderInfoBo getOrderInfo(OrderParamBo orderParamBo) {
        return  BeanConvertUtil.convert(orderMapper.getOrderInfo(orderParamBo),OrderInfoBo.class);
    }


    // TODO
    private boolean checkStock(Long skuId, Integer saleCount) {

        //扣减库存用
        String skuLockKey = SKU_LOCK_KEY + skuId;
        //查SKU库存用
        String skuKey = STR_SKU + skuId;

        // 查redis中SKU库存
        boolean exists = redisTemplate.hasKey(skuKey);
        if (!exists){

            //数据库中查库存
            Integer skuStockCount = skuClient.getSkuStockCountById(skuId).getData();

            //更新到redis中
            redisTemplate.opsForValue().set(skuKey,skuStockCount);
        }

        //存在,锁库存预扣减

        RLock lock = redissonClient.getLock(skuKey);
        boolean isLock = lock.tryLock();
        if (!isLock) {
            // 获取库存信息失败
            throw new BusinessException("获取库存信息失败");
        }

        return true;
    }

}
