package dujiacun.orderservice.service.impl;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
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
    public CommonResult<Long> createOrderWithTransaction(Long userId , OrderParamBo orderParamBo){
        return createOrder(userId,orderParamBo);
    }

    @SentinelResource(
            value = "createOrder", // 资源名，唯一标识，后续配置规则时会用到
            blockHandler = "handleDeductStockBlock", // 当触发流控或熔断时，调用此方法处理
            fallback = "fallbackDeductStock" // 当业务逻辑出现异常时，调用此方法处理
    )
    public CommonResult<Long> createOrder(Long userId , OrderParamBo orderParamBo){

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
        } catch (RuntimeException ex) {
            log.info("创建订单失败");
            return CommonResult.error("订单创建失败");
        }
    }


    /**
     * 降级处理方法 (blockHandler)
     * 当请求被Sentinel拦截（例如熔断开启、流控触发）时调用
     * 方法签名必须与原方法一致，最后多一个BlockException参数
     */
    public CommonResult<Long> handleDeductStockBlock(String productId, int quantity, BlockException ex) {
        System.out.println("【降级处理】请求被拦截，原因: " + ex.getMessage());
        // 这里可以记录日志、发送告警、或执行其他补偿逻辑
        return CommonResult.error("系统正忙，请稍后再试");
    }

    /**
     * 业务异常降级方法 (fallback)
     * 当原方法业务逻辑抛出异常时调用
     * 方法签名必须与原方法一致，最后多一个Throwable参数
     */
    public CommonResult<Long> fallbackDeductStock(String productId, int quantity, Throwable t) {
        System.out.println("【异常降级】业务执行出错，异常: " + t.getMessage());
        // 这里可以记录详细的业务异常信息
        return CommonResult.error("订单创建失败，请检查参数或稍后重试");
    }
}
