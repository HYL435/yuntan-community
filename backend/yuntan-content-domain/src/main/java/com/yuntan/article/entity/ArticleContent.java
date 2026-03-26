package com.yuntan.article.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 文章正文实体类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "文章正文实体")
public class ArticleContent implements Serializable {

    /**
     * 主键ID
     */
    @Schema(description = "正文ID（雪花ID）", example = "1")
    private Long id;

    /**
     * 关联文章ID
     */
    @Schema(
            description = "关联文章ID",
            example = "1",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Long articleId;

    /**
     * 文章正文内容
     */
    @Schema(
            description = "文章正文内容",
            example = "这是文章正文内容",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String content;

    /**
     * 正文类型：1-Markdown，2-HTML
     */
    @Schema(
            description = "正文类型：1-Markdown，2-HTML",
            example = "1",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Integer contentType;

    /**
     * 创建时间
     */
    @Schema(
            description = "创建时间",
            example = "2024-01-01 00:00:00"
    )
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(
            description = "更新时间",
            example = "2024-01-01 00:00:00"
    )
    private LocalDateTime updateTime;

}