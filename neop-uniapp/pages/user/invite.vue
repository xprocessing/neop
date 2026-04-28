<template>
	<view class="container">
		<!-- 邀请卡片 -->
		<view class="invite-card">
			<view class="invite-header">
				<text class="invite-title">邀请好友</text>
				<text class="invite-desc">邀请好友注册，双方均可获得奖励</text>
			</view>

			<!-- 邀请统计 -->
			<view class="invite-stats">
				<view class="stat-item">
					<text class="stat-number">{{ inviteCount }}</text>
					<text class="stat-label">邀请人数</text>
				</view>
				<view class="stat-item">
					<text class="stat-number">{{ totalReward }}</text>
					<text class="stat-label">获得奖励(积分)</text>
				</view>
			</view>

			<!-- 邀请链接 -->
			<view class="invite-link-section">
				<text class="section-title">我的邀请链接</text>
				<view class="link-box">
					<text class="link-text">{{ inviteLink }}</text>
					<view class="copy-btn" @click="copyLink">复制</view>
				</view>
			</view>

			<!-- 邀请二维码 -->
			<view class="qrcode-section">
				<text class="section-title">邀请二维码</text>
				<view class="qrcode-box">
					<image v-if="qrcodeUrl" :src="qrcodeUrl" mode="aspectFit" class="qrcode-image"></image>
					<button class="generate-btn" @click="generateQrcode">生成二维码</button>
				</view>
			</view>

			<!-- 分享按钮 -->
			<button class="share-btn" open-type="share">分享给好友</button>
		</view>

		<!-- 邀请记录 -->
		<view class="records-card">
			<text class="records-title">邀请记录</text>
			<view v-if="inviteList.length === 0" class="empty">
				<text class="empty-text">暂无邀请记录</text>
			</view>
			<view v-for="item in inviteList" :key="item.id" class="record-item">
				<view class="record-info">
					<text class="record-nickname">{{ item.nickname || '未设置昵称' }}</text>
					<text class="record-time">{{ formatTime(item.createTime) }}</text>
				</view>
				<view class="record-reward">
					<text class="reward-text">+{{ item.rewardPoints }}积分</text>
					<text class="reward-status" :class="'status-' + item.status">
						{{ item.status === 1 ? '待奖励' : item.status === 2 ? '已奖励' : '已失效' }}
					</text>
				</view>
			</view>
		</view>

		<!-- 邀请规则 -->
		<view class="rules-card">
			<text class="rules-title">邀请规则</text>
			<view class="rules-list">
				<text class="rule-item">1. 邀请好友注册，好友可获得 {{ inviteePoints }} 积分</text>
				<text class="rule-item">2. 好友完成注册后，您可获得 {{ inviterPoints }} 积分</text>
				<text class="rule-item">3. 好友首次消费后，您可获得额外奖励</text>
				<text class="rule-item">4. 邀请关系永久有效，好友后续消费您均可获得返利</text>
			</view>
		</view>
	</view>
</template>

<script>
import request from '@/utils/request'

export default {
	data() {
		return {
			inviteCode: '',
			inviteLink: '',
			qrcodeUrl: '',
			inviteCount: 0,
			totalReward: 0,
			inviteePoints: 50,
			inviterPoints: 100,
			inviteList: []
		}
	},
	onLoad() {
		this.loadInviteInfo()
		this.loadInviteList()
	},
	onShareAppMessage() {
		return {
			title: '加入NEOP，一起赚积分！',
			path: `/pages/user/register?inviteCode=${this.inviteCode}`,
			imageUrl: 'https://your-domain.com/share-image.png'
		}
	},
	methods: {
		async loadInviteInfo() {
			try {
				const res = await request({
					url: '/marketing/invite/info',
					method: 'GET'
				})
				if (res.code === 200) {
					this.inviteCode = res.data.inviteCode
					this.inviteLink = res.data.inviteLink
					this.inviteCount = res.data.inviteCount
					this.totalReward = res.data.totalReward
					this.inviteePoints = res.data.inviteePoints
					this.inviterPoints = res.data.inviterPoints
				}
			} catch (error) {
				console.error('加载邀请信息失败', error)
			}
		},
		async loadInviteList() {
			try {
				const res = await request({
					url: '/marketing/invite/list',
					method: 'GET'
				})
				if (res.code === 200) {
					this.inviteList = res.data || []
				}
			} catch (error) {
				console.error('加载邀请记录失败', error)
			}
		},
		copyLink() {
			uni.setClipboardData({
				data: this.inviteLink,
				success: () => {
					uni.showToast({ title: '链接已复制', icon: 'success' })
				}
			})
		},
		async generateQrcode() {
			try {
				const res = await request({
					url: '/marketing/invite/qrcode',
					method: 'POST',
					data: {
						inviteCode: this.inviteCode
					}
				})
				if (res.code === 200) {
					this.qrcodeUrl = res.data.qrcodeUrl
					uni.showToast({ title: '生成成功', icon: 'success' })
				}
			} catch (error) {
				uni.showToast({ title: '生成失败', icon: 'none' })
			}
		},
		formatTime(time) {
			if (!time) return ''
			return time.substring(0, 16)
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

.invite-card {
	background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
	border-radius: 20rpx;
	padding: 40rpx;
	margin-bottom: 20rpx;
	color: #fff;
}

.invite-header {
	margin-bottom: 30rpx;
}

.invite-title {
	font-size: 40rpx;
	font-weight: bold;
	display: block;
}

.invite-desc {
	font-size: 26rpx;
	opacity: 0.8;
	margin-top: 10rpx;
	display: block;
}

.invite-stats {
	display: flex;
	justify-content: space-around;
	margin-bottom: 40rpx;
}

.stat-item {
	display: flex;
	flex-direction: column;
	align-items: center;
}

.stat-number {
	font-size: 48rpx;
	font-weight: bold;
}

.stat-label {
	font-size: 22rpx;
	opacity: 0.8;
	margin-top: 10rpx;
}

.invite-link-section {
	margin-bottom: 30rpx;
}

.section-title {
	font-size: 28rpx;
	font-weight: bold;
	display: block;
	margin-bottom: 15rpx;
}

.link-box {
	display: flex;
	align-items: center;
	background: rgba(255,255,255,0.2);
	border-radius: 10rpx;
	padding: 15rpx;
}

.link-text {
	flex: 1;
	font-size: 24rpx;
	word-break: break-all;
}

.copy-btn {
	background: #fff;
	color: #f5576c;
	font-size: 24rpx;
	padding: 8rpx 20rpx;
	border-radius: 20rpx;
	margin-left: 15rpx;
}

.qrcode-section {
	margin-bottom: 30rpx;
}

.qrcode-box {
	display: flex;
	flex-direction: column;
	align-items: center;
}

.qrcode-image {
	width: 300rpx;
	height: 300rpx;
	border-radius: 10rpx;
	background: #fff;
	padding: 20rpx;
}

.generate-btn {
	background: rgba(255,255,255,0.3);
	color: #fff;
	font-size: 26rpx;
	padding: 15rpx 40rpx;
	border-radius: 30rpx;
	border: 1rpx solid #fff;
	margin-top: 20rpx;
}

.share-btn {
	background: #fff;
	color: #f5576c;
	font-size: 32rpx;
	font-weight: bold;
	padding: 25rpx;
	border-radius: 50rpx;
	border: none;
}

.records-card {
	background: #fff;
	border-radius: 20rpx;
	padding: 30rpx;
	margin-bottom: 20rpx;
}

.records-title {
	font-size: 32rpx;
	font-weight: bold;
	color: #333;
	display: block;
	margin-bottom: 20rpx;
}

.empty {
	padding: 40rpx 0;
	text-align: center;
}

.empty-text {
	font-size: 26rpx;
	color: #999;
}

.record-item {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 20rpx 0;
	border-bottom: 1rpx solid #f0f0f0;
}

.record-info {
	display: flex;
	flex-direction: column;
}

.record-nickname {
	font-size: 28rpx;
	color: #333;
}

.record-time {
	font-size: 24rpx;
	color: #999;
	margin-top: 10rpx;
}

.record-reward {
	display: flex;
	flex-direction: column;
	align-items: flex-end;
}

.reward-text {
	font-size: 28rpx;
	color: #f5222d;
	font-weight: bold;
}

.reward-status {
	font-size: 22rpx;
	margin-top: 10rpx;
}

.status-1 {
	color: #fa8c16;
}

.status-2 {
	color: #52c41a;
}

.status-3 {
	color: #999;
}

.rules-card {
	background: #fff;
	border-radius: 20rpx;
	padding: 30rpx;
}

.rules-title {
	font-size: 32rpx;
	font-weight: bold;
	color: #333;
	display: block;
	margin-bottom: 20rpx;
}

.rules-list {
	display: flex;
	flex-direction: column;
	gap: 15rpx;
}

.rule-item {
	font-size: 26rpx;
	color: #666;
	line-height: 1.5;
}
</style>
