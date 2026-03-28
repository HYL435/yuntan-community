package com.yuntan.interaction.notification.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 消息模板实体类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "消息模板实体")
public class MsgTemplate implements Serializable {

    /**
     * 主键雪花ID
     */
    @Schema(description = "主键雪花ID", example = "1")
    private Long id;

    /**
     * 模版唯一编码，如ARTICLE_LIKE
     */
    @Schema(
            description = "模版唯一编码",
            example = "ARTICLE_LIKE",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String templateCode;

    /**
     * 模版名称
     */
    @Schema(
            description = "模版名称",
            example = "文章点赞通知",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String templateName;

    /**
     * 模版内容，支持占位符：{userNick}、{articleTitle}
     */
    @Schema(
            description = "模版内容，支持占位符：{userNick}、{articleTitle}",
            example = "{userNick} 点赞了你的文章 {articleTitle}"
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
     * 发送渠道：1-站内信，2-邮件，3-短信，4-多渠道
     */
    @Schema(
            description = "发送渠道：1-站内信，2-邮件，3-短信，4-多渠道",
            example = "1",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Integer sendChannel;

    /**
     * 状态：0停用，1启用
     */
    @Schema(
            description = "状态：0停用，1启用",
            example = "1"
    )
    private Integer status;

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