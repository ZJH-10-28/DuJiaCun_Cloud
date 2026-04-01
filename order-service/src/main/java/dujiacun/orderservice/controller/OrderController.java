package dujiacun.orderservice.controller;

import dujiacun.orderservice.entity.UserEntity;
import dujiacun.orderservice.entity.bo.OrderInfoBo;
import dujiacun.orderservice.entity.bo.OrderParamBo;
import dujiacun.orderservice.entity.dto.OrderRequestDto;
import dujiacun.orderservice.entity.dto.OrderResponseDto;
import dujiacun.orderservice.service.IOrderService;
import dujiacun.orderservice.util.BeanConvertUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private IOrderService orderService;

    @Autowired
    RestTemplate restTemplate;

    @PostMapping("/getOrderInfo")
    public OrderResponseDto getOrderInfo(@RequestBody OrderRequestDto orderRequestDto) {
        OrderParamBo orderParamBo = BeanConvertUtil.convert(orderRequestDto, OrderParamBo.class);
        return BeanConvertUtil.convert(orderService.getOrderInfo(orderParamBo), OrderResponseDto.class);
    }

    @GetMapping("/{orderId}")
    public OrderResponseDto getOrderInfo(@PathVariable Long orderId) {
        OrderParamBo orderParamBo = new OrderParamBo(orderId,null,null,null,null,null);

        OrderInfoBo orderInfoBo = orderService.getOrderInfo(orderParamBo);
        String url = "http://user-service/user/" + orderInfoBo.getUserId();

        orderInfoBo.setUserEntity(restTemplate.getForObject(url, UserEntity.class));

        return BeanConvertUtil.convert(orderInfoBo, OrderResponseDto.class);
    }
}
