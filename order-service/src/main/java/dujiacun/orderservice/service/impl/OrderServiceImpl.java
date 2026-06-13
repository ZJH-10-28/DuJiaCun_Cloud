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
import java.util.concurrent.TimeUnit;

import static dujiacun.common.constant.SysConstant.*;
import static dujiacun.orderservice.constant.OrderStatusConstant.*;

@Service
public class OrderServiceImpl implements IOrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private FeignSkuClient skuClient;

    @Autowired
    private RedissonClient redissonClient;

    @GlobalTransactional(rollbackFor= Exception.class)
    public CommonResult<Long> createOrder(Long userId , OrderParamBo orderParamBo) {

        //创建订单
        Long orderId = Long.valueOf("123456");
        orderParamBo.setOrderId(orderId);
        //计算金额
        orderParamBo.setOrderPrice(99999.12);
        orderParamBo.setOrderStatus(ORDER_STATUS_WAIT_FOR_PAY);

        //保存订单信息到order数据库,标记为预扣减
        this.saveOrderInfo(orderParamBo);
        //保存订单明细到sku数据库
        skuClient.saveSkuDetail(orderParamBo.getOrderId(),orderParamBo.getSkuStockList());

        return CommonResult.success("订单创建成功",orderId);
    }
    public CommonResult<Long> afterCreateOrder(Long orderId) {

        //调用支付模块,支付成功后更新订单信息到OrderInfo,标记为支付成功

        //调用MQ异步扣减sku_master库存,修改订单明细状态为已支付

        return CommonResult.success("下单成功",orderId);
    }

    public void saveOrderInfo(OrderParamBo orderParamBo) {
        OrderEntity orderEntity = BeanConvertUtil.convert(orderParamBo, OrderEntity.class);
        orderEntity.setOrderStatus(ORDER_STATUS_WAIT_FOR_PAY);
        Integer result = orderMapper.saveOrderInfo(orderEntity);
        if (result <= 0){
            throw new BusinessException("保存订单信息失败");
        }
    }

    @Override
//    @Async("orderTaskExecutor") // 指定线程池
    public OrderInfoBo getOrderInfo(OrderParamBo orderParamBo) {
        return  BeanConvertUtil.convert(orderMapper.getOrderInfo(orderParamBo),OrderInfoBo.class);
    }


    // TODO redis库存预扣减
    public boolean checkStock(List<SkuStock> skuStockList) {

        for (SkuStock skuStock : skuStockList) {
            //扣减库存用
            String skuLockKey = SKU_LOCK_KEY + skuStock.getSkuId();
            //查SKU库存用
            String skuKey = STR_SKU + skuStock.getSkuId();

            // 查redis中SKU库存
            RLock lock = redissonClient.getLock(skuLockKey);
            boolean exists = redisTemplate.hasKey(skuKey);
            //redis中没数据,更新缓存
            if (!exists){
                //上锁
                boolean isLock = lock.tryLock();
                if (!isLock) {
                    // 获取库存信息失败
                    throw new BusinessException("获取库存信息失败");
                }
                try {
                    //数据库中查库存
                    Integer skuStockCount = skuClient.getSkuStockCountById(skuStock.getSkuId()).getData();
                    if (skuStockCount < skuStock.getSaleCount()){
                        throw new BusinessException("库存不足");
                    }
                    //扣减后更新到redis中
                    redisTemplate.opsForValue().set(skuKey,skuStockCount - skuStock.getSaleCount(), 60 * 10, TimeUnit.SECONDS);

                } catch (BusinessException e) {
                    throw new BusinessException(e.getMessage());
                }finally {
                    //释放锁
                    lock.unlock();
                }
            }
            else{
                //存在,锁库存预扣减
                //上锁
                boolean isLock = lock.tryLock();
                if (!isLock) {
                    // 获取库存信息失败
                    throw new BusinessException("获取库存信息失败");
                }
                try {
                    Integer skuStockCount = (Integer) redisTemplate.opsForValue().get(skuKey);
                    if (skuStockCount < skuStock.getSaleCount()){
                        throw new BusinessException("库存不足");
                    }
                    redisTemplate.opsForValue().set(skuKey,skuStockCount - skuStock.getSaleCount());
                } catch (BusinessException e) {
                    throw new BusinessException(e.getMessage());
                }finally {
                    //释放锁
                    lock.unlock();
                }
            }
        }
        return true;
    }

}
