from app.providers.base import BaseChatProvider
from app.providers.deepseek_provider import DeepSeekProvider
from app.providers.mock_provider import MockChatProvider
from app.providers.qwen_provider import QwenProvider
from app.schemas.chat import ChatStreamRequest


class ChatService:

    def __init__(self):
        self.providers: dict[str, BaseChatProvider] = {
            "mock": MockChatProvider(),
            "deepseek": DeepSeekProvider(),
            "qwen": QwenProvider(),
        }

    def get_provider(self, req: ChatStreamRequest) -> BaseChatProvider:
        provider_name = req.provider or "mock"
        provider = self.providers.get(provider_name)

        if provider is None:
            raise ValueError(f"Unsupported provider: {provider_name}")

        return provider