package dujiacun.orderservice.mapper;

import dujiacun.orderservice.entity.OrderEntity;
import dujiacun.orderservice.entity.SkuStock;
import dujiacun.orderservice.entity.bo.OrderParamBo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OrderMapper {
    OrderEntity getOrderInfo(@Param("orderParamBo") OrderParamBo orderParamBo);

    int saveOrderInfo(@Param("orderEntity") OrderEntity orderEntity);

    int saveOrderDetail(
            @Param("orderId") Long orderId,
            @Param("skuStockList") List<SkuStock> skuStockList
    );

}