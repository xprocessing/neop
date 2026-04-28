<template>
  <view class="payment-result-page">
    <!-- 结果图标 -->
    <view class="result-icon" :class="status">
      <text class="icon-text">{{ status === 'success' ? '✅' : '❌' }}</text>
    </view>

    <!-- 结果标题 -->
    <text class="result-title">{{ status === 'success' ? '支付成功' : '支付失败' }}</text>
    <text class="result-desc">{{ status === 'success' ? '您的订单已支付成功' : '抱歉，支付未成功' }}</text>

    <!-- 订单信息 -->
    <view class="order-card" v-if="status === 'success'">
      <view class="order-item">
        <text class="order-label">订单编号</text>
        <text class="order-value">{{ orderNo }}</text>
      </view>
      <view class="order-item">
        <text class="order-label">支付金额</text>
        <text class="order-value amount">¥{{ amount }}</text>
      </view>
      <view class="order-item">
        <text class="order-label">支付方式</text>
        <text class="order-value">{{ paymentMethod }}</text>
      </view>
      <view class="order-item">
        <text class="order-label">支付时间</text>
        <text class="order-value">{{ payTime }}</text>
      </view>
    </view>

    <!-- 失败原因 -->
    <view class="error-card" v-if="status === 'fail'">
      <text class="error-title">失败原因</text>
      <text class="error-desc">{{ errorMsg }}</text>
    </view>

    <!-- 操作按钮 -->
    <view class="action-buttons">
      <button class="action-btn primary" @click="viewOrder" v-if="status === 'success'">查看订单</button>
      <button class="action-btn" @click="continueShopping">继续购物</button>
      <button class="action-btn" @click="retryPay" v-if="status === 'fail'">重新支付</button>
      <button class="action-btn" @click="contactService" v-if="status === 'fail'">联系客服</button>
    </view>

    <!-- 推荐商品 -->
    <view class="recommend-card" v-if="status === 'success'">
      <text class="recommend-title">猜你喜欢</text>
      <view class="recommend-list">
        <view class="recommend-item" v-for="(item, index) in recommendList" :key="index" @click="goGoodsDetail(item)">
          <image class="recommend-image" :src="item.image" mode="aspectFill"></image>
          <text class="recommend-name">{{ item.name }}</text>
          <text class="recommend-price">¥{{ item.price }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onLoad } from 'vue'

// 数据
const status = ref('success') // success 或 fail
const orderNo = ref('')
const amount = ref('')
const paymentMethod = ref('')
const payTime = ref('')
const errorMsg = ref('')
const recommendList = ref([
  { id: 1, name: '推荐商品1', image: '/static/goods1.jpg', price: 99.00 },
  { id: 2, name: '推荐商品2', image: '/static/goods2.jpg', price: 199.00 },
  { id: 3, name: '推荐商品3', image: '/static/goods3.jpg', price: 299.00 }
])

// 获取页面参数
onLoad((options) => {
  if (options.status) {
    status.value = options.status
  }
  if (options.orderNo) {
    orderNo.value = options.orderNo
  }
  if (options.amount) {
    amount.value = options.amount
  }
  if (options.paymentMethod) {
    paymentMethod.value = options.paymentMethod
  }
  if (options.payTime) {
    payTime.value = options.payTime
  }
  if (options.errorMsg) {
    errorMsg.value = decodeURIComponent(options.errorMsg)
  }
})

// 查看订单
const viewOrder = () => {
  uni.navigateTo({
    url: `/pages/shop/order-detail?id=${orderNo.value}`
  })
}

// 继续购物
const continueShopping = () => {
  uni.switchTab({
    url: '/pages/shop/index'
  })
}

// 重新支付
const retryPay = () => {
  uni.navigateBack()
}

// 联系客服
const contactService = () => {
  uni.makePhoneCall({
    phoneNumber: '400-123-4567'
  })
}

// 跳转商品详情
const goGoodsDetail = (item) => {
  uni.navigateTo({
    url: `/pages/shop/goods-detail?id=${item.id}`
  })
}
</script>

<style scoped>
.payment-result-page {
  min-height: 100vh;
  background: #f5f5f5;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 80rpx 30rpx 40rpx;
}

.result-icon {
  width: 160rpx;
  height: 160rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 30rpx;
}

.result-icon.success {
  background: #f0f9eb;
}

.result-icon.fail {
  background: #fef0f0;
}

.icon-text {
  font-size: 80rpx;
}

.result-title {
  font-size: 40rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 15rpx;
}

.result-desc {
  font-size: 28rpx;
  color: #999;
  margin-bottom: 50rpx;
}

.order-card {
  width: 100%;
  background: white;
  border-radius: 20rpx;
  padding: 30rpx;
  margin-bottom: 40rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.05);
}

.order-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 0;
  border-bottom: 1rpx solid #f0f0f0;
}

.order-item:last-child {
  border-bottom: none;
}

.order-label {
  font-size: 28rpx;
  color: #999;
}

.order-value {
  font-size: 28rpx;
  color: #333;
}

.order-value.amount {
  color: #f5222d;
  font-weight: bold;
}

.error-card {
  width: 100%;
  background: white;
  border-radius: 20rpx;
  padding: 30rpx;
  margin-bottom: 40rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.05);
}

.error-title {
  font-size: 30rpx;
  font-weight: bold;
  color: #333;
  display: block;
  margin-bottom: 15rpx;
}

.error-desc {
  font-size: 26rpx;
  color: #999;
  line-height: 1.5;
}

.action-buttons {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 20rpx;
  margin-bottom: 50rpx;
}

.action-btn {
  min-width: 250rpx;
  height: 80rpx;
  line-height: 80rpx;
  font-size: 28rpx;
  border-radius: 40rpx;
  background: white;
  color: #666;
  border: 2rpx solid #ddd;
}

.action-btn.primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
}

.recommend-card {
  width: 100%;
}

.recommend-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  display: block;
  margin-bottom: 30rpx;
}

.recommend-list {
  display: flex;
  flex-wrap: wrap;
  gap: 20rpx;
}

.recommend-item {
  width: calc(33.33% - 14rpx);
  background: white;
  border-radius: 15rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.05);
}

.recommend-image {
  width: 100%;
  height: 200rpx;
}

.recommend-name {
  font-size: 24rpx;
  color: #333;
  padding: 15rpx;
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.recommend-price {
  font-size: 26rpx;
  color: #f5222d;
  font-weight: bold;
  padding: 0 15rpx 15rpx;
  display: block;
}
</style>
