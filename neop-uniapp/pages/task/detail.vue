<template>
  <view class="container">
    <!-- 任务头部 -->
    <view class="task-header">
      <image :src="task.image" mode="aspectFill" class="task-img" />
      <view class="task-title-area">
        <text class="task-title">{{ task.title }}</text>
        <view class="task-tags">
          <text class="tag" v-if="task.isMemberOnly === 1">会员专属</text>
          <text class="tag">{{ task.categoryName }}</text>
        </view>
      </view>
    </view>
    
    <!-- 任务信息 -->
    <view class="info-card">
      <view class="info-item">
        <text class="info-label">奖励金额</text>
        <text class="info-value reward">¥{{ task.rewardAmount }}</text>
      </view>
      <view class="info-item">
        <text class="info-label">任务限额</text>
        <text class="info-value">{{ task.totalLimit }}人</text>
      </view>
      <view class="info-item">
        <text class="info-label">已领取</text>
        <text class="info-value">{{ task.receiveCount }}人</text>
      </view>
      <view class="info-item">
        <text class="info-label">截止时间</text>
        <text class="info-value">{{ task.endTime }}</text>
      </view>
    </view>
    
    <!-- 任务描述 -->
    <view class="desc-card">
      <text class="card-title">任务描述</text>
      <text class="card-content">{{ task.description }}</text>
    </view>
    
    <!-- 任务要求 -->
    <view class="desc-card">
      <text class="card-title">任务要求</text>
      <text class="card-content">{{ task.requirement }}</text>
    </view>
    
    <!-- 提交材料说明 -->
    <view class="desc-card">
      <text class="card-title">提交材料</text>
      <text class="card-content">{{ task.submitMaterial }}</text>
    </view>
    
    <!-- 底部操作栏 -->
    <view class="bottom-bar">
      <button class="btn-receive" v-if="!myTask" @click="receiveTask">领取任务</button>
      <button class="btn-submit" v-else-if="myTask.status === 1" @click="goToSubmit">去提交</button>
      <button class="btn-disabled" v-else-if="myTask.status === 2">审核中</button>
      <button class="btn-withdraw" v-else-if="myTask.status === 3 && !myTask.withdrawTime" @click="goToWithdraw">申请提现</button>
      <text class="status-text" v-else-if="myTask.status === 4">已驳回</text>
      <text class="status-text" v-else-if="myTask.status === 5">已过期</text>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import request from '@/utils/request'

const taskId = ref('')
const task = ref({})
const myTask = ref(null)

onLoad((options) => {
  if (options.id) {
    taskId.value = options.id
    loadTaskDetail()
    loadMyTask()
  }
})

const loadTaskDetail = async () => {
  try {
    const res = await request.get(`/task/info/${taskId.value}`)
    if (res.code === 200) {
      task.value = res.data
    }
  } catch (err) {
    console.error('加载任务详情失败', err)
  }
}

const loadMyTask = async () => {
  try {
    const res = await request.get('/task/my/list')
    if (res.code === 200) {
      myTask.value = res.data.list.find(t => t.taskId === taskId.value)
    }
  } catch (err) {
    console.error('加载我的任务失败', err)
  }
}

const receiveTask = async () => {
  try {
    const res = await request.post('/task/receive', { taskId: taskId.value })
    if (res.code === 200) {
      uni.showToast({ title: '领取成功', icon: 'success' })
      loadMyTask()
    } else {
      uni.showToast({ title: res.message, icon: 'none' })
    }
  } catch (err) {
    console.error('领取任务失败', err)
  }
}

const goToSubmit = () => {
  uni.navigateTo({
    url: `/pages/task/submit?taskId=${taskId.value}&receiveId=${myTask.value.id}`
  })
}

const goToWithdraw = () => {
  uni.navigateTo({
    url: `/pages/user/withdraw?taskId=${taskId.value}&reward=${task.value.rewardAmount}`
  })
}
</script>

<style scoped>
.container {
  padding-bottom: 120rpx;
}

.task-header {
  position: relative;
}

.task-img {
  width: 100%;
  height: 400rpx;
}

.task-title-area {
  padding: 20rpx;
  background: #fff;
}

.task-title {
  font-size: 36rpx;
  font-weight: bold;
  color: #333;
  display: block;
  margin-bottom: 10rpx;
}

.task-tags {
  display: flex;
  gap: 10rpx;
}

.tag {
  font-size: 22rpx;
  color: #007aff;
  background: #e6f7ff;
  padding: 5rpx 15rpx;
  border-radius: 15rpx;
}

.info-card {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20rpx;
  padding: 20rpx;
  background: #fff;
  margin: 20rpx;
  border-radius: 20rpx;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 10rpx;
}

.info-label {
  font-size: 24rpx;
  color: #999;
}

.info-value {
  font-size: 28rpx;
  color: #333;
  font-weight: bold;
}

.info-value.reward {
  color: #ff4d4f;
  font-size: 32rpx;
}

.desc-card {
  padding: 20rpx;
  background: #fff;
  margin: 20rpx;
  border-radius: 20rpx;
  display: flex;
  flex-direction: column;
  gap: 15rpx;
}

.card-title {
  font-size: 30rpx;
  font-weight: bold;
  color: #333;
}

.card-content {
  font-size: 26rpx;
  color: #666;
  line-height: 1.6;
}

.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 20rpx;
  background: #fff;
  box-shadow: 0 -2rpx 10rpx rgba(0,0,0,0.1);
  display: flex;
  justify-content: center;
}

.btn-receive, .btn-submit, .btn-withdraw {
  width: 90%;
  height: 80rpx;
  line-height: 80rpx;
  text-align: center;
  border-radius: 40rpx;
  font-size: 30rpx;
  color: #fff;
  background: #007aff;
}

.btn-disabled {
  width: 90%;
  height: 80rpx;
  line-height: 80rpx;
  text-align: center;
  border-radius: 40rpx;
  font-size: 30rpx;
  color: #999;
  background: #f5f5f5;
}

.status-text {
  font-size: 28rpx;
  color: #999;
}
</style>
