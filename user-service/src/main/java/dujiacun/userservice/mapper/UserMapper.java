package dujiacun.userservice.mapper;

import dujiacun.userservice.entity.UserEntity;
import dujiacun.userservice.entity.bo.UserParamBo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {
    UserEntity getUserInfo(@Param("userParamBo") UserParamBo userParamBo);

    UserEntity getByUserId(@Param("userParamBo") UserParamBo userParamBo);
}