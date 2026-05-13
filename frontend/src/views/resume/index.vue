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
        <el-table-column prop="fileName" label="文件名" />
        <el-table-column prop="position" label="目标岗位" />
        <el-table-column prop="gmtCreate" label="上传时间" width="180" />
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="row.status === 'PARSED' ? 'success' : 'info'">
              {{ row.status === 'PARSED' ? '已解析' : '解析中' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="startInterview(row)">
              开始面试
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { uploadResumeApi, getResumeListApi } from '@/api/interview'

const router = useRouter()
const resumeList = ref<any[]>([])
const loading = ref(false)
const uploading = ref(false)

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
</style>
