from enum import Enum

class LLMProvider(str, Enum):
    QWEN = "qwen"
    WENXIN = "wenxin"
    GLM = "glm"
    DEEPSEEK = "deepseek"