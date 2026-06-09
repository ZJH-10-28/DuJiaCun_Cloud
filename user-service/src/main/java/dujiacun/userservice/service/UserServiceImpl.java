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
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements IUserService {

    @Autowired
    private UserMapper userMapper;

    @Value("${sa-token.token-prefix}")
    private String TOKEN_HEADER;

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
    public Map<String, String> login(String userName, String passWord) {


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
        claims.put("userId", userEntity.getUserId());
        claims.put("userName", userEntity.getUserName());
        claims.put("isAdmin", 0);

        String token = JwtUtil.generateToken(
                userEntity.getUserId().toString(),
                claims,
                30 * 60 * 1000);

        //将token返回给前端
        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("token", token);
        tokenMap.put("tokenHeader", TOKEN_HEADER);

        return tokenMap;
    }
}
