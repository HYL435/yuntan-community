# python
from typing import Any, cast, Optional  # 引入类型提示：Any（任意类型）、cast（类型转换）和 Optional（可为 None 的类型）  # 导入类型工具
from app.core.config import settings  # 从项目配置模块导入 settings 对象，包含 llm_api_key 等配置  # 导入项目设置

# 尝试导入 openai 包中的 AsyncOpenAI（适用于 openai v2 的异步客户端）  # 尝试使用官方异步客户端
try:
    from openai import AsyncOpenAI  # 如果安装了 openai 包，则从中导入 AsyncOpenAI 类  # 导入第三方库
except ImportError:
    AsyncOpenAI = None  # 若未安装 openai，则将 AsyncOpenAI 设为 None，代码后续会判断并使用 mock 或抛错  # 保护性处理导入失败

class LLMClient:  # 定义 LLMClient 类，用于封装与大模型的交互  # 类定义开始
    _client: Optional[Any] = None  # 类变量：缓存单例客户端实例，初始为 None（表示尚未创建）  # 单例缓存

    @classmethod  # 标记下面的方法为类方法，调用时第一个参数为类本身（cls）  # 装饰器说明
    def get_client(cls) -> Optional[Any]:  # 获取或创建客户端，返回值可能是客户端实例或 None  # 方法签名与返回类型说明
        # 如果缓存中没有客户端实例并且 AsyncOpenAI 可用，则创建一个新的异步客户端  # 逻辑说明
        if cls._client is None and AsyncOpenAI is not None:  # 检查是否需要创建客户端并且导入成功  # 条件判断
            cls._client = AsyncOpenAI(  # 使用 AsyncOpenAI 创建客户端实例并赋值给类变量 _client  # 创建客户端
                api_key=settings.llm_api_key,  # 从配置中读取 llm_api_key 并传给客户端作为 API Key  # API Key 传入
                base_url=settings.llm_base_url  # 从配置中读取 llm_base_url 并传给客户端作为基础 URL（可选）  # 基础地址传入
            )  # 客户端实例创建结束  # 实例化完成
        return cls._client  # 返回已缓存或新创建的客户端实例，若不可用则返回 None  # 返回客户端或 None

    @classmethod  # 标记下面的方法为类方法，支持直接用类名调用，例如 LLMClient.chat(...)  # 装饰器说明
    async def chat(cls, prompt: str) -> str:  # 异步方法：接受一个字符串 prompt，返回模型回复的字符串  # 方法签名说明
        # 开发/测试模式判断：如果在配置中启用了 mock_llm，则直接返回模拟回复，避免真实网络请求  # mock 逻辑说明
        if settings.mock_llm:  # 从配置读取 mock_llm 布尔值  # 条件判断
            return f"【模拟回复】你发送的内容是：{prompt}"  # 直接返回包含用户 prompt 的模拟文本，便于本地测试  # 返回模拟结果

        # 尝试获取真实的客户端实例（可能为 None，如果 openai 未安装则为 None）  # 获取客户端说明
        client = cls.get_client()  # 调用类方法 get_client 获取客户端实例  # 调用单例获取方法
        if client is None:  # 如果仍然没有客户端实例，说明 openai 包不可用  # 错误判断
            # 抛出运行时错误，提示用户启用 mock 或安装 openai 包  # 抛出明确错误供调用方处理
            raise RuntimeError("openai package 未安装或不可用，无法调用真实模型，请设置 MOCK_LLM=true 或 安装 openai 包")

        # 准备要发送给模型的 messages 列表，包含 system（系统指令）和 user（用户输入）两条消息  # messages 构建说明
        messages = cast(Any, [  # 使用 cast 将列表显式标记为 Any 类型，避免类型检查器对 SDK 类型不兼容的警告  # 类型转换说明
            {"role": "system", "content": "你是云潭社区的AI助手，请使用自然、准确、简洁的中文回答。"},  # 指定系统角色和行为准则  # 系统消息
            {"role": "user", "content": prompt}  # 将用户输入放入 messages 列表中，作为对话的 user 消息  # 用户消息
        ])  # messages 列表结束  # 列表构建完成

        # 使用 openai 客户端的 chat 接口创建一个 completion（具体方法名依赖 openai SDK 版本）  # 调用模型说明
        completion = await client.chat.completions.create(  # 异步调用 SDK 的创建接口以获取模型回复  # 异步请求
            model=settings.llm_model,  # 指定要使用的模型名称，从配置读取  # 模型选择
            messages=messages,  # 将刚构建的 messages 列表传给模型  # 传入消息
            temperature=0.7,  # 设置温度参数（控制生成文本的随机性），常见范围 0.0 - 1.0  # 生成参数
        )  # 调用结束，得到 completion 对象  # API 返回完成

        # 从 completion 对象中取出第一个候选回复的文本并返回，如果为空则返回空字符串以避免返回 None  # 解析并返回结果
        return completion.choices[0].message.content or ""  # 返回文本内容或空字符串  # 返回最终回复

