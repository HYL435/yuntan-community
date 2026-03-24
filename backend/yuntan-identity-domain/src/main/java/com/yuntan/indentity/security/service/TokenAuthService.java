package com.yuntan.indentity.security.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public interface TokenAuthService {

    /**
     * 解析 token 获取用户 ID
     * 也可以不传递参数，直接从请求上下文中获取 HttpServletRequest 对象
     */
    Map<String, Object> parseRequest(HttpServletRequest request);

}
