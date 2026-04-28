<template>
  <view class="help-page">
    <!-- 顶部导航 -->
    <view class="nav-bar">
      <view class="nav-back" @click="goBack">
        <text class="back-icon">←</text>
      </view>
      <text class="nav-title">帮助中心</text>
      <view class="nav-placeholder"></view>
    </view>

    <!-- 搜索框 -->
    <view class="search-bar">
      <text class="search-icon">🔍</text>
      <input 
        class="search-input" 
        type="text" 
        placeholder="搜索问题" 
        v-model="keyword"
        @input="handleSearch"
      />
      <text class="clear-icon" v-if="keyword" @click="clearKeyword">✕</text>
    </view>

    <!-- 常见问题 -->
    <view class="faq-card" v-if="!keyword">
      <text class="card-title">常见问题</text>
      <view class="faq-list">
        <view class="faq-item" v-for="(item, index) in faqList" :key="index" @click="toggleFaq(index)">
          <view class="faq-header">
            <text class="faq-question">{{ item.question }}</text>
            <text class="faq-arrow" :class="{ 'expanded': item.expanded }">›</text>
          </view>
          <text class="faq-answer" v-if="item.expanded">{{ item.answer }}</text>
        </view>
      </view>
    </view>

    <!-- 搜索结果 -->
    <view class="search-result" v-if="keyword && searchResults.length > 0">
      <text class="result-title">搜索结果</text>
      <view class="result-list">
        <view class="result-item" v-for="(item, index) in searchResults" :key="index" @click="viewDetail(item)">
          <text class="result-question">{{ item.question }}</text>
          <text class="result-arrow">›</text>
        </view>
      </view>
    </view>

    <!-- 联系客服 -->
    <view class="contact-card">
      <text class="card-title">联系客服</text>
      <view class="contact-list">
        <view class="contact-item" @click="callService">
          <text class="contact-icon">📞</text>
          <view class="contact-content">
            <text class="contact-label">客服电话</text>
            <text class="contact-value">400-123-4567</text>
          </view>
          <text class="contact-arrow">›</text>
        </view>
        <view class="contact-item" @click="openOnlineService">
          <text class="contact-icon">💬</text>
          <view class="contact-content">
            <text class="contact-label">在线客服</text>
            <text class="contact-value">工作时间：9:00-18:00</text>
          </view>
          <text class="contact-arrow">›</text>
        </view>
        <view class="contact-item" @click="openFeedback">
          <text class="contact-icon">📝</text>
          <view class="contact-content">
            <text class="contact-label">意见反馈</text>
            <text class="contact-value">您的反馈是我们改进的动力</text>
          </view>
          <text class="contact-arrow">›</text>
        </view>
      </view>
    </view>

    <!-- 更多帮助 -->
    <view class="more-card">
      <text class="card-title">更多帮助</text>
      <view class="more-list">
        <view class="more-item" @click="goPage('/pages/about/agreement')">
          <text class="more-icon">📄</text>
          <text class="more-text">用户协议</text>
          <text class="more-arrow">›</text>
        </view>
        <view class="more-item" @click="goPage('/pages/about/privacy')">
          <text class="more-icon">🔒</text>
          <text class="more-text">隐私政策</text>
          <text class="more-arrow">›</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onLoad } from 'vue'

// 数据
const keyword = ref('')
const searchResults = ref([])
const faqList = ref([
  {
    question: '如何注册账户？',
    answer: '您可以通过手机号注册账户。点击登录页面的"注册"按钮，输入手机号码，获取验证码，设置密码后即可完成注册。',
    expanded: false
  },
  {
    question: '如何绑定手机号码？',
    answer: '在"我的"页面点击"设置"，选择"手机号码"，输入新的手机号码和验证码即可完成绑定。',
    expanded: false
  },
  {
    question: '如何修改密码？',
    answer: '在"我的"页面点击"设置"，选择"修改密码"，输入原密码和新密码即可完成修改。如果忘记密码，可以在登录页面点击"忘记密码"进行重置。',
    expanded: false
  },
  {
    question: '如何查看我的积分？',
    answer: '在"我的"页面可以看到当前积分余额。点击"积分明细"可以查看积分的获取和使用记录。',
    expanded: false
  },
  {
    question: '如何邀请好友？',
    answer: '在"我的"页面点击"邀请好友"，可以通过微信、朋友圈、二维码等方式分享邀请链接。好友通过您的邀请链接注册后，您可以获得积分奖励。',
    expanded: false
  },
  {
    question: '如何下单购买商品？',
    answer: '在商城选择您想要的商品，点击"立即购买"或"加入购物车"，填写收货地址后点击"提交订单"，选择支付方式完成支付即可。',
    expanded: false
  },
  {
    question: '如何查看订单状态？',
    answer: '在"我的"页面点击"我的订单"，可以查看所有订单的状态，包括待付款、待发货、待收货、已完成等。',
    expanded: false
  },
  {
    question: '如何申请退款？',
    answer: '在订单详情页面点击"申请退款"，填写退款原因后提交。客服会在1-3个工作日内处理您的退款申请。',
    expanded: false
  }
])

// 处理搜索
const handleSearch = () => {
  if (!keyword.value) {
    searchResults.value = []
    return
  }
  
  // 模拟搜索
  searchResults.value = faqList.value.filter(item => 
    item.question.includes(keyword.value) || item.answer.includes(keyword.value)
  )
}

// 清空关键词
const clearKeyword = () => {
  keyword.value = ''
  searchResults.value = []
}

// 切换FAQ展开状态
const toggleFaq = (index) => {
  faqList.value[index].expanded = !faqList.value[index].expanded
}

// 查看详情
const viewDetail = (item) => {
  uni.showModal({
    title: item.question,
    content: item.answer,
    showCancel: false,
    confirmText: '我知道了'
  })
}

// 拨打客服电话
const callService = () => {
  uni.makePhoneCall({
    phoneNumber: '400-123-4567'
  })
}

// 打开在线客服
const openOnlineService = () => {
  // #ifdef H5 || APP-PLUS
  // 这里可以集成在线客服系统，如网易七鱼、美洽等
  uni.showToast({ title: '在线客服开发中', icon: 'none' })
  // #endif
  
  // #ifdef MP-WEIXIN
  // 小程序可以使用微信客服
  uni.showToast({ title: '请拨打客服电话', icon: 'none' })
  // #endif
}

// 打开意见反馈
const openFeedback = () => {
  uni.navigateTo({
    url: '/pages/feedback/index'
  })
}

// 跳转页面
const goPage = (url) => {
  uni.navigateTo({ url })
}

// 返回
const goBack = () => {
  uni.navigateBack()
}
</script>

<style scoped>
.help-page {
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

.search-bar {
  display: flex;
  align-items: center;
  margin: 20rpx 30rpx;
  padding: 0 30rpx;
  height: 70rpx;
  background: white;
  border-radius: 40rpx;
}

.search-icon {
  font-size: 32rpx;
  margin-right: 15rpx;
}

.search-input {
  flex: 1;
  height: 70rpx;
  font-size: 28rpx;
  color: #333;
}

.clear-icon {
  font-size: 28rpx;
  color: #999;
  padding: 10rpx;
}

.faq-card,
.contact-card,
.more-card {
  background: white;
  margin: 20rpx 30rpx;
  border-radius: 20rpx;
  padding: 30rpx;
}

.card-title {
  font-size: 30rpx;
  font-weight: bold;
  color: #333;
  display: block;
  margin-bottom: 20rpx;
}

.faq-list {
  margin-top: 10rpx;
}

.faq-item {
  padding: 20rpx 0;
  border-bottom: 1rpx solid #f0f0f0;
}

.faq-item:last-child {
  border-bottom: none;
}

.faq-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.faq-question {
  flex: 1;
  font-size: 28rpx;
  color: #333;
  font-weight: 500;
}

.faq-arrow {
  font-size: 32rpx;
  color: #ccc;
  transition: transform 0.3s;
}

.faq-arrow.expanded {
  transform: rotate(90deg);
}

.faq-answer {
  font-size: 26rpx;
  color: #666;
  line-height: 1.8;
  margin-top: 15rpx;
  padding: 20rpx;
  background: #f8f8f8;
  border-radius: 10rpx;
  display: block;
}

.search-result {
  margin: 20rpx 30rpx;
}

.result-title {
  font-size: 28rpx;
  color: #999;
  display: block;
  margin-bottom: 15rpx;
}

.result-list {
  background: white;
  border-radius: 20rpx;
  padding: 0 30rpx;
}

.result-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 25rpx 0;
  border-bottom: 1rpx solid #f0f0f0;
}

.result-item:last-child {
  border-bottom: none;
}

.result-question {
  font-size: 28rpx;
  color: #333;
}

.result-arrow {
  font-size: 32rpx;
  color: #ccc;
}

.contact-list {
  margin-top: 10rpx;
}

.contact-item {
  display: flex;
  align-items: center;
  padding: 20rpx 0;
  border-bottom: 1rpx solid #f0f0f0;
}

.contact-item:last-child {
  border-bottom: none;
}

.contact-icon {
  font-size: 40rpx;
  margin-right: 20rpx;
}

.contact-content {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.contact-label {
  font-size: 28rpx;
  color: #333;
  font-weight: 500;
  margin-bottom: 5rpx;
}

.contact-value {
  font-size: 24rpx;
  color: #999;
}

.contact-arrow {
  font-size: 32rpx;
  color: #ccc;
}

.more-list {
  margin-top: 10rpx;
}

.more-item {
  display: flex;
  align-items: center;
  padding: 25rpx 0;
  border-bottom: 1rpx solid #f0f0f0;
}

.more-item:last-child {
  border-bottom: none;
}

.more-icon {
  font-size: 36rpx;
  margin-right: 20rpx;
}

.more-text {
  flex: 1;
  font-size: 28rpx;
  color: #333;
}

.more-arrow {
  font-size: 32rpx;
  color: #ccc;
}
</style>
