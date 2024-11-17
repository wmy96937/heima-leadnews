package com.heima.article.feign;

import com.heima.apis.article.IArticleClient;
import com.heima.article.service.ArticleService;
import com.heima.model.article.dtos.ArticleDto;
import com.heima.model.common.dtos.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author ASUS
 * @date 2024-11-15 11:32
 **/
@RestController
public class ArticleClient implements IArticleClient {
    @Autowired
    private ArticleService articleService;
    @Override
    @PostMapping("/api/v1/article/save")
    public ResponseResult saveArticle(ArticleDto dto) {
        return articleService.saveArticle(dto);
    }
}
