<template>
	<view class="container">
		<!-- 标签切换 -->
		<view class="tabs">
			<view class="tab-item" :class="{ active: currentTab === 0 }" @click="switchTab(0)">
				<text>进行中</text>
				<text class="count">{{ inProgressCount }}</text>
			</view>
			<view class="tab-item" :class="{ active: currentTab === 1 }" @click="switchTab(1)">
				<text>待审核</text>
				<text class="count">{{ pendingCount }}</text>
			</view>
			<view class="tab-item" :class="{ active: currentTab === 2 }" @click="switchTab(2)">
				<text>已完成</text>
				<text class="count">{{ completedCount }}</text>
			</view>
			<view class="tab-item" :class="{ active: currentTab === 3 }" @click="switchTab(3)">
				<text>已结算</text>
				<text class="count">{{ settledCount }}</text>
			</view>
		</view>

		<!-- 任务列表 -->
		<scroll-view scroll-y class="task-list" @scrolltolower="loadMore">
			<view v-if="taskList.length === 0" class="empty">
				<text class="empty-icon">📋</text>
				<text class="empty-text">暂无任务</text>
				<button class="empty-btn" @click="goTaskList">去接单</button>
			</view>

			<view v-for="item in taskList" :key="item.id" class="task-card" @click="goDetail(item.taskId)">
				<view class="task-header">
					<text class="task-title">{{ item.taskTitle }}</text>
					<text class="task-status" :class="'status-' + item.status">
						{{ getStatusText(item.status) }}
					</text>
				</view>

				<view class="task-info">
					<view class="info-row">
						<text class="label">单价：</text>
						<text class="value price">¥{{ item.unitPrice }}</text>
					</view>
					<view class="info-row">
						<text class="label">完成数：</text>
						<text class="value">{{ item.completedCount }}/{{ item.requiredCount }}</text>
					</view>
					<view class="info-row">
						<text class="label">领取时间：</text>
						<text class="value">{{ formatTime(item.receiveTime) }}</text>
					</view>
					<view v-if="item.settleTime" class="info-row">
						<text class="label">结算时间：</text>
						<text class="value">{{ formatTime(item.settleTime) }}</text>
					</view>
				</view>

				<!-- 进度条 -->
				<view class="progress-bar">
					<view class="progress-fill" :style="{ width: getProgress(item) + '%' }"></view>
				</view>
				<text class="progress-text">进度 {{ item.completedCount }}/{{ item.requiredCount }}</text>

				<!-- 操作按钮 -->
				<view class="task-actions">
					<button v-if="item.status === 1" class="btn-primary" @click.stop="submitWork(item)">
						提交工单
					</button>
					<button v-if="item.status === 3" class="btn-success">
						审核通过
					</button>
					<button v-if="item.status === 4" class="btn-warning">
						已结算 ¥{{ item.settleAmount }}
					</button>
				</view>
			</view>

			<uni-load-more :status="loadMoreStatus"></uni-load-more>
		</scroll-view>
	</view>
</template>

<script>
import request from '@/utils/request'

export default {
	data() {
		return {
			currentTab: 0,
			taskList: [],
			pageNum: 1,
			pageSize: 10,
			loadMoreStatus: 'more',
			inProgressCount: 0,
			pendingCount: 0,
			completedCount: 0,
			settledCount: 0
		}
	},
	onLoad() {
		this.loadTaskList()
		this.loadCounts()
	},
	onPullDownRefresh() {
		this.pageNum = 1
		this.taskList = []
		this.loadTaskList()
		this.loadCounts()
		setTimeout(() => {
			uni.stopPullDownRefresh()
		}, 1000)
	},
	methods: {
		switchTab(tab) {
			this.currentTab = tab
			this.pageNum = 1
			this.taskList = []
			this.loadTaskList()
		},
		async loadTaskList() {
			this.loadMoreStatus = 'loading'
			try {
				const statusMap = {
					0: undefined,  // 全部（进行中+待审核）
					1: 1,
					2: 3,
					3: 4
				}
				
				const res = await request({
					url: '/task/receive/my',
					method: 'GET',
					data: {
						status: statusMap[this.currentTab],
						pageNum: this.pageNum,
						pageSize: this.pageSize
					}
				})
				
				if (res.code === 200) {
					const list = res.data.list || res.data
					if (this.pageNum === 1) {
						this.taskList = list
					} else {
						this.taskList = [...this.taskList, ...list]
					}
					this.loadMoreStatus = list.length < this.pageSize ? 'noMore' : 'more'
				}
			} catch (error) {
				uni.showToast({ title: '加载失败', icon: 'none' })
				this.loadMoreStatus = 'more'
			}
		},
		async loadCounts() {
			try {
				const res = await request({
					url: '/task/receive/count',
					method: 'GET'
				})
				if (res.code === 200) {
					this.inProgressCount = res.data.inProgress || 0
					this.pendingCount = res.data.pending || 0
					this.completedCount = res.data.completed || 0
					this.settledCount = res.data.settled || 0
				}
			} catch (error) {
				console.error('加载计数失败', error)
			}
		},
		loadMore() {
			if (this.loadMoreStatus === 'more') {
				this.pageNum++
				this.loadTaskList()
			}
		},
		getStatusText(status) {
			const map = {
				1: '进行中',
				2: '审核失败',
				3: '审核通过',
				4: '已结算'
			}
			return map[status] || '未知'
		},
		getProgress(item) {
			if (item.requiredCount === 0) return 0
			return Math.min(100, (item.completedCount / item.requiredCount) * 100)
		},
		formatTime(time) {
			if (!time) return ''
			return time.substring(0, 16)
		},
		goDetail(taskId) {
			uni.navigateTo({
				url: `/pages/task/detail?id=${taskId}`
			})
		},
		goTaskList() {
			uni.switchTab({
				url: '/pages/task/list'
			})
		},
		submitWork(item) {
			uni.navigateTo({
				url: `/pages/task/submit?receiveId=${item.id}&taskId=${item.taskId}`
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

.tabs {
	display: flex;
	background: #fff;
	padding: 20rpx 0;
	box-shadow: 0 2rpx 10rpx rgba(0,0,0,0.05);
}

.tab-item {
	flex: 1;
	display: flex;
	flex-direction: column;
	align-items: center;
	padding: 10rpx 0;
	position: relative;
}

.tab-item text:first-child {
	font-size: 28rpx;
	color: #666;
}

.tab-item.active text:first-child {
	color: #007aff;
	font-weight: bold;
}

.tab-item.active::after {
	content: '';
	position: absolute;
	bottom: 0;
	left: 50%;
	transform: translateX(-50%);
	width: 60rpx;
	height: 4rpx;
	background: #007aff;
	border-radius: 2rpx;
}

.count {
	font-size: 24rpx;
	color: #999;
	margin-top: 4rpx;
}

.tab-item.active .count {
	color: #007aff;
}

.task-list {
	flex: 1;
	padding: 20rpx;
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
	margin-bottom: 30rpx;
}

.empty-btn {
	background: #007aff;
	color: #fff;
	font-size: 28rpx;
	padding: 10rpx 40rpx;
	border-radius: 30rpx;
}

.task-card {
	background: #fff;
	border-radius: 16rpx;
	padding: 30rpx;
	margin-bottom: 20rpx;
	box-shadow: 0 2rpx 10rpx rgba(0,0,0,0.05);
}

.task-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 20rpx;
}

.task-title {
	font-size: 32rpx;
	font-weight: bold;
	color: #333;
	flex: 1;
	margin-right: 20rpx;
}

.task-status {
	font-size: 24rpx;
	padding: 4rpx 16rpx;
	border-radius: 20rpx;
}

.status-1 {
	background: #e6f7ff;
	color: #1890ff;
}

.status-2 {
	background: #fff2e8;
	color: #fa8c16;
}

.status-3 {
	background: #f6ffed;
	color: #52c41a;
}

.status-4 {
	background: #f9f0ff;
	color: #722ed1;
}

.task-info {
	margin-bottom: 20rpx;
}

.info-row {
	display: flex;
	margin-bottom: 10rpx;
	font-size: 26rpx;
}

.label {
	color: #999;
	width: 160rpx;
}

.value {
	color: #333;
}

.value.price {
	color: #f5222d;
	font-weight: bold;
}

.progress-bar {
	height: 12rpx;
	background: #f0f0f0;
	border-radius: 6rpx;
	overflow: hidden;
	margin-bottom: 10rpx;
}

.progress-fill {
	height: 100%;
	background: linear-gradient(90deg, #1890ff, #36cfc9);
	border-radius: 6rpx;
	transition: width 0.3s;
}

.progress-text {
	font-size: 24rpx;
	color: #999;
}

.task-actions {
	margin-top: 20rpx;
	display: flex;
	justify-content: flex-end;
}

.btn-primary {
	background: #007aff;
	color: #fff;
	font-size: 26rpx;
	padding: 8rpx 30rpx;
	border-radius: 30rpx;
	border: none;
}

.btn-success {
	background: #f6ffed;
	color: #52c41a;
	font-size: 26rpx;
	padding: 8rpx 30rpx;
	border-radius: 30rpx;
	border: 1rpx solid #b7eb8f;
}

.btn-warning {
	background: #f9f0ff;
	color: #722ed1;
	font-size: 26rpx;
	padding: 8rpx 30rpx;
	border-radius: 30rpx;
	border: 1rpx solid #d3adf7;
}
</style>
