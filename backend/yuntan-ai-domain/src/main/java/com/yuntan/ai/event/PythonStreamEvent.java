package com.yuntan.ai.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PythonStreamEvent {

    /**
     * 消息类型：start/stream/end/error
     */
    private String type;

    /**
     * 消息内容
     */
    private String content;

    private String model;
    private String provider;

    @JsonProperty("prompt_tokens")
    private Integer promptTokens;

    @JsonProperty("completion_tokens")
    private Integer completionTokens;

    @JsonProperty("total_tokens")
    private Integer totalTokens;

    @JsonProperty("finish_reason")
    private String finishReason;

    private String code;
    private String message;
}