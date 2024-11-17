package com.heima.wemedia.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.WmNewsDto;
import com.heima.model.wemedia.dtos.WmNewsPageReqDto;
import com.heima.model.wemedia.pojos.WmNews;

/**
 * @author ASUS
 * @date 2024-11-14 16:03
 **/
public interface WmNewsService extends IService<WmNews> {
    ResponseResult findAll(WmNewsPageReqDto dto);

    ResponseResult submitNews(WmNewsDto dto);

    ResponseResult detail(Integer id);

    ResponseResult deleteById(Integer id);

    ResponseResult upperAndLowerShelves(WmNewsDto dto);
}
