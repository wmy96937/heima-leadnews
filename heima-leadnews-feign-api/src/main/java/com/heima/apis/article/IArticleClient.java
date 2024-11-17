package com.heima.apis.article;

import com.heima.config.FeignClientConfig;
import com.heima.model.article.dtos.ArticleDto;
import com.heima.model.common.dtos.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author ASUS
 * @date 2024-11-15 11:28
 **/
@FeignClient(value = "leadnews-article",configuration = FeignClientConfig.class)
public interface IArticleClient {
    @PostMapping("/api/v1/article/save")
    public ResponseResult saveArticle(@RequestBody ArticleDto dto);
}
