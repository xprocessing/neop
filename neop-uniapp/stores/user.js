import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: '',
    userInfo: null
  }),
  actions: {
    setToken(token) {
      this.token = token
      uni.setStorageSync('token', token)
    },
    setUserInfo(info) {
      this.userInfo = info
      uni.setStorageSync('userInfo', info)
    },
    logout() {
      this.token = ''
      this.userInfo = null
      uni.removeStorageSync('token')
      uni.removeStorageSync('userInfo')
      uni.removeStorageSync('refreshToken')
      uni.reLaunch({ url: '/pages/auth/login' })
    }
  }
})
