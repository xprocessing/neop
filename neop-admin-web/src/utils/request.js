/**
 * Axios统一请求封装
 * 功能：Token管理、自动刷新、错误处理、请求/响应拦截
 */

import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import router from '@/router'

// 创建axios实例
const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE || '/api',
  timeout: 30000
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    const res = response.data
    
    if (res.code === 200) {
      return res
    } else if (res.code === 401) {
      // Token过期或无效
      handleTokenRefresh()
      return Promise.reject(new Error(res.message || 'Token失效'))
    } else if (res.code === 403) {
      ElMessage.error('没有权限访问')
      return Promise.reject(new Error(res.message || '没有权限'))
    } else {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }
  },
  error => {
    console.error('请求错误:', error)
    ElMessage.error(error.message || '网络错误')
    return Promise.reject(error)
  }
)

// Token刷新处理
let isRefreshing = false
let requestQueue = []

const handleTokenRefresh = () => {
  if (!isRefreshing) {
    isRefreshing = true
    const refreshToken = localStorage.getItem('refreshToken')
    
    if (!refreshToken) {
      redirectToLogin()
      return
    }
    
    // 调用刷新Token接口
    axios.post('/api/admin/refresh-token', { refreshToken })
      .then(res => {
        if (res.data.code === 200) {
          const newToken = res.data.data.token
          localStorage.setItem('token', newToken)
          
          // 重试队列中的请求
          requestQueue.forEach(callback => callback(newToken))
          requestQueue = []
        } else {
          redirectToLogin()
        }
      })
      .catch(() => {
        redirectToLogin()
      })
      .finally(() => {
        isRefreshing = false
      })
  }
}

const redirectToLogin = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('refreshToken')
  localStorage.removeItem('userInfo')
  
  router.push('/login')
}

export default service
