package com.heima.wemedia.controller.v1;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.WmMaterialDto;
import com.heima.wemedia.service.WmMaterialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author ASUS
 * @date 2024-11-14 11:09
 **/
@RequestMapping("/api/v1/material")
@RestController
public class WmMaterialController {

    @Autowired
    private WmMaterialService wmMaterialService;

    /**
     * 上传图片
     * @param multipartFile
     * @return
     */
    @PostMapping("/upload_picture")
    public ResponseResult uploadPicture(MultipartFile multipartFile){
        return wmMaterialService.uploadPicture(multipartFile);
    }


    /**
     * 查询素材列表
     * @param dto
     * @return
     */
    @PostMapping("/list")
    public ResponseResult findList(@RequestBody  WmMaterialDto dto){
        return wmMaterialService.findList(dto);
    }


    /**
     * 删除图片
     * @param id
     * @return
     */
    @GetMapping("/del_picture/{id}")
    public ResponseResult delete( @PathVariable Integer  id){
        return wmMaterialService.delete(id);
    }


    @GetMapping("/cancel_collect/{id}")
    public ResponseResult unfavorite(@PathVariable Integer id){
        return wmMaterialService.unfavorite(id);
    }


    @GetMapping("/collect/{id}")
    public ResponseResult favorite(@PathVariable Integer id){
        return wmMaterialService.favorite(id);
    }

}
