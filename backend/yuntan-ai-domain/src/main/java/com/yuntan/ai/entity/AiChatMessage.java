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
     * 角色：0-system 1-user 2-assistant
     */
    @Schema(
            description = "角色：0-system 1-user 2-assistant",
            example = "1",
            requiredMode = Schema.RequiredMode.REQUIRED,
            allowableValues = {"0", "1", "2"}
    )
    private Integer role;

    /**
     * 会话内消息顺序号，从1递增
     */
    @Schema(
            description = "会话内消息顺序号，从1递增",
            example = "1",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Integer sequenceNo;

    /**
     * 消息内容；流式生成中可为空，完成后回填
     */
    @Schema(
            description = "消息内容；流式生成中可为空，完成后回填",
            example = "请帮我解释一下Spring事务传播行为"
    )
    private String content;

    /**
     * 模型提供商，如 deepseek/qwen
     */
    @Schema(
            description = "模型提供商，如 deepseek/qwen",
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
    private String model;

    /**
     * 输入tokens
     */
    @Schema(
            description = "输入tokens",
            example = "100"
    )
    private Integer promptTokens;

    /**
     * 输出tokens
     */
    @Schema(
            description = "输出tokens",
            example = "200"
    )
    private Integer completionTokens;

    /**
     * 总tokens
     */
    @Schema(
            description = "总tokens",
            example = "300"
    )
    private Integer totalTokens;

    /**
     * 结束原因：stop/length/error/cancelled
     */
    @Schema(
            description = "结束原因：stop/length/error/cancelled",
            example = "stop",
            maxLength = 50
    )
    private String finishReason;

    /**
     * 状态：0-失败 1-成功 2-处理中 3-已撤回 4-已取消
     */
    @Schema(
            description = "状态：0-失败 1-成功 2-处理中 3-已撤回 4-已取消",
            example = "2",
            allowableValues = {"0", "1", "2", "3", "4"},
            defaultValue = "2"
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
     * 链路追踪ID
     */
    @Schema(
            description = "链路追踪ID",
            example = "trace-123456789",
            maxLength = 64
    )
    private String traceId;

    /**
     * 请求ID/幂等ID
     */
    @Schema(
            description = "请求ID/幂等ID",
            example = "req-987654321",
            maxLength = 64
    )
    private String requestId;

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
            example = "2026-04-21 10:01:00"
    )
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(
            description = "更新时间",
            example = "2026-04-21 10:02:00"
    )
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
