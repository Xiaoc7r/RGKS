<script setup>
import { onMounted, ref } from 'vue'
import { request } from '../api'

const billings = ref([])
const summary = ref(null)
const error = ref('')
const message = ref('')

async function loadBilling() {
  error.value = ''
  try {
    billings.value = await request('/api/operations/billing?semester=2026-FALL')
  } catch (reason) {
    error.value = reason.message
  }
}

async function closeRegistration() {
  error.value = ''
  message.value = ''
  if (!window.confirm('关选课会冻结课表并取消人数不足的教学班，是否继续？')) return
  try {
    summary.value = await request('/api/operations/close-registration?semester=2026-FALL', { method: 'POST' })
    message.value = '选课已经关闭，课表和计费记录已生成'
    await loadBilling()
  } catch (reason) {
    error.value = reason.message
  }
}

async function markSent(id) {
  error.value = ''
  try {
    await request(`/api/operations/billing/${id}/mark-sent`, { method: 'POST' })
    await loadBilling()
  } catch (reason) {
    error.value = reason.message
  }
}

onMounted(loadBilling)
</script>

<template>
  <header class="page-header">
    <p class="module-number">模块 6 · 关选课与计费</p>
    <h2>注册管理员工作台</h2>
    <p>关选课会检查教师和最低人数，执行备选补位，冻结课表并产生待发送计费记录。</p>
  </header>
  <section class="panel section-gap">
    <h3>关闭 2026 秋季选课</h3>
    <p class="muted">这是一次性操作。演示库只有一名选课学生时，低于 3 人的教学班会按题目规则取消。</p>
    <button class="button danger" @click="closeRegistration">执行关选课</button>
    <p v-if="message" class="success">{{ message }}</p>
    <p v-if="error" class="error">{{ error }}</p>
    <dl v-if="summary" class="summary">
      <dt>教学班总数</dt><dd>{{ summary.offeringCount }}</dd>
      <dt>取消教学班</dt><dd>{{ summary.cancelledOfferingCount }}</dd>
      <dt>备选补位</dt><dd>{{ summary.promotedAlternateCount }}</dd>
      <dt>冻结课表</dt><dd>{{ summary.finalizedScheduleCount }}</dd>
    </dl>
  </section>

  <h3>计费记录</h3>
  <div class="table-wrap">
    <table><thead><tr><th>ID</th><th>学生 ID</th><th>金额</th><th>状态</th><th>重试次数</th><th>操作</th></tr></thead>
      <tbody><tr v-for="billing in billings" :key="billing.id">
        <td>{{ billing.id }}</td><td>{{ billing.studentId }}</td><td>¥{{ billing.amount }}</td><td>{{ billing.status }}</td><td>{{ billing.retryCount }}</td>
        <td><button class="button" :disabled="billing.status === 'SENT'" @click="markSent(billing.id)">模拟发送成功</button></td>
      </tr></tbody>
    </table>
    <p v-if="!billings.length" class="empty">关选课后生成计费记录。</p>
  </div>
</template>

<style scoped>
.section-gap { margin-bottom: 24px; }
.summary { display: grid; grid-template-columns: 140px 1fr; gap: 8px; margin-top: 18px; }
.summary dd { margin: 0; font-weight: 700; }
</style>

