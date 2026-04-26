package com.yuntan.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class ChatPrepareResultDTO {

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
     * 用户消息ID
     */
    @Schema(
            description = "用户消息ID",
            example = "30001",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Long userMessageId;

    /**
     * AI消息ID
     */
    @Schema(
            description = "AI消息ID",
            example = "30002",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Long assistantMessageId;

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

}
