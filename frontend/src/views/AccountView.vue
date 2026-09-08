<script setup>
import { ref } from 'vue'
import { request } from '../api'
import { useAuth } from '../auth'

const { state } = useAuth()
const refreshed = ref(null)
const error = ref('')

async function refresh() {
  error.value = ''
  try {
    refreshed.value = await request('/api/auth/me')
  } catch (reason) {
    error.value = reason.message
  }
}
</script>

<template>
  <header class="page-header">
    <p class="module-number">模块 1 · 身份认证与角色权限</p>
    <h2>当前账号</h2>
    <p>后端根据账号角色执行接口级权限校验，前端菜单只负责改善使用体验。</p>
  </header>
  <section class="panel">
    <dl class="details">
      <dt>账号</dt><dd>{{ state.user.username }}</dd>
      <dt>显示名称</dt><dd>{{ state.user.displayName }}</dd>
      <dt>角色</dt><dd>{{ state.user.role }}</dd>
      <dt>关联档案 ID</dt><dd>{{ state.user.linkedPersonId ?? '管理员无人员档案' }}</dd>
    </dl>
    <button class="button" @click="refresh">重新向后端验证身份</button>
    <p v-if="refreshed" class="success">验证成功：{{ refreshed.displayName }}</p>
    <p v-if="error" class="error">{{ error }}</p>
  </section>
</template>

<style scoped>
.details { display: grid; grid-template-columns: 130px 1fr; gap: 12px; margin: 0 0 22px; }
dt { color: #627386; }
dd { margin: 0; font-weight: 700; }
</style>

