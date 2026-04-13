from pydantic import BaseModel, Field
from typing import Optional


class ChatRequest(BaseModel):
    message: str = Field(..., min_length=1, max_length=4000, description="用户消息")
    user_id: Optional[int] = Field(default=None, description="用户ID")
    session_id: Optional[str] = Field(default=None, description="会话ID")


class ChatResponse(BaseModel):
    reply: str = Field(..., description="AI回复内容")
    model: str = Field(..., description="模型名称")


class SummaryRequest(BaseModel):
    content: str = Field(..., min_length=1, max_length=20000, description="文章内容")
    user_id: Optional[int] = Field(default=None, description="用户ID")


class SummaryResponse(BaseModel):
    summary: str = Field(..., description="总结内容")
    model: str = Field(..., description="模型名称")