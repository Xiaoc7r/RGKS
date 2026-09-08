# 高校选课注册系统

这是 2026 软件工程课程设计的最小可运行架构。系统采用 Vue 前端、Spring Boot 后端、MySQL 数据库和 REST API，后端以模块化单体方式组织。

当前版本已经形成一条可演示的最小业务闭环：

1. 学生、教师或注册管理员登录。
2. 学生查看课程目录，建立 4 门主选课和 2 门备选课的课表并提交。
3. 教师查看本人教学班和学生名单，录入成绩。
4. 注册管理员关闭选课，系统取消不满足条件的教学班、尝试备选补位、冻结课表并生成计费记录。
5. 学生查看成绩单。

## 技术架构

```text
Vue 单页应用（localhost:5173）
            |
         REST API
            |
Spring Boot 模块化单体（localhost:8080）
  | identity   账号和角色
  | people     学生和教师档案
  | catalog    课程目录和教学班
  | registration  学生选课和课表
  | teaching   教师任课和成绩
  | operations 关选课和计费
            |
          MySQL 8
```

六个模块是代码和职责边界，不是六个独立进程。所有模块随一个 Spring Boot 应用部署，降低课程项目的开发和联调成本。详细分工见 [docs/team-division.md](docs/team-division.md)。

## 本机启动

### 1. 启动 MySQL

项目提供 Docker Compose 配置：

```powershell
cd course-registration-system
docker compose up -d mysql
```

默认数据库连接：

```text
数据库：course_registration
账号：course_user
密码：course_pass
宿主机端口：3307（避免与你电脑上已有的 MySQL 3306 冲突）
```

如果使用本机已有 MySQL，可以通过环境变量覆盖：

```powershell
$env:DB_URL='jdbc:mysql://localhost:3306/course_registration?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false'
$env:DB_USERNAME='你的数据库账号'
$env:DB_PASSWORD='你的数据库密码'
```

### 2. 启动后端

新开一个 PowerShell 窗口：

```powershell
cd course-registration-system/backend
mvn spring-boot:run
```

健康检查地址：`http://localhost:8080/actuator/health`

### 3. 启动前端

再开一个 PowerShell 窗口：

```powershell
cd course-registration-system/frontend
npm install
npm run dev
```

浏览器访问：`http://localhost:5173`

如需清空演示数据并从初始化状态重新开始，请先停止后端，然后执行：

```powershell
docker compose down -v
docker compose up -d mysql
```

该命令只删除本项目 Compose 创建的 MySQL 数据卷。

## 演示账号

| 身份 | 账号 | 密码 |
|---|---|---|
| 学生 | `student1` | `Student123!` |
| 教师 | `professor1` | `Professor123!` |
| 注册管理员 | `registrar` | `Registrar123!` |

首次启动会自动写入演示人员、课程、教学班、开放选课窗口和一条历史成绩。

## 测试与构建

后端测试使用 H2 的 MySQL 兼容模式，不影响正式 MySQL 数据：

```powershell
cd backend
mvn test
```

前端生产构建：

```powershell
cd frontend
npm run build
```

集成测试覆盖角色权限以及学生选课、教师录成绩、管理员关选课和生成计费记录的完整最小流程。

## 当前 MVP 边界

- 登录采用 HTTP Basic，适合本机课程演示。后续可替换为 JWT，并在正式环境启用 HTTPS。
- 旧课程目录系统通过只读 REST 资源和独立 `catalog` 模块模拟，没有搭建真实 Ingres 或 DEC VAX。
- 计费系统用 `billing_records` 状态机模拟，保留了发送状态和重试次数。
- 数据库表由 JPA 自动更新。进入稳定开发阶段后建议增加 Flyway 迁移脚本。
- 当前页面优先保证六个模块均可演示，复杂交互、分页、审计日志和压力测试可在后续迭代补充。
