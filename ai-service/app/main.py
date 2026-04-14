from fastapi import FastAPI  # 从 fastapi 导入 FastAPI 应用类
from app.api.router import api_router  # 导入在 app.api.router 中定义的主路由对象

# 创建 FastAPI 应用实例，并设置基本元信息
app = FastAPI(
    title="Yuntan AI Service",  # 应用的标题
    version="1.0.0",  # 应用的版本号
    description="云坛 AI 服务"  # 应用的描述信息
)


# 注册主路由，将 api_router 包含进来，使所有路由生效
app.include_router(api_router)  # 将 api_router 注册到 FastAPI 应用上，挂载到默认路径（api_router 内含前缀）
