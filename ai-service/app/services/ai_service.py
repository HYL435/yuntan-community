"""
模块说明：
AIService 提供与上层业务交互的 AI 能力封装，主要负责：
- 根据请求选择 LLM 提供者（支持从请求中指定或使用全局默认）
- 通过工厂构造对应的 LLM 客户端并调用其 chat 接口
- 将底层客户端的回复封装为统一的 ChatResponse 返回

注意：本服务不负责具体的鉴权/限流/持久化，调用者应在外层处理这些横切关注点。
"""

from app.schemas.chat import ChatStreamRequest, ChatResponse
from app.factory.llm_factory import LLMFactory
from app.core.config import settings


class AIService:
    """
    AIService - AI 能力业务层封装

    说明：
    - 接收已校验的 ChatRequest，负责调度对应的 LLM 客户端并返回 ChatResponse。
    - 通过 LLMFactory 动态获取客户端实现，便于运行时切换模型提供者。
    """

    @staticmethod
    async def chat(req: ChatStreamRequest) -> ChatResponse:
        """
        调用底层 LLM 客户端进行聊天并返回统一响应。

        参数：
        - req: 已通过 pydantic 校验的 ChatRequest，可能包含可选的 provider 字段

        行为：
        1. 从请求中读取 provider；若请求未指定则使用全局配置 settings.default_provider 作为回退。
        2. 通过 LLMFactory.get_client(provider) 获取对应的客户端实例（若 provider 不支持，会抛出 ValueError）。
        3. 调用客户端的异步 chat 方法获取回复；任何底层异常会向上抛出，由调用方处理（例如日志、重试或转换为 HTTP 响应）。
        4. 将最终回复封装为 ChatResponse 返回，包含 provider、model 与 reply 信息，便于上层展示或上报。

        返回：
        - ChatResponse: 包含 provider（实际使用的提供者）、model（实际模型名）与 reply（模型回复文本）
        """
        # 从请求中读取提供者；若未指定则使用全局默认 provider
        provider = req.provider or settings.default_provider

        # 通过工厂获取对应的 LLM 客户端实例（可能抛出 ValueError 表示不支持的 provider）
        client = LLMFactory.get_client(provider)

        # 调用客户端的 chat 方法获取回复（异步），底层可能执行网络请求或返回模拟内容
        reply = await client.chat(req.message)

        # 将客户端信息与回复封装到统一响应模型中返回
        return ChatResponse(
            provider=client.provider,
            model=client.model,
            reply=reply
        )

    """
    AIService 的摘要能力封装，与 Chat 类似，但返回 SummaryResponse。
    """
    @classmethod
    async def summary(cls, req):
        pass