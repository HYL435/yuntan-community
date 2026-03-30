package com.yuntan.infra.client.map.ip.service.impl;

import com.yuntan.infra.client.map.ip.client.AmapIpClient;
import com.yuntan.infra.client.map.ip.dto.AmapIpResponse;
import com.yuntan.infra.client.map.ip.dto.IpLocationDTO;
import com.yuntan.infra.client.map.ip.service.IpLocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.yuntan.common.exception.BusinessException;

@Service
@RequiredArgsConstructor
public class AmapIpLocationServiceImpl implements IpLocationService {

    private final AmapIpClient amapIpClient;

    @Override
    public IpLocationDTO locateByIp(String ip) {
        AmapIpResponse response = amapIpClient.locateByIp(ip);
        return convert(response);
    }

    @Override
    public IpLocationDTO locateCurrentRequestIp() {
        AmapIpResponse response = amapIpClient.locateByIp(null);
        return convert(response);
    }

    private IpLocationDTO convert(AmapIpResponse response) {
        if (response == null) {
            throw new BusinessException("高德 IP 定位失败：无响应");
        }

        if (!"1".equals(response.getStatus())) {
            throw new BusinessException("高德 IP 定位失败：" + response.getInfo());
        }

        return new IpLocationDTO(
                response.getProvince(),
                response.getCity(),
                response.getAdcode(),
                response.getRectangle()
        );
    }
}