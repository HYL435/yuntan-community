package com.yuntan.infra.client.map.ip.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "amap")
public class AmapProperties {

    private Boolean enable; // 是否启用高德地图功能

    private String key; // 高德地图API Key

    private String url; // 高德地图API Url

}
