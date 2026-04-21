from app.clients.base import BaseLLMClient
from app.core.config import settings


class DeepSeekClient(BaseLLMClient):
    @property
    def provider(self) -> str:
        return "deepseek"

    @property
    def model(self) -> str:
        return settings.deepseek_model

    async def chat(self, message: str) -> str:
        if settings.mock_llm:
            return f"【DeepSeek模拟回复】你发送的是：{message}"
        raise NotImplementedError("真实 DeepSeek API 调用暂未接入")