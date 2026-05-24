<template>
  <div class="interview-report-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <el-button text @click="$router.back()">
          <el-icon><ArrowLeft /></el-icon>
          返回
        </el-button>
        <h1 class="page-title">
          <el-icon class="title-icon"><DocumentChecked /></el-icon>
          面试评价报告
        </h1>
      </div>
      <div class="header-right">
        <el-tag :type="getStatusType(reportData?.status)" size="large">
          {{ getStatusText(reportData?.status) }}
        </el-tag>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-loading="loading" class="content-wrapper">
      <template v-if="reportData">
        <!-- 综合评分卡片 -->
        <el-card shadow="never" class="score-overview-card">
          <div class="score-overview">
            <div class="overall-score-section">
              <div class="score-circle" :class="getScoreClass(reportData.overallScore || 0)">
                <div class="score-inner">
                  <span class="score-value">{{ reportData.overallScore || '-' }}</span>
                  <span class="score-label">综合得分</span>
                </div>
              </div>
              <div class="score-badge" :class="getScoreClass(reportData.overallScore || 0)">
                {{ getScoreLabel(reportData.overallScore || 0) }}
              </div>
            </div>
            <div class="score-stats">
              <div class="stat-item">
                <span class="stat-value">{{ reportData.passedQuestion || 0 }}</span>
                <span class="stat-label">及格题目</span>
              </div>
              <div class="stat-item">
                <span class="stat-value">{{ reportData.excellentQuestion || 0 }}</span>
                <span class="stat-label">优秀题目</span>
              </div>
              <div class="stat-item">
                <span class="stat-value">{{ formatTime(reportData.gmtCreate) }}</span>
                <span class="stat-label">面试时间</span>
              </div>
            </div>
          </div>
        </el-card>

        <!-- 各项能力评分 -->
        <el-card shadow="never" class="ability-scores-card">
          <template #header>
            <div class="card-header">
              <span>
                <el-icon><TrendCharts /></el-icon>
                能力评估
              </span>
            </div>
          </template>
          <div class="ability-grid">
            <div class="ability-item">
              <div class="ability-header">
                <span class="ability-name">技术能力</span>
                <span class="ability-score" :class="getScoreClass(reportData.technicalSkills || 0)">
                  {{ reportData.technicalSkills || '-' }}
                </span>
              </div>
              <el-progress
                :percentage="(reportData.technicalSkills || 0)"
                :color="getProgressColor(reportData.technicalSkills || 0)"
                :show-text="false"
                :stroke-width="10"
              />
            </div>
            <div class="ability-item">
              <div class="ability-header">
                <span class="ability-name">逻辑思维</span>
                <span class="ability-score" :class="getScoreClass(reportData.logicalThinking || 0)">
                  {{ reportData.logicalThinking || '-' }}
                </span>
              </div>
              <el-progress
                :percentage="(reportData.logicalThinking || 0)"
                :color="getProgressColor(reportData.logicalThinking || 0)"
                :show-text="false"
                :stroke-width="10"
              />
            </div>
            <div class="ability-item">
              <div class="ability-header">
                <span class="ability-name">表达能力</span>
                <span class="ability-score" :class="getScoreClass(reportData.communication || 0)">
                  {{ reportData.communication || '-' }}
                </span>
              </div>
              <el-progress
                :percentage="(reportData.communication || 0)"
                :color="getProgressColor(reportData.communication || 0)"
                :show-text="false"
                :stroke-width="10"
              />
            </div>
            <div class="ability-item">
              <div class="ability-header">
                <span class="ability-name">问题解决</span>
                <span class="ability-score" :class="getScoreClass(reportData.problemSolving || 0)">
                  {{ reportData.problemSolving || '-' }}
                </span>
              </div>
              <el-progress
                :percentage="(reportData.problemSolving || 0)"
                :color="getProgressColor(reportData.problemSolving || 0)"
                :show-text="false"
                :stroke-width="10"
              />
            </div>
          </div>
        </el-card>

        <!-- 面试总结 -->
        <el-card shadow="never" class="summary-card" v-if="reportData.summary">
          <template #header>
            <div class="card-header">
              <span>
                <el-icon><ChatLineSquare /></el-icon>
                面试总结
              </span>
            </div>
          </template>
          <div class="summary-content">
            <p>{{ reportData.summary }}</p>
          </div>
        </el-card>

        <!-- 优势与不足 -->
        <div class="analysis-grid">
          <el-card shadow="never" class="strength-card">
            <template #header>
              <div class="card-header success-header">
                <span>
                  <el-icon><CircleCheckFilled /></el-icon>
                  优势分析
                </span>
              </div>
            </template>
            <div class="content-text" v-if="reportData.strength">
              <p>{{ reportData.strength }}</p>
            </div>
            <el-empty v-else description="暂无数据" :image-size="60" />
          </el-card>

          <el-card shadow="never" class="improvement-card">
            <template #header>
              <div class="card-header warning-header">
                <span>
                  <el-icon><WarnTriangleFilled /></el-icon>
                  待改进之处
                </span>
              </div>
            </template>
            <div class="content-text" v-if="reportData.improvement">
              <p>{{ reportData.improvement }}</p>
            </div>
            <el-empty v-else description="暂无数据" :image-size="60" />
          </el-card>
        </div>

        <!-- 建议 -->
        <el-card shadow="never" class="suggestion-card" v-if="reportData.suggestion">
          <template #header>
            <div class="card-header">
              <span>
                <el-icon><InfoFilled /></el-icon>
                改进建议
              </span>
            </div>
          </template>
          <div class="suggestion-content">
            <p>{{ reportData.suggestion }}</p>
          </div>
        </el-card>

        <!-- 查看详情按钮 -->
        <div class="action-section">
          <el-button type="primary" size="large" @click="goToDetail">
            <el-icon><View /></el-icon>
            查看完整面试内容
          </el-button>
          <el-button size="large" @click="$router.push('/interview')">
            返回列表
          </el-button>
        </div>
      </template>

      <!-- 加载失败 -->
      <el-empty
        v-else-if="!loading"
        description="加载失败，请重试"
      >
        <el-button type="primary" @click="fetchReport">重新加载</el-button>
      </el-empty>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  ArrowLeft,
  DocumentChecked,
  TrendCharts,
  ChatLineSquare,
  CircleCheckFilled,
  WarnTriangleFilled,
  InfoFilled,
  View
} from '@element-plus/icons-vue'
import { getInterviewDetailApi } from '@/api/interview'
import type { InterviewDetailDTO } from '@/api/interview'

const route = useRoute()
const router = useRouter()

const reportData = ref<InterviewDetailDTO | null>(null)
const loading = ref(false)

// 获取报告数据
async function fetchReport() {
  const interviewId = Number(route.params.id)
  if (!interviewId) {
    ElMessage.error('面试ID无效')
    router.push('/interview')
    return
  }

  loading.value = true
  try {
    const res = await getInterviewDetailApi(interviewId)
    reportData.value = res.data

    if (!reportData.value) {
      ElMessage.warning('未找到该面试记录')
    }
  } catch (e) {
    ElMessage.error('加载面试报告失败')
  } finally {
    loading.value = false
  }
}

// 查看完整面试详情
function goToDetail() {
  router.push(`/interview/detail/${route.params.id}`)
}

// 状态相关
function getStatusType(status?: string): 'success' | 'warning' | 'info' | 'danger' | undefined {
  const map: Record<string, 'success' | 'warning' | 'info' | 'danger'> = {
    GENERATING: 'info',
    GENERATE_ROUND: 'info',
    GENERATE_QA: 'info',
    IN_PROGRESS: 'warning',
    EVALUATING: 'warning',
    COMPLETED: 'success',
    CANCELLED: 'info',
  }
  if (status?.startsWith('FAILED')) {
    return 'danger'
  }
  return map[status || '']
}

function getStatusText(status?: string) {
  const map: Record<string, string> = {
    GENERATING: '生成中',
    GENERATE_ROUND: '生成轮次',
    GENERATE_QA: '生成题目',
    IN_PROGRESS: '面试进行中',
    EVALUATING: '评分中',
    COMPLETED: '已完成',
    CANCELLED: '已取消',
  }
  if (status?.startsWith('FAILED')) {
    return '面试失败'
  }
  return map[status || ''] || '未知状态'
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

function getProgressColor(score: number) {
  if (score >= 80) return '#52c41a'
  if (score >= 60) return '#faad14'
  return '#ff4d4f'
}

function formatTime(timeStr?: string) {
  if (!timeStr) return '-'
  const date = new Date(timeStr)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  })
}

onMounted(() => {
  fetchReport()
})
</script>

<style scoped>
.interview-report-page {
  max-width: 1000px;
  margin: 0 auto;
  padding: 24px;
}

/* 页面头部 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 10px;
  color: #1a1a1a;
}

.title-icon {
  font-size: 28px;
  color: #ff7e5f;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.content-wrapper {
  min-height: 400px;
}

/* 综合评分卡片 */
.score-overview-card {
  margin-bottom: 24px;
  border-radius: 16px;
}

.score-overview {
  display: flex;
  align-items: center;
  gap: 48px;
  padding: 24px;
}

.overall-score-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.score-circle {
  width: 160px;
  height: 160px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.score-circle::before {
  content: '';
  position: absolute;
  inset: 10px;
  border-radius: 50%;
  background: white;
}

.score-inner {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.score-circle.score-high {
  background: conic-gradient(#52c41a 0deg, #52c41a 288deg, #e8e8e8 288deg);
}

.score-circle.score-medium {
  background: conic-gradient(#faad14 0deg, #faad14 216deg, #e8e8e8 216deg);
}

.score-circle.score-low {
  background: conic-gradient(#ff4d4f 0deg, #ff4d4f 144deg, #e8e8e8 144deg);
}

.score-value {
  font-size: 42px;
  font-weight: 700;
  color: #1a1a1a;
  line-height: 1;
}

.score-label {
  font-size: 14px;
  color: #8c8c8c;
  margin-top: 4px;
}

.score-badge {
  padding: 8px 24px;
  border-radius: 20px;
  font-size: 16px;
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

.score-stats {
  display: flex;
  gap: 48px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #1a1a1a;
}

.stat-label {
  font-size: 14px;
  color: #8c8c8c;
}

/* 能力评分卡片 */
.ability-scores-card {
  margin-bottom: 24px;
  border-radius: 16px;
}

.card-header {
  font-weight: 600;
  font-size: 16px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.ability-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24px;
}

.ability-item {
  padding: 16px;
  background: #fafafa;
  border-radius: 12px;
}

.ability-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.ability-name {
  font-weight: 500;
  color: #303133;
}

.ability-score {
  font-size: 20px;
  font-weight: 700;
}

.ability-score.score-high {
  color: #52c41a;
}

.ability-score.score-medium {
  color: #faad14;
}

.ability-score.score-low {
  color: #ff4d4f;
}

/* 总结卡片 */
.summary-card {
  margin-bottom: 24px;
  border-radius: 16px;
}

.summary-content {
  font-size: 15px;
  line-height: 1.8;
  color: #303133;
  white-space: pre-wrap;
}

/* 分析网格 */
.analysis-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24px;
  margin-bottom: 24px;
}

.strength-card,
.improvement-card {
  border-radius: 16px;
}

.success-header {
  color: #52c41a;
}

.warning-header {
  color: #faad14;
}

.content-text {
  font-size: 14px;
  line-height: 1.8;
  color: #303133;
  white-space: pre-wrap;
}

/* 建议卡片 */
.suggestion-card {
  margin-bottom: 24px;
  border-radius: 16px;
  background: linear-gradient(135deg, #fff7e6 0%, #fff2e6 100%);
  border: 1px solid #ffd591;
}

.suggestion-content {
  font-size: 15px;
  line-height: 1.8;
  color: #303133;
  white-space: pre-wrap;
}

/* 操作区域 */
.action-section {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-top: 32px;
}

/* 响应式 */
@media (max-width: 768px) {
  .score-overview {
    flex-direction: column;
    gap: 24px;
  }

  .score-stats {
    gap: 24px;
  }

  .ability-grid {
    grid-template-columns: 1fr;
  }

  .analysis-grid {
    grid-template-columns: 1fr;
  }
}
</style>