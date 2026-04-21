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
 * AI聊天消息实体类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "AI聊天消息实体")
public class AiChatMessage implements Serializable {

    /**
     * 消息ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "消息ID", example = "1")
    private Long id;

    /**
     * 会话ID
     */
    @Schema(
            description = "会话ID",
            example = "20001",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Long sessionId;

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
     * 角色：user/assistant/system
     */
    @Schema(
            description = "角色：user/assistant/system",
            example = "user",
            requiredMode = Schema.RequiredMode.REQUIRED,
            allowableValues = {"user", "assistant", "system"},
            maxLength = 20
    )
    private String role;

    /**
     * 消息内容
     */
    @Schema(
            description = "消息内容",
            example = "请帮我解释一下Spring事务传播行为",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String content;

    /**
     * 模型提供商
     */
    @Schema(
            description = "模型提供商",
            example = "deepseek",
            maxLength = 50
    )
    private String provider;

    /**
     * 模型名称
     */
    @Schema(
            description = "模型名称",
            example = "deepseek-chat",
            maxLength = 100
    )
    private String modelName;

    /**
     * 状态：1-成功 0-失败
     */
    @Schema(
            description = "状态：1-成功 0-失败",
            example = "1",
            allowableValues = {"0", "1"},
            defaultValue = "1"
    )
    private Integer status;

    /**
     * 失败原因
     */
    @Schema(
            description = "失败原因",
            example = "调用模型超时",
            maxLength = 500
    )
    private String errorMessage;

    /**
     * 创建时间
     */
    @Schema(
            description = "创建时间",
            example = "2026-04-21 10:01:00"
    )
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}