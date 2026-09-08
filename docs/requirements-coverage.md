# 需求覆盖与后续迭代

## 当前已经覆盖

| 课程题目要求 | 当前实现位置 | 验证方式 |
|---|---|---|
| 学生、教师、管理员登录 | `identity` | 三个演示账号登录 |
| 学生只能修改自己的课表 | `CurrentUserService` 与 `RegistrationService` | 学生接口不接受外部 studentId |
| 教师只能维护自己的教学班 | `TeachingService.requireOwnedOffering` | 非任课教师请求被拒绝 |
| 管理员维护学生和教师 | `people` | 管理员 CRUD 接口 |
| 4 门主选和 2 门备选 | `RegistrationService.submit` | 数量不足时返回冲突错误 |
| 课程容量上限 10 人 | `CourseOffering.capacity` 与悲观锁 | 提交选课时锁定教学班并计数 |
| 先修课检查 | `GradeRepository.existsPassingGrade` | Java 课程需要程序设计基础及格成绩 |
| 时间冲突检查 | `CourseOffering.conflictsWith` | 提交课表或教师选课时校验 |
| 少于 3 人的教学班取消 | `OperationsService.closeRegistration` | 管理员关闭选课 |
| 无教师的教学班取消 | `OperationsService.closeRegistration` | 管理员关闭选课 |
| 备选课程补位 | `OperationsService.levelSchedule` | 主选取消后按优先级尝试补位 |
| 生成计费记录 | `BillingRecord` | 关选课后查询计费列表 |
| 计费失败重试信息 | `status`、`retryCount`、`lastAttemptAt` | 当前用模拟发送接口演示 |
| 教师录入 A/B/C/D/F/I | `TeachingService.submitGrade` | 非法成绩值被拒绝 |
| 学生查看成绩单 | `/api/teaching/my-report-card` | 学生账号查看历史成绩 |

## 下一迭代建议

### P0 必做

- 为六个模块分别补充单元测试和接口测试。
- 增加管理员修改档案的前端表单。
- 增加教师取消任课和学生删除整张课表。
- 增加数据库迁移脚本，固定表结构和演示数据版本。
- 形成需求规格说明、总体设计、详细设计、测试计划和测试报告。

### P1 展示加分

- 用 JMeter 编写容量竞争和并发选课场景。
- 将 Basic Auth 替换为 JWT，并增加密码修改。
- 增加操作审计日志和关键业务事件。
- 将计费模拟替换为本机第二个 Mock 服务，展示失败重试。
- 为课程目录增加缓存和 10 秒超时降级演示。

