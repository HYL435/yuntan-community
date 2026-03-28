package com.yuntan.interaction.notification.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户通知记录表实体类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "用户通知记录实体")
public class UserNotify implements Serializable {

    /**
     * 主键雪花ID
     */
    @Schema(description = "主键雪花ID", example = "1")
    private Long id;

    /**
     * 接收通知用户ID
     */
    @Schema(
            description = "接收通知用户ID",
            example = "1",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Long userId;

    /**
     * 关联消息模板编码
     */
    @Schema(
            description = "关联消息模板编码",
            example = "ARTICLE_LIKE",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String templateCode;

    /**
     * 实际发送的消息内容（替换占位符后）
     */
    @Schema(
            description = "实际发送的消息内容（替换占位符后）",
            example = "您的文章获得了新的点赞"
    )
    private String msgContent;

    /**
     * 消息类型：1系统通知，2互动通知，3邮件，4短信
     */
    @Schema(
            description = "消息类型：1系统通知，2互动通知，3邮件，4短信",
            example = "2",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Integer msgType;

    /**
     * 发送状态：0-待发送，1-发送成功，2-发送失败
     */
    @Schema(
            description = "发送状态：0-待发送，1-发送成功，2-发送失败",
            example = "0"
    )
    private Integer sendStatus;

    /**
     * 发送时间
     */
    @Schema(
            description = "发送时间",
            example = "2024-01-01 00:00:00"
    )
    private LocalDateTime sendTime;

    /**
     * 阅读状态：0-未读，1-已读
     */
    @Schema(
            description = "阅读状态：0-未读，1-已读",
            example = "0"
    )
    private Integer readStatus;

    /**
     * 阅读时间
     */
    @Schema(
            description = "阅读时间",
            example = "2024-01-01 00:00:00"
    )
    private LocalDateTime readTime;

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