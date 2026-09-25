import { computed, reactive } from 'vue'
import { request } from './api'

const state = reactive({
  user: JSON.parse(sessionStorage.getItem('course-user') || 'null')
})

export function useAuth() {
  const loggedIn = computed(() => Boolean(state.user))

  async function login(username, password) {
    const token = btoa(`${username}:${password}`)
    const user = await request('/api/auth/login', { method: 'POST', authToken: token })
    sessionStorage.setItem('course-auth', token)
    sessionStorage.setItem('course-user', JSON.stringify(user))
    state.user = user
    return user
  }

  function logout() {
    sessionStorage.removeItem('course-auth')
    sessionStorage.removeItem('course-user')
    state.user = null
  }

  return { state, loggedIn, login, logout }
}
