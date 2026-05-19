<template>
  <div class="resume-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>我的简历</span>
          <el-upload
            :show-file-list="false"
            :before-upload="handleUpload"
            accept=".pdf,.doc,.docx"
          >
            <el-button type="primary" :loading="uploading">上传简历</el-button>
          </el-upload>
        </div>
      </template>
      <el-table :data="resumeList" v-loading="loading" empty-text="暂无简历，请上传">
        <el-table-column prop="filename" label="文件名" />
        <el-table-column prop="gmtCreate" label="上传时间" width="180" />
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getStatusInfo(row.status).type">
              {{ getStatusInfo(row.status).text }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="340">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 'ANALYZED'"
              size="small"
              type="success"
              @click="handleViewResult(row)"
            >
              查看结果
            </el-button>
            <el-button
              v-if="getStatusInfo(row.status).showButton"
              size="small"
              type="warning"
              :loading="analysing === row.id"
              @click="handleAnalyse(row)"
            >
              分析简历
            </el-button>
            <el-button size="small" type="primary" @click="startInterview(row)">
              开始面试
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 分析结果弹窗 -->
    <el-dialog v-model="showResultDialog" title="简历分析结果" width="800px" destroy-on-close>
      <div v-if="currentResume" class="result-content">
        <!-- 技能标签 -->
        <div class="result-section">
          <h4>技能标签</h4>
          <div class="skill-tags">
            <el-tag v-for="(tag, index) in analysisResult.skillTags" :key="index" type="info" class="skill-tag">
              {{ tag }}
            </el-tag>
          </div>
          <p v-if="!analysisResult.skillTags?.length" class="no-data">暂无技能数据</p>
        </div>

        <!-- 基础定位 -->
        <div class="result-section" v-if="analysisResult.basicPosition">
          <h4>基础定位</h4>
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="程序员级别">{{ analysisResult.basicPosition.level?.desc || '-' }}</el-descriptions-item>
            <el-descriptions-item label="岗位适配">{{ analysisResult.basicPosition.positionFitDegree || '-' }}</el-descriptions-item>
            <el-descriptions-item label="级别判断依据" :span="2">{{ analysisResult.basicPosition.levelJudgmentBasis || '-' }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 技能分析 -->
        <div class="result-section" v-if="analysisResult.skillAnalysis">
          <h4>技能分析</h4>
          <div class="analysis-group">
            <div class="analysis-item advantage">
              <span class="analysis-label">优点</span>
              <ul>
                <li v-for="(item, i) in analysisResult.skillAnalysis.advantages" :key="i">{{ item }}</li>
              </ul>
            </div>
            <div class="analysis-item disadvantage">
              <span class="analysis-label">缺点</span>
              <ul>
                <li v-for="(item, i) in analysisResult.skillAnalysis.disadvantages" :key="i">{{ item }}</li>
              </ul>
            </div>
          </div>
        </div>

        <!-- 项目分析 -->
        <div class="result-section" v-if="analysisResult.projectAnalysis">
          <h4>项目经验分析</h4>
          <div class="analysis-group">
            <div class="analysis-item advantage">
              <span class="analysis-label">优点</span>
              <ul>
                <li v-for="(item, i) in analysisResult.projectAnalysis.advantages" :key="i">{{ item }}</li>
              </ul>
            </div>
            <div class="analysis-item disadvantage">
              <span class="analysis-label">缺点</span>
              <ul>
                <li v-for="(item, i) in analysisResult.projectAnalysis.disadvantages" :key="i">{{ item }}</li>
              </ul>
            </div>
          </div>
        </div>

        <!-- 整体总结 -->
        <div class="result-section" v-if="analysisResult.overallSummary">
          <h4>整体总结</h4>
          <el-card shadow="never">
            <div class="summary-item">
              <span class="summary-label">核心竞争力</span>
              <p>{{ analysisResult.overallSummary.coreCompetence || '-' }}</p>
            </div>
            <div class="summary-item" v-if="analysisResult.overallSummary.coreShortcomings?.length">
              <span class="summary-label">核心短板</span>
              <ul>
                <li v-for="(item, i) in analysisResult.overallSummary.coreShortcomings" :key="i">{{ item }}</li>
              </ul>
            </div>
            <div class="summary-item" v-if="analysisResult.overallSummary.adaptationSuggestions?.length">
              <span class="summary-label">适配建议</span>
              <ul>
                <li v-for="(item, i) in analysisResult.overallSummary.adaptationSuggestions" :key="i">{{ item }}</li>
              </ul>
            </div>
          </el-card>
        </div>

        <!-- 改进建议 -->
        <div class="result-section" v-if="analysisResult.improvementSuggestion">
          <h4>改进建议</h4>
          <div class="suggestion-list">
            <div class="suggestion-item emergency" v-if="analysisResult.improvementSuggestion.emergencyItems?.length">
              <span class="suggestion-tag">紧急</span>
              <ul>
                <li v-for="(item, i) in analysisResult.improvementSuggestion.emergencyItems" :key="i">{{ item }}</li>
              </ul>
            </div>
            <div class="suggestion-item longterm" v-if="analysisResult.improvementSuggestion.longTermItems?.length">
              <span class="suggestion-tag">长期</span>
              <ul>
                <li v-for="(item, i) in analysisResult.improvementSuggestion.longTermItems" :key="i">{{ item }}</li>
              </ul>
            </div>
          </div>
        </div>

        <!-- 整体评价 -->
        <div class="result-section" v-if="analysisResult.evaluation">
          <h4>面试官点评</h4>
          <div class="evaluation-box">{{ analysisResult.evaluation }}</div>
        </div>
      </div>
      <template #footer>
        <el-button @click="showResultDialog = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { uploadResumeApi, getResumeListApi, analyseResumeApi } from '@/api/interview'

interface AnalysisResult {
  // 基础定位
  basicPosition?: {
    level?: { desc?: string }
    levelJudgmentBasis?: string
    positionFitDegree?: string
    potentialGap?: string
  }
  // 技能分析
  skillAnalysis?: {
    advantages?: string[]
    disadvantages?: string[]
    detailsEvidence?: string[]
  }
  // 技能标签
  skillTags?: string[]
  // 整体评价
  evaluation?: string
  // 整体总结
  overallSummary?: {
    coreCompetence?: string
    coreShortcomings?: string[]
    adaptationSuggestions?: string[]
  }
  // 改进建议
  improvementSuggestion?: {
    emergencyItems?: string[]
    longTermItems?: string[]
    priorityOrder?: string[]
  }
  // 项目分析
  projectAnalysis?: {
    advantages?: string[]
    disadvantages?: string[]
    detailsEvidence?: string[]
  }
  // 教育分析
  educationAnalysis?: {
    advantages?: string[]
    disadvantages?: string[]
    detailsEvidence?: string[]
  }
  // 对标差距
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
const analysing = ref<number | null>(null) // 正在分析的简历ID

// 分析结果弹窗
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

// 分析简历
async function handleAnalyse(row: any) {
  if (row.status !== 'UPLOADED') {
    ElMessage.warning('该简历已分析或正在分析中')
    return
  }
  analysing.value = row.id
  try {
    await analyseResumeApi(row.id)
    ElMessage.success('简历分析已启动，请稍候')
    // 刷新列表查看状态变化
    setTimeout(() => fetchResumeList(), 1000)
  } catch (e) {
    // handled
  } finally {
    analysing.value = null
  }
}

// 查看分析结果
async function handleViewResult(row: any) {
  currentResume.value = row
  // 尝试解析evaluation字段
  try {
    if (row.evaluation) {
      analysisResult.value = JSON.parse(row.evaluation)
    } else if (row.skillTags) {
      // 如果没有完整的evaluation，至少显示skillTags
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

// 获取状态标签类型和文本
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
</script>

<style scoped>
.resume-page {
  max-width: 1000px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* 分析结果弹窗样式 */
.result-content {
  max-height: 60vh;
  overflow-y: auto;
}

.result-section {
  margin-bottom: 20px;
}

.result-section h4 {
  color: #409eff;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #ebeef5;
}

.skill-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.skill-tag {
  margin: 0;
}

/* 分析组样式 */
.analysis-group {
  display: flex;
  gap: 20px;
}

.analysis-item {
  flex: 1;
  padding: 12px;
  border-radius: 8px;
}

.analysis-item.advantage {
  background: #f0f9eb;
}

.analysis-item.advantage .analysis-label {
  color: #67c23a;
}

.analysis-item.disadvantage {
  background: #fef0f0;
}

.analysis-item.disadvantage .analysis-label {
  color: #f56c6c;
}

.analysis-label {
  font-weight: 600;
  font-size: 14px;
  display: block;
  margin-bottom: 8px;
}

.analysis-item ul {
  margin: 0;
  padding-left: 18px;
}

.analysis-item li {
  color: #606266;
  font-size: 13px;
  line-height: 1.6;
}

/* 总结样式 */
.summary-item {
  margin-bottom: 12px;
}

.summary-item:last-child {
  margin-bottom: 0;
}

.summary-label {
  color: #909399;
  font-size: 13px;
  display: block;
  margin-bottom: 4px;
}

.summary-item p {
  color: #303133;
  margin: 0;
  line-height: 1.6;
}

.summary-item ul {
  margin: 0;
  padding-left: 20px;
}

.summary-item li {
  color: #606266;
  line-height: 1.6;
}

/* 建议样式 */
.suggestion-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.suggestion-item {
  padding: 12px;
  border-radius: 8px;
}

.suggestion-item.emergency {
  background: #fef0f0;
}

.suggestion-item.emergency .suggestion-tag {
  background: #f56c6c;
}

.suggestion-item.longterm {
  background: #fdf6ec;
}

.suggestion-item.longterm .suggestion-tag {
  background: #e6a23c;
}

.suggestion-tag {
  color: #fff;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  display: inline-block;
  margin-bottom: 8px;
}

.suggestion-item ul {
  margin: 0;
  padding-left: 20px;
}

.suggestion-item li {
  color: #606266;
  line-height: 1.6;
}

/* 评价框样式 */
.evaluation-box {
  background: #f5f7fa;
  padding: 16px;
  border-radius: 8px;
  color: #606266;
  line-height: 1.8;
  white-space: pre-wrap;
}

.no-data {
  color: #909399;
  font-style: italic;
}
</style>
