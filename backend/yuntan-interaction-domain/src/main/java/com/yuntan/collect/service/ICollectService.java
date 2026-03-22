package com.yuntan.collect.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yuntan.collect.entity.ArticleCollect;

public interface ICollectService extends IService<ArticleCollect> {

    /**
     * 收藏文章
     */
    void collect(Long articleId);
}
