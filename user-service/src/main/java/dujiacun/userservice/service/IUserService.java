package dujiacun.userservice.service;

import cn.dev33.satoken.stp.SaTokenInfo;
import dujiacun.userservice.entity.bo.UserInfoBo;
import dujiacun.userservice.entity.bo.UserParamBo;

public interface IUserService {
    UserInfoBo getUserInfo(UserParamBo userParamBo);

    int setUserInfo(UserParamBo userParamBo);

    UserInfoBo getByUserId(UserParamBo userParamBo);

    SaTokenInfo login(String userName, String passWord);
}
