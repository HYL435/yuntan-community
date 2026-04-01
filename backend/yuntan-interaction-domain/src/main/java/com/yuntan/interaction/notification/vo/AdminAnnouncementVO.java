package com.yuntan.interaction.notification.vo;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 公告实体类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "公告实体")
public class AdminAnnouncementVO implements Serializable {

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "公告ID", example = "1")
    private Long id;

    /**
     * 公告标题
     */
    @Schema(
            description = "公告标题",
            example = "网站公告",
            requiredMode = Schema.RequiredMode.REQUIRED,
            maxLength = 60
    )
    private String title;

    /**
     * 公告内容
     */
    @Schema(
            description = "公告内容",
            example = "欢迎来到云坛。",
            requiredMode = Schema.RequiredMode.REQUIRED,
            maxLength = 500
    )
    private String content;

    /**
     * 跳转链接
     */
    @Schema(
            description = "跳转链接，可为空",
            example = "https://example.com/news",
            maxLength = 255
    )
    private String linkUrl;

    /**
     * 状态：0-草稿，1-已发布，2-已关闭
     */
    @Schema(
            description = "状态：0-草稿，1-已发布，2-已关闭",
            example = "1",
            allowableValues = {"0", "1", "2"},
            defaultValue = "0"
    )
    private Integer status;

    /**
     * 发布时间
     */
    @Schema(
            description = "发布时间",
            example = "2026-04-01 10:00:00"
    )
    private LocalDateTime publishTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(
            description = "更新时间",
            example = "2026-04-01 12:00:00"
    )
    private LocalDateTime updateTime;
}
