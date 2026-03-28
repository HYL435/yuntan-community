package com.yuntan.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "消息模板实体")
public class MailTemplateDTO {

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

}
