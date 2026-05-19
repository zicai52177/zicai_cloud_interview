<template>
  <div class="interview-detail-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <el-button text @click="$router.back()">
          <el-icon><ArrowLeft /></el-icon>
          返回
        </el-button>
        <h1 class="page-title">
          <el-icon class="title-icon"><Document /></el-icon>
          面试详情
        </h1>
      </div>
      <div class="header-right">
        <el-tag :type="getStatusType(detail?.status)" size="large">
          {{ getStatusText(detail?.status) }}
        </el-tag>
        <el-button type="danger" plain @click="handleDelete">
          <el-icon><Delete /></el-icon>
          删除记录
        </el-button>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-loading="loading" class="content-wrapper">
      <template v-if="detail">
        <!-- 基本信息卡片 -->
        <el-card shadow="never" class="info-card">
          <template #header>
            <div class="card-header">
              <span>面试信息</span>
            </div>
          </template>
          <el-descriptions :column="3" border>
            <el-descriptions-item label="面试岗位">
              <span class="highlight-text">{{ detail.position || '未指定' }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="面试得分">
              <div v-if="detail.score !== undefined && detail.score !== null" class="score-display">
                <span class="score-value" :class="getScoreClass(detail.score)">{{ detail.score }}</span>
                <span class="score-unit">分</span>
              </div>
              <el-tag v-else type="warning" size="small">进行中</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="面试时间">
              {{ formatDateTime(detail.gmtCreate) }}
            </el-descriptions-item>
          </el-descriptions>
        </el-card>

        <!-- 面试轮次与题目 -->
        <div class="rounds-section">
          <h2 class="section-title">
            <el-icon><Collection /></el-icon>
            面试内容
          </h2>

          <div v-if="detail.rounds && detail.rounds.length > 0" class="rounds-list">
            <el-card
              v-for="(round, roundIndex) in detail.rounds"
              :key="round.id"
              shadow="never"
              class="round-card"
            >
              <template #header>
                <div class="round-header">
                  <div class="round-info">
                    <el-tag type="primary" size="small">第 {{ roundIndex + 1 }} 轮</el-tag>
                    <span class="round-name">{{ round.roundName || '面试环节' }}</span>
                  </div>
                  <span class="question-count">{{ round.questions?.length || 0 }} 道题目</span>
                </div>
              </template>

              <div class="questions-list">
                <div
                  v-for="(question, qIndex) in round.questions"
                  :key="question.id"
                  class="question-item"
                >
                  <div class="question-header">
                    <span class="question-number">Q{{ qIndex + 1 }}</span>
                    <span class="question-content">{{ question.content }}</span>
                  </div>

                  <!-- 用户回答 -->
                  <div v-if="question.answer" class="answer-section user-answer">
                    <div class="answer-label">
                      <el-icon><User /></el-icon>
                      您的回答
                    </div>
                    <div class="answer-content">{{ question.answer }}</div>
                  </div>

                  <!-- AI评价 -->
                  <div v-if="question.evaluation" class="answer-section ai-evaluation">
                    <div class="answer-label">
                      <el-icon><ChatDotRound /></el-icon>
                      AI点评
                    </div>
                    <div class="answer-content">{{ question.evaluation }}</div>
                  </div>

                  <!-- 未回答提示 -->
                  <div v-else class="answer-section no-answer">
                    <el-empty description="暂无回答" :image-size="40" />
                  </div>
                </div>
              </div>
            </el-card>
          </div>

          <!-- 空状态 -->
          <el-empty
            v-else
            description="暂无面试内容"
            :image-size="80"
          >
            <template #image>
              <el-icon :size="60" class="empty-icon"><DocumentDelete /></el-icon>
            </template>
          </el-empty>
        </div>
      </template>

      <!-- 加载失败 -->
      <el-empty
        v-else-if="!loading"
        description="加载失败，请重试"
      >
        <el-button type="primary" @click="fetchDetail">重新加载</el-button>
      </el-empty>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ArrowLeft,
  Document,
  Delete,
  Collection,
  User,
  ChatDotRound,
  DocumentDelete
} from '@element-plus/icons-vue'
import {
  getInterviewDetailApi,
  deleteInterviewApi
} from '@/api/interview'
import type { InterviewDetailDTO } from '@/api/interview'

const route = useRoute()
const router = useRouter()

const detail = ref<InterviewDetailDTO | null>(null)
const loading = ref(false)

// 获取详情
async function fetchDetail() {
  const interviewId = Number(route.params.id)
  if (!interviewId) {
    ElMessage.error('面试ID无效')
    return
  }

  loading.value = true
  try {
    const res = await getInterviewDetailApi(interviewId)
    detail.value = res.data
  } catch (e) {
    // handled by request interceptor
  } finally {
    loading.value = false
  }
}

// 删除
async function handleDelete() {
  if (!detail.value?.id) return

  try {
    await ElMessageBox.confirm(
      '确定要删除这条面试记录吗？删除后将无法恢复。',
      '删除确认',
      {
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await deleteInterviewApi(detail.value.id)
    ElMessage.success('删除成功')
    router.push('/interview')
  } catch (e) {
    // user cancelled or error
  }
}

// 状态相关
function getStatusType(status?: string): 'success' | 'warning' | 'info' | 'danger' | undefined {
  const map: Record<string, 'success' | 'warning' | 'info' | 'danger'> = {
    PROCESSING: 'warning',
    FINISHED: 'success',
    CANCELLED: 'info'
  }
  return map[status || '']
}

function getStatusText(status?: string) {
  const map: Record<string, string> = {
    PROCESSING: '面试进行中',
    FINISHED: '已完成',
    CANCELLED: '已取消'
  }
  return map[status || ''] || '未知状态'
}

function getScoreClass(score: number) {
  if (score >= 80) return 'score-high'
  if (score >= 60) return 'score-medium'
  return 'score-low'
}

function formatDateTime(timeStr?: string) {
  if (!timeStr) return '-'
  const date = new Date(timeStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

onMounted(() => {
  fetchDetail()
})
</script>

<style scoped>
.interview-detail-page {
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
  color: #667eea;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.content-wrapper {
  min-height: 400px;
}

/* 信息卡片 */
.info-card {
  margin-bottom: 24px;
  border-radius: 12px;
}

.card-header {
  font-weight: 600;
  font-size: 16px;
}

.highlight-text {
  font-weight: 600;
  color: #667eea;
}

.score-display {
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.score-value {
  font-size: 24px;
  font-weight: 700;
}

.score-high {
  color: #52c41a;
}

.score-medium {
  color: #faad14;
}

.score-low {
  color: #ff4d4f;
}

.score-unit {
  font-size: 14px;
  color: #8c8c8c;
}

/* 轮次区域 */
.rounds-section {
  margin-top: 32px;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  margin: 0 0 20px 0;
  display: flex;
  align-items: center;
  gap: 10px;
  color: #1a1a1a;
}

.rounds-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.round-card {
  border-radius: 12px;
  border: 1px solid #f0f0f0;
}

.round-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.round-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.round-name {
  font-weight: 600;
  font-size: 16px;
}

.question-count {
  color: #8c8c8c;
  font-size: 14px;
}

/* 题目列表 */
.questions-list {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.question-item {
  padding: 20px;
  background: #fafafa;
  border-radius: 10px;
}

.question-header {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.question-number {
  flex-shrink: 0;
  width: 32px;
  height: 32px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 14px;
}

.question-content {
  flex: 1;
  font-size: 16px;
  font-weight: 500;
  line-height: 1.6;
  color: #1a1a1a;
}

/* 回答区域 */
.answer-section {
  margin-top: 12px;
  padding: 16px;
  border-radius: 8px;
}

.user-answer {
  background: #f0f9ff;
  border-left: 3px solid #409eff;
}

.ai-evaluation {
  background: #f6ffed;
  border-left: 3px solid #52c41a;
}

.no-answer {
  background: #fafafa;
  display: flex;
  justify-content: center;
  padding: 24px;
}

.answer-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 600;
  margin-bottom: 8px;
  color: #606266;
}

.user-answer .answer-label {
  color: #409eff;
}

.ai-evaluation .answer-label {
  color: #52c41a;
}

.answer-content {
  font-size: 14px;
  line-height: 1.8;
  color: #303133;
  white-space: pre-wrap;
}

.empty-icon {
  color: #d9d9d9;
}
</style>