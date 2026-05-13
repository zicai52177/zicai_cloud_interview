<template>
  <div class="interview-page">
    <el-card>
      <template #header>
        <span>AI面试</span>
      </template>
      <el-table :data="interviewList" v-loading="loading" empty-text="暂无面试记录">
        <el-table-column prop="position" label="面试岗位" />
        <el-table-column prop="score" label="得分" width="100">
          <template #default="{ row }">
            <el-tag :type="row.score >= 80 ? 'success' : row.score >= 60 ? 'warning' : 'danger'">
              {{ row.score ?? '进行中' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="gmtCreate" label="面试时间" width="180" />
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button size="small" @click="$router.push(`/interview/${row.id}`)">
              查看详情
            </el-button>
          </template>
        </el-table-column>
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
import { getInterviewListApi } from '@/api/interview'

const interviewList = ref<any[]>([])
const loading = ref(false)
const page = ref(1)
const size = ref(10)
const total = ref(0)

onMounted(() => {
  fetchList()
})

async function fetchList() {
  loading.value = true
  try {
    const res = await getInterviewListApi({ page: page.value, size: size.value })
    interviewList.value = res.data?.records || []
    total.value = res.data?.totalRecord || 0
  } catch (e) {
    // handled
  } finally {
    loading.value = false
  }
}

function handlePageChange(val: number) {
  page.value = val
  fetchList()
}
</script>

<style scoped>
.interview-page {
  max-width: 1000px;
  margin: 0 auto;
}

.pagination {
  margin-top: 20px;
  justify-content: center;
}
</style>
