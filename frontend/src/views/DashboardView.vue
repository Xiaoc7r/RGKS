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
  <header class="overview-hero">
    <div>
      <p class="module-number">COURSE DESIGN OVERVIEW</p>
      <h2>七个人，七个边界清晰的模块。</h2>
      <p>模块化单体部署：一个 Spring Boot 应用、一个数据库、七组可独立讲解的业务代码。当前登录身份为 <strong>{{ state.user?.displayName }}</strong>。</p>
    </div>
    <div class="hero-stat"><strong>7</strong><span>答辩模块</span></div>
  </header>
  <div class="section-heading"><div><p class="module-number">MODULE MAP</p><h3>功能地图</h3></div><span><i></i>系统可演示</span></div>
  <section class="module-grid">
    <article v-for="module in modules" :key="module.number" class="card module-card" :class="{ locked: !accessible(module) }">
      <div class="module-top"><span class="step-number">0{{ module.number }}</span><span class="role-tag">{{ module.roles }}</span></div>
      <h3>{{ module.title }}</h3>
      <p>{{ module.description }}</p>
      <div class="module-footer"><span>{{ module.owner }}</span><RouterLink v-if="accessible(module)" class="text-link" :to="module.route">打开模块 →</RouterLink><span v-else>当前角色只读</span></div>
    </article>
  </section>
</template>

<style scoped>
.overview-hero { display: flex; align-items: end; justify-content: space-between; gap: 28px; margin-bottom: 38px; padding: 34px 36px; color: #f8f1e9; background: #35312e; border-radius: 19px; }
.overview-hero h2 { max-width: 680px; margin: 7px 0 11px; font-family: Georgia, "Noto Serif SC", serif; font-size: clamp(30px, 4vw, 45px); font-weight: 500; letter-spacing: -.035em; }
.overview-hero > div > p:last-child { max-width: 720px; margin: 0; color: #c9c0b7; line-height: 1.75; }
.overview-hero .module-number { color: #e3a18a; }
.hero-stat { min-width: 128px; padding-left: 26px; border-left: 1px solid #625a54; }
.hero-stat strong { display: block; color: #e49a7e; font-family: Georgia, serif; font-size: 58px; font-weight: 400; line-height: 1; }
.hero-stat span { color: #c9c0b7; font-size: 12px; }
.section-heading { display: flex; align-items: end; justify-content: space-between; margin-bottom: 15px; }
.section-heading h3 { margin: 2px 0 0; font-family: Georgia, "Noto Serif SC", serif; font-size: 24px; font-weight: 500; }
.section-heading > span { color: #77716a; font-size: 12px; }
.section-heading i { display: inline-block; width: 7px; height: 7px; margin-right: 7px; border-radius: 50%; background: #5e8b70; }
.module-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(270px, 1fr)); gap: 14px; }
.module-card { min-height: 220px; display: flex; flex-direction: column; }
.module-card.locked { background: #f3f0eb; box-shadow: none; }
.module-top, .module-footer { display: flex; align-items: center; justify-content: space-between; gap: 12px; }
.step-number { color: #d58a70; font-family: Georgia, serif; font-size: 23px; }
.role-tag { padding: 5px 8px; color: #777068; background: #f1ede8; border-radius: 7px; font-size: 10px; }
.module-card h3 { margin-top: 22px; font-size: 18px; }
.module-card > p { flex: 1; margin: 0; }
.module-footer { margin-top: 20px; padding-top: 14px; color: #918980; border-top: 1px solid #ebe5de; font-size: 11px; }
.text-link { color: #b55336; font-weight: 800; text-decoration: none; }
@media (max-width: 680px) { .overview-hero { align-items: flex-start; flex-direction: column; padding: 28px 24px; } .hero-stat { padding: 15px 0 0; border-left: 0; border-top: 1px solid #625a54; } }
</style>
