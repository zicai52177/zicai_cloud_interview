import { request } from './request'

/**
 * AI面试相关API
 */

// ============ 类型定义 ============
export interface InterviewCreateReq {
  resumeId?: number
  targetPosition: string
  expectedSalary?: string
  workYears?: string
  targetCity?: string
  interviewType?: string
  specialContent?: string
}

export interface InterviewPageReq {
  page: number
  size: number
}

export interface AnswerReq {
  interviewId: number
  questionId: number
  answer: string
}

export interface InterviewFinishReq {
  interviewId: number
}

export interface InterviewDTO {
  id: number
  position: string
  score?: number
  status: string
  gmtCreate: string
  gmtModified: string
}

export interface InterviewDetailDTO {
  id: number
  position: string
  score?: number
  status: string
  gmtCreate: string
  rounds: InterviewRoundDTO[]
}

export interface InterviewRoundDTO {
  id: number
  roundName: string
  questions: QuestionDTO[]
}

export interface QuestionDTO {
  id: number
  content: string
  answer?: string
  evaluation?: string
}

export interface PageResult<T> {
  records: T[]
  totalRecord: number
  totalPage: number
  currentPage: number
  pageSize: number
}

export interface ApiResponse<T = any> {
  code: number
  data: T
  msg: string
}

// ============ API接口 ============

/**
 * 创建面试
 * POST /api/v1/interview/create
 */
export function createInterviewApi(data: InterviewCreateReq) {
  return request<InterviewDTO>({
    url: '/v1/interview/create',
    method: 'post',
    data
  })
}

/**
 * 查询面试状态
 * GET /api/v1/interview/status
 */
export function getInterviewStatusApi(interviewId: number) {
  return request<{ status: string; progress: number }>({
    url: '/v1/interview/status',
    method: 'get',
    params: { interviewId }
  })
}

/**
 * 回答题目
 * POST /api/v1/interview/answer
 */
export function submitAnswerApi(data: AnswerReq) {
  return request<{ questionId: number; evaluation: string }>({
    url: '/v1/interview/answer',
    method: 'post',
    data
  })
}

/**
 * 结束面试
 * POST /api/v1/interview/finish
 */
export function finishInterviewApi(data: InterviewFinishReq) {
  return request<InterviewDTO>({
    url: '/v1/interview/finish',
    method: 'post',
    data
  })
}

/**
 * 查看面试详情
 * GET /api/v1/interview/view
 */
export function getInterviewDetailApi(interviewId: number) {
  return request<InterviewDetailDTO>({
    url: '/v1/interview/view',
    method: 'get',
    params: { interviewId }
  })
}

/**
 * 分页查询面试记录
 * POST /api/v1/interview/page
 */
export function getInterviewListApi(data: InterviewPageReq) {
  return request<PageResult<InterviewDTO>>({
    url: '/v1/interview/page',
    method: 'post',
    data
  })
}

/**
 * 删除面试记录
 * DELETE /api/v1/interview/delete
 */
export function deleteInterviewApi(interviewId: number) {
  return request<boolean>({
    url: '/v1/interview/delete',
    method: 'delete',
    params: { interviewId }
  })
}

/**
 * 获取简历列表
 * GET /api/v1/resume/list
 */
export function getResumeListApi() {
  return request<any[]>({
    url: '/v1/resume/list',
    method: 'get'
  })
}

/**
 * 上传简历
 * POST /api/v1/resume/upload
 */
export function uploadResumeApi(formData: FormData) {
  return request<any>({
    url: '/v1/resume/upload',
    method: 'post',
    data: formData
  })
}

/**
 * 分析简历
 * GET /api/v1/resume/analyse
 */
export function analyseResumeApi(resumeId: number) {
  return request<boolean>({
    url: '/v1/resume/analyse',
    method: 'get',
    params: { id: resumeId }
  })
}