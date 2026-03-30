package com.yuntan.common.interceptors;

import cn.hutool.core.util.StrUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import com.yuntan.common.context.BaseContext;

/**
 * 登录拦截器
 * 获取登录用户信息，存入ThreadLocal
 */
@Component
public class UserInfoInterceptor implements HandlerInterceptor {

    // TODO 后续可以改为从token中获取用户信息，或者从session中获取用户信息，目前先从header中获取用户信息，方便测试

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取登录用户信息
        String userInfo = request.getHeader("user-info");
        // 判断是否获取了用户，如果有，存入ThreadLocal
        if (StrUtil.isNotBlank(userInfo)) {
            BaseContext.setUserId(Long.valueOf(userInfo));
        }
        // 放行
        return  true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 清理用户
        BaseContext.remove();
    }
}
