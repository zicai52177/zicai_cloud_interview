<template>
  <div class="home-page">
    <!-- Banner 区域 -->
    <el-carousel :interval="5000" height="300px" class="banner">
      <el-carousel-item v-for="item in bannerList" :key="item.id">
        <div class="banner-item" :style="{ backgroundImage: `url(${item.imgUrl})` }">
          <h2>{{ item.name }}</h2>
        </div>
      </el-carousel-item>
      <el-carousel-item v-if="bannerList.length === 0">
        <div class="banner-item banner-default">
          <h2>AI智能面试助手</h2>
          <p>上传简历，开始你的模拟面试之旅</p>
        </div>
      </el-carousel-item>
    </el-carousel>

    <!-- 功能入口 -->
    <div class="feature-grid">
      <el-card class="feature-card" shadow="hover" @click="$router.push('/resume')">
        <el-icon :size="40" color="#409eff"><Upload /></el-icon>
        <h3>上传简历</h3>
        <p>支持PDF、Word格式</p>
      </el-card>
      <el-card class="feature-card" shadow="hover" @click="$router.push('/interview')">
        <el-icon :size="40" color="#67c23a"><ChatDotRound /></el-icon>
        <h3>AI面试</h3>
        <p>智能出题，实时反馈</p>
      </el-card>
      <el-card class="feature-card" shadow="hover" @click="$router.push('/product')">
        <el-icon :size="40" color="#e6a23c"><ShoppingCart /></el-icon>
        <h3>套餐中心</h3>
        <p>选择适合你的方案</p>
      </el-card>
      <el-card class="feature-card" shadow="hover" @click="$router.push('/order')">
        <el-icon :size="40" color="#909399"><List /></el-icon>
        <h3>我的订单</h3>
        <p>查看订单记录</p>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Upload, ChatDotRound, ShoppingCart, List } from '@element-plus/icons-vue'
import { useCacheStore } from '@/store/cache'

interface Banner {
  id: number
  name: string
  imgUrl: string
  text: string
}

const cacheStore = useCacheStore()
const bannerList = ref<Banner[]>([])

onMounted(async () => {
  try {
    // 从缓存获取（5分钟有效）
    bannerList.value = await cacheStore.getBannerList()
  } catch (e) {
    // 静默处理
  }
})
</script>

<style scoped>
.home-page {
  max-width: 1200px;
  margin: 0 auto;
}

.banner {
  border-radius: 12px;
  overflow: hidden;
}

.banner-item {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background-size: cover;
  background-position: center;
  color: #fff;
}

.banner-default {
  background: linear-gradient(135deg, #ff7e5f 0%, #feb47b 100%);
}

.banner-default p {
  margin-top: 10px;
  font-size: 16px;
  opacity: 0.9;
}

.feature-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-top: 30px;
}

.feature-card {
  text-align: center;
  cursor: pointer;
  transition: transform 0.2s;
  padding: 20px 0;
}

.feature-card:hover {
  transform: translateY(-4px);
}

.feature-card h3 {
  margin: 12px 0 6px;
  color: #303133;
}

.feature-card p {
  color: #909399;
  font-size: 13px;
}

@media (max-width: 768px) {
  .feature-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
