from app.clients.base import BaseLLMClient
from app.core.config import settings


class GLMClient(BaseLLMClient):
    @property
    def provider(self) -> str:
        return "glm"

    @property
    def model(self) -> str:
        return settings.glm_model

    async def chat(self, message: str) -> str:
        if settings.mock_llm:
            return f"【GLM模拟回复】你发送的是：{message}"
        raise NotImplementedError("真实 GLM API 调用暂未接入")