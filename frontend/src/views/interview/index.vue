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
    <div class="filter-bar">
      <el-select v-model="filterStatus" placeholder="面试状态" clearable size="large" class="filter-select">
        <el-option label="全部状态" value="" />
        <el-option label="生成中" value="GENERATING" />
        <el-option label="进行中" value="IN_PROGRESS" />
        <el-option label="已完成" value="COMPLETED" />
        <el-option label="已取消" value="CANCELLED" />
        <el-option label="失败" value="FAILED" />
      </el-select>
      <el-input
        v-model="searchKeyword"
        placeholder="搜索岗位名称..."
        clearable
        class="search-input"
        size="large"
        @keyup.enter="handleSearch"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
        <template #append>
          <el-button @click="handleSearch" :icon="Search" />
        </template>
      </el-input>
    </div>

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
              <h3 class="position-name">{{ item.title || '未指定岗位' }}</h3>
              <el-tag
                :type="getStatusType(item.status)"
                size="small"
                effect="dark"
              >
                {{ getStatusText(item.status) }}
              </el-tag>
            </div>
          </div>

          <div class="card-body">
            <div class="score-section" v-if="item.overallScore !== undefined && item.overallScore !== null">
              <div class="score-ring" :class="getScoreClass(item.overallScore)">
                <div class="score-inner">
                  <span class="score-value">{{ item.overallScore }}</span>
                  <span class="score-unit">分</span>
                </div>
              </div>
              <div class="score-desc">
                <span class="score-label">面试得分</span>
                <span class="score-badge" :class="getScoreClass(item.overallScore)">{{ getScoreLabel(item.overallScore) }}</span>
              </div>
            </div>
            <div class="score-section no-score" v-else>
              <div class="processing-indicator">
                <el-icon class="process-icon"><Loading /></el-icon>
              </div>
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
                size="small"
                @click="goToDetail(item)"
              >
                {{ isInProgress(item.status) ? '继续' : '查看' }}
                <el-icon class="arrow-icon"><ArrowRight /></el-icon>
              </el-button>
              <el-popconfirm title="确定要删除这条面试记录吗？" @confirm="handleDelete(item.id)">
                <template #reference>
                  <el-button type="danger" link size="small">删除</el-button>
                </template>
              </el-popconfirm>
            </div>
          </div>
        </div>
      </template>

      <!-- 空状态 -->
      <div v-else class="empty-state">
        <div class="empty-visual">
          <el-icon :size="80" class="empty-icon"><Microphone /></el-icon>
        </div>
        <h3 class="empty-title">暂无面试记录</h3>
        <p class="empty-desc">开始您的第一次AI模拟面试，提升面试技巧</p>
        <el-button type="primary" size="large" @click="goToCreate">
          <el-icon><Plus /></el-icon>
          开始第一次面试
        </el-button>
      </div>
    </div>

    <!-- 分页 -->
    <div class="pagination-wrapper" v-if="total > 0">
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="size"
        :total="total"
        :page-sizes="[8, 12, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @current-change="handlePageChange"
        @size-change="handleSizeChange"
        background
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus, Search, Clock, Reading, Loading, Microphone, ArrowRight } from '@element-plus/icons-vue'
import { getInterviewListApi, deleteInterviewApi } from '@/api/interview'
import type { InterviewDTO } from '@/api/interview'

const router = useRouter()

// 列表数据
const interviewList = ref<InterviewDTO[]>([])
const loading = ref(false)

// 分页
const page = ref(1)
const size = ref(12)
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
      if (filterStatus.value === 'FAILED') {
        records = records.filter((item: InterviewDTO) => item.status.startsWith('FAILED'))
      } else {
        records = records.filter((item: InterviewDTO) => item.status === filterStatus.value)
      }
    }
    if (searchKeyword.value) {
      records = records.filter((item: InterviewDTO) =>
        item.title?.includes(searchKeyword.value)
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
  if (isInProgress(item.status)) {
    router.push(`/interview/conduct/${item.id}`)
  } else {
    router.push(`/interview/detail/${item.id}`)
  }
}

// 状态相关
function isInProgress(status: string): boolean {
  return ['GENERATING', 'GENERATE_ROUND', 'GENERATE_QA', 'IN_PROGRESS', 'EVALUATING'].includes(status)
}

function getStatusType(status: string): 'success' | 'warning' | 'info' | 'danger' | undefined {
  const map: Record<string, 'success' | 'warning' | 'info' | 'danger'> = {
    GENERATING: 'warning',
    GENERATE_ROUND: 'warning',
    GENERATE_QA: 'warning',
    IN_PROGRESS: 'warning',
    EVALUATING: 'warning',
    COMPLETED: 'success',
    CANCELLED: 'info',
    FAILED_PARSE_RESUME: 'danger',
    FAILED_GENERATE_ROUND: 'danger',
    FAILED_GENERATE_QA: 'danger',
    FAILED_CREATE_INTERVIEW: 'danger',
    FAILED_EVALUATE_INTERVIEW: 'danger'
  }
  return map[status]
}

function getStatusText(status: string) {
  const map: Record<string, string> = {
    GENERATING: '生成中',
    GENERATE_ROUND: '生成轮次',
    GENERATE_QA: '生成题目',
    IN_PROGRESS: '进行中',
    EVALUATING: '评估中',
    COMPLETED: '已完成',
    CANCELLED: '已取消',
    FAILED_PARSE_RESUME: '简历解析失败',
    FAILED_GENERATE_ROUND: '生成轮次失败',
    FAILED_GENERATE_QA: '生成题目失败',
    FAILED_CREATE_INTERVIEW: '创建失败',
    FAILED_EVALUATE_INTERVIEW: '评估失败'
  }
  return map[status] || status
}

function getScoreClass(score: number) {
  if (score >= 80) return 'score-high'
  if (score >= 60) return 'score-medium'
  return 'score-low'
}

function getScoreLabel(score: number) {
  if (score >= 80) return '优秀'
  if (score >= 60) return '合格'
  return '待提升'
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
  margin-bottom: 28px;
  padding: 32px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 20px;
  color: white;
}

.header-content {
  flex: 1;
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  margin: 0 0 10px 0;
  display: flex;
  align-items: center;
  gap: 12px;
}

.title-icon {
  font-size: 36px;
}

.page-desc {
  font-size: 15px;
  opacity: 0.9;
  margin: 0;
}

.start-btn {
  padding: 14px 32px;
  font-size: 16px;
  border-radius: 12px;
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
  transform: translateY(-3px);
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.25);
}

/* 筛选栏 */
.filter-bar {
  display: flex;
  gap: 16px;
  margin-bottom: 24px;
  padding: 20px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

.filter-select {
  width: 180px;
}

.search-input {
  flex: 1;
  max-width: 360px;
}

/* 面试卡片网格 */
.interview-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 24px;
  min-height: 400px;
}

.interview-card {
  background: white;
  border-radius: 20px;
  padding: 24px;
  box-shadow: 0 2px 16px rgba(0, 0, 0, 0.06);
  cursor: pointer;
  transition: all 0.4s ease;
  border: 1px solid #f0f0f0;
  position: relative;
  overflow: hidden;
}

.interview-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
  opacity: 0;
  transition: opacity 0.3s;
}

.interview-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 16px 48px rgba(102, 126, 234, 0.18);
  border-color: transparent;
}

.interview-card:hover::before {
  opacity: 1;
}

.card-header {
  margin-bottom: 20px;
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
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-body {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 28px 0;
}

.score-section {
  text-align: center;
}

/* 分数圆环 */
.score-ring {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 16px;
  position: relative;
}

.score-ring::before {
  content: '';
  position: absolute;
  inset: 6px;
  border-radius: 50%;
  background: white;
}

.score-high {
  background: conic-gradient(#52c41a 0deg, #52c41a 288deg, #e8e8e8 288deg);
}

.score-medium {
  background: conic-gradient(#faad14 0deg, #faad14 216deg, #e8e8e8 216deg);
}

.score-low {
  background: conic-gradient(#ff4d4f 0deg, #ff4d4f 144deg, #e8e8e8 144deg);
}

.score-inner {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.score-value {
  font-size: 36px;
  font-weight: 700;
  line-height: 1;
  color: #1a1a1a;
}

.score-unit {
  font-size: 14px;
  color: #8c8c8c;
}

.score-desc {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.score-label {
  color: #8c8c8c;
  font-size: 14px;
}

.score-badge {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
}

.score-badge.score-high {
  background: #f0f9eb;
  color: #52c41a;
}

.score-badge.score-medium {
  background: #fffbe6;
  color: #faad14;
}

.score-badge.score-low {
  background: #fff2f0;
  color: #ff4d4f;
}

/* 进行中状态 */
.no-score {
  color: #8c8c8c;
}

.processing-indicator {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea20 0%, #764ba220 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 16px;
}

.process-icon {
  font-size: 40px;
  color: #667eea;
  animation: rotate 2s linear infinite;
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* 底部区域 */
.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 20px;
  border-top: 1px solid #f5f5f5;
}

.time-info {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #8c8c8c;
  font-size: 13px;
}

.action-btns {
  display: flex;
  align-items: center;
  gap: 8px;
}

.arrow-icon {
  margin-left: 2px;
}

/* 空状态 */
.empty-state {
  grid-column: 1 / -1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 0;
}

.empty-visual {
  width: 160px;
  height: 160px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea15 0%, #764ba215 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 24px;
}

.empty-icon {
  color: #667eea;
  opacity: 0.6;
}

.empty-title {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 8px 0;
}

.empty-desc {
  font-size: 14px;
  color: #909399;
  margin: 0 0 28px 0;
}

/* 分页 */
.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 40px;
  padding: 20px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

/* 响应式 */
@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    gap: 20px;
  }

  .start-btn {
    width: 100%;
    justify-content: center;
  }

  .filter-bar {
    flex-direction: column;
  }

  .filter-select,
  .search-input {
    width: 100%;
    max-width: none;
  }
}
</style>