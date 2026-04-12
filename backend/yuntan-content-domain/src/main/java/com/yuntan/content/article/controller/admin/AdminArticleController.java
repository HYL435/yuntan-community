package com.yuntan.content.article.controller.admin;

import com.yuntan.common.domain.Result;
import com.yuntan.content.article.dto.ArticleSaveDTO;
import com.yuntan.content.article.dto.ArticleStatusDTO;
import com.yuntan.content.article.query.ArticleManageQuery;
import com.yuntan.content.article.service.IArticleService;
import com.yuntan.content.article.vo.ArticleAdminVO;
import com.yuntan.content.article.vo.ArticleDetailVO;
import com.yuntan.dto.ArticleInfoDTO;
import com.yuntan.infra.mysql.PageDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/admin/articles")
@Tag(description = "后台文章接口", name = "文章管理")
public class AdminArticleController {

    public final IArticleService articleService;


    @Operation(summary = "获取文章管理列表")
    @GetMapping("/page")
    public Result<PageDTO<ArticleAdminVO>> pageManage(ArticleManageQuery articleManageQuery) {

        log.info("获取文章管理列表，查询参数：{}", articleManageQuery);

        PageDTO<ArticleAdminVO> result = articleService.pageManage(articleManageQuery);

        return Result.ok(result);
    }

    @Operation(summary = "文章置顶")
    @PutMapping("/top")
    public Result<Void> articleTop(Long id,  Integer top) {

        log.info("文章置顶，文章ID：{}，置顶状态：{}", id, top);

        articleService.articleTop(id, top);

        return Result.ok();
    }


    @Operation(summary = "软删除文章")
    @PutMapping("/deleted/{id}")
    public Result<Void> deletedArticle(@PathVariable Long id) {

        log.info("软删除文章，文章ID：{}", id);

        articleService.removeById(id);

        return Result.ok();
    }

    @Operation(summary = "获取文章详情")
    @GetMapping("/{id}")
    public Result<ArticleDetailVO> getArticleDetail(@PathVariable Long id) {

        log.info("获取文章详情，文章ID：{}", id);

        ArticleDetailVO articleDetailVO = articleService.getArticleDetail(id);

        return Result.ok(articleDetailVO);
    }

    @Operation(summary = "保存文章")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<Void> saveArticle(@ModelAttribute ArticleSaveDTO articleSaveDTO) {

        log.info("保存文章，文章保存参数：{}", articleSaveDTO);

        articleService.saveArticle(articleSaveDTO);

        return Result.ok();
    }

    @Operation(summary = "修改文章状态")
    @PutMapping("/status")
    public Result<Void> updateArticleStatus(@RequestBody ArticleStatusDTO articleStatusDTO) {

        log.info("私有文章，文章ID：{}", articleStatusDTO);

        articleService.updateArticleStatus(articleStatusDTO);

        return Result.ok();
    }

    @GetMapping("/info/{id}")
    @Operation(summary = "获取文章信息")
    public Result<ArticleInfoDTO> getArticleInfoById(@PathVariable Long id) {
        log.info("获取文章信息，文章ID：{}", id);

        ArticleInfoDTO articleInfoDTO = articleService.getArticleInfoById(id);

        return Result.ok(articleInfoDTO);
    }

}