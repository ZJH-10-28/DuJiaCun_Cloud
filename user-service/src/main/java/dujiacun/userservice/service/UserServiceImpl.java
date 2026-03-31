package dujiacun.userservice.service;

import dujiacun.userservice.entity.bo.UserInfoBo;
import dujiacun.userservice.entity.bo.UserParamBo;
import dujiacun.userservice.entity.dto.UserResponseDto;
import dujiacun.userservice.mapper.UserMapper;
import dujiacun.userservice.util.BeanConvertUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements IUserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public UserInfoBo getUserInfo(UserParamBo userParamBo) {
        return BeanConvertUtil.convert(userMapper.getUserInfo(userParamBo), UserInfoBo.class);
    }

    @Override
    public UserInfoBo getByUserId(UserParamBo userParamBo) {
        return BeanConvertUtil.convert(userMapper.getByUserId(userParamBo), UserInfoBo.class);
    }
}
