import asyncio
from typing import AsyncGenerator

from app.providers.base import BaseChatProvider
from app.schemas.chat import ChatStreamRequest, StreamEvent


class MockChatProvider(BaseChatProvider):
    """
    一个假的模型适配器
    用来真实模拟大模型的流式输出
    """

    # 模拟模型的基本信息
    provider_name = "mock"
    model_name = "mock-model-v1"

    async def stream_chat(self, req: ChatStreamRequest) -> AsyncGenerator[StreamEvent, None]:

        # 1. start 事件：告诉 java 本次使用的模型信息
        # yield 关键字用于生成器函数，允许我们在函数执行过程中多次返回值
        yield StreamEvent(
            type="start",
            provider=self.provider_name,
            model=self.model_name,
        )

        # 模拟模型处理过程，await 用于等待异步操作完成
        await asyncio.sleep(0.2)

        chunks = [
            "你好",
            "我收到你的问题：",
            req.message,
            "。",
            "这是 mock 模型的测试输出。"
        ]
        # 2. delta 事件：模拟模型处理过程，每次返回一个片段
        for chunk in chunks:
            yield StreamEvent(
                type="chunk",
                provider=self.provider_name,
                model=self.model_name,
                content=chunk,
            )

            # 模拟模型处理过程，await 用于等待异步操作完成
            await asyncio.sleep(0.4)

        # 3. usage 事件：模拟token用量
        yield StreamEvent(
            type="usage",
            prompt_tokens=20,
            completion_tokens=30,
            total_tokens=50
        )

        # 4. done 事件：告诉 java 本次对话结束
        yield StreamEvent(
            type="done",
            finish_reason="stop"
        )