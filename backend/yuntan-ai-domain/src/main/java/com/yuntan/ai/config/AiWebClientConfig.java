package com.yuntan.ai.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class AiWebClientConfig {

    /**
     * 创建一个WebClient实例，用于调用AI服务
     *
     * @param baseUrl AI服务地址
     * @return WebClient实例
     */
    @Bean
    public WebClient aiWebClient(@Value("${ai.service.base-url}") String baseUrl) {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }
}