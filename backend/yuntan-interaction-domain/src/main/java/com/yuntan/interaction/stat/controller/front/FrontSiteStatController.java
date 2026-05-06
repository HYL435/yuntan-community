package com.yuntan.interaction.stat.controller.front;


import com.yuntan.common.domain.Result;
import com.yuntan.interaction.stat.dto.PvDTO;
import com.yuntan.interaction.stat.service.ISiteStatService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 站点统计接口
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/front/stat")
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


    /**
     * 记录访问
     */
    @PostMapping("/pv")
    @Operation(summary = "记录访问", description = "记录一次访问")
    public Result<Void> recordPv(@RequestBody PvDTO pvDTO, HttpServletRequest request) {
        log.info("记录访问: {}", pvDTO);

        siteStatService.recordPv(pvDTO.getPage(),  request);

        return Result.ok();
    }

}