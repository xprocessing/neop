<template>
	<view class="container">
		<!-- 标签切换 -->
		<view class="tabs">
			<view 
				v-for="(tab, index) in tabs" 
				:key="index"
				class="tab-item"
				:class="{ active: currentTab === index }"
				@click="switchTab(index)"
			>
				<text>{{ tab.name }}</text>
			</view>
		</view>

		<!-- 订单列表 -->
		<scroll-view scroll-y class="order-list" @scrolltolower="loadMore">
			<view v-if="orderList.length === 0" class="empty">
				<text class="empty-icon">📦</text>
				<text class="empty-text">暂无订单</text>
				<button class="empty-btn" @click="goShop">去逛逛</button>
			</view>

			<view v-for="item in orderList" :key="item.id" class="order-card">
				<view class="order-header">
					<text class="order-no">订单号：{{ item.orderNo }}</text>
					<text class="order-status" :class="'status-' + item.status">
						{{ getStatusText(item.status) }}
					</text>
				</view>

				<view class="order-goods">
					<image :src="item.mainImage" mode="aspectFill" class="goods-image"></image>
					<view class="goods-info">
						<text class="goods-title">{{ item.title }}</text>
						<text v-if="item.specInfo" class="goods-spec">{{ item.specInfo }}</text>
						<view class="goods-price-row">
							<text class="goods-price">¥{{ item.unitPrice }}</text>
							<text class="goods-quantity">x{{ item.quantity }}</text>
						</view>
					</view>
				</view>

				<view class="order-footer">
					<text class="order-total">共{{ item.quantity }}件 合计：¥{{ item.totalAmount }}</text>
					<view class="order-actions">
						<button 
							v-if="item.status === 1" 
							class="btn-primary"
							@click="payOrder(item)"
						>
							去支付
						</button>
						<button 
							v-if="item.status === 3" 
							class="btn-primary"
							@click="confirmReceive(item)"
						>
							确认收货
						</button>
						<button 
							v-if="item.status === 4" 
							class="btn-default"
							@click="viewLogistics(item)"
						>
							查看物流
						</button>
					</view>
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
			tabs: [
				{ name: '全部', status: undefined },
				{ name: '待支付', status: 1 },
				{ name: '待发货', status: 2 },
				{ name: '待收货', status: 3 },
				{ name: '已完成', status: 4 }
			],
			currentTab: 0,
			orderList: [],
			pageNum: 1,
			pageSize: 10,
			loadMoreStatus: 'more'
		}
	},
	onLoad() {
		this.loadOrderList()
	},
	onPullDownRefresh() {
		this.pageNum = 1
		this.orderList = []
		this.loadOrderList()
		setTimeout(() => {
			uni.stopPullDownRefresh()
		}, 1000)
	},
	methods: {
		switchTab(index) {
			this.currentTab = index
			this.pageNum = 1
			this.orderList = []
			this.loadOrderList()
		},
		async loadOrderList() {
			this.loadMoreStatus = 'loading'
			try {
				const res = await request({
					url: '/shop/order/list',
					method: 'GET',
					data: {
						status: this.tabs[this.currentTab].status,
						pageNum: this.pageNum,
						pageSize: this.pageSize
					}
				})
				if (res.code === 200) {
					const list = res.data.list || res.data
					if (this.pageNum === 1) {
						this.orderList = list
					} else {
						this.orderList = [...this.orderList, ...list]
					}
					this.loadMoreStatus = list.length < this.pageSize ? 'noMore' : 'more'
				}
			} catch (error) {
				uni.showToast({ title: '加载失败', icon: 'none' })
				this.loadMoreStatus = 'more'
			}
		},
		loadMore() {
			if (this.loadMoreStatus === 'more') {
				this.pageNum++
				this.loadOrderList()
			}
		},
		getStatusText(status) {
			const map = {
				1: '待支付',
				2: '待发货',
				3: '待收货',
				4: '已完成',
				5: '已取消',
				9: '已退款'
			}
			return map[status] || '未知'
		},
		async payOrder(order) {
			try {
				const res = await request({
					url: '/shop/order/pay',
					method: 'POST',
					data: {
						orderNo: order.orderNo
					}
				})
				if (res.code === 200) {
					// 调起微信支付
					uni.requestPayment({
						provider: 'wxpay',
						...res.data,
						success: () => {
							uni.showToast({ title: '支付成功', icon: 'success' })
							this.pageNum = 1
							this.orderList = []
							this.loadOrderList()
						},
						fail: () => {
							uni.showToast({ title: '支付失败', icon: 'none' })
						}
					})
				}
			} catch (error) {
				uni.showToast({ title: '支付失败', icon: 'none' })
			}
		},
		async confirmReceive(order) {
			uni.showModal({
				title: '提示',
				content: '确认已收到商品？',
				success: async (res) => {
					if (res.confirm) {
						try {
							const res = await request({
								url: '/shop/order/confirm',
								method: 'PUT',
								data: {
									orderNo: order.orderNo
								}
							})
							if (res.code === 200) {
								uni.showToast({ title: '确认成功', icon: 'success' })
								this.pageNum = 1
								this.orderList = []
								this.loadOrderList()
							}
						} catch (error) {
							uni.showToast({ title: '操作失败', icon: 'none' })
						}
					}
				}
			})
		},
		viewLogistics(order) {
			uni.navigateTo({
				url: `/pages/shop/logistics?orderNo=${order.orderNo}`
			})
		},
		goShop() {
			uni.switchTab({
				url: '/pages/shop/index'
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
	box-shadow: 0 2rpx 10rpx rgba(0,0,0,0.05);
}

.tab-item {
	flex: 1;
	text-align: center;
	padding: 25rpx 0;
	font-size: 28rpx;
	color: #666;
	position: relative;
}

.tab-item.active {
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

.order-list {
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
	padding: 15rpx 60rpx;
	border-radius: 30rpx;
}

.order-card {
	background: #fff;
	border-radius: 16rpx;
	padding: 30rpx;
	margin-bottom: 20rpx;
}

.order-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 20rpx;
	padding-bottom: 20rpx;
	border-bottom: 1rpx solid #f0f0f0;
}

.order-no {
	font-size: 26rpx;
	color: #999;
}

.order-status {
	font-size: 28rpx;
	font-weight: bold;
}

.status-1 {
	color: #fa8c16;
}

.status-2 {
	color: #1890ff;
}

.status-3 {
	color: #52c41a;
}

.status-4 {
	color: #999;
}

.order-goods {
	display: flex;
	margin-bottom: 20rpx;
}

.goods-image {
	width: 160rpx;
	height: 160rpx;
	border-radius: 8rpx;
}

.goods-info {
	flex: 1;
	margin-left: 20rpx;
}

.goods-title {
	font-size: 28rpx;
	color: #333;
	font-weight: bold;
	display: -webkit-box;
	-webkit-box-orient: vertical;
	-webkit-line-clamp: 2;
	overflow: hidden;
}

.goods-spec {
	font-size: 24rpx;
	color: #999;
	margin-top: 10rpx;
}

.goods-price-row {
	display: flex;
	justify-content: space-between;
	margin-top: 20rpx;
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

.order-footer {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding-top: 20rpx;
	border-top: 1rpx solid #f0f0f0;
}

.order-total {
	font-size: 28rpx;
	color: #333;
}

.order-actions {
	display: flex;
	gap: 20rpx;
}

.btn-primary {
	background: #007aff;
	color: #fff;
	font-size: 26rpx;
	padding: 8rpx 30rpx;
	border-radius: 30rpx;
	border: none;
}

.btn-default {
	background: #fff;
	color: #666;
	font-size: 26rpx;
	padding: 8rpx 30rpx;
	border-radius: 30rpx;
	border: 1rpx solid #d9d9d9;
}
</style>
