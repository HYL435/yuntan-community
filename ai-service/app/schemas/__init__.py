"""
* 定义请求和响应的数据模型
模块说明：
本模块提供统一的 API 响应模型 ApiResponse，基于 pydantic 的 GenericModel 实现泛型响应体。
适用于所有返回 JSON 的接口，封装 code/message/data 三个常用字段。
"""