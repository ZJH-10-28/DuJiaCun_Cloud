package dujiacun.userservice.controller;

import dujiacun.userservice.entity.bo.UserInfoBo;
import dujiacun.userservice.entity.bo.UserParamBo;
import dujiacun.userservice.entity.dto.UserRequestDto;
import dujiacun.userservice.entity.dto.UserResponseDto;
import dujiacun.userservice.service.IUserService;
import dujiacun.userservice.util.BeanConvertUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private IUserService userService;
    @PostMapping("/getUserInfo")
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
