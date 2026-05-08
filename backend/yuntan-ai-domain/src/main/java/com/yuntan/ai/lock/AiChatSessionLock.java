package com.yuntan.ai.lock;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class AiChatSessionLock {

    private final StringRedisTemplate redisTemplate;

    private static final String LOCK_KEY_PREFIX = "ai:chat:lock:";


    /**
     * 尝试获取会话生成锁
     *
     * @param sessionId 会话 ID
     * @param requestId 请求 ID
     * @return 是否获取成功
     */
    public boolean tryLockSessionGenerating(Long sessionId, String requestId) {

        // 尝试设置锁，过期时间为 5 分钟
        Boolean success = redisTemplate.opsForValue()
                .setIfAbsent(LOCK_KEY_PREFIX, requestId, Duration.ofMinutes(5));

        return Boolean.TRUE.equals(success);
    }

    /**
     * 释放会话生成锁
     *
     * @param sessionId 会话 ID
     * @param requestId 锁的请求 ID
     */
    public void unlockSessionGenerating(Long sessionId, String requestId) {

        // 释放锁，如果锁的请求 ID 与当前请求 ID 一致，则释放锁
        String currentRequestId = redisTemplate.opsForValue().get(LOCK_KEY_PREFIX);

        if (requestId.equals(currentRequestId)) {
            redisTemplate.delete(LOCK_KEY_PREFIX);
        }
    }

}
