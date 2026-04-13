from fastapi import FastAPI
from app.api.router import api_router # 导入路由模块

# 创建 FastAPI 应用实例
app = FastAPI(
    title="Yuntan AI Service",
    version="1.0.0",
    description="云坛 AI 服务"
)


# 注册路由
app.include_router(api_router)
