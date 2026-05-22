<template>
  <div class="product-page">
    <h2 class="page-title">套餐中心</h2>
    <div class="package-grid" v-loading="loading">
      <el-card
        v-for="pkg in packageList"
        :key="pkg.id"
        class="package-card"
        shadow="hover"
      >
        <div class="package-name">{{ pkg.name }}</div>
        <div class="package-price">
          <span class="price">¥{{ pkg.price }}</span>
        </div>
        <div class="package-desc">{{ pkg.description }}</div>
        <ul class="benefit-list">
          <li v-for="(benefit, idx) in pkg.benefits" :key="idx">
            {{ benefit.name }}
          </li>
        </ul>
        <el-button type="primary" class="buy-btn" @click="handleBuy(pkg)">
          立即购买
        </el-button>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createPackageOrderApi } from '@/api/product'
import { useCacheStore } from '@/store/cache'

const cacheStore = useCacheStore()

const packageList = ref<any[]>([])
const loading = ref(false)

onMounted(async () => {
  loading.value = true
  try {
    // 从缓存获取（5分钟有效）
    packageList.value = await cacheStore.getPackageList()
  } catch (e) {
    // handled
  } finally {
    loading.value = false
  }
})

async function handleBuy(pkg: any) {
  try {
    await ElMessageBox.confirm(`确认购买「${pkg.name}」，金额 ¥${pkg.price}？`, '确认购买')
    const res = await createPackageOrderApi({ packageId: pkg.id, payType: 'WECHAT_PAY' })
    if (res.data?.codeUrl) {
      ElMessage.success('订单创建成功，请扫码支付')
      // TODO: 展示支付二维码
    }
  } catch (e) {
    // cancelled or error
  }
}
</script>

<style scoped>
.product-page {
  max-width: 1200px;
  margin: 0 auto;
}

.page-title {
  margin-bottom: 24px;
  color: #303133;
}

.package-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
}

.package-card {
  text-align: center;
  padding: 20px 0;
}

.package-name {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.package-price {
  margin: 16px 0;
}

.price {
  font-size: 32px;
  font-weight: 700;
  color: #409eff;
}

.package-desc {
  color: #909399;
  font-size: 14px;
  margin-bottom: 16px;
}

.benefit-list {
  text-align: left;
  padding: 0 20px;
  margin-bottom: 20px;
}

.benefit-list li {
  padding: 4px 0;
  color: #606266;
  font-size: 14px;
}

.buy-btn {
  width: 80%;
}

@media (max-width: 768px) {
  .package-grid {
    grid-template-columns: 1fr;
  }
}
</style>
