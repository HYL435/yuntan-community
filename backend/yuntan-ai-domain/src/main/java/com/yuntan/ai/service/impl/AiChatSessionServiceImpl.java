package com.yuntan.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuntan.ai.dto.AiChatStreamDTO;
import com.yuntan.ai.dto.SequencePair;
import com.yuntan.ai.entity.AiChatSession;
import com.yuntan.ai.enums.AiSessionStatus;
import com.yuntan.ai.mapper.AiChatMessageMapper;
import com.yuntan.ai.mapper.AiChatSessionMapper;
import com.yuntan.ai.service.IAiChatMessageService;
import com.yuntan.ai.service.IAiChatSessionService;
import com.yuntan.common.context.BaseContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AiChatSessionServiceImpl extends ServiceImpl<AiChatSessionMapper, AiChatSession> implements IAiChatSessionService {


    private final IAiChatMessageService aiChatMessageService;
    private final AiChatSessionMapper aiChatSessionMapper;
    private final AiChatMessageMapper aiChatMessageMapper;



    // 获取会话标题，可以根据消息内容生成一个简短的标题，这里简单地取前20个字符作为标题
    private String getTitleFromContent(String content) {

        if (content != null && content.length() > 20) {
            return content.trim().substring(0, 20);
        } else return Objects.requireNonNullElse(content, "新会话");
    }



    // 获取会话或者创建会话
    public AiChatSession getOrCreateSession(AiChatStreamDTO aiChatStreamDTO) {
        Long userId = BaseContext.getUserId();

        if (aiChatStreamDTO.getSessionId() != null) {
            // 如果传入了会话ID，则查询会话
            AiChatSession session = aiChatSessionMapper.selectById(aiChatStreamDTO.getSessionId());
            if (session == null) {
                throw new RuntimeException("会话不存在");
            }
            if (!session.getUserId().equals(userId)) {
                throw new RuntimeException("无权访问该会话");
            }
            return session;
        }
        // 如果没有传入会话ID，则创建新的会话
        AiChatSession session = AiChatSession.builder()
                .userId(userId)
                .title(getTitleFromContent(aiChatStreamDTO.getContent()))
                .nextSequenceNo(1)
                .status(AiSessionStatus.NORMAL.getCode())
                .build();

        // 保存会话
        this.save(session);

        return session;
    }

    /**
     * 分配消息序列号
     * @param sessionId
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public SequencePair allocateSequence(Long sessionId) {

        // 加锁查询会话，确保同一时间只有一个线程能够分配序列号，避免并发问题
        AiChatSession session = aiChatSessionMapper.selectOne(
                new LambdaQueryWrapper<AiChatSession>()
                        .eq(AiChatSession::getId, sessionId)
                        .last("for update") // 加锁，确保同一时间只有一个线程能够分配序列号，避免并发问题
        );

        // 获取用户消息的下一个序列号，确保消息在会话中的顺序正确
        Integer userSeq = session.getNextSequenceNo();
        Integer assistantSeq = userSeq + 1;

        // 更新会话的序列号，确保下次分配时序列号正确
        AiChatSession update = AiChatSession.builder()
                .id(sessionId)
                .nextSequenceNo(userSeq + 2) // 更新会话的序列号
                .build();
        aiChatSessionMapper.updateById(update);

        return new SequencePair(userSeq, assistantSeq);
    }

}
