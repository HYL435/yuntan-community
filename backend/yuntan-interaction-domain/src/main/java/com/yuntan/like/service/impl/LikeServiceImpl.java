package com.yuntan.like.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuntan.like.entity.ArticleLike;
import com.yuntan.like.manager.LikeManager;
import com.yuntan.like.mapper.LikeMapper;
import com.yuntan.like.service.ILikeService;
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

        likeManager.likeEvent(articleId);

    }
}
