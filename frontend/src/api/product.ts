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

// 创建订单
export function createOrderApi(data: { productId: number; payType: string }) {
  return request({ url: '/v1/order/add', method: 'post', data })
}

// 查询订单状态
export function getOrderStatusApi(orderNo: string) {
  return request({ url: '/v1/order/detail', method: 'get', params: { orderNo } })
}

// 查询订单列表
export function getOrderListApi(data: { page: number; size: number }) {
  return request({ url: '/v1/order/page', method: 'post', data })
}
