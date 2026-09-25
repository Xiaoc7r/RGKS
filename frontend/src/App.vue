<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuth } from './auth'

const router = useRouter()
const { state, loggedIn, logout } = useAuth()

const navItems = computed(() => {
  const items = [
    { to: '/', label: '总览' },
    { to: '/account', label: '账号与权限' },
    { to: '/catalog', label: '课程目录' }
  ]
  if (state.user?.role === 'STUDENT') {
    items.push({ to: '/registration', label: '学生选课' })
    items.push({ to: '/report-card', label: '成绩单' })
  }
  if (state.user?.role === 'PROFESSOR') {
    items.push({ to: '/teaching', label: '任课与名单' })
    items.push({ to: '/grading', label: '成绩管理' })
  }
  if (state.user?.role === 'REGISTRAR') {
    items.push({ to: '/people', label: '人员档案' })
    items.push({ to: '/operations', label: '关选课与计费' })
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
      <div>
        <p class="eyebrow">2026 软件工程课程设计</p>
        <h1>高校选课注册系统</h1>
      </div>
      <div v-if="loggedIn" class="user-block">
        <span>{{ state.user.displayName }} · {{ state.user.role }}</span>
        <button class="button secondary" @click="signOut">退出</button>
      </div>
    </header>

    <div v-if="loggedIn" class="workspace">
      <nav class="sidebar" aria-label="系统导航">
        <RouterLink v-for="item in navItems" :key="item.to" :to="item.to">
          {{ item.label }}
        </RouterLink>
      </nav>
      <main class="content"><RouterView /></main>
    </div>
    <main v-else class="login-stage"><RouterView /></main>
  </div>
</template>
