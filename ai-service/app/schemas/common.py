# 导入类型和基类
# Optional 用于表示一个值可以是指定类型或 None，TypeVar 用于定义泛型类型变量，Generic 用于创建泛型类，
# BaseModel 是 pydantic 的基础模型类，GenericModel 是 pydantic 提供的支持泛型的模型基类。
from typing import Generic, Optional, TypeVar
from pydantic import BaseModel
from pydantic.generics import GenericModel

# 定义泛型类型变量 T，用于在 ApiResponse 中指定 data 的类型
# 通过 TypeVar 定义一个类型变量 T，使得 ApiResponse 可以在不同场景下使用不同类型的数据，例如 ApiResponse[User] 或 ApiResponse[List[Item]]。
# pydantic 的 GenericModel 可以自动处理泛型类型参数，并生成相应的类型提示。
T = TypeVar("T")

# GenericModel 继承自 BaseModel，用于定义通用的 API 响应模型，支持泛型类型参数 T，使得 data 字段可以根据具体使用场景灵活指定类型。
class ApiResponse(GenericModel, Generic[T]):
    """
    通用 API 响应模型（泛型）：
    - code: HTTP 风格的状态码或业务码，默认 200 表示成功
    - message: 对请求结果的简短描述
    - data: 可选的响应数据，类型由泛型 T 指定
    使用 GenericModel 使得 pydantic 在运行时能够校验不同类型的 data。
    """

    # 响应状态码，建议使用 HTTP 风格码（如 200、400、500）或自定义业务码
    code: int
    # 响应描述信息，简洁明了，供前端或调用方展示或日志记录
    message: str
    # 可选的响应主体，通常为字典、列表或自定义 pydantic 模型
    data: Optional[T] = None

    # cls 是类方法，用于创建一个成功的响应实例，cls 代表 ApiResponse 类本身，允许我们在方法内部调用类构造器来创建实例。
    @classmethod
    def success(cls, data: Optional[T] = None, message: str = "success"):
        """
        构造一个成功响应的快捷方法。

        参数：
        - data: 可选的响应数据，类型为泛型 T
        - message: 成功消息，默认为 "success"

        返回：
        - ApiResponse 实例，code=200
        """
        return cls(code=200, message=message, data=data)

    @classmethod
    def fail(cls, message: str = "fail", code: int = 500, data: Optional[T] = None):
        """
        构造一个失败响应的快捷方法。

        参数：
        - message: 错误描述，默认为 "fail"
        - code: 错误码，默认为 500（可替换为具体业务码或 HTTP 状态码）
        - data: 可选的额外错误数据（例如错误详情、验证信息等）

        返回：
        - ApiResponse 实例，包含指定的错误码与信息
        """
        return cls(code=code, message=message, data=data)