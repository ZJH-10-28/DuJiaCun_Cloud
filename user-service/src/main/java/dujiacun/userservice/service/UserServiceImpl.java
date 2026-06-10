package dujiacun.userservice.service;

import ch.qos.logback.core.util.StringUtil;
import dujiacun.common.exception.BusinessException;
import dujiacun.common.util.JwtUtil;
import dujiacun.userservice.entity.UserEntity;
import dujiacun.userservice.entity.bo.UserInfoBo;
import dujiacun.userservice.entity.bo.UserParamBo;
import dujiacun.userservice.mapper.UserMapper;
import dujiacun.userservice.util.BeanConvertUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import static dujiacun.common.constant.SysConstant.*;

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
        UserEntity userEntity = userMapper.getByUserId(userParamBo.getUserId());
        if (userEntity != null) {
            throw new BusinessException("用户已存在");
        }
        userParamBo.setIsAdmin(IS_USER);
        return userMapper.setUserInfo(userParamBo);
    }

    @Override
    public UserInfoBo getByUserId(Long userId) {
        return BeanConvertUtil.convert(userMapper.getByUserId(userId), UserInfoBo.class);
    }

    @Override
    public Map<String, Object> login(String userName, String passWord) {


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

        //生成JWT
        Map<String, Object> claims = new HashMap<>();
        claims.put(STR_USER_ID, userEntity.getUserId());
        claims.put(STR_USER_NAME, userEntity.getUserName());
        claims.put(STR_IS_ADMIN, userEntity.getIsAdmin());

        String token = JwtUtil.generateToken(
                userEntity.getUserId().toString(),
                claims,
                30 * 60 * 1000);

        //将token返回给前端
        Map<String, Object> tokenMap = new HashMap<>();
        tokenMap.put(STR_TOKEN, token);
        tokenMap.put(STR_TOKEN_HEADER, TOKEN_HEADER);
        tokenMap.put(STR_IS_ADMIN, userEntity.getIsAdmin());

        return tokenMap;
    }
}
