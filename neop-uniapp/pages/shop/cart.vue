<template>
	<view class="container">
		<!-- 购物车列表 -->
		<view v-if="cartList.length > 0" class="cart-list">
			<view class="cart-header">
				<text class="cart-title">购物车</text>
				<text class="cart-edit" @click="toggleEdit">{{ isEditing ? '完成' : '编辑' }}</text>
			</view>

			<view v-for="item in cartList" :key="item.id" class="cart-item">
				<view class="checkbox" @click="toggleSelect(item)">
					<text class="checkbox-icon">{{ item.selected ? '✅' : '⬜' }}</text>
				</view>
				<image :src="item.mainImage" mode="aspectFill" class="goods-image"></image>
				<view class="goods-info">
					<text class="goods-title">{{ item.title }}</text>
					<view class="goods-spec" v-if="item.specInfo">
						<text>{{ item.specInfo }}</text>
					</view>
					<view class="goods-bottom">
						<text class="goods-price">¥{{ item.price }}</text>
						<view class="quantity-control">
							<view class="qty-btn" @click="updateQuantity(item, -1)">-</view>
							<text class="qty-value">{{ item.quantity }}</text>
							<view class="qty-btn" @click="updateQuantity(item, 1)">+</view>
						</view>
					</view>
				</view>
			</view>
		</view>

		<!-- 空购物车 -->
		<view v-else class="empty">
			<text class="empty-icon">🛒</text>
			<text class="empty-text">购物车是空的</text>
			<button class="empty-btn" @click="goShop">去逛逛</button>
		</view>

		<!-- 底部结算栏 -->
		<view v-if="cartList.length > 0" class="bottom-bar">
			<view class="select-all" @click="toggleSelectAll">
				<text class="checkbox-icon">{{ allSelected ? '✅' : '⬜' }}</text>
				<text class="select-all-text">全选</text>
			</view>
			<view class="total-info">
				<text v-if="!isEditing" class="total-label">合计：</text>
				<text v-if="!isEditing" class="total-amount">¥{{ totalAmount }}</text>
			</view>
			<button 
				v-if="!isEditing" 
				class="checkout-btn" 
				:disabled="selectedCount === 0"
				@click="checkout"
			>
				结算({{ selectedCount }})
			</button>
			<button 
				v-else 
				class="delete-btn" 
				:disabled="selectedCount === 0"
				@click="deleteSelected"
			>
				删除
			</button>
		</view>
	</view>
</template>

<script>
import request from '@/utils/request'

export default {
	data() {
		return {
			cartList: [],
			isEditing: false,
			allSelected: false
		}
	},
	computed: {
		selectedCount() {
			return this.cartList.filter(item => item.selected).length
		},
		totalAmount() {
			let total = 0
			this.cartList.forEach(item => {
				if (item.selected) {
					total += item.price * item.quantity
				}
			})
			return total.toFixed(2)
		}
	},
	onShow() {
		this.loadCartList()
	},
	methods: {
		async loadCartList() {
			try {
				const res = await request({
					url: '/shop/cart/list',
					method: 'GET'
				})
				if (res.code === 200) {
					this.cartList = (res.data || []).map(item => ({
						...item,
						selected: false
					}))
				}
			} catch (error) {
				uni.showToast({ title: '加载失败', icon: 'none' })
			}
		},
		toggleSelect(item) {
			item.selected = !item.selected
			this.updateAllSelected()
		},
		toggleSelectAll() {
			this.allSelected = !this.allSelected
			this.cartList.forEach(item => {
				item.selected = this.allSelected
			})
		},
		updateAllSelected() {
			this.allSelected = this.cartList.length > 0 && this.cartList.every(item => item.selected)
		},
		async updateQuantity(item, delta) {
			const newQty = item.quantity + delta
			if (newQty < 1) {
				uni.showToast({ title: '最少购买1件', icon: 'none' })
				return
			}
			if (newQty > item.stock) {
				uni.showToast({ title: '库存不足', icon: 'none' })
				return
			}
			try {
				const res = await request({
					url: '/shop/cart/update',
					method: 'PUT',
					data: {
						id: item.id,
						quantity: newQty
					}
				})
				if (res.code === 200) {
					item.quantity = newQty
				}
			} catch (error) {
				uni.showToast({ title: '更新失败', icon: 'none' })
			}
		},
		toggleEdit() {
			this.isEditing = !this.isEditing
		},
		async deleteSelected() {
			const selectedIds = this.cartList.filter(item => item.selected).map(item => item.id)
			if (selectedIds.length === 0) {
				uni.showToast({ title: '请选择要删除的商品', icon: 'none' })
				return
			}
			uni.showModal({
				title: '提示',
				content: `确定要删除选中的${selectedIds.length}件商品吗？`,
				success: async (res) => {
					if (res.confirm) {
						try {
							const res = await request({
								url: '/shop/cart/delete',
								method: 'POST',
								data: { ids: selectedIds }
							})
							if (res.code === 200) {
								uni.showToast({ title: '删除成功', icon: 'success' })
								this.loadCartList()
							}
						} catch (error) {
							uni.showToast({ title: '删除失败', icon: 'none' })
						}
					}
				}
			})
		},
		checkout() {
			const selectedItems = this.cartList.filter(item => item.selected)
			if (selectedItems.length === 0) {
				uni.showToast({ title: '请选择商品', icon: 'none' })
				return
			}
			// 跳转到确认订单页
			const ids = selectedItems.map(item => item.id).join(',')
			uni.navigateTo({
				url: `/pages/shop/checkout?cartIds=${ids}`
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
	padding-bottom: 120rpx;
	min-height: 100vh;
	background: #f5f5f5;
}

.cart-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 30rpx;
	background: #fff;
}

.cart-title {
	font-size: 36rpx;
	font-weight: bold;
	color: #333;
}

.cart-edit {
	font-size: 28rpx;
	color: #007aff;
}

.cart-list {
	padding: 20rpx;
}

.cart-item {
	display: flex;
	align-items: center;
	background: #fff;
	border-radius: 16rpx;
	padding: 20rpx;
	margin-bottom: 20rpx;
}

.checkbox {
	padding: 20rpx;
}

.checkbox-icon {
	font-size: 40rpx;
}

.goods-image {
	width: 180rpx;
	height: 180rpx;
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

.goods-bottom {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-top: 20rpx;
}

.goods-price {
	font-size: 32rpx;
	color: #f5222d;
	font-weight: bold;
}

.quantity-control {
	display: flex;
	align-items: center;
	border: 1rpx solid #e0e0e0;
	border-radius: 8rpx;
}

.qty-btn {
	width: 50rpx;
	height: 50rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	font-size: 28rpx;
	color: #333;
}

.qty-value {
	width: 60rpx;
	text-align: center;
	font-size: 28rpx;
	border-left: 1rpx solid #e0e0e0;
	border-right: 1rpx solid #e0e0e0;
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

.bottom-bar {
	position: fixed;
	bottom: 0;
	left: 0;
	right: 0;
	height: 120rpx;
	background: #fff;
	display: flex;
	align-items: center;
	padding: 0 30rpx;
	box-shadow: 0 -2rpx 10rpx rgba(0,0,0,0.05);
}

.select-all {
	display: flex;
	align-items: center;
}

.select-all-text {
	font-size: 28rpx;
	color: #333;
	margin-left: 10rpx;
}

.total-info {
	flex: 1;
	text-align: right;
	margin-right: 20rpx;
}

.total-label {
	font-size: 28rpx;
	color: #333;
}

.total-amount {
	font-size: 36rpx;
	color: #f5222d;
	font-weight: bold;
}

.checkout-btn {
	background: #007aff;
	color: #fff;
	font-size: 28rpx;
	padding: 15rpx 40rpx;
	border-radius: 30rpx;
	border: none;
}

.checkout-btn[disabled] {
	background: #d9d9d9;
}

.delete-btn {
	background: #f5222d;
	color: #fff;
	font-size: 28rpx;
	padding: 15rpx 40rpx;
	border-radius: 30rpx;
	border: none;
}

.delete-btn[disabled] {
	background: #d9d9d9;
}
</style>
