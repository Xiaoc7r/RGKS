import { createRouter, createWebHistory } from 'vue-router'
import { useAuth } from './auth'
import LoginView from './views/LoginView.vue'
import DashboardView from './views/DashboardView.vue'
import AccountView from './views/AccountView.vue'
import PeopleView from './views/PeopleView.vue'
import CatalogView from './views/CatalogView.vue'
import RegistrationView from './views/RegistrationView.vue'
import TeachingView from './views/TeachingView.vue'
import GradingView from './views/GradingView.vue'
import OperationsView from './views/OperationsView.vue'
import ReportCardView from './views/ReportCardView.vue'

const routes = [
  { path: '/login', component: LoginView, meta: { public: true } },
  { path: '/', component: DashboardView },
  { path: '/account', component: AccountView },
  { path: '/people', component: PeopleView, meta: { roles: ['REGISTRAR'] } },
  { path: '/catalog', component: CatalogView },
  { path: '/registration', component: RegistrationView, meta: { roles: ['STUDENT'] } },
  { path: '/teaching', component: TeachingView, meta: { roles: ['PROFESSOR'] } },
  { path: '/grading', component: GradingView, meta: { roles: ['PROFESSOR'] } },
  { path: '/operations', component: OperationsView, meta: { roles: ['REGISTRAR'] } },
  { path: '/report-card', component: ReportCardView, meta: { roles: ['STUDENT'] } }
]

const router = createRouter({ history: createWebHistory(), routes })

router.beforeEach((to) => {
  const { state } = useAuth()
  if (!to.meta.public && !state.user) return '/login'
  if (to.path === '/login' && state.user) return '/'
  if (to.meta.roles && !to.meta.roles.includes(state.user?.role)) return '/'
})

export default router
