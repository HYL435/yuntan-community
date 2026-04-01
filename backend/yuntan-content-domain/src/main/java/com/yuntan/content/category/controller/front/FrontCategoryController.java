package com.yuntan.content.category.controller.front;

import com.yuntan.content.category.service.ICategoryService;
import com.yuntan.content.category.vo.CategoryFrontVO;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.yuntan.common.domain.Result;

import java.util.List;

@Slf4j
@RequestMapping("/front/categories")
@RestController
@RequiredArgsConstructor
@Tag(description = "前台分类接口", name = "分类")
public class FrontCategoryController {

    private final ICategoryService categoryService;

    @Schema(description = "获取所有分类名称")
    @GetMapping
    public Result<List<CategoryFrontVO>> getAllCategoryNames() {
        log.info("获取所有分类名称");

        List<CategoryFrontVO> list = categoryService.getAllCategoryNames();

        return Result.ok(list);
    }

}
