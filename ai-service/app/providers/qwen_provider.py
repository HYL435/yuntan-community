import os

from app.providers.openai_compatible import OpenAICompatibleProvider


class QwenProvider(OpenAICompatibleProvider):
    provider_name = "qwen"
    default_model = "qwen-plus"
    base_url = "https://dashscope.aliyuncs.com/compatible-mode/v1"

    def __init__(self):
        self.api_key = os.getenv("QWEN_API_KEY", "")