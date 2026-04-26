package com.yuntan.ai.request;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PythonChatStreamRequest {


    /**
     * 会话ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "会话ID", example = "1")
    @JsonProperty("session_id")
    private Long sessionId;

    /**
     * 用户ID
     */
    @Schema(
            description = "用户ID",
            example = "10001",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @JsonProperty("user_id")
    private Long userId;

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
     * 模型名称
     */
    @Schema(
            description = "模型名称",
            example = "gpt-3.5-turbo",
            maxLength = 100
    )
    private String model;

    /**
     * 消息内容
     */
    @Schema(
            description = "消息内容",
            example = "请帮我解释一下Spring事务传播行为",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @JsonProperty("message")
    private String  content;
}
