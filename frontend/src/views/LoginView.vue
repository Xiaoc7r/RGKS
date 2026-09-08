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
  <section class="login-card">
    <p class="module-number">模块 1 · 身份认证与角色权限</p>
    <h2>登录系统</h2>
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
      <button class="button login-button" :disabled="loading">
        {{ loading ? '登录中…' : '登录' }}
      </button>
    </form>
    <div class="demo-buttons">
      <button class="button secondary" @click="useDemo('student')">学生账号</button>
      <button class="button secondary" @click="useDemo('professor')">教师账号</button>
      <button class="button secondary" @click="useDemo('registrar')">管理员账号</button>
    </div>
  </section>
</template>

<style scoped>
.login-card { width: min(460px, 100%); padding: 32px; background: white; border-radius: 12px; border: 1px solid #dce4ed; box-shadow: 0 16px 42px rgba(22, 53, 82, .12); }
.login-card h2 { margin: 5px 0 8px; font-size: 28px; }
form { display: grid; gap: 16px; margin-top: 24px; }
.login-button { width: 100%; margin-top: 4px; }
.demo-buttons { display: flex; flex-wrap: wrap; gap: 8px; margin-top: 22px; }
</style>

