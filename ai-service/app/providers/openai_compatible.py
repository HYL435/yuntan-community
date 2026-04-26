import json
from asyncio import timeout
from typing import AsyncGenerator

import httpx

from app.providers.base import BaseChatProvider
from app.schemas.chat import ChatStreamRequest, StreamEvent

# 模块级说明：
# 本模块实现一个“OpenAI 兼容”的聊天提供者基类（抽象层），
# 负责构建兼容 OpenAI Chat Completions API 的请求载荷并以流式方式解析返回的数据。
# 教学提示：此处“兼容”表示遵循类似于 OpenAI 的接口规范（如 messages、model、stream 等字段），
# 但具体的 base_url、api_key 和 default_model 由子类提供（例如不同厂商的兼容层）。

class OpenAICompatibleProvider(BaseChatProvider):
    """
    OpenAI兼容接口的公共基类。
    只负责通用流式请求和解析，不代表某个具体厂商。

    关键点教学：
    - 继承 BaseChatProvider：表明这是一个“提供者”实现，负责和外部 LLM 服务交互。
    - 类属性 provider_name/default_model/base_url/api_key 在具体子类中被赋值。
    """

    provider_name: str = ""
    default_model: str = ""
    base_url: str = ""
    api_key: str = ""

    def build_messages(self, req: ChatStreamRequest) -> list[dict]:
        """
        将请求中的 message 转换为 OpenAI 风格的 messages 列表。

        参数：
        - req: ChatStreamRequest（类型注解表示期望该对象具有 message 等字段）
          teaching: 类型注解只是静态提示，运行时 Python 不会强制类型，但有助于 IDE 和检查工具。

        返回：
        - list[dict]：每个字典代表一条消息，包含 role/content 字段（OpenAI 风格）。
        """
        # 这里没有复杂的系统/assistant 角色处理，仅示例化为单条 user 消息。
        return [
            {
                "role": "user",
                "content": req.message
            }
        ]
 
    def build_payload(self, req: ChatStreamRequest) -> dict:
        """
        构建发送给 LLM 服务的 JSON 载荷（payload）。

        设计原因：
        - 将 model、messages、stream 明确写出，便于兼容 OpenAI API。
        - 使用 req.model 或 fallback default_model 以支持默认模型配置。
        """
        return {
            "model": req.model or self.default_model,
            "messages": self.build_messages(req),
            "stream": True  # 开启流式返回，服务器会以逐步增量（delta）发送响应
        }

    def build_headers(self) -> dict:
        """
        构建 HTTP 请求头。

        解释：
        - Authorization: 使用 Bearer token，这是 OpenAI 风格常见的认证方法。
        - Content-Type: 明确指定 application/json 表示请求体是 JSON。
        """
        return {
            "Authorization": f"Bearer {self.api_key}",
            "Content-Type": "application/json"
        }

    async def stream_chat(
        self,
        req: ChatStreamRequest
    ) -> AsyncGenerator[StreamEvent, None]:
        """
        异步生成器：以流式方式发送请求并逐步产出 StreamEvent。

        关键教学点：
        - async def 表示这是一个协程函数（异步函数），调用时需要 await 或在事件循环中运行。
        - 返回类型 AsyncGenerator[StreamEvent, None] 表示这是一个异步生成器；
          调用方可以异步遍历它来接收一系列 StreamEvent（start、delta、done、error 等）。
        - 使用异步生成器的原因：能够在接收服务端逐行数据（流）时，边读取边返回给上游，而不是等待全部完成。
        """

        # 解析使用的模型名称（优先使用请求中的 model）
        model_name = req.model or self.default_model

        # 首先发出一个 "start" 事件，通知调用方流已开始并带上提供者与模型信息
        # 这里使用 yield（在异步函数中使用时称为异步生成器的 yield）
        # teaching: 在异步生成器内部，yield 用于产出值给调用者；调用者用 "async for" 来获取这些值。
        yield StreamEvent(
            type="start",
            provider=self.provider_name,
            model=model_name
        )

        # 构建请求 URL、载荷和头
        url = f"{self.base_url}/chat/completions"
        payload = self.build_payload(req)
        headers = self.build_headers()

        # 构建超时设置
        # 连接超时（connect）10秒，读取超时（read）60秒，写入超时（write）10秒，连接池超时（pool）5秒
        timeout = httpx.Timeout(connect=10.0, read=60.0, write=10.0, pool=5.0)

        # 使用 httpx 的异步客户端进行网络请求
        # teaching: async with 用法类似于 with，但可用于异步上下文管理器，保证资源正确释放（如连接关闭）
        async with httpx.AsyncClient(timeout=timeout) as client:
            # client.stream 用于在响应体较大或支持流式返回时逐步读取数据流，
            # 避免一次性将整个响应加载到内存中，提高效率并支持实时性。
            async with client.stream(
                "POST",
                url,
                headers=headers,
                json=payload
            ) as response:

                # 如果状态码表示错误（>=400），读取响应体并产出 error 事件
                if response.status_code >= 400:
                    # response.aread() 是异步读取整个响应体的二进制内容
                    text = await response.aread()
                    # 解码时使用 errors="ignore" 以避免异常中断（教学：更严谨的处理可根据需求改进）
                    yield StreamEvent(
                        type="error",
                        code=f"{self.provider_name.upper()}_HTTP_ERROR",
                        message=text.decode("utf-8", errors="ignore")
                    )
                    return  # 发生错误则结束生成器

                # response.aiter_lines() 是一个异步迭代器，会逐行产出服务器发送的数据（常见于 SSE 或类似 OpenAI 的流）
                # teaching: async for 表示在异步迭代器上逐步迭代并且可以在每一步使用 await
                async for line in response.aiter_lines():
                    # 有时服务器可能发送空行，跳过它们
                    if not line:
                        continue

                    # OpenAI 风格的流通常以 "data: " 前缀开头，移除该前缀以得到纯 JSON 字符串
                    if line.startswith("data: "):
                        line = line[6:]

                    # 特殊标记表示流结束
                    if line == "[DONE]":
                        break

                    try:
                        # 将该行解析为 JSON 对象
                        data = json.loads(line)
                    except Exception:
                        # 教学：解析可能失败（例如服务器发送非 JSON 行），此处选择忽略该行并继续循环
                        continue

                    # data 中通常包含 choices 字段（OpenAI 风格），取第一个 choice
                    choices = data.get("choices") or []
                    if not choices:
                        # 没有 choices 则跳过
                        continue

                    choice = choices[0]
                    # delta 字段是增量内容（流式传输中常见）
                    delta = choice.get("delta") or {}

                    # 若 delta 中包含 content，则这是增量文本，产出一个 delta 事件
                    content = delta.get("content")
                    if content:
                        yield StreamEvent(
                            type="delta",
                            content=content
                        )

                    # finish_reason 表示该 choice 是否完成以及完成原因（如 stop/length 等）
                    finish_reason = choice.get("finish_reason")
                    if finish_reason:
                        # 若有 finish_reason，则产出 done 事件并结束生成器
                        yield StreamEvent(
                            type="done",
                            finish_reason=finish_reason
                        )
                        return  # 结束整个函数，资源会被 async with 正确释放

        # 如果循环正常结束（非异常），为了保证调用方能收到 done 事件，这里再产出一次 done。
        # finish_reason 设为 "stop" 表示正常停止。
        yield StreamEvent(
            type="done",
            finish_reason="stop"
        )