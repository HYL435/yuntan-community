import os

from app.providers.openai_compatible import OpenAICompatibleProvider


class DeepSeekProvider(OpenAICompatibleProvider):
    provider_name = "deepseek"
    default_model = "deepseek-v4-flash"
    base_url = "https://api.deepseek.com"

    def __init__(self):
        self.api_key = os.getenv("DEEPSEEK_API_KEY", "")