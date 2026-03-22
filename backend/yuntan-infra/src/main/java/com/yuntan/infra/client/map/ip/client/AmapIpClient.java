package com.yuntan.infra.client.map.ip.client;

import com.yuntan.infra.client.map.ip.config.AmapProperties;
import com.yuntan.infra.client.map.ip.dto.AmapIpResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
public class AmapIpClient {

    private final AmapProperties properties;
    private final RestTemplate restTemplate;

    public AmapIpResponse locateByIp(String ip) {

        String url = properties.getUrl() + "/v3/ip";

        // 拼接 url，添加参数
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("key", properties.getKey());

        // 如果 IP 不为空，则添加 IP 参数
        if (ip != null && !ip.isBlank()) {
            builder.queryParam("ip", ip);
        }

        return restTemplate.getForObject(builder.toUriString(), AmapIpResponse.class);
    }

}
