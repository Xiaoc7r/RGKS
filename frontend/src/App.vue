<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuth } from './auth'

const router = useRouter()
const { state, loggedIn, logout } = useAuth()

const roleLabel = computed(() => ({
  STUDENT: '学生',
  PROFESSOR: '教师',
  REGISTRAR: '注册管理员'
})[state.user?.role] || '')

const navItems = computed(() => {
  const items = [
    { to: '/', label: '项目总览', index: '00' },
    { to: '/account', label: '账号与权限', index: '01' },
    { to: '/catalog', label: '课程目录', index: '03' }
  ]
  if (state.user?.role === 'STUDENT') {
    items.push({ to: '/registration', label: '学生选课', index: '04' })
    items.push({ to: '/report-card', label: '我的成绩单', index: '06' })
  }
  if (state.user?.role === 'PROFESSOR') {
    items.push({ to: '/teaching', label: '任课与名单', index: '05' })
    items.push({ to: '/grading', label: '成绩管理', index: '06' })
  }
  if (state.user?.role === 'REGISTRAR') {
    items.push({ to: '/people', label: '人员档案', index: '02' })
    items.push({ to: '/operations', label: '结算与计费', index: '07' })
  }
  return items
})

function signOut() {
  logout()
  router.push('/login')
}
</script>

<template>
  <div class="app-shell">
    <header class="topbar">
      <div class="brand">
        <span class="brand-mark">选</span>
        <div>
          <p class="eyebrow">REGISTRATION STUDIO · 2026</p>
          <h1>高校选课注册系统</h1>
        </div>
      </div>
      <div v-if="loggedIn" class="user-block">
        <div class="user-chip"><strong>{{ state.user.displayName }}</strong><span>{{ roleLabel }} · {{ state.user.username }}</span></div>
        <button class="button secondary" @click="signOut">退出</button>
      </div>
    </header>

    <div v-if="loggedIn" class="workspace">
      <nav class="sidebar" aria-label="系统导航">
        <p class="nav-label">工作区</p>
        <RouterLink v-for="item in navItems" :key="item.to" :to="item.to" :data-index="item.index">
          {{ item.label }}
        </RouterLink>
        <p class="sidebar-note"><span class="ready-dot"></span>演示系统已就绪<br />7 个模块 · 单体架构</p>
      </nav>
      <main class="content"><RouterView /></main>
    </div>
    <main v-else class="login-stage"><RouterView /></main>
  </div>
</template>
