package com.yuntan.ai.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yuntan.ai.dto.AiChatStreamDTO;
import com.yuntan.ai.dto.SequencePair;
import com.yuntan.ai.entity.AiChatSession;

public interface IAiChatSessionService extends IService<AiChatSession> {


    /**
     * 获取会话或者创建会话
      * @param aiChatStreamDTO
      * @return
      * @throws RuntimeException 如果会话不存在或者无权访问会话
     */
    AiChatSession getOrCreateSession(AiChatStreamDTO aiChatStreamDTO);


    /**
     * 为当前会话分配序列号，确保消息在会话中的顺序正确
     * @param sessionId
     * @return
     */
    SequencePair allocateSequence(Long sessionId);
}
