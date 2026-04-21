package com.yuntan.ai.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuntan.ai.entity.AiChatSession;
import com.yuntan.ai.mapper.AiChatSessionMapper;
import com.yuntan.ai.service.IAiChatSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AiChatSessionServiceImpl extends ServiceImpl<AiChatSessionMapper, AiChatSession> implements IAiChatSessionService {
}
