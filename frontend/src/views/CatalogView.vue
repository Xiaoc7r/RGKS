<script setup>
import { onMounted, ref } from 'vue'
import { request } from '../api'

const offerings = ref([])
const catalogStatus = ref(null)
const error = ref('')

async function load() {
  error.value = ''
  try {
    ;[offerings.value, catalogStatus.value] = await Promise.all([
      request('/api/catalog/offerings?semester=2026-FALL'), request('/api/catalog/status')
    ])
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
    <p>通过适配层只读访问旧课程目录；本模块不提供任何课程目录写接口。</p>
  </header>
  <p v-if="catalogStatus" class="notice">数据源：{{ catalogStatus.source }} · 只读：{{ catalogStatus.readOnly ? '是' : '否' }} · 响应预算：{{ catalogStatus.latencyBudgetSeconds }} 秒</p>
  <p v-if="error" class="error">{{ error }}</p>
  <div class="table-wrap">
    <table>
      <thead><tr><th>课程</th><th>名称</th><th>院系</th><th>学分</th><th>先修课</th><th>学费</th><th>教师</th><th>时间</th><th>容量/余量</th><th>状态</th></tr></thead>
      <tbody>
        <tr v-for="item in offerings" :key="item.offeringId">
          <td>{{ item.courseCode }}</td>
          <td>{{ item.courseName }}</td>
          <td>{{ item.department }}</td><td>{{ item.credits }}</td><td>{{ item.prerequisiteCourseCode || '无' }}</td><td>¥{{ item.tuition }}</td>
          <td>{{ item.professorName || '待教师选择' }}</td>
          <td>周{{ item.dayOfWeek }} 第 {{ item.startPeriod }}-{{ item.endPeriod }} 节</td>
          <td>{{ item.enrolled }}/{{ item.capacity }}（余 {{ item.remainingSeats }}）</td>
          <td class="status">{{ item.status }}</td>
        </tr>
      </tbody>
    </table>
    <p v-if="!offerings.length && !error" class="empty">暂无教学班</p>
  </div>
</template>

<style scoped>.notice { padding: 10px 14px; background: #e8f1f8; border-radius: 7px; color: #31566f; }</style>
