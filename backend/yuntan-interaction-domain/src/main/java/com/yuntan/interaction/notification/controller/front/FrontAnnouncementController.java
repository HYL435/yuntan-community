package com.yuntan.interaction.notification.controller.front;

import com.yuntan.common.domain.Result;
import com.yuntan.interaction.notification.service.IAnnouncementService;
import com.yuntan.interaction.notification.vo.AnnouncementVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/front/announcements")
@Tag(description = "前台公告接口", name = "公告管理")
public class FrontAnnouncementController {

    private final IAnnouncementService announcementService;


    @GetMapping
    @Operation(summary = "获取最新公告")
    public Result<AnnouncementVO> getLatestAnnouncement() {

        log.info("获取最新公告");

        return Result.ok(announcementService.getLatestAnnouncement());

    }

}
