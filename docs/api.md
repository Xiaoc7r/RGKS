# REST API

除登录外均需 HTTP Basic。默认学期 `2026-FALL`；成绩演示学期 `2026-SPRING`。

## 1 身份认证

- `POST /api/auth/login`：校验凭据并返回当前账号。
- `GET /api/auth/me`：返回当前账号与角色。

## 2 人员档案（REGISTRAR）

- `GET|POST /api/people/students`
- `PUT|DELETE /api/people/students/{id}`
- `GET|POST /api/people/professors`
- `PUT|DELETE /api/people/professors/{id}`

创建时证件号必填；更新时留空代表保留；响应只含掩码。

## 3 只读课程目录（全角色）

- `GET /api/catalog/status`
- `GET /api/catalog/courses`
- `GET /api/catalog/offerings?semester=2026-FALL`

## 4 学生选课（STUDENT）

- `GET /api/registrations/my-schedule?semester=...`
- `POST /api/registrations/selections`
- `PUT|DELETE /api/registrations/selections/{itemId}?semester=...`
- `POST /api/registrations/save?semester=...`
- `DELETE /api/registrations/my-schedule?semester=...`
- `POST /api/registrations/submit?semester=...`

选课请求：`semester`、`offeringId`、`choiceType`、`priority`。

## 5 教师任课（PROFESSOR）

- `GET /api/teaching/eligible-offerings?semester=...`
- `GET /api/teaching/my-offerings?semester=...`
- `POST /api/teaching/offerings/{id}/select`
- `DELETE /api/teaching/offerings/{id}/selection`
- `GET /api/teaching/offerings/{id}/roster`

## 6 成绩（PROFESSOR/STUDENT）

- `GET /api/grading/my-offerings?semester=...`（教师）
- `GET /api/grading/offerings/{id}/roster`（教师）
- `PUT /api/grading/grades`（教师）
- `GET /api/grading/my-report-card?semester=...`（学生）

成绩请求：`offeringId`、`studentId`、`gradeValue`。

## 7 结算与计费（REGISTRAR）

- `GET /api/operations/overview?semester=...`
- `POST /api/operations/close-registration?semester=...`
- `GET /api/operations/billing?semester=...`
- `POST /api/operations/billing/{id}/attempt?success=false|true`

`success=false` 用于答辩演示外部系统不可用；随后传 `true` 展示重试成功。
