package com.yuntan.ai.handler;

import com.yuntan.ai.event.AiStreamEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;

@Slf4j
@RestControllerAdvice
public class AiChatExceptionHandler {

    /**
     * 处理 WebClient 调用外部服务时的异常(针对 SSE 流式接口)
     */
    @ExceptionHandler(WebClientResponseException.class)
    public Flux<ServerSentEvent<AiStreamEvent>> handleWebClientException(WebClientResponseException e) {
        log.error("WebClient 调用外部服务异常: {} - {}", e.getStatusCode(), e.getMessage());

        AiStreamEvent errorEvent = AiStreamEvent.builder()
                .type("error")
                .code(String.valueOf(e.getStatusCode().value()))
                .message("AI 服务调用失败: " + e.getMessage())
                .build();

        return Flux.just(
                ServerSentEvent.<AiStreamEvent>builder()
                        .event("error")
                        .data(errorEvent)
                        .build()
        );
    }

}
