# REST API 最小清单

所有业务接口都需要 HTTP Basic 认证。默认学期参数为 `2026-FALL`。

## 模块 1 身份认证

- `POST /api/auth/login`
- `GET /api/auth/me`

## 模块 2 人员档案

- `GET|POST /api/people/students`
- `PUT|DELETE /api/people/students/{id}`
- `GET|POST /api/people/professors`
- `PUT|DELETE /api/people/professors/{id}`

## 模块 3 课程目录

- `GET /api/catalog/offerings?semester=2026-FALL`

## 模块 4 学生选课

- `GET /api/registrations/my-schedule`
- `POST /api/registrations/selections`
- `DELETE /api/registrations/selections/{itemId}`
- `POST /api/registrations/submit`

## 模块 5 教师与成绩

- `GET /api/teaching/my-offerings`
- `POST /api/teaching/offerings/{offeringId}/select`
- `GET /api/teaching/offerings/{offeringId}/roster`
- `PUT /api/teaching/grades`
- `GET /api/teaching/my-report-card`

## 模块 6 关选课与计费

- `POST /api/operations/close-registration`
- `GET /api/operations/billing`
- `POST /api/operations/billing/{billingId}/mark-sent`
