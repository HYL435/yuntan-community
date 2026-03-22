package com.yuntan.infra.oss;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration(proxyBeanMethods = false)
@RequiredArgsConstructor
public class OssAutoConfiguration {

    private final OssProperties properties;

    @Bean
    @ConditionalOnMissingBean
    public OSS ossClient() {
        return new OSSClientBuilder().build(
                properties.getEndpoint(),
                properties.getAccessKey(),
                properties.getSecretKey()
        );
    }
}
