<template>
	<view class="container">
		<view class="address-list">
			<view v-if="addressList.length === 0" class="empty">
				<text class="empty-icon">📍</text>
				<text class="empty-text">暂无收货地址</text>
			</view>

			<view 
				v-for="item in addressList" 
				:key="item.id" 
				class="address-card"
				@click="selectAddress(item)"
			>
				<view class="address-info">
					<view class="address-header">
						<text class="receiver-name">{{ item.receiverName }}</text>
						<text class="receiver-phone">{{ item.receiverPhone }}</text>
						<text v-if="item.isDefault" class="default-tag">默认</text>
					</view>
					<text class="address-detail">
						{{ item.province }}{{ item.city }}{{ item.district }}{{ item.detailAddress }}
					</text>
				</view>
				<view class="address-actions">
					<view class="action-item" @click.stop="editAddress(item)">
						<text class="action-icon">✏️</text>
						<text>编辑</text>
					</view>
					<view class="action-item" @click.stop="deleteAddress(item)">
						<text class="action-icon">🗑️</text>
						<text>删除</text>
					</view>
				</view>
			</view>
		</view>

		<!-- 新增地址按钮 -->
		<view class="add-btn-wrap">
			<button class="add-btn" @click="addAddress">+ 新增收货地址</button>
		</view>

		<!-- 地址编辑弹窗 -->
		<uni-popup ref="addressPopup" type="bottom">
			<view class="address-form">
				<view class="form-header">
					<text class="form-title">{{ editingAddress.id ? '编辑地址' : '新增地址' }}</text>
					<text class="form-close" @click="closeForm">✕</text>
				</view>
				<view class="form-body">
					<view class="form-item">
						<text class="form-label">收货人 <text class="required">*</text></text>
						<input 
							class="form-input" 
							v-model="editingAddress.receiverName" 
							placeholder="请输入收货人姓名"
						/>
					</view>
					<view class="form-item">
						<text class="form-label">手机号 <text class="required">*</text></text>
						<input 
							class="form-input" 
							v-model="editingAddress.receiverPhone" 
							placeholder="请输入手机号"
							type="number"
							maxlength="11"
						/>
					</view>
					<view class="form-item">
						<text class="form-label">所在地区 <text class="required">*</text></text>
						<picker 
							mode="region" 
							:value="[editingAddress.province, editingAddress.city, editingAddress.district]"
							@change="onRegionChange"
						>
							<view class="form-input picker-input">
								<text v-if="editingAddress.province">{{ editingAddress.province }} {{ editingAddress.city }} {{ editingAddress.district }}</text>
								<text v-else class="placeholder">请选择所在地区</text>
							</view>
						</picker>
					</view>
					<view class="form-item">
						<text class="form-label">详细地址 <text class="required">*</text></text>
						<textarea 
							class="form-textarea" 
							v-model="editingAddress.detailAddress" 
							placeholder="请输入详细地址（如：街道、门牌号等）"
							maxlength="100"
						/>
					</view>
					<view class="form-item">
						<view class="switch-row">
							<text class="form-label">设为默认地址</text>
							<switch 
								:checked="editingAddress.isDefault" 
								@change="editingAddress.isDefault = !editingAddress.isDefault"
								color="#007aff"
							/>
						</view>
					</view>
				</view>
				<button class="save-btn" @click="saveAddress">保存</button>
			</view>
		</uni-popup>
	</view>
</template>

<script>
import request from '@/utils/request'

export default {
	data() {
		return {
			addressList: [],
			editingAddress: {
				id: null,
				receiverName: '',
				receiverPhone: '',
				province: '',
				city: '',
				district: '',
				detailAddress: '',
				isDefault: false
			},
			isSelectMode: false
		}
	},
	onLoad(options) {
		if (options.select === 'true') {
			this.isSelectMode = true
		}
	},
	onShow() {
		this.loadAddressList()
	},
	methods: {
		async loadAddressList() {
			try {
				const res = await request({
					url: '/shop/address/list',
					method: 'GET'
				})
				if (res.code === 200) {
					this.addressList = res.data || []
				}
			} catch (error) {
				uni.showToast({ title: '加载失败', icon: 'none' })
			}
		},
		selectAddress(address) {
			if (this.isSelectMode) {
				// 返回选中的地址
				const pages = getCurrentPages()
				const prevPage = pages[pages.length - 2]
				prevPage.$vm.selectedAddress = address
				uni.navigateBack()
			}
		},
		addAddress() {
			this.editingAddress = {
				id: null,
				receiverName: '',
				receiverPhone: '',
				province: '',
				city: '',
				district: '',
				detailAddress: '',
				isDefault: false
			}
			this.$refs.addressPopup.open()
		},
		editAddress(address) {
			this.editingAddress = { ...address }
			this.$refs.addressPopup.open()
		},
		onRegionChange(e) {
			const [province, city, district] = e.detail.value
			this.editingAddress.province = province
			this.editingAddress.city = city
			this.editingAddress.district = district
		},
		async saveAddress() {
			// 表单验证
			if (!this.editingAddress.receiverName) {
				uni.showToast({ title: '请输入收货人姓名', icon: 'none' })
				return
			}
			if (!this.editingAddress.receiverPhone) {
				uni.showToast({ title: '请输入手机号', icon: 'none' })
				return
			}
			if (!/^1[3-9]\d{9}$/.test(this.editingAddress.receiverPhone)) {
				uni.showToast({ title: '手机号格式不正确', icon: 'none' })
				return
			}
			if (!this.editingAddress.province) {
				uni.showToast({ title: '请选择所在地区', icon: 'none' })
				return
			}
			if (!this.editingAddress.detailAddress) {
				uni.showToast({ title: '请输入详细地址', icon: 'none' })
				return
			}

			try {
				const url = this.editingAddress.id ? '/shop/address/update' : '/shop/address/add'
				const method = this.editingAddress.id ? 'PUT' : 'POST'
				const res = await request({
					url,
					method,
					data: this.editingAddress
				})
				if (res.code === 200) {
					uni.showToast({ title: '保存成功', icon: 'success' })
					this.closeForm()
					this.loadAddressList()
				}
			} catch (error) {
				uni.showToast({ title: '保存失败', icon: 'none' })
			}
		},
		closeForm() {
			this.$refs.addressPopup.close()
		},
		async deleteAddress(address) {
			uni.showModal({
				title: '提示',
				content: '确定要删除这个地址吗？',
				success: async (res) => {
					if (res.confirm) {
						try {
							const res = await request({
								url: `/shop/address/delete/${address.id}`,
								method: 'DELETE'
							})
							if (res.code === 200) {
								uni.showToast({ title: '删除成功', icon: 'success' })
								this.loadAddressList()
							}
						} catch (error) {
							uni.showToast({ title: '删除失败', icon: 'none' })
						}
					}
				}
			})
		}
	}
}
</script>

<style scoped>
.container {
	padding-bottom: 140rpx;
	min-height: 100vh;
	background: #f5f5f5;
}

.address-list {
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

.address-card {
	background: #fff;
	border-radius: 16rpx;
	padding: 30rpx;
	margin-bottom: 20rpx;
}

.address-info {
	margin-bottom: 20rpx;
}

.address-header {
	display: flex;
	align-items: center;
	margin-bottom: 15rpx;
	gap: 20rpx;
}

.receiver-name {
	font-size: 32rpx;
	font-weight: bold;
	color: #333;
}

.receiver-phone {
	font-size: 28rpx;
	color: #666;
}

.default-tag {
	font-size: 22rpx;
	color: #fff;
	background: #007aff;
	padding: 2rpx 12rpx;
	border-radius: 20rpx;
}

.address-detail {
	font-size: 28rpx;
	color: #666;
	line-height: 1.5;
}

.address-actions {
	display: flex;
	justify-content: flex-end;
	gap: 40rpx;
	padding-top: 20rpx;
	border-top: 1rpx solid #f0f0f0;
}

.action-item {
	display: flex;
	align-items: center;
	gap: 10rpx;
	font-size: 26rpx;
	color: #666;
}

.action-icon {
	font-size: 28rpx;
}

.add-btn-wrap {
	position: fixed;
	bottom: 0;
	left: 0;
	right: 0;
	padding: 30rpx;
	background: #fff;
	box-shadow: 0 -2rpx 10rpx rgba(0,0,0,0.05);
}

.add-btn {
	background: #007aff;
	color: #fff;
	font-size: 32rpx;
	padding: 25rpx;
	border-radius: 50rpx;
	border: none;
}

.address-form {
	background: #fff;
	border-radius: 30rpx 30rpx 0 0;
	padding: 30rpx;
	max-height: 80vh;
	overflow-y: auto;
}

.form-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 30rpx;
}

.form-title {
	font-size: 36rpx;
	font-weight: bold;
	color: #333;
}

.form-close {
	font-size: 40rpx;
	color: #999;
}

.form-body {
	margin-bottom: 30rpx;
}

.form-item {
	margin-bottom: 30rpx;
}

.form-label {
	font-size: 28rpx;
	color: #333;
	font-weight: bold;
	margin-bottom: 15rpx;
	display: block;
}

.required {
	color: #f5222d;
}

.form-input {
	border: 1rpx solid #e0e0e0;
	border-radius: 8rpx;
	padding: 20rpx;
	font-size: 28rpx;
}

.picker-input {
	display: flex;
	align-items: center;
	min-height: 80rpx;
}

.placeholder {
	color: #999;
}

.form-textarea {
	border: 1rpx solid #e0e0e0;
	border-radius: 8rpx;
	padding: 20rpx;
	font-size: 28rpx;
	min-height: 120rpx;
	width: 100%;
	box-sizing: border-box;
}

.switch-row {
	display: flex;
	justify-content: space-between;
	align-items: center;
}

.save-btn {
	background: #007aff;
	color: #fff;
	font-size: 32rpx;
	padding: 25rpx;
	border-radius: 50rpx;
	border: none;
}
</style>
