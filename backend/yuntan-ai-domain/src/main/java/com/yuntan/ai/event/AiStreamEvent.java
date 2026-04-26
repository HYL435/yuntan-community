package com.yuntan.ai.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiStreamEvent {

    /**
     * 会话ID
     */
    private Long sessionId;

    /**
     * 消息类型：start/stream/end/error
     */
    private String type;

    /**
     * 消息内容
     */
    private String content;

    // 模型名称，只有在type为start时才会有值
    private String model;
    private String provider;


    // 统计信息，以下3个字段为统计信息，只有在type为end或error时才会有值
    private Integer promptTokens;
    private Integer completionTokens;
    private Integer totalTokens;

    // 结束原因，只有在type为end或error时才会有值，可能的值包括：stop（正常结束）、length（达到最大长度限制）、error（发生错误）等
    private String finishReason;

    // 错误代码，只有在type为error时才会有值
    private String code;
    // 错误信息，只有在type为error时才会有值
    private String message;


    // 工厂方法，创建一个会话开始事件，用于通知前端开始一个新的会话
    public static AiStreamEvent session(Long sessionId) {
        return AiStreamEvent.builder()
                .type("session")
                .sessionId(sessionId)
                .build();
    }
}