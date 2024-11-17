package com.heima.wemedia.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.file.service.FileStorageService;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.wemedia.dtos.WmMaterialDto;
import com.heima.model.wemedia.pojos.WmMaterial;
import com.heima.utils.thread.WmThreadLocalUtil;
import com.heima.wemedia.mapper.WmMaterialMapper;
import com.heima.wemedia.service.WmMaterialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.UUID;

/**
 * @author ASUS
 * @date 2024-11-14 11:11
 **/
@Service
public class WmMaterialImpl extends ServiceImpl<WmMaterialMapper, WmMaterial> implements WmMaterialService {


    @Autowired
    private FileStorageService fileStorageService;
    @Autowired
    private WmMaterialMapper wmMaterialMapper;

    @Override
    public ResponseResult uploadPicture(MultipartFile multipartFile) {
        //1.判空
        if(multipartFile == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }


        //2.上传图片
        String originalFilename = multipartFile.getOriginalFilename();
        String end = originalFilename.substring(originalFilename.lastIndexOf(".")+1);
        String start= UUID.randomUUID().toString();

        String path =null;
        try {
            path=fileStorageService.uploadImgFile(
                    "",
                    start+"."+end,
                    multipartFile.getInputStream()
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //3.更新数据库
        WmMaterial wmMaterial = new WmMaterial();
        wmMaterial.setUserId(WmThreadLocalUtil.getUser().getId());
        wmMaterial.setType((short)0);
        wmMaterial.setUrl(path);
        wmMaterial.setIsCollection((short)0);
        wmMaterial.setCreatedTime(new Date());
        int insert = wmMaterialMapper.insert(wmMaterial);
        if(insert<1){
            return ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR);
        }

        return ResponseResult.okResult(wmMaterial);
    }

    @Override
    public ResponseResult findList(WmMaterialDto dto) {
        //1.检查参数
        dto.checkParam();

        //2.分页查询
        Page page = new Page(dto.getPage(),dto.getSize());
        LambdaQueryWrapper<WmMaterial> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        //是否收藏
        if(dto.getIsCollection() != null && dto.getIsCollection()  ==1){
            lambdaQueryWrapper.eq(WmMaterial::getIsCollection,dto.getIsCollection());
        }

        //按照用户查询
        lambdaQueryWrapper.eq(WmMaterial::getUserId,WmThreadLocalUtil.getUser().getId());

        //按照时间倒序
        lambdaQueryWrapper.orderByDesc(WmMaterial::getCreatedTime);


        page = page(page,lambdaQueryWrapper);

        //3.结果返回
        ResponseResult responseResult = new PageResponseResult(dto.getPage(),dto.getSize(),(int)page.getTotal());
        responseResult.setData(page.getRecords());
        return responseResult;
    }

    /**
     * 删除图片
     * @param id
     * @return
     */
    @Override
    public ResponseResult delete(Integer id) {
        //判空
        if(id == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID,"参数失效");
        }
        WmMaterial wmMaterial = getById(id);
        if(wmMaterial == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST,"数据不存在");
        }
       //删除图片
        boolean remove = this.remove(Wrappers.<WmMaterial>lambdaQuery().eq(WmMaterial::getId, id));
        if(!remove){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID,"文件删除失败");
        }
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS.getCode(),"操作成功");
    }

    /**
     * 取消收藏
     * @param id
     * @return
     */
    @Override
    public ResponseResult unfavorite(Integer id) {
        boolean unfavorite = this.update(Wrappers.<WmMaterial>lambdaUpdate().set(WmMaterial::getIsCollection, 0).eq(WmMaterial::getId, id));
        if(!unfavorite){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID,"参数失效");
        }
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS.getCode(),"操作成功");
    }

    /**
     * 收藏
     * @param id
     * @return
     */
    @Override
    public ResponseResult favorite(Integer id) {
        boolean favorite = this.update(Wrappers.<WmMaterial>lambdaUpdate().set(WmMaterial::getIsCollection, 1).eq(WmMaterial::getId, id));
        if(!favorite){
            return ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR);
        }
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }
}
