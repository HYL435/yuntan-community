# 导入APIRouter类和AI模块的路由
from fastapi import APIRouter
from app.api.v1.ai import router as ai_router


# 创建APIRouter实例，并设置前缀为/api
api_router = APIRouter(prefix="/api")
# 将AI模块的路由包含到主路由中
api_router.include_router(ai_router)