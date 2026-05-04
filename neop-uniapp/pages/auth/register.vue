<template>
  <view class="register-page">
    <!-- 顶部装饰 -->
    <view class="header">
      <view class="header-bg"></view>
      <view class="header-content">
        <text class="title">创建账户</text>
        <text class="subtitle">加入NEOP平台</text>
      </view>
    </view>

    <!-- 注册表单 -->
    <view class="form-card">
      <view class="form-header">
        <text class="form-title">注册新账户</text>
        <text class="form-desc">填写以下信息完成注册</text>
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

        <!-- 邀请码输入 -->
        <view class="input-group">
          <view class="input-label">
            <text class="label-text">邀请码（可选）</text>
          </view>
          <view class="input-wrapper">
            <input 
              class="input-field" 
              type="text" 
              maxlength="20"
              placeholder="请输入邀请码（选填）" 
              v-model="inviteCode"
            />
          </view>
        </view>

        <!-- 密码输入 -->
        <view class="input-group">
          <view class="input-label">
            <text class="label-text">设置密码</text>
          </view>
          <view class="input-wrapper">
            <input 
              class="input-field" 
              :type="showPassword ? 'text' : 'password'"
              placeholder="请设置登录密码（6-20位）" 
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

        <!-- 协议勾选 -->
        <view class="agreement">
          <view class="checkbox" @click="agreeAgreement = !agreeAgreement">
            <view class="checkbox-inner" :class="{ 'checked': agreeAgreement }">
              <text v-if="agreeAgreement">✓</text>
            </view>
          </view>
          <text class="agreement-text">我已阅读并同意</text>
          <text class="agreement-link" @click="openAgreement">《用户协议》</text>
          <text class="agreement-text">和</text>
          <text class="agreement-link" @click="openPrivacy">《隐私政策》</text>
        </view>

        <!-- 注册按钮 -->
        <button class="register-btn" :class="{ 'disabled': !canRegister }" @click="handleRegister" :disabled="!canRegister">
          <text class="btn-text">注册</text>
        </button>

        <!-- 已有账户 -->
        <view class="login-link">
          <text class="link-text">已有账户？</text>
          <text class="link" @click="goLogin">立即登录</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'

// 表单数据
const phone = ref('')
const code = ref('')
const inviteCode = ref('')
const password = ref('')
const confirmPassword = ref('')
const showPassword = ref(false)
const showConfirmPassword = ref(false)
const agreeAgreement = ref(false)
const countdown = ref(0)
const errors = ref({})

// 计算属性
const canSendCode = computed(() => {
  return phone.value.length === 11 && countdown.value === 0
})

const canRegister = computed(() => {
  return phone.value.length === 11 && 
         code.value.length === 6 && 
         password.value.length >= 6 && 
         password.value === confirmPassword.value &&
         agreeAgreement.value
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
    await request.post('/auth/send-code', { phone: phone.value })
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

// 处理注册
const handleRegister = async () => {
  if (!canRegister.value) return

  validatePhone()
  validateCode()
  validatePassword()
  validateConfirmPassword()
  
  if (errors.value.phone || errors.value.code || errors.value.password || errors.value.confirmPassword) return

  if (!agreeAgreement.value) {
    uni.showToast({ title: '请同意用户协议和隐私政策', icon: 'none' })
    return
  }

  try {
    uni.showLoading({ title: '注册中...' })
    
    await request.post('/auth/register', {
      phone: phone.value,
      code: code.value,
      password: password.value,
      inviteCode: inviteCode.value
    })

    uni.hideLoading()
    uni.showToast({ title: '注册成功', icon: 'success' })
    
    setTimeout(() => {
      uni.navigateBack()
    }, 1500)
  } catch (error) {
    uni.hideLoading()
    uni.showToast({ title: error.message || '注册失败', icon: 'none' })
  }
}

// 跳转登录
const goLogin = () => {
  uni.navigateBack()
}

// 打开协议
const openAgreement = () => {
  uni.navigateTo({ url: '/pages/about/agreement' })
}

const openPrivacy = () => {
  uni.navigateTo({ url: '/pages/about/privacy' })
}
</script>

<style scoped>
.register-page {
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

.agreement {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  margin: 30rpx 0;
}

.checkbox {
  margin-right: 10rpx;
}

.checkbox-inner {
  width: 36rpx;
  height: 36rpx;
  border: 2rpx solid #ddd;
  border-radius: 8rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24rpx;
  color: white;
}

.checkbox-inner.checked {
  background: #667eea;
  border-color: #667eea;
}

.agreement-text {
  font-size: 24rpx;
  color: #999;
}

.agreement-link {
  font-size: 24rpx;
  color: #667eea;
}

.register-btn {
  width: 100%;
  height: 90rpx;
  line-height: 90rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  font-size: 32rpx;
  font-weight: bold;
  border-radius: 15rpx;
  margin-top: 20rpx;
}

.register-btn.disabled {
  background: #ccc;
}

.login-link {
  text-align: center;
  margin-top: 30rpx;
}

.link-text {
  font-size: 26rpx;
  color: #999;
}

.link {
  font-size: 26rpx;
  color: #667eea;
  font-weight: bold;
}
</style>
