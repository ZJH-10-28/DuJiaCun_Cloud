package dujiacun.userservice.controller;

import dujiacun.common.CommonResult;
import dujiacun.common.error.ErrorCode;
import dujiacun.userservice.entity.UserEntity;
import dujiacun.userservice.entity.bo.UserParamBo;
import dujiacun.userservice.entity.dto.UserRequestDto;
import dujiacun.userservice.entity.dto.UserResponseDto;
import dujiacun.userservice.service.IUserService;
import dujiacun.userservice.util.BeanConvertUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map;


@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private IUserService userService;

    @PostMapping("/login")
    public CommonResult<Map<String, String>> login (@RequestBody UserRequestDto userRequestDto) {
        Map<String, String> tokenMap = userService.login(userRequestDto.getUserName(),userRequestDto.getPassWord());
        if (tokenMap == null){
            return CommonResult.error(ErrorCode.VALIDATE_FAILED);
        }

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
