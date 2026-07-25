package dujiacun.userservice.service;

import dujiacun.userservice.entity.bo.UserInfoBo;
import dujiacun.userservice.entity.bo.UserParamBo;

import java.util.Map;

public interface IUserService {
    UserInfoBo getUserInfo(UserParamBo userParamBo);

    int setUserInfo(UserParamBo userParamBo);

    UserInfoBo getByUserId(Long userId);

    Map<String, Object> login(String userName, String passWord);

    /**
     * 使用一次性refresh token轮换新的令牌对。
     */
    Map<String, Object> refreshToken(String refreshToken);
}
