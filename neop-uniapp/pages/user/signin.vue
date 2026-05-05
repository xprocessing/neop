<template>
	<view class="container">
		<!-- 签到卡片 -->
		<view class="sign-card">
			<view class="sign-header">
				<text class="sign-title">每日签到</text>
				<text class="sign-desc">连续签到可获得额外奖励</text>
			</view>

			<!-- 连续签到天数 -->
			<view class="streak-info">
				<view class="streak-item">
					<text class="streak-number">{{ continuousDays }}</text>
					<text class="streak-label">连续签到(天)</text>
				</view>
				<view class="streak-item">
					<text class="streak-number">{{ totalDays }}</text>
					<text class="streak-label">累计签到(天)</text>
				</view>
				<view class="streak-item">
					<text class="streak-number">{{ todayPoints }}</text>
					<text class="streak-label">今日获得(积分)</text>
				</view>
			</view>

			<!-- 签到按钮 -->
			<button 
				class="sign-btn" 
				:class="{ signed: todaySigned }"
				@click="doSign"
				:disabled="todaySigned"
			>
				{{ todaySigned ? '今日已签到 ✓' : '立即签到' }}
			</button>
		</view>

		<!-- 签到日历 -->
		<view class="calendar-card">
			<view class="calendar-header">
				<text class="calendar-title">{{ currentMonth }}月签到记录</text>
			</view>
			<view class="calendar-week">
				<text v-for="day in weekDays" :key="day" class="week-item">{{ day }}</text>
			</view>
			<view class="calendar-days">
				<view 
					v-for="(day, index) in calendarDays" 
					:key="index"
					class="day-item"
					:class="{ 
						signed: day.signed, 
						today: day.isToday,
						empty: !day.day 
					}"
				>
					<text v-if="day.day" class="day-text">{{ day.day }}</text>
					<text v-if="day.signed" class="signed-icon">✓</text>
				</view>
			</view>
		</view>

		<!-- 签到规则 -->
		<view class="rules-card">
			<text class="rules-title">签到规则</text>
			<view class="rules-list">
				<text class="rule-item">1. 每日签到可获得 {{ basePoints }} 积分</text>
				<text class="rule-item">2. 连续签到7天额外奖励 {{ weekBonus }} 积分</text>
				<text class="rule-item">3. 连续签到30天额外奖励 {{ monthBonus }} 积分</text>
				<text class="rule-item">4. 签到中断后连续天数重新计算</text>
			</view>
		</view>
	</view>
</template>

<script>
import request from '@/utils/request'

export default {
	data() {
		return {
			todaySigned: false,
			continuousDays: 0,
			totalDays: 0,
			todayPoints: 0,
			basePoints: 10,
			weekBonus: 50,
			monthBonus: 300,
			currentMonth: new Date().getMonth() + 1,
			weekDays: ['日', '一', '二', '三', '四', '五', '六'],
			calendarDays: [],
			signRecords: []
		}
	},
	onLoad() {
		this.loadSignInfo()
		this.generateCalendar()
	},
	methods: {
		async loadSignInfo() {
			try {
				const res = await request({
					url: '/marketing/sign/info',
					method: 'GET'
				})
				if (res.code === 200) {
					this.todaySigned = res.data.todaySigned
					this.continuousDays = res.data.continuousDays
					this.totalDays = res.data.totalDays
					this.todayPoints = res.data.todayPoints || 0
					this.signRecords = res.data.signRecords || []
					this.generateCalendar()
				}
			} catch (error) {
				console.error('加载签到信息失败', error)
			}
		},
		async doSign() {
			if (this.todaySigned) {
				uni.showToast({ title: '今日已签到', icon: 'none' })
				return
			}
			try {
				const res = await request({
					url: '/marketing/sign/do',
					method: 'POST'
				})
				if (res.code === 200) {
					uni.showToast({ 
						title: `签到成功 +${res.data.points}积分`, 
						icon: 'success' 
					})
					this.todaySigned = true
					this.todayPoints = res.data.points
					this.continuousDays++
					this.totalDays++
					this.signRecords.push(this.formatDate(new Date()))
				}
			} catch (error) {
				uni.showToast({ title: '签到失败', icon: 'none' })
			}
		},
		generateCalendar() {
			const year = new Date().getFullYear()
			const month = new Date().getMonth()
			const firstDay = new Date(year, month, 1).getDay()
			const daysInMonth = new Date(year, month + 1, 0).getDate()
			
			const days = []
			
			// 填充月初空白
			for (let i = 0; i < firstDay; i++) {
				days.push({ day: '', signed: false, isToday: false })
			}
			
			// 填充日期
			const today = new Date()
			for (let d = 1; d <= daysInMonth; d++) {
				const dateStr = this.formatDate(new Date(year, month, d))
				days.push({
					day: d,
					signed: this.signRecords.includes(dateStr),
					isToday: d === today.getDate() && month === today.getMonth()
				})
			}
			
			this.calendarDays = days
		},
		formatDate(date) {
			const y = date.getFullYear()
			const m = String(date.getMonth() + 1).padStart(2, '0')
			const d = String(date.getDate()).padStart(2, '0')
			return `${y}-${m}-${d}`
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

.sign-card {
	background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
	border-radius: 20rpx;
	padding: 40rpx;
	margin-bottom: 20rpx;
	color: #fff;
}

.sign-header {
	margin-bottom: 30rpx;
}

.sign-title {
	font-size: 40rpx;
	font-weight: bold;
	display: block;
}

.sign-desc {
	font-size: 26rpx;
	opacity: 0.8;
	margin-top: 10rpx;
	display: block;
}

.streak-info {
	display: flex;
	justify-content: space-around;
	margin-bottom: 40rpx;
}

.streak-item {
	display: flex;
	flex-direction: column;
	align-items: center;
}

.streak-number {
	font-size: 48rpx;
	font-weight: bold;
}

.streak-label {
	font-size: 22rpx;
	opacity: 0.8;
	margin-top: 10rpx;
}

.sign-btn {
	background: #fff;
	color: #667eea;
	font-size: 32rpx;
	font-weight: bold;
	padding: 25rpx;
	border-radius: 50rpx;
	border: none;
}

.sign-btn.signed {
	background: rgba(255,255,255,0.3);
	color: #fff;
}

.calendar-card {
	background: #fff;
	border-radius: 20rpx;
	padding: 30rpx;
	margin-bottom: 20rpx;
}

.calendar-header {
	margin-bottom: 20rpx;
}

.calendar-title {
	font-size: 32rpx;
	font-weight: bold;
	color: #333;
}

.calendar-week {
	display: flex;
	border-bottom: 1rpx solid #f0f0f0;
	padding-bottom: 15rpx;
	margin-bottom: 15rpx;
}

.week-item {
	flex: 1;
	text-align: center;
	font-size: 24rpx;
	color: #999;
}

.calendar-days {
	display: flex;
	flex-wrap: wrap;
}

.day-item {
	width: 14.28%;
	height: 80rpx;
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	position: relative;
}

.day-item.signed {
	background: #e6f7ff;
	border-radius: 50%;
}

.day-item.today {
	font-weight: bold;
}

.day-text {
	font-size: 28rpx;
	color: #333;
}

.signed-icon {
	font-size: 20rpx;
	color: #007aff;
	position: absolute;
	bottom: 5rpx;
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
