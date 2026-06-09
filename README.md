# 后勤物品借出管理系统 API

基于 Spring Boot 构建的后勤物品借还管理系统，支持物品建档、借出归还、维修流转、库存预警、异常识别、筛选查询、统计报表和登录认证。

## 技术栈

- Java 11
- Spring Boot 2.7.18
- Spring Data JPA
- Spring Security + JWT
- H2 Database（文件模式持久化）
- SpringDoc OpenAPI (Swagger UI)
- Lombok
- Docker / Docker Compose

## 功能特性

### 核心功能
- **物品管理**: 物品分类、存放位置、物品建档、库存管理
- **借用管理**: 借用申请、借出确认、归还登记、异常备注
- **维修管理**: 维修登记、维修处理、维修复核
- **库存预警**: 库存不足提醒、逾期未归还提醒
- **异常识别**: 逾期未还、维修未复核、连续异常归还
- **查询筛选**: 按分类、位置、借用人、状态、日期范围、异常类型筛选
- **统计报表**: 库存不足清单、逾期归还排行、维修待复核列表

### 安全认证
- JWT 令牌认证
- 角色权限控制（管理员/普通用户）

## 快速开始

### 使用 Docker 启动（推荐）

#### 方案一：默认方式（使用官方镜像）

```bash
docker compose up -d --build
```

#### 方案二：使用阿里云镜像（国内推荐，解决拉取失败问题）

如果遇到 Docker Hub 镜像拉取失败或速度慢的问题，使用阿里云镜像版本：

```bash
docker compose -f docker-compose.aliyun.yml up -d --build
```

#### 方案三：配置 Docker 镜像加速器

可以配置 Docker Desktop 使用国内镜像加速器：

1. 打开 Docker Desktop 设置
2. 找到 Docker Engine 配置
3. 添加以下配置：

```json
{
  "registry-mirrors": [
    "https://docker.mirrors.ustc.edu.cn",
    "https://hub-mirror.c.163.com",
    "https://registry.docker-cn.com"
  ]
}
```

4. 保存并重启 Docker

#### 访问服务

- API 地址: http://localhost:8011/api
- Swagger 文档: http://localhost:8011/api/swagger-ui.html
- H2 控制台: http://localhost:8011/api/h2-console
  - JDBC URL: `jdbc:h2:file:/data/item-lending-db
  - 用户名: `sa`
  - 密码: （空）

### 默认账号

- 管理员账号: `admin`
- 密码: `admin123`

## API 接口文档

启动服务后访问 Swagger UI 查看完整接口文档:
http://localhost:8011/api/swagger-ui.html

### 主要接口模块

| 模块 | 路径 | 说明 |
|------|------|------|
| 认证管理 | /api/auth | 用户登录 |
| 用户管理 | /api/users | 用户信息管理 |
| 物品分类 | /api/item-categories | 物品分类管理 |
| 存放位置 | /api/item-locations | 存放位置管理 |
| 物品管理 | /api/items | 物品信息管理 |
| 借用管理 | /api/borrow-records | 借用归还管理 |
| 维修管理 | /api/repair-records | 维修流程管理 |
| 预警管理 | /api/alerts | 预警消息管理 |
| 统计管理 | /api/statistics | 统计报表 |

## 端口说明

### 1. 登录获取 Token
```
POST /api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}
```

响应示例：
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "userId": 1,
    "username": "admin",
    "realName": "系统管理员",
    "role": "ADMIN"
  }
}
```

### 2. 使用 Token 访问受保护接口

在请求头中添加 Authorization：
```
Authorization: Bearer <token>
```

## 数据库持久化

H2 数据库文件通过 Docker volume 持久化，数据保存在 Docker volume `logistics-item-lending-data` 中。

查看 volume 数据 volume 名称:

```bash
# 查看 volume
docker volume ls

# 删除 volume
docker volume rm logistics-item-lending-data
```

## 本地开发

### 环境要求
- JDK 11+
- Maven 3.6+
- Maven 3.6+

### 本地运行

```bash
# 编译项目
mvn clean package -DskipTests

# 运行项目
mvn spring-boot:run
```

### 项目结构

```
src/main/java/com/logistics/itemlending
├── ItemLendingApplication.java    # 启动类
├── common/                          # 公共类
│   └── Result.java               # 统一响应结果
├── config/                          # 配置类
│   ├── SecurityConfig.java     # 安全配置
│   ├── SwaggerConfig.java      # Swagger 配置
│   └── DataInitializer.java       # 数据初始化
├── controller/                     # 控制器层
├── dto/                          # 数据传输对象
│   ├── request/
│   └── response/
├── entity/                      # 实体类
├── enums/                         # 枚举类
├── exception/                   # 异常处理
│   ├── BusinessException.java  # 业务异常
│   └── GlobalExceptionHandler.java # 全局异常处理
├── repository/                   # 数据访问层
├── security/                     # 安全相关
│   ├── JwtTokenUtil.java      # JWT 工具类
│   ├── JwtRequestFilter.java  # JWT 过滤器
│   └── JwtUserDetailsService.java # 用户详情服务
├── service/                        # 业务逻辑层
└── util/                           # 工具类
    └── CurrentUserUtil.java   # 当前用户工具
```

## 核心流程

### 物品借出流程
1. 管理员创建物品分类、存放位置
2. 管理员创建物品档案（设置分类、位置、数量、责任人等
3. 用户提交借用申请
4. 管理员确认借出
5. 用户归还物品
6. 管理员确认归还

### 物品维修流程
1. 用户提交维修申请
2. 维修人员开始维修
3. 维修完成后提交维修结果
4. 管理员复核维修结果

## 配置说明

主要配置项在 `application.yml` 中：

- `server.port`: 服务端口，默认 8011
- `jwt.secret`: JWT 密钥
- `jwt.expiration`: JWT 过期时间（毫秒），默认 24 小时
- `stock.warning.threshold`: 库存预警阈值
- `borrow.overdue.days`: 逾期天数，默认 7 天
