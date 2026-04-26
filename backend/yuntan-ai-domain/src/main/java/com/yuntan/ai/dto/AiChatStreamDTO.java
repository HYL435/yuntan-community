package com.yuntan.ai.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiChatStreamDTO {

    /**
     * 会话ID，可以为空，如果为空则创建新的会话
     */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "会话ID", example = "1")
    private Long sessionId;

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
    private String content;

    /**
     * 请求ID/幂等ID
     */
    @Schema(
            description = "请求ID/幂等ID",
            example = "req-987654321",
            maxLength = 64
    )
    private String requestId;

}
