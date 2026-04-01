package com.yuntan.content.stat.task;

import com.yuntan.content.stat.service.ISiteStatService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * UV 同步任务
 *
 * 功能：
 * 每天凌晨2点：
 * 1. 从 Redis 获取 UV
 * 2. 写入 MySQL
 */
@Component
@RequiredArgsConstructor
public class SiteUvSyncTask {

    private final StringRedisTemplate stringRedisTemplate;
    private final ISiteStatService siteStatService;

    @Scheduled(cron = "0 0 2 * * ?")
    public void syncTodayUv() {

        String date = LocalDate.now().toString();
        String key = "site:uv:" + date;

        // 获取数据 - - HyperLogLog 的 size 方法会返回近似值，误差率在 0.81% 左右
        Long uv = stringRedisTemplate.opsForHyperLogLog().size(key);

        // 写入数据库 - - 如果当天已经有记录，则更新 UV，否则插入新记录
        siteStatService.insertOrUpdate(LocalDate.now(), uv == null ? 0 : uv);

        // 可选：删除数据
        // stringRedisTemplate.delete(key);
    }
}