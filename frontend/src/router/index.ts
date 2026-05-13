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
        meta: { title: '首页' },
      },
      {
        path: 'resume',
        name: 'Resume',
        component: () => import('@/views/resume/index.vue'),
        meta: { title: '简历管理' },
      },
      {
        path: 'interview',
        name: 'Interview',
        component: () => import('@/views/interview/index.vue'),
        meta: { title: 'AI面试' },
      },
      {
        path: 'interview/:id',
        name: 'InterviewDetail',
        component: () => import('@/views/interview/detail.vue'),
        meta: { title: '面试详情' },
      },
      {
        path: 'product',
        name: 'Product',
        component: () => import('@/views/product/index.vue'),
        meta: { title: '套餐中心' },
      },
      {
        path: 'order',
        name: 'Order',
        component: () => import('@/views/order/index.vue'),
        meta: { title: '我的订单' },
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
