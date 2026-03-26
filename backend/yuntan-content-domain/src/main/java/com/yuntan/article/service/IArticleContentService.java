package com.yuntan.article.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yuntan.article.entity.ArticleContent;

import java.util.Optional;

public interface IArticleContentService extends IService<ArticleContent> {

    /**
     * 根据ID查询文章内容
     */
    Optional<String> getArticleContentById(Long articleContentId);

    /**
     * 添加文章内容
     */
    Long addArticleContent(Long id, String content);

    /**
     * 更新文章内容
     */
    void updateArticleContent(Long articleContentId, String content);
}
