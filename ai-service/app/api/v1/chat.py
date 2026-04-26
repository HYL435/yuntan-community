import json

from fastapi import APIRouter
from starlette.responses import StreamingResponse

from app.core.exceptions import AIServiceException
# 导入请求/响应模型
from app.schemas.chat import ChatStreamRequest, ChatResponse, SummaryRequest, SummaryResponse, StreamEvent
from app.schemas.common import ApiResponse
# 导入服务层实现
from app.services.ai_service import AIService
from app.services.chat_Service import ChatService

# 创建子路由，所有路由前缀为 /v1/ai，并打上标签用于文档分组
router = APIRouter(prefix="/v1/chat", tags=["chat"])

# 创建服务层实例
chat_service = ChatService()

"""
将 StreamEvent 转换为 SSE 格式的字符串，供前端消费
"""
def to_sse(event: StreamEvent) -> str:
    # 这里我们将 event 对象转换为字典，并排除掉值为 None 的字段，以减少冗余数据
    # data = event.model_dump(exclude_none=True)
    # 将字典转换为 JSON 字符串，作为 SSE 的数据部分
    # json_data = json.dumps(data)

    # 由于不需要对字典进行进一步操作，可以选择一步到位地将 event 对象转换为 JSON 字符串
    json_data = event.model_dump_json(exclude_none=True)

    # SSE 格式要求每条消息以 "data: " 开头，并以两个换行符结尾
    return f"data: {json_data}\n\n"


"""
路由层的流式生成器
"""
async def stream_generator(req: ChatStreamRequest):
    # 调用服务层获取提供者实例
    try:
        # 获取提供者实例
        provider = chat_service.get_provider(req)

        # 调用提供者的 stream_chat 方法，获取一个异步生成器，逐步产出 StreamEvent
        async for event in provider.stream_chat(req):
            yield to_sse(event)

    # 捕获所有异常
    except Exception as e:
        error_event = StreamEvent(
            type="error",
            content=f"CHAT_STREAM_ERROR:",
            message=str(e)
        )
        yield error_event


@router.post("/completions/stream", response_model=ApiResponse[ChatStreamRequest])
async def chat(req: ChatStreamRequest):
    """
    流式聊天接口
    使用 StreamingResponse 返回 SSE 格式的流式数据
    """
    return StreamingResponse(
        stream_generator(req),
        media_type="text/event-stream"
    )

@router.post("/summary", response_model=ApiResponse[SummaryResponse])
async def summary(req: SummaryRequest):
    # 摘要接口，接收 SummaryRequest，调用服务层并返回结果
    result = await AIService.summary(req)
    return ApiResponse.success(data=result, message="总结成功")


@router.get("/health")
async def health():
    # 健康检查接口，返回统一响应格式
    return ApiResponse.success(
        data={"status": "ok", "service": "ai-service"},
        message="服务运行正常"
    )