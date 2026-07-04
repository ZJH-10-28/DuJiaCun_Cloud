package dujiacun.skuservice.mapper;

import dujiacun.skuservice.entity.MqConsumeLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MqConsumeLogMapper {

    int insertIgnore(@Param("log") MqConsumeLog log);
}
