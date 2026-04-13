from app.core.config import settings

try:
    from openai import AsyncOpenAI
except ImportError:
    AsyncOpenAI = None


class LLMClient:
    _client = None

    @classmethod
    def get_client(cls):
        if cls._client is None and AsyncOpenAI is not None:
            cls._client = AsyncOpenAI(
                api_key=settings.llm_api_key,
                base_url=settings.llm_base_url
            )
        return cls._client

    @classmethod
    async def chat(cls, prompt: str) -> str:
        # 开发模式：直接返回模拟结果
        if settings.mock_llm:
            return f"【模拟回复】你发送的内容是：{prompt}"

        client = cls.get_client()
        if client is None:
            raise RuntimeError("openai package 未安装，无法调用真实模型")

        completion = await client.chat.completions.create(
            model=settings.llm_model,
            messages=[
                {"role": "system", "content": "你是云潭社区的AI助手，请使用自然、准确、简洁的中文回答。"},
                {"role": "user", "content": prompt}
            ],
            temperature=0.7,
        )

        return completion.choices[0].message.content or ""