package com.yuntan.content.site.controller.admin;

import com.yuntan.common.domain.Result;
import com.yuntan.content.site.dto.AdminSiteTimelineDTO;
import com.yuntan.content.site.dto.AdminSiteTimelineStatusDTO;
import com.yuntan.content.site.dto.SiteTimelineUpdateDTO;
import com.yuntan.content.site.query.AdminSiteTimelineQuery;
import com.yuntan.content.site.service.ISiteTimelineService;
import com.yuntan.content.site.vo.AdminSiteTimelineVO;
import com.yuntan.infra.mysql.PageDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/timeline")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "建站历程时间轴接口", description = "提供建站历程时间轴相关的接口")
public class AdminSiteTimelineController {

    private final ISiteTimelineService siteTimelineService;


    @GetMapping("/list")
    @Operation(summary = "获取建站历程时间轴列表")
    public Result<PageDTO<AdminSiteTimelineVO>> list(AdminSiteTimelineQuery query) {
        log.info("查询参数{}", query);

        PageDTO<AdminSiteTimelineVO> page = siteTimelineService.listSiteTimelines(query);

        return Result.ok(page);
    }

    @PostMapping
    @Operation(summary = "创建建站历程时间轴")
    public Result<Void> create(@RequestBody AdminSiteTimelineDTO adminSiteTimelineDTO) {

        log.info("创建建站历程时间轴，{}", adminSiteTimelineDTO);

        siteTimelineService.create(adminSiteTimelineDTO);

        return Result.ok();
    }

    @PutMapping
    @Operation(summary = "更新建站历程时间轴")
    public Result<Void> updateTimeline(@RequestBody SiteTimelineUpdateDTO siteTimelineUpdateDTO) {

        log.info("更新建站历程时间轴，{}", siteTimelineUpdateDTO);

        siteTimelineService.updateTimeline(siteTimelineUpdateDTO);

        return Result.ok();
    }

    @PutMapping("/status")
    @Operation(summary = "修改建站历程时间轴状态")
    public Result<Void> changeTimelineStatus(@RequestBody AdminSiteTimelineStatusDTO adminSiteTimelineStatusDTO) {
        log.info("修改建站历程时间轴状态，{}", adminSiteTimelineStatusDTO);

        siteTimelineService.updateTimelineStatus(adminSiteTimelineStatusDTO);

        return Result.ok();
    }

    @PutMapping("/deleted/{id}")
    @Operation(summary = "删除建站历程时间轴")
    public Result<Void> deleteTimeline(@PathVariable Long id) {
        log.info("删除建站历程时间轴，id={}", id);

        siteTimelineService.removeById(id);

        return Result.ok();
    }

}
