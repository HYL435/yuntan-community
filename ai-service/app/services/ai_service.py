from app.clients.llm_client import LLMClient
from app.schemas.ai import ChatRequest, ChatResponse, SummaryRequest, SummaryResponse
from app.core.config import settings


class AIService:
    @staticmethod
    async def chat(req: ChatRequest) -> ChatResponse:
        prompt = req.message.strip()
        reply = await LLMClient.chat(prompt)
        return ChatResponse(
            reply=reply,
            model=settings.llm_model
        )

    @staticmethod
    async def summary(req: SummaryRequest) -> SummaryResponse:
        prompt = f"请对以下内容做简洁准确的总结：\n\n{req.content.strip()}"
        summary_text = await LLMClient.chat(prompt)
        return SummaryResponse(
            summary=summary_text,
            model=settings.llm_model
        )