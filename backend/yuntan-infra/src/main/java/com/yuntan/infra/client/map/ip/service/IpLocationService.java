package com.yuntan.infra.client.map.ip.service;

import com.yuntan.infra.client.map.ip.dto.IpLocationDTO;

public interface IpLocationService {

    /**
     * 根据指定 IP 查询位置
     */
    IpLocationDTO locateByIp(String ip);

    /**
     * 不传 IP，由第三方按请求来源 IP 定位
     */
    IpLocationDTO locateCurrentRequestIp();
}