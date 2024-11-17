package com.heima.wemedia.controller.v1;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.WmNewsDto;
import com.heima.model.wemedia.dtos.WmNewsPageReqDto;
import com.heima.wemedia.service.WmNewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
    @PostMapping("/submit")
    public ResponseResult submitNews(@RequestBody WmNewsDto dto){
        ResponseResult responseResult=  wmNewsService.submitNews(dto);
        return responseResult;
    }


    /**
     * 文章详情
     * @param id
     * @return
     */
    @GetMapping("/one/{id}")
    public ResponseResult detail(@PathVariable Integer id){
        ResponseResult responseResult=  wmNewsService.detail(id);
        return responseResult;
    }

    /**
     * 文章详情
     * @param id
     * @return
     */
    @GetMapping("/del_news/{id}")
    public ResponseResult deleteById(@PathVariable Integer id){
        ResponseResult responseResult=  wmNewsService.deleteById(id);
        return responseResult;
    }


    @PostMapping("/down_or_up")
    public ResponseResult upperAndLowerShelves(@RequestBody WmNewsDto dto){
        ResponseResult responseResult=  wmNewsService.upperAndLowerShelves(dto);
        return responseResult;
    }
}
