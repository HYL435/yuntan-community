package com.yuntan.ai.service.impl;

import com.yuntan.ai.dto.AiChatStreamDTO;
import com.yuntan.ai.dto.ChatPrepareResultDTO;
import com.yuntan.ai.dto.SequencePair;
import com.yuntan.ai.entity.AiChatMessage;
import com.yuntan.ai.entity.AiChatSession;
import com.yuntan.ai.service.AiChatPrepareService;
import com.yuntan.ai.service.IAiChatMessageService;
import com.yuntan.ai.service.IAiChatSessionService;
import com.yuntan.common.context.BaseContext;
import com.yuntan.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiChatPrepareServiceImpl implements AiChatPrepareService {

    private final IAiChatSessionService aiChatSessionService;
    private final IAiChatMessageService aiChatMessageService;

    @Transactional(rollbackFor = Exception.class)
    public ChatPrepareResultDTO prepareChat(AiChatStreamDTO aiChatStreamDTO) {

        // 检查请求ID，如果存在，则返回一个错误消息，表示请求已经存在，避免重复处理同一个请求
        String requestId = aiChatStreamDTO.getRequestId();

        // 校验 requestId 是否存在，如果存在则返回错误，避免重复处理同一个请求
        if (requestId == null || requestId.isBlank()) {
            throw new IllegalArgumentException("请求ID不能为空");
        }

        // 创建或获取会话
        AiChatSession aiChatSession = aiChatSessionService.getOrCreateSession(aiChatStreamDTO);

        // 获取会话ID和用户ID，用于后续的日志记录和数据落库
        Long sessionId = aiChatSession.getId();
        Long userId = BaseContext.getUserId();

        // 生成唯一标识符，用于追踪整个请求的生命周期，方便日志记录和问题排查
        String traceId = UUID.randomUUID().toString().replace("-", "");

        // 分配用户消息和助手消息的序列号，确保消息在会话中的顺序正确
        SequencePair sequencePair = aiChatSessionService.allocateSequence(sessionId);
        // 获取用户消息的下一个序列号，确保消息在会话中的顺序正确
        Integer userSeq = sequencePair.getUserSeq();
        // 获取助手消息的序列号，确保消息在会话中的顺序正确
        Integer assistantSeq = sequencePair.getAssistantSeq();

        AiChatMessage assistantMessage =  null;

        try {
            // 创建用户消息，并保存到数据库中，记录用户的输入内容和相关的追踪信息
            aiChatMessageService.saveUSerMessage(
                    sessionId,
                    userId,
                    userSeq,
                    aiChatStreamDTO.getContent(),
                    traceId,
                    requestId
            );

            // 创建助手消息，用于保存助手的回复，并返回给前端一个占位消息，表示助手正在生成回复
            assistantMessage = aiChatMessageService.saveAssistantPlaceholderMessage(
                    sessionId,
                    userId,
                    assistantSeq,
                    traceId,
                    requestId
            );
        } catch (DuplicateKeyException e) {
            String msg  = e.getMessage();

            // UK_USER_REQUEST_ROLE是用户请求唯一约束，可能是因为同一个用户在同一个会话中重复提交了相同的请求，导致数据库唯一约束冲突，
            if (msg != null && msg.contains("uk_user_request_role")) {
                throw new DuplicateKeyException("请勿重复提交");
            }

            // 消息号冲突，可能是因为在高并发场景下，多个请求同时获取了相同的序列号，导致数据库唯一约束冲突，
            // 这时我们可以提示用户稍后重试，或者直接重试当前操作
            if (msg != null && msg.contains("uk_session_sequence")) {
                throw new DuplicateKeyException("消息号冲突，请重试");
            }
            // 其他唯一约束冲突，可能由于数据结构设计错误或者数据量过大，导致数据库唯一约束冲突，
            // 这时我们可以提示用户稍后重试，或者直接重试当前操作
            throw new BusinessException("消息唯一约束冲突，请稍后重试");
        } catch (DataAccessException e) {
            log.error("数据库访问异常，异常信息：{}", e.getMessage());
            throw new BusinessException("消息保存失败，请稍后重试");
        }

        assert assistantMessage != null;
        Long assistantMessageId = assistantMessage.getId();

        return ChatPrepareResultDTO.builder()
                .sessionId(sessionId)
                .userId(userId)
                .assistantMessageId(assistantMessageId)
                .traceId(traceId)
                .requestId(requestId)
                .build();

    }

}
