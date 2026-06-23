package dujiacun.orderservice.service.impl;

import dujiacun.common.CommonResult;
import dujiacun.orderservice.entity.SkuStock;
import dujiacun.orderservice.entity.bo.OrderParamBo;
import dujiacun.orderservice.mapper.OrderMapper;
import dujiacun.orderservice.service.ICreateOrderService;
import dujiacun.orderservice.service.IOrderService;
import dujiacun.orderservice.service.feignClient.FeignSkuClient;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static dujiacun.common.constant.OrderConstant.ORDER_STATUS_WAIT_FOR_PAY;

@Slf4j
@Service
public class CreateOrderServiceImpl implements ICreateOrderService {

    @Autowired
    private IOrderService orderService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private FeignSkuClient skuClient;

    @GlobalTransactional(rollbackFor= Exception.class)
    public CommonResult<Long> createOrder(Long userId , OrderParamBo orderParamBo) throws InterruptedException {

        List<SkuStock> skuStockList = orderParamBo.getSkuStockList();
        Double orderPrice = 0.0;

        try {
            //创建订单
            log.info("开始创建订单");
            Long orderId = Long.valueOf(
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                            + redisTemplate.opsForValue().increment("STR_ORDER_ID_GENERATOR", 1)
            );
            orderParamBo.setOrderId(orderId);
            orderParamBo.setUserId(userId);
            //计算金额
            for (SkuStock skuStock : skuStockList) {
                orderPrice += skuStock.getSkuPrice() * skuStock.getSaleCount();
            }
            orderParamBo.setOrderPrice(Math.round(orderPrice * 100.0) / 100.0);
            orderParamBo.setOrderStatus(ORDER_STATUS_WAIT_FOR_PAY);

            //保存订单信息到order数据库,标记为预扣减
            orderService.saveOrderInfo(orderParamBo);

            //保存订单明细到sku数据库
            skuClient.saveSkuDetail(orderParamBo.getOrderId(), skuStockList);

            log.info("结束创建订单");
            return CommonResult.success("订单创建成功", orderId);
        } catch (Exception e) {
            log.info("创建订单失败");
            return CommonResult.error("订单创建失败");
        }
    }
}
