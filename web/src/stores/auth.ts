//stores/auth.ts 负责保存登录结果、当前用户和退出登录

import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { getCurrentUser, login, type CurrentUser, type LoginRequest } from '../api/auth'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('access_token') ?? '')
  const user = ref<CurrentUser | null>(null)

  const isLoggedIn = computed(() => token.value !== '')
  const isMerchant = computed(() => user.value?.userType.startsWith('MERCHANT_') ?? false)

  async function signIn(request: LoginRequest) {
    const result = await login(request)

    token.value = result.accessToken
    user.value = result.user
    localStorage.setItem('access_token', result.accessToken)
  }

  async function loadCurrentUser() {
    if (!token.value) {
      return
    }

    user.value = await getCurrentUser()
  }

  function signOut() {
    token.value = ''
    user.value = null
    localStorage.removeItem('access_token')
  }

  return {
    token,
    user,
    isLoggedIn,
    isMerchant,
    signIn,
    loadCurrentUser,
    signOut,
  }
})