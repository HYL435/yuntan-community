package com.yuntan.content.stat.filter;


import com.yuntan.content.stat.service.ISiteStatService;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 全站访客统计过滤器
 *
 * 功能：
 * 1. 拦截所有请求
 * 2. 过滤静态资源 / 后台接口
 * 3. 提取客户端标识（IP）
 * 4. 调用 UV 统计服务
 */
@Component
@RequiredArgsConstructor
public class SiteUvFilter implements Filter {

    private final ISiteStatService siteStatService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;

        String uri = req.getRequestURI();

        // ===== 判断是否需要统计 =====
        if (shouldCount(uri)) {

            // ===== 获取客户端标识 =====
            String clientId = buildClientId(req);

            // ===== 记录访问 =====
            siteStatService.recordVisit(clientId);
        }

        chain.doFilter(request, response);
    }

    /**
     * 是否统计该请求
     */
    private boolean shouldCount(String uri) {
        if (uri == null) {
            return false;
        }

        // 只统计前台接口
        if (!uri.startsWith("/front")) {
            return false;
        }

        // 过滤静态资源
        return !(uri.endsWith(".js")
                || uri.endsWith(".css")
                || uri.endsWith(".png")
                || uri.endsWith(".jpg")
                || uri.endsWith(".ico"));
    }

    /**
     * 构建客户端唯一标识
     *
     * 推荐组合：IP + User-Agent（更抗刷）
     */
    private String buildClientId(HttpServletRequest request) {

        // 获取客户端IP（兼容代理）
        String ip = getClientIp(request);

        // 获取 User-Agent，防止同一 IP 刷新导致的重复统计
        String ua = request.getHeader("User-Agent");
        if (ua == null) {
            ua = "";
        }

        return ip + ":" + ua.hashCode();
    }

    /**
     * 获取真实 IP（兼容代理）
     */
    private String getClientIp(HttpServletRequest request) {

        // 获取代理 IP（可能包含多个 IP，取第一个）
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isBlank() && !"unknown".equalsIgnoreCase(ip)) {
            return ip.split(",")[0].trim();
        }

        // 获取非代理 IP。优先级高于代理 IP，防止 IP 欺骗
        ip = request.getHeader("X-Real-IP");
        if (ip != null && !ip.isBlank()) {
            return ip.trim();
        }

        return request.getRemoteAddr();
    }
}