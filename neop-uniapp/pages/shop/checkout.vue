<template>
  <view class="checkout-page">
    <!-- 顶部导航 -->
    <view class="nav-bar">
      <view class="nav-back" @click="goBack">
        <text class="back-icon">←</text>
      </view>
      <text class="nav-title">确认订单</text>
      <view class="nav-placeholder"></view>
    </view>

    <!-- 收货地址 -->
    <view class="address-card" @click="selectAddress">
      <view class="address-icon">📍</view>
      <view class="address-content" v-if="selectedAddress">
        <view class="address-header">
          <text class="address-name">{{ selectedAddress.name }}</text>
          <text class="address-phone">{{ selectedAddress.phone }}</text>
        </view>
        <text class="address-detail">{{ selectedAddress.province }} {{ selectedAddress.city }} {{ selectedAddress.district }} {{ selectedAddress.detail }}</text>
      </view>
      <view class="address-empty" v-else>
        <text class="empty-text">请选择收货地址</text>
      </view>
      <text class="arrow-icon">›</text>
    </view>

    <!-- 商品列表 -->
    <view class="goods-card">
      <view class="card-title">商品信息</view>
      <view class="goods-list">
        <view class="goods-item" v-for="(item, index) in goodsList" :key="index">
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
    </view>

    <!-- 支付方式 -->
    <view class="payment-card">
      <view class="card-title">支付方式</view>
      <view class="payment-list">
        <view class="payment-item" @click="selectPayment('wechat')">
          <view class="payment-left">
            <text class="payment-icon">💬</text>
            <text class="payment-name">微信支付</text>
          </view>
          <view class="payment-check" :class="{ 'checked': paymentMethod === 'wechat' }">
            <text v-if="paymentMethod === 'wechat'">✓</text>
          </view>
        </view>
        <view class="payment-item" @click="selectPayment('alipay')">
          <view class="payment-left">
            <text class="payment-icon">💰</text>
            <text class="payment-name">支付宝支付</text>
          </view>
          <view class="payment-check" :class="{ 'checked': paymentMethod === 'alipay' }">
            <text v-if="paymentMethod === 'alipay'">✓</text>
          </view>
        </view>
        <view class="payment-item" @click="selectPayment('points')">
          <view class="payment-left">
            <text class="payment-icon">🎁</text>
            <text class="payment-name">积分支付（剩余：{{ userPoints }}积分）</text>
          </view>
          <view class="payment-check" :class="{ 'checked': paymentMethod === 'points' }">
            <text v-if="paymentMethod === 'points'">✓</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 订单备注 -->
    <view class="remark-card">
      <view class="card-title">订单备注</view>
      <textarea 
        class="remark-input" 
        placeholder="选填，请输入备注信息" 
        v-model="remark"
        maxlength="200"
      ></textarea>
    </view>

    <!-- 价格明细 -->
    <view class="price-card">
      <view class="price-item">
        <text class="price-label">商品总价</text>
        <text class="price-value">¥{{ goodsTotal }}</text>
      </view>
      <view class="price-item">
        <text class="price-label">运费</text>
        <text class="price-value">¥{{ shippingFee }}</text>
      </view>
      <view class="price-item" v-if="discount > 0">
        <text class="price-label">优惠</text>
        <text class="price-value discount">-¥{{ discount }}</text>
      </view>
      <view class="price-divider"></view>
      <view class="price-item total">
        <text class="price-label">实付金额</text>
        <text class="price-value total-price">¥{{ totalAmount }}</text>
      </view>
    </view>

    <!-- 底部提交栏 -->
    <view class="submit-bar">
      <view class="submit-left">
        <text class="submit-label">实付：</text>
        <text class="submit-total">¥{{ totalAmount }}</text>
      </view>
      <button class="submit-btn" @click="handleSubmit">提交订单</button>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onLoad } from 'vue'

// 数据
const goodsList = ref([])
const selectedAddress = ref(null)
const paymentMethod = ref('wechat')
const remark = ref('')
const shippingFee = ref(0)
const discount = ref(0)
const userPoints = ref(1000)

// 获取页面参数
onLoad((options) => {
  if (options.goods) {
    goodsList.value = JSON.parse(decodeURIComponent(options.goods))
  }
  loadDefaultAddress()
})

// 计算属性
const goodsTotal = computed(() => {
  return goodsList.value.reduce((total, item) => {
    return total + item.price * item.quantity
  }, 0)
})

const totalAmount = computed(() => {
  return goodsTotal.value + shippingFee.value - discount.value
})

// 加载默认地址
const loadDefaultAddress = async () => {
  try {
    const res = await request.get('/user/address/default')
    selectedAddress.value = res.data
  } catch (error) {
    console.error('加载默认地址失败', error)
  }
}

// 选择地址
const selectAddress = () => {
  uni.navigateTo({
    url: '/pages/shop/address?selectMode=true'
  })
}

// 选择支付方式
const selectPayment = (method) => {
  paymentMethod.value = method
}

// 提交订单
const handleSubmit = async () => {
  if (!selectedAddress.value) {
    uni.showToast({ title: '请选择收货地址', icon: 'none' })
    return
  }

  try {
    uni.showLoading({ title: '提交中...' })
    
    const orderData = {
      goodsList: goodsList.value,
      addressId: selectedAddress.value.id,
      paymentMethod: paymentMethod.value,
      remark: remark.value,
      totalAmount: totalAmount.value
    }

    const res = await request.post('/shop/order/create', orderData)
    
    uni.hideLoading()
    
    // 跳转支付
    if (paymentMethod.value === 'points') {
      // 积分支付，直接成功
      uni.redirectTo({
        url: '/pages/shop/payment-result?orderId=' + res.data.orderId + '&status=success'
      })
    } else {
      // 第三方支付
      uni.redirectTo({
        url: '/pages/shop/payment?orderId=' + res.data.orderId + '&amount=' + totalAmount.value
      })
    }
  } catch (error) {
    uni.hideLoading()
    uni.showToast({ title: error.message || '提交失败', icon: 'none' })
  }
}

// 返回
const goBack = () => {
  uni.navigateBack()
}
</script>

<style scoped>
.checkout-page {
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

.address-empty {
  flex:1;
}

.empty-text {
  font-size: 28rpx;
  color: #999;
}

.arrow-icon {
  font-size: 36rpx;
  color: #ccc;
}

.goods-card,
.payment-card,
.remark-card,
.price-card {
  background: white;
  padding: 30rpx;
  margin-bottom: 20rpx;
}

.card-title {
  font-size: 30rpx;
  font-weight: bold;
  color: #333;
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

.payment-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 25rpx 0;
  border-bottom: 1rpx solid #f0f0f0;
}

.payment-item:last-child {
  border-bottom: none;
}

.payment-left {
  display: flex;
  align-items: center;
}

.payment-icon {
  font-size: 40rpx;
  margin-right: 20rpx;
}

.payment-name {
  font-size: 28rpx;
  color: #333;
}

.payment-check {
  width: 40rpx;
  height: 40rpx;
  border: 2rpx solid #ddd;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24rpx;
  color: white;
}

.payment-check.checked {
  background: #667eea;
  border-color: #667eea;
}

.remark-input {
  width: 100%;
  height: 150rpx;
  background: #f8f8f8;
  border-radius: 10rpx;
  padding: 20rpx;
  font-size: 26rpx;
  box-sizing: border-box;
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

.submit-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: white;
  padding: 20rpx 30rpx;
  box-shadow: 0 -4rpx 10rpx rgba(0, 0, 0, 0.05);
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
}

.submit-left {
  display: flex;
  align-items: baseline;
}

.submit-label {
  font-size: 26rpx;
  color: #666;
}

.submit-total {
  font-size: 40rpx;
  color: #f5222d;
  font-weight: bold;
}

.submit-btn {
  width: 300rpx;
  height: 80rpx;
  line-height: 80rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  font-size: 30rpx;
  font-weight: bold;
  border-radius: 40rpx;
}
</style>
