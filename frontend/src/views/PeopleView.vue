<script setup>
import { onMounted, reactive, ref } from 'vue'
import { request } from '../api'

const students = ref([])
const professors = ref([])
const error = ref('')
const message = ref('')
const studentForm = reactive({ studentNumber: '', name: '', major: '软件工程', graduationDate: '2030-06-30' })
const professorForm = reactive({ employeeNumber: '', name: '', department: '计算机科学与技术学院' })

async function load() {
  error.value = ''
  try {
    ;[students.value, professors.value] = await Promise.all([
      request('/api/people/students'),
      request('/api/people/professors')
    ])
  } catch (reason) {
    error.value = reason.message
  }
}

async function createStudent() {
  await perform(async () => {
    await request('/api/people/students', { method: 'POST', body: JSON.stringify(studentForm) })
    Object.assign(studentForm, { studentNumber: '', name: '', major: '软件工程', graduationDate: '2030-06-30' })
  }, '学生档案已新增')
}

async function createProfessor() {
  await perform(async () => {
    await request('/api/people/professors', { method: 'POST', body: JSON.stringify(professorForm) })
    Object.assign(professorForm, { employeeNumber: '', name: '', department: '计算机科学与技术学院' })
  }, '教师档案已新增')
}

async function remove(type, id) {
  await perform(() => request(`/api/people/${type}/${id}`, { method: 'DELETE' }), '档案已删除')
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
    <p class="module-number">模块 2 · 学生与教师档案</p>
    <h2>人员档案维护</h2>
    <p>仅注册管理员可以调用这些接口；第一版提供新增、查询和删除，后端同时保留修改接口。</p>
  </header>
  <p v-if="message" class="success">{{ message }}</p>
  <p v-if="error" class="error">{{ error }}</p>

  <section class="panel section-gap">
    <h3>新增学生</h3>
    <form class="form-row" @submit.prevent="createStudent">
      <div class="field"><label>学号</label><input v-model="studentForm.studentNumber" required /></div>
      <div class="field"><label>姓名</label><input v-model="studentForm.name" required /></div>
      <div class="field"><label>专业</label><input v-model="studentForm.major" required /></div>
      <div class="field"><label>预计毕业日期</label><input v-model="studentForm.graduationDate" type="date" /></div>
      <button class="button">新增</button>
    </form>
    <div class="table-wrap">
      <table><thead><tr><th>学号</th><th>姓名</th><th>专业</th><th>毕业日期</th><th>操作</th></tr></thead>
        <tbody><tr v-for="student in students" :key="student.id">
          <td>{{ student.studentNumber }}</td><td>{{ student.name }}</td><td>{{ student.major }}</td><td>{{ student.graduationDate }}</td>
          <td><button class="button danger" @click="remove('students', student.id)">删除</button></td>
        </tr></tbody>
      </table>
    </div>
  </section>

  <section class="panel">
    <h3>新增教师</h3>
    <form class="form-row" @submit.prevent="createProfessor">
      <div class="field"><label>工号</label><input v-model="professorForm.employeeNumber" required /></div>
      <div class="field"><label>姓名</label><input v-model="professorForm.name" required /></div>
      <div class="field"><label>院系</label><input v-model="professorForm.department" required /></div>
      <button class="button">新增</button>
    </form>
    <div class="table-wrap">
      <table><thead><tr><th>工号</th><th>姓名</th><th>院系</th><th>操作</th></tr></thead>
        <tbody><tr v-for="professor in professors" :key="professor.id">
          <td>{{ professor.employeeNumber }}</td><td>{{ professor.name }}</td><td>{{ professor.department }}</td>
          <td><button class="button danger" @click="remove('professors', professor.id)">删除</button></td>
        </tr></tbody>
      </table>
    </div>
  </section>
</template>

<style scoped>
.section-gap { margin-bottom: 20px; }
</style>

