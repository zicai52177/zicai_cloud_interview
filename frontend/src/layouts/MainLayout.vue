<template>
  <el-container class="main-layout">
    <el-header class="header">
      <div class="logo">
        <h1>云面试系统</h1>
      </div>
      <el-menu
        :default-active="activeMenu"
        mode="horizontal"
        router
        class="nav-menu"
      >
        <el-menu-item index="/home">首页</el-menu-item>
        <el-menu-item index="/resume">简历管理</el-menu-item>
        <el-menu-item index="/interview">AI面试</el-menu-item>
        <el-menu-item index="/product">套餐中心</el-menu-item>
        <el-menu-item index="/order">我的订单</el-menu-item>
      </el-menu>
      <div class="user-area">
        <el-dropdown trigger="hover" :hide-timeout="200">
          <span class="user-info">
            <el-avatar :size="32" :src="userStore.userInfo?.headImg" />
            <span class="username">{{ userStore.userInfo?.username || '用户' }}</span>
            <el-icon class="el-icon--right"><arrow-down /></el-icon>
          </span>
          <template #dropdown>
            <!-- 悬停气泡：显示用户基本信息 -->
            <div class="user-hover-card">
              <div class="hover-header">
                <el-avatar :size="48" :src="userStore.userInfo?.headImg" />
                <div class="hover-info">
                  <div class="hover-username">{{ userStore.userInfo?.username || '用户' }}</div>
                  <div class="hover-id">ID: {{ userStore.userInfo?.id }}</div>
                </div>
              </div>
              <div class="hover-divider"></div>
              <div class="hover-stats">
                <div class="stat-item">
                  <span class="stat-label">手机</span>
                  <span class="stat-value">{{ userStore.userInfo?.phone || '未绑定' }}</span>
                </div>
                <div class="stat-item">
                  <span class="stat-label">注册</span>
                  <span class="stat-value">{{ formatDate(userStore.userInfo?.gmtCreate) }}</span>
                </div>
              </div>
              <div class="hover-actions">
                <el-button type="primary" link @click="$router.push('/user')">查看详情</el-button>
              </div>
            </div>
          </template>
        </el-dropdown>
      </div>
    </el-header>
    <el-main class="main-content">
      <router-view />
    </el-main>
  </el-container>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowDown } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)

// 组件挂载时刷新用户信息
onMounted(() => {
  userStore.refreshUserInfo()
})

// 格式化日期
function formatDate(dateStr: string | undefined): string {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

function handleLogout() {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.main-layout {
  min-height: 100vh;
}

.header {
  display: flex;
  align-items: center;
  border-bottom: 1px solid #e4e7ed;
  padding: 0 20px;
}

.logo h1 {
  font-size: 18px;
  color: #409eff;
  margin: 0;
  white-space: nowrap;
}

.nav-menu {
  flex: 1;
  margin-left: 40px;
  border-bottom: none;
}

.user-area {
  margin-left: auto;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.username {
  font-size: 14px;
  color: #606266;
}

.main-content {
  background: #f5f7fa;
  padding: 20px;
}

/* 悬停气泡样式 */
.user-hover-card {
  padding: 16px;
  min-width: 220px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
}

.hover-header {
  display: flex;
  align-items: center;
  gap: 12px;
}

.hover-info {
  flex: 1;
}

.hover-username {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.hover-id {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}

.hover-divider {
  height: 1px;
  background: #ebeef5;
  margin: 12px 0;
}

.hover-stats {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.stat-item {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
}

.stat-label {
  color: #909399;
}

.stat-value {
  color: #606266;
}

.hover-actions {
  margin-top: 12px;
  text-align: center;
}
</style>
