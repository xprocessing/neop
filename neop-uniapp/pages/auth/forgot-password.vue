<template>
  <view class="forgot-password-page">
    <!-- 顶部装饰 -->
    <view class="header">
      <view class="header-bg"></view>
      <view class="header-content">
        <text class="title">找回密码</text>
        <text class="subtitle">通过手机号码重置密码</text>
      </view>
    </view>

    <!-- 步骤指示器 -->
    <view class="steps">
      <view class="step" :class="{ 'active': currentStep >= 1 }">
        <view class="step-number">1</view>
        <text class="step-text">验证手机</text>
      </view>
      <view class="step-line" :class="{ 'active': currentStep >= 2 }"></view>
      <view class="step" :class="{ 'active': currentStep >= 2 }">
        <view class="step-number">2</view>
        <text class="step-text">重置密码</text>
      </view>
      <view class="step-line" :class="{ 'active': currentStep >= 3 }"></view>
      <view class="step" :class="{ 'active': currentStep >= 3 }">
        <view class="step-number">3</view>
        <text class="step-text">完成</text>
      </view>
    </view>

    <!-- 步骤1：验证手机 -->
    <view class="form-card" v-if="currentStep === 1">
      <view class="form-header">
        <text class="form-title">验证手机号码</text>
        <text class="form-desc">请输入您的手机号码</text>
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

        <!-- 下一步按钮 -->
        <button class="next-btn" :class="{ 'disabled': !canVerify }" @click="handleVerify" :disabled="!canVerify">
          <text class="btn-text">下一步</text>
        </button>
      </view>
    </view>

    <!-- 步骤2：重置密码 -->
    <view class="form-card" v-if="currentStep === 2">
      <view class="form-header">
        <text class="form-title">设置新密码</text>
        <text class="form-desc">请设置一个新的登录密码</text>
      </view>

      <view class="form-body">
        <!-- 新密码输入 -->
        <view class="input-group">
          <view class="input-label">
            <text class="label-text">新密码</text>
          </view>
          <view class="input-wrapper">
            <input 
              class="input-field" 
              :type="showPassword ? 'text' : 'password'"
              placeholder="请设置新密码（6-20位）" 
              v-model="password"
              @input="validatePassword"
            />
            <view class="eye-icon" @click="showPassword = !showPassword">
              <text>{{ showPassword ? '👁️' : '🔒' }}</text>
            </view>
          </view>
          <text class="error-text" v-if="errors.password">{{ errors.password }}</text>
        </view>

        <!-- 确认密码 -->
        <view class="input-group">
          <view class="input-label">
            <text class="label-text">确认密码</text>
          </view>
          <view class="input-wrapper">
            <input 
              class="input-field" 
              :type="showConfirmPassword ? 'text' : 'password'"
              placeholder="请再次输入密码" 
              v-model="confirmPassword"
              @input="validateConfirmPassword"
            />
            <view class="eye-icon" @click="showConfirmPassword = !showConfirmPassword">
              <text>{{ showConfirmPassword ? '👁️' : '🔒' }}</text>
            </view>
          </view>
          <text class="error-text" v-if="errors.confirmPassword">{{ errors.confirmPassword }}</text>
        </view>

        <!-- 重置按钮 -->
        <button class="reset-btn" :class="{ 'disabled': !canReset }" @click="handleReset" :disabled="!canReset">
          <text class="btn-text">重置密码</text>
        </button>
      </view>
    </view>

    <!-- 步骤3：完成 -->
    <view class="form-card success-card" v-if="currentStep === 3">
      <view class="success-icon">✅</view>
      <text class="success-title">密码重置成功</text>
      <text class="success-desc">您的密码已成功重置，请使用新密码登录</text>
      
      <button class="login-btn" @click="goLogin">
        <text class="btn-text">立即登录</text>
      </button>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'

// 状态管理
const currentStep = ref(1)
const phone = ref('')
const code = ref('')
const password = ref('')
const confirmPassword = ref('')
const showPassword = ref(false)
const showConfirmPassword = ref(false)
const countdown = ref(0)
const errors = ref({})

// 计算属性
const canSendCode = computed(() => {
  return phone.value.length === 11 && countdown.value === 0
})

const canVerify = computed(() => {
  return phone.value.length === 11 && code.value.length === 6
})

const canReset = computed(() => {
  return password.value.length >= 6 && 
         password.value === confirmPassword.value
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

// 验证密码
const validatePassword = () => {
  errors.value.password = ''
  if (password.value && (password.value.length < 6 || password.value.length > 20)) {
    errors.value.password = '密码长度为6-20位'
  }
}

// 验证确认密码
const validateConfirmPassword = () => {
  errors.value.confirmPassword = ''
  if (confirmPassword.value && password.value !== confirmPassword.value) {
    errors.value.confirmPassword = '两次输入的密码不一致'
  }
}

// 发送验证码
const sendCode = async () => {
  if (!canSendCode.value) return
  
  validatePhone()
  if (errors.value.phone) return

  try {
    await request.post('/api/auth/send-reset-code', { phone: phone.value })
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

// 验证手机（步骤1）
const handleVerify = async () => {
  if (!canVerify.value) return

  validatePhone()
  validateCode()
  
  if (errors.value.phone || errors.value.code) return

  try {
    uni.showLoading({ title: '验证中...' })
    
    await request.post('/api/auth/verify-phone', {
      phone: phone.value,
      code: code.value
    })

    uni.hideLoading()
    currentStep.value = 2
  } catch (error) {
    uni.hideLoading()
    uni.showToast({ title: error.message || '验证失败', icon: 'none' })
  }
}

// 重置密码（步骤2）
const handleReset = async () => {
  if (!canReset.value) return

  validatePassword()
  validateConfirmPassword()
  
  if (errors.value.password || errors.value.confirmPassword) return

  try {
    uni.showLoading({ title: '重置中...' })
    
    await request.post('/api/auth/reset-password', {
      phone: phone.value,
      password: password.value
    })

    uni.hideLoading()
    currentStep.value = 3
  } catch (error) {
    uni.hideLoading()
    uni.showToast({ title: error.message || '重置失败', icon: 'none' })
  }
}

// 跳转登录（步骤3）
const goLogin = () => {
  uni.navigateBack()
}
</script>

<style scoped>
.forgot-password-page {
  min-height: 100vh;
  background: #f5f5f5;
}

.header {
  position: relative;
  height: 350rpx;
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
  padding-top: 80rpx;
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

.steps {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40rpx 60rpx;
  margin-top: -50rpx;
  position: relative;
  z-index: 2;
}

.step {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.step-number {
  width: 60rpx;
  height: 60rpx;
  border-radius: 50%;
  background: #ddd;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28rpx;
  font-weight: bold;
  margin-bottom: 10rpx;
}

.step.active .step-number {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.step-text {
  font-size: 22rpx;
  color: #999;
}

.step.active .step-text {
  color: #667eea;
  font-weight: bold;
}

.step-line {
  width: 100rpx;
  height: 4rpx;
  background: #ddd;
  margin: 0 20rpx;
  margin-bottom: 30rpx;
}

.step-line.active {
  background: #667eea;
}

.form-card {
  margin: 0 30rpx;
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
  margin-bottom: 25rpx;
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

.eye-icon {
  padding: 10rpx;
  font-size: 36rpx;
}

.error-text {
  font-size: 24rpx;
  color: #f5222d;
  margin-top: 8rpx;
  display: block;
}

.next-btn,
.reset-btn {
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

.next-btn.disabled,
.reset-btn.disabled {
  background: #ccc;
}

.success-card {
  text-align: center;
  padding: 80rpx 40rpx;
}

.success-icon {
  font-size: 120rpx;
  margin-bottom: 30rpx;
}

.success-title {
  font-size: 36rpx;
  font-weight: bold;
  color: #333;
  display: block;
  margin-bottom: 15rpx;
}

.success-desc {
  font-size: 26rpx;
  color: #999;
  display: block;
  margin-bottom: 50rpx;
}

.login-btn {
  width: 100%;
  height: 90rpx;
  line-height: 90rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  font-size: 32rpx;
  font-weight: bold;
  border-radius: 15rpx;
}
</style>
