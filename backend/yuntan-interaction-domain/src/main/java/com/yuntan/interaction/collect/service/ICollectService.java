package com.yuntan.interaction.collect.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yuntan.interaction.collect.entity.ArticleCollect;

public interface ICollectService extends IService<ArticleCollect> {

    /**
     * 收藏文章
     */
    void collect(Long articleId);
}
