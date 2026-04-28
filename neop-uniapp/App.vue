<template>
  <view id="app">
    <router-view />
  </view>
</template>

<script setup>
import { onLaunch, onShow, onHide } from '@dcloudio/uni-app'
import { ref } from 'vue'

onLaunch(() => {
  console.log('App Launch')
  // 检查登录状态
  checkLogin()
})

onShow(() => {
  console.log('App Show')
})

onHide(() => {
  console.log('App Hide')
})

const checkLogin = () => {
  const token = uni.getStorageSync('token')
  if (!token) {
    // 微信小程序自动登录
    // #ifdef MP-WEIXIN
    wxLogin()
    // #endif
  }
}

const wxLogin = () => {
  uni.login({
    provider: 'weixin',
    success: (loginRes) => {
      if (loginRes.code) {
        // 调用后端登录接口
        uni.request({
          url: 'https://api.gongziyu.com/api/user/wechat/login',
          method: 'POST',
          data: { code: loginRes.code },
          success: (res) => {
            if (res.data.code === 200) {
              uni.setStorageSync('token', res.data.data.token)
              uni.setStorageSync('userInfo', res.data.data.userInfo)
            }
          }
        })
      }
    }
  })
}
</script>

<style>
/* 全局样式 */
page {
  background-color: #f5f5f5;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  font-size: 28rpx;
  color: #333;
}

/* 安全区域适配 */
.safe-area-bottom {
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
}
</style>
