<template>
  <div class="resume-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">
          <el-icon class="title-icon"><Document /></el-icon>
          简历管理中心
        </h1>
        <p class="page-desc">上传并分析您的简历，获取专业的面试建议</p>
      </div>
      <el-upload
        :show-file-list="false"
        :before-upload="handleUpload"
        accept=".pdf,.doc,.docx"
      >
        <el-button type="primary" size="large" class="upload-btn" :loading="uploading">
          <el-icon><Upload /></el-icon>
          上传简历
        </el-button>
      </el-upload>
    </div>

    <!-- 简历列表 -->
    <el-card class="resume-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">我的简历</span>
          <span class="resume-count" v-if="resumeList.length > 0">{{ resumeList.length }} 份</span>
        </div>
      </template>

      <el-table :data="resumeList" v-loading="loading" stripe class="resume-table" empty-text="暂无简历，请上传您的第一份简历">
        <el-table-column prop="filename" label="文件名" min-width="180">
          <template #default="{ row }">
            <div class="filename-cell">
              <el-icon class="file-icon"><Document /></el-icon>
              <span class="filename">{{ row.filename }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="gmtCreate" label="上传时间" width="160">
          <template #default="{ row }">
            <span class="time-text">{{ formatDate(row.gmtCreate) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="140" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusInfo(row.status).type" effect="dark" size="small">
              {{ getStatusInfo(row.status).text }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="320" align="center">
          <template #default="{ row }">
            <div class="action-btns">
              <el-button
                v-if="row.status === 'ANALYZED'"
                size="small"
                type="success"
                plain
                @click="handleViewResult(row)"
              >
                <el-icon><View /></el-icon>
                查看结果
              </el-button>
              <el-button
                v-if="getStatusInfo(row.status).showButton"
                size="small"
                type="warning"
                plain
                :loading="analysing === row.id"
                @click="handleAnalyse(row)"
              >
                <el-icon><DataAnalysis /></el-icon>
                {{ row.status === 'ERROR' ? '重新分析' : '分析简历' }}
              </el-button>
              <el-button size="small" type="primary" plain @click="startInterview(row)">
                <el-icon><VideoPlay /></el-icon>
                开始面试
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 分析结果弹窗 -->
    <el-dialog
      v-model="showResultDialog"
      :title="currentResume ? `简历分析 - ${currentResume.filename}` : '简历分析结果'"
      width="900px"
      destroy-on-close
      class="result-dialog"
    >
      <div v-if="currentResume" class="result-content">
        <!-- 技能标签 -->
        <div class="result-section">
          <div class="section-header">
            <el-icon class="section-icon"><Collection /></el-icon>
            <h4>技能标签</h4>
          </div>
          <div class="skill-tags" v-if="analysisResult.skillTags?.length">
            <el-tag v-for="(tag, index) in analysisResult.skillTags" :key="index" class="skill-tag" type="info" effect="plain">
              {{ tag }}
            </el-tag>
          </div>
          <div class="no-data-tip" v-else>
            <el-icon><Warning /></el-icon>
            暂无技能数据
          </div>
        </div>

        <!-- 基础定位 -->
        <div class="result-section" v-if="analysisResult.basicPosition">
          <div class="section-header">
            <el-icon class="section-icon"><UserFilled /></el-icon>
            <h4>基础定位</h4>
          </div>
          <el-descriptions :column="2" border size="small" class="result-descriptions">
            <el-descriptions-item label="程序员级别">{{ analysisResult.basicPosition.level?.desc || '-' }}</el-descriptions-item>
            <el-descriptions-item label="岗位适配">{{ analysisResult.basicPosition.positionFitDegree || '-' }}</el-descriptions-item>
            <el-descriptions-item label="级别判断依据" :span="2">{{ analysisResult.basicPosition.levelJudgmentBasis || '-' }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 技能与项目分析 -->
        <el-row :gutter="20" v-if="analysisResult.skillAnalysis || analysisResult.projectAnalysis">
          <el-col :span="12">
            <div class="result-section" v-if="analysisResult.skillAnalysis">
              <div class="section-header">
                <el-icon class="section-icon"><Tools /></el-icon>
                <h4>技能分析</h4>
              </div>
              <div class="analysis-cards">
                <div class="analysis-card advantage">
                  <div class="card-label">优点</div>
                  <ul v-if="analysisResult.skillAnalysis.advantages?.length">
                    <li v-for="(item, i) in analysisResult.skillAnalysis.advantages" :key="i">{{ item }}</li>
                  </ul>
                  <div class="no-data-tip" v-else>暂无数据</div>
                </div>
                <div class="analysis-card disadvantage">
                  <div class="card-label">缺点</div>
                  <ul v-if="analysisResult.skillAnalysis.disadvantages?.length">
                    <li v-for="(item, i) in analysisResult.skillAnalysis.disadvantages" :key="i">{{ item }}</li>
                  </ul>
                  <div class="no-data-tip" v-else>暂无数据</div>
                </div>
              </div>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="result-section" v-if="analysisResult.projectAnalysis">
              <div class="section-header">
                <el-icon class="section-icon"><Box /></el-icon>
                <h4>项目经验分析</h4>
              </div>
              <div class="analysis-cards">
                <div class="analysis-card advantage">
                  <div class="card-label">优点</div>
                  <ul v-if="analysisResult.projectAnalysis.advantages?.length">
                    <li v-for="(item, i) in analysisResult.projectAnalysis.advantages" :key="i">{{ item }}</li>
                  </ul>
                  <div class="no-data-tip" v-else>暂无数据</div>
                </div>
                <div class="analysis-card disadvantage">
                  <div class="card-label">缺点</div>
                  <ul v-if="analysisResult.projectAnalysis.disadvantages?.length">
                    <li v-for="(item, i) in analysisResult.projectAnalysis.disadvantages" :key="i">{{ item }}</li>
                  </ul>
                  <div class="no-data-tip" v-else>暂无数据</div>
                </div>
              </div>
            </div>
          </el-col>
        </el-row>

        <!-- 整体总结 -->
        <div class="result-section" v-if="analysisResult.overallSummary">
          <div class="section-header">
            <el-icon class="section-icon"><Memo /></el-icon>
            <h4>整体总结</h4>
          </div>
          <div class="summary-cards">
            <div class="summary-item">
              <div class="summary-label">
                <el-icon><Star /></el-icon>
                核心竞争力
              </div>
              <p class="summary-text">{{ analysisResult.overallSummary.coreCompetence || '暂无数据' }}</p>
            </div>
            <div class="summary-item" v-if="analysisResult.overallSummary.coreShortcomings?.length">
              <div class="summary-label">
                <el-icon><WarningFilled /></el-icon>
                核心短板
              </div>
              <ul class="summary-list">
                <li v-for="(item, i) in analysisResult.overallSummary.coreShortcomings" :key="i">{{ item }}</li>
              </ul>
            </div>
            <div class="summary-item" v-if="analysisResult.overallSummary.adaptationSuggestions?.length">
              <div class="summary-label">
                <el-icon><Opportunity /></el-icon>
                适配建议
              </div>
              <ul class="summary-list">
                <li v-for="(item, i) in analysisResult.overallSummary.adaptationSuggestions" :key="i">{{ item }}</li>
              </ul>
            </div>
          </div>
        </div>

        <!-- 改进建议 -->
        <div class="result-section" v-if="analysisResult.improvementSuggestion">
          <div class="section-header">
            <el-icon class="section-icon"><MagicStick /></el-icon>
            <h4>改进建议</h4>
          </div>
          <div class="suggestion-list">
            <div class="suggestion-item emergency" v-if="analysisResult.improvementSuggestion.emergencyItems?.length">
              <div class="suggestion-tag">
                <el-icon><Clock /></el-icon>
                紧急改进
              </div>
              <ul>
                <li v-for="(item, i) in analysisResult.improvementSuggestion.emergencyItems" :key="i">{{ item }}</li>
              </ul>
            </div>
            <div class="suggestion-item longterm" v-if="analysisResult.improvementSuggestion.longTermItems?.length">
              <div class="suggestion-tag">
                <el-icon><Calendar /></el-icon>
                长期规划
              </div>
              <ul>
                <li v-for="(item, i) in analysisResult.improvementSuggestion.longTermItems" :key="i">{{ item }}</li>
              </ul>
            </div>
          </div>
        </div>

        <!-- 整体评价 -->
        <div class="result-section evaluation-section" v-if="analysisResult.evaluation">
          <div class="section-header">
            <el-icon class="section-icon"><ChatLineRound /></el-icon>
            <h4>面试官点评</h4>
          </div>
          <div class="evaluation-box">
            <pre>{{ analysisResult.evaluation }}</pre>
          </div>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showResultDialog = false">关闭</el-button>
          <el-button type="primary" @click="startInterviewWithResult">
            <el-icon><VideoPlay /></el-icon>
            基于简历开始面试
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Upload, Document, View, DataAnalysis, VideoPlay, Collection, Warning,
  UserFilled, Tools, Box, Memo, Star, Opportunity, MagicStick, Clock, Calendar, ChatLineRound
} from '@element-plus/icons-vue'
import { uploadResumeApi, getResumeListApi, analyseResumeApi } from '@/api/interview'

interface AnalysisResult {
  basicPosition?: {
    level?: { desc?: string }
    levelJudgmentBasis?: string
    positionFitDegree?: string
    potentialGap?: string
  }
  skillAnalysis?: {
    advantages?: string[]
    disadvantages?: string[]
    detailsEvidence?: string[]
  }
  skillTags?: string[]
  evaluation?: string
  overallSummary?: {
    coreCompetence?: string
    coreShortcomings?: string[]
    adaptationSuggestions?: string[]
  }
  improvementSuggestion?: {
    emergencyItems?: string[]
    longTermItems?: string[]
    priorityOrder?: string[]
  }
  projectAnalysis?: {
    advantages?: string[]
    disadvantages?: string[]
    detailsEvidence?: string[]
  }
  educationAnalysis?: {
    advantages?: string[]
    disadvantages?: string[]
    detailsEvidence?: string[]
  }
  benchmarkGap?: {
    sameLevelStandard?: string[]
    sameLevelGapPoints?: string[]
    gapDegree?: string
    samePositionRequirements?: string[]
    samePositionGap?: string
  }
}

const router = useRouter()
const resumeList = ref<any[]>([])
const loading = ref(false)
const uploading = ref(false)
const analysing = ref<number | null>(null)

const showResultDialog = ref(false)
const currentResume = ref<any>(null)
const analysisResult = ref<AnalysisResult>({})

onMounted(() => {
  fetchResumeList()
})

async function fetchResumeList() {
  loading.value = true
  try {
    const res = await getResumeListApi()
    resumeList.value = res.data || []
  } catch (e) {
    // handled
  } finally {
    loading.value = false
  }
}

async function handleUpload(file: File) {
  uploading.value = true
  const formData = new FormData()
  formData.append('file', file)
  try {
    await uploadResumeApi(formData)
    ElMessage.success('简历上传成功')
    fetchResumeList()
  } catch (e) {
    // handled
  } finally {
    uploading.value = false
  }
  return false
}

async function handleAnalyse(row: any) {
  if (row.status !== 'UPLOADED' && row.status !== 'ERROR') {
    ElMessage.warning('该简历已分析或正在分析中')
    return
  }
  analysing.value = row.id
  try {
    await analyseResumeApi(row.id)
    ElMessage.success('简历分析已启动，请稍候')
    setTimeout(() => fetchResumeList(), 1000)
  } catch (e) {
    // handled
  } finally {
    analysing.value = null
  }
}

async function handleViewResult(row: any) {
  currentResume.value = row
  try {
    if (row.evaluation) {
      analysisResult.value = JSON.parse(row.evaluation)
    } else if (row.skillTags) {
      analysisResult.value = {
        skillTags: JSON.parse(row.skillTags)
      }
    } else {
      analysisResult.value = {}
    }
  } catch (e) {
    console.error('解析分析结果失败:', e)
    analysisResult.value = {}
  }
  showResultDialog.value = true
}

function getStatusInfo(status: string): { type: 'success' | 'warning' | 'info' | 'danger' | undefined; text: string; showButton: boolean } {
  switch (status) {
    case 'UPLOADED':
      return { type: 'info', text: '待分析', showButton: true }
    case 'IN_PROCESS':
      return { type: 'warning', text: '分析中', showButton: false }
    case 'ANALYZED':
      return { type: 'success', text: '已分析', showButton: false }
    case 'ERROR':
      return { type: 'danger', text: '分析失败', showButton: true }
    default:
      return { type: undefined, text: '未知', showButton: false }
  }
}

function startInterview(row: any) {
  router.push({ path: '/interview', query: { resumeId: row.id } })
}

function startInterviewWithResult() {
  if (currentResume.value) {
    showResultDialog.value = false
    router.push({ path: '/interview', query: { resumeId: currentResume.value.id } })
  }
}

function formatDate(dateStr: string) {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}
</script>

<style scoped>
.resume-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 0 40px 0;
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

.upload-btn {
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

.upload-btn:hover {
  transform: translateY(-3px);
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.25);
}

/* 简历列表卡片 */
.resume-card {
  border-radius: 20px;
  border: none;
  overflow: hidden;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.resume-count {
  font-size: 14px;
  color: #909399;
  background: #f5f7fa;
  padding: 4px 12px;
  border-radius: 12px;
}

/* 表格样式 */
.resume-table {
  border-radius: 12px;
}

.filename-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.file-icon {
  font-size: 20px;
  color: #667eea;
}

.filename {
  font-weight: 500;
  color: #303133;
}

.time-text {
  color: #909399;
  font-size: 13px;
}

.action-btns {
  display: flex;
  justify-content: center;
  gap: 10px;
}

.action-btns .el-button {
  border-radius: 8px;
}

/* 分析结果弹窗样式 */
.result-dialog :deep(.el-dialog__header) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 20px 24px;
  margin: 0;
}

.result-dialog :deep(.el-dialog__title) {
  color: white;
  font-weight: 600;
}

.result-dialog :deep(.el-dialog__headerbtn .el-dialog__close) {
  color: white;
}

.result-content {
  max-height: 65vh;
  overflow-y: auto;
  padding: 0 8px;
}

.result-section {
  margin-bottom: 28px;
  background: #fafafa;
  border-radius: 16px;
  padding: 20px;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
}

.section-icon {
  font-size: 20px;
  color: #667eea;
}

.section-header h4 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.skill-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.skill-tag {
  border-radius: 20px;
}

.no-data-tip {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #909399;
  font-size: 14px;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
}

.result-descriptions {
  border-radius: 12px;
  overflow: hidden;
}

.analysis-cards {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.analysis-card {
  padding: 16px;
  border-radius: 12px;
}

.analysis-card.advantage {
  background: linear-gradient(135deg, #f0f9eb 0%, #e8f5e1 100%);
  border: 1px solid #d4edda;
}

.analysis-card.disadvantage {
  background: linear-gradient(135deg, #fef0f0 0%, #fde8e8 100%);
  border: 1px solid #f5c6cb;
}

.card-label {
  font-weight: 600;
  font-size: 14px;
  margin-bottom: 10px;
}

.advantage .card-label {
  color: #52c41a;
}

.disadvantage .card-label {
  color: #ff4d4f;
}

.analysis-card ul {
  margin: 0;
  padding-left: 20px;
}

.analysis-card li {
  color: #606266;
  font-size: 13px;
  line-height: 1.8;
}

.summary-cards {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.summary-item {
  padding: 16px;
  background: white;
  border-radius: 12px;
  border: 1px solid #f0f0f0;
}

.summary-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  font-size: 14px;
  color: #667eea;
  margin-bottom: 10px;
}

.summary-text {
  color: #606266;
  margin: 0;
  line-height: 1.8;
}

.summary-list {
  margin: 0;
  padding-left: 20px;
}

.summary-list li {
  color: #606266;
  line-height: 1.8;
}

.suggestion-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.suggestion-item {
  padding: 16px;
  border-radius: 12px;
}

.suggestion-item.emergency {
  background: linear-gradient(135deg, #fff2f0 0%, #ffe8e8 100%);
  border: 1px solid #ffccc7;
}

.suggestion-item.longterm {
  background: linear-gradient(135deg, #fffbe6 0%, #fff5cc 100%);
  border: 1px solid #ffe58f;
}

.suggestion-tag {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 12px;
  border-radius: 16px;
  font-size: 12px;
  font-weight: 600;
  margin-bottom: 12px;
}

.emergency .suggestion-tag {
  background: #ff4d4f;
  color: white;
}

.longterm .suggestion-tag {
  background: #e6a23c;
  color: white;
}

.suggestion-item ul {
  margin: 0;
  padding-left: 20px;
}

.suggestion-item li {
  color: #606266;
  line-height: 1.8;
}

.evaluation-section {
  background: linear-gradient(135deg, #667eea10 0%, #764ba210 100%);
}

.evaluation-box {
  background: white;
  padding: 20px;
  border-radius: 12px;
  border: 1px solid #f0f0f0;
}

.evaluation-box pre {
  margin: 0;
  color: #606266;
  line-height: 1.8;
  white-space: pre-wrap;
  font-family: inherit;
  font-size: 14px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.dialog-footer .el-button {
  border-radius: 8px;
}

/* 响应式 */
@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    gap: 20px;
  }

  .upload-btn {
    width: 100%;
    justify-content: center;
  }
}

/* 手机端适配 */
@media (max-width: 600px) {
  .resume-page {
    padding: 0 0 24px 0;
  }

  .page-header {
    padding: 24px 16px;
    border-radius: 16px;
    margin-bottom: 20px;
  }

  .page-title {
    font-size: 22px;
  }

  .page-desc {
    font-size: 13px;
  }

  /* 表格响应式 */
  .resume-table :deep(.el-table__body-wrapper) {
    overflow-x: auto;
  }

  /* 操作按钮在手机上 */
  .action-btns {
    flex-direction: column;
    gap: 8px;
  }

  .action-btns .el-button {
    width: 100%;
    margin: 0 !important;
  }

  /* 分析结果弹窗 */
  .result-dialog {
    margin: 10px !important;
    width: calc(100% - 20px) !important;
  }

  .result-dialog :deep(.el-dialog__body) {
    padding: 16px;
  }

  .result-content {
    max-height: 55vh;
  }

  .result-section {
    margin-bottom: 20px;
    padding: 16px;
    border-radius: 12px;
  }

  .section-header h4 {
    font-size: 14px;
  }

  /* 技能标签 */
  .skill-tags {
    gap: 8px;
  }

  .skill-tag {
    font-size: 12px;
    padding: 4px 10px;
  }

  /* 描述列表 */
  .result-descriptions :deep(.el-descriptions__label) {
    font-size: 12px;
  }

  .result-descriptions :deep(.el-descriptions__content) {
    font-size: 12px;
  }

  /* 卡片样式 */
  .analysis-card {
    padding: 12px;
  }

  .card-label {
    font-size: 13px;
  }

  .analysis-card li {
    font-size: 12px;
    line-height: 1.6;
  }

  /* 建议列表 */
  .suggestion-item {
    padding: 12px;
  }

  .suggestion-item ul li {
    font-size: 12px;
  }

  /* 整体评价 */
  .evaluation-box {
    padding: 12px;
  }

  .evaluation-box pre {
    font-size: 12px;
    line-height: 1.6;
  }

  /* 弹窗底部按钮 */
  .dialog-footer {
    flex-direction: column-reverse;
  }

  .dialog-footer .el-button {
    width: 100%;
  }

  /* 卡片头部 */
  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .resume-count {
    align-self: flex-start;
  }
}
</style>