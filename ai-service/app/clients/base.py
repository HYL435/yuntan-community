# ABC, abstractmethod 用于定义抽象基类和抽象方法，确保所有子类都实现这些方法。
from abc import ABC, abstractmethod


class BaseLLMClient(ABC):
    """
    通用 LLM 客户端基类（抽象）：
    - 规范了必须实现的接口：异步 chat 方法，以及 provider/model 属性。
    - 实现者负责将具体后端的请求/响应适配到这三个接口上。
    """

    @abstractmethod
    async def chat(self, message: str) -> str:
        """
        异步聊天接口（必须实现）。

        参数：
        - message: 用户输入的文本消息

        返回：
        - str: 模型生成的回复文本

        实现者责任：
        - 将 message 发送到对应后端（可能包含鉴权、请求构造、超时和重试等逻辑）
        - 处理后端响应并返回最终的文本回复（如需返回复杂结构，请在实现类中扩展方法）
        """
        pass

    @property
    @abstractmethod
    def provider(self) -> str:
        """
        提供者标识（必须实现）。

        返回：
        - str: 标识当前客户端所属的模型提供者，例如 "qwen", "wenxin", "glm" 等。
        用途：运行时选择、日志记录或上报时用于区分不同后端。
        """
        pass

    @property
    @abstractmethod
    def model(self) -> str:
        """
        模型名称或版本（必须实现）。

        返回：
        - str: 当前客户端使用的模型名称或版本，例如 "qwen-plus", "ernie-speed-128k" 等。
        用途：用于追踪、调试或在响应中返回模型信息。
        """
        pass