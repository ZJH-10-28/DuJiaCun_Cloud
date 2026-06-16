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
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static dujiacun.common.constant.RabbitMQConstant.*;
import static dujiacun.common.constant.SysConstant.*;
import static dujiacun.common.constant.OrderConstant.*;

@Slf4j
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

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @GlobalTransactional(rollbackFor= Exception.class)
    public CommonResult<Long> createOrder(Long userId , OrderParamBo orderParamBo) {

        log.info("开始创建订单");
        List<SkuStock> skuStockList = orderParamBo.getSkuStockList();
        Double orderPrice = 0.0;

        try {
        //创建订单
        // TODO 订单生成
        Long orderId = Long.valueOf(
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + userId
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss"))
        );
        orderParamBo.setOrderId(orderId);
        orderParamBo.setUserId(userId);
        //计算金额
        for (SkuStock skuStock : skuStockList){
            orderPrice += skuStock.getSkuPrice() * skuStock.getSaleCount();
        }
        orderParamBo.setOrderPrice(Math.round(orderPrice * 100.0) / 100.0 );
        orderParamBo.setOrderStatus(ORDER_STATUS_WAIT_FOR_PAY);

        //保存订单信息到order数据库,标记为预扣减
        this.saveOrderInfo(orderParamBo);

        //保存订单明细到sku数据库
        skuClient.saveSkuDetail(orderParamBo.getOrderId(),skuStockList);

        return CommonResult.success("订单创建成功",orderId);
        } catch (Exception e) {
            //回滚Redis
            this.rollbackStock(skuStockList);
            //抛出异常让seata捕获
            throw new RuntimeException(e);
        }
    }
    public CommonResult<Long> afterCreateOrder(Long orderId) {

        //TODO
        //调用支付模块,支付成功后更新订单信息到OrderInfo,标记为支付成功

        //调用MQ异步扣减sku_master库存,修改订单明细状态为已支付
        rabbitTemplate.convertAndSend(
                ORDER_EXCHANGE,
                ROUTING_KEY,
                orderId.toString()
        );

        return CommonResult.success("下单成功",orderId);
    }

    public void saveOrderInfo(OrderParamBo orderParamBo) {
        log.info("开始订单预扣减");
        OrderEntity orderEntity = BeanConvertUtil.convert(orderParamBo, OrderEntity.class);
        orderEntity.setOrderStatus(ORDER_STATUS_WAIT_FOR_PAY);
        Integer result = orderMapper.saveOrderInfo(orderEntity);
        if (result <= 0){
            throw new BusinessException("保存订单信息失败");
        }
        log.info("结束订单预扣减");
    }

    @Override
//    @Async("orderTaskExecutor") // 指定线程池
    public OrderInfoBo getOrderInfo(OrderParamBo orderParamBo) {
        return  BeanConvertUtil.convert(orderMapper.getOrderInfo(orderParamBo),OrderInfoBo.class);
    }


    // TODO redis库存预扣减
    public boolean checkStock(List<SkuStock> skuStockList) {

        log.info("开始验证库存");
        //按SKU排序
        List<SkuStock> sortedSkuStockList = skuStockList.stream()
                .sorted(Comparator.comparing(SkuStock::getSkuId))
                .toList();

        // 取得所有SKU对应的Rediskey
        List<String> skuRedisKeys = sortedSkuStockList.stream()
                .map(s -> STR_SKU + s.getSkuId())
                .toList();
        List<Long> skuRedisIds = sortedSkuStockList.stream()
                .map(SkuStock::getSkuId)
                .toList();

        // 批量查询Redis
        List<Object> stocks = redisTemplate.opsForValue().multiGet(skuRedisKeys);
        if (stocks == null || stocks.isEmpty()){
            dbTORedis(skuRedisIds,skuRedisKeys);
        }

        //找出Redis中没有的SKU
        List<String> noSkuRedisKeys = new ArrayList<>();
        List<Long> noSkuRedisIds = new ArrayList<>();
        for (int i = 0; i < stocks.size(); i++) {
            if (stocks.get(i) == null){
                noSkuRedisKeys.add(skuRedisKeys.get(i));
                noSkuRedisIds.add(skuRedisIds.get(i));
            }
        }

        //Redis中缺少数据
        if (!noSkuRedisKeys.isEmpty()){
            dbTORedis(noSkuRedisIds,noSkuRedisKeys);
        }

        String luaScript =
                "for i, key in ipairs(KEYS) do " +
                "   local stock = redis.call('GET', key) " +
                "   if not stock then " +
                "       return i " +
                "   end " +
                "   if tonumber(stock) < tonumber(ARGV[i]) then " +
                "       return i " +
                "   end " +
                "end " +
                "for i, key in ipairs(KEYS) do " +
                "   redis.call('DECRBY', key, ARGV[i]) " +
                "end " +
                "return -1";

        List<String> keys = skuRedisKeys;
        List<Integer> values = sortedSkuStockList.stream()
                .map(s -> s.getSaleCount())
                .toList();
        long result = (long)redisTemplate.execute(
                new DefaultRedisScript<>(luaScript, long.class),
                keys,
                values.toArray()
        );

        if (result >= 0){
            log.info("结束验证库存:库存不足");
            throw new BusinessException("库存不足:" + sortedSkuStockList.get((int)result - 1).getSkuId());
        }
        log.info("结束验证库存");
        return true;
    }

    public void rollbackStock(List<SkuStock> skuStockList) {
        log.info("开始回滚Redis");
        RLock lock = redissonClient.getLock(REDIS_STOCK_LOCK);
        try {
            if (lock.tryLock(3, 10, TimeUnit.SECONDS)) {

                //再次检查SKU是否存在
                Map<String,Integer> needRollBackSkusMap = new HashMap<>();
                List<Long> rollBackSkuIds = skuStockList.stream()
                        .map(SkuStock::getSkuId)
                        .toList();
                for (int i = 0; i < rollBackSkuIds.size(); i++) {
                    //Redis中存在SKU才进行回滚,不存在直接忽略
                    if (redisTemplate.hasKey(STR_SKU + rollBackSkuIds.get(i))){
                        needRollBackSkusMap.put(STR_SKU + rollBackSkuIds.get(i) , skuStockList.get(i).getSaleCount());
                    }
                }
                //不存在直接忽略
                if (needRollBackSkusMap.isEmpty()){
                    return;
                }
                String rollBackLuaScript =
                        "for i, key in ipairs(KEYS) do " +
                        "   redis.call('INCRBY', key, ARGV[i]) " +
                        "end " +
                        "return -1";
                //批量写入Redis
                List<String> keys = new ArrayList<>(needRollBackSkusMap.keySet());
                List<Integer> values = new ArrayList<>(needRollBackSkusMap.values());
                long result = (long) redisTemplate.execute(
                        new DefaultRedisScript<>(rollBackLuaScript, long.class),
                        keys,
                        values.toArray()
                );
                if (result >= 0){
                    throw new BusinessException("Redis回滚库存失败");
                }
                //释放锁
                lock.unlock();
            }
        } catch (InterruptedException e) {
            //设置中断标志
            Thread.currentThread().interrupt();
            throw new BusinessException("Redis回滚库存失败");
        }
        log.info("结束回滚Redis");
    }

    // TODO 从数据库中查没有的SKU到Redis中
    public void dbTORedis(List<Long> noSkuRedisIds , List<String> noSkuRedisKeys) {
        RLock lock = redissonClient.getLock(REDIS_STOCK_LOCK);

        try {
            if (lock.tryLock(3, 10, TimeUnit.SECONDS)) {

                // 向redis写入时使用的 multiSetIfAbsent ,可以省略双重检查
                List<Long> needLoadSkuIds = new ArrayList<>();
                for (int i = 0; i < noSkuRedisKeys.size(); i++) {
                    if (!redisTemplate.hasKey(noSkuRedisKeys.get(i))) {
                        needLoadSkuIds.add(noSkuRedisIds.get(i));
                    }
                }
                if (needLoadSkuIds.isEmpty()){
                    return;
                }

                // 批量从 DB 查询库存
                Map<Long, Integer> dbStocks = skuClient.getStocksBySkuIds(needLoadSkuIds);
                Map<String, Integer> redisStocks = new HashMap<>();
                for (Long skuId : needLoadSkuIds) {
//                  //数据库没有当前SKU就返回0,防止缓存穿透
                    redisStocks.put(STR_SKU + skuId, dbStocks.get(skuId) == null ? 0 :dbStocks.get(skuId));
                }
                // 写入 Redis
                redisTemplate.opsForValue().multiSetIfAbsent(redisStocks);
//                for (Long skuId : needLoadSkuIds) {
//                    redisTemplate.opsForValue().set(
//                            STR_SKU + skuId,
//                            //数据库没有当前SKU就返回0,防止缓存穿透
//                            dbStocks.get(skuId) == null ? 0 : dbStocks.get(skuId),
//                            3600 + ThreadLocalRandom.current().nextLong(0,600),
//                            TimeUnit.SECONDS
//                    );
//                }

                //释放锁
                lock.unlock();

            } else {
                throw new BusinessException("系统繁忙，请稍后重试");
            }
        } catch (InterruptedException e) {
            //设置中断标志
            Thread.currentThread().interrupt();
            throw new BusinessException("加载库存失败");
        }
    }

}
