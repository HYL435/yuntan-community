package com.yuntan.ai.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yuntan.ai.entity.AiChatMessage;

public interface IAiChatMessageService extends IService<AiChatMessage> {


    /**
     * 保存用户聊天消息
     */
    AiChatMessage saveUSerMessage(
            Long sessionId,
            Long userId,
            Integer sequenceNo,
            String content,
            String traceId,
            String requestId
    );


    /**
     * 保存 assistant 占位消息
     */
    AiChatMessage saveAssistantPlaceholderMessage(
            Long sessionId,
            Long userId,
            Integer sequenceNo,
            String traceId,
            String requestId
    );



    /**
     * 更新 assistant 聊天消息成功
     */
    void updateAssistantMessageSuccess(
            Long assistantMessageId,
            String content,
            String provider,
            String modelName,
            Integer promptTokens,
            Integer completionTokens,
            Integer totalTokens,
            String finishReason
    );


    /**
     * 更新 assistant 聊天消息失败
     */
    void updateAssistantMessageError(
            Long assistantMessageId,
            String errorMessage
    );

    /**
     * 获取当前会话的下一个消息序列号
     * @param sessionId
     * @return
     */
    Integer getNextSequenceNo(Long sessionId);

}
