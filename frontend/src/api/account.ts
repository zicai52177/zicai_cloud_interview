import { request } from './request'

// 登录
export function loginApi(data: { identifier: string; checkCode: string; type: string }) {
  return request({ url: '/v1/account/login', method: 'post', data })
}

// 获取验证码图片URL（直接拼接img src使用）
export function getCaptchaUrl(identifier: string, type: string) {
  return `/api/v1/account/captcha?identifier=${encodeURIComponent(identifier)}&type=${encodeURIComponent(type)}&t=${Date.now()}`
}

// 发送短信验证码
export function sendSmsCodeApi(data: { identifier: string; captcha: string; type: string }) {
  return request({ url: '/v1/account/send_check_code', method: 'post', data })
}

// 获取用户信息
export function getUserInfoApi() {
  return request({ url: '/v1/account/detail', method: 'get' })
}

// 上传头像
export function uploadAvatarApi(file: FormData) {
  return request({ url: '/v1/account/avatar', method: 'post', data: file })
}

// 更新邮箱
export function updateEmailApi(email: string) {
  return request({ url: '/v1/account/email', method: 'post', params: { email } })
}

// ============ 微信登录相关API ============

// 获取微信登录二维码
export function getWechatQrcodeApi() {
  return request({ url: '/v1/wechat/qrcode', method: 'get' })
}

// 轮询微信登录结果
export function getWechatLoginResultApi(sceneId: string) {
  return request({ url: `/v1/wechat/login/result?sceneId=${sceneId}`, method: 'get' })
}
