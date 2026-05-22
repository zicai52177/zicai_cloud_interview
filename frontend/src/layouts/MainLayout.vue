<template>
  <el-container class="main-layout">
    <el-header class="header">
      <div class="logo">
        <el-icon class="logo-icon"><Microphone /></el-icon>
        <h1>云面试系统</h1>
      </div>
      <el-menu
        :default-active="activeMenu"
        mode="horizontal"
        router
        class="nav-menu"
        :ellipsis="false"
      >
        <el-menu-item index="/home">
          <el-icon><HomeFilled /></el-icon>
          <span>首页</span>
        </el-menu-item>
        <el-menu-item index="/resume">
          <el-icon><Document /></el-icon>
          <span>简历管理</span>
        </el-menu-item>
        <el-menu-item index="/interview">
          <el-icon><ChatDotRound /></el-icon>
          <span>AI面试</span>
        </el-menu-item>
        <el-menu-item index="/product">
          <el-icon><Goods /></el-icon>
          <span>套餐中心</span>
        </el-menu-item>
        <el-menu-item index="/order">
          <el-icon><Tickets /></el-icon>
          <span>我的订单</span>
        </el-menu-item>
      </el-menu>
      <div class="user-area">
        <el-dropdown trigger="click" :hide-timeout="200">
          <span class="user-info">
            <el-avatar :size="36" :src="userStore.userInfo?.headImg">
              {{ userStore.userInfo?.username?.charAt(0) || '用' }}
            </el-avatar>
            <span class="username">{{ userStore.userInfo?.username || '用户' }}</span>
            <el-icon class="el-icon--right"><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <!-- 悬停气泡：显示用户基本信息 -->
            <div class="user-hover-card">
              <div class="hover-header">
                <el-avatar :size="56" :src="userStore.userInfo?.headImg">
                  {{ userStore.userInfo?.username?.charAt(0) || '用' }}
                </el-avatar>
                <div class="hover-info">
                  <div class="hover-username">{{ userStore.userInfo?.username || '用户' }}</div>
                  <el-tag :type="userStore.userInfo?.role === 'ADMIN' ? 'danger' : 'info'" size="small" effect="dark">
                    {{ userStore.userInfo?.role === 'ADMIN' ? '超级会员' : '普通用户' }}
                  </el-tag>
                </div>
              </div>
              <div class="hover-divider"></div>
              <div class="hover-stats">
                <div class="stat-item">
                  <span class="stat-label">手机</span>
                  <span class="stat-value">{{ maskPhone(userStore.userInfo?.phone) }}</span>
                </div>
                <div class="stat-item">
                  <span class="stat-label">注册</span>
                  <span class="stat-value">{{ formatDate(userStore.userInfo?.gmtCreate) }}</span>
                </div>
              </div>
              <div class="hover-divider"></div>
              <div class="hover-actions">
                <el-button type="primary" plain size="small" @click="$router.push('/user')">
                  <el-icon><User /></el-icon>
                  个人中心
                </el-button>
                <el-button type="danger" plain size="small" @click="handleLogout">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-button>
              </div>
            </div>
          </template>
        </el-dropdown>
      </div>
    </el-header>
    <el-main class="main-content">
      <router-view v-slot="{ Component, route }">
        <keep-alive>
          <component :is="Component" :key="route.path" v-if="route.meta.keepAlive" />
        </keep-alive>
        <component :is="Component" :key="route.path" v-if="!route.meta.keepAlive" />
      </router-view>
    </el-main>
  </el-container>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowDown, Microphone, HomeFilled, Document, ChatDotRound, Goods, Tickets, User, SwitchButton } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
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

// 手机号脱敏
function maskPhone(phone: string | undefined): string {
  if (!phone) return '未绑定'
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
}

// 退出登录
function handleLogout() {
  userStore.logout()
  ElMessage.success('已退出登录')
  router.push('/login')
}
</script>

<style scoped>
.main-layout {
  min-height: 100vh;
  background: #f5f7fa;
}

.header {
  display: flex;
  align-items: center;
  padding: 0 24px;
  height: 64px;
  background: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  position: sticky;
  top: 0;
  z-index: 100;
}

/* Logo区域 */
.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-right: 40px;
}

.logo-icon {
  font-size: 28px;
  color: #667eea;
}

.logo h1 {
  font-size: 20px;
  color: #303133;
  margin: 0;
  font-weight: 700;
  white-space: nowrap;
}

/* 导航菜单 */
.nav-menu {
  flex: 1;
  border-bottom: none;
  background: transparent;
}

.nav-menu :deep(.el-menu-item) {
  height: 64px;
  line-height: 64px;
  padding: 0 20px;
  font-size: 15px;
  color: #606266;
  transition: all 0.3s;
}

.nav-menu :deep(.el-menu-item:hover) {
  color: #667eea;
  background: transparent;
}

.nav-menu :deep(.el-menu-item.is-active) {
  color: #667eea;
  border-bottom: 3px solid #667eea;
}

.nav-menu :deep(.el-menu-item .el-icon) {
  margin-right: 6px;
  font-size: 18px;
}

/* 用户区域 */
.user-area {
  margin-left: auto;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 24px;
  transition: all 0.3s;
}

.user-info:hover {
  background: #f5f7fa;
}

.username {
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}

/* 悬停气泡样式 */
.user-hover-card {
  padding: 20px;
  min-width: 260px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
}

.hover-header {
  display: flex;
  align-items: center;
  gap: 14px;
}

.hover-info {
  flex: 1;
}

.hover-username {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}

.hover-divider {
  height: 1px;
  background: #f0f0f0;
  margin: 16px 0;
}

.hover-stats {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.stat-item {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  padding: 4px 0;
}

.stat-label {
  color: #909399;
}

.stat-value {
  color: #606266;
  font-weight: 500;
}

.hover-actions {
  display: flex;
  gap: 10px;
}

.hover-actions .el-button {
  flex: 1;
  border-radius: 8px;
}

/* 主内容区域 */
.main-content {
  padding: 24px;
  min-height: calc(100vh - 64px);
}

/* 响应式 */
@media (max-width: 900px) {
  .header {
    padding: 0 16px;
  }

  .logo h1 {
    display: none;
  }

  .nav-menu :deep(.el-menu-item span) {
    display: none;
  }

  .username {
    display: none;
  }
}
</style>