<template>
	<view class="container">
		<!-- 积分概览 -->
		<view class="points-overview">
			<view class="overview-header">
				<text class="overview-title">我的积分</text>
			</view>
			<view class="overview-body">
				<view class="points-number">{{ balance }}</view>
				<view class="points-actions">
					<button class="action-btn" @click="goSign">签到领积分</button>
					<button class="action-btn" @click="goTask">做任务赚积分</button>
				</view>
			</view>
		</view>

		<!-- 积分明细 -->
		<view class="detail-card">
			<view class="detail-header">
				<text class="detail-title">积分明细</text>
				<view class="detail-filter">
					<text 
						class="filter-item" 
						:class="{ active: currentType === '' }"
						@click="switchType('')"
					>全部</text>
					<text 
						class="filter-item" 
						:class="{ active: currentType === 'EARN' }"
						@click="switchType('EARN')"
					>获取</text>
					<text 
						class="filter-item" 
						:class="{ active: currentType === 'CONSUME' }"
						@click="switchType('CONSUME')"
					>消费</text>
				</view>
			</view>

			<!-- 明细列表 -->
			<scroll-view scroll-y class="detail-list" @scrolltolower="loadMore">
				<view v-if="detailList.length === 0" class="empty">
					<text class="empty-icon">💰</text>
					<text class="empty-text">暂无积分明细</text>
				</view>

				<view v-for="item in detailList" :key="item.id" class="detail-item">
					<view class="detail-info">
						<text class="detail-type">{{ item.typeName }}</text>
						<text class="detail-time">{{ formatTime(item.createTime) }}</text>
					</view>
					<view class="detail-amount">
						<text 
							class="amount-text" 
							:class="{ earn: item.amount > 0, consume: item.amount < 0 }"
						>
							{{ item.amount > 0 ? '+' + item.amount : item.amount }}
						</text>
						<text class="balance-text">余额: {{ item.balance }}</text>
					</view>
				</view>

				<uni-load-more :status="loadMoreStatus"></uni-load-more>
			</scroll-view>
		</view>
	</view>
</template>

<script>
import request from '@/utils/request'

export default {
	data() {
		return {
			balance: 0,
			currentType: '',
			detailList: [],
			pageNum: 1,
			pageSize: 20,
			loadMoreStatus: 'more'
		}
	},
	onLoad() {
		this.loadBalance()
		this.loadDetailList()
	},
	onPullDownRefresh() {
		this.pageNum = 1
		this.detailList = []
		this.loadBalance()
		this.loadDetailList()
		setTimeout(() => {
			uni.stopPullDownRefresh()
		}, 1000)
	},
	methods: {
		async loadBalance() {
			try {
				const res = await request({
					url: '/marketing/points/balance',
					method: 'GET'
				})
				if (res.code === 200) {
					this.balance = res.data.balance
				}
			} catch (error) {
				console.error('加载积分余额失败', error)
			}
		},
		async loadDetailList() {
			this.loadMoreStatus = 'loading'
			try {
				const res = await request({
					url: '/marketing/points/detail',
					method: 'GET',
					data: {
						type: this.currentType,
						pageNum: this.pageNum,
						pageSize: this.pageSize
					}
				})
				if (res.code === 200) {
					const list = res.data.records || res.data
					if (this.pageNum === 1) {
						this.detailList = list
					} else {
						this.detailList = [...this.detailList, ...list]
					}
					this.loadMoreStatus = list.length < this.pageSize ? 'noMore' : 'more'
				}
			} catch (error) {
				uni.showToast({ title: '加载失败', icon: 'none' })
				this.loadMoreStatus = 'more'
			}
		},
		switchType(type) {
			this.currentType = type
			this.pageNum = 1
			this.detailList = []
			this.loadDetailList()
		},
		loadMore() {
			if (this.loadMoreStatus === 'more') {
				this.pageNum++
				this.loadDetailList()
			}
		},
		formatTime(time) {
			if (!time) return ''
			return time.substring(0, 16)
		},
		goSign() {
			uni.navigateTo({
				url: '/pages/user/sign'
			})
		},
		goTask() {
			uni.switchTab({
				url: '/pages/task/list'
			})
		}
	}
}
</script>

<style scoped>
.container {
	display: flex;
	flex-direction: column;
	height: 100vh;
	background: #f5f5f5;
}

.points-overview {
	background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
	padding: 40rpx;
	color: #fff;
}

.overview-header {
	margin-bottom: 20rpx;
}

.overview-title {
	font-size: 28rpx;
	opacity: 0.8;
}

.overview-body {
	display: flex;
	justify-content: space-between;
	align-items: center;
}

.points-number {
	font-size: 64rpx;
	font-weight: bold;
}

.points-actions {
	display: flex;
	gap: 20rpx;
}

.action-btn {
	background: rgba(255,255,255,0.3);
	color: #fff;
	font-size: 24rpx;
	padding: 10rpx 20rpx;
	border-radius: 20rpx;
	border: 1rpx solid #fff;
}

.detail-card {
	flex: 1;
	background: #fff;
	border-radius: 20rpx 20rpx 0 0;
	padding: 30rpx;
	margin-top: -20rpx;
}

.detail-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 20rpx;
}

.detail-title {
	font-size: 32rpx;
	font-weight: bold;
	color: #333;
}

.detail-filter {
	display: flex;
	gap: 20rpx;
}

.filter-item {
	font-size: 26rpx;
	color: #999;
	padding: 5rpx 15rpx;
	border-radius: 20rpx;
}

.filter-item.active {
	background: #007aff;
	color: #fff;
}

.detail-list {
	height: calc(100vh - 400rpx);
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

.detail-item {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 25rpx 0;
	border-bottom: 1rpx solid #f0f0f0;
}

.detail-info {
	display: flex;
	flex-direction: column;
}

.detail-type {
	font-size: 28rpx;
	color: #333;
}

.detail-time {
	font-size: 24rpx;
	color: #999;
	margin-top: 10rpx;
}

.detail-amount {
	display: flex;
	flex-direction: column;
	align-items: flex-end;
}

.amount-text {
	font-size: 32rpx;
	font-weight: bold;
}

.amount-text.earn {
	color: #52c41a;
}

.amount-text.consume {
	color: #f5222d;
}

.balance-text {
	font-size: 22rpx;
	color: #999;
	margin-top: 10rpx;
}
</style>
