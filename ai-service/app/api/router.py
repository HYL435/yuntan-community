# 导入 APIRouter 用于创建路由分组
from fastapi import APIRouter
# 从子模块导入 AI 路由并重命名为 ai_router
from app.api.v1.ai import router as ai_router


# 创建主路由实例，并设置公共前缀为 /api
api_router = APIRouter(prefix="/api")
# 将 ai 模块的路由包含（挂载）到主路由下
api_router.include_router(ai_router)  # 现在 AI 路由会在 /api/v1/ai 下生效
