package com.yuntan.interaction.notification.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 公告实体类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "公告实体")
public class UpdateAnnouncementDTO implements Serializable {

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
}
