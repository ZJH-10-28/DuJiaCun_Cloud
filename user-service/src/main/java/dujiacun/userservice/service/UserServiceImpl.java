package dujiacun.userservice.service;

import ch.qos.logback.core.util.StringUtil;
import dujiacun.common.error.ErrorCode;
import dujiacun.common.exception.BusinessException;
import dujiacun.common.util.BeanConvertUtil;
import dujiacun.common.util.JwtUtil;
import dujiacun.userservice.entity.UserEntity;
import dujiacun.userservice.entity.bo.UserInfoBo;
import dujiacun.userservice.entity.bo.UserParamBo;
import dujiacun.userservice.mapper.UserMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static dujiacun.common.constant.SysConstant.*;

@Service
public class UserServiceImpl implements IUserService {

    /**
     * access token有效期为30分钟。
     */
    private static final long ACCESS_TOKEN_TTL_MILLIS = TimeUnit.MINUTES.toMillis(30);

    /**
     * refresh token有效期为7天。
     */
    private static final long REFRESH_TOKEN_TTL_MILLIS = TimeUnit.DAYS.toMillis(7);

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

        return generateTokenPair(userEntity);
    }

    /**
     * 校验并消费一次性refresh token，再轮换新的令牌对。
     */
    @Override
    public Map<String, Object> refreshToken(String refreshToken) {
        Claims refreshClaims;
        try {
            refreshClaims = JwtUtil.parseToken(refreshToken);
        } catch (JwtException | IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        String tokenType = refreshClaims.get(STR_TOKEN_TYPE, String.class);
        String refreshTokenId = refreshClaims.getId();
        if (!TOKEN_TYPE_REFRESH.equals(tokenType)
                || StringUtil.isNullOrEmpty(refreshTokenId)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        Long userId;
        try {
            userId = Long.valueOf(refreshClaims.getSubject());
        } catch (NumberFormatException e) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        // 刷新时重新查询用户，确保新access token使用最新用户信息。
        UserEntity userEntity = userMapper.getByUserId(userId);
        if (userEntity == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        Boolean isDeleted;
        try {
            // Redis删除操作具有原子性，只有第一个刷新请求可以消费成功。
            isDeleted = redisTemplate.delete(STR_REFRESH_TOKEN_KEY + refreshTokenId);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SERVICE_DEGRADED);
        }
        if (!Boolean.TRUE.equals(isDeleted)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        return generateTokenPair(userEntity);
    }

    /**
     * 生成 access、refresh 令牌对，并在Redis中登记一次性refresh会话。
     */
    private Map<String, Object> generateTokenPair(UserEntity userEntity) {
        Map<String, Object> accessClaims = new HashMap<>();
        accessClaims.put(STR_USER_ID, userEntity.getUserId());
        accessClaims.put(STR_USER_NAME, userEntity.getUserName());
        accessClaims.put(STR_IS_ADMIN, userEntity.getIsAdmin());
        accessClaims.put(STR_TOKEN_TYPE, TOKEN_TYPE_ACCESS);

        String token = JwtUtil.generateToken(
                userEntity.getUserId().toString(),
                accessClaims,
                ACCESS_TOKEN_TTL_MILLIS);

        String refreshTokenId = UUID.randomUUID().toString();
        Map<String, Object> refreshClaims = new HashMap<>();
        refreshClaims.put(STR_USER_ID, userEntity.getUserId());
        refreshClaims.put(STR_TOKEN_TYPE, TOKEN_TYPE_REFRESH);
        refreshClaims.put(STR_TOKEN_ID, refreshTokenId);

        String refreshToken = JwtUtil.generateToken(
                userEntity.getUserId().toString(),
                refreshClaims,
                REFRESH_TOKEN_TTL_MILLIS);

        try {
            redisTemplate.opsForValue().set(
                    STR_REFRESH_TOKEN_KEY + refreshTokenId,
                    userEntity.getUserId().toString(),
                    7,
                    TimeUnit.DAYS);
        } catch (Exception e) {
            // Redis异常时不返回未登记的refresh token，避免产生不可控会话。
            throw new BusinessException(ErrorCode.SERVICE_DEGRADED);
        }

        //将token返回给前端
        Map<String, Object> tokenMap = new HashMap<>();
        tokenMap.put(STR_TOKEN, token);
        tokenMap.put(STR_TOKEN_HEADER, TOKEN_HEADER);
        tokenMap.put(STR_REFRESH_TOKEN, refreshToken);
        tokenMap.put(STR_IS_ADMIN, userEntity.getIsAdmin());
        // 将当前登录用户ID返回给前端，便于前端保存登录用户上下文。
        tokenMap.put(STR_USER_ID, userEntity.getUserId());
        return tokenMap;
    }
}
