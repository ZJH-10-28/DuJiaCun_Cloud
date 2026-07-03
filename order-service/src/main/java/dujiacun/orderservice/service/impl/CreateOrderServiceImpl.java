package dujiacun.orderservice.service.impl;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import dujiacun.common.CommonResult;
import dujiacun.common.exception.BusinessException;
import dujiacun.orderservice.entity.SkuStock;
import dujiacun.orderservice.entity.bo.OrderParamBo;
import dujiacun.orderservice.service.ICreateOrderService;
import dujiacun.orderservice.service.IOrderService;
import dujiacun.orderservice.service.feignClient.FeignSkuClient;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
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

    @Autowired
    @Lazy
    private ICreateOrderService createOrderService;

    @GlobalTransactional(rollbackFor = Exception.class)
    public CommonResult<Long> createOrderWithTransaction(Long userId, OrderParamBo orderParamBo) throws InterruptedException {
        try {
            return createOrderService.createOrder(userId, orderParamBo);
        } catch (Exception e) {
            log.error("创建订单异常,执行Redis回滚", e);
            orderService.rollbackStock(orderParamBo.getSkuStockList());
            throw new BusinessException("订单创建失败");
        }
    }

    @SentinelResource(
            value = "createOrder", // 资源名，唯一标识，后续配置规则时会用到
            blockHandler = "handleDeductStockBlock", // 当触发流控或熔断时，调用此方法处理
            fallback = "fallbackDeductStock" // 当业务逻辑出现异常时，调用此方法处理
    )
    public CommonResult<Long> createOrder(Long userId, OrderParamBo orderParamBo) {

        List<SkuStock> skuStockList = orderParamBo.getSkuStockList();
        double orderPrice = 0.0;

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
            CommonResult<String> skuResult = skuClient.saveSkuDetail(orderParamBo.getOrderId(), skuStockList);
            if (!skuResult.getCode().equals(CommonResult.success().getCode())) {
                throw new BusinessException("订单明细保存失败");
            }

            log.info("结束创建订单");
            return CommonResult.success("订单创建成功", orderId);
        } catch (RuntimeException ex) {
            log.info("创建订单异常,整体回滚", ex);
            throw new BusinessException("订单创建失败");
        }
    }

    /**
     * 降级处理方法 (blockHandler)
     * 当请求被Sentinel拦截（例如熔断开启、流控触发）时调用
     * 方法签名必须与原方法一致，最后多一个BlockException参数
     */
    public CommonResult<Long> handleDeductStockBlock(Long userId, OrderParamBo orderParamBo, BlockException ex) {
        log.warn("创建订单被Sentinel限流或熔断,userId={},reason={}", userId, ex.getMessage());
        throw new BusinessException("系统正忙，请稍后再试");
    }

    /**
     * 业务异常降级方法 (fallback)
     * 当原方法业务逻辑抛出异常时调用
     * 方法签名必须与原方法一致，最后多一个Throwable参数
     */
    public CommonResult<Long> fallbackDeductStock(Long userId, OrderParamBo orderParamBo, Throwable t) {
        log.warn("创建订单触发Sentinel fallback,userId={}", userId, t);
        throw new BusinessException("订单创建失败，请检查参数或稍后重试");
    }
}
