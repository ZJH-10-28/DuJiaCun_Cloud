package dujiacun.userservice.service;

import dujiacun.userservice.entity.bo.UserInfoBo;
import dujiacun.userservice.entity.bo.UserParamBo;

public interface IUserService {
    UserInfoBo getUserInfo(UserParamBo userParamBo);
}
