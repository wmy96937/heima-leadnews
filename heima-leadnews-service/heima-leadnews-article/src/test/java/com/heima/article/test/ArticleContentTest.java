package com.heima.article.test;

import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.heima.article.mapper.ApArticleContentMapper;
import com.heima.article.mapper.ApArticleMapper;
import com.heima.file.service.impl.MinIOFileStorageService;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.article.pojos.ApArticleContent;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashMap;
/**
 * @author ASUS
 * @date 2024-11-12 16:20
 **/
@SpringBootTest
public class ArticleContentTest {

    @Autowired
    private ApArticleContentMapper apArticleContentMapper;

    @Autowired
    private Configuration configuration;

    @Autowired
    private MinIOFileStorageService minIOFileStorageService;

    @Autowired
    private ApArticleMapper apArticleMapper;
    @Test
    void ApArticleContenttest() throws Exception{
        //1.拿到当前文章对应的内容
        ApArticleContent apArticleContent = apArticleContentMapper.selectOne(Wrappers.lambdaQuery(ApArticleContent.class).eq(ApArticleContent::getArticleId, 1383827911810011137L));
        //判空
        if(ObjectUtils.isNotEmpty(apArticleContent) && ObjectUtils.isNotEmpty(apArticleContent.getContent())) {
            //freemarker生成静态页面
            Template template = configuration.getTemplate("article.ftl");

            StringWriter out = new StringWriter();

            //保存内容
            HashMap<String, Object> map = new HashMap<>();
            map.put("content", JSONArray.parseArray(apArticleContent.getContent()));

            //生成静态页面
            template.process(map, out);
            //拿到输入流
            InputStream is = new ByteArrayInputStream(out.toString().getBytes());

            String path = minIOFileStorageService.uploadHtmlFile("",  apArticleContent.getArticleId() + ".html", is);

            ApArticle apArticle = new ApArticle();
            apArticle.setId(apArticleContent.getArticleId());
            apArticle.setStaticUrl(path);
            apArticleMapper.updateById(apArticle);


        }
    }
}
