package dujiacun.skuservice.mapper;

import dujiacun.skuservice.entity.MqDeadLetterMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MqDeadLetterMessageMapper {

    int insertMessage(@Param("message") MqDeadLetterMessage message);
}
