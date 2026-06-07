package dujiacun.orderservice.controller;

import dujiacun.orderservice.config.OrderProperties;
import dujiacun.orderservice.entity.bo.OrderParamBo;
import dujiacun.orderservice.entity.dto.OrderRequestDto;
import dujiacun.orderservice.entity.dto.OrderResponseDto;
import dujiacun.orderservice.service.IOrderService;
import dujiacun.orderservice.util.BeanConvertUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import dujiacun.common.CommonResult;
import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private IOrderService orderService;

    //订单服务配置:Nacos动态配置
    @Autowired
    private OrderProperties orderProperties;

    @PostMapping("/orderInfo")
    public CommonResult<List<Long>> createOrder(@Validated @RequestBody OrderRequestDto orderRequestDto) {
        OrderParamBo orderParamBo = BeanConvertUtil.convert(orderRequestDto, OrderParamBo.class);
        List<Long> orderList = orderService.createOrder(orderParamBo.getUserId(), orderParamBo);
        return CommonResult.success(orderList);
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
        return CommonResult.success(
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
