package com.yuntan.collect.controller.front;

import com.yuntan.collect.service.ICollectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import yuntan.common.domain.Result;

@Slf4j
@RequestMapping("/api/front/collects")
@RestController
@RequiredArgsConstructor
@Tag(description = "前台交互接口", name = "交互")
public class FrontCollectController {

    private final ICollectService collectService;

    @Operation(summary = "文章收藏")
    @PostMapping("/{articleId}")
    public Result<Void> collect(@PathVariable Long articleId) {

        log.info("收藏");

        collectService.collect(articleId);

        return Result.ok();
    }

}
