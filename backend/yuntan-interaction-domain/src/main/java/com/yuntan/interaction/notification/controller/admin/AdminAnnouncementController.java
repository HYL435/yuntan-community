package com.yuntan.interaction.notification.controller.admin;


import com.yuntan.common.domain.Result;
import com.yuntan.interaction.notification.dto.AnnouncementDTO;
import com.yuntan.interaction.notification.dto.AnnouncementStatusDTO;
import com.yuntan.interaction.notification.dto.UpdateAnnouncementDTO;
import com.yuntan.interaction.notification.service.IAnnouncementService;
import com.yuntan.interaction.notification.vo.AdminAnnouncementVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/admin/announcements")
@Tag(description = "后台公告接口", name = "公告管理")
public class AdminAnnouncementController {

    private final IAnnouncementService announcementService;


    @GetMapping
    @Operation(summary = "获取最新公告")
    public Result<AdminAnnouncementVO> adminGetLatestAnnouncement() {

        log.info("获取最新公告");

        return Result.ok(announcementService.adminGetLatestAnnouncement());
    }

    @PostMapping
    @Operation(summary = "添加公告")
    public Result<Void> addAnnouncement(@RequestBody AnnouncementDTO announcementDTO) {
        log.info("添加公告， {}", announcementDTO);

        announcementService.addAnnouncement(announcementDTO);

        return Result.ok();
    }


    @PutMapping
    @Operation(summary = "更新公告")
    public Result<Void> updateAnnouncement(@RequestBody UpdateAnnouncementDTO updateAnnouncementDTO) {
        log.info("更新公告， {}", updateAnnouncementDTO);

        announcementService.updateAnnouncement(updateAnnouncementDTO);

        return Result.ok();
    }

    @PutMapping("status")
    public Result<Void> changeStatus(@RequestBody AnnouncementStatusDTO statusDTO) {
        log.info("修改公告状态，{}", statusDTO);

        announcementService.changeStatus(statusDTO);

        return Result.ok();
    }



}
