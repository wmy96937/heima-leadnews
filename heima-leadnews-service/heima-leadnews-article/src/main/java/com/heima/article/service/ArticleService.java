package com.heima.article.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.article.dtos.ArticleDto;
import com.heima.model.article.dtos.ArticleHomeDto;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.common.dtos.ResponseResult;

import java.util.List;


/**
 * @author ASUS
 * @date 2024-11-11 16:22
 **/



public interface  ArticleService extends IService<ApArticle> {
    ResponseResult load(Short loadtype, ArticleHomeDto dto);

    ResponseResult saveArticle(ArticleDto dto);
}
