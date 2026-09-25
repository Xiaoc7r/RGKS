<script setup>
import { onMounted, ref } from 'vue'
import { request } from '../api'

const report = ref({ items: [], attemptedCredits: 0, earnedCredits: 0, gradePointAverage: 0 })
const error = ref('')

async function load() {
  try { report.value = await request('/api/grading/my-report-card?semester=2026-SPRING') }
  catch (reason) { error.value = reason.message }
}
onMounted(load)
</script>

<template>
  <header class="page-header"><p class="module-number">模块 6 · 成绩与成绩单</p><h2>我的成绩单</h2><p>学生只能查看本人正式成绩；I 表示暂缓，GPA 按 A=4、B=3、C=2、D=1、F=0 计算。</p></header>
  <p v-if="error" class="error">{{ error }}</p>
  <div class="summary-line"><strong>{{ report.studentNumber }} {{ report.studentName }}</strong><span>学期：{{ report.semester }}</span><span>修读学分：{{ report.attemptedCredits }}</span><span>获得学分：{{ report.earnedCredits }}</span><strong>GPA：{{ report.gradePointAverage }}</strong></div>
  <div class="table-wrap"><table><thead><tr><th>学期</th><th>课程编号</th><th>课程名称</th><th>学分</th><th>成绩</th></tr></thead><tbody><tr v-for="item in report.items" :key="`${item.semester}-${item.courseCode}`"><td>{{ item.semester }}</td><td>{{ item.courseCode }}</td><td>{{ item.courseName }}</td><td>{{ item.credits }}</td><td class="status">{{ item.gradeValue }}</td></tr></tbody></table><p v-if="!report.items.length && !error" class="empty">暂无成绩信息。</p></div>
</template>

<style scoped>.summary-line { display: flex; flex-wrap: wrap; gap: 22px; margin-bottom: 18px; padding: 14px 18px; background: white; border: 1px solid #dce4ed; border-radius: 8px; }</style>
