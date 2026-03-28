package com.yuntan.interaction.like.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuntan.interaction.like.entity.ArticleLike;
import com.yuntan.interaction.like.manager.LikeManager;
import com.yuntan.interaction.like.mapper.LikeMapper;
import com.yuntan.interaction.like.service.ILikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LikeServiceImpl extends ServiceImpl<LikeMapper, ArticleLike> implements ILikeService {

    private final LikeManager likeManager;

    /**
     * 点赞
     */
    @Override
    public void like(Long articleId) {

        // 点赞事件
        likeManager.likeEvent(articleId);

    }
}
