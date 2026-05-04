<template>
  <view class="order-detail-page">
    <!-- 顶部导航 -->
    <view class="nav-bar">
      <view class="nav-back" @click="goBack">
        <text class="back-icon">←</text>
      </view>
      <text class="nav-title">订单详情</text>
      <view class="nav-placeholder"></view>
    </view>

    <!-- 订单状态 -->
    <view class="status-card" :class="orderStatus.class">
      <text class="status-icon">{{ orderStatus.icon }}</text>
      <view class="status-content">
        <text class="status-text">{{ orderStatus.text }}</text>
        <text class="status-desc">{{ orderStatus.desc }}</text>
      </view>
    </view>

    <!-- 收货信息 -->
    <view class="address-card">
      <view class="address-icon">📍</view>
      <view class="address-content">
        <view class="address-header">
          <text class="address-name">{{ order.address.name }}</text>
          <text class="address-phone">{{ order.address.phone }}</text>
        </view>
        <text class="address-detail">{{ order.address.province }} {{ order.address.city }} {{ order.address.district }} {{ order.address.detail }}</text>
      </view>
    </view>

    <!-- 商品信息 -->
    <view class="goods-card">
      <view class="goods-item" v-for="(item, index) in order.goodsList" :key="index">
        <image class="goods-image" :src="item.image" mode="aspectFill"></image>
        <view class="goods-info">
          <text class="goods-name">{{ item.name }}</text>
          <text class="goods-spec">{{ item.spec }}</text>
          <view class="goods-bottom">
            <text class="goods-price">¥{{ item.price }}</text>
            <text class="goods-quantity">x{{ item.quantity }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 订单信息 -->
    <view class="order-info-card">
      <view class="info-item">
        <text class="info-label">订单编号</text>
        <view class="info-value">
          <text class="value-text">{{ order.orderNo }}</text>
          <text class="copy-btn" @click="copyOrderNo">复制</text>
        </view>
      </view>
      <view class="info-item">
        <text class="info-label">下单时间</text>
        <text class="info-value">{{ order.createTime }}</text>
      </view>
      <view class="info-item" v-if="order.payTime">
        <text class="info-label">支付时间</text>
        <text class="info-value">{{ order.payTime }}</text>
      </view>
      <view class="info-item" v-if="order.deliveryTime">
        <text class="info-label">发货时间</text>
        <text class="info-value">{{ order.deliveryTime }}</text>
      </view>
      <view class="info-item">
        <text class="info-label">支付方式</text>
        <text class="info-value">{{ order.paymentMethod }}</text>
      </view>
    </view>

    <!-- 价格明细 -->
    <view class="price-card">
      <view class="price-item">
        <text class="price-label">商品总价</text>
        <text class="price-value">¥{{ order.goodsTotal }}</text>
      </view>
      <view class="price-item">
        <text class="price-label">运费</text>
        <text class="price-value">¥{{ order.shippingFee }}</text>
      </view>
      <view class="price-item" v-if="order.discount > 0">
        <text class="price-label">优惠</text>
        <text class="price-value discount">-¥{{ order.discount }}</text>
      </view>
      <view class="price-divider"></view>
      <view class="price-item total">
        <text class="price-label">实付金额</text>
        <text class="price-value total-price">¥{{ order.totalAmount }}</text>
      </view>
    </view>

    <!-- 底部操作栏 -->
    <view class="action-bar">
      <button class="action-btn" v-if="order.status === 'pending'" @click="cancelOrder">取消订单</button>
      <button class="action-btn primary" v-if="order.status === 'pending'" @click="payOrder">立即支付</button>
      <button class="action-btn" v-if="order.status === 'shipped'" @click="viewLogistics">查看物流</button>
      <button class="action-btn primary" v-if="order.status === 'shipped'" @click="confirmReceive">确认收货</button>
      <button class="action-btn" v-if="order.status === 'completed'" @click="reviewOrder">评价</button>
      <button class="action-btn" v-if="order.status === 'cancelled'" @click="deleteOrder">删除订单</button>
      <button class="action-btn primary" v-if="order.status === 'completed'" @click="buyAgain">再次购买</button>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onLoad } from 'vue'

// 订单数据
const order = ref({
  orderNo: '',
  status: '',
  address: {
    name: '',
    phone: '',
    province: '',
    city: '',
    district: '',
    detail: ''
  },
  goodsList: [],
  goodsTotal: 0,
  shippingFee: 0,
  discount: 0,
  totalAmount: 0,
  createTime: '',
  payTime: '',
  deliveryTime: '',
  paymentMethod: ''
})

// 订单状态映射
const orderStatus = computed(() => {
  const statusMap = {
    'pending': { text: '等待支付', desc: '请尽快完成支付', icon: '⏰', class: 'pending' },
    'paid': { text: '已支付', desc: '商家正在处理', icon: '✅', class: 'paid' },
    'shipped': { text: '已发货', desc: '商品正在运输中', icon: '🚚', class: 'shipped' },
    'completed': { text: '已完成', desc: '感谢您的购买', icon: '🎉', class: 'completed' },
    'cancelled': { text: '已取消', desc: '订单已取消', icon: '❌', class: 'cancelled' }
  }
  return statusMap[order.value.status] || { text: '未知状态', desc: '', icon: '❓', class: '' }
})

// 获取页面参数
onLoad((options) => {
  if (options.id) {
    loadOrderDetail(options.id)
  }
})

// 加载订单详情
const loadOrderDetail = async (orderId) => {
  try {
    uni.showLoading({ title: '加载中...' })
    const res = await request.get(`/shop/order/info/${orderId}`)
    order.value = res.data
    uni.hideLoading()
  } catch (error) {
    uni.hideLoading()
    uni.showToast({ title: error.message || '加载失败', icon: 'none' })
  }
}

// 复制订单号
const copyOrderNo = () => {
  uni.setClipboardData({
    data: order.value.orderNo,
    success: () => {
      uni.showToast({ title: '已复制', icon: 'success' })
    }
  })
}

// 取消订单
const cancelOrder = async () => {
  uni.showModal({
    title: '提示',
    content: '确定要取消此订单吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          await request.post('/shop/order/cancel', { orderId: order.value.id })
          uni.showToast({ title: '订单已取消', icon: 'success' })
          loadOrderDetail(order.value.id)
        } catch (error) {
          uni.showToast({ title: error.message || '操作失败', icon: 'none' })
        }
      }
    }
  })
}

// 支付订单
const payOrder = () => {
  uni.navigateTo({
    url: `/pages/shop/payment?orderId=${order.value.id}&amount=${order.value.totalAmount}`
  })
}

// 查看物流
const viewLogistics = () => {
  uni.navigateTo({
    url: `/pages/shop/logistics?orderId=${order.value.id}`
  })
}

// 确认收货
const confirmReceive = async () => {
  uni.showModal({
    title: '提示',
    content: '确认已收到商品吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          await request.post('/shop/order/confirm', { orderId: order.value.id })
          uni.showToast({ title: '确认收货成功', icon: 'success' })
          loadOrderDetail(order.value.id)
        } catch (error) {
          uni.showToast({ title: error.message || '操作失败', icon: 'none' })
        }
      }
    }
  })
}

// 评价订单
const reviewOrder = () => {
  uni.navigateTo({
    url: `/pages/shop/review?orderId=${order.value.id}`
  })
}

// 删除订单
const deleteOrder = async () => {
  uni.showModal({
    title: '提示',
    content: '确定要删除此订单吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          await request.delete('/shop/order/delete', { data: { orderId: order.value.id } })
          uni.showToast({ title: '订单已删除', icon: 'success' })
          setTimeout(() => {
            uni.navigateBack()
          }, 1500)
        } catch (error) {
          uni.showToast({ title: error.message || '操作失败', icon: 'none' })
        }
      }
    }
  })
}

// 再次购买
const buyAgain = () => {
  const goodsList = order.value.goodsList.map(item => ({
    id: item.id,
    name: item.name,
    image: item.image,
    price: item.price,
    quantity: item.quantity
  }))
  uni.navigateTo({
    url: `/pages/shop/checkout?goods=${encodeURIComponent(JSON.stringify(goodsList))}`
  })
}

// 返回
const goBack = () => {
  uni.navigateBack()
}
</script>

<style scoped>
.order-detail-page {
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: 120rpx;
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

.status-card {
  display: flex;
  align-items: center;
  padding: 40rpx 30rpx;
  color: white;
}

.status-card.pending {
  background: linear-gradient(135deg, #faad14 0%, #fa8c16 100%);
}

.status-card.paid {
  background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
}

.status-card.shipped {
  background: linear-gradient(135deg, #13c2c2 0%, #08979c 100%);
}

.status-card.completed {
  background: linear-gradient(135deg, #52c41a 0%, #389e0d 100%);
}

.status-card.cancelled {
  background: linear-gradient(135deg, #999 0%, #666 100%);
}

.status-icon {
  font-size: 60rpx;
  margin-right: 20rpx;
}

.status-content {
  display: flex;
  flex-direction: column;
}

.status-text {
  font-size: 36rpx;
  font-weight: bold;
  margin-bottom: 5rpx;
}

.status-desc {
  font-size: 26rpx;
  opacity: 0.8;
}

.address-card {
  display: flex;
  align-items: center;
  background: white;
  padding: 30rpx;
  margin: 20rpx 0;
}

.address-icon {
  font-size: 48rpx;
  margin-right: 20rpx;
}

.address-content {
  flex:1;
}

.address-header {
  display: flex;
  align-items: center;
  margin-bottom: 10rpx;
}

.address-name {
  font-size: 30rpx;
  font-weight: bold;
  color: #333;
  margin-right: 20rpx;
}

.address-phone {
  font-size: 28rpx;
  color: #666;
}

.address-detail {
  font-size: 26rpx;
  color: #999;
  line-height: 1.5;
}

.goods-card {
  background: white;
  padding: 30rpx;
  margin-bottom: 20rpx;
}

.goods-item {
  display: flex;
  padding: 20rpx 0;
  border-bottom: 1rpx solid #f0f0f0;
}

.goods-item:last-child {
  border-bottom: none;
}

.goods-image {
  width: 160rpx;
  height: 160rpx;
  border-radius: 10rpx;
  margin-right: 20rpx;
}

.goods-info {
  flex:1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.goods-name {
  font-size: 28rpx;
  color: #333;
  line-height: 1.5;
}

.goods-spec {
  font-size: 24rpx;
  color: #999;
}

.goods-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.goods-price {
  font-size: 30rpx;
  color: #f5222d;
  font-weight: bold;
}

.goods-quantity {
  font-size: 26rpx;
  color: #999;
}

.order-info-card,
.price-card {
  background: white;
  padding: 30rpx;
  margin-bottom: 20rpx;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15rpx 0;
}

.info-label {
  font-size: 28rpx;
  color: #999;
}

.info-value {
  display: flex;
  align-items: center;
  font-size: 28rpx;
  color: #333;
}

.value-text {
  margin-right: 15rpx;
}

.copy-btn {
  font-size: 24rpx;
  color: #667eea;
}

.price-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15rpx 0;
}

.price-label {
  font-size: 28rpx;
  color: #666;
}

.price-value {
  font-size: 28rpx;
  color: #333;
}

.price-value.discount {
  color: #52c41a;
}

.price-divider {
  height: 1rpx;
  background: #f0f0f0;
  margin: 15rpx 0;
}

.price-item.total {
  padding-top: 20rpx;
}

.price-value.total-price {
  font-size: 36rpx;
  color: #f5222d;
  font-weight: bold;
}

.action-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  background: white;
  padding: 20rpx 30rpx;
  box-shadow: 0 -4rpx 10rpx rgba(0, 0, 0, 0.05);
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
}

.action-btn {
  height: 70rpx;
  line-height: 70rpx;
  padding: 0 30rpx;
  font-size: 26rpx;
  border-radius: 35rpx;
  background: white;
  color: #666;
  border: 2rpx solid #ddd;
  margin-left: 20rpx;
}

.action-btn.primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
}
</style>
