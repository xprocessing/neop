<template>
	<view class="container">
		<!-- 搜索栏 -->
		<view class="search-bar">
			<view class="search-input-wrap">
				<text class="search-icon">🔍</text>
				<input 
					class="search-input" 
					placeholder="搜索商品" 
					v-model="keyword"
					@confirm="search"
				/>
			</view>
		</view>

		<!-- 分类导航 -->
		<scroll-view scroll-x class="category-nav">
			<view 
				v-for="item in categoryList" 
				:key="item.id"
				class="category-item"
				:class="{ active: currentCategory === item.id }"
				@click="switchCategory(item.id)"
			>
				<text>{{ item.name }}</text>
			</view>
		</scroll-view>

		<!-- 商品列表 -->
		<scroll-view scroll-y class="goods-list" @scrolltolower="loadMore">
			<view v-if="goodsList.length === 0" class="empty">
				<text class="empty-icon">🛍️</text>
				<text class="empty-text">暂无商品</text>
			</view>

			<view class="goods-grid">
				<view 
					v-for="item in goodsList" 
					:key="item.id" 
					class="goods-card"
					@click="goDetail(item.id)"
				>
					<image :src="item.mainImage" mode="aspectFill" class="goods-image"></image>
					<view class="goods-info">
						<text class="goods-title">{{ item.title }}</text>
						<text class="goods-desc">{{ item.description }}</text>
						<view class="goods-price-row">
							<text class="goods-price">¥{{ item.price }}</text>
							<text v-if="item.originalPrice" class="goods-original-price">¥{{ item.originalPrice }}</text>
						</view>
						<view class="goods-sales">
							<text>销量 {{ item.salesCount }}</text>
						</view>
					</view>
				</view>
			</view>

			<uni-load-more :status="loadMoreStatus"></uni-load-more>
		</scroll-view>

		<!-- 悬浮购物车按钮 -->
		<view class="float-cart" @click="goCart">
			<text class="cart-icon">🛒</text>
			<text v-if="cartCount > 0" class="cart-badge">{{ cartCount }}</text>
		</view>
	</view>
</template>

<script>
import request from '@/utils/request'

export default {
	data() {
		return {
			keyword: '',
			currentCategory: 0,
			categoryList: [
				{ id: 0, name: '全部' }
			],
			goodsList: [],
			pageNum: 1,
			pageSize: 10,
			loadMoreStatus: 'more',
			cartCount: 0
		}
	},
	onLoad() {
		this.loadCategory()
		this.loadGoodsList()
		this.loadCartCount()
	},
	onShow() {
		this.loadCartCount()
	},
	onPullDownRefresh() {
		this.pageNum = 1
		this.goodsList = []
		this.loadGoodsList()
		setTimeout(() => {
			uni.stopPullDownRefresh()
		}, 1000)
	},
	methods: {
		async loadCategory() {
			try {
				const res = await request({
					url: '/shop/category/list',
					method: 'GET'
				})
				if (res.code === 200) {
					this.categoryList = [{ id: 0, name: '全部' }, ...res.data]
				}
			} catch (error) {
				console.error('加载分类失败', error)
			}
		},
		async loadGoodsList() {
			this.loadMoreStatus = 'loading'
			try {
				const res = await request({
					url: '/shop/product/list',
					method: 'GET',
					data: {
						keyword: this.keyword,
						categoryId: this.currentCategory === 0 ? undefined : this.currentCategory,
						pageNum: this.pageNum,
						pageSize: this.pageSize
					}
				})
				
				if (res.code === 200) {
					const list = res.data.records || res.data
					if (this.pageNum === 1) {
						this.goodsList = list
					} else {
						this.goodsList = [...this.goodsList, ...list]
					}
					this.loadMoreStatus = list.length < this.pageSize ? 'noMore' : 'more'
				}
			} catch (error) {
				uni.showToast({ title: '加载失败', icon: 'none' })
				this.loadMoreStatus = 'more'
			}
		},
		async loadCartCount() {
			try {
				const res = await request({
					url: '/shop/cart/count',
					method: 'GET'
				})
				if (res.code === 200) {
					this.cartCount = res.data || 0
				}
			} catch (error) {
				console.error('加载购物车数量失败', error)
			}
		},
		search() {
			this.pageNum = 1
			this.goodsList = []
			this.loadGoodsList()
		},
		switchCategory(categoryId) {
			this.currentCategory = categoryId
			this.pageNum = 1
			this.goodsList = []
			this.loadGoodsList()
		},
		loadMore() {
			if (this.loadMoreStatus === 'more') {
				this.pageNum++
				this.loadGoodsList()
			}
		},
		goDetail(goodsId) {
			uni.navigateTo({
				url: `/pages/shop/detail?id=${goodsId}`
			})
		},
		goCart() {
			uni.navigateTo({
				url: '/pages/shop/cart'
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

.search-bar {
	padding: 20rpx;
	background: #fff;
}

.search-input-wrap {
	display: flex;
	align-items: center;
	background: #f5f5f5;
	border-radius: 30rpx;
	padding: 15rpx 20rpx;
}

.search-icon {
	font-size: 28rpx;
	margin-right: 10rpx;
}

.search-input {
	flex: 1;
	font-size: 28rpx;
}

.category-nav {
	white-space: nowrap;
	background: #fff;
	padding: 20rpx 0;
	box-shadow: 0 2rpx 10rpx rgba(0,0,0,0.05);
}

.category-item {
	display: inline-block;
	padding: 10rpx 30rpx;
	margin: 0 10rpx;
	font-size: 28rpx;
	color: #666;
	border-radius: 30rpx;
	background: #f5f5f5;
}

.category-item.active {
	background: #007aff;
	color: #fff;
}

.goods-list {
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
}

.goods-grid {
	display: flex;
	flex-wrap: wrap;
	gap: 20rpx;
}

.goods-card {
	width: calc(50% - 10rpx);
	background: #fff;
	border-radius: 16rpx;
	overflow: hidden;
	box-shadow: 0 2rpx 10rpx rgba(0,0,0,0.05);
}

.goods-image {
	width: 100%;
	height: 350rpx;
}

.goods-info {
	padding: 20rpx;
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

.goods-desc {
	font-size: 24rpx;
	color: #999;
	margin-top: 10rpx;
	display: block;
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
}

.goods-price-row {
	display: flex;
	align-items: baseline;
	margin-top: 15rpx;
	gap: 10rpx;
}

.goods-price {
	font-size: 32rpx;
	color: #f5222d;
	font-weight: bold;
}

.goods-original-price {
	font-size: 24rpx;
	color: #999;
	text-decoration: line-through;
}

.goods-sales {
	margin-top: 10rpx;
	font-size: 24rpx;
	color: #999;
}

.float-cart {
	position: fixed;
	right: 40rpx;
	bottom: 100rpx;
	width: 100rpx;
	height: 100rpx;
	background: #007aff;
	border-radius: 50%;
	display: flex;
	align-items: center;
	justify-content: center;
	box-shadow: 0 4rpx 20rpx rgba(0,122,255,0.3);
	z-index: 100;
}

.cart-icon {
	font-size: 40rpx;
}

.cart-badge {
	position: absolute;
	top: -10rpx;
	right: -10rpx;
	background: #f5222d;
	color: #fff;
	font-size: 20rpx;
	min-width: 32rpx;
	height: 32rpx;
	border-radius: 16rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	padding: 0 8rpx;
}
</style>
