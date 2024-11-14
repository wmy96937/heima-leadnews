package com.heima.wemedia.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.pojos.WmChannel;
import com.heima.wemedia.mapper.WmChannelMapper;
import org.springframework.stereotype.Service;

/**
 * @author ASUS
 * @date 2024-11-14 14:54
 **/

public interface WmChannelService extends IService<WmChannel> {
    ResponseResult channels();

}
