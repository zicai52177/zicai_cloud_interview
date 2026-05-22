<template>
  <div class="user-page">
    <!-- 欢迎区域 -->
    <div class="welcome-section">
      <div class="welcome-content">
        <div class="welcome-avatar">
          <el-avatar :size="72" :src="userStore.userInfo?.headImg">
            {{ userStore.userInfo?.username?.charAt(0) || '用' }}
          </el-avatar>
          <div class="vip-badge" v-if="userStore.userInfo?.role === 'ADMIN'">
            <el-icon><Star /></el-icon>
            VIP
          </div>
        </div>
        <div class="welcome-info">
          <h1>你好，{{ userStore.userInfo?.username || '用户' }}</h1>
          <p>{{ getWelcomeMessage() }}</p>
        </div>
        <div class="welcome-stats">
          <div class="stat-item">
            <span class="stat-value">0</span>
            <span class="stat-label">面试次数</span>
          </div>
          <div class="stat-item">
            <span class="stat-value">0</span>
            <span class="stat-label">简历数量</span>
          </div>
          <div class="stat-item">
            <span class="stat-value">0</span>
            <span class="stat-label">我的订单</span>
          </div>
        </div>
      </div>
    </div>

    <el-row :gutter="20">
      <!-- 左侧个人信息卡片 -->
      <el-col :span="8">
        <el-card class="profile-card" shadow="hover">
          <div class="profile-header">
            <h3 class="section-title">个人信息</h3>
          </div>
          <div class="profile-avatar">
            <el-avatar :size="64" :src="userStore.userInfo?.headImg">
              {{ userStore.userInfo?.username?.charAt(0) || '用' }}
            </el-avatar>
            <div class="profile-name">
              <h4>{{ userStore.userInfo?.username || '用户' }}</h4>
              <el-tag :type="userStore.userInfo?.role === 'ADMIN' ? 'danger' : 'info'" size="small" effect="dark">
                {{ userStore.userInfo?.role === 'ADMIN' ? '超级会员' : '普通用户' }}
              </el-tag>
            </div>
          </div>
          <el-divider />
          <div class="profile-info">
            <div class="info-item">
              <el-icon><Message /></el-icon>
              <span class="info-label">手机号</span>
              <span class="info-value">{{ maskPhone(userStore.userInfo?.phone) }}</span>
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
          </div>
          <div class="profile-actions">
            <el-button type="primary" plain @click="handleRefresh">
              <el-icon><Refresh /></el-icon>
              刷新信息
            </el-button>
            <el-button type="danger" plain @click="handleLogout">
              <el-icon><SwitchButton /></el-icon>
              退出登录
            </el-button>
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
              <el-icon class="arrow-icon"><ArrowRight /></el-icon>
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
              <el-icon class="arrow-icon"><ArrowRight /></el-icon>
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
              <el-icon class="arrow-icon"><ArrowRight /></el-icon>
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
              <el-icon class="arrow-icon"><ArrowRight /></el-icon>
            </el-card>
          </el-col>
        </el-row>

        <!-- 快速操作区域 -->
        <el-card class="quick-actions" shadow="never">
          <template #header>
            <div class="quick-header">
              <span>快捷操作</span>
            </div>
          </template>
          <div class="action-list">
            <div class="action-item" @click="$router.push('/interview/create')">
              <el-icon class="action-icon"><VideoPlay /></el-icon>
              <span>开始新面试</span>
            </div>
            <div class="action-item" @click="$router.push('/resume')">
              <el-icon class="action-icon"><Upload /></el-icon>
              <span>上传简历</span>
            </div>
            <div class="action-item" @click="$router.push('/product')">
              <el-icon class="action-icon"><Goods /></el-icon>
              <span>浏览套餐</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Message, Calendar, Document, ChatDotRound, ShoppingBag, Tickets,
  Refresh, SwitchButton, ArrowRight, Star, VideoPlay, Upload, Goods
} from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()

onMounted(() => {
  userStore.refreshUserInfo()
})

// 获取欢迎语
function getWelcomeMessage(): string {
  const hour = new Date().getHours()
  if (hour < 12) return '上午好！今天也要加油哦~'
  if (hour < 18) return '下午好！保持专注，继续前进'
  return '晚上好！辛苦了一天，注意休息'
}

// 手机号脱敏
function maskPhone(phone: string | undefined): string {
  if (!phone) return '未绑定'
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
}

// 格式化日期
function formatDate(dateStr: string | undefined): string {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
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
  padding: 0 0 40px 0;
}

/* 欢迎区域 */
.welcome-section {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  padding: 32px;
  margin-bottom: 24px;
  color: white;
}

.welcome-content {
  display: flex;
  align-items: center;
  gap: 24px;
}

.welcome-avatar {
  position: relative;
}

.welcome-avatar :deep(.el-avatar) {
  border: 3px solid rgba(255, 255, 255, 0.3);
}

.vip-badge {
  position: absolute;
  bottom: -8px;
  left: 50%;
  transform: translateX(-50%);
  background: linear-gradient(135deg, #ffd700 0%, #ffb700 100%);
  color: #333;
  font-size: 10px;
  font-weight: 700;
  padding: 2px 8px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  gap: 2px;
}

.welcome-info {
  flex: 1;
}

.welcome-info h1 {
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 600;
}

.welcome-info p {
  margin: 0;
  font-size: 14px;
  opacity: 0.9;
}

.welcome-stats {
  display: flex;
  gap: 40px;
  padding-left: 40px;
  border-left: 1px solid rgba(255, 255, 255, 0.3);
}

.stat-item {
  text-align: center;
}

.stat-value {
  display: block;
  font-size: 28px;
  font-weight: 700;
}

.stat-label {
  font-size: 13px;
  opacity: 0.8;
}

/* 个人信息卡片 */
.profile-card {
  border-radius: 16px;
  border: none;
}

.profile-header {
  margin-bottom: 16px;
}

.section-title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.profile-avatar {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
}

.profile-name h4 {
  margin: 0 0 8px 0;
  font-size: 18px;
  font-weight: 600;
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
  color: #667eea;
  font-size: 18px;
}

.info-label {
  color: #909399;
  min-width: 60px;
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

.profile-actions .el-button {
  flex: 1;
  border-radius: 8px;
}

/* 功能卡片 */
.feature-card {
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
  height: 140px;
  display: flex;
  align-items: center;
  padding: 20px 24px;
  border: 1px solid #f0f0f0;
  position: relative;
}

.feature-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 40px rgba(102, 126, 234, 0.15);
  border-color: #667eea;
}

.feature-icon {
  width: 56px;
  height: 56px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26px;
  color: #fff;
  margin-right: 18px;
  flex-shrink: 0;
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

.feature-content {
  flex: 1;
}

.feature-content h3 {
  margin: 0 0 6px 0;
  color: #303133;
  font-size: 16px;
  font-weight: 600;
}

.feature-content p {
  margin: 0;
  color: #909399;
  font-size: 13px;
}

.arrow-icon {
  position: absolute;
  right: 20px;
  color: #c0c4cc;
  font-size: 20px;
  transition: all 0.3s;
}

.feature-card:hover .arrow-icon {
  color: #667eea;
  transform: translateX(4px);
}

/* 快捷操作 */
.quick-actions {
  margin-top: 20px;
  border-radius: 16px;
  border: none;
}

.quick-header {
  font-weight: 600;
  color: #303133;
}

.action-list {
  display: flex;
  gap: 16px;
}

.action-item {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.3s;
}

.action-item:hover {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.action-icon {
  font-size: 20px;
}

.action-item span {
  font-size: 14px;
  font-weight: 500;
}

/* 响应式 */
@media (max-width: 900px) {
  .welcome-content {
    flex-direction: column;
    text-align: center;
  }

  .welcome-stats {
    padding-left: 0;
    border-left: none;
    margin-top: 20px;
  }
}
</style>