<script setup>
import { onMounted, ref } from 'vue'
import { request } from '../api'

const offerings = ref([])
const roster = ref([])
const selectedOffering = ref(null)
const error = ref('')
const message = ref('')

async function load() {
  try { offerings.value = await request('/api/grading/my-offerings?semester=2026-SPRING') }
  catch (reason) { error.value = reason.message }
}

async function showRoster(offering) {
  selectedOffering.value = offering; error.value = ''
  try { roster.value = await request(`/api/grading/offerings/${offering.offeringId}/roster`) }
  catch (reason) { error.value = reason.message }
}

async function saveGrade(student) {
  error.value = ''; message.value = ''
  try {
    await request('/api/grading/grades', { method: 'PUT', body: JSON.stringify({ offeringId: selectedOffering.value.offeringId, studentId: student.studentId, gradeValue: student.gradeValue || '' }) })
    message.value = student.gradeValue ? '成绩已保存' : '空成绩已保留为未录入'
    await showRoster(selectedOffering.value)
  } catch (reason) { error.value = reason.message }
}

onMounted(load)
</script>

<template>
  <header class="page-header"><p class="module-number">模块 6 · 成绩与成绩单</p><h2>教师成绩管理</h2><p>仅能为本人已结束的教学班录入 A、B、C、D、F、I；空值代表暂未录入。</p></header>
  <p v-if="message" class="success">{{ message }}</p><p v-if="error" class="error">{{ error }}</p>
  <div class="table-wrap section-gap"><table><thead><tr><th>学期</th><th>课程</th><th>名称</th><th>人数</th><th>状态</th><th>操作</th></tr></thead><tbody><tr v-for="item in offerings" :key="item.offeringId"><td>{{ item.semester }}</td><td>{{ item.courseCode }}</td><td>{{ item.courseName }}</td><td>{{ item.enrolled }}</td><td>{{ item.status }}</td><td><button class="button" @click="showRoster(item)">录入成绩</button></td></tr></tbody></table></div>
  <section v-if="selectedOffering" class="panel"><h3>{{ selectedOffering.courseName }} 成绩册</h3><div class="table-wrap"><table><thead><tr><th>学号</th><th>姓名</th><th>成绩</th><th>操作</th></tr></thead><tbody><tr v-for="student in roster" :key="student.studentId"><td>{{ student.studentNumber }}</td><td>{{ student.studentName }}</td><td><select v-model="student.gradeValue"><option value="">未录入</option><option v-for="grade in ['A','B','C','D','F','I']" :key="grade">{{ grade }}</option></select></td><td><button class="button" @click="saveGrade(student)">保存</button></td></tr></tbody></table></div></section>
</template>

<style scoped>.section-gap { margin-bottom: 24px; } select { min-width: 110px; padding: 7px; }</style>
