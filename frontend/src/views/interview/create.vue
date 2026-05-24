<template>
  <div class="interview-create-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <el-button text @click="$router.back()">
          <el-icon><ArrowLeft /></el-icon>
          返回
        </el-button>
        <h1 class="page-title">
          <el-icon class="title-icon"><Plus /></el-icon>
          创建面试
        </h1>
      </div>
    </div>

    <!-- 创建表单 -->
    <el-card shadow="never" class="form-card">
      <template #header>
        <div class="card-header">
          <span>面试配置</span>
          <el-tag type="info" size="small">必填项已标注 *</el-tag>
        </div>
      </template>

      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="140px"
        label-position="right"
        class="create-form"
      >
        <!-- 目标岗位 -->
        <el-form-item label="目标岗位" prop="targetPosition">
          <el-input
            v-model="formData.targetPosition"
            placeholder="请输入您期望面试的岗位"
            maxlength="50"
            show-word-limit
          >
            <template #prefix>
              <el-icon><Briefcase /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <!-- 简历选择 -->
        <el-form-item label="关联简历" prop="resumeId">
          <el-select
            v-model="formData.resumeId"
            placeholder="请选择简历（可选）"
            clearable
            filterable
            class="resume-select"
          >
            <el-option
              v-for="resume in resumeList"
              :key="resume.id"
              :label="resume.fileName || `简历 ${resume.id}`"
              :value="resume.id"
            >
              <div class="resume-option">
                <span class="resume-name">{{ resume.fileName || `简历 ${resume.id}` }}</span>
                <span class="resume-time">{{ formatTime(resume.gmtCreate) }}</span>
              </div>
            </el-option>
          </el-select>
          <div class="form-tip">
            关联简历可以让AI更准确地评估您的背景
          </div>
        </el-form-item>

        <!-- 面试类型 -->
        <el-form-item label="面试类型" prop="interviewType">
          <el-radio-group v-model="formData.interviewType" class="type-group">
            <el-radio value="COMMON_INTERVIEW">
              <div class="type-option">
                <span class="type-label">普通面试</span>
                <span class="type-desc">涵盖多个领域的综合面试</span>
              </div>
            </el-radio>
            <el-radio value="SPECIAL_INTERVIEW">
              <div class="type-option">
                <span class="type-label">专项面试</span>
                <span class="type-desc">针对特定技术或领域的深度面试</span>
              </div>
            </el-radio>
          </el-radio-group>
        </el-form-item>

        <!-- 专项内容（当选择专项面试时显示） -->
        <el-form-item
          v-if="formData.interviewType === 'SPECIAL_INTERVIEW'"
          label="专项内容"
          prop="specialContent"
        >
          <el-input
            v-model="formData.specialContent"
            type="textarea"
            :rows="3"
            placeholder="请输入您想重点考察的领域，如：Java并发编程、数据库优化、系统设计等"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>

        <!-- 期望薪资 -->
        <el-form-item label="期望薪资" prop="expectedSalary">
          <el-input
            v-model="formData.expectedSalary"
            placeholder="请输入期望薪资，如：20K-30K"
            maxlength="30"
          >
            <template #prefix>
              <el-icon><Money /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <!-- 工作年限 -->
        <el-form-item label="工作年限" prop="workYears">
          <el-select v-model="formData.workYears" placeholder="请选择工作年限" class="work-years-select">
            <el-option label="应届生" value="0" />
            <el-option label="1年以下" value="<1" />
            <el-option label="1-3年" value="1-3" />
            <el-option label="3-5年" value="3-5" />
            <el-option label="5-10年" value="5-10" />
            <el-option label="10年以上" value=">10" />
          </el-select>
        </el-form-item>

        <!-- 目标城市 -->
        <el-form-item label="目标城市" prop="targetCity">
          <el-input
            v-model="formData.targetCity"
            placeholder="请输入期望工作的城市"
            maxlength="50"
          >
            <template #prefix>
              <el-icon><Location /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <!-- 表单操作 -->
        <el-form-item class="form-actions">
          <el-button size="large" @click="$router.back()">取消</el-button>
          <el-button
            type="primary"
            size="large"
            :loading="submitting"
            @click="handleSubmit"
          >
            <el-icon v-if="!submitting"><VideoPlay /></el-icon>
            {{ submitting ? '创建中...' : '开始面试' }}
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 面试说明 -->
    <el-card shadow="never" class="tips-card">
      <template #header>
        <span class="tips-title">
          <el-icon><InfoFilled /></el-icon>
          面试须知
        </span>
      </template>
      <ul class="tips-list">
        <li>面试将根据您的简历和配置信息，生成针对性的面试题目</li>
        <li>每个面试包含多轮问答，AI面试官会根据您的回答进行追问</li>
        <li>面试结束后，系统将生成综合评价报告和得分</li>
        <li>请保持网络畅通，回答时尽量详尽以获得更准确的评估</li>
      </ul>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  ArrowLeft,
  Plus,
  Briefcase,
  Money,
  Location,
  VideoPlay,
  InfoFilled
} from '@element-plus/icons-vue'
import {
  createInterviewApi,
  getResumeListApi
} from '@/api/interview'
import type { FormInstance, FormRules } from 'element-plus'

const router = useRouter()
const formRef = ref<FormInstance>()
const submitting = ref(false)
const resumeList = ref<any[]>([])

// 表单数据
const formData = reactive({
  targetPosition: '',
  resumeId: undefined as number | undefined,
  interviewType: 'COMMON_INTERVIEW',
  specialContent: '',
  expectedSalary: '',
  workYears: '',
  targetCity: ''
})

// 表单验证规则
const formRules: FormRules = {
  targetPosition: [
    { required: true, message: '请输入目标岗位', trigger: 'blur' },
    { min: 2, max: 50, message: '岗位名称长度在 2 到 50 个字符', trigger: 'blur' }
  ]
}

// 加载简历列表
async function fetchResumes() {
  try {
    const res = await getResumeListApi()
    resumeList.value = res.data || []
  } catch (e) {
    // handled by request interceptor
  }
}

// 提交表单
async function handleSubmit() {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
  } catch {
    ElMessage.warning('请完善表单信息')
    return
  }

  submitting.value = true
  try {
    const res = await createInterviewApi({
      targetPosition: formData.targetPosition,
      resumeId: formData.resumeId,
      interviewType: formData.interviewType,
      specialContent: formData.specialContent,
      expectedSalary: formData.expectedSalary,
      workYears: formData.workYears,
      targetCity: formData.targetCity
    })

    if (res.data?.interviewId) {
      ElMessage.success('面试创建成功，即将开始...')
      // 跳转到面试进行页面
      setTimeout(() => {
        router.push(`/interview/conduct/${res.data.interviewId}`)
      }, 1000)
    }
  } catch (e) {
    // handled by request interceptor
  } finally {
    submitting.value = false
  }
}

// 格式化时间
function formatTime(timeStr?: string) {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  return date.toLocaleDateString('zh-CN')
}

onMounted(() => {
  fetchResumes()
})
</script>

<style scoped>
.interview-create-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 24px;
}

/* 页面头部 */
.page-header {
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

/* 表单卡片 */
.form-card {
  border-radius: 16px;
  margin-bottom: 24px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
}

.create-form {
  padding: 20px 0;
}

.resume-select {
  width: 100%;
  max-width: 400px;
}

.resume-option {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 4px 0;
}

.resume-name {
  font-weight: 500;
}

.resume-time {
  color: #8c8c8c;
  font-size: 12px;
}

.form-tip {
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
}

/* 面试类型 */
.type-group {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.type-option {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.type-label {
  font-weight: 600;
}

.type-desc {
  font-size: 12px;
  color: #8c8c8c;
}

/* 工作年限选择 */
.work-years-select {
  width: 200px;
}

/* 表单操作按钮 */
.form-actions {
  margin-top: 40px;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
}

.form-actions :deep(.el-form-item__content) {
  justify-content: flex-end;
  gap: 16px;
}

/* 提示卡片 */
.tips-card {
  border-radius: 12px;
  background: #fafafa;
}

.tips-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
}

.tips-list {
  margin: 0;
  padding-left: 20px;
  color: #606266;
  line-height: 2;
}

.tips-list li {
  position: relative;
}

.tips-list li::marker {
  color: #667eea;
}
</style>