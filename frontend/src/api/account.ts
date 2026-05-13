import { request } from './request'

// 登录
export function loginApi(data: { phone: string; code: string }) {
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
  return request({ url: '/v1/account/upload_avatar', method: 'post', data: file })
}
