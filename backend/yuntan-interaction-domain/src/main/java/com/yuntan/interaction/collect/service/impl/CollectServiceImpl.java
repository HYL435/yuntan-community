package com.yuntan.interaction.collect.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuntan.common.context.BaseContext;
import com.yuntan.interaction.collect.entity.ArticleCollect;
import com.yuntan.interaction.collect.manager.CollectManager;
import com.yuntan.interaction.collect.mapper.CollectMapper;
import com.yuntan.interaction.collect.service.ICollectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CollectServiceImpl extends ServiceImpl<CollectMapper, ArticleCollect> implements ICollectService {

    private final CollectManager collectManager;

    /**
     * 收藏取消收藏文章
     */
    @Override
    public void collect(Long articleId) {

        // 发布收藏事件
        collectManager.collectEvent(articleId);

    }

    /**
     * 是否收藏
     */
    @Override
    public Boolean isCollected(Long articleId) {

        Long userId = BaseContext.getUserId();

        return this.count(
                new LambdaQueryWrapper<ArticleCollect>()
                        .eq(ArticleCollect::getUserId, userId)
                        .eq(ArticleCollect::getArticleId, articleId)
        ) > 0;
    }
}
