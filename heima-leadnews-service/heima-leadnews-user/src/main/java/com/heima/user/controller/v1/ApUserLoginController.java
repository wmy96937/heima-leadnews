package com.heima.user.controller.v1;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.user.dtos.LoginDto;
import com.heima.user.service.ApUserLoginService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author ASUS
 * @date 2024-11-09 17:21
 **/
@Api(value = "app端用户登录", tags = "ap_user")
@RequestMapping("/api/v1/login")
@RestController
public class ApUserLoginController {
@Autowired
private ApUserLoginService apUserLoginService;


    @ApiOperation("用户登录")
    @PostMapping("/login_auth")
    public ResponseResult login(@RequestBody LoginDto dto){

        return apUserLoginService.login(dto);

    }
}
