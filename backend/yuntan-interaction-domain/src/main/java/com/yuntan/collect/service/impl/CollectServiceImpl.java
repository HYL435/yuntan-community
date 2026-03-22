package com.yuntan.collect.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuntan.collect.entity.ArticleCollect;
import com.yuntan.collect.manager.CollectManager;
import com.yuntan.collect.mapper.CollectMapper;
import com.yuntan.collect.service.ICollectService;
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
}
