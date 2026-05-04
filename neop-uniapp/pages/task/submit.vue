<template>
	<view class="container">
		<view class="form-card">
			<view class="form-header">
				<text class="form-title">提交工单</text>
				<text class="form-desc">请填写任务完成信息并提交审核</text>
			</view>

			<!-- 任务信息 -->
			<view class="task-info">
				<text class="info-title">{{ taskTitle }}</text>
				<text class="info-price">单价：¥{{ unitPrice }}</text>
			</view>

			<!-- 完成数量 -->
			<view class="form-item">
				<text class="form-label">完成数量 <text class="required">*</text></text>
				<input 
					class="form-input" 
					type="number" 
					v-model="form.completedCount" 
					placeholder="请输入完成数量"
					@input="calcAmount"
				/>
				<text class="form-hint">目标数量：{{ requiredCount }}</text>
			</view>

			<!-- 完成链接 -->
			<view class="form-item">
				<text class="form-label">完成链接 <text class="required">*</text></text>
				<textarea 
					class="form-textarea" 
					v-model="form.completedLink" 
					placeholder="请输入完成任务的链接（如：文章链接、视频链接等）"
					maxlength="500"
				/>
			</view>

			<!-- 完成截图 -->
			<view class="form-item">
				<text class="form-label">完成截图 <text class="required">*</text></text>
				<view class="upload-area">
					<view v-for="(img, index) in form.images" :key="index" class="image-item">
						<image :src="img" mode="aspectFill" class="preview-image"></image>
						<view class="delete-btn" @click="removeImage(index)">×</view>
					</view>
					<view v-if="form.images.length < 9" class="upload-btn" @click="chooseImage">
						<text class="upload-icon">+</text>
						<text class="upload-text">上传截图</text>
					</view>
				</view>
				<text class="form-hint">最多上传9张截图，单张不超过10MB</text>
			</view>

			<!-- 备注说明 -->
			<view class="form-item">
				<text class="form-label">备注说明</text>
				<textarea 
					class="form-textarea" 
					v-model="form.remark" 
					placeholder="选填，可补充说明完成情况"
					maxlength="200"
				/>
			</view>

			<!-- 结算金额预览 -->
			<view class="amount-preview">
				<text class="amount-label">预计结算金额</text>
				<text class="amount-value">¥{{ settleAmount }}</text>
			</view>

			<!-- 提交按钮 -->
			<button class="submit-btn" @click="submitForm" :disabled="submitting">
				{{ submitting ? '提交中...' : '提交审核' }}
			</button>
		</view>
	</view>
</template>

<script>
import request from '@/utils/request'

export default {
	data() {
		return {
			receiveId: '',
			taskId: '',
			taskTitle: '',
			unitPrice: 0,
			requiredCount: 0,
			form: {
				completedCount: '',
				completedLink: '',
				images: [],
				remark: ''
			},
			settleAmount: '0.00',
			submitting: false
		}
	},
	onLoad(options) {
		this.receiveId = options.receiveId
		this.taskId = options.taskId
		this.loadTaskInfo()
	},
	methods: {
		async loadTaskInfo() {
			try {
				const res = await request({
					url: `/task/${this.taskId}`,
					method: 'GET'
				})
				if (res.code === 200) {
					this.taskTitle = res.data.title
					this.unitPrice = res.data.unitPrice
					this.requiredCount = res.data.requiredCount
				}
			} catch (error) {
				uni.showToast({ title: '加载失败', icon: 'none' })
			}
		},
		calcAmount() {
			const count = parseInt(this.form.completedCount) || 0
			this.settleAmount = (count * this.unitPrice).toFixed(2)
		},
		chooseImage() {
			uni.chooseImage({
				count: 9 - this.form.images.length,
				sizeType: ['compressed'],
				sourceType: ['album', 'camera'],
				success: (res) => {
					// 上传图片到服务器
					res.tempFilePaths.forEach(path => {
						this.uploadImage(path)
					})
				}
			})
		},
		async uploadImage(filePath) {
			uni.showLoading({ title: '上传中...' })
			try {
				const res = await request.upload('/upload/image', filePath)
				if (res.code === 200) {
					this.form.images.push(res.data.url)
				}
			} catch (error) {
				uni.showToast({ title: '上传失败', icon: 'none' })
			} finally {
				uni.hideLoading()
			}
		},
		removeImage(index) {
			this.form.images.splice(index, 1)
		},
		async submitForm() {
			// 表单验证
			if (!this.form.completedCount) {
				uni.showToast({ title: '请输入完成数量', icon: 'none' })
				return
			}
			if (this.form.completedCount > this.requiredCount) {
				uni.showToast({ title: '完成数量不能超过目标数量', icon: 'none' })
				return
			}
			if (!this.form.completedLink) {
				uni.showToast({ title: '请输入完成链接', icon: 'none' })
				return
			}
			if (this.form.images.length === 0) {
				uni.showToast({ title: '请上传完成截图', icon: 'none' })
				return
			}

			this.submitting = true
			try {
				const res = await request({
					url: '/task/submit',
					method: 'POST',
					data: {
						receiveId: this.receiveId,
						completedCount: this.form.completedCount,
						completedLink: this.form.completedLink,
						images: this.form.images.join(','),
						remark: this.form.remark
					}
				})

				if (res.code === 200) {
					uni.showToast({ title: '提交成功', icon: 'success' })
					setTimeout(() => {
						uni.navigateBack()
					}, 1500)
				} else {
					uni.showToast({ title: res.message || '提交失败', icon: 'none' })
				}
			} catch (error) {
				uni.showToast({ title: '提交失败', icon: 'none' })
			} finally {
				this.submitting = false
			}
		}
	}
}
</script>

<style scoped>
.container {
	padding: 20rpx;
	background: #f5f5f5;
	min-height: 100vh;
}

.form-card {
	background: #fff;
	border-radius: 16rpx;
	padding: 30rpx;
	box-shadow: 0 2rpx 10rpx rgba(0,0,0,0.05);
}

.form-header {
	margin-bottom: 30rpx;
}

.form-title {
	font-size: 36rpx;
	font-weight: bold;
	color: #333;
	display: block;
}

.form-desc {
	font-size: 26rpx;
	color: #999;
	margin-top: 10rpx;
	display: block;
}

.task-info {
	background: #f8f9fa;
	border-radius: 12rpx;
	padding: 20rpx;
	margin-bottom: 30rpx;
}

.info-title {
	font-size: 30rpx;
	font-weight: bold;
	color: #333;
	display: block;
}

.info-price {
	font-size: 28rpx;
	color: #f5222d;
	font-weight: bold;
	margin-top: 10rpx;
	display: block;
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

.form-textarea {
	border: 1rpx solid #e0e0e0;
	border-radius: 8rpx;
	padding: 20rpx;
	font-size: 28rpx;
	min-height: 150rpx;
	width: 100%;
	box-sizing: border-box;
}

.form-hint {
	font-size: 24rpx;
	color: #999;
	margin-top: 10rpx;
	display: block;
}

.upload-area {
	display: flex;
	flex-wrap: wrap;
	gap: 20rpx;
}

.image-item {
	position: relative;
	width: 200rpx;
	height: 200rpx;
}

.preview-image {
	width: 100%;
	height: 100%;
	border-radius: 8rpx;
}

.delete-btn {
	position: absolute;
	top: -10rpx;
	right: -10rpx;
	width: 40rpx;
	height: 40rpx;
	background: #f5222d;
	color: #fff;
	border-radius: 50%;
	display: flex;
	align-items: center;
	justify-content: center;
	font-size: 24rpx;
}

.upload-btn {
	width: 200rpx;
	height: 200rpx;
	border: 2rpx dashed #d9d9d9;
	border-radius: 8rpx;
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
}

.upload-icon {
	font-size: 60rpx;
	color: #d9d9d9;
}

.upload-text {
	font-size: 24rpx;
	color: #999;
	margin-top: 10rpx;
}

.amount-preview {
	background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
	border-radius: 12rpx;
	padding: 30rpx;
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 30rpx;
}

.amount-label {
	font-size: 28rpx;
	color: #fff;
}

.amount-value {
	font-size: 48rpx;
	font-weight: bold;
	color: #fff;
}

.submit-btn {
	background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
	color: #fff;
	font-size: 32rpx;
	padding: 25rpx;
	border-radius: 50rpx;
	border: none;
	font-weight: bold;
}

.submit-btn[disabled] {
	background: #d9d9d9;
}
</style>
