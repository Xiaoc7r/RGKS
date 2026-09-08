<script setup>
import { computed, onMounted, ref } from 'vue'
import { request } from '../api'

const offerings = ref([])
const schedule = ref({ items: [], status: 'DRAFT' })
const choiceType = ref('PRIMARY')
const priority = ref(1)
const error = ref('')
const message = ref('')

const selectedOfferingIds = computed(() => new Set(schedule.value.items.map(item => item.offeringId)))
const primaryCount = computed(() => schedule.value.items.filter(item => item.choiceType === 'PRIMARY').length)
const alternateCount = computed(() => schedule.value.items.filter(item => item.choiceType === 'ALTERNATE').length)

async function load() {
  error.value = ''
  try {
    ;[offerings.value, schedule.value] = await Promise.all([
      request('/api/catalog/offerings?semester=2026-FALL'),
      request('/api/registrations/my-schedule?semester=2026-FALL')
    ])
  } catch (reason) {
    error.value = reason.message
  }
}

async function add(offeringId) {
  await perform(() => request('/api/registrations/selections', {
    method: 'POST',
    body: JSON.stringify({ semester: '2026-FALL', offeringId, choiceType: choiceType.value, priority: Number(priority.value) })
  }), '课程已加入课表')
}

async function remove(itemId) {
  await perform(() => request(`/api/registrations/selections/${itemId}?semester=2026-FALL`, { method: 'DELETE' }), '选课记录已删除')
}

async function submitSchedule() {
  await perform(() => request('/api/registrations/submit?semester=2026-FALL', { method: 'POST' }), '课表提交成功')
}

async function perform(action, success) {
  error.value = ''
  message.value = ''
  try {
    await action()
    message.value = success
    await load()
  } catch (reason) {
    error.value = reason.message
  }
}

onMounted(load)
</script>

<template>
  <header class="page-header">
    <p class="module-number">模块 4 · 学生选课与课表</p>
    <h2>我的 2026 秋季课表</h2>
    <p>提交条件：4 门主选课和 2 门备选课。后端负责先修课、容量和时间冲突校验。</p>
  </header>
  <div class="summary-line">
    <strong>主选 {{ primaryCount }}/4</strong>
    <strong>备选 {{ alternateCount }}/2</strong>
    <span>课表状态：{{ schedule.status }}</span>
  </div>
  <div class="form-row">
    <div class="field"><label>本次加入类型</label><select v-model="choiceType"><option>PRIMARY</option><option>ALTERNATE</option></select></div>
    <div class="field"><label>优先级</label><select v-model="priority"><option v-for="n in 4" :key="n" :value="n">{{ n }}</option></select></div>
    <button class="button" :disabled="primaryCount !== 4 || alternateCount !== 2" @click="submitSchedule">提交课表</button>
  </div>
  <p v-if="message" class="success">{{ message }}</p>
  <p v-if="error" class="error">{{ error }}</p>

  <div class="table-wrap section-gap">
    <table><thead><tr><th>课程</th><th>名称</th><th>类型</th><th>优先级</th><th>状态</th><th>操作</th></tr></thead>
      <tbody><tr v-for="item in schedule.items" :key="item.itemId">
        <td>{{ item.courseCode }}</td><td>{{ item.courseName }}</td><td>{{ item.choiceType }}</td><td>{{ item.priority }}</td><td>{{ item.status }}</td>
        <td><button class="button danger" @click="remove(item.itemId)">移除</button></td>
      </tr></tbody>
    </table>
    <p v-if="!schedule.items.length" class="empty">课表还是空的，请从下方课程目录加入。</p>
  </div>

  <h3>可选教学班</h3>
  <div class="table-wrap">
    <table><thead><tr><th>课程</th><th>名称</th><th>教师</th><th>时间</th><th>余量</th><th>操作</th></tr></thead>
      <tbody><tr v-for="item in offerings" :key="item.offeringId">
        <td>{{ item.courseCode }}</td><td>{{ item.courseName }}</td><td>{{ item.professorName }}</td>
        <td>周{{ item.dayOfWeek }} 第 {{ item.startPeriod }}-{{ item.endPeriod }} 节</td>
        <td>{{ item.capacity - item.enrolled }}</td>
        <td><button class="button" :disabled="selectedOfferingIds.has(item.offeringId) || item.status !== 'OPEN'" @click="add(item.offeringId)">加入</button></td>
      </tr></tbody>
    </table>
  </div>
</template>

<style scoped>
.summary-line { display: flex; flex-wrap: wrap; gap: 20px; margin-bottom: 18px; padding: 14px 18px; background: white; border: 1px solid #dce4ed; border-radius: 8px; }
.section-gap { margin-bottom: 24px; }
</style>
