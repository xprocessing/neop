<template>
  <view class="bind-phone-page">
    <!-- 顶部装饰 -->
    <view class="header">
      <view class="header-bg"></view>
      <view class="header-content">
        <text class="title">绑定手机</text>
        <text class="subtitle">绑定手机号码，保障账户安全</text>
      </view>
    </view>

    <!-- 绑定表单 -->
    <view class="form-card">
      <view class="form-header">
        <text class="form-title">绑定手机号码</text>
        <text class="form-desc">请输入您的手机号码完成绑定</text>
      </view>

      <view class="form-body">
        <!-- 手机号输入 -->
        <view class="input-group">
          <view class="input-label">
            <text class="label-text">手机号码</text>
          </view>
          <view class="input-wrapper">
            <input 
              class="input-field" 
              type="number" 
              maxlength="11"
              placeholder="请输入手机号码" 
              v-model="phone"
              @input="validatePhone"
            />
          </view>
          <text class="error-text" v-if="errors.phone">{{ errors.phone }}</text>
        </view>

        <!-- 验证码输入 -->
        <view class="input-group">
          <view class="input-label">
            <text class="label-text">验证码</text>
          </view>
          <view class="input-wrapper code-wrapper">
            <input 
              class="input-field" 
              type="number" 
              maxlength="6"
              placeholder="请输入验证码" 
              v-model="code"
              @input="validateCode"
            />
            <button 
              class="code-btn" 
              :class="{ 'disabled': !canSendCode }"
              @click="sendCode"
              :disabled="!canSendCode"
            >
              {{ codeText }}
            </button>
          </view>
          <text class="error-text" v-if="errors.code">{{ errors.code }}</text>
        </view>

        <!-- 绑定按钮 -->
        <button class="bind-btn" :class="{ 'disabled': !canBind }" @click="handleBind" :disabled="!canBind">
          <text class="btn-text">确认绑定</text>
        </button>
      </view>
    </view>

    <!-- 提示信息 -->
    <view class="tips">
      <view class="tip-item">
        <text class="tip-icon">🔒</text>
        <text class="tip-text">绑定手机后，可用于登录和找回密码</text>
      </view>
      <view class="tip-item">
        <text class="tip-icon">📱</text>
        <text class="tip-text">手机号码仅用于账户安全验证</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onLoad } from 'vue'

// 表单数据
const phone = ref('')
const code = ref('')
const countdown = ref(0)
const errors = ref({})
const userId = ref('')

// 获取页面参数
onLoad((options) => {
  if (options.userId) {
    userId.value = options.userId
  }
})

// 计算属性
const canSendCode = computed(() => {
  return phone.value.length === 11 && countdown.value === 0
})

const canBind = computed(() => {
  return phone.value.length === 11 && code.value.length === 6
})

const codeText = computed(() => {
  return countdown.value > 0 ? `${countdown.value}s后重发` : '获取验证码'
})

// 验证手机号
const validatePhone = () => {
  errors.value.phone = ''
  if (phone.value && !/^1[3-9]\d{9}$/.test(phone.value)) {
    errors.value.phone = '请输入正确的手机号码'
  }
}

// 验证验证码
const validateCode = () => {
  errors.value.code = ''
  if (code.value && !/^\d{6}$/.test(code.value)) {
    errors.value.code = '验证码为6位数字'
  }
}

// 发送验证码
const sendCode = async () => {
  if (!canSendCode.value) return
  
  validatePhone()
  if (errors.value.phone) return

  try {
    await request.post('/api/auth/send-bind-code', { phone: phone.value })
    uni.showToast({ title: '验证码已发送', icon: 'success' })
    
    countdown.value = 60
    const timer = setInterval(() => {
      countdown.value--
      if (countdown.value === 0) {
        clearInterval(timer)
      }
    }, 1000)
  } catch (error) {
    uni.showToast({ title: error.message || '发送失败', icon: 'none' })
  }
}

// 处理绑定
const handleBind = async () => {
  if (!canBind.value) return

  validatePhone()
  validateCode()
  
  if (errors.value.phone || errors.value.code) return

  try {
    uni.showLoading({ title: '绑定中...' })
    
    await request.post('/api/user/bind-phone', {
      userId: userId.value,
      phone: phone.value,
      code: code.value
    })

    uni.hideLoading()
    uni.showToast({ title: '绑定成功', icon: 'success' })
    
    setTimeout(() => {
      uni.navigateBack()
    }, 1500)
  } catch (error) {
    uni.hideLoading()
    uni.showToast({ title: error.message || '绑定失败', icon: 'none' })
  }
}
</script>

<style scoped>
.bind-phone-page {
  min-height: 100vh;
  background: #f5f5f5;
}

.header {
  position: relative;
  height: 400rpx;
  overflow: hidden;
}

.header-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.header-content {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  padding-top: 100rpx;
}

.title {
  font-size: 48rpx;
  font-weight: bold;
  color: white;
  margin-bottom: 10rpx;
}

.subtitle {
  font-size: 28rpx;
  color: rgba(255, 255, 255, 0.8);
}

.form-card {
  margin: -100rpx 30rpx 0;
  position: relative;
  z-index: 2;
  background: white;
  border-radius: 20rpx;
  padding: 50rpx 40rpx;
  box-shadow: 0 10rpx 40rpx rgba(0, 0, 0, 0.1);
}

.form-header {
  text-align: center;
  margin-bottom: 40rpx;
}

.form-title {
  font-size: 36rpx;
  font-weight: bold;
  color: #333;
  display: block;
  margin-bottom: 10rpx;
}

.form-desc {
  font-size: 26rpx;
  color: #999;
}

.input-group {
  margin-bottom: 30rpx;
}

.input-label {
  margin-bottom: 12rpx;
}

.label-text {
  font-size: 28rpx;
  color: #333;
  font-weight: 500;
}

.input-wrapper {
  display: flex;
  align-items: center;
  background: #f8f8f8;
  border-radius: 15rpx;
  padding: 0 30rpx;
  height: 90rpx;
  border: 2rpx solid transparent;
  transition: all 0.3s;
}

.input-wrapper:focus-within {
  border-color: #667eea;
  background: white;
}

.code-wrapper {
  padding-right: 10rpx;
}

.input-field {
  flex: 1;
  height: 90rpx;
  font-size: 28rpx;
  color: #333;
}

.code-btn {
  min-width: 200rpx;
  height: 70rpx;
  line-height: 70rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  font-size: 24rpx;
  border-radius: 10rpx;
  padding: 0 20rpx;
}

.code-btn.disabled {
  background: #ccc;
}

.error-text {
  font-size: 24rpx;
  color: #f5222d;
  margin-top: 8rpx;
  display: block;
}

.bind-btn {
  width: 100%;
  height: 90rpx;
  line-height: 90rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  font-size: 32rpx;
  font-weight: bold;
  border-radius: 15rpx;
  margin-top: 40rpx;
}

.bind-btn.disabled {
  background: #ccc;
}

.tips {
  padding: 40rpx 30rpx;
}

.tip-item {
  display: flex;
  align-items: center;
  margin-bottom: 20rpx;
}

.tip-icon {
  font-size: 32rpx;
  margin-right: 15rpx;
}

.tip-text {
  font-size: 26rpx;
  color: #666;
}
</style>
