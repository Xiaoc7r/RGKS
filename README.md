# 高校选课注册系统

面向 2026 软件工程课程设计的完整可运行实现。项目采用 Vue 3 + Spring Boot + MySQL，后端保持一个进程、七个清晰业务模块，既避免微服务带来的部署复杂度，也方便 7 名成员均等开发和答辩。

## 已完成的业务闭环

1. 学生、教师、注册管理员登录，后端按角色强制鉴权。
2. 注册管理员完整维护学生与教师档案，身份证件仅返回掩码。
3. 所有角色通过只读适配层查看旧课程目录、先修课、学费和教学班余量。
4. 学生创建、保存、修改、删除课表，按 4 门主选 + 2 门备选提交。
5. 教师按院系选择/退选教学班，系统检查时间冲突，并可查看本人班级名单。
6. 教师为已结束课程录入 A/B/C/D/F/I，学生查看学分与 GPA 成绩单。
7. 管理员关闭选课：取消无教师班、备选补位、取消不足 3 人班、冻结课表、生成计费并演示失败重试。

## 架构

```text
Vue 3 SPA :5173 → REST/Basic Auth → Spring Boot :8080 → MySQL 8 :3307
                                      ├─ 1 identity
                                      ├─ 2 people
                                      ├─ 3 catalog
                                      ├─ 4 registration
                                      ├─ 5 teaching
                                      ├─ 6 grading
                                      └─ 7 operations
```

七个模块是代码职责边界，不是七个独立服务。详细设计见 [架构说明](docs/architecture.md)，人员分工见 [七人分工](docs/team-division.md)。

## 一键启动

在项目根目录运行：

```powershell
docker compose up --build
```

浏览器访问 `http://localhost:5173`。如只启动数据库并本地调试：

```powershell
docker compose up -d mysql
cd backend
mvn spring-boot:run

# 另开 PowerShell
cd frontend
npm install
npm run dev
```

默认数据库为 `course_registration`，账号 `course_user`，密码 `course_pass`，宿主机端口 `3307`。清空演示数据可在停止服务后执行 `docker compose down -v`，该命令仅删除本项目的数据卷。

## 演示账号

| 身份 | 账号 | 密码 |
|---|---|---|
| 学生 | `student1` | `Student123!` |
| 教师 | `professor1` | `Professor123!` |
| 注册管理员 | `registrar` | `Registrar123!` |

演示库另外预置两名背景学生，使四个主选教学班已有 2 人；`student1` 提交后恰好达到最低 3 人，便于演示正常开课。另有两个无人任教班，关选课时会被取消。

## 验证

```powershell
cd backend
mvn test

cd ../frontend
npm ci
npm run build
npm audit --audit-level=high
```

集成测试按七个模块各设一条主测试，覆盖权限、人员 CRUD、只读目录、4+2 课表、教师选课、成绩单以及结算/计费重试。完整人工用例见 [测试计划](docs/test-plan.md)。

## 课程项目边界

- HTTP Basic 仅用于本机答辩，凭据只保存在当前浏览器标签页；生产环境应改用 HTTPS + 会话或短期令牌。
- 原始 Ingres/DEC VAX 无法在课程环境中接入，因此以只读 `catalog` 适配层明确模拟；系统没有课程目录写接口。
- 外部计费以 `billing_records` 状态机模拟，可展示 PENDING、FAILED、SENT 与重试次数。
- 使用 JPA 自动更新表结构，足以支撑课程演示；生产化时再引入 Flyway、审计日志和压力测试。

## 文档导航

- [需求覆盖矩阵](docs/requirements-coverage.md)
- [七人分工与答辩边界](docs/team-division.md)
- [架构和关键流程](docs/architecture.md)
- [REST API](docs/api.md)
- [测试计划](docs/test-plan.md)
- [七人答辩指南](docs/defense-guide.md)
