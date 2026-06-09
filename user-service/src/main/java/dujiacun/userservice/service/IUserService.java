package dujiacun.userservice.service;

import dujiacun.userservice.entity.bo.UserInfoBo;
import dujiacun.userservice.entity.bo.UserParamBo;

import java.util.Map;

public interface IUserService {
    UserInfoBo getUserInfo(UserParamBo userParamBo);

    int setUserInfo(UserParamBo userParamBo);

    UserInfoBo getByUserId(UserParamBo userParamBo);

    Map<String, Object> login(String userName, String passWord);
}
