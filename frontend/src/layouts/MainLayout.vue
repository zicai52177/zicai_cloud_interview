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
        <el-dropdown>
          <span class="user-info">
            <el-avatar :size="32" :src="userStore.userInfo?.headImg" />
            <span class="username">{{ userStore.userInfo?.username || '用户' }}</span>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="$router.push('/user')">个人中心</el-dropdown-item>
              <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
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
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)

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
</style>
