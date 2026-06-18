# 第三方工业设备维保工单资产台账系统

## 项目简介

本系统是一款面向第三方工业设备维保服务企业的资产台账与工单管理平台，提供设备档案管理、维保工单调度、备件库存管理、客户管理、维保计划等核心功能，助力企业实现维保业务数字化管理。

## 技术栈

### 前端
- **框架**: Vue 3.4 + Vue Router 4
- **UI 组件库**: Element Plus 2.7
- **HTTP 客户端**: Axios 1.7
- **构建工具**: Vite 5.4
- **Web 服务器**: Nginx (Docker 部署)

### 后端
- **框架**: Spring Boot 3.3.0
- **JDK 版本**: 17
- **ORM**: Spring Data JPA
- **缓存**: Spring Data Redis
- **数据库**: MySQL 8.0
- **构建工具**: Maven 3.9
- **数据库迁移**: Spring SQL 初始化 (幂等 SQL 脚本)

### 基础设施
- **数据库**: MySQL 8.0
- **缓存**: Redis 7
- **容器化**: Docker + Docker Compose
- **镜像加速**: docker.1ms.run (国内镜像源)

## 功能模块

### 1. 首页概览 (Dashboard)
- 设备总数、工单总数、在保设备数、库存预警数等核心指标
- 快捷操作入口
- 即将到期维保设备列表

### 2. 设备档案管理 (Equipments)
- 设备档案增删改查
- 按客户、设备分类、状态筛选
- 设备分类管理
- 安装日期、维保周期、状态跟踪

### 3. 维保工单管理 (Orders)
- 工单创建、编辑、状态流转
- 工单列表视图与日历视图
- 工单类型：定期维保、故障维修、整机清洁
- 工单状态：待执行、执行中、已完成、已取消
- 备件消耗记录管理
- 批量编辑功能

### 4. 维保计划管理 (Plans)
- 维保计划制定
- 周期设置：季度、半年度、年度
- 计划状态跟踪
- 关联工单生成

### 5. 备件库存管理 (SpareParts)
- 备件档案管理
- 品类分类
- 库存数量与安全库存
- 库存预警提醒
- 库存调整功能

### 6. 客户管理 (Customers)
- 客户单位信息管理
- 联系人与联系方式
- 客户设备关联

## 快速开始

### 环境要求
- Docker 20.10+
- Docker Compose 2.0+
- 至少 2GB 可用内存

### 一键启动

```bash
./start.sh
```

脚本会自动完成：
1. 端口冲突检测
2. Docker 镜像构建与容器启动
3. 等待服务就绪
4. 输出访问地址

### 手动启动

```bash
# 1. 检测端口
./check-ports.sh

# 2. 构建并启动
docker compose up --build -d

# 3. 查看状态
docker compose ps

# 4. 查看日志
docker compose logs -f backend
```

### 访问地址

- **前端页面**: http://localhost:3010
- **后端 API**: http://localhost:8090/api
- **MySQL**: 127.0.0.1:3311
- **Redis**: 127.0.0.1:6382

### 默认账号

- **MySQL root 密码**: maintenance_root_2024
- **MySQL 普通用户**: maintenance / maintenance_2024
- **数据库名**: maintenance_db

## 端口说明

| 服务 | 宿主机端口 | 容器端口 | 绑定地址 | 说明 |
|------|-----------|----------|----------|------|
| 前端 (Nginx) | 3010 | 80 | 127.0.0.1 | Web 页面访问 |
| 后端 (Spring Boot) | 8090 | 8080 | 127.0.0.1 | REST API 接口 |
| MySQL | 3311 | 3306 | 127.0.0.1 | 数据库 |
| Redis | 6382 | 6379 | 127.0.0.1 | 缓存 |

> 所有端口均绑定 `127.0.0.1`，仅本机可访问，保证安全性。

## 项目结构

```
.
├── backend/                     # 后端项目
│   ├── src/main/java/com/maintenance/
│   │   ├── common/              # 通用类 (Result、PageResult、异常处理)
│   │   ├── config/              # 配置类 (CORS、Redis、数据初始化)
│   │   ├── controller/          # 控制层 (5个 Controller)
│   │   ├── entity/              # 实体类 (9个实体)
│   │   ├── repository/          # 数据访问层 (9个 Repository)
│   │   └── service/             # 业务逻辑层 (6个 Service)
│   ├── src/main/resources/
│   │   ├── sql/                 # SQL 初始化脚本 (幂等)
│   │   ├── application.yml      # 本地开发配置
│   │   └── application-docker.yml # Docker 环境配置
│   ├── .dockerignore
│   ├── Dockerfile               # 三阶段构建 (deps → build → runtime)
│   ├── pom.xml
│   └── settings.xml             # Maven 华为云镜像配置
├── frontend/                    # 前端项目
│   ├── src/
│   │   ├── api/                 # API 接口封装
│   │   ├── router/              # 路由配置
│   │   ├── utils/               # 工具函数
│   │   └── views/               # 页面组件 (6个视图)
│   ├── .dockerignore
│   ├── Dockerfile               # 三阶段构建 (deps → build → runtime)
│   ├── nginx.conf               # Nginx 配置
│   ├── vite.config.js           # Vite 配置
│   └── package.json
├── .env                         # 全局环境变量
├── docker-compose.yml           # Docker Compose 编排
├── check-ports.sh               # 端口冲突检测脚本
├── start.sh                     # 一键启动脚本
└── README.md
```

## Docker 构建优化说明

### 分层缓存机制

前后端均采用 **三阶段构建** 策略，充分利用 Docker 分层缓存：

#### 后端构建阶段
1. **deps 阶段**: 仅复制 `pom.xml` 和 `settings.xml`，下载 Maven 依赖。只要 pom.xml 不变，此层缓存复用。
2. **build 阶段**: 复制源码，编译打包。仅源码变更时重新编译。
3. **runtime 阶段**: 基于 JRE 镜像，仅复制打包好的 jar，最小化运行镜像体积。

#### 前端构建阶段
1. **deps 阶段**: 仅复制 `package.json`，执行 `npm install`。只要依赖不变，此层缓存复用。
2. **build 阶段**: 复制源码，执行 `npm run build`。仅源码变更时重新构建。
3. **runtime 阶段**: 基于 Nginx 镜像，托管静态资源，配置 API 代理。

### .dockerignore

排除无关文件，减少构建上下文体积：
- 后端排除: `target/`、`*.class`、`*.log`、`.idea/` 等
- 前端排除: `node_modules/`、`dist/`、`*.log`、`.idea/` 等

### 国内镜像源

- **Docker 镜像**: `docker.1ms.run` 镜像加速器
- **Maven 依赖**: 华为云开源镜像站
- **npm 依赖**: 京东 npm 镜像 (registry.npmmirror.com)

## API 接口列表

### 客户管理
- `GET /api/customers` - 分页查询客户列表
- `GET /api/customers/{id}` - 获取客户详情
- `GET /api/customers/all` - 获取所有客户
- `POST /api/customers` - 新增客户
- `PUT /api/customers/{id}` - 更新客户
- `DELETE /api/customers/{id}` - 删除客户

### 设备管理
- `GET /api/equipments` - 分页查询设备列表
- `GET /api/equipments/{id}` - 获取设备详情
- `GET /api/equipments/categories` - 获取设备分类列表
- `GET /api/equipments/upcoming-maintenance` - 获取即将到期维保设备
- `POST /api/equipments` - 新增设备
- `PUT /api/equipments/{id}` - 更新设备
- `DELETE /api/equipments/{id}` - 删除设备

### 备件管理
- `GET /api/spare-parts` - 分页查询备件列表
- `GET /api/spare-parts/{id}` - 获取备件详情
- `GET /api/spare-parts/categories` - 获取备件分类列表
- `GET /api/spare-parts/low-stock` - 获取库存预警备件
- `POST /api/spare-parts` - 新增备件
- `PUT /api/spare-parts/{id}` - 更新备件
- `PUT /api/spare-parts/{id}/stock` - 调整库存
- `DELETE /api/spare-parts/{id}` - 删除备件

### 工单管理
- `GET /api/orders` - 分页查询工单列表
- `GET /api/orders/{id}` - 获取工单详情
- `GET /api/orders/stats` - 获取工单统计
- `GET /api/orders/{id}/parts` - 获取工单备件消耗
- `POST /api/orders` - 新增工单
- `PUT /api/orders/{id}` - 更新工单
- `PUT /api/orders/{id}/status` - 更新工单状态
- `PUT /api/orders/batch-status` - 批量更新工单状态
- `POST /api/orders/{id}/parts` - 添加工单备件消耗
- `DELETE /api/orders/{id}/parts/{partId}` - 删除工单备件消耗
- `DELETE /api/orders/{id}` - 删除工单

### 维保计划
- `GET /api/plans` - 分页查询维保计划列表
- `GET /api/plans/{id}` - 获取维保计划详情
- `POST /api/plans` - 新增维保计划
- `PUT /api/plans/{id}` - 更新维保计划
- `DELETE /api/plans/{id}` - 删除维保计划

## 数据库设计

### 核心数据表

| 表名 | 说明 | 数量 |
|------|------|------|
| equipment_category | 设备分类表 | 8 种分类 |
| customer | 客户表 | - |
| equipment | 设备档案表 | - |
| spare_part_category | 备件品类表 | 10 种品类 |
| spare_part | 备件库存表 | - |
| maintenance_process | 维保工序表 | - |
| maintenance_order | 维保工单表 | - |
| order_part_consumption | 工单备件消耗表 | - |
| maintenance_plan | 维保计划表 | - |

### 数据初始化

系统启动时通过 Spring SQL 初始化机制自动执行建表和数据初始化：
- `schema.sql`: 建表语句 (CREATE TABLE IF NOT EXISTS)
- `data.sql`: 初始化数据 (INSERT ... ON DUPLICATE KEY UPDATE)

所有 SQL 脚本均为幂等设计，重复执行不会产生错误。

## 常见问题

### 1. 端口被占用怎么办？

启动脚本会自动检测端口冲突。如果端口被占用，请：
- 停止占用端口的进程，或
- 修改 `.env` 文件中的端口配置，然后重新启动

### 2. 如何重启某个服务？

```bash
# 重启后端
docker compose restart backend

# 重启前端
docker compose restart frontend
```

### 3. 如何查看服务日志？

```bash
# 查看所有服务日志
docker compose logs -f

# 查看后端日志
docker compose logs -f backend

# 查看前端日志
docker compose logs -f frontend
```

### 4. 如何清理数据重新初始化？

```bash
# 停止并删除数据卷
docker compose down -v

# 重新启动
docker compose up -d
```

### 5. 构建镜像时拉取失败？

项目已配置国内镜像源，如仍失败请检查：
- 网络连接是否正常
- Docker 镜像加速器配置是否生效
- 可尝试修改 `.env` 中的 `DOCKER_REGISTRY` 为其他镜像源

## 许可证

本项目仅供学习研究使用。
