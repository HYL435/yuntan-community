package com.yuntan.content.stat.controller.front;


import com.yuntan.common.domain.Result;
import com.yuntan.content.stat.service.ISiteStatService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 站点统计接口
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/front/stats")
public class FrontSiteStatController {

    private final ISiteStatService siteStatService;

    /**
     * 获取今日热度（访客数）
     */
    @GetMapping("/hot/today")
    @Operation(summary = "获取今日热度（访客数）", description = "返回今日的访客数，代表站点的热度")
    public Result<Long> todayHot() {
        log.info("获取今日热度（访客数）");

        return Result.ok(siteStatService.getTodayUv());
    }
}