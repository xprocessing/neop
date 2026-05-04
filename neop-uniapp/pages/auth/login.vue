<template>
  <view class="login-page">
    <!-- 顶部装饰 -->
    <view class="header">
      <view class="header-bg"></view>
      <view class="header-content">
        <image class="logo" src="/static/logo.png" mode="aspectFit"></image>
        <text class="title">NEOP平台</text>
        <text class="subtitle">登录您的账户</text>
      </view>
    </view>

    <!-- 登录表单 -->
    <view class="form-card">
      <view class="form-header">
        <text class="form-title">欢迎回来</text>
        <text class="form-desc">请输入您的手机号码登录</text>
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
            <view class="input-icon">
              <text class="icon-phone">📱</text>
            </view>
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

        <!-- 登录按钮 -->
        <button class="login-btn" :class="{ 'disabled': !canLogin }" @click="handleLogin" :disabled="!canLogin">
          <text class="btn-text">登录</text>
        </button>

        <!-- 其他登录方式 -->
        <view class="other-login">
          <view class="divider">
            <view class="divider-line"></view>
            <text class="divider-text">其他登录方式</text>
            <view class="divider-line"></view>
          </view>
          
          <view class="login-methods">
            <button class="method-btn wechat" @click="handleWechatLogin">
              <text class="method-icon">💬</text>
              <text class="method-text">微信登录</text>
            </button>
          </view>
        </view>
      </view>
    </view>

    <!-- 底部协议 -->
    <view class="footer">
      <text class="footer-text">登录即表示您同意</text>
      <text class="footer-link" @click="openAgreement">《用户协议》</text>
      <text class="footer-text">和</text>
      <text class="footer-link" @click="openPrivacy">《隐私政策》</text>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

// 表单数据
const phone = ref('')
const code = ref('')
const countdown = ref(0)
const errors = ref({})

// 计算属性
const canSendCode = computed(() => {
  return phone.value.length === 11 && countdown.value === 0
})

const canLogin = computed(() => {
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
    await request.post('/auth/send-code', { phone: phone.value })
    uni.showToast({ title: '验证码已发送', icon: 'success' })
    
    // 开始倒计时
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

// 处理登录
const handleLogin = async () => {
  if (!canLogin.value) return

  validatePhone()
  validateCode()
  if (errors.value.phone || errors.value.code) return

  try {
    uni.showLoading({ title: '登录中...' })
    
    const res = await request.post('/auth/login', {
      phone: phone.value,
      code: code.value
    })

    // 保存登录信息
    userStore.setToken(res.data.token)
    userStore.setUserInfo(res.data.user)

    uni.hideLoading()
    uni.showToast({ title: '登录成功', icon: 'success' })
    
    // 跳转到首页
    setTimeout(() => {
      uni.switchTab({ url: '/pages/index/index' })
    }, 1500)
  } catch (error) {
    uni.hideLoading()
    uni.showToast({ title: error.message || '登录失败', icon: 'none' })
  }
}

// 微信登录
const handleWechatLogin = () => {
  uni.showToast({ title: '微信登录功能开发中', icon: 'none' })
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
.login-page {
  min-height: 100vh;
  background: #f5f5f5;
}

.header {
  position: relative;
  height: 500rpx;
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

.logo {
  width: 120rpx;
  height: 120rpx;
  border-radius: 30rpx;
  margin-bottom: 30rpx;
  background: white;
  padding: 10rpx;
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
  margin-bottom: 50rpx;
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
  margin-bottom: 15rpx;
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

.input-icon {
  margin-left: 20rpx;
}

.icon-phone {
  font-size: 36rpx;
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
  margin-top: 10rpx;
  display: block;
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
  margin-top: 40rpx;
}

.login-btn.disabled {
  background: #ccc;
}

.other-login {
  margin-top: 50rpx;
}

.divider {
  display: flex;
  align-items: center;
  margin-bottom: 30rpx;
}

.divider-line {
  flex: 1;
  height: 1rpx;
  background: #e0e0e0;
}

.divider-text {
  padding: 0 20rpx;
  font-size: 24rpx;
  color: #999;
}

.login-methods {
  display: flex;
  justify-content: center;
}

.method-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 300rpx;
  height: 80rpx;
  background: #f0f9eb;
  border-radius: 15rpx;
}

.method-icon {
  font-size: 36rpx;
  margin-right: 10rpx;
}

.method-text {
  font-size: 28rpx;
  color: #07c160;
}

.footer {
  position: fixed;
  bottom: 50rpx;
  left: 0;
  right: 0;
  text-align: center;
  font-size: 24rpx;
  color: #999;
}

.footer-link {
  color: #667eea;
}
</style>
