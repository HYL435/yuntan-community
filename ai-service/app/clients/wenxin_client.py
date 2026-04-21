from app.clients.base import BaseLLMClient
from app.core.config import settings


class WenxinClient(BaseLLMClient):
    @property
    def provider(self) -> str:
        return "wenxin"

    @property
    def model(self) -> str:
        return settings.wenxin_model

    async def chat(self, message: str) -> str:
        if settings.mock_llm:
            return f"【文心模拟回复】你发送的是：{message}"
        raise NotImplementedError("真实文心 API 调用暂未接入")