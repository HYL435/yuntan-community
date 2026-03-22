# ai-service/Dockerfile.dev
FROM python:3.11-slim

WORKDIR /app

# 安装依赖
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt \
    && pip install --no-cache-dir watchdog  # watchdog 用于文件变化监控

# 拷贝源码（挂载时会覆盖）
COPY . .

# 暴露端口
EXPOSE 5000

# 开发模式启动：watchdog 监控文件变化，自动重启
CMD ["watchmedo", "auto-restart", "--pattern=*.py", "--recursive", "python", "main.py"]