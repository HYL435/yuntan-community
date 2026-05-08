package com.yuntan.ai.service.impl;

import com.yuntan.ai.service.AiRateLimitService;
import com.yuntan.common.context.BaseContext;
import com.yuntan.common.exception.AiRateLimitException;
import com.yuntan.common.utils.BaseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class AiRateLimitServiceImpl implements AiRateLimitService {

    private final StringRedisTemplate redisTemplate;

    /**
     * 检查用户是否超出限制
     */
    @Override
    public void checkUserLimit() {

        Long userId = BaseContext.getUserId();

        // 检查用户是否超出限制，假设每分钟限制 5 次请求
        checkMinuteLimit(userId);
        // 检查用户是否超出限制，假设每天限制 50 次请求
        checkDailyLimit(userId);

    }

    /**
     * 检查IP是否超出限制，假设每分钟限制 30 次请求
     */
    @Override
    public void checkIpLimit() {
        String ip = BaseUtil.getClientIp(BaseContext.getRequest());
        String key = "ai:limit:ip:minute:" + ip;

        Long count = redisTemplate.opsForValue().increment(key);
        // 判断是否首次访问，如果是首次访问则设置过期时间为 1 分钟
        if (count != null && count == 1) {
            redisTemplate.expire(key, Duration.ofMinutes(1));
        }

        // 如果计数器超过 30，则抛出异常，提示IP请求过于频繁
        if (count != null && count > 30) {
            throw new AiRateLimitException("当前IP请求过于频繁，请稍后再试");
        }
    }

    /**
     * 检查用户是否超出限制，每天限制 50 次请求
     * @param userId
     */
    private void checkDailyLimit(Long userId) {
        String today = LocalDate.now().toString();
        String key = "ai:limit:user:daily:" + userId + ":" + today;

        // 创建一个计数器，如果计数器不存在则创建，初始值为 1
        Long count = redisTemplate.opsForValue().increment(key);

        // 判断是否首次访问，如果是首次访问则设置过期时间为 2 天，开始计数
        if (count != null && count == 1) {
            redisTemplate.expire(key, Duration.ofDays(2));
        }

        // 如果计数器超过 50，则抛出异常，提示用户请求过于频繁
        if (count != null && count > 50) {
            throw new AiRateLimitException("今日AI请求次数已达上限，请明天再试");
        }
    }

    /**
     * 检查用户是否超出限制，每分钟限制 5 次请求
     * @param userId
     */
    private void checkMinuteLimit(Long userId) {
        String key = "ai:limit:user:minute:" + userId;

        // 增加计数器，如果计数器不存在则创建，初始值为 1
        Long count = redisTemplate.opsForValue().increment(key);

        // 判断是否首次访问，如果是首次访问则设置过期时间为 1 分钟，开始计数
        if (count != null && count == 1) {
            // 设置过期时间为 1 分钟
            redisTemplate.expire(key, Duration.ofMinutes(1));
        }

        // 如果计数器超过 5，则抛出异常，提示用户请求过于频繁
        if (count != null && count > 5) {
            throw new AiRateLimitException("请求过于频繁，请稍后再试");
        }

    }
}
