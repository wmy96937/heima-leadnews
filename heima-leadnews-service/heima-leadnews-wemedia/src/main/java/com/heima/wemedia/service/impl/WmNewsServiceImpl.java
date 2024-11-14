package com.heima.wemedia.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.common.WemediaConstants;
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
import com.heima.wemedia.service.WmNewsService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.apache.hadoop.hbase.Version.user;

/**
 * @author ASUS
 * @date 2024-11-14 16:04
 **/
@Service
public class WmNewsServiceImpl extends ServiceImpl<WmNewsMapper, WmNews> implements WmNewsService {

    /**
     * 文章条件分页列表
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

        //type设置,暂时设置空
        if (dto.getType() == -1) {
            wmNews.setType(null);
        }

        //保存或修改文章
        saveOrUpdateWmNews(wmNews);


        //3.关联正文图片和素材的关系
        //判断是否为草稿，为草稿不保存关系
        if (dto.getStatus().equals(WmNews.Status.NORMAL.getCode())) {
            return ResponseResult.okResult(null);
        }

        //拿到文章中的正文，为json格式，转为map
        String content = dto.getContent();

        //解析正文，拿到图片对象
        List<String>  wmMaterials =ectractUrlInfo(content);

        //关联文章图片和素材的关系
        saveRelativeInfoForContent(wmNews.getId(), wmMaterials);

        //3.关联封面图片和素材的关系




        return ResponseResult.okResult(wmNews.getId());
    }

    @Autowired
    private WmMaterialMapper wmMaterialMapper;
    private void saveRelativeInfoForContent(Integer id, List<String> wmMaterials) {
        //参数判空
        if (ObjectUtils.isEmpty(id) || ObjectUtils.isEmpty(wmMaterials)) {
            throw new RuntimeException("正文图片或id为空");
        }
        //根据地址找到素材id
        List<WmMaterial> wmMaterials1 = wmMaterialMapper.selectList(Wrappers.<WmMaterial>lambdaQuery().in(WmMaterial::getUrl, wmMaterials));

        //正文图片对应素材id
        List<Integer> collect = wmMaterials1.stream().map(wmMaterial -> wmMaterial.getId()).collect(Collectors.toList());
        //保存关系
        wmNewsMaterialMapper.saveRelations(collect, id, WemediaConstants.WM_CONTENT_REFERENCE);
    }


    /**
     * 解析文章内容，获取图片列表
     * @param content
     * @return
     */
    private List<String> ectractUrlInfo(String content) {
        //存放图片地址
        ArrayList<String> wmMaterials = new ArrayList<>();

        //正文
        List<Map> maps = JSON.parseArray(content, Map.class);
        if (maps != null && maps.size() > 0) {
            return null;
        }
        for (Map map : maps) {
            //判断是否是图片
            if (map.get("type").equals("image")) {
                //拿到图片url
                String imgUrl = map.get("value").toString();
                wmMaterials.add(imgUrl);

                }
            }
        return wmMaterials;
    }

    @Autowired
    private WmNewsMaterialMapper wmNewsMaterialMapper;

    /**
     * 新增或修改文章
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
        }else {
            //修改，删除文章图片和素材关系
            wmNewsMaterialMapper.delete(Wrappers.<WmNewsMaterial>lambdaQuery().eq(WmNewsMaterial::getNewsId, wmNews.getId()));
            //修改文章
            updateById(wmNews);
        }
    }
}
