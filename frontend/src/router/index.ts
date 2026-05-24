import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/store/user'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { requiresAuth: false },
  },
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/home',
    children: [
      {
        path: 'home',
        name: 'Home',
        component: () => import('@/views/home/index.vue'),
        meta: { title: '首页', keepAlive: true },
      },
      {
        path: 'resume',
        name: 'Resume',
        component: () => import('@/views/resume/index.vue'),
        meta: { title: '简历管理', keepAlive: true },
      },
      {
        path: 'interview',
        name: 'Interview',
        component: () => import('@/views/interview/index.vue'),
        meta: { title: 'AI面试', keepAlive: true },
      },
      {
        path: 'interview/create',
        name: 'InterviewCreate',
        component: () => import('@/views/interview/create.vue'),
        meta: { title: '创建面试' },
      },
      {
        path: 'interview/conduct/:id',
        name: 'InterviewConduct',
        component: () => import('@/views/interview/conduct.vue'),
        meta: { title: '面试进行中' },
      },
      {
        path: 'interview/detail/:id',
        name: 'InterviewDetail',
        component: () => import('@/views/interview/detail.vue'),
        meta: { title: '面试详情' },
      },
      {
        path: 'interview/report/:id',
        name: 'InterviewReport',
        component: () => import('@/views/interview/report.vue'),
        meta: { title: '面试评价报告' },
      },
      {
        path: 'product',
        name: 'Product',
        component: () => import('@/views/product/index.vue'),
        meta: { title: '套餐中心', keepAlive: true },
      },
      {
        path: 'order',
        name: 'Order',
        component: () => import('@/views/order/index.vue'),
        meta: { title: '我的订单', keepAlive: true },
      },
      {
        path: 'user',
        name: 'User',
        component: () => import('@/views/user/index.vue'),
        meta: { title: '个人中心' },
      },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

// 路由守卫
router.beforeEach((to, _from, next) => {
  if (to.meta.requiresAuth === false) {
    next()
    return
  }

  // 同时检查 Pinia store 和 localStorage，避免响应式时序问题
  const userStore = useUserStore()
  const hasToken = userStore.isLoggedIn() || !!localStorage.getItem('token')

  if (!hasToken) {
    next({ path: '/login', query: { redirect: to.fullPath } })
    return
  }

  next()
})

export default router
