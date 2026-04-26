from pydantic import BaseModel, Field
from typing import Optional

# 聊天请求模型
class ChatStreamRequest(BaseModel):
    """
    ChatRequest - 聊天接口的请求结构

    字段说明：
    - message: 用户发送的消息内容，必须非空，长度限制 1-4000 字符（用于防止超长输入）
    - user_id: 可选，调用方传入的用户 ID（用于鉴权或记录）
    - session_id: 可选，会话 ID，用于在多轮对话中保持上下文
    """
    # 用户消息（必填，长度限制）
    message: str = Field(..., min_length=1, max_length=4000, description="用户消息，1-4000 字符")
    # 用户 ID（可选）
    user_id: Optional[int] = Field(default=None, description="用户ID，可选，用于记录或鉴权")
    # 会话 ID（可选，用于多轮对话）
    session_id: Optional[int] = Field(default=None, description="会话ID，可选，用于多轮上下文关联")
    # 模型提供商（可选，默认为通义千问）
    provider: Optional[str] = Field(default="qwen", description="模型提供商")
    # 模型名称
    model: Optional[str] = Field(default="qwen", description="模型名称")

# 流式事件模型，用于在聊天过程中逐步返回生成的内容和相关信息
class StreamEvent(BaseModel):
    """
    StreamEvent - 流式事件模型

    字段说明：
    - type: 事件类型，用于标识当前事件的类型，如 "chunk" 表示一个分块的回复内容
    - content: 分块的回复内容
    - provider: 模型提供者名称
    - model: 模型名称
    - prompt_tokens: 提示词的token数量
    - completion_tokens: 完成词的token数量
    - total_tokens: 总token数量
    - finish_reason: 完成原因
    - code: 错误码
    - message: 错误信息
    """
    type: str
    content: Optional[str] = None
    provider: Optional[str] = Field(default="qwen", description="模型提供商")
    model: Optional[str] = Field(default="qwen", description="模型名称")
    prompt_tokens: Optional[int] = Field(default=None, description="提示词的token数量")
    completion_tokens: Optional[int] = Field(default=None, description="完成词的token数量")
    total_tokens: Optional[int] = Field(default=None, description="总token数量")
    finish_reason: Optional[str] = Field(default=None, description="完成原因")
    code: Optional[str] = Field(default=None, description="错误码")
    message: Optional[str] = Field(default=None, description="错误信息")



# 聊天响应模型
class ChatResponse(BaseModel):
    """
    ChatResponse - 聊天接口的响应结构

    字段说明：
    - reply: 模型生成的回复文本
    - model: 用于生成回复的模型名称或版本（便于追踪或展示）
    """
    # 提供者名称（例如 "qwen", "wenxin" 等）
    provider: str = Field(..., description="模型提供者名称")
    # 模型回复文本
    reply: str = Field(..., description="AI回复内容")
    # 生成回复所使用的模型名称
    model: str = Field(..., description="模型名称")


# 摘要请求模型
class SummaryRequest(BaseModel):
    """
    SummaryRequest - 摘要接口的请求结构

    字段说明：
    - content: 需要生成摘要的原始文本内容，必须非空，长度上限 20000 字符（防止超大请求）
    - user_id: 可选，调用方传入的用户 ID（便于记录或个性化）
    """
    # 需要摘要的原文内容（必填，长度限制）
    content: str = Field(..., min_length=1, max_length=20000, description="文章内容，1-20000 字符")
    # 用户 ID（可选）
    user_id: Optional[int] = Field(default=None, description="用户ID，可选，用于记录或鉴权")


# 摘要响应模型
class SummaryResponse(BaseModel):
    """
    SummaryResponse - 摘要接口的响应结构

    字段说明：
    - summary: 生成的摘要文本
    - model: 用于生成摘要的模型名称或版本
    """
    # 生成的摘要文本
    summary: str = Field(..., description="总结内容")
    # 生成摘要所用的模型名称
    model: str = Field(..., description="模型名称")