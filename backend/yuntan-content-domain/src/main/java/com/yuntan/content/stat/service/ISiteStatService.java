package com.yuntan.content.stat.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yuntan.content.stat.entity.SiteStatDaily;

import java.time.LocalDate;

/**
 * 全站 UV（访客数）统计服务
 * 职责：
 * 1. 记录访客（带防刷）
 * 2. 获取今日 UV
 */

public interface ISiteStatService extends IService<SiteStatDaily> {


    /**
     * 记录访客
     *
     * @param clientId 客户端唯一标识
     */
    void recordVisit(String clientId);

    /**
     * 获取今日 UV
     */
    long getTodayUv();

    /**
     * 插入或更新统计数据
     */
    void insertOrUpdate(LocalDate date, long uv);
}