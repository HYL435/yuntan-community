# backend/yuntan-monolith-app/Dockerfile.dev
FROM maven:3.9.12-eclipse-temurin-21 AS builder

WORKDIR /app

# 拷贝源码和 pom.xml
COPY pom.xml .
COPY src ./src

# 安装依赖（缓存优化）
RUN mvn dependency:go-offline

# 开发模式启动命令
CMD ["mvn", "spring-boot:run", "-Dspring-boot.run.profiles=dev"]