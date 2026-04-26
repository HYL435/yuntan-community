package com.yuntan.interaction.collect.manager;

import com.yuntan.interaction.collect.entity.ArticleCollect;
import com.yuntan.interaction.collect.mapper.CollectMapper;
import com.yuntan.infra.redis.RedisConstant;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import com.yuntan.common.context.BaseContext;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class CollectManager {

    private final CollectMapper collectMapper;

    private final StringRedisTemplate redisTemplate;

    public void collectEvent(Long articleId) {
        // 发布收藏事件
        Long userId = BaseContext.getUserId();

        // 检查是否已收藏
        boolean isCollected = collectMapper.isCollect(articleId, userId) > 0;

        if (isCollected) {
            // 取消收藏
            collectMapper.deleteCollect(articleId, userId);
            // 更新Redis计数
            updateCollectCount(articleId, -1);
        } else {
            // 添加收藏记录
            ArticleCollect collect = ArticleCollect.builder()
                    .articleId(articleId)
                    .userId(userId)
                    .createTime(LocalDateTime.now())
                    .build();
            collectMapper.collect(collect);
            // 更新Redis计数
            updateCollectCount(articleId, 1);
        }
    }

    /**
     * 更新收藏数
     */
    private void updateCollectCount(Long articleId, int delta) {
        String key = RedisConstant.ARTICLE_COUNTER_HASH_PREFIX + articleId;

        // 原子递增Redis Hash中的collectCount
        redisTemplate.opsForHash().increment(key, "collectCount", delta);

        // 将ID加入脏数据集合，用于后续同步到数据库
        redisTemplate.opsForSet().add(RedisConstant.DIRTY_SET_KEY, String.valueOf(articleId));
    }

}