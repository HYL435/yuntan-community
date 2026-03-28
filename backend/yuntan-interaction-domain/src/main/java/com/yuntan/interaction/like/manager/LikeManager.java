package com.yuntan.interaction.like.manager;

import com.yuntan.infra.redis.RedisConstant;
import com.yuntan.interaction.like.entity.ArticleLike;
import com.yuntan.interaction.like.mapper.LikeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import yuntan.common.context.BaseContext;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class LikeManager {

    private final LikeMapper likeMapper;
    private final StringRedisTemplate redisTemplate;

    /**
     * 点赞事件
     */
    public void likeEvent(Long articleId) {

        Long userId = BaseContext.getUserId();

        // 检查是否已点赞
        boolean isLiked = likeMapper.isLike(articleId, userId) > 0;

        if (isLiked) {
            // 取消点赞
            likeMapper.deleteLike(articleId, userId);
            // 更新Redis计数
            updateLikeCount(articleId, -1);
        } else {
            // 添加点赞记录
            ArticleLike like = ArticleLike.builder()
                    .articleId(articleId)
                    .userId(userId)
                    .createTime(LocalDateTime.now())
                    .build();
            likeMapper.like(like);
            // 更新Redis计数
            updateLikeCount(articleId, 1);
        }

    }

    /**
     * 更新点赞数
     */
    private void updateLikeCount(Long articleId, int delta) {
        String key = RedisConstant.ARTICLE_COUNTER_HASH_PREFIX + articleId;

        // 原子递增Redis Hash中的likeCount
        redisTemplate.opsForHash().increment(key, "likeCount", delta);

        // 将ID加入脏数据集合，用于后续同步到数据库
        redisTemplate.opsForSet().add(RedisConstant.DIRTY_SET_KEY, String.valueOf(articleId));
    }
}
