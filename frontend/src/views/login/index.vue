<template>
  <div class="login-page">
    <!-- 左侧系统介绍区域 -->
    <div class="intro-section">
      <div class="intro-content">
        <!-- Logo和标题 -->
        <div class="brand">
          <div class="logo">
            <el-icon :size="48"><Microphone /></el-icon>
          </div>
          <h1 class="brand-name">云面试系统</h1>
          <p class="brand-slogan">AI驱动的智能面试平台</p>
        </div>

        <!-- 功能特点 -->
        <div class="features">
          <div class="feature-item" v-for="(feature, index) in features" :key="index">
            <div class="feature-icon">
              <el-icon :size="24"><component :is="feature.icon" /></el-icon>
            </div>
            <div class="feature-text">
              <h3>{{ feature.title }}</h3>
              <p>{{ feature.desc }}</p>
            </div>
          </div>
        </div>

        <!-- 统计数据 -->
        <div class="stats">
          <div class="stat-item" v-for="(stat, index) in stats" :key="index">
            <div class="stat-value">{{ stat.value }}</div>
            <div class="stat-label">{{ stat.label }}</div>
          </div>
        </div>
      </div>

      <!-- 装饰背景 -->
      <div class="intro-decoration">
        <div class="circle circle-1"></div>
        <div class="circle circle-2"></div>
        <div class="circle circle-3"></div>
      </div>
    </div>

    <!-- 右侧登录区域 -->
    <div class="login-section">
      <div class="login-card">
        <!-- 登录方式切换 -->
        <div class="login-tabs">
          <div
            :class="['tab-item', { active: loginMethod === 'sms' }]"
            @click="switchToSmsLogin"
          >
            <el-icon><Message /></el-icon>
            短信登录
          </div>
          <div
            :class="['tab-item', { active: loginMethod === 'wechat' }]"
            @click="switchToWechatLogin"
          >
            <el-icon><ChatDotRound /></el-icon>
            微信登录
          </div>
        </div>

        <!-- 短信验证码登录 -->
        <div v-show="loginMethod === 'sms'" class="login-form">
          <div class="form-header">
            <h2>欢迎回来</h2>
            <p>请输入您的手机号和验证码登录</p>
          </div>

          <el-form ref="formRef" :model="form" :rules="rules" label-width="0">
            <el-form-item prop="phone">
              <div class="input-wrapper">
                <el-icon class="input-icon"><Phone /></el-icon>
                <el-input
                  v-model="form.phone"
                  placeholder="请输入手机号"
                  size="large"
                  clearable
                />
              </div>
            </el-form-item>

            <el-form-item prop="captcha">
              <div class="captcha-wrapper">
                <div class="input-wrapper captcha-input">
                  <el-icon class="input-icon"><Picture /></el-icon>
                  <el-input
                    v-model="form.captcha"
                    placeholder="图形验证码"
                    size="large"
                    maxlength="4"
                    clearable
                  />
                </div>
                <div class="captcha-img-wrapper" @click="refreshCaptcha">
                  <img v-if="captchaUrl" :src="captchaUrl" class="captcha-img" alt="验证码" />
                  <div v-else class="captcha-placeholder">
                    <el-icon><Refresh /></el-icon>
                  </div>
                </div>
              </div>
            </el-form-item>

            <el-form-item prop="code">
              <div class="code-wrapper">
                <div class="input-wrapper code-input">
                  <el-icon class="input-icon"><Lock /></el-icon>
                  <el-input
                    v-model="form.code"
                    placeholder="短信验证码"
                    size="large"
                    maxlength="6"
                    clearable
                  />
                </div>
                <el-button
                  class="send-code-btn"
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
                <span v-if="!loading">登 录</span>
                <span v-else>登录中...</span>
              </el-button>
            </el-form-item>
          </el-form>
        </div>

        <!-- 微信登录 -->
        <div v-show="loginMethod === 'wechat'" class="login-form wechat-form">
          <div class="form-header">
            <h2>微信扫码登录</h2>
            <p>使用微信App扫描下方二维码快速登录</p>
          </div>

          <div class="qrcode-container">
            <div v-if="qrcodeLoading" class="qrcode-loading">
              <el-icon class="is-loading" :size="40"><Loading /></el-icon>
              <p>加载中...</p>
            </div>
            <div v-else-if="qrcodeError" class="qrcode-error">
              <el-icon :size="48"><WarningFilled /></el-icon>
              <p>加载失败</p>
              <el-button type="primary" @click="loadWechatQrcode">重新加载</el-button>
            </div>
            <img
              v-else-if="qrcodeUrl"
              :src="qrcodeUrl"
              class="qrcode-img"
              alt="微信登录二维码"
            />
            <div v-else class="qrcode-placeholder">
              <el-icon :size="48"><Grid /></el-icon>
              <p>点击加载二维码</p>
              <el-button type="primary" @click="loadWechatQrcode">加载二维码</el-button>
            </div>
          </div>

          <div class="wechat-status">
            <p v-if="loginStatus === 'waiting'" class="status-waiting">
              <el-icon class="blink"><Loading /></el-icon>
              请使用微信扫描二维码
            </p>
            <p v-else-if="loginStatus === 'scanned'" class="status-scanned">
              <el-icon><CircleCheck /></el-icon>
              已扫描，请在手机确认登录
            </p>
            <p v-else-if="loginStatus === 'expired'" class="status-expired">
              <el-icon><WarningFilled /></el-icon>
              二维码已过期
              <el-button type="primary" link @click="loadWechatQrcode">点击刷新</el-button>
            </p>
            <p v-else class="status-init">
              打开微信 → 扫一扫 → 确认登录
            </p>
          </div>
        </div>

        <!-- 底部信息 -->
        <div class="login-footer">
          <p>登录即表示同意<a href="#">《用户协议》</a>和<a href="#">《隐私政策》</a></p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, watch, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Microphone,
  Message,
  ChatDotRound,
  Phone,
  Picture,
  Lock,
  Refresh,
  Loading,
  WarningFilled,
  CircleCheck,
  Grid,
  DocumentCopy,
  VideoCamera,
  Monitor,
  DataLine
} from '@element-plus/icons-vue'
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

// 功能特点数据
const features = [
  {
    icon: VideoCamera,
    title: 'AI模拟面试',
    desc: '真实面试场景，多轮问答练习'
  },
  {
    icon: Monitor,
    title: '智能评估',
    desc: 'AI自动评分，多维度能力分析'
  },
  {
    icon: DocumentCopy,
    title: '简历解析',
    desc: '智能分析简历，精准匹配合适岗位'
  },
  {
    icon: DataLine,
    title: '数据追踪',
    desc: '完整面试记录，成长曲线可视化'
  }
]

// 统计数据
const stats = [
  { value: '10,000+', label: '累计面试' },
  { value: '95%', label: '用户满意度' },
  { value: '500+', label: '企业用户' }
]

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
  if (!form.captcha || form.captcha.length !== 4) {
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
    const qrData = res.data?.data?.qrCodeUrl ? res.data.data : null
    if (qrData && qrData.qrCodeUrl) {
      qrcodeUrl.value = qrData.qrCodeUrl
      sceneId.value = qrData.sceneId
      loginStatus.value = 'waiting'
      startPolling()

      if (expireTimer) clearTimeout(expireTimer)
      expireTimer = setTimeout(() => {
        if (loginStatus.value !== 'success') {
          loginStatus.value = 'expired'
          stopPolling()
        }
      }, 5 * 60 * 1000)
    } else {
      ElMessage.error('获取二维码失败')
      qrcodeError.value = true
    }
  } catch (e: any) {
    console.error('加载微信二维码失败:', e)
    ElMessage.error('加载二维码失败')
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
          pollTimer = setTimeout(poll, 2000)
        }
      } else {
        pollTimer = setTimeout(poll, 2000)
      }
    } catch (e) {
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
.login-page {
  min-height: 100vh;
  display: flex;
  background: #0a0a0a;
}

/* ========== 左侧介绍区域 ========== */
.intro-section {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px;
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
}

.intro-content {
  max-width: 520px;
  z-index: 1;
}

/* 品牌标识 */
.brand {
  margin-bottom: 60px;
}

.logo {
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  margin-bottom: 24px;
  box-shadow: 0 10px 30px rgba(102, 126, 234, 0.4);
}

.brand-name {
  font-size: 42px;
  font-weight: 700;
  color: #fff;
  margin: 0 0 12px 0;
  letter-spacing: 2px;
}

.brand-slogan {
  font-size: 18px;
  color: rgba(255, 255, 255, 0.7);
  margin: 0;
}

/* 功能特点 */
.features {
  display: flex;
  flex-direction: column;
  gap: 24px;
  margin-bottom: 60px;
}

.feature-item {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  padding: 20px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  transition: all 0.3s ease;
}

.feature-item:hover {
  background: rgba(255, 255, 255, 0.08);
  transform: translateX(8px);
}

.feature-icon {
  width: 48px;
  height: 48px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
}

.feature-text h3 {
  font-size: 16px;
  font-weight: 600;
  color: #fff;
  margin: 0 0 4px 0;
}

.feature-text p {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.6);
  margin: 0;
}

/* 统计数据 */
.stats {
  display: flex;
  gap: 40px;
}

.stat-item {
  text-align: center;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  color: #667eea;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.6);
}

/* 装饰背景 */
.intro-decoration {
  position: absolute;
  inset: 0;
  overflow: hidden;
}

.circle {
  position: absolute;
  border-radius: 50%;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.3) 0%, rgba(118, 75, 162, 0.3) 100%);
}

.circle-1 {
  width: 400px;
  height: 400px;
  top: -100px;
  right: -100px;
  animation: float 8s ease-in-out infinite;
}

.circle-2 {
  width: 300px;
  height: 300px;
  bottom: -50px;
  left: -100px;
  animation: float 10s ease-in-out infinite reverse;
}

.circle-3 {
  width: 200px;
  height: 200px;
  bottom: 30%;
  right: 20%;
  animation: float 6s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translate(0, 0); }
  50% { transform: translate(20px, -20px); }
}

/* ========== 右侧登录区域 ========== */
.login-section {
  width: 500px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  background: #fff;
}

.login-card {
  width: 100%;
  max-width: 400px;
}

/* 登录方式切换 */
.login-tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 32px;
  padding: 4px;
  background: #f5f7fa;
  border-radius: 12px;
}

.tab-item {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px 16px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 14px;
  font-weight: 500;
  color: #606266;
}

.tab-item:hover {
  color: #409eff;
}

.tab-item.active {
  background: #fff;
  color: #409eff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

/* 表单头部 */
.form-header {
  text-align: center;
  margin-bottom: 32px;
}

.form-header h2 {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 8px 0;
}

.form-header p {
  font-size: 14px;
  color: #909399;
  margin: 0;
}

/* 输入框样式 */
.input-wrapper {
  display: flex;
  align-items: center;
  background: #f5f7fa;
  border-radius: 12px;
  padding: 0 16px;
  transition: all 0.3s ease;
}

.input-wrapper:focus-within {
  background: #fff;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}

.input-icon {
  color: #909399;
  margin-right: 12px;
  font-size: 18px;
}

.input-wrapper :deep(.el-input__wrapper) {
  background: transparent;
  box-shadow: none;
  padding: 0;
}

.input-wrapper :deep(.el-input__inner) {
  font-size: 15px;
  height: 48px;
}

/* 验证码相关 */
.captcha-wrapper {
  display: flex;
  gap: 12px;
  width: 100%;
}

.captcha-input {
  flex: 1;
}

.captcha-img-wrapper {
  width: 100px;
  height: 48px;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  flex-shrink: 0;
}

.captcha-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.captcha-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  color: #909399;
  border: 1px dashed #dcdfe6;
  border-radius: 12px;
}

/* 短信验证码相关 */
.code-wrapper {
  display: flex;
  gap: 12px;
  width: 100%;
}

.code-input {
  flex: 1;
}

.send-code-btn {
  flex-shrink: 0;
  width: 120px;
  border-radius: 12px;
  font-weight: 500;
}

.send-code-btn:not(:disabled):hover {
  background: #409eff;
  color: #fff;
}

/* 登录按钮 */
.login-btn {
  width: 100%;
  height: 52px;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  margin-top: 16px;
}

.login-btn:not(:disabled):hover {
  background: linear-gradient(135deg, #5a71d4 0%, #6a4190 100%);
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.4);
}

/* 微信登录样式 */
.wechat-form {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.qrcode-container {
  width: 200px;
  height: 200px;
  background: #f5f7fa;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 24px;
  overflow: hidden;
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
  text-align: center;
}

.qrcode-loading p,
.qrcode-error p,
.qrcode-placeholder p {
  margin: 8px 0;
  font-size: 14px;
}

.qrcode-error {
  color: #f56c6c;
}

.wechat-status {
  text-align: center;
  margin-top: 16px;
}

.wechat-status p {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin: 0;
  font-size: 14px;
}

.status-waiting {
  color: #409eff;
}

.status-scanned {
  color: #67c23a;
}

.status-expired {
  color: #e6a23c;
}

.status-init {
  color: #909399;
}

.blink {
  animation: blink 1.5s ease-in-out infinite;
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.3; }
}

/* 登录页脚 */
.login-footer {
  text-align: center;
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid #ebeef5;
}

.login-footer p {
  font-size: 12px;
  color: #909399;
  margin: 0;
}

.login-footer a {
  color: #409eff;
  text-decoration: none;
}

.login-footer a:hover {
  text-decoration: underline;
}

/* ========== 响应式布局 ========== */
@media (max-width: 1200px) {
  .intro-section {
    padding: 40px;
  }

  .brand-name {
    font-size: 36px;
  }

  .stats {
    gap: 24px;
  }

  .stat-value {
    font-size: 28px;
  }
}

@media (max-width: 900px) {
  .login-page {
    flex-direction: column;
  }

  .intro-section {
    padding: 40px 24px;
    min-height: 40vh;
  }

  .features {
    display: none;
  }

  .stats {
    margin-top: 20px;
  }

  .login-section {
    width: 100%;
    padding: 40px 24px;
  }
}
</style>