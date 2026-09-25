<script setup>
import { onMounted, reactive, ref } from 'vue'
import { request } from '../api'

const students = ref([])
const professors = ref([])
const error = ref('')
const message = ref('')
const editingStudentId = ref(null)
const editingProfessorId = ref(null)
const defaultStudent = () => ({ studentNumber: '', name: '', dateOfBirth: '2006-01-01', identityNumber: '', status: 'ACTIVE', major: '软件工程', graduationDate: '2030-06-30' })
const defaultProfessor = () => ({ employeeNumber: '', name: '', dateOfBirth: '1980-01-01', identityNumber: '', status: 'ACTIVE', department: '计算机科学与技术学院' })
const studentForm = reactive(defaultStudent())
const professorForm = reactive(defaultProfessor())
const statuses = ['ACTIVE', 'INACTIVE', 'GRADUATED', 'ON_LEAVE']

async function load() {
  error.value = ''
  try {
    ;[students.value, professors.value] = await Promise.all([
      request('/api/people/students'), request('/api/people/professors')
    ])
  } catch (reason) { error.value = reason.message }
}

async function saveStudent() {
  const editing = editingStudentId.value
  await perform(() => request(editing ? `/api/people/students/${editing}` : '/api/people/students', {
    method: editing ? 'PUT' : 'POST', body: JSON.stringify(studentForm)
  }), editing ? '学生档案已更新' : '学生档案已新增')
  editingStudentId.value = null
  Object.assign(studentForm, defaultStudent())
}

async function saveProfessor() {
  const editing = editingProfessorId.value
  await perform(() => request(editing ? `/api/people/professors/${editing}` : '/api/people/professors', {
    method: editing ? 'PUT' : 'POST', body: JSON.stringify(professorForm)
  }), editing ? '教师档案已更新' : '教师档案已新增')
  editingProfessorId.value = null
  Object.assign(professorForm, defaultProfessor())
}

function editStudent(item) {
  editingStudentId.value = item.id
  Object.assign(studentForm, item, { identityNumber: '' })
}

function editProfessor(item) {
  editingProfessorId.value = item.id
  Object.assign(professorForm, item, { identityNumber: '' })
}

async function remove(type, id) {
  if (!window.confirm('确认删除这条档案吗？')) return
  await perform(() => request(`/api/people/${type}/${id}`, { method: 'DELETE' }), '档案已删除')
}

async function perform(action, success) {
  error.value = ''; message.value = ''
  try { await action(); message.value = success; await load() }
  catch (reason) { error.value = reason.message; throw reason }
}

onMounted(load)
</script>

<template>
  <header class="page-header">
    <p class="module-number">模块 2 · 学生与教师档案</p>
    <h2>人员档案维护</h2>
    <p>管理员可完整增删改查；身份证件只返回掩码，修改时留空表示保留原号码。</p>
  </header>
  <p v-if="message" class="success">{{ message }}</p>
  <p v-if="error" class="error">{{ error }}</p>

  <section class="panel section-gap">
    <h3>{{ editingStudentId ? '修改学生' : '新增学生' }}</h3>
    <form class="form-row" @submit.prevent="saveStudent">
      <div class="field"><label>学号</label><input v-model="studentForm.studentNumber" required /></div>
      <div class="field"><label>姓名</label><input v-model="studentForm.name" required /></div>
      <div class="field"><label>出生日期</label><input v-model="studentForm.dateOfBirth" type="date" required /></div>
      <div class="field"><label>身份证件{{ editingStudentId ? '（留空不改）' : '' }}</label><input v-model="studentForm.identityNumber" :required="!editingStudentId" /></div>
      <div class="field"><label>状态</label><select v-model="studentForm.status"><option v-for="status in statuses" :key="status">{{ status }}</option></select></div>
      <div class="field"><label>专业</label><input v-model="studentForm.major" required /></div>
      <div class="field"><label>预计毕业日期</label><input v-model="studentForm.graduationDate" type="date" /></div>
      <button class="button">{{ editingStudentId ? '保存修改' : '新增' }}</button>
      <button v-if="editingStudentId" class="button secondary" type="button" @click="editingStudentId = null; Object.assign(studentForm, defaultStudent())">取消</button>
    </form>
    <div class="table-wrap"><table><thead><tr><th>学号</th><th>姓名</th><th>出生日期</th><th>证件</th><th>状态</th><th>专业</th><th>毕业日期</th><th>操作</th></tr></thead>
      <tbody><tr v-for="item in students" :key="item.id"><td>{{ item.studentNumber }}</td><td>{{ item.name }}</td><td>{{ item.dateOfBirth }}</td><td>{{ item.maskedIdentityNumber }}</td><td>{{ item.status }}</td><td>{{ item.major }}</td><td>{{ item.graduationDate }}</td><td class="actions"><button class="button secondary" @click="editStudent(item)">修改</button><button class="button danger" @click="remove('students', item.id)">删除</button></td></tr></tbody>
    </table></div>
  </section>

  <section class="panel">
    <h3>{{ editingProfessorId ? '修改教师' : '新增教师' }}</h3>
    <form class="form-row" @submit.prevent="saveProfessor">
      <div class="field"><label>工号</label><input v-model="professorForm.employeeNumber" required /></div>
      <div class="field"><label>姓名</label><input v-model="professorForm.name" required /></div>
      <div class="field"><label>出生日期</label><input v-model="professorForm.dateOfBirth" type="date" required /></div>
      <div class="field"><label>身份证件{{ editingProfessorId ? '（留空不改）' : '' }}</label><input v-model="professorForm.identityNumber" :required="!editingProfessorId" /></div>
      <div class="field"><label>状态</label><select v-model="professorForm.status"><option v-for="status in statuses" :key="status">{{ status }}</option></select></div>
      <div class="field"><label>院系</label><input v-model="professorForm.department" required /></div>
      <button class="button">{{ editingProfessorId ? '保存修改' : '新增' }}</button>
      <button v-if="editingProfessorId" class="button secondary" type="button" @click="editingProfessorId = null; Object.assign(professorForm, defaultProfessor())">取消</button>
    </form>
    <div class="table-wrap"><table><thead><tr><th>工号</th><th>姓名</th><th>出生日期</th><th>证件</th><th>状态</th><th>院系</th><th>操作</th></tr></thead>
      <tbody><tr v-for="item in professors" :key="item.id"><td>{{ item.employeeNumber }}</td><td>{{ item.name }}</td><td>{{ item.dateOfBirth }}</td><td>{{ item.maskedIdentityNumber }}</td><td>{{ item.status }}</td><td>{{ item.department }}</td><td class="actions"><button class="button secondary" @click="editProfessor(item)">修改</button><button class="button danger" @click="remove('professors', item.id)">删除</button></td></tr></tbody>
    </table></div>
  </section>
</template>

<style scoped>.section-gap { margin-bottom: 20px; }</style>
