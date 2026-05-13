<template>
  <div class="login-container">
    <div class="login-card">
      <h2 class="title">云面试系统</h2>
      <p class="subtitle">AI驱动的智能面试平台</p>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="0">
        <el-form-item prop="phone">
          <el-input
            v-model="form.phone"
            placeholder="请输入手机号"
            prefix-icon="Phone"
            size="large"
          />
        </el-form-item>
        <el-form-item prop="captcha">
          <div class="captcha-row">
            <el-input
              v-model="form.captcha"
              placeholder="请输入图形验证码"
              prefix-icon="Picture"
              size="large"
              maxlength="4"
            />
            <img
              v-if="captchaUrl"
              :src="captchaUrl"
              class="captcha-img"
              title="点击刷新验证码"
              @click="refreshCaptcha"
            />
            <div v-else class="captcha-placeholder" @click="refreshCaptcha">
              点击获取
            </div>
          </div>
        </el-form-item>
        <el-form-item prop="code">
          <div class="code-row">
            <el-input
              v-model="form.code"
              placeholder="请输入短信验证码"
              prefix-icon="Message"
              size="large"
            />
            <el-button
              type="primary"
              size="large"
              :disabled="countdown > 0"
              @click="handleSendCode"
            >
              {{ countdown > 0 ? `${countdown}s` : '获取验证码' }}
            </el-button>
          </div>
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            class="login-btn"
            :loading="loading"
            @click="handleLogin"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { loginApi, sendSmsCodeApi, getCaptchaUrl } from '@/api/account'
import { useUserStore } from '@/store/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const formRef = ref<FormInstance>()
const loading = ref(false)
const countdown = ref(0)
const captchaUrl = ref('')

const form = reactive({
  phone: '',
  captcha: '',
  code: '',
})

const rules: FormRules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' },
  ],
  captcha: [
    { required: true, message: '请输入图形验证码', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9]{4}$/, message: '验证码为4位数字或字母', trigger: 'blur' },
  ],
  code: [
    { required: true, message: '请输入短信验证码', trigger: 'blur' },
  ],
}

// 手机号输入完成后自动加载验证码图片
watch(() => form.phone, (val) => {
  if (/^1[3-9]\d{9}$/.test(val)) {
    refreshCaptcha()
  } else {
    captchaUrl.value = ''
  }
})

function refreshCaptcha() {
  if (!form.phone || !/^1[3-9]\d{9}$/.test(form.phone)) {
    ElMessage.warning('请先输入正确的手机号')
    return
  }
  captchaUrl.value = getCaptchaUrl(form.phone, 'LOGIN')
  form.captcha = ''
}

async function handleSendCode() {
  if (!form.phone || !/^1[3-9]\d{9}$/.test(form.phone)) {
    ElMessage.warning('请输入正确的手机号')
    return
  }
  if (!form.captcha || !/^[a-zA-Z0-9]{4}$/.test(form.captcha)) {
    ElMessage.warning('请输入4位图形验证码')
    return
  }
  try {
    await sendSmsCodeApi({
      identifier: form.phone,
      captcha: form.captcha,
      type: 'LOGIN',
    })
    ElMessage.success('验证码已发送')
    countdown.value = 60
    const timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) clearInterval(timer)
    }, 1000)
  } catch (e) {
    // 验证码错误时刷新图片
    refreshCaptcha()
  }
}

async function handleLogin() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const res = await loginApi({ identifier: form.phone, checkCode: form.code, type: 'LOGIN' })
    const token = res.data?.token
    if (!token) {
      ElMessage.error('登录异常：未获取到token')
      return
    }
    userStore.setToken(token)
    if (res.data.accountDTO) {
      userStore.setUserInfo(res.data.accountDTO)
    }
    ElMessage.success('登录成功')
    const redirect = (route.query.redirect as string) || '/home'
    const failure = await router.push(redirect)
    if (failure) {
      console.error('路由跳转失败:', failure)
      window.location.href = redirect
    }
  } catch (e: any) {
    console.error('登录流程异常:', e)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
  width: 400px;
  padding: 40px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
}

.title {
  text-align: center;
  font-size: 28px;
  color: #303133;
  margin-bottom: 8px;
}

.subtitle {
  text-align: center;
  color: #909399;
  margin-bottom: 30px;
}

.captcha-row {
  display: flex;
  gap: 12px;
  width: 100%;
  align-items: center;
}

.captcha-row .el-input {
  flex: 1;
}

.captcha-img {
  height: 40px;
  width: 120px;
  cursor: pointer;
  border-radius: 4px;
  border: 1px solid #dcdfe6;
}

.captcha-placeholder {
  height: 40px;
  width: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px dashed #dcdfe6;
  border-radius: 4px;
  color: #909399;
  font-size: 12px;
  cursor: pointer;
}

.captcha-placeholder:hover {
  border-color: #409eff;
  color: #409eff;
}

.code-row {
  display: flex;
  gap: 12px;
  width: 100%;
}

.code-row .el-input {
  flex: 1;
}

.login-btn {
  width: 100%;
}
</style>
