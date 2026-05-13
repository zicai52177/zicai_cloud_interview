<template>
  <div class="interview-detail">
    <el-card v-loading="loading">
      <template #header>
        <div class="detail-header">
          <span>面试详情</span>
          <el-button @click="$router.back()">返回</el-button>
        </div>
      </template>
      <div v-if="detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="面试岗位">{{ detail.position }}</el-descriptions-item>
          <el-descriptions-item label="面试得分">
            <el-tag :type="detail.score >= 80 ? 'success' : detail.score >= 60 ? 'warning' : 'danger'">
              {{ detail.score }}分
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="面试时间">{{ detail.gmtCreate }}</el-descriptions-item>
          <el-descriptions-item label="面试状态">{{ detail.status }}</el-descriptions-item>
        </el-descriptions>
        <div class="chat-history" v-if="detail.messages">
          <h4>面试对话</h4>
          <div
            v-for="(msg, idx) in detail.messages"
            :key="idx"
            :class="['message', msg.role === 'assistant' ? 'ai-msg' : 'user-msg']"
          >
            <div class="msg-role">{{ msg.role === 'assistant' ? 'AI面试官' : '我' }}</div>
            <div class="msg-content">{{ msg.content }}</div>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getInterviewDetailApi } from '@/api/interview'

const route = useRoute()
const detail = ref<any>(null)
const loading = ref(false)

onMounted(async () => {
  const id = Number(route.params.id)
  if (!id) return
  loading.value = true
  try {
    const res = await getInterviewDetailApi(id)
    detail.value = res.data
  } catch (e) {
    // handled
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.interview-detail {
  max-width: 900px;
  margin: 0 auto;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chat-history {
  margin-top: 24px;
}

.message {
  padding: 12px 16px;
  margin-bottom: 12px;
  border-radius: 8px;
}

.ai-msg {
  background: #f0f9ff;
  border-left: 3px solid #409eff;
}

.user-msg {
  background: #f0fdf4;
  border-left: 3px solid #67c23a;
}

.msg-role {
  font-weight: 600;
  font-size: 13px;
  margin-bottom: 4px;
  color: #606266;
}

.msg-content {
  white-space: pre-wrap;
  line-height: 1.6;
}
</style>
