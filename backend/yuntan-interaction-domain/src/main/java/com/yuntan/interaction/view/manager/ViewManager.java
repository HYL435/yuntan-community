package com.yuntan.interaction.view.manager;

import com.yuntan.infra.redis.RedisConstant;
import com.yuntan.interaction.view.entity.ArticleView;
import com.yuntan.interaction.view.mapper.ViewMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import com.yuntan.common.context.BaseContext;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class ViewManager {

    private final ViewMapper viewMapper;
    private final StringRedisTemplate redisTemplate;

    /**
     * 文章被浏览时调用的方法
     */
    public void viewEvent(Long articleId) {
        // 获取用户ID，如果没有登录则为null
        Long userId = BaseContext.getUserId();
        // 获取IP地址（需要从请求上下文中获取）
        String ip = getCurrentRequestIp(); // 这个方法需要你自己实现

        // 添加浏览记录到数据库
        addViewRecord(articleId, userId, ip);

        // 更新Redis计数
        updateViewCount(articleId, 1);
    }
    /**
     * 添加浏览记录到数据库
     */
    private void addViewRecord(Long articleId, Long userId, String ip) {

        ArticleView view = ArticleView.builder()
                .articleId(articleId)
                .userId(userId)
                .ip(ip)
                .createTime(LocalDateTime.now())
                .build();

        viewMapper.addView(view); // 需要在mapper中添加对应方法
    }

    /**
     * 获取当前请求的IP地址
     */
    private String getCurrentRequestIp() {
        // 从请求上下文中获取IP地址
        // 这通常需要注入 HttpServletRequest 或使用其他框架提供的工具类
        // 示例实现：
        try {
            HttpServletRequest request = BaseContext.getRequest();
            String ip = request.getHeader("X-Forwarded-For");
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("Proxy-Client-IP");
            }
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("WL-Proxy-Client-IP");
            }
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getRemoteAddr();
            }
            return ip;
        } catch (Exception e) {
            // 如果无法获取IP，返回默认值
            return "0.0.0.0";
        }
    }

    /**
     * 更新阅读量
     */
    private void updateViewCount(Long articleId, int delta) {
        String key = RedisConstant.ARTICLE_COUNTER_HASH_PREFIX + articleId;

        // 原子递增Redis Hash中的viewCount
        redisTemplate.opsForHash().increment(key, "viewCount", delta);

        // 将ID加入脏数据集合，用于后续同步到数据库
        redisTemplate.opsForSet().add(RedisConstant.DIRTY_SET_KEY, String.valueOf(articleId));
    }
}
