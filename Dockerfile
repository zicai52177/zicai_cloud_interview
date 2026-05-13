# ============= Stage 1: Build =============
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /build

# 先复制 pom.xml 利用 Docker 层缓存
COPY pom.xml .
COPY common/pom.xml common/
COPY account-service/pom.xml account-service/
COPY ai-service/pom.xml ai-service/
COPY product-service/pom.xml product-service/
COPY gateway/pom.xml gateway/
COPY mcp-server/pom.xml mcp-server/

# 下载依赖（利用缓存）
RUN mvn dependency:go-offline -B -q 2>/dev/null || true

# 复制源码并构建
COPY . .
RUN mvn package -DskipTests -q

# ============= Stage 2: Runtime =============
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# 安装字体（PDF解析需要）
RUN apk add --no-cache fontconfig ttf-dejavu

ARG SERVICE_NAME
ARG SERVICE_PORT

COPY --from=builder /build/${SERVICE_NAME}/target/*.jar app.jar

ENV JAVA_OPTS="-Xms256m -Xmx512m -Djava.security.egd=file:/dev/./urandom"
ENV SERVER_PORT=${SERVICE_PORT}

EXPOSE ${SERVICE_PORT}

ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar app.jar"]
