package com.yuntan.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuntan.ai.entity.AiChatMessage;
import com.yuntan.ai.enums.AiMessageRole;
import com.yuntan.ai.enums.AiMessageStatus;
import com.yuntan.ai.mapper.AiChatMessageMapper;
import com.yuntan.ai.service.IAiChatMessageService;
import com.yuntan.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AiChatMessageServiceImpl extends ServiceImpl<AiChatMessageMapper, AiChatMessage> implements IAiChatMessageService {

    private final AiChatMessageMapper aiChatMessageMapper;






    // 保存用户聊天消息
    public AiChatMessage saveUSerMessage(
            Long sessionId,
            Long userId,
            Integer sequenceNo,
            String content,
            String traceId,
            String requestId
            ) {
        // 构建聊天消息对象
        AiChatMessage aiChatMessage = AiChatMessage.builder()
                .sessionId(sessionId)
                .userId(userId)
                .role(AiMessageRole.USER.getCode())
                .sequenceNo(sequenceNo)
                .content(content)
                .status(AiMessageStatus.SUCCESS.getCode())
                .traceId(traceId)
                .requestId(requestId)
                .build();

        try {
            // 保存聊天消息到数据库
            aiChatMessageMapper.insert(aiChatMessage);

            return aiChatMessage;
        } catch (Exception e) {
            throw new BusinessException("请勿重复提交");
        }
    }

    // 保存 assistant 占位消息
    public AiChatMessage saveAssistantPlaceholderMessage(
            Long sessionId,
            Long userId,
            Integer sequenceNo,
            String traceId,
            String requestId
    ) {
        // 构建占位消息对象
        AiChatMessage aiChatMessage = AiChatMessage.builder()
                .sessionId(sessionId)
                .userId(userId)
                .role(AiMessageRole.ASSISTANT.getCode())
                .sequenceNo(sequenceNo)
                .content(null) // 占位消息内容为空
                .status(AiMessageStatus.GENERATING.getCode()) // 设置状态为生成中
                .traceId(traceId)
                .requestId(requestId)
                .build();

        // 保存占位消息到数据库
        aiChatMessageMapper.insert(aiChatMessage);

        return aiChatMessage;
    }

    // 更新 assistant 聊天消息成功
    public void updateAssistantMessageSuccess(
            Long assistantMessageId,
            String content,
            String provider,
            String model,
            Integer promptTokens,
            Integer completionTokens,
            Integer totalTokens,
            String finishReason
    ) {
        AiChatMessage update = AiChatMessage.builder()
                .id(assistantMessageId)
                .content(content)
                .provider(provider)
                .model(model)
                .promptTokens(promptTokens)
                .completionTokens(completionTokens)
                .totalTokens(totalTokens)
                .finishReason(finishReason)
                .status(AiMessageStatus.SUCCESS.getCode())
                .errorMessage(null)
                .build();

        // 更新数据库
        aiChatMessageMapper.updateById(update);
    }

    // 更新 assistant 聊天消息失败
    public void updateAssistantMessageError(
            Long assistantMessageId,
            String errorMessage
    ) {
        AiChatMessage update = AiChatMessage.builder()
                .id(assistantMessageId)
                .status(AiMessageStatus.FAILED.getCode())
                .errorMessage(errorMessage)
                .build();

        // 更新数据库
        aiChatMessageMapper.updateById(update);
    }

    // 获取当前会话最大消息序列号
    public Integer getNextSequenceNo(Long sessionId) {
        // 查询当前会话的最后一条消息，获取其序列号，然后加1作为下一条消息的序列号
        AiChatMessage lastMessage = aiChatMessageMapper.selectOne(
                new LambdaQueryWrapper<AiChatMessage>()
                        .eq(AiChatMessage::getSessionId, sessionId)
                        .orderByDesc(AiChatMessage::getSequenceNo)
                        .last("LIMIT 1")
        );

        if (lastMessage == null || lastMessage.getSequenceNo() == null) {
            return 1; // 如果没有消息，则从1开始
        } else {
            return lastMessage.getSequenceNo() + 1; // 否则在当前最大序列号基础上加1
        }
    }


}
