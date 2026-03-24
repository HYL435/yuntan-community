package com.yuntan.like.controller.front;

import com.yuntan.like.service.ILikeService;
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
@RequestMapping("/front/likes")
@RestController
@RequiredArgsConstructor
@Tag(description = "前台交互接口", name = "交互")
public class FrontLikeController {

    private final ILikeService likeService;


    @Operation(summary = "文章点赞")
    @PostMapping("/{articleId}")
    public Result<Void> like(@PathVariable Long articleId) {

        log.info("点赞");

        likeService.like(articleId);

        return Result.ok();
    }

}
