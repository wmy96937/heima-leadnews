package com.heima.wemedia.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.common.constants.WemediaConstants;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.wemedia.dtos.WmNewsDto;
import com.heima.model.wemedia.dtos.WmNewsPageReqDto;
import com.heima.model.wemedia.pojos.WmMaterial;
import com.heima.model.wemedia.pojos.WmNews;
import com.heima.model.wemedia.pojos.WmNewsMaterial;
import com.heima.utils.thread.WmThreadLocalUtil;
import com.heima.wemedia.mapper.WmMaterialMapper;
import com.heima.wemedia.mapper.WmNewsMapper;
import com.heima.wemedia.mapper.WmNewsMaterialMapper;
import com.heima.wemedia.service.WmNewsAutoScanService;
import com.heima.wemedia.service.WmNewsService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author ASUS
 * @date 2024-11-14 16:04
 **/
@Service
public class WmNewsServiceImpl extends ServiceImpl<WmNewsMapper, WmNews> implements WmNewsService {


    @Autowired
    private WmNewsMaterialMapper wmNewsMaterialMapper;


    /**
     * 文章条件分页列表
     *
     * @param dto
     * @return
     */
    @Override
    public ResponseResult findAll(WmNewsPageReqDto dto) {
        if (ObjectUtils.isEmpty(dto)) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        dto.checkParam();

        Page<WmNews> page = new Page<>(dto.getPage(), dto.getSize());


        //状态精确查询
        LambdaQueryWrapper<WmNews> lambdaQueryWrapper = Wrappers.lambdaQuery();
        if (dto.getStatus() != null) {
            lambdaQueryWrapper.eq(WmNews::getStatus, dto.getStatus());
        }

        //频道精确查询
        if (dto.getChannelId() != null) {
            lambdaQueryWrapper.eq(WmNews::getChannelId, dto.getChannelId());
        }

        //时间范围查询
        if (dto.getBeginPubDate() != null && dto.getEndPubDate() != null) {
            lambdaQueryWrapper.between(WmNews::getPublishTime, dto.getBeginPubDate(), dto.getEndPubDate());
        }

        //关键字模糊查询
        if (StringUtils.isNotBlank(dto.getKeyword())) {
            lambdaQueryWrapper.like(WmNews::getTitle, dto.getKeyword());
        }

        //查询当前登录用户的文章
        lambdaQueryWrapper.eq(WmNews::getUserId, WmThreadLocalUtil.getUser().getId());

        //发布时间倒序查询
        lambdaQueryWrapper.orderByDesc(WmNews::getCreatedTime);

        page = page(page, lambdaQueryWrapper);

        //3.结果返回
        ResponseResult responseResult = new PageResponseResult(dto.getPage(), dto.getSize(), (int) page.getTotal());
        responseResult.setData(page.getRecords());

        return responseResult;
    }


    /**
     * 文章详情
     *
     * @param id
     * @return
     */
    @Override
    public ResponseResult detail(Integer id) {
        if (ObjectUtils.isEmpty(id)) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        WmNews wmNews = getById(id);
        //判空
        if (ObjectUtils.isEmpty(wmNews)) {
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST, "文章不存在");
        }
        return ResponseResult.okResult(wmNews);
    }

    /**
     * 删除文章
     *
     * @param id
     * @return
     */
    @Transactional
    @Override
    public ResponseResult deleteById(Integer id) {
        //判空
        if (ObjectUtils.isEmpty(id)) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID, "文章Id不可缺少");
        }
        WmNews wmNews = getById(id);
        //如果文章已经发布则不能删除
        if (wmNews.getStatus().equals(WmNews.Status.PUBLISHED.getCode())) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID, "文章已发布，不能删除");
        }

        //删除文章
        boolean remove = removeById(id);
        if (!remove) {
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST, "文章不存在");
        }
        //删除文章和图片的关系
        LambdaQueryWrapper<WmNewsMaterial> queryWrapper = Wrappers.<WmNewsMaterial>lambdaQuery();
        queryWrapper.eq(WmNewsMaterial::getNewsId, id);
        wmNewsMaterialMapper.delete(queryWrapper);

        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS.getCode(), "操作成功");
    }

    /**
     * 文章上下架
     *
     * @param dto
     * @return
     */
    @Override
    public ResponseResult upperAndLowerShelves(WmNewsDto dto) {
        //id判空
        if (ObjectUtils.isEmpty(dto.getId())) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID, "文章id不可缺少");
        }
        WmNews wmNews = getById(dto.getId());
        if (ObjectUtils.isEmpty(wmNews)) {
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST, "文章不存在");
        }
        //文章发布状态
        if (!wmNews.getStatus().equals(WmNews.Status.PUBLISHED.getCode())) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID, "当前文章不是发布状态，不能下架");
        }
        wmNews.setEnable(dto.getEnable());
        try {
            updateById(wmNews);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS.getCode(), "操作成功");

    }

    @Autowired
    private WmMaterialMapper wmMaterialMapper;


    /**
     * 文章提交
     *
     * @param dto
     * @return
     */
    @Override
    public ResponseResult submitNews(WmNewsDto dto) {
        //1.判空操作
        if (ObjectUtils.isEmpty(dto) || dto.getContent() == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        //2.保存或修改文章
        WmNews wmNews = new WmNews();
        BeanUtils.copyProperties(dto, wmNews);
        //封面图片设置
        if (dto.getImages() != null && dto.getImages().size() > 0) {
            wmNews.setImages(StringUtils.join(dto.getImages(), ","));
        }

        //type设置,暂时设置空(无图0、单图1、三图3)
        if (dto.getType() == -1) {
            wmNews.setType(null);
        }

        //保存或修改文章
        saveOrUpdateWmNews(wmNews);


        //3.关联正文图片和素材的关系
        //判断是否为草稿，为草稿不保存关系
        if (dto.getStatus().equals(WmNews.Status.NORMAL.getCode())) {
            return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
        }

        //拿到文章中的正文，为json格式，转为map
        String content = dto.getContent();

        //解析正文，拿到图片对象
        List<String> wmMaterials = ectractUrlInfo(content);

        //关联文章图片和素材的关系
        saveRelativeInfoForContent(wmNews.getId(), wmMaterials);

        //3.关联封面图片和素材的关系
        saveRelativeInfoForCover(dto, wmNews, wmMaterials);

        //自动审核文章
        wmNewsAutoScanService.autoScanWmNews(wmNews.getId());

        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }
@Autowired
private WmNewsAutoScanService wmNewsAutoScanService;

    /**
     * 关联封面图片和素材的关系
     * @param dto         前端提交的数据
     * @param wmNews      文章对象
     * @param wmMaterials 文章中解析出的图片地址集合
     */
    private void saveRelativeInfoForCover(WmNewsDto dto, WmNews wmNews, List<String> wmMaterials) {
        //封面图片
        List<String> images = dto.getImages();
        if (dto.getType() == -1) {
            //自动判断
            if (wmMaterials.size() >= 1 && wmMaterials.size() < 3) {
                //单图
                wmNews.setType(WemediaConstants.WM_NEWS_SINGLE_IMAGE);
                images = wmMaterials.stream().limit(1).collect(Collectors.toList());
            } else if (wmMaterials.size() >= 3) {
                //三图
                wmNews.setType(WemediaConstants.WM_NEWS_MANY_IMAGE);
                images = wmMaterials.stream().limit(3).collect(Collectors.toList());
            } else {
                wmNews.setType(WemediaConstants.WM_NEWS_NONE_IMAGE);
            }
            //保存到对象中更新到数据库
            if (!ObjectUtils.isEmpty(images) && !images.isEmpty()) {
                wmNews.setImages(StringUtils.join(images, ","));
            }
            updateById(wmNews);
        }

        //更新封面与素材的关系
        if (!ObjectUtils.isEmpty(images) && !images.isEmpty()) {
            saveRelationInfo(wmNews.getId(), images, WemediaConstants.WM_COVER_REFERENCE);
        }


    }


    /**
     * 关联文章正文图片和素材的关系
     * @param id
     * @param wmMaterials
     */
    private void saveRelativeInfoForContent(Integer id, List<String> wmMaterials) {
        saveRelationInfo(id, wmMaterials,WemediaConstants.WM_CONTENT_REFERENCE);
    }

    /**
     * 保存文章图片与素材的关系
     * @param id
     * @param wmMaterials
     */
    private void saveRelationInfo(Integer id, List<String> wmMaterials,Short type) {
        //参数不为空
        if (!ObjectUtils.isEmpty(wmMaterials) && wmMaterials != null) {


            //根据地址找到素材id
            List<WmMaterial> wmMaterials1 = wmMaterialMapper.selectList(Wrappers.<WmMaterial>lambdaQuery().in(WmMaterial::getUrl, wmMaterials));

            //正文图片对应素材id
            List<Integer> collect = wmMaterials1.stream().map(wmMaterial -> wmMaterial.getId()).collect(Collectors.toList());
            //保存关系
            wmNewsMaterialMapper.saveRelations(collect, id,type );
        }
    }


    /**
     * 解析文章内容，获取图片列表
     *
     * @param content
     * @return
     */
    private List<String> ectractUrlInfo(String content) {
        //存放图片地址
        ArrayList<String> wmMaterials = new ArrayList<>();

        List<Map> maps = JSON.parseArray(content, Map.class);
        for (Map map : maps) {
            if(map.get("type").equals("image")){
                String imgUrl = (String) map.get("value");
                wmMaterials.add(imgUrl);
            }
        }
        return wmMaterials;
    }


    /**
     * 新增或修改文章
     *
     * @param wmNews
     */
    private void saveOrUpdateWmNews(WmNews wmNews) {
        //封装参数
        wmNews.setUserId(WmThreadLocalUtil.getUser().getId());
        wmNews.setEnable((short) 1);
        wmNews.setCreatedTime(new Date());
        wmNews.setSubmitedTime(new Date());

        //判断主键id
        if (wmNews.getId() == null) {
            //新增
            save(wmNews);
        } else {
            //修改，删除文章图片和素材关系
            wmNewsMaterialMapper.delete(Wrappers.<WmNewsMaterial>lambdaQuery().eq(WmNewsMaterial::getNewsId, wmNews.getId()));
            //修改文章
            updateById(wmNews);
        }
    }
}
