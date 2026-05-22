<template>
  <div class="order-page">
    <el-card>
      <template #header>
        <span>我的订单</span>
      </template>
      <el-table :data="orderList" v-loading="loading" empty-text="暂无订单">
        <el-table-column prop="outTradeNo" label="订单号" width="200" />
        <el-table-column prop="title" label="商品" />
        <el-table-column prop="payAmount" label="金额" width="100">
          <template #default="{ row }">¥{{ row.payAmount }}</template>
        </el-table-column>
        <el-table-column prop="orderState" label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.orderState)">{{ getStatusText(row.orderState) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="gmtCreate" label="下单时间" width="180" />
      </el-table>
      <el-pagination
        v-if="total > 0"
        class="pagination"
        :current-page="page"
        :page-size="size"
        :total="total"
        layout="prev, pager, next"
        @current-change="handlePageChange"
      />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getOrderListApi } from '@/api/product'

const orderList = ref<any[]>([])
const loading = ref(false)
const page = ref(1)
const size = ref(10)
const total = ref(0)

onMounted(() => fetchOrders())

async function fetchOrders() {
  loading.value = true
  try {
    const res = await getOrderListApi({ page: page.value, size: size.value })
    orderList.value = res.data?.records || []
    total.value = res.data?.totalRecord || 0
  } catch (e) {
    // handled
  } finally {
    loading.value = false
  }
}

function handlePageChange(val: number) {
  page.value = val
  fetchOrders()
}

function getStatusType(state: string): 'primary' | 'success' | 'warning' | 'info' | 'danger' {
  const map: Record<string, 'primary' | 'success' | 'warning' | 'info' | 'danger'> = {
    NEW: 'warning', PAY: 'success', CANCEL: 'info', REFUND: 'danger',
  }
  return map[state] || 'info'
}

function getStatusText(state: string) {
  const map: Record<string, string> = {
    NEW: '待支付', PAY: '已支付', CANCEL: '已取消', REFUND: '已退款',
  }
  return map[state] || state
}
</script>

<style scoped>
.order-page {
  max-width: 1000px;
  margin: 0 auto;
}

.pagination {
  margin-top: 20px;
  justify-content: center;
}
</style>
