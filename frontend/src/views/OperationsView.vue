<script setup>
import { onMounted, ref } from 'vue'
import { request } from '../api'

const overview = ref(null)
const billings = ref([])
const summary = ref(null)
const error = ref('')
const message = ref('')

async function load() {
  error.value = ''
  try {
    ;[overview.value, billings.value] = await Promise.all([
      request('/api/operations/overview?semester=2026-FALL'),
      request('/api/operations/billing?semester=2026-FALL')
    ])
  } catch (reason) { error.value = reason.message }
}

async function closeRegistration() {
  if (!window.confirm('关选课会冻结课表并生成计费记录，是否继续？')) return
  await perform(async () => { summary.value = await request('/api/operations/close-registration?semester=2026-FALL', { method: 'POST' }) }, '选课已经关闭，结算和计费已完成')
}

async function attempt(id, success) {
  await perform(() => request(`/api/operations/billing/${id}/attempt?success=${success}`, { method: 'POST' }), success ? '计费发送成功' : '已记录一次计费失败，可继续重试')
}

async function perform(action, success) {
  error.value = ''; message.value = ''
  try { await action(); message.value = success; await load() }
  catch (reason) { error.value = reason.message }
}

onMounted(load)
</script>

<template>
  <header class="page-header"><p class="module-number">模块 7 · 关选课与计费</p><h2>注册管理员工作台</h2><p>依次处理无教师课程、备选补位、低人数取消、课表冻结、计费生成与失败重试。</p></header>
  <p v-if="message" class="success">{{ message }}</p><p v-if="error" class="error">{{ error }}</p>
  <section v-if="overview" class="grid section-gap">
    <article class="card"><p>选课窗口</p><h3>{{ overview.registrationOpen ? '开放' : '关闭' }}</h3></article>
    <article class="card"><p>教学班</p><h3>{{ overview.offeringCount }}</h3></article>
    <article class="card"><p>已提交课表</p><h3>{{ overview.submittedScheduleCount }}</h3></article>
    <article class="card"><p>累计计费</p><h3>¥{{ overview.billedAmount }}</h3></article>
  </section>
  <section class="panel section-gap"><h3>关闭 2026 秋季选课</h3><p class="muted">演示库预置两名背景学生；student1 提交后，四个主选班恰好达到最低 3 人。</p><button class="button danger" :disabled="overview && !overview.registrationOpen" @click="closeRegistration">执行关选课</button>
    <dl v-if="summary" class="summary"><dt>教学班总数</dt><dd>{{ summary.offeringCount }}</dd><dt>取消教学班</dt><dd>{{ summary.cancelledOfferingCount }}</dd><dt>备选补位</dt><dd>{{ summary.promotedAlternateCount }}</dd><dt>冻结课表</dt><dd>{{ summary.finalizedScheduleCount }}</dd><dt>计费记录</dt><dd>{{ summary.billingCount }}</dd><dt>计费合计</dt><dd>¥{{ summary.totalBillingAmount }}</dd></dl>
  </section>
  <h3>计费记录</h3>
  <div class="table-wrap"><table><thead><tr><th>学生</th><th>金额</th><th>状态</th><th>尝试次数</th><th>最后尝试</th><th>操作</th></tr></thead><tbody><tr v-for="billing in billings" :key="billing.id"><td>{{ billing.studentNumber }} {{ billing.studentName }}</td><td>¥{{ billing.amount }}</td><td>{{ billing.status }}</td><td>{{ billing.retryCount }}</td><td>{{ billing.lastAttemptAt || '尚未发送' }}</td><td class="actions"><button class="button danger" @click="attempt(billing.id, false)">模拟失败</button><button class="button" @click="attempt(billing.id, true)">重试成功</button></td></tr></tbody></table><p v-if="!billings.length" class="empty">关选课后生成计费记录。</p></div>
</template>

<style scoped>.section-gap { margin-bottom: 24px; }.summary { display: grid; grid-template-columns: 140px 1fr; gap: 8px; margin-top: 18px; }.summary dd { margin: 0; font-weight: 700; }</style>
