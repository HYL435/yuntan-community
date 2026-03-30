package com.yuntan.interaction.comment.controller.admin;


import com.yuntan.interaction.comment.dto.admin.CommentStatusDTO;
import com.yuntan.interaction.comment.query.CommentQuery;
import com.yuntan.interaction.comment.service.ICommentService;
import com.yuntan.interaction.comment.vo.admin.CommentAdminVO;
import com.yuntan.infra.mysql.PageDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import com.yuntan.common.domain.Result;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/admin/comments")
@Tag(name = "后台-评论接口")
public class AdminCommentController  {

    public final ICommentService commentService;

    @Operation(summary = "删除评论")
    @RequestMapping("/deleted/{id}")
    public Result<Void> deleteComment(@PathVariable Long id) {

        log.info("删除评论，评论ID：{}", id);

        commentService.removeById(id);

        return Result.ok();
    }

    @GetMapping
    @Operation(summary = "获取评论列表")
    public Result<PageDTO<CommentAdminVO>> listCommentsAdmin(CommentQuery pageQuery) {

        log.info("获取评论列表，分页参数：{}", pageQuery);

        PageDTO<CommentAdminVO> list = commentService.listCommentsAdmin(pageQuery);

        return Result.ok(list);
    }

    @PutMapping("/status")
    @Operation(summary = "修改评论状态")
    public Result<Void> updateCommentStatus(@RequestBody CommentStatusDTO commentStatusDTO) {

        log.info("修改评论状态，评论ID：{}，新状态：{}", commentStatusDTO.getId(), commentStatusDTO.getStatus());

        commentService.updateStatusById(commentStatusDTO);

        return Result.ok();

    }

}
