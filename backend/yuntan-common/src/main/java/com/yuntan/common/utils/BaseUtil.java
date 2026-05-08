package com.yuntan.common.utils;

import jakarta.servlet.http.HttpServletRequest;

public class BaseUtil {

        /**
        * 获取当前登录用户的IP
        *
        * @return 当前登录用户的IP
        */
        // 获取客户端 IP 地址
        public static String getClientIp(HttpServletRequest request) {

            // 先尝试从 X-Forwarded-For 头获取（可能有多个 IP，取第一个），再尝试 X-Real-IP，最后使用 request.getRemoteAddr()
            String ip = request.getHeader("X-Forwarded-For");
            if (ip != null && !ip.isBlank()) {
                return ip.split(",")[0].trim();
            }
            // 尝试 X-Real-IP 头获取
            ip = request.getHeader("X-Real-IP");
            if (ip != null && !ip.isBlank()) {
                return ip.trim();
            }

            // 最后使用 request.getRemoteAddr() 获取 IP 地址
            return request.getRemoteAddr() == null
                    ? "unknown"
                    : request.getRemoteAddr();

        }

}
