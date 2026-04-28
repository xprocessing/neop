<template>
  <view class="search-page">
    <!-- 搜索栏 -->
    <view class="search-bar">
      <view class="search-input-wrapper">
        <text class="search-icon">🔍</text>
        <input 
          class="search-input" 
          type="text" 
          placeholder="搜索商品/任务" 
          v-model="keyword"
          @input="handleInput"
          @confirm="handleSearch"
          focus
        />
        <text class="clear-icon" v-if="keyword" @click="clearKeyword">✕</text>
      </view>
      <text class="cancel-btn" @click="goBack">取消</text>
    </view>

    <!-- 搜索历史 -->
    <view class="history-card" v-if="!keyword && searchHistory.length > 0">
      <view class="card-header">
        <text class="card-title">搜索历史</text>
        <text class="clear-btn" @click="clearHistory">清空</text>
      </view>
      <view class="tag-list">
        <view class="tag-item" v-for="(item, index) in searchHistory" :key="index" @click="selectTag(item)">
          <text class="tag-text">{{ item }}</text>
        </view>
      </view>
    </view>

    <!-- 热门搜索 -->
    <view class="hot-card" v-if="!keyword">
      <view class="card-header">
        <text class="card-title">热门搜索</text>
      </view>
      <view class="tag-list">
        <view class="tag-item hot" v-for="(item, index) in hotKeywords" :key="index" @click="selectTag(item)">
          <text class="tag-rank" :class="{ 'top': index < 3 }">{{ index + 1 }}</text>
          <text class="tag-text">{{ item.keyword }}</text>
          <text class="tag-hot" v-if="item.hot">🔥</text>
        </view>
      </view>
    </view>

    <!-- 搜索建议 -->
    <view class="suggestion-card" v-if="keyword && suggestions.length > 0">
      <view class="suggestion-item" v-for="(item, index) in suggestions" :key="index" @click="selectSuggestion(item)">
        <text class="suggestion-icon">🔍</text>
        <text class="suggestion-text">{{ item }}</text>
      </view>
    </view>

    <!-- 搜索结果 -->
    <view class="result-card" v-if="showResult">
      <view class="result-tabs">
        <view class="tab-item" :class="{ 'active': activeTab === 'goods' }" @click="switchTab('goods')">
          <text class="tab-text">商品</text>
          <text class="tab-count">({{ goodsTotal }})</text>
        </view>
        <view class="tab-item" :class="{ 'active': activeTab === 'task' }" @click="switchTab('task')">
          <text class="tab-text">任务</text>
          <text class="tab-count">({{ taskTotal }})</text>
        </view>
      </view>

      <!-- 商品结果 -->
      <view class="goods-list" v-if="activeTab === 'goods'">
        <view class="goods-item" v-for="(item, index) in goodsList" :key="index" @click="goGoodsDetail(item)">
          <image class="goods-image" :src="item.image" mode="aspectFill"></image>
          <view class="goods-info">
            <text class="goods-name">{{ item.name }}</text>
            <text class="goods-desc">{{ item.desc }}</text>
            <text class="goods-price">¥{{ item.price }}</text>
          </view>
        </view>
      </view>

      <!-- 任务结果 -->
      <view class="task-list" v-if="activeTab === 'task'">
        <view class="task-item" v-for="(item, index) in taskList" :key="index" @click="goTaskDetail(item)">
          <view class="task-header">
            <text class="task-title">{{ item.title }}</text>
            <text class="task-reward">+{{ item.reward }}</text>
          </view>
          <text class="task-desc">{{ item.desc }}</text>
          <view class="task-footer">
            <text class="task-category">{{ item.category }}</text>
            <text class="task-count">已参与{{ item.participants }}人</text>
          </view>
        </view>
      </view>

      <!-- 空结果 -->
      <view class="empty" v-if="(activeTab === 'goods' && goodsList.length === 0) || (activeTab === 'task' && taskList.length === 0)">
        <text class="empty-icon">🔍</text>
        <text class="empty-text">未找到相关结果</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onLoad } from 'vue'

// 数据
const keyword = ref('')
const searchHistory = ref(['手机', '电脑', '任务赚钱'])
const hotKeywords = ref([
  { keyword: '智能手机', hot: true },
  { keyword: '笔记本电脑', hot: true },
  { keyword: '耳机', hot: false },
  { keyword: '任务赚钱', hot: true },
  { keyword: '积分兑换', hot: false }
])
const suggestions = ref([])
const showResult = ref(false)
const activeTab = ref('goods')
const goodsList = ref([])
const taskList = ref([])
const goodsTotal = ref(0)
const taskTotal = ref(0)

// 处理输入
const handleInput = () => {
  if (!keyword.value) {
    showResult.value = false
    suggestions.value = []
    return
  }
  
  // 模拟获取搜索建议
  suggestions.value = [
    keyword.value + '手机',
    keyword.value + '电脑',
    keyword.value + '配件'
  ]
}

// 处理搜索
const handleSearch = () => {
  if (!keyword.value) return
  
  // 保存到搜索历史
  if (!searchHistory.value.includes(keyword.value)) {
    searchHistory.value.unshift(keyword.value)
    if (searchHistory.value.length > 10) {
      searchHistory.value.pop()
    }
    // 保存到本地存储
    uni.setStorageSync('searchHistory', searchHistory.value)
  }
  
  // 执行搜索
  doSearch()
}

// 执行搜索
const doSearch = async () => {
  showResult.value = true
  suggestions.value = []
  
  try {
    // 搜索商品
    const goodsRes = await request.get('/api/shop/goods/search', {
      keyword: keyword.value,
      page: 1,
      pageSize: 20
    })
    goodsList.value = goodsRes.data.list
    goodsTotal.value = goodsRes.data.total
    
    // 搜索任务
    const taskRes = await request.get('/api/task/search', {
      keyword: keyword.value,
      page: 1,
      pageSize: 20
    })
    taskList.value = taskRes.data.list
    taskTotal.value = taskRes.data.total
  } catch (error) {
    console.error('搜索失败', error)
  }
}

// 选择标签
const selectTag = (tag) => {
  keyword.value = tag
  doSearch()
}

// 选择建议
const selectSuggestion = (suggestion) => {
  keyword.value = suggestion
  doSearch()
}

// 清空关键词
const clearKeyword = () => {
  keyword.value = ''
  showResult.value = false
  suggestions.value = []
}

// 清空历史
const clearHistory = () => {
  uni.showModal({
    title: '提示',
    content: '确定要清空搜索历史吗？',
    success: (res) => {
      if (res.confirm) {
        searchHistory.value = []
        uni.removeStorageSync('searchHistory')
      }
    }
  })
}

// 切换标签
const switchTab = (tab) => {
  activeTab.value = tab
}

// 跳转商品详情
const goGoodsDetail = (item) => {
  uni.navigateTo({
    url: `/pages/shop/goods-detail?id=${item.id}`
  })
}

// 跳转任务详情
const goTaskDetail = (item) => {
  uni.navigateTo({
    url: `/pages/task/detail?id=${item.id}`
  })
}

// 返回
const goBack = () => {
  uni.navigateBack()
}

// 加载搜索历史
onLoad(() => {
  const history = uni.getStorageSync('searchHistory')
  if (history) {
    searchHistory.value = history
  }
})
</script>

<style scoped>
.search-page {
  min-height: 100vh;
  background: #f5f5f5;
}

.search-bar {
  display: flex;
  align-items: center;
  padding: 20rpx 30rpx;
  background: white;
  position: sticky;
  top: 0;
  z-index: 100;
}

.search-input-wrapper {
  flex: 1;
  display: flex;
  align-items: center;
  background: #f5f5f5;
  border-radius: 40rpx;
  padding: 0 30rpx;
  height: 70rpx;
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

.cancel-btn {
  font-size: 28rpx;
  color: #667eea;
  margin-left: 20rpx;
}

.history-card,
.hot-card {
  background: white;
  padding: 30rpx;
  margin-top: 20rpx;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.card-title {
  font-size: 30rpx;
  font-weight: bold;
  color: #333;
}

.clear-btn {
  font-size: 24rpx;
  color: #999;
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 15rpx;
}

.tag-item {
  display: flex;
  align-items: center;
  padding: 12rpx 25rpx;
  background: #f5f5f5;
  border-radius: 30rpx;
}

.tag-item.hot {
  background: #fff7e6;
}

.tag-text {
  font-size: 26rpx;
  color: #333;
}

.tag-rank {
  width: 36rpx;
  height: 36rpx;
  border-radius: 50%;
  background: #ddd;
  color: white;
  font-size: 22rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 10rpx;
}

.tag-rank.top {
  background: #f5222d;
}

.tag-hot {
  font-size: 24rpx;
  margin-left: 8rpx;
}

.suggestion-card {
  background: white;
  margin-top: 2rpx;
}

.suggestion-item {
  display: flex;
  align-items: center;
  padding: 25rpx 30rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.suggestion-icon {
  font-size: 28rpx;
  margin-right: 15rpx;
  color: #999;
}

.suggestion-text {
  font-size: 28rpx;
  color: #333;
}

.result-card {
  margin-top: 20rpx;
  background: white;
}

.result-tabs {
  display: flex;
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

.tab-count {
  font-size: 24rpx;
  color: #999;
  margin-left: 8rpx;
}

.tab-item.active .tab-count {
  color: #667eea;
}

.goods-list,
.task-list {
  padding: 0 30rpx;
}

.goods-item {
  display: flex;
  padding: 20rpx 0;
  border-bottom: 1rpx solid #f0f0f0;
}

.goods-image {
  width: 160rpx;
  height: 160rpx;
  border-radius: 10rpx;
  margin-right: 20rpx;
}

.goods-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.goods-name {
  font-size: 28rpx;
  color: #333;
  line-height: 1.5;
}

.goods-desc {
  font-size: 24rpx;
  color: #999;
}

.goods-price {
  font-size: 32rpx;
  color: #f5222d;
  font-weight: bold;
}

.task-item {
  padding: 25rpx 0;
  border-bottom: 1rpx solid #f0f0f0;
}

.task-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10rpx;
}

.task-title {
  font-size: 30rpx;
  font-weight: bold;
  color: #333;
}

.task-reward {
  font-size: 28rpx;
  color: #52c41a;
  font-weight: bold;
}

.task-desc {
  font-size: 26rpx;
  color: #999;
  display: block;
  margin-bottom: 15rpx;
}

.task-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.task-category {
  font-size: 22rpx;
  color: #667eea;
  background: #f0f5ff;
  padding: 4rpx 15rpx;
  border-radius: 20rpx;
}

.task-count {
  font-size: 22rpx;
  color: #999;
}

.empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 100rpx 0;
}

.empty-icon {
  font-size: 80rpx;
  margin-bottom: 20rpx;
}

.empty-text {
  font-size: 28rpx;
  color: #999;
}
</style>
