package com.yuntan.interaction.stat.controller.admin;


import com.yuntan.common.domain.Result;
import com.yuntan.interaction.stat.service.ISiteStatService;
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
@RequestMapping("/admin/stat")
public class AdminSiteStatController {

    private final ISiteStatService siteStatService;

    /**
     * 获取近七日接口统计
     */
    @GetMapping("/interface/for7")
    @Operation(summary = "近七日接口统计", description = "获取近七日接口访问量")
    public Result<Long> InterfaceStaFor7() {

        log.info("近七日接口统计");

        Long count = siteStatService.InterfaceStaFor7();

        return Result.ok(count);
    }

}