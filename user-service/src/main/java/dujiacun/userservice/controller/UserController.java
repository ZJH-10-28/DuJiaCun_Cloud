package dujiacun.userservice.controller;

import dujiacun.common.CommonResult;
import dujiacun.common.error.ErrorCode;
import dujiacun.common.util.BeanConvertUtil;
import dujiacun.userservice.entity.UserEntity;
import dujiacun.userservice.entity.bo.UserParamBo;
import dujiacun.userservice.entity.dto.UserRequestDto;
import dujiacun.userservice.entity.dto.UserResponseDto;
import dujiacun.userservice.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.Map;


@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private IUserService userService;

    @PostMapping("/login")
    public CommonResult<Map<String, Object>> login (@RequestBody UserRequestDto userRequestDto) {
        Map<String, Object> tokenMap = userService.login(userRequestDto.getUserName(),userRequestDto.getPassWord());
        if (tokenMap == null){
            return CommonResult.error(ErrorCode.VALIDATE_FAILED);
        }

        return CommonResult.success(tokenMap);
    }

    @PostMapping("/register")
    public CommonResult register(@RequestBody @Validated UserRequestDto userRequestDto) {
        UserParamBo userParamBo = BeanConvertUtil.convert(userRequestDto, UserParamBo.class);

        //向用户订单表中插入订单ID
        int isInsert = userService.setUserInfo(userParamBo);
        if (isInsert <= 0){
            return CommonResult.error(ErrorCode.FAILED);
        }
        return CommonResult.success("新用户创建成功");
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
        return BeanConvertUtil.convert(userService.getByUserId(userId), UserEntity.class);
    }

}
