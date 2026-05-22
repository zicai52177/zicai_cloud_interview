import { request } from './request'

// 获取产品套餐列表
export function getPackagesApi() {
  return request({ url: '/v1/product/packages', method: 'get' })
}

// 获取权益列表
export function getBenefitsApi() {
  return request({ url: '/v1/product/benefits', method: 'get' })
}

// 获取 Banner 列表
export function getBannerListApi() {
  return request({ url: '/v1/banner/list', method: 'get' })
}

// 创建套餐订单
export function createPackageOrderApi(data: { PackageId: number; payType: string; discount?: number }) {
  return request({ url: '/v1/order/package/create', method: 'post', data })
}

// 创建权益订单
export function createBenefitOrderApi(data: { benefitId: number; porchaseCount: number; payType: string; discount?: number }) {
  return request({ url: '/v1/order/benefit/create', method: 'post', data })
}

// 查询订单支付状态
export function getOrderStatusApi(outTradeNo: string) {
  return request({ url: '/v1/order/status', method: 'get', params: { outTradeNo } })
}

// 查询订单列表（分页）
export function getOrderListApi(data: { page: number; size: number; orderState?: string; orderType?: string }) {
  return request({ url: '/v1/order/page', method: 'post', data })
}
