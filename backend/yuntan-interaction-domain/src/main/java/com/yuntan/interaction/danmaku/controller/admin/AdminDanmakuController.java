package com.yuntan.interaction.danmaku.controller.admin;

import com.yuntan.common.domain.Result;
import com.yuntan.infra.mysql.PageDTO;
import com.yuntan.interaction.danmaku.dto.DanmakuStatusDTO;
import com.yuntan.interaction.danmaku.query.AdminDanmakuQuery;
import com.yuntan.interaction.danmaku.service.IDanmakuService;
import com.yuntan.interaction.danmaku.vo.AdminDanmakuVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/admin/danmaku")
@Tag(name = "后台-弹幕接口")
public class AdminDanmakuController {

    private final IDanmakuService danmakuService;

    @GetMapping("page")
    @Operation(summary = "分页获取后台弹幕列表")
    public Result<PageDTO<AdminDanmakuVO>> listDanmaku(AdminDanmakuQuery pageQuery) {
        log.info("获取弹幕列表");

        PageDTO<AdminDanmakuVO> list = danmakuService.listDanmakuAdmin(pageQuery);

        return Result.ok(list);
    }

    @PutMapping
    @Operation(summary = "修改弹幕状态")
    public Result<Void> updateDanmaku(@RequestBody DanmakuStatusDTO danmakuStatusDTO) {
        log.info("修改弹幕状态");

        danmakuService.updateStatusById(danmakuStatusDTO);

        return Result.ok();
    }

    @PutMapping("/delete")
    @Operation(summary = "删除弹幕")
    public Result<Void> deleteDanmaku(Long id) {
        log.info("删除弹幕， {}", id);

        // 逻辑删除
        danmakuService.removeById(id);

        return Result.ok();
    }

}
