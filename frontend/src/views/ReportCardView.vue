<script setup>
import { onMounted, ref } from 'vue'
import { request } from '../api'

const grades = ref([])
const error = ref('')

async function load() {
  try {
    grades.value = await request('/api/teaching/my-report-card')
  } catch (reason) {
    error.value = reason.message
  }
}

onMounted(load)
</script>

<template>
  <header class="page-header">
    <p class="module-number">模块 5 · 教师任课与成绩</p>
    <h2>我的成绩单</h2>
    <p>学生只能查看与本人账号关联的成绩记录。</p>
  </header>
  <p v-if="error" class="error">{{ error }}</p>
  <div class="table-wrap">
    <table><thead><tr><th>学期</th><th>课程编号</th><th>课程名称</th><th>成绩</th></tr></thead>
      <tbody><tr v-for="item in grades" :key="`${item.semester}-${item.courseCode}`">
        <td>{{ item.semester }}</td><td>{{ item.courseCode }}</td><td>{{ item.courseName }}</td><td class="status">{{ item.gradeValue }}</td>
      </tr></tbody>
    </table>
    <p v-if="!grades.length && !error" class="empty">暂无成绩信息。</p>
  </div>
</template>
