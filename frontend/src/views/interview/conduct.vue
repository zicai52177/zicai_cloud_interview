<template>
  <div class="interview-conduct-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">
          <el-icon class="title-icon"><Microphone /></el-icon>
          AI 面试进行中
        </h1>
      </div>
      <div class="header-right">
        <el-tag type="warning" size="large" effect="plain">
          <el-icon class="blink-icon"><Loading /></el-icon>
          面试进行中
        </el-tag>
      </div>
    </div>

    <!-- 面试状态 -->
    <el-card shadow="never" class="status-card" v-loading="statusLoading">
      <div class="status-content">
        <div class="status-info">
          <div class="position-label">当前面试岗位</div>
          <div class="position-value">{{ interviewInfo.title || '加载中...' }}</div>
        </div>
        <div class="progress-section">
          <el-progress
            :percentage="progress"
            :stroke-width="8"
            :color="progressColors"
            :show-text="true"
          />
          <div class="progress-text">面试进度：{{ currentQuestionIndex + 1 }} / {{ totalQuestions }}</div>
        </div>
      </div>
    </el-card>

    <!-- 面试内容区 -->
    <div class="interview-content" v-loading="contentLoading">
      <template v-if="currentQuestion">
        <!-- 当前题目 -->
        <el-card shadow="never" class="question-card">
          <template #header>
            <div class="question-header">
              <div class="round-info">
                <el-tag type="primary" size="small">
                  {{ currentRoundIndex + 1 }} / {{ interviewDetail?.rounds?.length || 0 }} 轮
                </el-tag>
                <span class="round-name">{{ currentRound?.roundName || '面试环节' }}</span>
              </div>
              <span class="question-tag">第 {{ currentQuestionIndex + 1 }} 题</span>
            </div>
          </template>
          <div class="question-content">
            <div class="ai-avatar">
              <el-icon :size="36"><UserFilled /></el-icon>
            </div>
            <div class="question-text">
              <p>{{ currentQuestion.content }}</p>
            </div>
          </div>
        </el-card>

        <!-- 历史对话 -->
        <div v-if="chatHistory.length > 0" class="chat-history">
          <div
            v-for="(chat, idx) in chatHistory"
            :key="idx"
            :class="['chat-item', chat.type]"
          >
            <div class="chat-avatar">
              <el-icon v-if="chat.type === 'ai'" :size="24"><Service /></el-icon>
              <el-icon v-else :size="24"><UserFilled /></el-icon>
            </div>
            <div class="chat-content">
              <div class="chat-label">{{ chat.type === 'ai' ? 'AI面试官' : '我' }}</div>
              <div class="chat-text">{{ chat.content }}</div>
            </div>
          </div>
        </div>

        <!-- 回答输入区 -->
        <el-card shadow="never" class="answer-card">
          <template #header>
            <div class="answer-header">
              <span>请回答</span>
              <span class="answer-tip">AI正在等待您的回答</span>
            </div>
          </template>
          <div class="answer-form">
            <el-input
              v-model="answerText"
              type="textarea"
              :rows="5"
              :placeholder="'请输入您的回答...\n- 建议详细阐述您的思路和经验\n- 可以结合实际案例说明'"
              maxlength="2000"
              show-word-limit
              :disabled="submitting"
              class="answer-textarea"
            />
            <div class="answer-actions">
              <el-button
                size="large"
                @click="handleSaveDraft"
                :disabled="submitting"
              >
                <el-icon><Document /></el-icon>
                保存草稿
              </el-button>
              <el-button
                type="primary"
                size="large"
                :loading="submitting"
                :disabled="!answerText.trim()"
                @click="handleSubmitAnswer"
              >
                <el-icon v-if="!submitting"><Position /></el-icon>
                {{ submitting ? '提交中...' : '提交回答' }}
              </el-button>
            </div>
          </div>
        </el-card>
      </template>

      <!-- 加载状态 -->
      <div v-else class="loading-state">
        <el-icon class="loading-icon" :size="48"><Loading /></el-icon>
        <p>正在加载面试内容...</p>
      </div>
    </div>

    <!-- 底部操作 -->
    <div class="bottom-actions">
      <el-button @click="handleQuitInterview" :disabled="submitting">
        退出面试
      </el-button>
      <el-button
        type="success"
        @click="handleFinishInterview"
        :disabled="!canFinish"
      >
        <el-icon><CircleCheck /></el-icon>
        结束面试
      </el-button>
    </div>

    <!-- 确认结束对话框 -->
    <el-dialog
      v-model="finishDialogVisible"
      title="确认结束面试"
      width="400"
      :close-on-click-modal="false"
    >
      <div class="finish-dialog-content">
        <p>确定要结束本次面试吗？</p>
        <p class="finish-tip">结束后，AI将根据您的表现生成综合评价报告</p>
      </div>
      <template #footer>
        <el-button @click="finishDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="finishLoading" @click="confirmFinish">
          确认结束
        </el-button>
      </template>
    </el-dialog>

    <!-- 退出确认对话框 -->
    <el-dialog
      v-model="quitDialogVisible"
      title="确认退出"
      width="400"
    >
      <div class="quit-dialog-content">
        <p>确定要退出当前面试吗？</p>
        <p class="quit-tip">面试记录将保留，您可以稍后继续</p>
      </div>
      <template #footer>
        <el-button @click="quitDialogVisible = false">继续面试</el-button>
        <el-button type="danger" @click="confirmQuit">确认退出</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Microphone,
  Loading,
  UserFilled,
  Service,
  Document,
  Position,
  CircleCheck
} from '@element-plus/icons-vue'
import {
  getInterviewStatusApi,
  getInterviewDetailApi,
  submitAnswerApi,
  finishInterviewApi
} from '@/api/interview'
import type { InterviewDetailDTO } from '@/api/interview'

const route = useRoute()
const router = useRouter()

// 状态
const interviewId = ref<number>(0)
const interviewInfo = ref<{ title?: string }>({})
const interviewDetail = ref<InterviewDetailDTO | null>(null)
const statusLoading = ref(false)
const contentLoading = ref(false)
const submitting = ref(false)
const finishLoading = ref(false)

// 对话历史
const chatHistory = ref<Array<{ type: 'ai' | 'user'; content: string }>>([])
const answerText = ref('')

// 对话框
const finishDialogVisible = ref(false)
const quitDialogVisible = ref(false)

// 轮次和题目索引
const currentRoundIndex = ref(0)
const currentQuestionIndex = ref(0)

// 进度
const progress = computed(() => {
  const total = totalQuestions.value
  if (total === 0) return 0
  return Math.round(((currentRoundIndex.value * 5 + currentQuestionIndex.value + 1) / total) * 100)
})

const progressColors = [
  { color: '#f56c6c', percentage: 30 },
  { color: '#e6a23c', percentage: 60 },
  { color: '#67c23a', percentage: 100 }
]

const totalQuestions = computed(() => {
  if (!interviewDetail.value?.rounds) return 0
  return interviewDetail.value.rounds.reduce((sum: number, round: { questions?: Array<{ content: string }> }) => sum + (round.questions?.length || 0), 0)
})

const currentRound = computed(() => {
  return interviewDetail.value?.rounds?.[currentRoundIndex.value]
})

const currentQuestion = computed(() => {
  return currentRound.value?.questions?.[currentQuestionIndex.value]
})

const canFinish = computed(() => {
  return currentQuestionIndex.value > 0 || chatHistory.value.length > 0
})

// 轮询状态
let statusPollingTimer: number | null = null

// 获取面试状态
async function fetchInterviewStatus() {
  if (!interviewId.value) return

  statusLoading.value = true
  try {
    await getInterviewStatusApi(interviewId.value)
    // 保持已有position或使用从详情获取的
  } catch (e) {
    // handled by request interceptor
  } finally {
    statusLoading.value = false
  }
}

// 获取面试详情
async function fetchInterviewDetail() {
  if (!interviewId.value) return

  contentLoading.value = true
  try {
    const res = await getInterviewDetailApi(interviewId.value)
    interviewDetail.value = res.data

    if (res.data?.title) {
      interviewInfo.value.title = res.data.title
    }

    // 如果有已回答的题目，加载对话历史
    loadChatHistory()
  } catch (e) {
    // handled by request interceptor
  } finally {
    contentLoading.value = false
  }
}

// 加载对话历史
function loadChatHistory() {
  if (!interviewDetail.value?.rounds) return

  const history: Array<{ type: 'ai' | 'user'; content: string }> = []

  interviewDetail.value.rounds.forEach((round: { questions?: Array<{ content: string; answer?: string }> }) => {
    round.questions?.forEach((question: { content: string; answer?: string }) => {
      // 添加AI问题
      history.push({
        type: 'ai',
        content: question.content
      })
      // 添加用户回答
      if (question.answer) {
        history.push({
          type: 'user',
          content: question.answer
        })
      }
    })
  })

  chatHistory.value = history

  // 设置当前轮次和题目索引
  if (interviewDetail.value.rounds.length > 0) {
    const lastRound = interviewDetail.value.rounds[interviewDetail.value.rounds.length - 1]
    const lastQuestion = lastRound.questions?.[lastRound.questions.length - 1]

    // 如果最后一个问题已有回答，则移到下一个
    if (lastQuestion?.answer) {
      // 已全部回答，显示最后状态
    }
  }
}

// 提交回答
async function handleSubmitAnswer() {
  if (!answerText.value.trim() || !currentQuestion.value) return

  submitting.value = true
  try {
    await submitAnswerApi({
      interviewId: interviewId.value,
      questionId: currentQuestion.value.id,
      answer: answerText.value.trim()
    })

    // 添加到对话历史
    chatHistory.value.push({
      type: 'user',
      content: answerText.value.trim()
    })

    // 清空输入
    answerText.value = ''

    ElMessage.success('回答已提交')

    // 刷新详情获取AI评价
    setTimeout(() => {
      fetchInterviewDetail()
    }, 1000)
  } catch (e) {
    // handled by request interceptor
  } finally {
    submitting.value = false
  }
}

// 保存草稿
function handleSaveDraft() {
  localStorage.setItem(`interview_draft_${interviewId.value}`, answerText.value)
  ElMessage.success('草稿已保存')
}

// 加载草稿
function loadDraft() {
  const draft = localStorage.getItem(`interview_draft_${interviewId.value}`)
  if (draft) {
    answerText.value = draft
  }
}

// 结束面试
function handleFinishInterview() {
  finishDialogVisible.value = true
}

async function confirmFinish() {
  finishLoading.value = true
  try {
    await finishInterviewApi({ interviewId: interviewId.value })

    // 清除草稿
    localStorage.removeItem(`interview_draft_${interviewId.value}`)

    ElMessage.success('面试已结束，正在生成评价报告...')

    // 跳转到详情页
    setTimeout(() => {
      router.push(`/interview/detail/${interviewId.value}`)
    }, 1500)
  } catch (e) {
    // handled by request interceptor
  } finally {
    finishLoading.value = false
    finishDialogVisible.value = false
  }
}

// 退出面试
function handleQuitInterview() {
  quitDialogVisible.value = true
}

function confirmQuit() {
  quitDialogVisible.value = false
  router.push('/interview')
}

// 开始轮询
function startPolling() {
  statusPollingTimer = window.setInterval(() => {
    fetchInterviewStatus()
  }, 5000) // 每5秒轮询一次
}

// 停止轮询
function stopPolling() {
  if (statusPollingTimer) {
    clearInterval(statusPollingTimer)
    statusPollingTimer = null
  }
}

onMounted(() => {
  const id = Number(route.params.id)
  if (!id) {
    ElMessage.error('面试ID无效')
    router.push('/interview')
    return
  }

  interviewId.value = id

  // 获取面试信息和详情
  fetchInterviewStatus()
  fetchInterviewDetail()
  loadDraft()

  // 开始轮询状态
  startPolling()
})

onUnmounted(() => {
  stopPolling()
})
</script>

<style scoped>
.interview-conduct-page {
  max-width: 900px;
  margin: 0 auto;
  padding: 24px;
  min-height: 100vh;
  background: linear-gradient(180deg, #f5f7fa 0%, #fff 100%);
}

/* 页面头部 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 12px;
  color: #1a1a1a;
}

.title-icon {
  font-size: 28px;
  color: #ff7e5f;
}

.blink-icon {
  animation: blink 1s ease-in-out infinite;
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.3; }
}

/* 状态卡片 */
.status-card {
  margin-bottom: 24px;
  border-radius: 16px;
}

.status-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 0;
}

.status-info {
  flex: 1;
}

.position-label {
  font-size: 14px;
  color: #8c8c8c;
  margin-bottom: 8px;
}

.position-value {
  font-size: 20px;
  font-weight: 600;
  color: #1a1a1a;
}

.progress-section {
  flex: 1;
  max-width: 300px;
}

.progress-text {
  margin-top: 8px;
  font-size: 14px;
  color: #606266;
  text-align: center;
}

/* 面试内容区 */
.interview-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 题目卡片 */
.question-card {
  border-radius: 16px;
}

.question-header {
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
}

.question-tag {
  color: #8c8c8c;
  font-size: 14px;
}

.question-content {
  display: flex;
  gap: 16px;
  padding: 20px 0;
}

.ai-avatar {
  width: 48px;
  height: 48px;
  background: linear-gradient(135deg, #ff7e5f 0%, #feb47b 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
}

.question-text {
  flex: 1;
  font-size: 16px;
  line-height: 1.8;
  color: #303133;
}

/* 对话历史 */
.chat-history {
  display: flex;
  flex-direction: column;
  gap: 16px;
  max-height: 400px;
  overflow-y: auto;
  padding: 16px;
  background: #fafafa;
  border-radius: 12px;
}

.chat-item {
  display: flex;
  gap: 12px;
  max-width: 85%;
}

.chat-item.ai {
  align-self: flex-start;
}

.chat-item.user {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.chat-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.ai .chat-avatar {
  background: linear-gradient(135deg, #ff7e5f 0%, #feb47b 100%);
  color: white;
}

.user .chat-avatar {
  background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%);
  color: white;
}

.chat-content {
  background: white;
  padding: 12px 16px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.chat-label {
  font-size: 12px;
  color: #8c8c8c;
  margin-bottom: 4px;
}

.chat-text {
  font-size: 14px;
  line-height: 1.6;
  color: #303133;
  white-space: pre-wrap;
}

/* 回答卡片 */
.answer-card {
  border-radius: 16px;
}

.answer-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.answer-tip {
  color: #8c8c8c;
  font-size: 14px;
}

.answer-form {
  padding: 16px 0;
}

.answer-textarea :deep(.el-textarea__inner) {
  border-radius: 12px;
  padding: 16px;
  font-size: 15px;
  line-height: 1.8;
}

.answer-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 16px;
}

/* 加载状态 */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 0;
  color: #8c8c8c;
}

.loading-icon {
  animation: rotate 1.5s linear infinite;
  margin-bottom: 16px;
  color: #ff7e5f;
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* 底部操作 */
.bottom-actions {
  display: flex;
  justify-content: space-between;
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid #f0f0f0;
}

/* 对话框内容 */
.finish-dialog-content,
.quit-dialog-content {
  text-align: center;
}

.finish-dialog-content p,
.quit-dialog-content p {
  margin: 0 0 8px 0;
  font-size: 16px;
}

.finish-tip,
.quit-tip {
  color: #8c8c8c;
  font-size: 14px;
}
</style>