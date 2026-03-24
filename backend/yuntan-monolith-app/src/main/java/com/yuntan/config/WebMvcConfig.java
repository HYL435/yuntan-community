package com.yuntan.config;

import com.yuntan.indentity.security.interceptor.LoginInterceptor;
import com.yuntan.indentity.security.properties.SecurityWhiteListProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(SecurityWhiteListProperties.class) // 加载安全白名单配置
public class WebMvcConfig implements WebMvcConfigurer {

    private final LoginInterceptor loginInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)  // 添加登录拦截器
                .addPathPatterns("/**"); // 拦截所有请求
    }

}
