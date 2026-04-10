package com.yuntan.content.site.controller.front;

import com.yuntan.common.domain.Result;
import com.yuntan.content.site.service.ISiteTimelineService;
import com.yuntan.content.site.vo.SiteTimelineVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/front/timeline")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "建站历程时间轴接口", description = "提供建站历程时间轴相关的接口")
public class FrontSiteTimelineController {

    private final ISiteTimelineService siteTimelineService;

    @GetMapping
    @Operation(summary = "获取建站历程时间轴")
    public Result<List<SiteTimelineVO>> getTimeline() {
        log.info("获取建站历程时间轴");

        List<SiteTimelineVO> timeline = siteTimelineService.getTimeline();

        return Result.ok(timeline);
    }

}
