package dujiacun.userservice.controller;

import cn.dev33.satoken.stp.SaTokenInfo;
import dujiacun.common.CommonResult;
import dujiacun.common.constant.SysConstant;
import dujiacun.common.error.ErrorCode;
import dujiacun.userservice.entity.bo.UserParamBo;
import dujiacun.userservice.entity.dto.UserRequestDto;
import dujiacun.userservice.entity.dto.UserResponseDto;
import dujiacun.userservice.service.IUserService;
import dujiacun.userservice.util.BeanConvertUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private IUserService userService;

    @PostMapping("/login")
    public CommonResult<Map<String, String>> login (@RequestBody UserRequestDto userRequestDto) {
        SaTokenInfo saTokenInfo = userService.login(userRequestDto.getUserName(),userRequestDto.getPassWord());
        if (saTokenInfo == null){
            return CommonResult.error(ErrorCode.VALIDATE_FAILED);
        }
        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("token", saTokenInfo.getTokenValue());
        tokenMap.put("tokenHead", SysConstant.TOKEN_HEADER);
        return CommonResult.success(tokenMap);
    }

    @PostMapping("/info")
    public UserResponseDto getUserInfo(@RequestBody UserRequestDto userRequestDto) {
        UserParamBo userParamBo = BeanConvertUtil.convert(userRequestDto, UserParamBo.class);

        return BeanConvertUtil.convert(userService.getUserInfo(userParamBo), UserResponseDto.class);
    }

    @GetMapping("/{userId}")
    public UserResponseDto getByUserId(@PathVariable Long userId) {
        UserParamBo userParamBo = new UserParamBo(userId,null,null,null);
        return BeanConvertUtil.convert(userService.getByUserId(userParamBo), UserResponseDto.class);
    }
}
