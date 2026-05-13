<template>
  <div class="user-page">
    <el-card>
      <template #header>
        <span>个人中心</span>
      </template>
      <div class="user-profile">
        <el-avatar :size="80" :src="userStore.userInfo?.headImg" />
        <div class="user-detail">
          <h3>{{ userStore.userInfo?.username || '用户' }}</h3>
          <p class="user-id">ID: {{ userStore.userInfo?.id }}</p>
        </div>
      </div>
      <el-divider />
      <el-descriptions :column="1" border>
        <el-descriptions-item label="用户名">{{ userStore.userInfo?.username }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ userStore.userInfo?.phone || '未绑定' }}</el-descriptions-item>
      </el-descriptions>
      <div class="action-area">
        <el-button type="danger" @click="handleLogout">退出登录</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()

function handleLogout() {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.user-page {
  max-width: 600px;
  margin: 0 auto;
}

.user-profile {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 20px 0;
}

.user-detail h3 {
  margin: 0;
  color: #303133;
}

.user-id {
  color: #909399;
  font-size: 13px;
  margin-top: 4px;
}

.action-area {
  margin-top: 24px;
  text-align: center;
}
</style>
