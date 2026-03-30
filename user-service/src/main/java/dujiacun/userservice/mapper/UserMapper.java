package dujiacun.userservice.mapper;

import dujiacun.userservice.entity.bo.UserInfoBo;
import dujiacun.userservice.entity.bo.UserParamBo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {
    UserInfoBo getUserInfo(@Param("userParamBo") UserParamBo userParamBo);
}