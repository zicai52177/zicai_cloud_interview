<template>
  <div class="user-page">
    <el-row :gutter="20">
      <!-- 左侧个人信息卡片 -->
      <el-col :span="8">
        <el-card class="profile-card">
          <div class="profile-header">
            <el-avatar :size="80" :src="userStore.userInfo?.headImg" />
            <div class="profile-title">
              <h2>{{ userStore.userInfo?.username || '用户' }}</h2>
              <el-tag :type="userStore.userInfo?.role === 'ADMIN' ? 'danger' : 'info'" size="small">
                {{ userStore.userInfo?.role === 'ADMIN' ? '超级会员' : '普通用户' }}
              </el-tag>
            </div>
          </div>
          <el-divider />
          <div class="profile-info">
            <div class="info-item">
              <el-icon><Message /></el-icon>
              <span class="info-label">手机号</span>
              <span class="info-value">{{ userStore.userInfo?.phone || '未绑定' }}</span>
            </div>
            <div class="info-item">
              <el-icon><Message /></el-icon>
              <span class="info-label">邮箱</span>
              <span class="info-value">{{ userStore.userInfo?.email || '未绑定' }}</span>
            </div>
            <div class="info-item">
              <el-icon><Calendar /></el-icon>
              <span class="info-label">注册时间</span>
              <span class="info-value">{{ formatDate(userStore.userInfo?.gmtCreate) }}</span>
            </div>
            <div class="info-item">
              <el-icon><Clock /></el-icon>
              <span class="info-label">更新时间</span>
              <span class="info-value">{{ formatDate(userStore.userInfo?.gmtModified) }}</span>
            </div>
          </div>
          <div class="profile-actions">
            <el-button type="primary" @click="handleRefresh">刷新信息</el-button>
            <el-button type="danger" @click="handleLogout">退出登录</el-button>
          </div>
        </el-card>
      </el-col>

      <!-- 右侧功能卡片 -->
      <el-col :span="16">
        <el-row :gutter="20">
          <!-- 简历管理 -->
          <el-col :span="12">
            <el-card class="feature-card" shadow="hover" @click="$router.push('/resume')">
              <div class="feature-icon resume-icon">
                <el-icon><Document /></el-icon>
              </div>
              <div class="feature-content">
                <h3>简历管理</h3>
                <p>上传、解析、分析您的简历</p>
              </div>
            </el-card>
          </el-col>

          <!-- AI面试 -->
          <el-col :span="12">
            <el-card class="feature-card" shadow="hover" @click="$router.push('/interview')">
              <div class="feature-icon interview-icon">
                <el-icon><ChatDotRound /></el-icon>
              </div>
              <div class="feature-content">
                <h3>AI面试</h3>
                <p>与AI进行模拟面试训练</p>
              </div>
            </el-card>
          </el-col>
        </el-row>

        <el-row :gutter="20" style="margin-top: 20px;">
          <!-- 套餐中心 -->
          <el-col :span="12">
            <el-card class="feature-card" shadow="hover" @click="$router.push('/product')">
              <div class="feature-icon product-icon">
                <el-icon><ShoppingBag /></el-icon>
              </div>
              <div class="feature-content">
                <h3>套餐中心</h3>
                <p>查看和购买服务套餐</p>
              </div>
            </el-card>
          </el-col>

          <!-- 我的订单 -->
          <el-col :span="12">
            <el-card class="feature-card" shadow="hover" @click="$router.push('/order')">
              <div class="feature-icon order-icon">
                <el-icon><Tickets /></el-icon>
              </div>
              <div class="feature-content">
                <h3>我的订单</h3>
                <p>查看购买记录和权益</p>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Message, Calendar, Clock, Document, ChatDotRound, ShoppingBag, Tickets } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()

onMounted(() => {
  userStore.refreshUserInfo()
})

// 格式化日期
function formatDate(dateStr: string | undefined): string {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

async function handleRefresh() {
  await userStore.refreshUserInfo()
  ElMessage.success('信息已刷新')
}

function handleLogout() {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.user-page {
  max-width: 1200px;
  margin: 0 auto;
}

.profile-card {
  border-radius: 12px;
}

.profile-header {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 10px 0;
}

.profile-title h2 {
  margin: 0 0 8px 0;
  color: #303133;
}

.profile-info {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.info-item .el-icon {
  color: #409eff;
  font-size: 18px;
}

.info-label {
  color: #909399;
  min-width: 70px;
}

.info-value {
  color: #606266;
  font-weight: 500;
}

.profile-actions {
  margin-top: 24px;
  display: flex;
  gap: 12px;
}

.feature-card {
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
  height: 140px;
  display: flex;
  align-items: center;
  padding: 20px;
}

.feature-card:hover {
  transform: translateY(-5px);
}

.feature-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: #fff;
  margin-right: 16px;
}

.resume-icon {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.interview-icon {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.product-icon {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.order-icon {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.feature-content h3 {
  margin: 0 0 4px 0;
  color: #303133;
  font-size: 16px;
}

.feature-content p {
  margin: 0;
  color: #909399;
  font-size: 13px;
}
</style>