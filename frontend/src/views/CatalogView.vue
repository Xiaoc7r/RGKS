<script setup>
import { onMounted, ref } from 'vue'
import { request } from '../api'

const offerings = ref([])
const error = ref('')

async function load() {
  error.value = ''
  try {
    offerings.value = await request('/api/catalog/offerings?semester=2026-FALL')
  } catch (reason) {
    error.value = reason.message
  }
}

onMounted(load)
</script>

<template>
  <header class="page-header">
    <p class="module-number">模块 3 · 课程目录与教学班</p>
    <h2>2026 秋季课程目录</h2>
    <p>新系统只读课程目录；示例数据模拟旧课程目录系统提供的开放 SQL 接口。</p>
  </header>
  <p v-if="error" class="error">{{ error }}</p>
  <div class="table-wrap">
    <table>
      <thead><tr><th>课程</th><th>名称</th><th>教师</th><th>时间</th><th>容量</th><th>状态</th></tr></thead>
      <tbody>
        <tr v-for="item in offerings" :key="item.offeringId">
          <td>{{ item.courseCode }}</td>
          <td>{{ item.courseName }}</td>
          <td>{{ item.professorName }}</td>
          <td>周{{ item.dayOfWeek }} 第 {{ item.startPeriod }}-{{ item.endPeriod }} 节</td>
          <td>{{ item.enrolled }}/{{ item.capacity }}</td>
          <td class="status">{{ item.status }}</td>
        </tr>
      </tbody>
    </table>
    <p v-if="!offerings.length && !error" class="empty">暂无教学班</p>
  </div>
</template>
