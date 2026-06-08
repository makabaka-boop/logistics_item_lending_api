# logistics_item_lending_api

基于 Spring Boot 构建的后勤物品借还管理 API，支持物品建档、借出归还、维修流转、库存预警、异常识别、筛选查询、统计报表和登录认证。

## 技术栈

- Java 17 + Spring Boot 3.2
- Spring Data JPA + H2（文件模式持久化，通过 docker volume 挂载 `/data`）
- Spring Security（Bearer Token 认证）
- springdoc-openapi（Swagger UI 接口文档）

## 启动方式（Docker）

本地无需 Java 环境，直接使用 docker compose：

```bash
docker compose up -d --build
```

服务监听本机 `8011` 端口，数据库文件持久化在 `./data` 目录。

## 接口文档

- Swagger UI：http://localhost:8011/swagger-ui.html
- OpenAPI JSON：http://localhost:8011/v3/api-docs
- H2 控制台：http://localhost:8011/h2-console （JDBC URL：`jdbc:h2:file:/data/logistics`，账号 `sa`/`sa`）

## 登录认证

默认账号：

- 用户名：`admin`
- 密码：`admin123`

```bash
# 1. 登录获取 token
curl -X POST http://localhost:8011/api/auth/login \
     -H "Content-Type: application/json" \
     -d '{"username":"admin","password":"admin123"}'

# 2. 后续接口在请求头携带：
#    Authorization: Bearer logistics-token-2026
```

> Postman 中可在 Authorization 选择 `Bearer Token` 并填入 `logistics-token-2026`。

## 接口总览

| 模块 | 路径前缀 | 说明 |
| ---- | ---- | ---- |
| 认证 | `/api/auth` | 登录、查询当前用户 |
| 分类 | `/api/categories` | 物品分类维护 |
| 位置 | `/api/locations` | 存放位置维护 |
| 物品 | `/api/items` | 物品建档/可借数量/责任人/筛选 |
| 借用 | `/api/lendings` | 申请、借出、归还、异常确认、补充申请、逾期扫描 |
| 维修 | `/api/repairs` | 维修登记/开始/完成/复核 |
| 统计 | `/api/stats` | 库存不足、逾期排行、维修待复核、概览 |

## 核心规则

- **库存拦截**：借出确认时若 `availableQuantity` 不足，直接返回 409 错误。
- **逾期识别**：调用 `/api/lendings/scan-overdue` 扫描；归还时也会自动判定。
- **异常归还**：损坏 → 自动转入维修中；丢失 → 自动核减总数量。
- **连续异常**：同一借用人最近 N 次（默认 3）连续异常归还，自动打上 `REPEATED_ABNORMAL` 标记。
- **维修复核**：维修完成后状态 `DONE_PENDING_REVIEW`，需要调用 `/api/repairs/{id}/review` 复核才能回库。
- **库存预警**：`availableQuantity / totalQuantity <= 0.2` 时进入预警。

## 目录结构

```
src/main/java/com/gsb/logistics
├── LogisticsApplication.java
├── config/        # AppProperties / OpenApi
├── domain/        # 实体与枚举
├── repository/    # JPA Repository
├── security/      # Bearer Token 鉴权
├── service/       # 业务逻辑
└── web/           # Controller、统一返回、异常处理
```
