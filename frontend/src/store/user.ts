import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getUserInfoApi } from '@/api/account'

export interface UserInfo {
  id: number
  username: string
  headImg: string
  phone?: string
  email?: string
  role?: string
  status?: string
  gmtCreate?: string
  gmtModified?: string
}

export const useUserStore = defineStore('user', () => {
  const token = ref<string>(localStorage.getItem('token') || '')
  const userInfo = ref<UserInfo | null>(null)

  function setToken(newToken: string) {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  function setUserInfo(info: UserInfo) {
    userInfo.value = info
  }

  // 从服务器刷新用户信息
  async function refreshUserInfo() {
    if (!token.value) return null
    try {
      const res = await getUserInfoApi()
      if (res.data) {
        userInfo.value = res.data
      }
      return res.data
    } catch (e) {
      console.error('刷新用户信息失败:', e)
      return null
    }
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
  }

  function isLoggedIn(): boolean {
    return !!token.value
  }

  return { token, userInfo, setToken, setUserInfo, refreshUserInfo, logout, isLoggedIn }
})
