package com.heima.model.user.dtos;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author ASUS
 * @date 2024-11-09 17:31
 **/


@Data
public class LoginDto {

    /**
     * 手机号
     */
    @ApiModelProperty(value="手机号")
    private String phone;

    /**
     * 密码
     */
    @ApiModelProperty(value="密码")
    private String password;
}
