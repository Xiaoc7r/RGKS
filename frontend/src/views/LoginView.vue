<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuth } from '../auth'

const router = useRouter()
const { login } = useAuth()
const username = ref('student1')
const password = ref('Student123!')
const error = ref('')
const loading = ref(false)

async function submit() {
  loading.value = true
  error.value = ''
  try {
    await login(username.value, password.value)
    router.push('/')
  } catch (reason) {
    error.value = reason.message
  } finally {
    loading.value = false
  }
}

function useDemo(role) {
  const demos = {
    student: ['student1', 'Student123!'],
    professor: ['professor1', 'Professor123!'],
    registrar: ['registrar', 'Registrar123!']
  }
  ;[username.value, password.value] = demos[role]
}
</script>

<template>
  <section class="login-layout">
    <div class="login-intro">
      <p class="module-number">COURSE REGISTRATION · 2026</p>
      <h2>让选课流程<br />清晰、可靠、可解释。</h2>
      <p>一个为课程设计答辩准备的完整业务闭环。七个模块、三个角色、一套可重复演示的数据。</p>
      <div class="intro-meta"><span>Vue 3</span><span>Spring Boot</span><span>H2 / MySQL</span></div>
    </div>
    <div class="login-card">
      <p class="module-number">模块 1 · 身份认证与角色权限</p>
      <h3>欢迎回来</h3>
      <p class="muted">选择演示身份，或输入账号密码。</p>
      <form @submit.prevent="submit">
        <div class="field">
          <label for="username">账号</label>
          <input id="username" v-model="username" autocomplete="username" required />
        </div>
        <div class="field">
          <label for="password">密码</label>
          <input id="password" v-model="password" type="password" autocomplete="current-password" required />
        </div>
        <p v-if="error" class="error">{{ error }}</p>
        <button class="button login-button" :disabled="loading">{{ loading ? '登录中…' : '进入系统' }}</button>
      </form>
      <div class="demo-divider"><span>快速演示</span></div>
      <div class="demo-buttons">
        <button class="button secondary" @click="useDemo('student')">学生</button>
        <button class="button secondary" @click="useDemo('professor')">教师</button>
        <button class="button secondary" @click="useDemo('registrar')">管理员</button>
      </div>
    </div>
  </section>
</template>

<style scoped>
.login-layout { width: min(980px, 100%); display: grid; grid-template-columns: 1.08fr .92fr; overflow: hidden; background: #fffdf9; border: 1px solid #e1dbd4; border-radius: 22px; box-shadow: 0 28px 70px rgba(62, 50, 42, .12); }
.login-intro { padding: 58px 52px; color: #f8f1e9; background: #35312e; }
.login-intro .module-number { color: #e6a58f; }
.login-intro h2 { margin: 18px 0; font-family: Georgia, "Noto Serif SC", serif; font-size: clamp(34px, 4vw, 48px); font-weight: 500; line-height: 1.2; letter-spacing: -.035em; }
.login-intro > p { max-width: 410px; color: #c9c0b7; line-height: 1.8; }
.intro-meta { display: flex; flex-wrap: wrap; gap: 8px; margin-top: 38px; }
.intro-meta span { padding: 7px 10px; color: #d9cec4; border: 1px solid #5c5550; border-radius: 8px; font-size: 11px; }
.login-card { padding: 50px 42px; }
.login-card h3 { margin: 5px 0 8px; font-family: Georgia, "Noto Serif SC", serif; font-size: 28px; font-weight: 500; }
form { display: grid; gap: 16px; margin-top: 24px; }
.login-button { width: 100%; margin-top: 4px; }
.demo-divider { display: flex; align-items: center; gap: 10px; margin: 24px 0 14px; color: #999089; font-size: 11px; }
.demo-divider::before, .demo-divider::after { content: ""; flex: 1; height: 1px; background: #e5dfd8; }
.demo-buttons { display: grid; grid-template-columns: repeat(3, 1fr); gap: 8px; }
@media (max-width: 760px) { .login-layout { grid-template-columns: 1fr; } .login-intro { padding: 34px 28px; } .login-intro h2 { font-size: 32px; } .login-card { padding: 34px 26px; } }
</style>
