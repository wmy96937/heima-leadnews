package com.heima.wemedia.controller.v1;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.WmNewsDto;
import com.heima.model.wemedia.dtos.WmNewsPageReqDto;
import com.heima.wemedia.service.WmNewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author ASUS
 * @date 2024-11-14 16:01
 **/
@RestController
@RequestMapping("/api/v1/news")
public class WmNewsController {
    @Autowired
    private WmNewsService wmNewsService;

    /**
     * 分页条件文章列表
     * @param dto
     * @return
     */
    @PostMapping("/list")
    public ResponseResult findAll(@RequestBody WmNewsPageReqDto dto){
        return wmNewsService.findAll(dto);
    }

    /**
     * 发布文章
     * @param dto
     * @return
     */
    @PostMapping("/submitNews")
    public ResponseResult submitNews(@RequestBody WmNewsDto dto){
        ResponseResult responseResult=  wmNewsService.submitNews(dto);
        return responseResult;
    }
}
