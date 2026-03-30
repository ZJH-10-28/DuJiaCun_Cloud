package dujiacun.orderservice.mapper;

import dujiacun.orderservice.entity.bo.OrderInfoBo;
import dujiacun.orderservice.entity.bo.OrderParamBo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface OrderMapper {
    OrderInfoBo getOrderInfo(@Param("orderParamBo") OrderParamBo orderParamBo);
}