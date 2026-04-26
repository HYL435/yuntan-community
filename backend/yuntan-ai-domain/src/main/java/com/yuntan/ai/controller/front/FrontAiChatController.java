package com.yuntan.ai.controller.front;

import com.yuntan.ai.dto.AiChatStreamDTO;
import com.yuntan.ai.event.AiStreamEvent;
import com.yuntan.ai.service.AiChatStreamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/front/ai")
@Tag(description = "前台AI聊天接口", name = "AI聊天")
@Slf4j
@RequiredArgsConstructor
public class FrontAiChatController {

    private final AiChatStreamService aiChatSessionService;

    /**
     * MediaType.TEXT_EVENT_STREAM_VALUE 表示返回的数据类型是Server-Sent Events流，
     * 这是一种基于HTTP的单向通信协议，服务器可以持续不断地向客户端发送数据，而客户端只需要发起一次请求即可。
     * 这种方式非常适合用于实现实时聊天等场景，可以大大减少网络开销和延迟，提高用户体验。
     */
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "AI流式聊天")
    public Flux<ServerSentEvent<AiStreamEvent>> streamChat(@RequestBody AiChatStreamDTO aiChatStreamDTO) {
        log.info("AI流式聊天 {}" , aiChatStreamDTO);

        return aiChatSessionService.streamChat(aiChatStreamDTO);
    }

}
