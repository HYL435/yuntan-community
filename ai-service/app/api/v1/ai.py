from fastapi import APIRouter
# 导入请求/响应模型
from app.schemas.ai import ChatRequest, ChatResponse, SummaryRequest, SummaryResponse
from app.schemas.common import ApiResponse
# 导入服务层实现
from app.services.ai_service import AIService

# 创建子路由，所有路由前缀为 /v1/ai，并打上标签用于文档分组
router = APIRouter(prefix="/v1/ai", tags=["AI"])


@router.get("/health")
async def health():
    # 健康检查接口，返回统一响应格式
    return ApiResponse.success(
        data={"status": "ok", "service": "ai-service"},
        message="服务运行正常"
    )


@router.post("/chat", response_model=ApiResponse[ChatResponse])
async def chat(req: ChatRequest):
    # 聊天接口，接收 ChatRequest，调用服务层处理并包装为 ApiResponse
    result = await AIService.chat(req)
    return ApiResponse.success(data=result, message="聊天成功")


@router.post("/summary", response_model=ApiResponse[SummaryResponse])
async def summary(req: SummaryRequest):
    # 摘要接口，接收 SummaryRequest，调用服务层并返回结果
    result = await AIService.summary(req)
    return ApiResponse.success(data=result, message="总结成功")