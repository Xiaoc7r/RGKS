const baseUrl = import.meta.env.VITE_API_BASE_URL || ''

export async function request(path, options = {}) {
  const token = options.authToken || localStorage.getItem('course-auth')
  const headers = new Headers(options.headers || {})
  if (token) headers.set('Authorization', `Basic ${token}`)
  if (options.body && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json')
  }
  const response = await fetch(`${baseUrl}${path}`, { ...options, headers })
  if (!response.ok) {
    const payload = await response.json().catch(() => ({}))
    throw new Error(payload.message || `请求失败：${response.status}`)
  }
  if (response.status === 204) return null
  return response.json()
}

