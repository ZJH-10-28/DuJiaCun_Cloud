package dujiacun.userservice.service;

import ch.qos.logback.core.util.StringUtil;
import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import dujiacun.common.constant.SysConstant;
import dujiacun.common.constant.UserTokenConstant;
import dujiacun.common.exception.BusinessException;
import dujiacun.userservice.entity.UserEntity;
import dujiacun.userservice.entity.bo.UserInfoBo;
import dujiacun.userservice.entity.bo.UserParamBo;
import dujiacun.userservice.entity.dto.UserResponseDto;
import dujiacun.userservice.mapper.UserMapper;
import dujiacun.userservice.util.BeanConvertUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class UserServiceImpl implements IUserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public UserInfoBo getUserInfo(UserParamBo userParamBo) {
        return BeanConvertUtil.convert(userMapper.getUserInfo(userParamBo), UserInfoBo.class);
    }

    @Override
    public int setUserInfo(UserParamBo userParamBo) {
        return userMapper.setUserInfo(userParamBo);
    }

    @Override
    public UserInfoBo getByUserId(UserParamBo userParamBo) {
        return BeanConvertUtil.convert(userMapper.getByUserId(userParamBo), UserInfoBo.class);
    }

    @Override
    public SaTokenInfo login(String userName, String passWord) {

        if(StringUtil.isNullOrEmpty(userName) || StringUtil.isNullOrEmpty(passWord)){
            throw new BusinessException("用户名或密码不能为空");
        }

        //根据用户名查询用户信息
        UserEntity userEntity = userMapper.getByUserName(userName);

        if (userEntity == null){
            throw new BusinessException("用户不存在");
        }
        if (!userEntity.getPassWord().equals(passWord)){
            throw new BusinessException("密码错误");
        }

        //satoken核心API 根据用户ID生成token
        StpUtil.login(userEntity.getUserId());

        UserTokenConstant userTokenConstant = new UserTokenConstant();
        userTokenConstant.setUserId(userEntity.getUserId());
        userTokenConstant.setUserName(userEntity.getUserName());
        userTokenConstant.setIsAdmin(0);

        redisTemplate.opsForValue().set("USER_TOKEN:" + StpUtil.getTokenValue(), userTokenConstant);

        return StpUtil.getTokenInfo();
    }
}
