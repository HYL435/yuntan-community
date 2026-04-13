from fastapi import APIRouter
from app.schemas.ai import ChatRequest, ChatResponse, SummaryRequest, SummaryResponse
from app.schemas.common import ApiResponse
from app.services.ai_service import AIService

router = APIRouter(prefix="/v1/ai", tags=["AI"])


@router.get("/health")
async def health():
    return ApiResponse.success(
        data={"status": "ok", "service": "ai-service"},
        message="服务运行正常"
    )


@router.post("/chat", response_model=ApiResponse[ChatResponse])
async def chat(req: ChatRequest):
    result = await AIService.chat(req)
    return ApiResponse.success(data=result, message="聊天成功")


@router.post("/summary", response_model=ApiResponse[SummaryResponse])
async def summary(req: SummaryRequest):
    result = await AIService.summary(req)
    return ApiResponse.success(data=result, message="总结成功")