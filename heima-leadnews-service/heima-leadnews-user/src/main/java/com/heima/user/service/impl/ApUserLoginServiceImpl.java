package com.heima.user.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.user.dtos.LoginDto;
import com.heima.model.user.pojos.ApUser;
import com.heima.user.mapper.ApUserMapper;
import com.heima.user.service.ApUserLoginService;
import com.heima.utils.common.AppJwtUtil;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.ObjectUtils;

import java.util.HashMap;
import java.util.Objects;

/**
 * @author ASUS
 * @date 2024-11-09 17:33
 **/
@Service
public class ApUserLoginServiceImpl extends ServiceImpl<ApUserMapper, ApUser> implements ApUserLoginService {
    @Override
    public ResponseResult login(LoginDto dto) {
        //1. 判断参数
        if (!ObjectUtils.isEmpty(dto.getPhone()) && !ObjectUtils.isEmpty(dto.getPassword())){
            //2.登录,数据库查询
            ApUser user = getOne(Wrappers.lambdaQuery(ApUser.class).eq(ApUser::getPhone, dto.getPhone()));
            if(!ObjectUtils.isEmpty(user)){
                //3.不为空，拼接密码
                String inpswd = dto.getPassword() + user.getSalt();
                //加密后和数据库判断
                String password = DigestUtils.md5DigestAsHex(inpswd.getBytes());
                if (Objects.equals(password, user.getPassword())){
                    //3.1.密码正确，生成token
                    String token = AppJwtUtil.getToken(user.getId().longValue());

                    //4.返回
                    HashMap<String, Object> result = new HashMap<String, Object>();
                    user.setPassword("******");
                    user.setSalt("******");
                    result.put("user", user);
                    result.put("token", token);


                    return ResponseResult.okResult(result);
                }
                //密码错误
                return ResponseResult.errorResult(AppHttpCodeEnum.LOGIN_PASSWORD_ERROR);
            }
            //4.为空，返回不存在
            return ResponseResult.errorResult(AppHttpCodeEnum.AP_USER_DATA_NOT_EXIST);

        }else {
            //4.为空，游客登录返回
            HashMap<String, Object> result = new HashMap<String, Object>();
            String token = AppJwtUtil.getToken(0L);
            result.put("token", token);
            return ResponseResult.okResult(result);

        }
    }
}
