

# 云面试系统 (ZiCai Cloud Interview)

一个基于微服务架构的智能云面试平台，提供AI辅助面试、账号管理、产品展示等功能。

## 项目架构

```
zicai_cloud_interview
├── account-service    # 账号服务 - 用户注册/登录/认证
├── ai-service         # AI服务 - 智能面试功能
├── product-service    # 产品服务 - Banner/套餐管理
├── common             # 公共模块 - 工具类/配置
└── gateway            # API网关
```

## 技术栈

- **后端框架**: Spring Boot
- **数据库**: MySQL + MyBatis Plus
- **缓存**: Redis
- **对象存储**: 阿里云 OSS
- **服务注册/发现**: Nacos
- **HTTP客户端**: Feign
- **认证**: JWT
- **短信服务**: SMS API

## 功能模块

### 1. 账号服务 (account-service)

提供用户账号管理核心功能：

- **图形验证码**: 防止恶意请求
- **短信验证码**: 手机号验证登录
- **账号登录**: JWT Token认证
- **个人信息**: 查询和修改
- **头像上传**: 支持公共/私有存储

### 2. AI服务 (ai-service)

智能面试核心模块：

- **面试管理**: 面试记录和轮次管理
- **AI对话**: 智能问答交互
- **简历管理**: 简历上传和解析
- **题库管理**: 面试题目管理

### 3. 产品服务 (product-service)

运营推广模块：

- **Banner管理**: 首页轮播图
- **套餐管理**: 产品套餐配置
- **权益管理**: 用户权益配置
- **订单管理**: 购买订单

### 4. 公共模块 (common)

共用组件：

- **文件服务**: OSS文件上传/下载
- **短信服务**: 验证码发送
- **工具类**: JWT/JSON/验证码等
- **配置管理**: Redis/OSS/短信配置

## 快速开始

### 环境要求

- JDK 1.8+
- Maven 3.6+
- Redis
- MySQL
- Nacos (可选)

### 配置说明

复制环境配置模板：

```bash
cp account-service/src/main/resources/env.example account-service/src/main/resources/application-dev.yml
```

配置项说明：

```yaml
# Redis配置
spring:
  redis:
    host: localhost
    port: 6379

# 数据库配置
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/zicai_cloud
    username: root
    password: your_password

# 阿里云OSS配置
aliyun:
  oss:
    access-key-id: your_access_key_id
    access-key-secret: your_access_key_secret
    secure-bucket-name: your_bucket

# 短信配置
sms:
  host: api.example.com
  path: /api/send
  appCode: your_app_code
```

### 构建运行

```bash
# 编译项目
mvn clean package -DskipTests

# 启动账号服务
cd account-service
java -jar target/account-service.jar

# 启动AI服务
cd ai-service
java -jar target/ai-service.jar

# 启动产品服务
cd product-service
java -jar target/product-service.jar
```

## API 接口

### 账号服务接口

| 接口 | 方法 | 描述 |
|------|------|------|
| `/api/v1/account/captcha` | GET | 获取图形验证码 |
| `/api/v1/account/send_check_code` | POST | 发送短信验证码 |
| `/api/v1/account/login` | POST | 账号登录 |
| `/api/v1/account/detail` | GET | 获取个人信息 |
| `/api/v1/account/avatar` | POST | 上传头像 |

### 产品服务接口

| 接口 | 方法 | 描述 |
|------|------|------|
| `/api/banner/v1/detail` | GET | Banner详情 |
| `/api/banner/v1/list` | GET | Banner列表 |

## 项目结构

```
src/main/java/net/zicai/
├── controller/      # REST控制器
├── service/        # 业务逻辑
├── mapper/         # 数据访问层
├── model/          # 数据模型
├── dto/            # 数据传输对象
├── config/         # 配置类
├── util/            # 工具类
├── interceptor/    # 请求拦截器
├── enums/          # 枚举类
└── exception/     # 异常处理
```

## 数据库表

- `account` - 用户表
- `account_benefit` - 用户权益表
- `benefit_task` - 权益任务表
- `benefit_usage_log` - 权益使用日志
- `interview` - 面试记录
- `interview_round` - 面试轮次
- `question` - 题目库
- `resume` - 简历表
- `ai_chat_record` - AI对话记录
- `banner` - Banner表
- `product_package` - 套餐表
- `benefit` - 权益表
- `package_benefit` - 套餐权益关联
- `product_order` - 订单表

## 许可证

本项目仅供学习参考使用。