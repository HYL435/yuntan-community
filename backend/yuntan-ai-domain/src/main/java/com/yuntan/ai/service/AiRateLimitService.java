package com.yuntan.ai.service;

public interface AiRateLimitService {


    /**
     * 检查用户限流，如果超过限制则抛出异常
     */
    void checkUserLimit();

    /**
     * 检查IP限流，如果超过限制则抛出异常
     */
    void checkIpLimit();
}
