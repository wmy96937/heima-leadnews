package com.heima.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.user.dtos.LoginDto;
import com.heima.model.user.pojos.ApUser;

/**
 * @author ASUS
 * @date 2024-11-09 17:32
 **/
public interface ApUserLoginService extends IService<ApUser> {
    ResponseResult login(LoginDto dto);
}
