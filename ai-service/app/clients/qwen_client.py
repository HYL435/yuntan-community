# 导入 OpenAI 异步客户端库
from openai import AsyncOpenAI
# 导入 OpenAI 消息类型定义，用于类型检查
from openai.types.chat import ChatCompletionSystemMessageParam, ChatCompletionUserMessageParam

from app.clients.base import BaseLLMClient
from app.core.config import settings
from app.core.exceptions import AIServiceException


class QwenClient(BaseLLMClient):
    """通义千问 LLM 客户端实现"""

    def __init__(self):
        # 初始化异步 OpenAI 客户端，使用通义千问的 API 配置
        self._client = AsyncOpenAI(
            api_key=settings.qwen_api_key,
            base_url=settings.qwen_base_url
        )

    @property
    def provider(self) -> str:
        """返回提供商名称"""
        return "qwen"

    @property
    def model(self) -> str:
        """返回使用的模型名称"""
        return settings.qwen_model

    async def chat(self, message: str) -> str:
        """
        发送消息到通义千问并获取回复

        Args:
            message: 用户发送的消息内容

        Returns:
            str: AI 助手的回复内容
        """
        # 如果启用了模拟模式，返回模拟回复
        if settings.mock_llm:
            return f"【通义模拟回复】你发送的是：{message}"

        # 检查 API Key 是否已配置
        if not settings.qwen_api_key:
            raise AIServiceException("QWEN_API_KEY 未配置")

        try:
            # 构建消息列表，包含系统提示和用户消息
            messages: list[ChatCompletionSystemMessageParam | ChatCompletionUserMessageParam] = [
                ChatCompletionSystemMessageParam(
                    role="system",
                    content="你是云坛社区的AI助手，请使用自然、准确、简洁的中文回答。"
                ),
                ChatCompletionUserMessageParam(
                    role="user",
                    content=message
                )
            ]
            # 调用通义千问 API 获取回复
            completion = await self._client.chat.completions.create(
                model=self.model,
                messages=messages,
                temperature=0.7,  # 控制回复的随机性，0.7 表示适度创造性
            )
        except Exception as e:
            raise AIServiceException(f"通义千问调用失败: {e}") from e

        try:
            # 从 API 响应中提取回复内容
            content = completion.choices[0].message.content
        except Exception as e:
            raise AIServiceException("通义千问返回结果解析失败") from e

        # 检查回复内容是否为空
        if not content:
            raise AIServiceException("通义千问返回内容为空")

        return content
