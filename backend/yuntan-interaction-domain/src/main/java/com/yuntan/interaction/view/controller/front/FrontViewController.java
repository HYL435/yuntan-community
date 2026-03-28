package com.yuntan.interaction.view.controller.front;

import com.yuntan.interaction.view.service.IViewService;
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
@RequestMapping("/front/views")
@RestController
@RequiredArgsConstructor
@Tag(description = "前台交互接口", name = "交互")
public class FrontViewController {

    private final IViewService viewService;

    @Operation(summary = "文章浏览量")
    @PostMapping("/{articleId}")
    public Result<Void> view(@PathVariable Long articleId) {

        log.info("浏览量");

        viewService.view(articleId);

        return Result.ok();
    }

}
