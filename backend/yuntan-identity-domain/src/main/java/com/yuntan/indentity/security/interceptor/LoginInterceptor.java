package com.yuntan.indentity.security.interceptor;

import com.yuntan.indentity.constant.KeyConstant;
import com.yuntan.indentity.security.service.TokenAuthService;
import com.yuntan.indentity.security.whitelist.WhitelistMatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import yuntan.common.context.BaseContext;
import yuntan.common.exception.BusinessException;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoginInterceptor implements HandlerInterceptor {

    /**
     * 管理员角色值，按你的业务定义修改
     * 假设：1 = 管理员
     */
    private static final int ADMIN_ROLE = 1;

    private final WhitelistMatcher whitelistMatcher;
    private final TokenAuthService tokenAuthService;

    /**
     * 在请求处理之前进行拦截，检查是否需要登录验证和权限校验
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        String path = request.getRequestURI();

        // 1. 白名单
        if (whitelistMatcher.isWhitelisted(path)) {
            return true;
        }

        // 2. Token解析
        Map<String, Object> userInfo = tokenAuthService.parseRequest(request);
        if (userInfo == null) {
            throw BusinessException.loginFailed(); // 401
        }

        // 3. 用户ID校验
        Object userIdObj = userInfo.get(KeyConstant.USER_ID);
        if (userIdObj == null) {
            throw BusinessException.loginFailed();
        }

        Long userId = Long.valueOf(userIdObj.toString());
        BaseContext.setUserId(userId);

        // 4. 权限校验（admin）
        if (path.startsWith("/admin")) {
            Object roleObj = userInfo.get(KeyConstant.ROLE);
            Integer role = roleObj == null ? null : Integer.valueOf(roleObj.toString());

            if (role == null || role != 1) {
                throw BusinessException.forbidden(); // 403
            }
        }

        return true;
    }

    /**
     * 请求完成后清理 ThreadLocal，防止线程复用导致数据污染
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        BaseContext.remove();
    }

    /**
     * 统一输出 JSON 响应
     */
    private void writeJsonResponse(HttpServletResponse response, int status, String error, String message) throws IOException {
        response.setStatus(status);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(
                String.format("{\"error\":\"%s\",\"message\":\"%s\"}", error, message)
        );
    }
}