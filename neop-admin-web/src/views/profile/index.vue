<template>
  <div class="profile-container">
    <el-card class="profile-card">
      <template #header>
        <div class="card-header">
          <span>个人信息</span>
        </div>
      </template>
      
      <div class="profile-content">
        <!-- 头像上传 -->
        <div class="avatar-section">
          <el-avatar :size="120" :src="form.avatar || defaultAvatar" />
          <el-upload
            class="avatar-uploader"
            :action="uploadUrl"
            :show-file-list="false"
            :on-success="handleAvatarSuccess"
            :before-upload="beforeAvatarUpload"
          >
            <el-button type="primary" size="small">更换头像</el-button>
          </el-upload>
        </div>
        
        <!-- 个人信息表单 -->
        <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" class="profile-form">
          <el-form-item label="用户名" prop="username">
            <el-input v-model="form.username" placeholder="请输入用户名" />
          </el-form-item>
          
          <el-form-item label="昵称" prop="nickname">
            <el-input v-model="form.nickname" placeholder="请输入昵称" />
          </el-form-item>
          
          <el-form-item label="邮箱" prop="email">
            <el-input v-model="form.email" placeholder="请输入邮箱" />
          </el-form-item>
          
          <el-form-item label="手机号" prop="phone">
            <el-input v-model="form.phone" placeholder="请输入手机号" />
          </el-form-item>
          
          <el-form-item label="角色">
            <el-tag type="primary">{{ form.roleName }}</el-tag>
          </el-form-item>
          
          <el-form-item label="创建时间">
            <span>{{ form.createTime }}</span>
          </el-form-item>
          
          <el-form-item>
            <el-button type="primary" @click="handleSubmit">保存修改</el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

// 默认头像
const defaultAvatar = ref('https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png')

// 上传地址
const uploadUrl = ref('/api/upload/avatar')

// 表单数据
const formRef = ref()
const form = reactive({
  id: '',
  username: '',
  nickname: '',
  email: '',
  phone: '',
  avatar: '',
  roleName: '',
  createTime: ''
})

// 表单验证规则
const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
}

// 加载个人信息
const loadProfile = async () => {
  try {
    const res = await request({
      url: '/api/profile/info',
      method: 'get'
    })
    Object.assign(form, res.data)
  } catch (error) {
    ElMessage.error(error.message || '加载失败')
  }
}

// 头像上传成功
const handleAvatarSuccess = (res) => {
  form.avatar = res.data.url
  ElMessage.success('头像更新成功')
}

// 头像上传前验证
const beforeAvatarUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2
  
  if (!isImage) {
    ElMessage.error('上传头像图片只能是图片格式!')
  }
  if (!isLt2M) {
    ElMessage.error('上传头像图片大小不能超过 2MB!')
  }
  return isImage && isLt2M
}

// 提交表单
const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    
    await request({
      url: '/api/profile/update',
      method: 'put',
      data: form
    })
    
    ElMessage.success('保存成功')
  } catch (error) {
    if (error.message) {
      ElMessage.error(error.message)
    }
  }
}

// 初始化
onMounted(() => {
  loadProfile()
})
</script>

<style scoped>
.profile-container {
  padding: 20px;
}

.profile-card {
  max-width: 800px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 18px;
  font-weight: bold;
}

.profile-content {
  padding: 20px 0;
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 40px;
}

.avatar-uploader {
  margin-top: 15px;
}

.profile-form {
  max-width: 500px;
  margin: 0 auto;
}

.el-tag {
  font-size: 14px;
  padding: 8px 15px;
}
</style>
