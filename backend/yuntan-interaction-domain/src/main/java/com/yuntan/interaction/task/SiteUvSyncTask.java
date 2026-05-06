package com.yuntan.interaction.task;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yuntan.interaction.stat.entity.SiteStatDaily;
import com.yuntan.interaction.stat.mapper.SiteStatMapper;
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
    private final SiteStatMapper siteStatMapper;

    // 0 0 2 * * ? 代表每天凌晨2点执行
    @Scheduled(cron = "0 0 2 * * ?")
    public void syncTodayUv() {

        String date = LocalDate.now().minusDays(1).toString(); // 获取昨天的日期字符串，格式为 "2024-06-01"

        // 获取数据的 Redis key
        String pvKey = "site:pv:" + date;
        String uvKey = "site:uv:" + date;
        String hotScoreKey = "site:hot:score:" + date;

        // 获取数据，pvStr 是字符串形式的 PV 计数
        String pvStr = stringRedisTemplate.opsForValue().get(pvKey);
        // 计算 UV，uv 是 HyperLogLog 结构的近似计数结果，uv 是 Long 类型的对象，可能为 null
        // 反回的uv是近似的，误差在 0.81% 以内，适合大数据量的场景
        // size 方法返回的是 HyperLogLog 中不同元素的近似数量，如果 uvKey 不存在，则返回 0L。
        Long uv = stringRedisTemplate.opsForHyperLogLog().size(uvKey);

        // 获取热度值，hotScore 是字符串形式的热度值，可能为 null
        String hotScoreStr = stringRedisTemplate.opsForValue().get(hotScoreKey);

        // 处理数据，pv 是 Long 类型的 PV 计数，如果 pvStr 为 null，则默认值为 0L；
        Long pv = pvStr == null ? 0L : Long.parseLong(pvStr);
        Long hotScore = hotScoreStr == null ? 0L : Long.parseLong(hotScoreStr);

        // 插入或更新 MySQL 中的记录
        siteStatMapper.insertOrUpdate(SiteStatDaily.builder()
                .statDate(LocalDate.parse(date))
                .uvCount(uv)
                .pvCount(pv)
                .hotScore(hotScore)
                .build());

        // 可选：删除数据
        // stringRedisTemplate.delete(key);
    }


    /**
     * 插入或更新
     */
    private void insertOrUpdate(LocalDate date, long uv)  {

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