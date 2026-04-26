import os

# 全局配置类，集中管理应用的环境变量配置import os
from dotenv import load_dotenv

# 加载环境变量文件（.env）中的配置
load_dotenv()


# 将字符串类型的环境变量转换为布尔值
# 支持的值："1", "true", "yes", "on"（不区分大小写）
def to_bool(value: str, default: bool = False) -> bool:
    if value is None:
        return default
    return value.strip().lower() in {"1", "true", "yes", "on"}


# 全局配置类，集中管理应用的环境变量配置
class Settings:
    # 应用基础配置
    app_name: str = os.getenv("APP_NAME", "Yuntan AI Service")  # 应用名称
    app_env: str = os.getenv("APP_ENV", "dev")  # 运行环境（dev/prod）
    app_host: str = os.getenv("APP_HOST", "0.0.0.0")  # 服务监听地址
    app_port: int = int(os.getenv("APP_PORT", "8000"))  # 服务监听端口

    # LLM 通用配置
    default_provider: str = os.getenv("DEFAULT_PROVIDER", "qwen")  # 默认 LLM 提供商
    mock_llm: bool = to_bool(os.getenv("MOCK_LLM", "true"), default=True)  # 是否启用 LLM 模拟模式

    # 通义千问（Qwen）配置
    qwen_api_key: str = os.getenv("QWEN_API_KEY", "")  # Qwen API 密钥
    qwen_base_url: str = os.getenv(  # Qwen API 基础 URL
        "QWEN_BASE_URL",
        "https://dashscope.aliyuncs.com/compatible-mode/v1"
    )
    qwen_model: str = os.getenv("QWEN_MODEL", "qwen-plus")  # Qwen 模型名称

    # 百度文心一言（Wenxin）配置
    wenxin_api_key: str = os.getenv("WENXIN_API_KEY", "")  # 文心 API 密钥
    wenxin_base_url: str = os.getenv("WENXIN_BASE_URL", "")  # 文心 API 基础 URL
    wenxin_model: str = os.getenv("WENXIN_MODEL", "ernie-speed-128k")  # 文心模型名称

    # 智谱 GLM 配置
    glm_api_key: str = os.getenv("GLM_API_KEY", "")  # GLM API 密钥
    glm_base_url: str = os.getenv("GLM_BASE_URL", "https://open.bigmodel.cn/api/paas/v4")  # GLM API 基础 URL
    glm_model: str = os.getenv("GLM_MODEL", "glm-4-flash")  # GLM 模型名称

    # DeepSeek 配置
    deepseek_api_key: str = os.getenv("DEEPSEEK_API_KEY", "")  # DeepSeek API 密钥
    deepseek_base_url: str = os.getenv("DEEPSEEK_BASE_URL", "https://api.deepseek.com")  # DeepSeek API 基础 URL
    deepseek_model: str = os.getenv("DEEPSEEK_MODEL", "deepseek-chat")  # DeepSeek 模型名称


# 创建全局配置实例，供其他模块导入使用
settings = Settings()