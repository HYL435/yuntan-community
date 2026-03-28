package com.yuntan.interaction.like.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yuntan.interaction.like.entity.ArticleLike;

public interface ILikeService extends IService<ArticleLike> {

    /**
     * 点赞文章或取消点赞
     */
    void like(Long articleId);
}
