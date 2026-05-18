<template>
  <div class="interview-list-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">
          <el-icon class="title-icon"><Reading /></el-icon>
          AI 智能面试
        </h1>
        <p class="page-desc">通过AI模拟真实面试场景，提升您的面试技能</p>
      </div>
      <el-button type="primary" size="large" @click="goToCreate" class="start-btn">
        <el-icon><Plus /></el-icon>
        开始面试
      </el-button>
    </div>

    <!-- 筛选区域 -->
    <el-card shadow="never" class="filter-card">
      <div class="filter-row">
        <el-select v-model="filterStatus" placeholder="面试状态" clearable size="default" class="filter-select">
          <el-option label="全部" value="" />
          <el-option label="进行中" value="PROCESSING" />
          <el-option label="已完成" value="FINISHED" />
        </el-select>
        <el-input
          v-model="searchKeyword"
          placeholder="搜索岗位..."
          clearable
          class="search-input"
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button @click="handleSearch">搜索</el-button>
      </div>
    </el-card>

    <!-- 面试列表 -->
    <div class="interview-grid" v-loading="loading">
      <template v-if="interviewList.length > 0">
        <div
          v-for="item in interviewList"
          :key="item.id"
          class="interview-card"
          @click="goToDetail(item)"
        >
          <div class="card-header">
            <div class="position-info">
              <h3 class="position-name">{{ item.position || '未指定岗位' }}</h3>
              <el-tag
                :type="getStatusType(item.status)"
                size="small"
              >
                {{ getStatusText(item.status) }}
              </el-tag>
            </div>
          </div>

          <div class="card-body">
            <div class="score-section" v-if="item.score !== undefined && item.score !== null">
              <div class="score-circle" :class="getScoreClass(item.score)">
                <span class="score-value">{{ item.score }}</span>
                <span class="score-label">分</span>
              </div>
              <div class="score-desc">面试得分</div>
            </div>
            <div class="score-section no-score" v-else>
              <el-icon class="process-icon" :size="40"><Loading /></el-icon>
              <div class="score-desc">面试进行中</div>
            </div>
          </div>

          <div class="card-footer">
            <div class="time-info">
              <el-icon><Clock /></el-icon>
              <span>{{ formatTime(item.gmtCreate) }}</span>
            </div>
            <div class="action-btns" @click.stop>
              <el-button
                type="primary"
                link
                @click="goToDetail(item)"
              >
                {{ item.status === 'PROCESSING' ? '继续面试' : '查看详情' }}
              </el-button>
              <el-popconfirm title="确定要删除这条面试记录吗？" @confirm="handleDelete(item.id)">
                <template #reference>
                  <el-button type="danger" link>删除</el-button>
                </template>
              </el-popconfirm>
            </div>
          </div>
        </div>
      </template>

      <!-- 空状态 -->
      <div v-else class="empty-state">
        <el-empty description="暂无面试记录">
          <template #image>
            <el-icon :size="80" class="empty-icon"><Interview /></el-icon>
          </template>
          <el-button type="primary" @click="goToCreate">开始您的第一次面试</el-button>
        </el-empty>
      </div>
    </div>

    <!-- 分页 -->
    <div class="pagination-wrapper" v-if="total > 0">
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="size"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @current-change="handlePageChange"
        @size-change="handleSizeChange"
        background
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus, Search, Clock, Reading, Loading, Interview } from '@element-plus/icons-vue'
import { getInterviewListApi, deleteInterviewApi } from '@/api/interview'
import type { InterviewDTO } from '@/api/interview'

const router = useRouter()

// 列表数据
const interviewList = ref<InterviewDTO[]>([])
const loading = ref(false)

// 分页
const page = ref(1)
const size = ref(10)
const total = ref(0)

// 筛选
const filterStatus = ref('')
const searchKeyword = ref('')

// 加载列表
async function fetchList() {
  loading.value = true
  try {
    const res = await getInterviewListApi({
      page: page.value,
      size: size.value
    })

    let records = res.data?.records || []
    // 前端过滤（实际应后端支持）
    if (filterStatus.value) {
      records = records.filter((item: InterviewDTO) => item.status === filterStatus.value)
    }
    if (searchKeyword.value) {
      records = records.filter((item: InterviewDTO) =>
        item.position?.includes(searchKeyword.value)
      )
    }

    interviewList.value = records
    total.value = res.data?.totalRecord || 0
  } catch (e) {
    // handled by request interceptor
  } finally {
    loading.value = false
  }
}

// 搜索
function handleSearch() {
  page.value = 1
  fetchList()
}

// 分页变化
function handlePageChange(val: number) {
  page.value = val
  fetchList()
}

function handleSizeChange(val: number) {
  size.value = val
  page.value = 1
  fetchList()
}

// 删除
async function handleDelete(interviewId: number) {
  try {
    await deleteInterviewApi(interviewId)
    ElMessage.success('删除成功')
    fetchList()
  } catch (e) {
    // handled by request interceptor
  }
}

// 跳转
function goToCreate() {
  router.push('/interview/create')
}

function goToDetail(item: InterviewDTO) {
  if (item.status === 'PROCESSING') {
    router.push(`/interview/conduct/${item.id}`)
  } else {
    router.push(`/interview/detail/${item.id}`)
  }
}

// 状态相关
function getStatusType(status: string) {
  const map: Record<string, string> = {
    PROCESSING: 'warning',
    FINISHED: 'success',
    CANCELLED: 'info'
  }
  return map[status] || 'info'
}

function getStatusText(status: string) {
  const map: Record<string, string> = {
    PROCESSING: '进行中',
    FINISHED: '已完成',
    CANCELLED: '已取消'
  }
  return map[status] || status
}

function getScoreClass(score: number) {
  if (score >= 80) return 'score-high'
  if (score >= 60) return 'score-medium'
  return 'score-low'
}

// 格式化时间
function formatTime(timeStr: string) {
  if (!timeStr) return '-'
  const date = new Date(timeStr)
  const now = new Date()
  const diff = now.getTime() - date.getTime()

  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  if (diff < 604800000) return `${Math.floor(diff / 86400000)}天前`

  return date.toLocaleDateString('zh-CN')
}

onMounted(() => {
  fetchList()
})
</script>

<style scoped>
.interview-list-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

/* 页面头部 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 32px;
  padding: 24px 32px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  color: white;
}

.header-content {
  flex: 1;
}

.page-title {
  font-size: 28px;
  font-weight: 600;
  margin: 0 0 8px 0;
  display: flex;
  align-items: center;
  gap: 12px;
}

.title-icon {
  font-size: 32px;
}

.page-desc {
  font-size: 15px;
  opacity: 0.9;
  margin: 0;
}

.start-btn {
  padding: 12px 28px;
  font-size: 16px;
  border-radius: 8px;
  background: white;
  color: #667eea;
  border: none;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.3s;
}

.start-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.2);
}

/* 筛选卡片 */
.filter-card {
  margin-bottom: 24px;
  border-radius: 12px;
}

.filter-row {
  display: flex;
  gap: 16px;
  align-items: center;
}

.filter-select {
  width: 160px;
}

.search-input {
  flex: 1;
  max-width: 300px;
}

/* 面试卡片网格 */
.interview-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
  gap: 24px;
  min-height: 300px;
}

.interview-card {
  background: white;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  cursor: pointer;
  transition: all 0.3s;
  border: 1px solid #f0f0f0;
}

.interview-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 30px rgba(102, 126, 234, 0.15);
  border-color: #667eea;
}

.card-header {
  margin-bottom: 16px;
}

.position-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.position-name {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0;
}

.card-body {
  display: flex;
  justify-content: center;
  padding: 24px 0;
}

.score-section {
  text-align: center;
}

.score-circle {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  margin: 0 auto 12px;
}

.score-high {
  background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%);
  color: white;
}

.score-medium {
  background: linear-gradient(135deg, #faad14 0%, #ffc53d 100%);
  color: white;
}

.score-low {
  background: linear-gradient(135deg, #ff4d4f 0%, #ff7875 100%);
  color: white;
}

.score-value {
  font-size: 32px;
  font-weight: 700;
  line-height: 1;
}

.score-label {
  font-size: 14px;
  opacity: 0.9;
}

.score-desc {
  color: #8c8c8c;
  font-size: 14px;
}

.no-score {
  color: #8c8c8c;
}

.process-icon {
  margin-bottom: 12px;
  animation: rotate 1.5s linear infinite;
  color: #667eea;
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}

.time-info {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #8c8c8c;
  font-size: 14px;
}

.action-btns {
  display: flex;
  gap: 8px;
}

/* 空状态 */
.empty-state {
  grid-column: 1 / -1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 0;
}

.empty-icon {
  color: #d9d9d9;
}

/* 分页 */
.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 32px;
}
</style>