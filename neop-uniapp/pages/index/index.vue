<template>
  <view class="container">
    <!-- 搜索栏 -->
    <view class="search-bar">
      <input class="search-input" placeholder="搜索任务..." v-model="keyword" @confirm="search" />
    </view>
    
    <!-- 轮播图 -->
    <swiper class="banner" autoplay circular>
      <swiper-item v-for="(item, index) in banners" :key="index">
        <image :src="item.image" mode="aspectFill" class="banner-img" />
      </swiper-item>
    </swiper>
    
    <!-- 功能入口 -->
    <view class="grid-menu">
      <view class="grid-item" @click="goToTask">
        <text class="grid-icon">📋</text>
        <text class="grid-text">任务广场</text>
      </view>
      <view class="grid-item" @click="goToShop">
        <text class="grid-icon">🛍️</text>
        <text class="grid-text">积分商城</text>
      </view>
      <view class="grid-item" @click="goToSignIn">
        <text class="grid-icon">🎁</text>
        <text class="grid-text">每日签到</text>
      </view>
      <view class="grid-item" @click="goToInvite">
        <text class="grid-icon">👥</text>
        <text class="grid-text">邀请有礼</text>
      </view>
    </view>
    
    <!-- 热门任务 -->
    <view class="section">
      <view class="section-header">
        <text class="section-title">热门任务</text>
        <text class="section-more" @click="goToTask">查看更多 ></text>
      </view>
      <view class="task-list">
        <view class="task-item" v-for="task in hotTasks" :key="task.id" @click="goToTaskDetail(task.id)">
          <image :src="task.image" mode="aspectFill" class="task-img" />
          <view class="task-info">
            <text class="task-title">{{ task.title }}</text>
            <text class="task-reward">奖励：{{ task.rewardAmount }}元</text>
            <text class="task-count">已领：{{ task.receiveCount }}人</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request'

const keyword = ref('')
const banners = ref([
  { image: '/static/banner1.jpg' },
  { image: '/static/banner2.jpg' }
])
const hotTasks = ref([])

onMounted(() => {
  loadHotTasks()
})

const loadHotTasks = async () => {
  try {
    const res = await request.get('/task/list', { page: 1, pageSize: 10 })
    if (res.code === 200) {
      hotTasks.value = res.data.records
    }
  } catch (err) {
    console.error('加载热门任务失败', err)
  }
}

const search = () => {
  if (keyword.value.trim()) {
    uni.navigateTo({
      url: `/pages/task/list?keyword=${keyword.value}`
    })
  }
}

const goToTask = () => {
  uni.switchTab({ url: '/pages/task/list' })
}

const goToShop = () => {
  uni.switchTab({ url: '/pages/shop/index' })
}

const goToSignIn = () => {
  uni.navigateTo({ url: '/pages/user/signin' })
}

const goToInvite = () => {
  uni.navigateTo({ url: '/pages/user/invite' })
}

const goToTaskDetail = (taskId) => {
  uni.navigateTo({
    url: `/pages/task/detail?id=${taskId}`
  })
}
</script>

<style scoped>
.container {
  padding: 20rpx;
}

.search-bar {
  margin-bottom: 20rpx;
}

.search-input {
  background: #fff;
  border-radius: 40rpx;
  padding: 15rpx 30rpx;
  font-size: 28rpx;
}

.banner {
  height: 300rpx;
  border-radius: 20rpx;
  overflow: hidden;
  margin-bottom: 30rpx;
}

.banner-img {
  width: 100%;
  height: 100%;
}

.grid-menu {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20rpx;
  margin-bottom: 30rpx;
}

.grid-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20rpx;
  background: #fff;
  border-radius: 20rpx;
}

.grid-icon {
  font-size: 60rpx;
  margin-bottom: 10rpx;
}

.grid-text {
  font-size: 24rpx;
  color: #666;
}

.section {
  margin-bottom: 30rpx;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: bold;
}

.section-more {
  font-size: 24rpx;
  color: #999;
}

.task-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.task-item {
  display: flex;
  background: #fff;
  border-radius: 20rpx;
  padding: 20rpx;
}

.task-img {
  width: 200rpx;
  height: 150rpx;
  border-radius: 10rpx;
  margin-right: 20rpx;
}

.task-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.task-title {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
}

.task-reward {
  font-size: 26rpx;
  color: #ff4d4f;
}

.task-count {
  font-size: 24rpx;
  color: #999;
}
</style>
