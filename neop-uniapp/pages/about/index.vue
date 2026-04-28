<template>
  <view class="about-page">
    <!-- 顶部导航 -->
    <view class="nav-bar">
      <view class="nav-back" @click="goBack">
        <text class="back-icon">←</text>
      </view>
      <text class="nav-title">关于我们</text>
      <view class="nav-placeholder"></view>
    </view>

    <!-- Logo和版本 -->
    <view class="header">
      <image class="logo" src="/static/logo.png" mode="aspectFit"></image>
      <text class="app-name">NEOP平台</text>
      <text class="app-version">版本 1.0.0</text>
    </view>

    <!-- 公司信息 -->
    <view class="info-card">
      <view class="info-item">
        <text class="info-icon">🏢</text>
        <view class="info-content">
          <text class="info-label">公司名称</text>
          <text class="info-value">NEOP科技有限公司</text>
        </view>
      </view>
      <view class="info-item">
        <text class="info-icon">📍</text>
        <view class="info-content">
          <text class="info-label">公司地址</text>
          <text class="info-value">北京市朝阳区xxx路xxx号</text>
        </view>
      </view>
      <view class="info-item">
        <text class="info-icon">📞</text>
        <view class="info-content">
          <text class="info-label">客服电话</text>
          <text class="info-value phone" @click="callService">400-123-4567</text>
        </view>
      </view>
      <view class="info-item">
        <text class="info-icon">✉️</text>
        <view class="info-content">
          <text class="info-label">客服邮箱</text>
          <text class="info-value">support@neop.com</text>
        </view>
      </view>
      <view class="info-item">
        <text class="info-icon">🌐</text>
        <view class="info-content">
          <text class="info-label">官方网站</text>
          <text class="info-value link" @click="openWebsite">www.neop.com</text>
        </view>
      </view>
    </view>

    <!-- 功能列表 -->
    <view class="function-card">
      <view class="function-item" @click="goPage('/pages/about/agreement')">
        <text class="function-text">用户协议</text>
        <text class="function-arrow">›</text>
      </view>
      <view class="function-item" @click="goPage('/pages/about/privacy')">
        <text class="function-text">隐私政策</text>
        <text class="function-arrow">›</text>
      </view>
      <view class="function-item" @click="goPage('/pages/help/index')">
        <text class="function-text">帮助中心</text>
        <text class="function-arrow">›</text>
      </view>
      <view class="function-item" @click="shareApp">
        <text class="function-text">分享给好友</text>
        <text class="function-arrow">›</text>
      </view>
      <view class="function-item" @click="rateApp">
        <text class="function-text">给应用评分</text>
        <text class="function-arrow">›</text>
      </view>
    </view>

    <!-- 版权信息 -->
    <view class="footer">
      <text class="copyright">© 2026 NEOP科技有限公司</text>
      <text class="copyright">保留所有权利</text>
    </view>
  </view>
</template>

<script setup>
import { onLoad } from 'vue'

// 跳转页面
const goPage = (url) => {
  uni.navigateTo({ url })
}

// 拨打客服电话
const callService = () => {
  uni.makePhoneCall({
    phoneNumber: '400-123-4567'
  })
}

// 打开官方网站
const openWebsite = () => {
  // #ifdef H5
  window.open('https://www.neop.com')
  // #endif
  
  // #ifdef APP-PLUS
  plus.runtime.openURL('https://www.neop.com')
  // #endif
}

// 分享应用
const shareApp = () => {
  // #ifdef H5 || APP-PLUS
  uni.share({
    provider: 'weixin',
    scene: 'WXSceneSession',
    type: 0,
    title: 'NEOP平台',
    summary: '快来加入NEOP平台，一起赚钱！',
    href: 'https://www.neop.com',
    imageUrl: '/static/logo.png',
    success: () => {
      uni.showToast({ title: '分享成功', icon: 'success' })
    },
    fail: () => {
      uni.showToast({ title: '分享失败', icon: 'none' })
    }
  })
  // #endif
  
  // #ifdef MP-WEIXIN
  uni.showToast({ title: '请点击右上角分享', icon: 'none' })
  // #endif
}

// 应用评分
const rateApp = () => {
  // #ifdef APP-PLUS
  plus.runtime.launchApplication({
    action: `market://details?id=${plus.runtime.appid}`
  }, () => {
    uni.showToast({ title: '打开应用商店失败', icon: 'none' })
  })
  // #endif
  
  // #ifdef H5 || MP-WEIXIN
  uni.showToast({ title: '评分功能开发中', icon: 'none' })
  // #endif
}

// 返回
const goBack = () => {
  uni.navigateBack()
}
</script>

<style scoped>
.about-page {
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

.header {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 50rpx 0;
  background: white;
}

.logo {
  width: 150rpx;
  height: 150rpx;
  border-radius: 30rpx;
  margin-bottom: 20rpx;
}

.app-name {
  font-size: 36rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 10rpx;
}

.app-version {
  font-size: 26rpx;
  color: #999;
}

.info-card,
.function-card {
  background: white;
  margin-top: 20rpx;
  padding: 0 30rpx;
}

.info-item {
  display: flex;
  align-items: center;
  padding: 25rpx 0;
  border-bottom: 1rpx solid #f0f0f0;
}

.info-item:last-child {
  border-bottom: none;
}

.info-icon {
  font-size: 36rpx;
  margin-right: 20rpx;
}

.info-content {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.info-label {
  font-size: 24rpx;
  color: #999;
  margin-bottom: 5rpx;
}

.info-value {
  font-size: 28rpx;
  color: #333;
}

.info-value.phone {
  color: #667eea;
}

.info-value.link {
  color: #667eea;
}

.function-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx 0;
  border-bottom: 1rpx solid #f0f0f0;
}

.function-item:last-child {
  border-bottom: none;
}

.function-text {
  font-size: 28rpx;
  color: #333;
}

.function-arrow {
  font-size: 32rpx;
  color: #ccc;
}

.footer {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 50rpx 0;
}

.copyright {
  font-size: 24rpx;
  color: #999;
  line-height: 1.8;
}
</style>
