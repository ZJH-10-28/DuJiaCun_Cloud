package dujiacun.userservice.controller;

import cn.dev33.satoken.stp.SaTokenInfo;
import dujiacun.common.CommonResult;
import dujiacun.common.constant.SysConstant;
import dujiacun.common.error.ErrorCode;
import dujiacun.userservice.entity.UserEntity;
import dujiacun.userservice.entity.bo.UserParamBo;
import dujiacun.userservice.entity.dto.UserRequestDto;
import dujiacun.userservice.entity.dto.UserResponseDto;
import dujiacun.userservice.service.IUserService;
import dujiacun.userservice.util.BeanConvertUtil;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private IUserService userService;

    @Value("${sa-token.token-prefix}")
    private String TOKEN_HEADER;

    @PostMapping("/login")
    public CommonResult<Map<String, String>> login (@RequestBody UserRequestDto userRequestDto) {
        SaTokenInfo saTokenInfo = userService.login(userRequestDto.getUserName(),userRequestDto.getPassWord());
        if (saTokenInfo == null){
            return CommonResult.error(ErrorCode.VALIDATE_FAILED);
        }
        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("token", saTokenInfo.getTokenValue());
        tokenMap.put("tokenHeader", TOKEN_HEADER);
        return CommonResult.success(tokenMap);
    }

    @PostMapping("/userInfo")
    public CommonResult setUserInfo(@RequestBody UserRequestDto userRequestDto) {
        UserParamBo userParamBo = BeanConvertUtil.convert(userRequestDto, UserParamBo.class);

        //向用户订单表中插入订单ID
        int isInsert = userService.setUserInfo(userParamBo);
        if (isInsert <= 0){
            return CommonResult.error(ErrorCode.FAILED);
        }
        return CommonResult.success();
    }
    @GetMapping("/userInfo")
    public UserResponseDto getUserInfo(@RequestBody UserRequestDto userRequestDto) {
        UserParamBo userParamBo = BeanConvertUtil.convert(userRequestDto, UserParamBo.class);

        return BeanConvertUtil.convert(userService.getUserInfo(userParamBo), UserResponseDto.class);
    }

    @GetMapping("/{userId}")
    public UserEntity getByUserId(@PathVariable Long userId) {
        UserParamBo userParamBo = new UserParamBo(userId,null,null,null);
        return BeanConvertUtil.convert(userService.getByUserId(userParamBo), UserEntity.class);
    }
}
