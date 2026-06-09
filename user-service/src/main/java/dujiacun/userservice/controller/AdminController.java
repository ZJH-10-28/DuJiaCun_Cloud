package dujiacun.userservice.controller;

import dujiacun.common.CommonResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admins")
public class AdminController {

    @PostMapping("/userInfo")
    public CommonResult setAdminInfo() {
        return CommonResult.success("admin创建");
    }

    @GetMapping("/userInfo")
    public CommonResult getAminInfo() {
        return CommonResult.success("admin登录");
    }
}
