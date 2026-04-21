"""
模块说明：
该模块提供 LLMFactory，用于根据 provider 字符串返回对应的 LLM 客户端实例。
目的：统一构造不同后端（qwen/wenxin/glm/deepseek）的客户端，便于在应用中动态选择模型提供者。
"""

# 引入各提供者的客户端实现（实现类均实现了 BaseLLMClient 接口）
from app.clients.qwen_client import QwenClient  # 通义提供者客户端
from app.clients.glm_client import GLMClient  # GLM 提供者客户端
from app.clients.deepseek_client import DeepSeekClient  # DeepSeek 提供者客户端
from app.clients.wenxin_client import WenxinClient  # 文心提供者客户端
from app.clients.base import BaseLLMClient
from app.core.enums import LLMProvider  # provider 枚举，推荐使用其 value 进行比较


class LLMFactory:
    """
    LLM 客户端工厂类。

    说明：
    - 使用静态方法 get_client(provider: str) 根据 provider 字符串返回对应的 BaseLLMClient 实例。
    - provider 建议传入 LLMProvider 枚举的 value（例如 LLMProvider.QWEN.value），以保持统一。
    - 若传入不支持的 provider，会抛出 ValueError，调用方应捕获并处理（或在调用前校验 provider）。
    """

    @staticmethod
    def get_client(provider: str) -> BaseLLMClient:
        """
        根据 provider 返回对应的 LLM 客户端实例。

        参数：
        - provider: 提供者标识字符串（例如 "qwen", "wenxin", "glm", "deepseek"）

        返回：
        - 对应的 BaseLLMClient 子类实例

        异常：
        - 当 provider 不在支持列表中时，抛出 ValueError
        """
        # 如果是通义（qwen）
        if provider == LLMProvider.QWEN.value:
            return QwenClient()
        # 如果是文心（wenxin）
        if provider == LLMProvider.WENXIN.value:
            return WenxinClient()
        # 如果是 GLM
        if provider == LLMProvider.GLM.value:
            return GLMClient()
        # 如果是 DeepSeek
        if provider == LLMProvider.DEEPSEEK.value:
            return DeepSeekClient()

        # 未知 provider，明确抛错以便上层处理
        raise ValueError(f"不支持的 provider: {provider}")