<template>
  <view class="container">
    <!-- 用户信息区 -->
    <view class="user-header">
      <image :src="userInfo.avatar || '/static/default-avatar.png'" class="avatar" />
      <view class="user-info" v-if="userInfo.id">
        <text class="nickname">{{ userInfo.nickname }}</text>
        <view class="member-badge" v-if="userInfo.isMember === 1">
          <text>会员</text>
        </view>
        <text class="invite-code">邀请码：{{ userInfo.inviteCode }}</text>
      </view>
      <view class="login-btn" v-else @click="wxLogin">
        <text>微信登录</text>
      </view>
    </view>
    
    <!-- 资产卡片 -->
    <view class="asset-card">
      <view class="asset-item" @click="goToPoints">
        <text class="asset-value">{{ userInfo.points || 0 }}</text>
        <text class="asset-label">积分</text>
      </view>
      <view class="asset-item" @click="goToWithdrawList">
        <text class="asset-value">{{ withdrawable || '0.00' }}</text>
        <text class="asset-label">可提现(元)</text>
      </view>
      <view class="asset-item" @click="goToMember">
        <text class="asset-value">{{ userInfo.isMember === 1 ? 'VIP' : '普通' }}</text>
        <text class="asset-label">会员状态</text>
      </view>
    </view>
    
    <!-- 功能列表 -->
    <view class="menu-list">
      <view class="menu-item" @click="goToTaskMy">
        <text class="menu-icon">📋</text>
        <text class="menu-text">我的任务</text>
        <text class="menu-arrow">></text>
      </view>
      <view class="menu-item" @click="goToOrders">
        <text class="menu-icon">📦</text>
        <text class="menu-text">我的订单</text>
        <text class="menu-arrow">></text>
      </view>
      <view class="menu-item" @click="goToSignIn">
        <text class="menu-icon">🎁</text>
        <text class="menu-text">每日签到</text>
        <text class="menu-arrow">></text>
      </view>
      <view class="menu-item" @click="goToInvite">
        <text class="menu-icon">👥</text>
        <text class="menu-text">邀请有礼</text>
        <text class="menu-arrow">></text>
      </view>
      <view class="menu-item" @click="goToAddress">
        <text class="menu-icon">📍</text>
        <text class="menu-text">收货地址</text>
        <text class="menu-arrow">></text>
      </view>
      <view class="menu-item" @click="goToWithdraw">
        <text class="menu-icon">💰</text>
        <text class="menu-text">提现记录</text>
        <text class="menu-arrow">></text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted, onShow } from 'vue'
import request from '@/utils/request'

const userInfo = ref({})
const withdrawable = ref('0.00')

onMounted(() => {
  loadUserInfo()
})

onShow(() => {
  loadUserInfo()
})

const loadUserInfo = async () => {
  const token = uni.getStorageSync('token')
  if (!token) return
  
  try {
    const res = await request.get('/user/info')
    if (res.code === 200) {
      userInfo.value = res.data
    }
  } catch (err) {
    console.error('加载用户信息失败', err)
  }
}

const wxLogin = () => {
  // #ifdef MP-WEIXIN
  uni.login({
    provider: 'weixin',
    success: (loginRes) => {
      if (loginRes.code) {
        request.post('/user/wechat/login', { code: loginRes.code })
          .then(res => {
            if (res.code === 200) {
              uni.setStorageSync('token', res.data.token)
              uni.setStorageSync('userInfo', res.data.userInfo)
              userInfo.value = res.data.userInfo
              uni.showToast({ title: '登录成功', icon: 'success' })
            }
          })
      }
    }
  })
  // #endif
  
  // #ifdef H5
  uni.showToast({ title: '请在微信环境中登录', icon: 'none' })
  // #endif
}

const goToPoints = () => {
  uni.navigateTo({ url: '/pages/user/points' })
}

const goToWithdrawList = () => {
  uni.navigateTo({ url: '/pages/user/withdraw-list' })
}

const goToMember = () => {
  uni.navigateTo({ url: '/pages/user/member' })
}

const goToTaskMy = () => {
  uni.navigateTo({ url: '/pages/task/my' })
}

const goToOrders = () => {
  uni.navigateTo({ url: '/pages/shop/order' })
}

const goToSignIn = () => {
  uni.navigateTo({ url: '/pages/user/signin' })
}

const goToInvite = () => {
  uni.navigateTo({ url: '/pages/user/invite' })
}

const goToAddress = () => {
  uni.navigateTo({ url: '/pages/user/address' })
}

const goToWithdraw = () => {
  uni.navigateTo({ url: '/pages/user/withdraw-list' })
}
</script>

<style scoped>
.container {
  padding-bottom: 120rpx;
}

.user-header {
  display: flex;
  align-items: center;
  padding: 40rpx 30rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.avatar {
  width: 120rpx;
  height: 120rpx;
  border-radius: 60rpx;
  border: 4rpx solid #fff;
  margin-right: 20rpx;
}

.user-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 10rpx;
}

.nickname {
  font-size: 32rpx;
  color: #fff;
  font-weight: bold;
}

.member-badge {
  display: inline-block;
  background: #ffd700;
  color: #333;
  font-size: 20rpx;
  padding: 5rpx 15rpx;
  border-radius: 20rpx;
  width: fit-content;
}

.invite-code {
  font-size: 24rpx;
  color: rgba(255,255,255,0.8);
}

.login-btn {
  background: #fff;
  color: #667eea;
  padding: 15rpx 40rpx;
  border-radius: 30rpx;
  font-size: 28rpx;
}

.asset-card {
  display: flex;
  justify-content: space-around;
  padding: 30rpx;
  background: #fff;
  margin: 20rpx;
  border-radius: 20rpx;
  box-shadow: 0 4rpx 12rpx rgba(0,0,0,0.1);
}

.asset-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10rpx;
}

.asset-value {
  font-size: 36rpx;
  font-weight: bold;
  color: #333;
}

.asset-label {
  font-size: 24rpx;
  color: #999;
}

.menu-list {
  background: #fff;
  margin: 20rpx;
  border-radius: 20rpx;
  overflow: hidden;
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 30rpx;
  border-bottom: 1rpx solid #f5f5f5;
}

.menu-item:last-child {
  border-bottom: none;
}

.menu-icon {
  font-size: 36rpx;
  margin-right: 20rpx;
}

.menu-text {
  flex: 1;
  font-size: 28rpx;
  color: #333;
}

.menu-arrow {
  font-size: 28rpx;
  color: #999;
}
</style>
