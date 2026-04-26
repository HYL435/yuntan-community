package com.yuntan.ai.service;

import com.yuntan.ai.dto.AiChatStreamDTO;
import com.yuntan.ai.event.AiStreamEvent;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;

public interface AiChatStreamService {

    /**
     * 流式聊天
     * @param aiChatStreamDTO
     * @return
     */
    Flux<ServerSentEvent<AiStreamEvent>> streamChat(AiChatStreamDTO aiChatStreamDTO);

}
