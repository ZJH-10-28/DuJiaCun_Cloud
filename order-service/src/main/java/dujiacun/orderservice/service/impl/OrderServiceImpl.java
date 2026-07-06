package dujiacun.orderservice.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import dujiacun.common.CommonResult;
import dujiacun.common.exception.BusinessException;
import dujiacun.common.util.BeanConvertUtil;
import dujiacun.orderservice.entity.MqMessage;
import dujiacun.orderservice.entity.OrderEntity;
import dujiacun.orderservice.entity.SkuStock;
import dujiacun.orderservice.entity.bo.OrderInfoBo;
import dujiacun.orderservice.entity.bo.OrderParamBo;
import dujiacun.orderservice.entity.dto.OrderResponseDto;
import dujiacun.orderservice.mapper.MqMessageMapper;
import dujiacun.orderservice.mapper.OrderMapper;
import dujiacun.orderservice.service.IOrderService;
import dujiacun.orderservice.service.feignClient.FeignSkuClient;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static dujiacun.common.constant.RabbitMQConstant.*;
import static dujiacun.common.constant.SysConstant.*;
import static dujiacun.common.constant.OrderConstant.*;

@Slf4j
@Service
public class OrderServiceImpl implements IOrderService {

    private static final int LOCK_RETRY_COUNT = 3;

    private static final long NORMAL_STOCK_CACHE_BASE_TTL_SECONDS = 3600;

    private static final long NORMAL_STOCK_CACHE_RANDOM_TTL_SECONDS = 600;

    private static final long EMPTY_STOCK_CACHE_BASE_TTL_SECONDS = 60;

    private static final long EMPTY_STOCK_CACHE_RANDOM_TTL_SECONDS = 240;

    private static final long ROLLBACK_IDEMPOTENT_TTL_SECONDS = 7 * 24 * 60 * 60;

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

    @Autowired
    private MqMessageMapper mqMessageMapper;

    static long resolveSkuCacheTtlSeconds(Integer stockCount) {
        // 空库存使用短TTL，减少无货或不存在商品长期停留在缓存中的风险。
        if (stockCount == null || stockCount <= 0) {
            return EMPTY_STOCK_CACHE_BASE_TTL_SECONDS + ThreadLocalRandom.current().nextLong(EMPTY_STOCK_CACHE_RANDOM_TTL_SECONDS + 1);
        }
        // 正常库存使用长TTL和随机抖动，降低大量缓存同时过期带来的回源压力。
        return NORMAL_STOCK_CACHE_BASE_TTL_SECONDS + ThreadLocalRandom.current().nextLong(NORMAL_STOCK_CACHE_RANDOM_TTL_SECONDS + 1);
    }

    private boolean tryLockAllSkuLocks(List<RLock> skuLocks) throws InterruptedException {
        // 多商品加锁必须按固定顺序获取，避免并发订单互相等待造成死锁。
        for (RLock skuLock : skuLocks) {
            if (!skuLock.tryLock(3, 10, TimeUnit.SECONDS)) {
                // 任意商品锁获取失败时立即释放已获取的锁，避免本次重试残留锁占用。
                unlockAllSkuLocks(skuLocks);
                return false;
            }
        }
        return true;
    }

    private void unlockAllSkuLocks(List<RLock> skuLocks) {
        // 按反向顺序释放锁，保证已经获取的商品锁全部归还。
        for (int i = skuLocks.size() - 1; i >= 0; i--) {
            RLock skuLock = skuLocks.get(i);
            if (skuLock.isHeldByCurrentThread()) {
                skuLock.unlock();
            }
        }
    }


    public CommonResult<Long> afterCreateOrder(Long orderId) {

        //TODO
        //调用支付模块,支付成功后更新订单信息到OrderInfo,标记为支付成功

        //调用MQ异步扣减sku_master库存,修改订单明细状态为已支付
        String messageId = MQ_MESSAGE_ID_ORDER_PREFIX + orderId;
        String payload = orderId.toString();

        MqMessage mqMessage = new MqMessage();
        mqMessage.setMessageId(messageId);
        mqMessage.setBizType(MQ_BIZ_TYPE_ORDER_STOCK_DEDUCT);
        mqMessage.setBizId(orderId.toString());
        mqMessage.setExchangeName(ORDER_EXCHANGE);
        mqMessage.setRoutingKey(ROUTING_KEY);
        mqMessage.setPayload(payload);
        mqMessage.setStatus(MQ_STATUS_INIT);
        mqMessage.setRetryCount(0);
        mqMessage.setMaxRetryCount(5);
        mqMessageMapper.insertMessage(mqMessage);

        rabbitTemplate.convertAndSend(
                ORDER_EXCHANGE,
                ROUTING_KEY,
                payload,
                message -> {
                    message.getMessageProperties().setMessageId(messageId);
                    message.getMessageProperties().setHeader(MQ_HEADER_BIZ_TYPE, MQ_BIZ_TYPE_ORDER_STOCK_DEDUCT);
                    message.getMessageProperties().setHeader(MQ_HEADER_BIZ_ID, orderId.toString());
                    return message;
                },
                new CorrelationData(messageId)
        );

        return CommonResult.success("下单成功",orderId);
    }

    @Transactional
    public void saveOrderInfo(OrderParamBo orderParamBo) {
        log.info("开始订单预扣减");
        OrderEntity orderEntity = BeanConvertUtil.convert(orderParamBo, OrderEntity.class);
        orderEntity.setOrderStatus(ORDER_STATUS_WAIT_FOR_PAY);
        Integer result = orderMapper.saveOrderInfo(orderEntity);
        if (result <= 0){
            log.info("订单预扣减失败");
            throw new BusinessException("保存订单信息失败");
        }
        log.info("结束订单预扣减");
    }

    @Override
//    @Async("orderTaskExecutor") // 指定线程池
    public OrderInfoBo getOrderInfo(OrderParamBo orderParamBo) {
        return  BeanConvertUtil.convert(orderMapper.getOrderInfo(orderParamBo),OrderInfoBo.class);
    }

    @Override
    public PageInfo<OrderResponseDto> getOrderInfoByUserId(Integer pageNum, Integer pageSize, Long userId) {
        PageHelper.startPage(pageNum, pageSize);
        List<OrderEntity> orderEntityList = orderMapper.getOrderInfoByUserId(userId);
        PageInfo<OrderEntity> pageInfo = new PageInfo<>(orderEntityList);

        List<OrderResponseDto> orderResponseDtoList = new ArrayList<>();
        for (OrderEntity orderEntity : orderEntityList){
            OrderResponseDto orderResponseDto = BeanConvertUtil.convert(orderEntity, OrderResponseDto.class);
            orderResponseDtoList.add(orderResponseDto);
        }

        PageInfo<OrderResponseDto> responseDtoPageInfo = new PageInfo<>(orderResponseDtoList);
        responseDtoPageInfo.setTotal(pageInfo.getTotal()); // 复制正确的总记录数
        responseDtoPageInfo.setPageNum(pageInfo.getPageNum());
        responseDtoPageInfo.setPageSize(pageInfo.getPageSize());

        return responseDtoPageInfo;
    }


    public CommonResult checkStock(List<SkuStock> skuStockList) throws InterruptedException {

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
            //恢复缓存
            dbTORedis(skuRedisIds,skuRedisKeys);
        }
        else {
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

        // Redis Lua脚本会在服务端原子执行库存校验和扣减，这里不再额外加分布式锁。
        log.info("开始Redis预扣减");
        long result = (long)redisTemplate.execute(
                new DefaultRedisScript<>(luaScript, long.class),
                keys,
                values.toArray()
        );

        if (result >= 0){
            log.info("结束验证库存:库存不足");
            return CommonResult.error("库存不足:" + sortedSkuStockList.get((int)result - 1).getSkuId());
        }
        log.info("Redis预扣减成功");
        log.info("结束Redis预扣减");
        return CommonResult.success("Redis预扣减成功");
    }

    public void rollbackStock(List<SkuStock> skuStockList, String rollbackId) throws InterruptedException {
        List<RLock> rollBackLocks = skuStockList.stream()
                .map(SkuStock::getSkuId)
                .distinct()
                .sorted()
                .map(skuId -> redissonClient.getLock(SKU_LOCK_KEY + skuId))
                .toList();
        log.info("开始回滚Redis");
        try {
            for (int retryCount = 1; retryCount <= LOCK_RETRY_COUNT; retryCount++) {
                if (tryLockAllSkuLocks(rollBackLocks)) {
                    try {
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
                                "local idempotentKey = ARGV[1] " +
                                "if redis.call('EXISTS', idempotentKey) == 1 then " +
                                "   return -1 " +
                                "end " +
                                "for i, key in ipairs(KEYS) do " +
                                "   redis.call('INCRBY', key, ARGV[i + 1]) " +
                                "end " +
                                "redis.call('SETEX', idempotentKey, ARGV[#ARGV], '1') " +
                                "return -1";
                        //批量写入Redis
                        List<String> keys = new ArrayList<>(needRollBackSkusMap.keySet());
                        List<Integer> values = new ArrayList<>(needRollBackSkusMap.values());
                        List<Object> args = new ArrayList<>();
                        // 使用rollbackId幂等Key保证同步回滚和MQ补偿最多只有一次真正加回库存。
                        args.add("rollback:stock:" + rollbackId);
                        args.addAll(values);
                        args.add(ROLLBACK_IDEMPOTENT_TTL_SECONDS);
                        long result = (long) redisTemplate.execute(
                                new DefaultRedisScript<>(rollBackLuaScript, long.class),
                                keys,
                                args.toArray()
                        );
                        if (result >= 0){
                            throw new BusinessException("Redis回滚库存失败");
                        }
                        log.info("Redis回滚库存成功");
                        return;
                    } catch (Exception e) {
                        throw new BusinessException("Redis回滚库存失败");
                    }
                    finally {
                        //释放当前订单涉及的全部商品锁
                        unlockAllSkuLocks(rollBackLocks);
                    }
                }
                log.info("Redis回滚获取锁失败,第{}次重试", retryCount);
            }
            throw new BusinessException("系统繁忙，请稍后重试");
        } finally {
            log.info("结束回滚Redis");
        }
    }

    // TODO 从数据库中查没有的SKU到Redis中
    public void dbTORedis(List<Long> noSkuRedisIds , List<String> noSkuRedisKeys) {
        RLock redisUpdateLock = redissonClient.getLock(REDIS_STOCK_LOCK);

        for (int retryCount = 1; retryCount <= LOCK_RETRY_COUNT; retryCount++) {
            try {
                if (redisUpdateLock.tryLock(3, 10, TimeUnit.SECONDS)) {
                    try {
                        // 双重检查
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
                        // 使用Pipeline批量写入Redis并设置TTL，减少多SKU回源后的网络往返次数。
                        RedisSerializer keySerializer = redisTemplate.getKeySerializer();
                        RedisSerializer valueSerializer = redisTemplate.getValueSerializer();
                        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
                            for (Map.Entry<String, Integer> entry : redisStocks.entrySet()) {
                                long ttlSeconds = resolveSkuCacheTtlSeconds(entry.getValue());
                                connection.stringCommands().setEx(
                                        keySerializer.serialize(entry.getKey()),
                                        ttlSeconds,
                                        valueSerializer.serialize(entry.getValue())
                                );
                            }
                            return null;
                        });
                        return;
                    }
                    finally {
                        if (redisUpdateLock.isHeldByCurrentThread()) {
                            //释放锁
                            redisUpdateLock.unlock();
                        }
                    }
                }
                log.info("加载库存到Redis获取锁失败,第{}次重试", retryCount);
            } catch (InterruptedException e) {
                //设置中断标志
                Thread.currentThread().interrupt();
                throw new BusinessException("加载库存失败");
            }
        }
        throw new BusinessException("系统繁忙，请稍后重试");
    }

}
