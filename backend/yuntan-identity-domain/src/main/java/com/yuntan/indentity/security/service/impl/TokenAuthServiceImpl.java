package com.yuntan.indentity.security.service.impl;

import com.yuntan.indentity.config.JwtProperties;
import com.yuntan.indentity.security.service.TokenAuthService;
import com.yuntan.indentity.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class TokenAuthServiceImpl implements TokenAuthService {

    private final JwtProperties jwtProperties;
    private final JwtUtil jwtUtil;

    /**
     * 解析请求中的Token，获取用户信息和角色信息
     */
    @Override
    public Map<String, Object> parseRequest(HttpServletRequest request) {
        // 从请求头中获取Token
        String token = request.getHeader(jwtProperties.getHeader());

        if (token == null || token.isBlank()) {
            return null;
        }


        // 解析Token，获取用户信息和角色信息
        return jwtUtil.parseToken(token);

    }
}
