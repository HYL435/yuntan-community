package com.yuntan.content.stat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuntan.content.stat.entity.SiteStatDaily;
import com.yuntan.content.stat.mapper.SiteStatMapper;
import com.yuntan.content.stat.service.ISiteStatService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class SiteStatServiceImpl extends ServiceImpl<SiteStatMapper, SiteStatDaily> implements ISiteStatService {

    private final StringRedisTemplate stringRedisTemplate;
    private final SiteStatMapper siteStatMapper;

    /**
     * 记录一次访问（带防刷）
     *
     * @param clientId 唯一标识（IP 或 userId 或 ip+ua）
     */
    public void recordVisit(String clientId) {
        if (clientId == null || clientId.isBlank()) {
            return;
        }

        // ===== 1. 今日 UV key =====
        String uvKey = "site:uv:" + LocalDate.now();

        // ===== 2. 防刷 key（1分钟内只算一次）=====
        String dedupKey = "site:uv:dedup:" + clientId;

        // 如果是第一次（1分钟内没有访问过），则设置 dedupKey 的值，并设置过期时间（1分钟）
        Boolean firstVisit = stringRedisTemplate.opsForValue()
                .setIfAbsent(dedupKey, "1", Duration.ofMinutes(1));

        // 如果不是第一次（1分钟内重复访问），直接返回
        if (Boolean.FALSE.equals(firstVisit)) {
            return;
        }

        // ===== 3. 记录到 HyperLogLog =====
        stringRedisTemplate.opsForHyperLogLog().add(uvKey, clientId);

        // ===== 4. 设置过期时间（防止长期占用内存）=====
        stringRedisTemplate.expire(uvKey, Duration.ofDays(2));
    }

    /**
     * 获取今日访客数（UV）
     */
    public long getTodayUv() {
        String uvKey = "site:uv:" + LocalDate.now();

        return stringRedisTemplate.opsForHyperLogLog().size(uvKey);
    }

    /**
     * 插入或更新
     */
    @Override
    public void insertOrUpdate(LocalDate date, long uv) {

        // 如果当天的记录不存在，则插入；如果存在，则更新 UV 字段。
        // 先尝试查询当天的记录
        SiteStatDaily siteStatDaily = siteStatMapper.selectOne(
                new QueryWrapper<SiteStatDaily>()
                        .eq("stat_date", date)
        );

        if (siteStatDaily == null) {
            siteStatDaily = SiteStatDaily.builder()
                    .statDate(date)
                    .uvCount(uv)
                    .pvCount(0L)
                    .hotScore(uv) // 暂时用 UV 值作为热度值，后期会进行优化
                    .build();
                    siteStatMapper.insert(siteStatDaily);

        } else {
            siteStatDaily.setUvCount(uv);
            siteStatMapper.updateById(siteStatDaily);
        }
    }
}
