"""
定义所有模型适配器的统一接口，所有模型都必须继承这个基类，并实现其中的抽象方法。
"""

# ABC Python的抽象基类模块，提供了一个基础类和装饰器，用于定义抽象基类和抽象方法。
from abc import ABC, abstractmethod
# typing模块提供了类型提示功能，可以帮助开发者更好地理解代码的输入输出类型，提高代码的可读性和可维护性。
# AsyncGenerator 是一个类型提示，表示一个异步生成器函数的返回类型。
# 异步生成器函数是一种特殊的函数，可以使用async def定义，并且可以使用yield语句来生成值，同时支持异步操作。
from typing import AsyncGenerator

from app.schemas.chat import ChatStreamRequest, StreamEvent


class BaseChatProvider(ABC):
    """
    定义所有模型适配器的统一接口，所有模型都必须继承这个基类，并实现其中的抽象方法。
    """

    @abstractmethod
    async def stream_chat(self, req: ChatStreamRequest) -> AsyncGenerator[StreamEvent, None]:
        """
        流式对话接口，接收 ChatStreamRequest，返回 AsyncGenerator[StreamEvent, None]
        其中 StreamEvent 包含了模型生成的回复内容、相关信息以及可能的错误信息。

        流式聊天接口

        输入：
        - req: ChatStreamRequest - 包含用户消息、用户ID、会话ID等信息的请求对象

        输出：
        - AsyncGenerator[StreamEvent, None] - 一个异步生成器，逐步返回 StreamEvent 对象，包含生成的回复内容、相关信息以及可能的错误信息
        """
        pass