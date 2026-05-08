package com.yuntan.interaction.stat.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuntan.common.context.BaseContext;
import com.yuntan.common.utils.BaseUtil;
import com.yuntan.interaction.stat.entity.SiteStatDaily;
import com.yuntan.interaction.stat.mapper.SiteStatMapper;
import com.yuntan.interaction.stat.service.ISiteStatService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SiteStatServiceImpl extends ServiceImpl<SiteStatMapper, SiteStatDaily> implements ISiteStatService {

    private final StringRedisTemplate stringRedisTemplate;
    private final StringRedisTemplate redisTemplate;

    /**
     * 获取今日访客数（UV）
     */
    @Override
    public long getTodayUv() {

        String today = LocalDate.now().toString();

        String uvKey = "site:uv:" + today;
        String pvKey = "site:pv:" + today;

        // 获取数据，pvStr 是字符串形式的 PV 计数
        String pvStr = stringRedisTemplate.opsForValue().get(pvKey);
        // 计算 UV，uv 是 HyperLogLog 结构的近似计数结果，uv 是 Long 类型的对象，可能为 null
        // 反回的uv是近似的，误差在 0.81% 以内，适合大数据量的场景
        // size 方法返回的是 HyperLogLog 中不同元素的近似数量，如果 uvKey 不存在，则返回 0L。
        Long uv = stringRedisTemplate.opsForHyperLogLog().size(uvKey);

        // 处理数据，pv 是 Long 类型的 PV 计数，如果 pvStr 为 null，则默认值为 0L；
        Long pv = pvStr == null ? 0L : Long.parseLong(pvStr);

        calculateHotScore(uv, pv, today);

        return stringRedisTemplate.opsForHyperLogLog().size(uvKey);
    }

    // 计算热度值并存入redis 中
    private void calculateHotScore(Long uv, Long pv, String today) {

        String hotScoreKey = "site:hot:score:" + today;

        Long hotScore = (long) (uv * 0.60 + pv * 0.40);

        redisTemplate.opsForValue().set(hotScoreKey, String.valueOf(hotScore));
    }

    /**
     * 记录一次 PV
     */
    @Override
    public void recordPv(String page, HttpServletRequest request) {

        String clientId = getClientId(request);

        // 1. 校验 page 参数
        if (!isValidPage(page)) {
            return;
        }
        String today = LocalDate.now().toString();


        // PV 防刷：同一 clientId 对同一 page，20 秒只记 1 次
        String pvDedupKey = "site:pv:dedup:" + today + ":" + page + ":" + clientId;
        // setIfAbsent 方法会尝试设置一个值，如果该 key 不存在，则设置成功并返回 true；如果该 key 已经存在，则设置失败并返回 false。
        // 通过设置一个短暂的过期时间（20 秒），可以有效地防止同一 clientId 在短时间内对同一 page 进行重复访问，从而避免刷 PV。
        Boolean firstHit = redisTemplate.opsForValue()
                .setIfAbsent(pvDedupKey, "1", Duration.ofSeconds(20));
        if (Boolean.FALSE.equals(firstHit)) {
            return; // 20 秒内重复访问，不计 PV
        }

        // 2. 记录 pv 数
        String pvKey = "site:pv:" + today;
        redisTemplate.opsForValue().increment(pvKey);

        // 页面维度的 PV 统计（后续会用到）
        String pvPageKey = "site:pv:" + today + ":" + page;
        redisTemplate.opsForValue().increment(pvPageKey);

        // 3. uv 统计（带防刷）

        String uvKey = "site:uv:" + today;
        // 记录到 HyperLogLog 中，HyperLogLog 会根据 clientId 自动去重
        redisTemplate.opsForHyperLogLog().add(uvKey, clientId);

        // 设置过期时间（防止长期占用内存）
        redisTemplate.expire(pvKey, Duration.ofDays(7));
        redisTemplate.expire(uvKey, Duration.ofDays(7));
        redisTemplate.expire(pvPageKey, Duration.ofDays(7));

    }

    // 校验 page 参数
    private boolean isValidPage(String page) {

        // 目前先简单校验一下，后续会根据实际情况进行调整
        return Set.of("home", "article", "tag", "message-board", "about",
                        "category", "topics", "search", "profile", "bookshelf", "toolbox", "settings")
                .contains(page);

    }

    // 获取客户端标识（IP 或 userId 或 ip+ua）
    private String getClientId(HttpServletRequest request) {

        // 登录用户优先
        Long userId = BaseContext.getUserId();
        if (userId != null) {
            return "u:" + userId;
        }

        // 未登录用户，使用 IP + User-Agent 作为标识
        String ip = BaseUtil.getClientIp(request);
        String ua = request.getHeader("User-Agent");
        if (ip != null && ua != null) {
            // 使用 MD5 加密 IP + User-Agent，生成一个固定长度的字符串，避免过长的 key
            return DigestUtils.md5DigestAsHex((ip + ua).getBytes());
        }

        // 最后兜底，使用 IP 作为标识
        return ip;
    }

}
