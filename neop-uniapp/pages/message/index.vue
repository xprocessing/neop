<template>
  <view class="message-page">
    <!-- 顶部导航 -->
    <view class="nav-bar">
      <text class="nav-title">消息通知</text>
      <text class="nav-action" @click="readAll">全部已读</text>
    </view>

    <!-- 消息分类 -->
    <view class="category-tabs">
      <view class="tab-item" :class="{ 'active': activeCategory === 'all' }" @click="switchCategory('all')">
        <text class="tab-text">全部</text>
        <text class="tab-badge" v-if="unreadCount.all > 0">{{ unreadCount.all }}</text>
      </view>
      <view class="tab-item" :class="{ 'active': activeCategory === 'system' }" @click="switchCategory('system')">
        <text class="tab-text">系统通知</text>
        <text class="tab-badge" v-if="unreadCount.system > 0">{{ unreadCount.system }}</text>
      </view>
      <view class="tab-item" :class="{ 'active': activeCategory === 'order' }" @click="switchCategory('order')">
        <text class="tab-text">订单通知</text>
        <text class="tab-badge" v-if="unreadCount.order > 0">{{ unreadCount.order }}</text>
      </view>
      <view class="tab-item" :class="{ 'active': activeCategory === 'activity' }" @click="switchCategory('activity')">
        <text class="tab-text">活动通知</text>
        <text class="tab-badge" v-if="unreadCount.activity > 0">{{ unreadCount.activity }}</text>
      </view>
    </view>

    <!-- 消息列表 -->
    <view class="message-list">
      <view class="message-item" :class="{ 'unread': !item.read }" v-for="(item, index) in messageList" :key="index" @click="readMessage(item)">
        <view class="message-icon" :class="item.type">
          <text class="icon-text">{{ item.icon }}</text>
        </view>
        <view class="message-content">
          <view class="message-header">
            <text class="message-title">{{ item.title }}</text>
            <text class="message-time">{{ item.time }}</text>
          </view>
          <text class="message-desc">{{ item.desc }}</text>
        </view>
        <view class="unread-dot" v-if="!item.read"></view>
      </view>
    </view>

    <!-- 空状态 -->
    <view class="empty" v-if="messageList.length === 0">
      <text class="empty-icon">📭</text>
      <text class="empty-text">暂无消息通知</text>
    </view>
  </view>
</template>

<script setup>
import { ref, onLoad } from 'vue'

// 数据
const activeCategory = ref('all')
const unreadCount = ref({
  all: 3,
  system: 1,
  order: 1,
  activity: 1
})
const messageList = ref([
  {
    id: 1,
    type: 'system',
    icon: '🔔',
    title: '系统通知',
    desc: '您的账户已完成实名认证，现在可以享受更多服务。',
    time: '2026-04-28 10:30',
    read: false
  },
  {
    id: 2,
    type: 'order',
    icon: '📦',
    title: '订单通知',
    desc: '您的订单已发货，快递单号：SF1234567890',
    time: '2026-04-27 15:20',
    read: false
  },
  {
    id: 3,
    type: 'activity',
    icon: '🎉',
    title: '活动通知',
    desc: '新用户注册送100积分，邀请好友再送50积分！',
    time: '2026-04-26 09:00',
    read: false
  },
  {
    id: 4,
    type: 'system',
    icon: '🔔',
    title: '系统维护通知',
    desc: '系统将于2026年4月29日凌晨2:00-4:00进行维护升级，届时将暂停服务。',
    time: '2026-04-25 18:00',
    read: true
  },
  {
    id: 5,
    type: 'order',
    icon: '📦',
    title: '订单已完成',
    desc: '您的订单已确认收货，感谢您的购买！',
    time: '2026-04-24 14:30',
    read: true
  }
])

// 切换分类
const switchCategory = (category) => {
  activeCategory.value = category
  loadMessages()
}

// 加载消息
const loadMessages = async () => {
  try {
    const res = await request.get('/message/list', {
      category: activeCategory.value,
      page: 1,
      pageSize: 20
    })
    messageList.value = res.data.records
  } catch (error) {
    console.error('加载消息失败', error)
  }
}

// 读取消息
const readMessage = (item) => {
  if (!item.read) {
    item.read = true
    unreadCount.value[item.type]--
    unreadCount.value.all--
    
    // 调用API标记为已读
    request.post(`/message/read/${item.id}`)
  }
  
  // 根据消息类型跳转
  if (item.type === 'order') {
    uni.navigateTo({
      url: `/pages/shop/order-detail?id=${item.orderId}`
    })
  } else if (item.type === 'activity') {
    uni.navigateTo({
      url: '/pages/activity/detail?id=' + item.activityId
    })
  }
}

// 全部已读
const readAll = async () => {
  if (unreadCount.value.all === 0) {
    uni.showToast({ title: '没有未读消息', icon: 'none' })
    return
  }
  
  try {
    await request.post('/message/read-all')
    
    // 更新本地状态
    messageList.value.forEach(item => {
      item.read = true
    })
    unreadCount.value = {
      all: 0,
      system: 0,
      order: 0,
      activity: 0
    }
    
    uni.showToast({ title: '已全部标记为已读', icon: 'success' })
  } catch (error) {
    uni.showToast({ title: error.message || '操作失败', icon: 'none' })
  }
}

// 加载数据
onLoad(() => {
  loadMessages()
})
</script>

<style scoped>
.message-page {
  min-height: 100vh;
  background: #f5f5f5;
}

.nav-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 60rpx 30rpx 20rpx;
  background: white;
}

.nav-title {
  font-size: 34rpx;
  font-weight: bold;
  color: #333;
}

.nav-action {
  font-size: 26rpx;
  color: #667eea;
}

.category-tabs {
  display: flex;
  background: white;
  padding: 0 30rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.tab-item {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 25rpx 0;
  position: relative;
}

.tab-item.active {
  color: #667eea;
}

.tab-item.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 60rpx;
  height: 4rpx;
  background: #667eea;
}

.tab-text {
  font-size: 28rpx;
  color: #333;
}

.tab-item.active .tab-text {
  color: #667eea;
  font-weight: bold;
}

.tab-badge {
  min-width: 32rpx;
  height: 32rpx;
  line-height: 32rpx;
  background: #f5222d;
  color: white;
  font-size: 20rpx;
  border-radius: 16rpx;
  padding: 0 8rpx;
  margin-left: 8rpx;
}

.message-list {
  padding: 20rpx 30rpx;
}

.message-item {
  display: flex;
  align-items: flex-start;
  background: white;
  padding: 25rpx;
  border-radius: 15rpx;
  margin-bottom: 15rpx;
  position: relative;
}

.message-item.unread {
  background: #f0f5ff;
}

.message-icon {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20rpx;
  flex-shrink: 0;
}

.message-icon.system {
  background: #fff7e6;
}

.message-icon.order {
  background: #e6f7ff;
}

.message-icon.activity {
  background: #f6ffed;
}

.icon-text {
  font-size: 40rpx;
}

.message-content {
  flex: 1;
}

.message-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10rpx;
}

.message-title {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
}

.message-time {
  font-size: 22rpx;
  color: #999;
}

.message-desc {
  font-size: 26rpx;
  color: #666;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
}

.unread-dot {
  position: absolute;
  top: 30rpx;
  right: 25rpx;
  width: 16rpx;
  height: 16rpx;
  border-radius: 50%;
  background: #f5222d;
}

.empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 150rpx 0;
}

.empty-icon {
  font-size: 100rpx;
  margin-bottom: 30rpx;
}

.empty-text {
  font-size: 28rpx;
  color: #999;
}
</style>
