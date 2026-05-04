<template>
	<view class="container">
		<!-- 个人资料表单 -->
		<view class="profile-card">
			<!-- 头像 -->
			<view class="avatar-section">
				<image :src="userInfo.avatar || '/static/default-avatar.png'" mode="aspectFill" class="avatar"></image>
				<view class="avatar-edit" @click="chooseAvatar">更换头像</view>
			</view>

			<!-- 表单 -->
			<view class="form-list">
				<view class="form-item">
					<text class="form-label">昵称</text>
					<input 
						class="form-input" 
						v-model="userInfo.nickname" 
						placeholder="请输入昵称"
						maxlength="20"
					/>
				</view>
				<view class="form-item">
					<text class="form-label">性别</text>
					<picker 
						:range="genderOptions" 
						range-key="label"
						:value="genderIndex"
						@change="onGenderChange"
					>
						<view class="form-input picker-input">
							<text>{{ genderOptions[genderIndex].label }}</text>
						</view>
					</picker>
				</view>
				<view class="form-item">
					<text class="form-label">生日</text>
					<picker 
						mode="date" 
						:value="userInfo.birthday"
						@change="onBirthdayChange"
					>
						<view class="form-input picker-input">
							<text v-if="userInfo.birthday">{{ userInfo.birthday }}</text>
							<text v-else class="placeholder">请选择生日</text>
						</view>
					</picker>
				</view>
				<view class="form-item">
					<text class="form-label">手机号</text>
					<text class="form-value">{{ userInfo.phone || '未绑定' }}</text>
					<text class="form-link" @click="bindPhone">绑定</text>
				</view>
				<view class="form-item">
					<text class="form-label">邮箱</text>
					<input 
						class="form-input" 
						v-model="userInfo.email" 
						placeholder="请输入邮箱"
						type="email"
					/>
				</view>
				<view class="form-item">
					<text class="form-label">个性签名</text>
					<textarea 
						class="form-textarea" 
						v-model="userInfo.bio" 
						placeholder="介绍一下自己吧"
						maxlength="100"
					/>
				</view>
			</view>
		</view>

		<!-- 保存按钮 -->
		<button class="save-btn" @click="saveProfile" :disabled="saving">
			{{ saving ? '保存中...' : '保存修改' }}
		</button>
	</view>
</template>

<script>
import request from '@/utils/request'

export default {
	data() {
		return {
			userInfo: {
				avatar: '',
				nickname: '',
				gender: 0,
				birthday: '',
				phone: '',
				email: '',
				bio: ''
			},
			genderOptions: [
				{ label: '未知', value: 0 },
				{ label: '男', value: 1 },
				{ label: '女', value: 2 }
			],
			genderIndex: 0,
			saving: false
		}
	},
	onLoad() {
		this.loadUserInfo()
	},
	methods: {
		async loadUserInfo() {
			try {
				const res = await request({
					url: '/user/info',
					method: 'GET'
				})
				if (res.code === 200) {
					this.userInfo = {
						avatar: res.data.avatar || '',
						nickname: res.data.nickname || '',
						gender: res.data.gender || 0,
						birthday: res.data.birthday || '',
						phone: res.data.phone || '',
						email: res.data.email || '',
						bio: res.data.bio || ''
					}
					this.genderIndex = this.userInfo.gender
				}
			} catch (error) {
				uni.showToast({ title: '加载失败', icon: 'none' })
			}
		},
		chooseAvatar() {
			uni.chooseImage({
				count: 1,
				sizeType: ['compressed'],
				sourceType: ['album', 'camera'],
				success: (res) => {
					this.uploadAvatar(res.tempFilePaths[0])
				}
			})
		},
		async uploadAvatar(filePath) {
			uni.showLoading({ title: '上传中...' })
			try {
				const res = await request.upload('/upload/image', filePath)
				if (res.code === 200) {
					this.userInfo.avatar = res.data.url
					uni.showToast({ title: '上传成功', icon: 'success' })
				}
			} catch (error) {
				uni.showToast({ title: '上传失败', icon: 'none' })
			} finally {
				uni.hideLoading()
			}
		},
		onGenderChange(e) {
			this.genderIndex = e.detail.value
			this.userInfo.gender = this.genderOptions[this.genderIndex].value
		},
		onBirthdayChange(e) {
			this.userInfo.birthday = e.detail.value
		},
		bindPhone() {
			uni.navigateTo({
				url: '/pages/user/bind-phone'
			})
		},
		async saveProfile() {
			if (!this.userInfo.nickname) {
				uni.showToast({ title: '请输入昵称', icon: 'none' })
				return
			}

			this.saving = true
			try {
				const res = await request({
					url: '/user/update',
					method: 'PUT',
					data: this.userInfo
				})
				if (res.code === 200) {
					uni.showToast({ title: '保存成功', icon: 'success' })
					setTimeout(() => {
						uni.navigateBack()
					}, 1500)
				}
			} catch (error) {
				uni.showToast({ title: '保存失败', icon: 'none' })
			} finally {
				this.saving = false
			}
		}
	}
}
</script>

<style scoped>
.container {
	padding: 20rpx;
	min-height: 100vh;
	background: #f5f5f5;
}

.profile-card {
	background: #fff;
	border-radius: 20rpx;
	padding: 30rpx;
	margin-bottom: 30rpx;
}

.avatar-section {
	display: flex;
	flex-direction: column;
	align-items: center;
	padding: 40rpx 0;
}

.avatar {
	width: 160rpx;
	height: 160rpx;
	border-radius: 50%;
	border: 4rpx solid #f0f0f0;
}

.avatar-edit {
	font-size: 26rpx;
	color: #007aff;
	margin-top: 20rpx;
}

.form-list {
	margin-top: 20rpx;
}

.form-item {
	display: flex;
	align-items: center;
	padding: 25rpx 0;
	border-bottom: 1rpx solid #f0f0f0;
}

.form-label {
	font-size: 28rpx;
	color: #333;
	width: 160rpx;
}

.form-input {
	flex: 1;
	font-size: 28rpx;
	color: #333;
}

.picker-input {
	display: flex;
	align-items: center;
	min-height: 60rpx;
}

.placeholder {
	color: #999;
}

.form-value {
	flex: 1;
	font-size: 28rpx;
	color: #333;
}

.form-link {
	font-size: 26rpx;
	color: #007aff;
}

.form-textarea {
	flex: 1;
	font-size: 28rpx;
	color: #333;
	min-height: 100rpx;
}

.save-btn {
	background: #007aff;
	color: #fff;
	font-size: 32rpx;
	padding: 25rpx;
	border-radius: 50rpx;
	border: none;
	font-weight: bold;
}

.save-btn[disabled] {
	background: #d9d9d9;
}
</style>
