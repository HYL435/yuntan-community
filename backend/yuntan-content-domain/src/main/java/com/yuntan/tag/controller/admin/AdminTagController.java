package com.yuntan.tag.controller.admin;

import com.yuntan.tag.dto.TagDTO;
import com.yuntan.tag.dto.TagStatusDTO;
import com.yuntan.tag.dto.TagUpdateDTO;
import com.yuntan.tag.service.ITagService;
import com.yuntan.tag.vo.TagContentVO;
import com.yuntan.tag.vo.TagVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import yuntan.common.domain.Result;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/admin/tags")
@RequiredArgsConstructor
@Tag(description = "后台标签接口", name = "标签")
public class AdminTagController {

    private final ITagService tagService;


    @Operation(summary = "获取所有标签名称")
    @GetMapping
    public Result<List<TagVO>> list() {

        log.info("获取所有标签名称");

        List<TagVO> list = tagService.getAdminTag();

        return Result.ok(list);
    }

    @Operation(summary = "添加标签")
    @PostMapping
    public Result<Void> addTag(@RequestBody TagDTO tagDTO) {

        log.info("添加标签 {}", tagDTO);

        tagService.addTag(tagDTO);

        return Result.ok();
    }

    @Operation(summary = "更新标签")
    @PutMapping
    public Result<Void> updateTag(@RequestBody TagUpdateDTO tagUpdateDTO) {

        log.info("更新标签，{}", tagUpdateDTO);

        tagService.updateTag(tagUpdateDTO);

        return Result.ok();
    }

    @Operation(summary = "获取标签内容")
    @GetMapping("/{id}")
    public Result<TagContentVO> getTagById(@PathVariable Long id) {
        log.info("获取标签内容 {}", id);

        TagContentVO tagContentVO = tagService.getTagById(id);

        return Result.ok(tagContentVO);
    }


    @Operation(summary = "修改标签状态")
    @PutMapping("status")
    public Result<Void> changeTagStatus(@RequestBody TagStatusDTO tagStatusDTO) {
        log.info("修改标签状态 {}， 状态为 {}", tagStatusDTO.getId(), tagStatusDTO.getStatus());

        tagService.changeTagStatus(tagStatusDTO);

        return Result.ok();
    }

    @Operation(summary = "删除标签")
    @PutMapping("deleted/{id}")
    public Result<Void> deleteTag(@PathVariable Long id) {
        log.info("删除标签 {}", id);

        tagService.removeById(id);

        return Result.ok();
    }

}
