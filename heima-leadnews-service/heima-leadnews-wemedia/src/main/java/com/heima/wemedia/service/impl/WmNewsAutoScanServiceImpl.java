package com.heima.wemedia.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.heima.apis.article.IArticleClient;
import com.heima.common.aliyun.GreenImageScan;
import com.heima.common.aliyun.GreenTextScan;
import com.heima.model.article.dtos.ArticleDto;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.pojos.WmChannel;
import com.heima.model.wemedia.pojos.WmNews;
import com.heima.model.wemedia.pojos.WmUser;
import com.heima.wemedia.mapper.WmChannelMapper;
import com.heima.wemedia.mapper.WmNewsMapper;
import com.heima.wemedia.mapper.WmUserMapper;
import com.heima.wemedia.service.WmNewsAutoScanService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.*;

/**
 * @author ASUS
 * @date 2024-11-15 15:11
 **/
@Service
public class WmNewsAutoScanServiceImpl implements WmNewsAutoScanService {
    @Autowired
    private WmNewsMapper wmNewsMapper;
    @Autowired
    private GreenTextScan greenTextScan;
    @Autowired
    private GreenImageScan greenImageScan;
    @Autowired
    private IArticleClient articleClient;
    @Autowired
    private WmUserMapper wmUserMapper;
    @Autowired
    private WmChannelMapper wmChannelMapper;


    /**
     * 文章自动审核
     * @param id
     */
    @Override
    @Async
    public void autoScanWmNews(Integer id) {
        //查询文章
        WmNews wmNews = wmNewsMapper.selectById(id);
        if (!ObjectUtils.isEmpty(wmNews)) {
            if (wmNews.getStatus().equals((short) 1)) {
                //拿到文本和图片
                Map<String, Object> map = handleTextAndImage(wmNews);

                //文本审核
                String content = (String) map.get("content");
                boolean boolText = autoScanText(content, wmNews);
                if (!boolText) {
                    return;
                }

                //图片审核
                ArrayList<String> images = (ArrayList<String>) map.get("images");
                boolean boolImage = autoScanImage(images, wmNews);
                if (!boolImage) {
                    return;
                }

                //审核通过，保存app端相关数据
               ResponseResult responseResult= saveAppArticle(wmNews);
                if(!responseResult.getCode().equals(200)){
                    throw new RuntimeException("文章审核、保存app端数据失败");
                }
                //获得app文章id
                Long articleId =(Long) responseResult.getData();
                wmNews.setArticleId(articleId);
                updateWmNews(wmNews, (short) 9, "审核通过");
            }
        }
    }




    /**
     * 保存app端文章相关数据
     * @param wmNews
     * @return
     */
    private ResponseResult saveAppArticle(WmNews wmNews) {
        ArticleDto articleDto = new ArticleDto();
        BeanUtils.copyProperties(wmNews, articleDto);
        WmUser wmUser = wmUserMapper.selectById(wmNews.getUserId());
        //作者名称
        if (!ObjectUtils.isEmpty(wmUser.getName())) {
            articleDto.setAuthorName(wmUser.getName());
        }
        //作者id
        articleDto.setAuthorId(wmNews.getUserId().longValue());

        //频道
        WmChannel wmChannel = wmChannelMapper.selectById(wmNews.getChannelId());
        if (!ObjectUtils.isEmpty(wmChannel)) {
            articleDto.setChannelName(wmChannel.getName());
        }
        //布局
        articleDto.setLayout(wmNews.getType());

        //设置文章id
        if(wmNews.getArticleId() != null){
            articleDto.setId(wmNews.getArticleId());
        }
        articleDto.setCreatedTime(new Date());
        ResponseResult responseResult = articleClient.saveArticle(articleDto);
        return responseResult;
    }


    /**
     * 图片审核，返回true表示图片无违规，返回false表示图片有违规
     * @param images 文章图片集合
     * @param wmNews 对象
     * @return 审核结果
     */
    private boolean autoScanImage(ArrayList<String> images, WmNews wmNews) {

        Boolean boolImage = true;
        List<byte[]> imageBytes = new ArrayList<>();
        for (String image : images) {
            imageBytes.add(image.getBytes());
        }
        if (!ObjectUtils.isEmpty(images)) {
            try {
                Map map = greenImageScan.imageScan(imageBytes);
                if (map != null) {

                    if (map.get("suggestion").equals("block")) {
                        boolImage = false;
                        updateWmNews(wmNews, (short) 2, "图片内容包含违规词汇");
                    } else if (map.get("suggestion").equals("review")) {
                        boolImage = false;
                        updateWmNews(wmNews, (short) 3, "图片内容需要人工审核");
                    }
                }
            } catch (Exception e) {
                boolImage = false;
                throw new RuntimeException(e);
            }
        }
        return boolImage;
    }


    /**
     * 文章文本审核，返回true表示文本无违规，返回false表示文本有违规
     *
     * @param content 文章内容
     * @param wmNews  文章对象，修改状态和违规原因
     * @return 审核结果
     */
    private boolean autoScanText(String content, WmNews wmNews) {
        Boolean boolText = true;
        if (!ObjectUtils.isEmpty(content)) {
            try {
                Map map = greenTextScan.greeTextScan(content);
                if (map != null) {

                    if (map.get("suggestion").equals("block")) {
                        boolText = false;
                        updateWmNews(wmNews, (short) 2, "文章内容包含违规词汇");
                    } else if (map.get("suggestion").equals("review")) {
                        boolText = false;
                        updateWmNews(wmNews, (short) 3, "文章内容需要人工审核");
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return boolText;
    }


    /**
     * 修改文章状态和原因
     *
     * @param wmNews 文章对象
     * @param status 文章状态
     * @param msg    文章原因
     */
    private void updateWmNews(WmNews wmNews, short status, String msg) {
        wmNews.setStatus(status);
        wmNews.setReason(msg);
        wmNewsMapper.updateById(wmNews);

    }


    /**
     * 根据文章对象获得文章正文和正文图片、封面图片
     *
     * @param wmNews 文章对象
     * @return map结果
     */
    private Map<String, Object> handleTextAndImage(WmNews wmNews) {

        StringBuilder sbText = new StringBuilder();
        sbText.append(wmNews.getTitle());
        ArrayList<String> images = new ArrayList<>();


        if (wmNews.getContent() != null) {
            List<Map> maps = JSONArray.parseArray(wmNews.getContent(), Map.class);
            if (!ObjectUtils.isEmpty(maps)) {
                for (Map map : maps) {
                    if (map.get("type").equals("text")) {
                        sbText.append(map.get("value"));
                    } else if (map.get("type").equals("image")) {
                        images.add((String) map.get("value"));
                    }
                }
                //封面图片，采用，分割
                String cover = wmNews.getImages();
                if (cover != null) {
                    String[] split = cover.split(",");
                    images.addAll(Arrays.asList(split));
                }
            }
        }
        HashMap<String, Object> TextAndImage = new HashMap<>();
        TextAndImage.put("content", sbText.toString());
        TextAndImage.put("images", images);
        return TextAndImage;
    }
}
