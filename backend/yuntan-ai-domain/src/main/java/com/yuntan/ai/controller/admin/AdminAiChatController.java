package com.yuntan.ai.controller.admin;

import com.yuntan.ai.service.IAiChatSessionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/ai/chat")
@Tag(description = "后台AI会话接口", name = "AI会话管理")
@RequiredArgsConstructor
@Slf4j
public class AdminAiChatController {

    private final IAiChatSessionService aiChatSessionService;

}
