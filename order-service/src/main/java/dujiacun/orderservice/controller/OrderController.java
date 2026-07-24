package dujiacun.orderservice.controller;

import com.github.pagehelper.PageInfo;
import dujiacun.common.CommonResult;
import dujiacun.common.constant.UserThreadLocal;
import dujiacun.common.error.ErrorCode;
import dujiacun.common.util.BeanConvertUtil;
import dujiacun.orderservice.config.OrderProperties;
import dujiacun.orderservice.entity.SkuResponseDto;
import dujiacun.orderservice.entity.bo.OrderParamBo;
import dujiacun.orderservice.entity.dto.OrderInfoByUserIdDto;
import dujiacun.orderservice.entity.dto.OrderInfoListDto;
import dujiacun.orderservice.entity.dto.OrderRequestDto;
import dujiacun.orderservice.entity.dto.OrderResponseDto;
import dujiacun.orderservice.service.ICreateOrderService;
import dujiacun.orderservice.service.IOrderService;
import dujiacun.orderservice.service.impl.OrderIdempotencyService;
import dujiacun.orderservice.service.feignClient.FeignSkuClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static dujiacun.common.constant.SysConstant.STR_IDEMPOTENCY_KEY;

@Slf4j
@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private IOrderService orderService;

    @Autowired
    private ICreateOrderService createOrderService;

    //订单服务配置:Nacos动态配置
    @Autowired
    private OrderProperties orderProperties;

    @Autowired
    private FeignSkuClient skuClient;

    @Autowired
    private OrderIdempotencyService orderIdempotencyService;

    @PostMapping("/orderInfo")
    public CommonResult<Long> createOrder(
            @RequestHeader(value = STR_IDEMPOTENCY_KEY, required = false) String idempotencyKey,
            @Validated @RequestBody OrderRequestDto orderRequestDto
    ) throws InterruptedException {
        String userId = UserThreadLocal.getUserId();
        String incrementId = UserThreadLocal.getIncrementId();
        if (userId == null || userId.isBlank() || incrementId == null || incrementId.isBlank()) {
            log.info("创建订单缺少用户上下文,userId={},incrementId={}", userId, incrementId);
            return CommonResult.error(ErrorCode.UNAUTHORIZED);
        }
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            log.info("创建订单缺少幂等请求头,userId={},incrementId={}", userId, incrementId);
            return CommonResult.error(ErrorCode.VALIDATE_FAILED.getCode(), "缺少Idempotency-Key请求头");
        }

        Long userIdLong = Long.parseLong(userId);
        String requestHash = orderIdempotencyService.buildRequestHash(orderRequestDto);
        OrderIdempotencyService.IdempotencyCheckResult idempotencyCheckResult =
                orderIdempotencyService.tryBegin(userIdLong, idempotencyKey, requestHash);
        if (idempotencyCheckResult.isConflict()) {
            return CommonResult.error(409L, "同一幂等标识不能提交不同订单内容");
        }
        if (idempotencyCheckResult.isSuccess()) {
            return CommonResult.success("重复请求返回已有订单", idempotencyCheckResult.getOrderId());
        }
        if (idempotencyCheckResult.isProcessing()) {
            return CommonResult.error(ErrorCode.TOO_MANY_REQUESTS.getCode(), "订单处理中，请勿重复提交");
        }

        OrderParamBo orderParamBo = BeanConvertUtil.convert(orderRequestDto, OrderParamBo.class);
        orderParamBo.setIdempotencyKey(idempotencyKey);
        orderParamBo.setRequestHash(requestHash);
        CommonResult checkResult = orderService.checkStock(orderParamBo.getSkuStockList());
        if (checkResult.getCode() != ErrorCode.SUCCESS.getCode()){
            orderIdempotencyService.markFailed(userIdLong, idempotencyKey, requestHash);
            return CommonResult.error("订单预扣减失败");
        }
        // Redis预扣减成功后生成回滚幂等标识,不依赖订单ID生成逻辑。
        orderParamBo.setRollbackId("user:" + userId + ":idempotency:" + idempotencyKey);
        CommonResult<Long> result;
        try {
            result = createOrderService.createOrderWithTransaction(userIdLong, orderParamBo);
        } catch (InterruptedException e) {
            // 线程中断时标记幂等失败并继续向上抛出,避免处理中状态长时间占用。
            orderIdempotencyService.markFailed(userIdLong, idempotencyKey, requestHash);
            throw e;
        } catch (RuntimeException e) {
            // 订单创建异常时标记幂等失败,库存回滚由createOrderWithTransaction内部负责。
            orderIdempotencyService.markFailed(userIdLong, idempotencyKey, requestHash);
            throw e;
        }
        if (result.getCode() != ErrorCode.SUCCESS.getCode()) {
            orderIdempotencyService.markFailed(userIdLong, idempotencyKey, requestHash);
            return CommonResult.error("订单创建失败");
        }
        orderIdempotencyService.markSuccess(userIdLong, idempotencyKey, result.getData(), requestHash);
        if ("重复请求返回已有订单".equals(result.getMessage())) {
            return CommonResult.success(result.getMessage(), result.getData());
        }
        return orderService.afterCreateOrder(result.getData());
    }

    @PostMapping("/orderInfoByUserId")
    public CommonResult<PageInfo<OrderInfoListDto>> getOrderInfoByUserId(
            @RequestBody OrderInfoByUserIdDto orderInfoByUserIdDto
    ) {
        
        PageInfo<OrderResponseDto> orderResponsePageInfo = orderService.getOrderInfoByUserId(orderInfoByUserIdDto.getPageNum(),orderInfoByUserIdDto.getPageSize(),orderInfoByUserIdDto.getUserId());
        if (orderResponsePageInfo == null || orderResponsePageInfo.getList().isEmpty()){
            return CommonResult.error("无订单信息");
        }
        List<Long> orderIds = orderResponsePageInfo.getList().stream()
                .map(OrderResponseDto::getOrderId)
                .toList();

        Map<Long,List<SkuResponseDto>> skuMapList = skuClient.getSkuDetailByOrderId(orderIds).getData();
        if (skuMapList == null || skuMapList.isEmpty()) {
            return CommonResult.error("无商品信息");
        }

        List<OrderInfoListDto> orderInfoListDtoList = new ArrayList<>();
        for (OrderResponseDto orderResponseDto : orderResponsePageInfo.getList()) {
            OrderInfoListDto orderInfoListDto = new OrderInfoListDto();
            orderInfoListDto.setOrderId(orderResponseDto.getOrderId());
            orderInfoListDto.setOrderPrice(orderResponseDto.getOrderPrice());
            orderInfoListDto.setOrderStatus(orderResponseDto.getOrderStatus());
            orderInfoListDto.setCreateTime(orderResponseDto.getCreateTime());
            orderInfoListDto.setSkuList(skuMapList.get(orderResponseDto.getOrderId()));
            orderInfoListDtoList.add(orderInfoListDto);
        }
        PageInfo<OrderInfoListDto> resultPageInfo = new PageInfo<>(orderInfoListDtoList);
        resultPageInfo.setTotal(orderResponsePageInfo.getTotal());
        resultPageInfo.setPageNum(orderResponsePageInfo.getPageNum());
        resultPageInfo.setPageSize(orderResponsePageInfo.getPageSize());

        return CommonResult.success(resultPageInfo);
    }

    @GetMapping("/orderInfo")
    public CommonResult<OrderResponseDto> getOrderInfo(@Validated @RequestBody OrderRequestDto orderRequestDto) {
        OrderParamBo orderParamBo = BeanConvertUtil.convert(orderRequestDto, OrderParamBo.class);
        return CommonResult.success(
                BeanConvertUtil.convert(orderService.getOrderInfo(orderParamBo), OrderResponseDto.class)
        );
    }

    @GetMapping("/{orderId}")
    public CommonResult<OrderResponseDto> getOrderInfo(@PathVariable Long orderId) {
        OrderParamBo orderParamBo = new OrderParamBo();
        orderParamBo.setOrderId(orderId);
        OrderResponseDto orderResponseDto = BeanConvertUtil.convert(orderService.getOrderInfo(orderParamBo), OrderResponseDto.class);
        if (orderResponseDto == null){
            return CommonResult.error("检索不到相关内容");
        }
        return CommonResult.success(
                "检索成功",
                BeanConvertUtil.convert(orderService.getOrderInfo(orderParamBo), OrderResponseDto.class)
        );
    }

    @GetMapping("/config")
    public CommonResult<String> getConfig() {
        return CommonResult.success(
                orderProperties.getOrderId() +
                        " - " + orderProperties.getOrderName() +
                        " - " + orderProperties.getOrderPrice()
        );
    }
}
