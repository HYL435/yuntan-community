from app.clients.llm_client import LLMClient
from app.schemas.ai import ChatRequest, ChatResponse, SummaryRequest, SummaryResponse
from app.core.config import settings


class AIService:
    @staticmethod
    async def chat(req: ChatRequest) -> ChatResponse:
        # 从请求中取出用户消息并去除两端空白
        prompt = req.message.strip()
        # 调用 LLM 客户端获取回复（可能是模拟或真实模型）
        reply = await LLMClient.chat(prompt)
        # 返回 ChatResponse 数据模型实例
        return ChatResponse(
            reply=reply,
            model=settings.llm_model
        )

    @staticmethod
    async def summary(req: SummaryRequest) -> SummaryResponse:
        # 构造用于摘要的提示词
        prompt = f"请对以下内容做简洁准确的总结：\n\n{req.content.strip()}"
        # 使用 LLM 客户端获取摘要文本
        summary_text = await LLMClient.chat(prompt)
        # 返回 SummaryResponse 数据模型实例
        return SummaryResponse(
            summary=summary_text,
            model=settings.llm_model
        )