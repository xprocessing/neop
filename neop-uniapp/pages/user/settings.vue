<template>
  <view class="settings-page">
    <!-- 顶部导航 -->
    <view class="nav-bar">
      <view class="nav-back" @click="goBack">
        <text class="back-icon">←</text>
      </view>
      <text class="nav-title">设置</text>
      <view class="nav-placeholder"></view>
    </view>

    <!-- 设置列表 -->
    <view class="settings-list">
      <!-- 账户安全 -->
      <view class="settings-group">
        <text class="group-title">账户安全</text>
        <view class="settings-item" @click="goPage('/pages/auth/bind-phone')">
          <view class="item-left">
            <text class="item-icon">📱</text>
            <text class="item-text">手机号码</text>
          </view>
          <view class="item-right">
            <text class="item-value">{{ userPhone }}</text>
            <text class="item-arrow">›</text>
          </view>
        </view>
        <view class="settings-item" @click="goPage('/pages/auth/forgot-password')">
          <view class="item-left">
            <text class="item-icon">🔒</text>
            <text class="item-text">修改密码</text>
          </view>
          <view class="item-right">
            <text class="item-arrow">›</text>
          </view>
        </view>
        <view class="settings-item">
          <view class="item-left">
            <text class="item-icon">🔐</text>
            <text class="item-text">指纹/面部识别登录</text>
          </view>
          <view class="item-right">
            <switch class="item-switch" :checked="biometricEnabled" @change="toggleBiometric"></switch>
          </view>
        </view>
      </view>

      <!-- 通知设置 -->
      <view class="settings-group">
        <text class="group-title">通知设置</text>
        <view class="settings-item">
          <view class="item-left">
            <text class="item-icon">🔔</text>
            <text class="item-text">系统通知</text>
          </view>
          <view class="item-right">
            <switch class="item-switch" :checked="systemNotification" @change="toggleSystemNotification"></switch>
          </view>
        </view>
        <view class="settings-item">
          <view class="item-left">
            <text class="item-icon">📦</text>
            <text class="item-text">订单通知</text>
          </view>
          <view class="item-right">
            <switch class="item-switch" :checked="orderNotification" @change="toggleOrderNotification"></switch>
          </view>
        </view>
        <view class="settings-item">
          <view class="item-left">
            <text class="item-icon">🎉</text>
            <text class="item-text">活动通知</text>
          </view>
          <view class="item-right">
            <switch class="item-switch" :checked="activityNotification" @change="toggleActivityNotification"></switch>
          </view>
        </view>
      </view>

      <!-- 通用设置 -->
      <view class="settings-group">
        <text class="group-title">通用设置</text>
        <view class="settings-item" @click="clearCache">
          <view class="item-left">
            <text class="item-icon">🗑️</text>
            <text class="item-text">清除缓存</text>
          </view>
          <view class="item-right">
            <text class="item-value">{{ cacheSize }}</text>
            <text class="item-arrow">›</text>
          </view>
        </view>
        <view class="settings-item">
          <view class="item-left">
            <text class="item-icon">📏</text>
            <text class="item-text">字体大小</text>
          </view>
          <view class="item-right">
            <text class="item-value">{{ fontSize }}</text>
            <text class="item-arrow">›</text>
          </view>
        </view>
        <view class="settings-item">
          <view class="item-left">
            <text class="item-icon">🌙</text>
            <text class="item-text">深色模式</text>
          </view>
          <view class="item-right">
            <switch class="item-switch" :checked="darkMode" @change="toggleDarkMode"></switch>
          </view>
        </view>
      </view>

      <!-- 关于 -->
      <view class="settings-group">
        <text class="group-title">关于</text>
        <view class="settings-item" @click="goPage('/pages/about/index')">
          <view class="item-left">
            <text class="item-icon">ℹ️</text>
            <text class="item-text">关于我们</text>
          </view>
          <view class="item-right">
            <text class="item-arrow">›</text>
          </view>
        </view>
        <view class="settings-item" @click="goPage('/pages/help/index')">
          <view class="item-left">
            <text class="item-icon">❓</text>
            <text class="item-text">帮助中心</text>
          </view>
          <view class="item-right">
            <text class="item-arrow">›</text>
          </view>
        </view>
        <view class="settings-item">
          <view class="item-left">
            <text class="item-icon">📱</text>
            <text class="item-text">当前版本</text>
          </view>
          <view class="item-right">
            <text class="item-value">v1.0.0</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 退出登录 -->
    <view class="logout-btn" @click="logout">
      <text class="logout-text">退出登录</text>
    </view>
  </view>
</template>

<script setup>
import { ref, onLoad } from 'vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

// 数据
const userPhone = ref('138****8888')
const biometricEnabled = ref(false)
const systemNotification = ref(true)
const orderNotification = ref(true)
const activityNotification = ref(true)
const cacheSize = ref('12.5MB')
const fontSize = ref('标准')
const darkMode = ref(false)

// 跳转页面
const goPage = (url) => {
  uni.navigateTo({ url })
}

// 切换生物识别
const toggleBiometric = (e) => {
  biometricEnabled.value = e.detail.value
  if (biometricEnabled.value) {
    // 检查设备支持
    uni.checkIsSupportSoterAuthentication({
      success: (res) => {
        console.log('支持的生物识别方式', res.supportMode)
      },
      fail: () => {
        uni.showToast({ title: '设备不支持', icon: 'none' })
        biometricEnabled.value = false
      }
    })
  }
}

// 切换系统通知
const toggleSystemNotification = (e) => {
  systemNotification.value = e.detail.value
  // 保存设置
  saveSettings()
}

// 切换订单通知
const toggleOrderNotification = (e) => {
  orderNotification.value = e.detail.value
  saveSettings()
}

// 切换活动通知
const toggleActivityNotification = (e) => {
  activityNotification.value = e.detail.value
  saveSettings()
}

// 切换深色模式
const toggleDarkMode = (e) => {
  darkMode.value = e.detail.value
  // 保存设置
  saveSettings()
  uni.showToast({ title: '深色模式开发中', icon: 'none' })
}

// 清除缓存
const clearCache = () => {
  uni.showModal({
    title: '提示',
    content: '确定要清除缓存吗？',
    success: (res) => {
      if (res.confirm) {
        // 清除缓存
        uni.clearStorageSync()
        cacheSize.value = '0MB'
        uni.showToast({ title: '缓存已清除', icon: 'success' })
      }
    }
  })
}

// 保存设置
const saveSettings = () => {
  const settings = {
    biometricEnabled: biometricEnabled.value,
    systemNotification: systemNotification.value,
    orderNotification: orderNotification.value,
    activityNotification: activityNotification.value,
    darkMode: darkMode.value
  }
  uni.setStorageSync('settings', settings)
}

// 退出登录
const logout = () => {
  uni.showModal({
    title: '提示',
    content: '确定要退出登录吗？',
    success: (res) => {
      if (res.confirm) {
        userStore.logout()
        uni.reLaunch({ url: '/pages/auth/login' })
      }
    }
  })
}

// 返回
const goBack = () => {
  uni.navigateBack()
}

// 加载设置
onLoad(() => {
  const settings = uni.getStorageSync('settings')
  if (settings) {
    biometricEnabled.value = settings.biometricEnabled || false
    systemNotification.value = settings.systemNotification !== false
    orderNotification.value = settings.orderNotification !== false
    activityNotification.value = settings.activityNotification !== false
    darkMode.value = settings.darkMode || false
  }
  
  // 计算缓存大小
  const res = uni.getStorageInfoSync()
  cacheSize.value = (res.currentSize / 1024).toFixed(1) + 'MB'
})
</script>

<style scoped>
.settings-page {
  min-height: 100vh;
  background: #f5f5f5;
}

.nav-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 60rpx 30rpx 20rpx;
  background: white;
}

.nav-back {
  width: 60rpx;
  height: 60rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.back-icon {
  font-size: 40rpx;
  color: #333;
}

.nav-title {
  font-size: 34rpx;
  font-weight: bold;
  color: #333;
}

.nav-placeholder {
  width: 60rpx;
}

.settings-list {
  padding: 20rpx 0;
}

.settings-group {
  background: white;
  margin-bottom: 20rpx;
  padding: 0 30rpx;
}

.group-title {
  font-size: 26rpx;
  color: #999;
  padding: 20rpx 0;
  display: block;
}

.settings-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 25rpx 0;
  border-bottom: 1rpx solid #f0f0f0;
}

.settings-item:last-child {
  border-bottom: none;
}

.item-left {
  display: flex;
  align-items: center;
}

.item-icon {
  font-size: 36rpx;
  margin-right: 20rpx;
}

.item-text {
  font-size: 28rpx;
  color: #333;
}

.item-right {
  display: flex;
  align-items: center;
}

.item-value {
  font-size: 26rpx;
  color: #999;
  margin-right: 10rpx;
}

.item-arrow {
  font-size: 32rpx;
  color: #ccc;
}

.item-switch {
  transform: scale(0.8);
}

.logout-btn {
  margin: 40rpx 30rpx;
  height: 90rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: white;
  border-radius: 15rpx;
}

.logout-text {
  font-size: 30rpx;
  color: #f5222d;
  font-weight: bold;
}
</style>
