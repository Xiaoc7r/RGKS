<script setup>
import { useAuth } from '../auth'

const { state } = useAuth()
const modules = [
  { number: 1, title: '身份认证与角色权限', owner: '成员 1', roles: '全角色', route: '/account', description: '登录、当前用户、Basic Auth 与角色访问控制。' },
  { number: 2, title: '学生与教师档案', owner: '成员 2', roles: '管理员', route: '/people', description: '学生和教师信息的增删改查。' },
  { number: 3, title: '课程目录与教学班', owner: '成员 3', roles: '全角色', route: '/catalog', description: '课程、学分、先修课、上课时间和容量。' },
  { number: 4, title: '学生选课与课表', owner: '成员 4', roles: '学生', route: '/registration', description: '四门主选、两门备选、冲突和容量校验。' },
  { number: 5, title: '教师任课与名单', owner: '成员 5', roles: '教师', route: '/teaching', description: '按院系选择或退选教学班、冲突校验、查看学生名单。' },
  { number: 6, title: '成绩与成绩单', owner: '成员 6', roles: '教师/学生', route: state.user?.role === 'STUDENT' ? '/report-card' : '/grading', description: '结课后录入 A-F/I 成绩，学生查看学分与 GPA。' },
  { number: 7, title: '关选课与计费', owner: '成员 7', roles: '管理员', route: '/operations', description: '取消教学班、备选补位、课表冻结、计费与失败重试。' }
]

function accessible(module) {
  if (module.number === 2 || module.number === 7) return state.user?.role === 'REGISTRAR'
  if (module.number === 4) return state.user?.role === 'STUDENT'
  if (module.number === 5) return state.user?.role === 'PROFESSOR'
  if (module.number === 6) return ['STUDENT', 'PROFESSOR'].includes(state.user?.role)
  return true
}
</script>

<template>
  <header class="page-header">
    <h2>项目总览</h2>
    <p>模块化单体部署，一个 Spring Boot 应用按七个业务包组织，恰好由 7 名成员均等负责与答辩。</p>
  </header>
  <section class="grid">
    <article v-for="module in modules" :key="module.number" class="card">
      <p class="module-number">模块 {{ module.number }} · {{ module.owner }}</p>
      <h3>{{ module.title }}</h3>
      <p>{{ module.description }}</p>
      <p class="muted">答辩角色：{{ module.roles }}</p>
      <RouterLink v-if="accessible(module)" class="text-link" :to="module.route">进入模块</RouterLink>
      <span v-else class="muted">当前角色无权访问</span>
    </article>
  </section>
</template>

<style scoped>
.text-link { color: #0d6fa8; font-weight: 700; text-decoration: none; }
</style>
