import { request } from './request'

// 上传简历
export function uploadResumeApi(file: FormData) {
  return request({ url: '/v1/resume/upload', method: 'post', data: file })
}

// 获取简历列表
export function getResumeListApi() {
  return request({ url: '/v1/resume/list', method: 'get' })
}

// 开始AI面试 (SSE流式)
export function startInterviewApi(data: { resumeId: number; position: string }) {
  return request({ url: '/v1/interview/start', method: 'post', data })
}

// 获取面试历史列表
export function getInterviewListApi(data: { page: number; size: number }) {
  return request({ url: '/v1/interview/page', method: 'post', data })
}

// 获取面试详情
export function getInterviewDetailApi(id: number) {
  return request({ url: '/v1/interview/detail', method: 'get', params: { id } })
}
