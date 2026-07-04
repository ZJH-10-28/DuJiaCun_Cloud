package dujiacun.orderservice.mapper;

import dujiacun.orderservice.entity.MqMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MqMessageMapper {

    int insertMessage(@Param("message") MqMessage message);

    int updateStatus(
            @Param("messageId") String messageId,
            @Param("status") String status,
            @Param("failReason") String failReason,
            @Param("currentStatus") String currentStatus
    );
}
