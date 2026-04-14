from typing import Any, cast
from app.core.config import settings

# 尝试导入 openai 包中的 AsyncOpenAI（适用于 openai v2 的异步客户端）
try:
    from openai import AsyncOpenAI
except ImportError:
    AsyncOpenAI = None  # 若未安装 openai，则保持为 None，代码会使用 mock 模式或抛错


class LLMClient:
    # 单例客户端引用，延迟创建
    _client = None

    @classmethod
    def get_client(cls):
        # 如果尚未创建客户端且 AsyncOpenAI 可用，则创建一个
        if cls._client is None and AsyncOpenAI is not None:
            cls._client = AsyncOpenAI(
                api_key=settings.llm_api_key,
                base_url=settings.llm_base_url
            )
        return cls._client

    @classmethod
    async def chat(cls, prompt: str) -> str:
        # 开发模式：如果启用了 mock_llm，则直接返回模拟回复，避免真实网络调用
        if settings.mock_llm:
            return f"【模拟回复】你发送的内容是：{prompt}"

        # 获取真实客户端，如果未安装 openai 包则抛出运行时错误
        client = cls.get_client()
        if client is None:
            raise RuntimeError("openai package 未安装或不可用，无法调用真实模型，请设置 MOCK_LLM=true 或 安装 openai 包")

        # 将 messages 列表显式 cast 为 Any 来避免类型检查器对 openai SDK 类型的不兼容警告
        messages = cast(Any, [
            {"role": "system", "content": "你是云潭社区的AI助手，请使用自然、准确、简洁的中文回答。"},
            {"role": "user", "content": prompt}
        ])

        # 调用 openai 的 chat 完成接口（依赖具体 openai SDK 的用法）
        completion = await client.chat.completions.create(
            model=settings.llm_model,
            messages=messages,
            temperature=0.7,
        )

        # 返回模型回复文本，防止 None 的情况返回空字符串
        return completion.choices[0].message.content or ""