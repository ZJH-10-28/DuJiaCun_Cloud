package dujiacun.orderservice.mapper;

import dujiacun.orderservice.entity.OrderEntity;
import dujiacun.orderservice.entity.bo.OrderParamBo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface OrderMapper {
    OrderEntity getOrderInfo(@Param("orderParamBo") OrderParamBo orderParamBo);

}