package com.yuntan.interaction.stat.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yuntan.interaction.stat.entity.SiteStatDaily;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 全站 UV（访客数）统计服务
 * 职责：
 * 1. 记录访客（带防刷）
 * 2. 获取今日 UV
 */

public interface ISiteStatService extends IService<SiteStatDaily> {

    /**
     * 获取今日 UV
     */
    long getTodayUv();

    /**
     * 记录 PV
     *
     * @param page 页面名
     * @param request 请求对象
     */
    void recordPv(String page, HttpServletRequest request);
}