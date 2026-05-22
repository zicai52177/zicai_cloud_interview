import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getPackagesApi, getBenefitsApi, getBannerListApi } from '@/api/product'

// 缓存过期时间（5分钟）
const CACHE_TTL = 5 * 60 * 1000

interface CacheItem<T> {
  data: T
  expireTime: number
}

export const useCacheStore = defineStore('cache', () => {
  // 缓存数据
  const packageListCache = ref<CacheItem<any[]> | null>(null)
  const benefitListCache = ref<CacheItem<any[]> | null>(null)
  const bannerListCache = ref<CacheItem<any[]> | null>(null)

  // 判断缓存是否有效
  function isCacheValid<T>(cache: CacheItem<T> | null): boolean {
    if (!cache) return false
    return Date.now() < cache.expireTime
  }

  // 获取套餐列表（带缓存）
  async function getPackageList(forceRefresh = false) {
    // 检查缓存
    if (!forceRefresh && isCacheValid(packageListCache.value)) {
      return packageListCache.value!.data
    }

    // 请求新数据
    const res = await getPackagesApi()
    const data = res.data || []

    // 更新缓存
    packageListCache.value = {
      data,
      expireTime: Date.now() + CACHE_TTL
    }

    return data
  }

  // 获取权益列表（带缓存）
  async function getBenefitList(forceRefresh = false) {
    if (!forceRefresh && isCacheValid(benefitListCache.value)) {
      return benefitListCache.value!.data
    }

    const res = await getBenefitsApi()
    const data = res.data || []

    benefitListCache.value = {
      data,
      expireTime: Date.now() + CACHE_TTL
    }

    return data
  }

  // 获取Banner列表（带缓存）
  async function getBannerList(forceRefresh = false) {
    if (!forceRefresh && isCacheValid(bannerListCache.value)) {
      return bannerListCache.value!.data
    }

    const res = await getBannerListApi()
    const data = res.data || []

    bannerListCache.value = {
      data,
      expireTime: Date.now() + CACHE_TTL
    }

    return data
  }

  // 清除所有缓存
  function clearAllCache() {
    packageListCache.value = null
    benefitListCache.value = null
    bannerListCache.value = null
  }

  // 清除指定缓存
  function clearCache(type: 'package' | 'benefit' | 'banner') {
    switch (type) {
      case 'package':
        packageListCache.value = null
        break
      case 'benefit':
        benefitListCache.value = null
        break
      case 'banner':
        bannerListCache.value = null
        break
    }
  }

  return {
    getPackageList,
    getBenefitList,
    getBannerList,
    clearAllCache,
    clearCache
  }
})