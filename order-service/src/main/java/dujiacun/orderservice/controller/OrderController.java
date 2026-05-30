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
import spring.common.result.CommonResult;

@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private IOrderService orderService;

    //订单服务配置:Nacos动态配置
    @Autowired
    private OrderProperties orderProperties;

    @PostMapping("/getOrderInfo")
    public OrderResponseDto getOrderInfo(@Validated @RequestBody OrderRequestDto orderRequestDto) {
        OrderParamBo orderParamBo = BeanConvertUtil.convert(orderRequestDto, OrderParamBo.class);
        return BeanConvertUtil.convert(orderService.getOrderInfo(orderParamBo), OrderResponseDto.class);
    }

    @GetMapping("/{orderId}")
    public OrderResponseDto getOrderInfo(@PathVariable Long orderId) {
        OrderParamBo orderParamBo = new OrderParamBo(orderId,null,null,null,null,null);
        return BeanConvertUtil.convert(orderService.getOrderInfo(orderParamBo), OrderResponseDto.class);
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
