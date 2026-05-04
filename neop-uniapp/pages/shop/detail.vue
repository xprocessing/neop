<template>
	<view class="container">
		<!-- 商品主图 -->
		<swiper class="banner-swiper" indicator-dots autoplay circular>
			<swiper-item v-for="(img, index) in goodsImages" :key="index">
				<image :src="img" mode="aspectFill" class="banner-image"></image>
			</swiper-item>
		</swiper>

		<!-- 商品信息 -->
		<view class="goods-info">
			<view class="price-row">
				<text class="price">¥{{ goodsInfo.price }}</text>
				<text v-if="goodsInfo.originalPrice" class="original-price">¥{{ goodsInfo.originalPrice }}</text>
				<text class="sales">销量 {{ goodsInfo.salesCount }}</text>
			</view>
			<text class="goods-title">{{ goodsInfo.title }}</text>
			<text class="goods-desc">{{ goodsInfo.description }}</text>
		</view>

		<!-- 规格选择 -->
		<view class="spec-card" @click="showSpecPopup">
			<text class="spec-label">规格</text>
			<view class="spec-value">
				<text v-if="selectedSpec">{{ selectedSpec.specInfo }}</text>
				<text v-else>请选择规格</text>
				<text class="arrow">></text>
			</view>
		</view>

		<!-- 商品详情 -->
		<view class="detail-card">
			<text class="detail-title">商品详情</text>
			<rich-text :nodes="goodsInfo.detail" class="detail-content"></rich-text>
		</view>

		<!-- 底部操作栏 -->
		<view class="bottom-bar">
			<view class="bar-actions">
				<view class="bar-action-item" @click="goHome">
					<text class="action-icon">🏠</text>
					<text>首页</text>
				</view>
				<view class="bar-action-item" @click="goCart">
					<text class="action-icon">🛒</text>
					<text>购物车</text>
					<text v-if="cartCount > 0" class="badge">{{ cartCount }}</text>
				</view>
				<view class="bar-action-item" @click="toggleCollect">
					<text class="action-icon">{{ collected ? '❤️' : '🤍' }}</text>
					<text>{{ collected ? '已收藏' : '收藏' }}</text>
				</view>
			</view>
			<view class="bar-buttons">
				<button class="btn-cart" @click="addToCart">加入购物车</button>
				<button class="btn-buy" @click="buyNow">立即购买</button>
			</view>
		</view>

		<!-- 规格选择弹窗 -->
		<uni-popup ref="specPopup" type="bottom">
			<view class="spec-popup">
				<view class="spec-header">
					<image :src="goodsInfo.mainImage" mode="aspectFill" class="spec-image"></image>
					<view class="spec-info">
						<text class="spec-price">¥{{ selectedSpec ? selectedSpec.price : goodsInfo.price }}</text>
						<text class="spec-stock">库存 {{ selectedSpec ? selectedSpec.stock : goodsInfo.stock }}件</text>
						<text class="spec-selected">已选：{{ selectedSpec ? selectedSpec.specInfo : '请选择规格' }}</text>
					</view>
					<text class="spec-close" @click="closeSpecPopup">✕</text>
				</view>
				<view class="spec-body">
					<view class="spec-group" v-for="(group, gIndex) in specList" :key="gIndex">
						<text class="group-name">{{ group.name }}</text>
						<view class="group-options">
							<text 
								v-for="(option, oIndex) in group.options" 
								:key="oIndex"
								class="option-item"
								:class="{ active: selectedSpecs[gIndex] === oIndex }"
								@click="selectSpec(gIndex, oIndex, option)"
							>
								{{ option }}
							</text>
						</view>
					</view>
				</view>
				<view class="spec-footer">
					<view class="quantity-row">
						<text>购买数量</text>
						<view class="quantity-control">
							<view class="qty-btn" @click="decreaseQuantity">-</view>
							<text class="qty-value">{{ quantity }}</text>
							<view class="qty-btn" @click="increaseQuantity">+</view>
						</view>
					</view>
					<button class="confirm-btn" @click="confirmSpec">确定</button>
				</view>
			</view>
		</uni-popup>
	</view>
</template>

<script>
import request from '@/utils/request'

export default {
	data() {
		return {
			goodsId: '',
			goodsInfo: {},
			goodsImages: [],
			specList: [],
			selectedSpecs: [],
			selectedSpec: null,
			quantity: 1,
			cartCount: 0,
			collected: false
		}
	},
	onLoad(options) {
		this.goodsId = options.id
		this.loadGoodsDetail()
		this.loadCartCount()
	},
	methods: {
		async loadGoodsDetail() {
			try {
				const res = await request({
					url: `/shop/product/info/${this.goodsId}`,
					method: 'GET'
				})
				if (res.code === 200) {
					this.goodsInfo = res.data
					this.goodsImages = res.data.images || [res.data.mainImage]
					this.specList = res.data.specList || []
					this.collected = res.data.collected || false
				}
			} catch (error) {
				uni.showToast({ title: '加载失败', icon: 'none' })
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
		showSpecPopup() {
			this.$refs.specPopup.open()
		},
		closeSpecPopup() {
			this.$refs.specPopup.close()
		},
		selectSpec(groupIndex, optionIndex, option) {
			this.selectedSpecs[groupIndex] = optionIndex
			this.selectedSpecs = [...this.selectedSpecs]
			// 根据选择的规格查找对应的规格信息
			this.findSelectedSpec()
		},
		findSelectedSpec() {
			// 简化逻辑：实际应根据选择的规格组合查找对应的sku
			if (this.goodsInfo.skus && this.goodsInfo.skus.length > 0) {
				// 这里需要根据实际业务逻辑匹配sku
				this.selectedSpec = this.goodsInfo.skus[0]
			}
		},
		increaseQuantity() {
			if (this.quantity < (this.selectedSpec?.stock || this.goodsInfo.stock)) {
				this.quantity++
			} else {
				uni.showToast({ title: '库存不足', icon: 'none' })
			}
		},
		decreaseQuantity() {
			if (this.quantity > 1) {
				this.quantity--
			}
		},
		confirmSpec() {
			if (!this.selectedSpec) {
				uni.showToast({ title: '请选择规格', icon: 'none' })
				return
			}
			this.closeSpecPopup()
		},
		async addToCart() {
			if (!this.selectedSpec && this.specList.length > 0) {
				uni.showToast({ title: '请选择规格', icon: 'none' })
				return
			}
			try {
				const res = await request({
					url: '/shop/cart/add',
					method: 'POST',
					data: {
						goodsId: this.goodsId,
						skuId: this.selectedSpec?.id,
						quantity: this.quantity
					}
				})
				if (res.code === 200) {
					uni.showToast({ title: '已加入购物车', icon: 'success' })
					this.loadCartCount()
				}
			} catch (error) {
				uni.showToast({ title: '添加失败', icon: 'none' })
			}
		},
		buyNow() {
			if (!this.selectedSpec && this.specList.length > 0) {
				uni.showToast({ title: '请选择规格', icon: 'none' })
				return
			}
			// 跳转到确认订单页
			const params = {
				goodsId: this.goodsId,
				skuId: this.selectedSpec?.id,
				quantity: this.quantity
			}
			uni.navigateTo({
				url: `/pages/shop/checkout?${Object.entries(params).map(([k,v]) => `${k}=${v}`).join('&')}`
			})
		},
		async toggleCollect() {
			try {
				const url = this.collected ? '/shop/product/uncollect' : '/shop/product/collect'
				const res = await request({
					url,
					method: 'POST',
					data: { goodsId: this.goodsId }
				})
				if (res.code === 200) {
					this.collected = !this.collected
					uni.showToast({ 
						title: this.collected ? '收藏成功' : '已取消收藏', 
						icon: 'success' 
					})
				}
			} catch (error) {
				uni.showToast({ title: '操作失败', icon: 'none' })
			}
		},
		goHome() {
			uni.switchTab({
				url: '/pages/shop/index'
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
	padding-bottom: 120rpx;
	background: #f5f5f5;
}

.banner-swiper {
	height: 750rpx;
}

.banner-image {
	width: 100%;
	height: 100%;
}

.goods-info {
	background: #fff;
	padding: 30rpx;
}

.price-row {
	display: flex;
	align-items: baseline;
	margin-bottom: 20rpx;
	gap: 20rpx;
}

.price {
	font-size: 48rpx;
	color: #f5222d;
	font-weight: bold;
}

.original-price {
	font-size: 28rpx;
	color: #999;
	text-decoration: line-through;
}

.sales {
	font-size: 24rpx;
	color: #999;
	margin-left: auto;
}

.goods-title {
	font-size: 36rpx;
	font-weight: bold;
	color: #333;
	display: block;
	margin-bottom: 15rpx;
}

.goods-desc {
	font-size: 28rpx;
	color: #666;
	line-height: 1.5;
}

.spec-card {
	background: #fff;
	margin-top: 20rpx;
	padding: 30rpx;
	display: flex;
	justify-content: space-between;
	align-items: center;
}

.spec-label {
	font-size: 28rpx;
	color: #333;
}

.spec-value {
	display: flex;
	align-items: center;
	gap: 10rpx;
	font-size: 28rpx;
	color: #666;
}

.arrow {
	color: #999;
}

.detail-card {
	background: #fff;
	margin-top: 20rpx;
	padding: 30rpx;
}

.detail-title {
	font-size: 32rpx;
	font-weight: bold;
	color: #333;
	display: block;
	margin-bottom: 20rpx;
}

.detail-content {
	font-size: 28rpx;
	color: #666;
	line-height: 1.8;
}

.bottom-bar {
	position: fixed;
	bottom: 0;
	left: 0;
	right: 0;
	background: #fff;
	display: flex;
	align-items: center;
	padding: 20rpx 30rpx;
	box-shadow: 0 -2rpx 10rpx rgba(0,0,0,0.05);
}

.bar-actions {
	display: flex;
	gap: 40rpx;
}

.bar-action-item {
	display: flex;
	flex-direction: column;
	align-items: center;
	font-size: 22rpx;
	color: #666;
	position: relative;
}

.action-icon {
	font-size: 40rpx;
}

.badge {
	position: absolute;
	top: -10rpx;
	right: -10rpx;
	background: #f5222d;
	color: #fff;
	font-size: 18rpx;
	min-width: 28rpx;
	height: 28rpx;
	border-radius: 14rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	padding: 0 6rpx;
}

.bar-buttons {
	flex: 1;
	display: flex;
	gap: 20rpx;
	margin-left: 40rpx;
}

.btn-cart {
	flex: 1;
	background: #ff9500;
	color: #fff;
	font-size: 28rpx;
	padding: 20rpx;
	border-radius: 40rpx;
	border: none;
}

.btn-buy {
	flex: 1;
	background: #f5222d;
	color: #fff;
	font-size: 28rpx;
	padding: 20rpx;
	border-radius: 40rpx;
	border: none;
}

.spec-popup {
	background: #fff;
	border-radius: 30rpx 30rpx 0 0;
	padding: 30rpx;
	max-height: 80vh;
	overflow-y: auto;
}

.spec-header {
	display: flex;
	margin-bottom: 30rpx;
}

.spec-image {
	width: 160rpx;
	height: 160rpx;
	border-radius: 10rpx;
}

.spec-info {
	flex: 1;
	margin-left: 20rpx;
	display: flex;
	flex-direction: column;
	justify-content: space-between;
}

.spec-price {
	font-size: 36rpx;
	color: #f5222d;
	font-weight: bold;
}

.spec-stock {
	font-size: 24rpx;
	color: #999;
}

.spec-selected {
	font-size: 26rpx;
	color: #333;
}

.spec-close {
	font-size: 40rpx;
	color: #999;
}

.spec-body {
	margin-bottom: 30rpx;
}

.spec-group {
	margin-bottom: 30rpx;
}

.group-name {
	font-size: 28rpx;
	color: #333;
	font-weight: bold;
	display: block;
	margin-bottom: 15rpx;
}

.group-options {
	display: flex;
	flex-wrap: wrap;
	gap: 20rpx;
}

.option-item {
	padding: 10rpx 30rpx;
	border: 1rpx solid #e0e0e0;
	border-radius: 30rpx;
	font-size: 26rpx;
	color: #333;
}

.option-item.active {
	background: #007aff;
	color: #fff;
	border-color: #007aff;
}

.spec-footer {
	border-top: 1rpx solid #f0f0f0;
	padding-top: 30rpx;
}

.quantity-row {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 30rpx;
}

.quantity-control {
	display: flex;
	align-items: center;
	border: 1rpx solid #e0e0e0;
	border-radius: 8rpx;
}

.qty-btn {
	width: 60rpx;
	height: 60rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	font-size: 32rpx;
	color: #333;
}

.qty-value {
	width: 80rpx;
	text-align: center;
	font-size: 28rpx;
	border-left: 1rpx solid #e0e0e0;
	border-right: 1rpx solid #e0e0e0;
}

.confirm-btn {
	background: #007aff;
	color: #fff;
	font-size: 32rpx;
	padding: 25rpx;
	border-radius: 50rpx;
	border: none;
}
</style>
