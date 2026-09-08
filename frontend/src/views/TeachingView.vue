<script setup>
import { onMounted, ref } from 'vue'
import { request } from '../api'

const mine = ref([])
const catalog = ref([])
const roster = ref([])
const selectedOffering = ref(null)
const error = ref('')
const message = ref('')

async function load() {
  error.value = ''
  try {
    ;[mine.value, catalog.value] = await Promise.all([
      request('/api/teaching/my-offerings?semester=2026-FALL'),
      request('/api/catalog/offerings?semester=2026-FALL')
    ])
  } catch (reason) {
    error.value = reason.message
  }
}

async function selectOffering(id) {
  await perform(() => request(`/api/teaching/offerings/${id}/select`, { method: 'POST' }), '任课安排已更新')
}

async function showRoster(offering) {
  selectedOffering.value = offering
  error.value = ''
  try {
    roster.value = await request(`/api/teaching/offerings/${offering.offeringId}/roster`)
  } catch (reason) {
    error.value = reason.message
  }
}

async function saveGrade(student) {
  await perform(() => request('/api/teaching/grades', {
    method: 'PUT',
    body: JSON.stringify({ offeringId: selectedOffering.value.offeringId, studentId: student.studentId, gradeValue: student.gradeValue })
  }), '成绩已保存', false)
  await showRoster(selectedOffering.value)
}

async function perform(action, success, reload = true) {
  error.value = ''
  message.value = ''
  try {
    await action()
    message.value = success
    if (reload) await load()
  } catch (reason) {
    error.value = reason.message
  }
}

onMounted(load)
</script>

<template>
  <header class="page-header">
    <p class="module-number">模块 5 · 教师任课与成绩</p>
    <h2>教师教学工作台</h2>
    <p>教师只能维护自己任教的教学班和其中已选课学生的成绩。</p>
  </header>
  <p v-if="message" class="success">{{ message }}</p>
  <p v-if="error" class="error">{{ error }}</p>

  <h3>我的任课</h3>
  <div class="table-wrap section-gap">
    <table><thead><tr><th>课程</th><th>名称</th><th>时间</th><th>人数</th><th>操作</th></tr></thead>
      <tbody><tr v-for="item in mine" :key="item.offeringId">
        <td>{{ item.courseCode }}</td><td>{{ item.courseName }}</td>
        <td>周{{ item.dayOfWeek }} 第 {{ item.startPeriod }}-{{ item.endPeriod }} 节</td><td>{{ item.enrolled }}</td>
        <td><button class="button" @click="showRoster(item)">查看名单</button></td>
      </tr></tbody>
    </table>
  </div>

  <section v-if="selectedOffering" class="panel section-gap">
    <h3>{{ selectedOffering.courseName }} 学生名单</h3>
    <div class="table-wrap">
      <table><thead><tr><th>学号</th><th>姓名</th><th>成绩</th><th>操作</th></tr></thead>
        <tbody><tr v-for="student in roster" :key="student.studentId">
          <td>{{ student.studentNumber }}</td><td>{{ student.studentName }}</td>
          <td><select v-model="student.gradeValue"><option value="">未录入</option><option v-for="grade in ['A','B','C','D','F','I']" :key="grade">{{ grade }}</option></select></td>
          <td><button class="button" :disabled="!student.gradeValue" @click="saveGrade(student)">保存成绩</button></td>
        </tr></tbody>
      </table>
      <p v-if="!roster.length" class="empty">当前教学班还没有已提交选课的学生。</p>
    </div>
  </section>

  <h3>选择任课教学班</h3>
  <div class="table-wrap">
    <table><thead><tr><th>课程</th><th>名称</th><th>现任教师</th><th>时间</th><th>操作</th></tr></thead>
      <tbody><tr v-for="item in catalog" :key="item.offeringId">
        <td>{{ item.courseCode }}</td><td>{{ item.courseName }}</td><td>{{ item.professorName }}</td>
        <td>周{{ item.dayOfWeek }} 第 {{ item.startPeriod }}-{{ item.endPeriod }} 节</td>
        <td><button class="button" :disabled="item.status !== 'OPEN'" @click="selectOffering(item.offeringId)">选择</button></td>
      </tr></tbody>
    </table>
  </div>
</template>

<style scoped>
.section-gap { margin-bottom: 24px; }
select { min-width: 100px; padding: 7px; }
</style>

