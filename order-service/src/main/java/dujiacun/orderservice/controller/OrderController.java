package dujiacun.orderservice.controller;

import dujiacun.common.constant.UserThreadLocal;
import dujiacun.common.error.ErrorCode;
import dujiacun.common.util.BeanConvertUtil;
import dujiacun.orderservice.config.OrderProperties;
import dujiacun.orderservice.entity.SkuResponseDto;
import dujiacun.orderservice.entity.UserEntity;
import dujiacun.orderservice.entity.bo.OrderParamBo;
import dujiacun.orderservice.entity.dto.OrderInfoListDto;
import dujiacun.orderservice.entity.dto.OrderRequestDto;
import dujiacun.orderservice.entity.dto.OrderResponseDto;
import dujiacun.orderservice.service.ICreateOrderService;
import dujiacun.orderservice.service.IOrderService;
import dujiacun.orderservice.service.feignClient.FeignSkuClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import dujiacun.common.CommonResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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
    private RedisTemplate redisTemplate;

    @Autowired
    private FeignSkuClient skuClient;

    @PostMapping("/orderInfo")
    public CommonResult<Long> createOrder(@Validated @RequestBody OrderRequestDto orderRequestDto) throws InterruptedException {
        //防抖
        if (Boolean.FALSE.equals(
                redisTemplate.opsForValue().setIfAbsent("incr:" + UserThreadLocal.getIncrementId(), UserThreadLocal.getIncrementId(), 3000, TimeUnit.MILLISECONDS)
        )){
            return CommonResult.error(ErrorCode.TOO_MANY_REQUESTS);
        }
        OrderParamBo orderParamBo = BeanConvertUtil.convert(orderRequestDto, OrderParamBo.class);
        CommonResult checkResult = orderService.checkStock(orderParamBo.getSkuStockList());
        if (checkResult.getCode() != ErrorCode.SUCCESS.getCode()){
            return CommonResult.error("订单预扣减失败");
        }
        CommonResult<Long> result = createOrderService.createOrder(Long.parseLong(UserThreadLocal.getUserId()), orderParamBo);
        if (result.getCode() != ErrorCode.SUCCESS.getCode()) {
            orderService.rollbackStock(orderParamBo.getSkuStockList());
            return CommonResult.error("订单创建失败");
        }
        return orderService.afterCreateOrder(result.getData());
    }

    @PostMapping("/orderInfoByUserId")
    public CommonResult<List<OrderInfoListDto>> getOrderInfoByUserId(@RequestBody UserEntity userEntity) {
        List<OrderInfoListDto> orderInfoListDtoList = new ArrayList<>();

        List<OrderResponseDto> orderResponseDtoList = orderService.getOrderInfoByUserId(userEntity.getUserId());
        if (orderResponseDtoList == null || orderResponseDtoList.isEmpty()){
            return CommonResult.error("无订单信息");
        }
        List<Long> orderIds = orderResponseDtoList.stream()
                .map(OrderResponseDto::getOrderId)
                .toList();

        Map<Long,List<SkuResponseDto>> skuMapList = skuClient.getSkuDetailByOrderId(orderIds).getData();
        if (skuMapList == null || skuMapList.isEmpty()) {
            return CommonResult.error("无商品信息");
        }

        for (OrderResponseDto orderResponseDto : orderResponseDtoList) {
            OrderInfoListDto orderInfoListDto = new OrderInfoListDto();
            orderInfoListDto.setOrderId(orderResponseDto.getOrderId());
            orderInfoListDto.setOrderPrice(orderResponseDto.getOrderPrice());
            orderInfoListDto.setOrderStatus(orderResponseDto.getOrderStatus());
            orderInfoListDto.setCreateTime(orderResponseDto.getCreateTime());
            orderInfoListDto.setSkuList(skuMapList.get(orderResponseDto.getOrderId()));
            orderInfoListDtoList.add(orderInfoListDto);
        }
        return CommonResult.success(orderInfoListDtoList);
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
