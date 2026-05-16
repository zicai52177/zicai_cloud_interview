<template>
  <div class="login-container">
    <div class="login-card">
      <h2 class="title">云面试系统</h2>
      <p class="subtitle">AI驱动的智能面试平台</p>

      <!-- 登录方式切换 -->
      <div class="login-tabs">
        <div
          :class="['tab-item', { active: loginMethod === 'sms' }]"
          @click="switchToSmsLogin"
        >
          短信登录
        </div>
        <div
          :class="['tab-item', { active: loginMethod === 'wechat' }]"
          @click="switchToWechatLogin"
        >
          微信登录
        </div>
      </div>

      <!-- 短信验证码登录 -->
      <div v-show="loginMethod === 'sms'" class="login-form">
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

      <!-- 微信登录 -->
      <div v-show="loginMethod === 'wechat'" class="login-form wechat-login">
        <div class="qrcode-container">
          <div v-if="qrcodeLoading" class="qrcode-loading">
            <el-icon class="is-loading"><Loading /></el-icon>
            <p>加载中...</p>
          </div>
          <div v-else-if="qrcodeError" class="qrcode-error">
            <p>加载失败</p>
            <el-button size="small" @click="loadWechatQrcode">重新加载</el-button>
          </div>
          <img
            v-else-if="qrcodeUrl"
            :src="qrcodeUrl"
            class="qrcode-img"
            alt="微信登录二维码"
          />
          <div v-else class="qrcode-placeholder">
            <el-icon :size="48"><User /></el-icon>
          </div>
        </div>
        <p class="wechat-hint">
          <span v-if="loginStatus === 'waiting'">请使用微信扫描二维码</span>
          <span v-else-if="loginStatus === 'scanned'">已扫描，请在手机确认登录</span>
          <span v-else-if="loginStatus === 'expired'">二维码已过期</span>
          <span v-else>打开微信扫一扫登录</span>
        </p>
        <el-button
          v-if="loginStatus === 'expired'"
          type="primary"
          text
          @click="loadWechatQrcode"
        >
          刷新二维码
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, watch, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Loading } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { loginApi, sendSmsCodeApi, getCaptchaUrl, getWechatQrcodeApi, getWechatLoginResultApi } from '@/api/account'
import { useUserStore } from '@/store/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const formRef = ref<FormInstance>()
const loading = ref(false)
const countdown = ref(0)
const captchaUrl = ref('')

// 登录方式
const loginMethod = ref<'sms' | 'wechat'>('sms')

// 短信登录表单
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

// 微信登录相关
const qrcodeUrl = ref('')
const qrcodeLoading = ref(false)
const qrcodeError = ref(false)
const sceneId = ref('')
const loginStatus = ref<'init' | 'waiting' | 'scanned' | 'expired' | 'success'>('init')
let pollTimer: ReturnType<typeof setTimeout> | null = null
let expireTimer: ReturnType<typeof setTimeout> | null = null

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
    await router.push(redirect)
  } catch (e: any) {
    console.error('登录流程异常:', e)
  } finally {
    loading.value = false
  }
}

// 切换到短信登录
function switchToSmsLogin() {
  loginMethod.value = 'sms'
  stopPolling()
}

// 切换到微信登录
async function switchToWechatLogin() {
  loginMethod.value = 'wechat'
  if (!qrcodeUrl.value && !qrcodeLoading.value) {
    await loadWechatQrcode()
  }
}

// 加载微信二维码
async function loadWechatQrcode() {
  qrcodeLoading.value = true
  qrcodeError.value = false
  qrcodeUrl.value = ''
  loginStatus.value = 'init'

  try {
    const res = await getWechatQrcodeApi()
    console.log('API响应:', res)
    console.log('qrCodeUrl:', res.data?.qrCodeUrl)

    const qrData = res.data?.data?.qrCodeUrl ? res.data.data : null
    if (qrData && qrData.qrCodeUrl) {
      qrcodeUrl.value = qrData.qrCodeUrl
      sceneId.value = qrData.sceneId
      loginStatus.value = 'waiting'
      console.log('二维码URL:', qrcodeUrl.value)
      console.log('sceneId:', sceneId.value)

      // 启动轮询
      startPolling()

      // 5分钟后二维码过期
      if (expireTimer) clearTimeout(expireTimer)
      expireTimer = setTimeout(() => {
        if (loginStatus.value !== 'success') {
          loginStatus.value = 'expired'
          stopPolling()
        }
      }, 5 * 60 * 1000)
    } else {
      console.error('获取二维码失败，数据为空:', res.data)
      ElMessage.error('获取二维码失败')
      qrcodeError.value = true
    }
  } catch (e: any) {
    console.error('加载微信二维码失败:', e)
    ElMessage.error('加载二维码失败: ' + (e.message || '未知错误'))
    qrcodeError.value = true
  } finally {
    qrcodeLoading.value = false
  }
}

// 启动轮询
function startPolling() {
  if (pollTimer) clearTimeout(pollTimer)

  const poll = async () => {
    if (!sceneId.value || loginStatus.value === 'success') return

    try {
      const res = await getWechatLoginResultApi(sceneId.value)
      console.log('轮询结果:', res)

      if (res.code === 0 && res.data) {
        const status = res.data.status || res.data.loginStatus || ''

        if (status === 'SCANNED') {
          loginStatus.value = 'scanned'
          pollTimer = setTimeout(poll, 2000)
        } else if (status === 'CONFIRMED' || status === 'success') {
          loginStatus.value = 'success'
          const token = res.data.token || res.data.accessToken
          if (token) {
            userStore.setToken(token)
            if (res.data.accountDTO) {
              userStore.setUserInfo(res.data.accountDTO)
            }
            ElMessage.success('登录成功')
            const redirect = (route.query.redirect as string) || '/home'
            await router.push(redirect)
          }
        } else {
          // WAITING 或其他状态，继续轮询
          pollTimer = setTimeout(poll, 2000)
        }
      } else {
        // 请求失败，继续轮询
        pollTimer = setTimeout(poll, 2000)
      }
    } catch (e) {
      console.error('轮询登录结果失败:', e)
      // 失败也继续轮询
      pollTimer = setTimeout(poll, 2000)
    }
  }

  pollTimer = setTimeout(poll, 2000)
}

// 停止轮询
function stopPolling() {
  if (pollTimer) {
    clearTimeout(pollTimer)
    pollTimer = null
  }
  if (expireTimer) {
    clearTimeout(expireTimer)
    expireTimer = null
  }
}

// 组件卸载时清理
onUnmounted(() => {
  stopPolling()
})
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

/* 登录方式切换 */
.login-tabs {
  display: flex;
  margin-bottom: 24px;
  border-bottom: 1px solid #ebeef5;
}

.tab-item {
  flex: 1;
  text-align: center;
  padding: 12px 0;
  color: #909399;
  cursor: pointer;
  transition: all 0.3s;
  position: relative;
}

.tab-item:hover {
  color: #409eff;
}

.tab-item.active {
  color: #409eff;
  font-weight: 500;
}

.tab-item.active::after {
  content: '';
  position: absolute;
  bottom: -1px;
  left: 50%;
  transform: translateX(-50%);
  width: 60%;
  height: 2px;
  background: #409eff;
}

.login-form {
  min-height: 280px;
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

/* 微信登录样式 */
.wechat-login {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 20px;
}

.qrcode-container {
  width: 200px;
  height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
  border-radius: 8px;
  margin-bottom: 16px;
}

.qrcode-img {
  width: 180px;
  height: 180px;
  object-fit: contain;
}

.qrcode-loading,
.qrcode-error,
.qrcode-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #909399;
}

.qrcode-loading p,
.qrcode-error p {
  margin-top: 8px;
  font-size: 14px;
}

.wechat-hint {
  text-align: center;
  color: #606266;
  font-size: 14px;
  margin-bottom: 12px;
}
</style>