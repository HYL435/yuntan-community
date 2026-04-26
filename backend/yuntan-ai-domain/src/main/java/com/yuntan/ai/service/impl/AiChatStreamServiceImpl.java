package com.yuntan.ai.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yuntan.ai.dto.AiChatStreamDTO;
import com.yuntan.ai.dto.ChatPrepareResultDTO;
import com.yuntan.ai.event.AiStreamEvent;
import com.yuntan.ai.event.PythonStreamEvent;
import com.yuntan.ai.request.PythonChatStreamRequest;
import com.yuntan.ai.service.AiChatPrepareService;
import com.yuntan.ai.service.AiChatStreamService;
import com.yuntan.ai.service.IAiChatMessageService;
import com.yuntan.ai.service.IAiChatSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.concurrent.atomic.AtomicReference;



@Service
@RequiredArgsConstructor
@Slf4j
public class AiChatStreamServiceImpl implements AiChatStreamService {

    private final IAiChatSessionService aiChatSessionService;
    private final IAiChatMessageService aiChatMessageService;
    private final AiChatPrepareService aiChatPrepareService;

    private final WebClient aiWebClient;
    // 创建一个ObjectMapper对象，用于将JSON字符串转为PythonStreamEvent对象
    private final ObjectMapper objectMapper;


    /**
     * 流式聊天
     *
     * @param aiChatStreamDTO
     * @return
     */
    public Flux<ServerSentEvent<AiStreamEvent>> streamChat(AiChatStreamDTO aiChatStreamDTO) {

        // 预处理，获取会话ID、用户ID、消息ID等必要的信息，并进行相关的数据库操作，比如创建消息记录，更新消息状态等
        ChatPrepareResultDTO prepareResult = aiChatPrepareService.prepareChat(aiChatStreamDTO);

        // 构建发送给Python服务的DTO
        PythonChatStreamRequest pythonRequest = new PythonChatStreamRequest(
                prepareResult.getSessionId(),
                prepareResult.getUserId(),
                aiChatStreamDTO.getProvider(),
                aiChatStreamDTO.getModel(),
                aiChatStreamDTO.getContent()
        );

        // 创建一个用于存储完整答案的StringBuilder对象
        StringBuilder fullAnswer = new StringBuilder();

        // 创建一个 AtomicReference 用于存储模型提供商
        // AtomicReference 是一个原子类，用于保证线程安全
        // 在多线程环境下，AtomicReference 可以保证多个线程对同一个变量的访问和修改是安全的
        AtomicReference<String> providerRef = new AtomicReference<>();
        AtomicReference<String> modelNameRef = new AtomicReference<>();
        AtomicReference<Integer> promptTokensRef = new AtomicReference<>();
        AtomicReference<Integer> completionTokensRef = new AtomicReference<>();
        AtomicReference<Integer> totalTokensRef = new AtomicReference<>();
        AtomicReference<String> finishReasonRef = new AtomicReference<>();


        // 创建一个用于发送会话信息的Server-Sent Events流，前端可以根据这个事件来显示会话相关的信息，比如消息ID、会话ID等
        Flux<ServerSentEvent<AiStreamEvent>> sessionEvent = Flux.just(
                ServerSentEvent.<AiStreamEvent>builder()
                        .event("session")
                        .data(AiStreamEvent.session(prepareResult.getSessionId()))
                        .build()
        );

        // 发送请求给Python服务，并将响应转换为Server-Sent Events流
        Flux<ServerSentEvent<AiStreamEvent>> pythonStreamEvent = aiWebClient.post()
                .uri("/api/v1/chat/completions/stream")
                // 设置请求头，指定内容类型为JSON，接受的响应类型为SSE流
                .contentType(MediaType.APPLICATION_JSON)
                // 响应类型为SSE流，告诉服务器我们希望以流式的方式接收数据，而不是一次性接收完整的响应
                .accept(MediaType.TEXT_EVENT_STREAM)
                // 设置请求体，将PythonChatStreamRequest对象作为请求体
                .bodyValue(pythonRequest)
                // 发送请求并获取响应，retrieve()方法会触发请求的发送，并返回一个ResponseSpec对象，我们可以通过这个对象来处理响应
                .retrieve()
                // 将响应流转换为Server-Sent Events流
                .bodyToFlux(String.class)
                // 将python原始字符串转为PythonStreamEvent对象
                .map(this::parsePythonEvent)
                // 处理每个事件，它只是"观察"或"监听"数据，执行一些额外操作
                // 数据会原封不动地传递给下一个操作符，每当 Flux 中有一个新元素到达时立即执行
                // 对于流式场景，每收到一块数据就触发一次
                .doOnNext(event -> {
                    // 处理开始事件
                    if ("start".equals(event.getType())) {
                        providerRef.set(event.getProvider());
                        modelNameRef.set(event.getModel());
                    }
                    // 处理增量数据事件
                    if ("chunk".equals(event.getType())) {
                        fullAnswer.append(event.getContent());
                    }
                    // 处理消耗统计事件
                    if ("usage".equals(event.getType())) {
                        promptTokensRef.set(event.getPromptTokens());
                        completionTokensRef.set(event.getCompletionTokens());
                        totalTokensRef.set(event.getTotalTokens());
                    }
                    // 处理结束事件
                    if ("done".equals(event.getType())) {
                        finishReasonRef.set(event.getFinishReason());
                    }
                })
                // 将PythonStreamEvent对象转为前端SSE格式
                .map(event -> ServerSentEvent.<AiStreamEvent>builder()
                        .event(event.getType())
                        .data(toAiStreamEvent(event))
                        .build()
                )
                // 监听完成事件，处理流结束的逻辑，比如清理资源，数据落库
                .doOnComplete(() -> {
                    // 流结束，更新消息状态为成功，并保存完整的答案和相关的统计信息
                    aiChatMessageService.updateAssistantMessageSuccess(
                            prepareResult.getAssistantMessageId(),
                            fullAnswer.toString(),
                            providerRef.get(),
                            modelNameRef.get(),
                            promptTokensRef.get(),
                            completionTokensRef.get(),
                            totalTokensRef.get(),
                            finishReasonRef.get()
                    );
                })
                .doOnError(e -> {
                    log.error("流异常结束，异常信息：{}", e.getMessage());
                    // 流式过程中发生任何异常都会触发这个回调，我们可以在这里进行统一的错误处理，比如记录日志，更新消息状态等
                    aiChatMessageService.updateAssistantMessageError(
                            prepareResult.getAssistantMessageId(),
                            e.getMessage()
                    );
                });

        // 返回最终的SSE流，包含session和python的SSE流，前端可以根据事件类型进行相应的处理，比如显示会话信息，展示增量数据，显示错误提示等
        return Flux.concat(sessionEvent, pythonStreamEvent);
    }

    /**
     * 将PythonStreamEvent对象转为AiStreamEvent对象，以便统一处理
     */
    private AiStreamEvent toAiStreamEvent(PythonStreamEvent event) {
        // 将PythonStreamEvent对象的属性映射到AiStreamEvent对象中，保持属性名称和类型一致
        return AiStreamEvent.builder()
                .type(event.getType())
                .content(event.getContent())
                .model(event.getModel())
                .provider(event.getProvider())
                .promptTokens(event.getPromptTokens())
                .completionTokens(event.getCompletionTokens())
                .totalTokens(event.getTotalTokens())
                .finishReason(event.getFinishReason())
                .code(event.getCode())
                .message(event.getMessage())
                .build();
    }

    /**
     * 将python原始字符串转为PythonStreamEvent对象，以便统一处理
     */
    private PythonStreamEvent parsePythonEvent(String raw) {
        try {
            // 去除首尾空格
            String text = raw.trim();
            if (text.startsWith("data:")) {
                // Python 服务返回的是 SSE 格式的数据，每一行都以 data: 开头，这是 SSE 协议的规范
                text = text.substring(5).trim();
            }
            // 将JSON字符串转为PythonStreamEvent对象
            return objectMapper.readValue(text, PythonStreamEvent.class);
        } catch (Exception e) {
            log.error("解析Python事件失败，原始数据：{}，异常信息：{}", raw, e.getMessage());
            // 创建一个fallback事件，用于返回给前端，告知无法解析事件数据
            PythonStreamEvent fallback = new PythonStreamEvent();
            fallback.setType("error");
            fallback.setContent("无法解析的事件数据: " + raw);
            fallback.setMessage("Python 流事件解析失败：" + raw);
            // 返回fallback事件，前端可以根据事件类型和消息内容进行相应的处理，比如显示错误提示
            return fallback;
        }
    }
}
