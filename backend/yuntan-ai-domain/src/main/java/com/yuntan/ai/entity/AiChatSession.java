package com.yuntan.ai.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * AI聊天会话实体类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "AI聊天会话实体")
public class AiChatSession implements Serializable {

    /**
     * 会话ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "会话ID", example = "1")
    private Long id;

    /**
     * 用户ID
     */
    @Schema(
            description = "用户ID",
            example = "10001",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Long userId;

    /**
     * 会话标题
     */
    @Schema(
            description = "会话标题",
            example = "Spring Boot学习问答",
            maxLength = 200
    )
    private String title;

    /**
     * 默认模型提供商
     */
    @Schema(
            description = "默认模型提供商",
            example = "deepseek",
            maxLength = 50
    )
    private String provider;

    /**
     * 状态：1-正常 0-关闭
     */
    @Schema(
            description = "状态：1-正常 0-关闭",
            example = "1",
            allowableValues = {"0", "1"},
            defaultValue = "1"
    )
    private Integer status;

    /**
     * 创建时间
     */
    @Schema(
            description = "创建时间",
            example = "2026-04-21 10:00:00"
    )
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(
            description = "更新时间",
            example = "2026-04-21 10:05:00"
    )
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}