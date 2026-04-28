<template>
  <view class="container">
    <!-- 筛选栏 -->
    <view class="filter-bar">
      <view class="filter-item" :class="{ active: filter === 'all' }" @click="setFilter('all')">全部</view>
      <view class="filter-item" :class="{ active: filter === 'pending' }" @click="setFilter('pending')">待提交</view>
      <view class="filter-item" :class="{ active: filter === 'submitted' }" @click="setFilter('submitted')">审核中</view>
      <view class="filter-item" :class="{ active: filter === 'approved' }" @click="setFilter('approved')">已通过</view>
    </view>
    
    <!-- 任务列表 -->
    <view class="task-list">
      <view class="task-item" v-for="task in taskList" :key="task.id" @click="goToDetail(task.id)">
        <image :src="task.image" mode="aspectFill" class="task-img" />
        <view class="task-info">
          <text class="task-title">{{ task.title }}</text>
          <text class="task-desc">{{ task.description }}</text>
          <view class="task-bottom">
            <text class="task-reward">¥{{ task.rewardAmount }}</text>
            <text class="task-status" :class="statusClass(task.status)">{{ statusText(task.status) }}</text>
          </view>
          <view class="task-tags">
            <text class="tag" v-if="task.isMemberOnly === 1">会员专属</text>
            <text class="tag">{{ task.receiveCount }}人已领</text>
          </view>
        </view>
      </view>
    </view>
    
    <!-- 加载更多 -->
    <view class="load-more" v-if="hasMore">
      <text @click="loadMore">加载更多</text>
    </view>
    <view class="no-more" v-else>
      <text>没有更多了</text>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request'

const filter = ref('all')
const taskList = ref([])
const page = ref(1)
const pageSize = 10
const hasMore = ref(true)

onMounted(() => {
  loadTasks()
})

const setFilter = (type) => {
  filter.value = type
  page.value = 1
  taskList.value = []
  loadTasks()
}

const loadTasks = async () => {
  try {
    const params = {
      page: page.value,
      pageSize
    }
    
    if (filter.value !== 'all') {
      params.status = filter.value
    }
    
    const res = await request.get('/task/list', params)
    if (res.code === 200) {
      taskList.value = [...taskList.value, ...res.data.list]
      hasMore.value = res.data.list.length === pageSize
    }
  } catch (err) {
    console.error('加载任务失败', err)
  }
}

const loadMore = () => {
  page.value++
  loadTasks()
}

const goToDetail = (taskId) => {
  uni.navigateTo({
    url: `/pages/task/detail?id=${taskId}`
  })
}

const statusText = (status) => {
  const map = { 1: '待提交', 2: '审核中', 3: '已通过', 4: '已驳回', 5: '已过期' }
  return map[status] || '未知'
}

const statusClass = (status) => {
  return `status-${status}`
}
</script>

<style scoped>
.container {
  padding: 20rpx;
}

.filter-bar {
  display: flex;
  gap: 20rpx;
  margin-bottom: 30rpx;
  background: #fff;
  padding: 20rpx;
  border-radius: 20rpx;
}

.filter-item {
  padding: 10rpx 30rpx;
  border-radius: 30rpx;
  font-size: 26rpx;
  color: #666;
  background: #f5f5f5;
}

.filter-item.active {
  background: #007aff;
  color: #fff;
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

.task-desc {
  font-size: 24rpx;
  color: #999;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.task-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.task-reward {
  font-size: 30rpx;
  color: #ff4d4f;
  font-weight: bold;
}

.task-status {
  font-size: 24rpx;
  padding: 5rpx 15rpx;
  border-radius: 20rpx;
}

.status-1 { background: #e6f7ff; color: #1890ff; }
.status-2 { background: #fff7e6; color: #fa8c16; }
.status-3 { background: #f6ffed; color: #52c41a; }
.status-4 { background: #fff1f0; color: #ff4d4f; }
.status-5 { background: #f5f5f5; color: #999; }

.task-tags {
  display: flex;
  gap: 10rpx;
}

.tag {
  font-size: 22rpx;
  color: #999;
  background: #f5f5f5;
  padding: 5rpx 15rpx;
  border-radius: 15rpx;
}
</style>
