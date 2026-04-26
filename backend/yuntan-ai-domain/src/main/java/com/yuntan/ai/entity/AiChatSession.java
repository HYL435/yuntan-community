package com.yuntan.ai.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
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
     * 状态：0-已关闭 1-正常 2-已归档
     */
    @Schema(
            description = "状态：0-已关闭 1-正常 2-已归档",
            example = "1",
            allowableValues = {"0", "1", "2"},
            defaultValue = "1"
    )
    private Integer status;

    /**
     * 下一个消息序列号
     */
    @Schema(
            description = "下一个消息序列号",
            example = "1",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Integer nextSequenceNo;

    /**
     * 逻辑删除：0-未删除 1-已删除
     */
    @Schema(
            description = "逻辑删除标记：0-未删除 1-已删除",
            example = "0",
            allowableValues = {"0", "1"},
            defaultValue = "0"
    )
    @TableLogic
    private Integer deleted;

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
