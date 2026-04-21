# 自定义异常类
class AIServiceException(Exception):
    """AI 服务调用异常"""

    def __init__(self, message: str):
        self.message = message
        super().__init__(message)