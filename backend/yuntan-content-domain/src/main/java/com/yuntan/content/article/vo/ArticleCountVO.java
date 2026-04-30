package com.yuntan.content.article.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class ArticleCountVO {

    public abstract Long getId();

    /**
     * 文章点赞数（缓存同步）
     */
    @Schema(
            description = "点赞数",
            example = "10",
            defaultValue = "0"
    )
    private Long likeCount;

    /**
     * 文章评论数（缓存同步）
     */
    @Schema(
            description = "评论数",
            example = "5",
            defaultValue = "0"
    )
    private Long commentCount;

    /**
     * 文章收藏数（缓存同步）
     */
    @Schema(
            description = "收藏数",
            example = "3",
            defaultValue = "0"
    )
    private Long collectCount;

    /**
     * 文章浏览量（缓存同步）
     */
    @Schema(
            description = "浏览量",
            example = "100",
            defaultValue = "0"
    )
    private Long viewCount;
}
