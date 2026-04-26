package com.yuntan.ai.service;

import com.yuntan.ai.dto.AiChatStreamDTO;
import com.yuntan.ai.dto.ChatPrepareResultDTO;

public interface AiChatPrepareService {

    /**
     * 预处理
     * @param aiChatStreamDTO
     * @return
     */
    ChatPrepareResultDTO prepareChat(AiChatStreamDTO aiChatStreamDTO);
}
