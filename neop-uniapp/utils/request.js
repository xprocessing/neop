/**
 * 统一请求封装（文档31章模板）
 * 功能：Token管理、自动刷新、错误处理
 */

const BASE_URL = 'https://api.gongziyu.com/api'

// 请求队列（Token刷新时暂存）
let requestQueue = []
let isRefreshing = false

/**
 * 统一请求方法
 * @param {Object} options 请求配置
 * @returns {Promise}
 */
const request = (options) => {
  return new Promise((resolve, reject) => {
    const token = uni.getStorageSync('token')
    
    const header = {
      'Content-Type': 'application/json',
      ...options.header
    }
    
    if (token) {
      header['Authorization'] = `Bearer ${token}`
    }
    
    uni.request({
      url: `${BASE_URL}${options.url}`,
      method: options.method || 'GET',
      data: options.data || {},
      header,
      success: (res) => {
        if (res.statusCode === 200) {
          if (res.data.code === 200) {
            resolve(res.data)
          } else if (res.data.code === 401) {
            // Token过期，尝试刷新
            handleTokenRefresh(options, resolve, reject)
          } else {
            uni.showToast({
              title: res.data.message || '请求失败',
              icon: 'none'
            })
            reject(res.data)
          }
        } else {
          uni.showToast({
            title: '服务器错误',
            icon: 'none'
          })
          reject(res)
        }
      },
      fail: (err) => {
        uni.showToast({
          title: '网络连接失败',
          icon: 'none'
        })
        reject(err)
      }
    })
  })
}

/**
 * Token过期处理（自动刷新）
 */
const handleTokenRefresh = (originalRequest, resolve, reject) => {
  if (!isRefreshing) {
    isRefreshing = true
    const refreshToken = uni.getStorageSync('refreshToken')
    
    if (!refreshToken) {
      // 无刷新Token，跳转登录
      redirectToLogin()
      return
    }
    
    // 调用刷新Token接口
    uni.request({
      url: `${BASE_URL}/user/refresh-token`,
      method: 'POST',
      data: { refreshToken },
      success: (res) => {
        if (res.data.code === 200) {
          const newToken = res.data.data.token
          uni.setStorageSync('token', newToken)
          
          // 重新执行队列中的请求
          requestQueue.forEach(callback => callback(newToken))
          requestQueue = []
          
          // 重试原请求
          originalRequest.header = originalRequest.header || {}
          originalRequest.header['Authorization'] = `Bearer ${newToken}`
          request(originalRequest).then(resolve).catch(reject)
        } else {
          redirectToLogin()
        }
      },
      fail: () => {
        redirectToLogin()
      },
      complete: () => {
        isRefreshing = false
      }
    })
  } else {
    // 正在刷新，将请求加入队列
    requestQueue.push((newToken) => {
      originalRequest.header = originalRequest.header || {}
      originalRequest.header['Authorization'] = `Bearer ${newToken}`
      request(originalRequest).then(resolve).catch(reject)
    })
  }
}

/**
 * 跳转登录页
 */
const redirectToLogin = () => {
  uni.removeStorageSync('token')
  uni.removeStorageSync('refreshToken')
  uni.removeStorageSync('userInfo')
  
  uni.reLaunch({
    url: '/pages/user/index'
  })
}

/**
 * 快捷方法
 */
export default {
  get: (url, data) => request({ url, method: 'GET', data }),
  post: (url, data) => request({ url, method: 'POST', data }),
  put: (url, data) => request({ url, method: 'PUT', data }),
  delete: (url, data) => request({ url, method: 'DELETE', data }),
  
  /**
   * 上传文件
   * @param {string} url 接口地址
   * @param {string} filePath 本地文件路径
   * @param {Object} formData 额外表单数据
   * @returns {Promise}
   */
  upload: (url, filePath, formData = {}) => {
    return new Promise((resolve, reject) => {
      const token = uni.getStorageSync('token')
      
      uni.uploadFile({
        url: `${BASE_URL}${url}`,
        filePath,
        name: 'file',
        formData,
        header: token ? { 'Authorization': `Bearer ${token}` } : {},
        success: (res) => {
          const data = JSON.parse(res.data)
          if (data.code === 200) {
            resolve(data)
          } else {
            reject(data)
          }
        },
        fail: (err) => {
          reject(err)
        }
      })
    })
  }
}
