# 高校选课注册系统 · 七人答辩版

面向 2026 软件工程课程设计的可运行项目。技术栈为 **Vue 3 + Spring Boot + JPA + H2/MySQL**，采用模块化单体：只部署一个后端进程，但按七个业务包划分职责，刚好对应七名成员独立答辩。

## 队长最快启动

Windows 下直接双击根目录的 **`启动项目.cmd`**。脚本会：

1. 用 `demo` 配置启动 Spring Boot；
2. 使用内存 H2 自动创建标准答辩数据，不依赖 Docker/MySQL；
3. 安装缺失的前端依赖并启动 Vite；
4. 自动打开 `http://localhost:5173`。

结束时双击 **`停止项目.cmd`**。发布前可双击 **`检查项目.cmd`**，自动运行后端 7 模块测试和前端生产构建。详细故障处理见 [队长运行手册](docs/队长运行手册.md)。

## 演示账号

| 身份 | 账号 | 密码 | 可演示功能 |
|---|---|---|---|
| 学生 | `student1` | `Student123!` | 课程目录、4+2 选课、成绩单 |
| 教师 | `professor1` | `Professor123!` | 任课选择、班级名单、成绩录入 |
| 注册管理员 | `registrar` | `Registrar123!` | 人员档案、关选课、计费重试 |

每次重新启动答辩模式，数据自动恢复。建议教师任课和学生选课先演示，管理员“关闭选课”最后演示。

## 七个模块与七份独立答辩文档

| 成员 | 模块 | 后端包 | 前端页面 | 独立答辩文档 |
|---|---|---|---|---|
| 1 | 身份认证与角色权限 | `identity` + `SecurityConfig` | Login、Account | [成员 1 文档](docs/defense/01-身份认证与角色权限.md) |
| 2 | 学生与教师档案 | `people` | People | [成员 2 文档](docs/defense/02-学生与教师档案.md) |
| 3 | 课程目录与教学班 | `catalog` | Catalog | [成员 3 文档](docs/defense/03-课程目录与教学班.md) |
| 4 | 学生选课与课表 | `registration` | Registration | [成员 4 文档](docs/defense/04-学生选课与课表.md) |
| 5 | 教师任课与名单 | `teaching` | Teaching | [成员 5 文档](docs/defense/05-教师任课与名单.md) |
| 6 | 成绩与成绩单 | `grading` | Grading、ReportCard | [成员 6 文档](docs/defense/06-成绩与成绩单.md) |
| 7 | 关选课与计费 | `operations` | Operations | [成员 7 文档](docs/defense/07-关选课与计费.md) |

七份文档都包含：小白解释、代码文件定位、对应核心代码块、60 秒背诵稿、必背术语、演示步骤和老师追问。统一入口见 [七人答辩文档目录](docs/defense/README.md)。

## 完整业务闭环

1. 三种角色登录，后端执行角色权限和数据所有权校验。
2. 管理员维护学生/教师完整档案，证件号只返回掩码。
3. 全角色只读查看课程、先修课、学费、教学班和余量。
4. 学生创建、保存、修改、删除课表，按 4 主选 + 2 备选提交。
5. 教师按院系选择/退选教学班，检查时间冲突，查看本人班级名单。
6. 教师为已结束课程录入 A/B/C/D/F/I，学生查看学分和 GPA。
7. 管理员关闭选课：取消无教师班、备选补位、取消不足 3 人班、冻结课表、计费并重试失败记录。

## 架构

```text
Vue 3 SPA :5173 → REST / Basic Auth → Spring Boot :8080
                                      ├─ 1 identity
                                      ├─ 2 people
                                      ├─ 3 catalog
                                      ├─ 4 registration
                                      ├─ 5 teaching
                                      ├─ 6 grading
                                      └─ 7 operations
                                             ↓
                             H2（答辩）或 MySQL 8（持久化）
```

这是“模块化单体”，不是微服务。它保留清晰代码边界，又避免课程项目承担服务发现、网关和分布式事务等额外复杂度。

## 手动启动

推荐的免数据库答辩模式：

```powershell
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=demo

# 另开 PowerShell
cd frontend
npm install
npm run dev
```

需要 MySQL 持久化时：

```powershell
docker compose up -d mysql
cd backend
mvn spring-boot:run
```

MySQL 默认库 `course_registration`，用户 `course_user`，密码 `course_pass`，宿主机端口 `3307`。此时前端仍单独运行 `npm run dev`。

## 测试

```powershell
cd backend
mvn test

cd ../frontend
npm ci
npm run build
npm audit --audit-level=high
```

后端集成测试严格对应七个模块，成功标准为 `Tests run: 7, Failures: 0, Errors: 0`。

## 课程项目边界

- HTTP Basic 只用于本机演示；凭据放在当前标签页 `sessionStorage`。生产环境应使用 HTTPS 和服务端会话或短期令牌。
- 题目中的真实 Ingres/DEC VAX 无法接入，因此使用明确标注的只读课程目录适配模拟器，不提供目录写接口。
- 外部计费平台用 PENDING/FAILED/SENT 状态机模拟，保留失败与重试语义。
- JPA 自动建表适合课程演示；生产化可再引入 Flyway、审计日志与并发压测。

## 文档导航

- [项目当前状态](docs/项目现状.md)
- [队长运行手册](docs/队长运行手册.md)
- [七人答辩入口](docs/defense/README.md)
- [需求覆盖矩阵](docs/requirements-coverage.md)
- [架构与关键流程](docs/architecture.md)
- [REST API](docs/api.md)
- [测试计划](docs/test-plan.md)
