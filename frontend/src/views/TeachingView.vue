<script setup>
import { onMounted, ref } from 'vue'
import { request } from '../api'

const mine = ref([])
const eligible = ref([])
const roster = ref([])
const selectedOffering = ref(null)
const error = ref('')
const message = ref('')

async function load() {
  error.value = ''
  try {
    ;[mine.value, eligible.value] = await Promise.all([
      request('/api/teaching/my-offerings?semester=2026-FALL'),
      request('/api/teaching/eligible-offerings?semester=2026-FALL')
    ])
  } catch (reason) { error.value = reason.message }
}

async function selectOffering(id) { await perform(() => request(`/api/teaching/offerings/${id}/select`, { method: 'POST' }), '任课教学班已选择') }
async function deselectOffering(id) { await perform(() => request(`/api/teaching/offerings/${id}/selection`, { method: 'DELETE' }), '已退选该教学班') }

async function showRoster(offering) {
  selectedOffering.value = offering; error.value = ''
  try { roster.value = await request(`/api/teaching/offerings/${offering.offeringId}/roster`) }
  catch (reason) { error.value = reason.message }
}

async function perform(action, success) {
  error.value = ''; message.value = ''
  try { await action(); message.value = success; selectedOffering.value = null; roster.value = []; await load() }
  catch (reason) { error.value = reason.message }
}

onMounted(load)
</script>

<template>
  <header class="page-header">
    <p class="module-number">模块 5 · 教师任课与名单</p>
    <h2>教师任课工作台</h2>
    <p>教师只能选择本院系且无人任教的教学班；系统检查时间冲突，选课关闭后禁止调整。</p>
  </header>
  <p v-if="message" class="success">{{ message }}</p><p v-if="error" class="error">{{ error }}</p>

  <h3>我的任课</h3>
  <div class="table-wrap section-gap"><table><thead><tr><th>课程</th><th>名称</th><th>院系</th><th>时间</th><th>人数</th><th>状态</th><th>操作</th></tr></thead>
    <tbody><tr v-for="item in mine" :key="item.offeringId"><td>{{ item.courseCode }}</td><td>{{ item.courseName }}</td><td>{{ item.department }}</td><td>周{{ item.dayOfWeek }} 第 {{ item.startPeriod }}-{{ item.endPeriod }} 节</td><td>{{ item.enrolled }}</td><td>{{ item.status }}</td><td class="actions"><button class="button" @click="showRoster(item)">查看名单</button><button class="button danger" :disabled="item.status !== 'OPEN'" @click="deselectOffering(item.offeringId)">退选任课</button></td></tr></tbody>
  </table></div>

  <section v-if="selectedOffering" class="panel section-gap">
    <h3>{{ selectedOffering.courseName }} 学生名单</h3>
    <div class="table-wrap"><table><thead><tr><th>学生 ID</th><th>学号</th><th>姓名</th></tr></thead><tbody><tr v-for="student in roster" :key="student.studentId"><td>{{ student.studentId }}</td><td>{{ student.studentNumber }}</td><td>{{ student.studentName }}</td></tr></tbody></table><p v-if="!roster.length" class="empty">当前教学班还没有已提交选课的学生。</p></div>
  </section>

  <h3>符合条件的教学班</h3>
  <div class="table-wrap"><table><thead><tr><th>课程</th><th>名称</th><th>时间</th><th>当前归属</th><th>操作</th></tr></thead>
    <tbody><tr v-for="item in eligible" :key="item.offeringId"><td>{{ item.courseCode }}</td><td>{{ item.courseName }}</td><td>周{{ item.dayOfWeek }} 第 {{ item.startPeriod }}-{{ item.endPeriod }} 节</td><td>{{ item.ownedByMe ? '我的课程' : '待选择' }}</td><td><button class="button" :disabled="item.ownedByMe" @click="selectOffering(item.offeringId)">选择任课</button></td></tr></tbody>
  </table></div>
</template>

<style scoped>.section-gap { margin-bottom: 24px; }</style>
