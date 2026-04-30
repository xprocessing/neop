<template>
  <div class="app-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>用户管理</span>
        </div>
      </template>

      <!-- 搜索栏 -->
      <el-form :inline="true" :model="queryParams" class="search-form">
        <el-form-item label="用户名">
          <el-input v-model="queryParams.username" placeholder="请输入用户名" clearable />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="queryParams.nickname" placeholder="请输入昵称" clearable />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="queryParams.phone" placeholder="请输入手机号" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
            <el-option label="正常" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 用户列表 -->
      <el-table v-loading="loading" :data="userList" border style="width: 100%">
        <el-table-column type="index" label="#" width="50" align="center" />
        <el-table-column prop="avatar" label="头像" width="80" align="center">
          <template #default="{ row }">
            <el-avatar :src="row.avatar" :size="40">{{ row.nickname?.charAt(0) }}</el-avatar>
          </template>
        </el-table-column>
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column prop="nickname" label="昵称" min-width="120" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="email" label="邮箱" min-width="180" />
        <el-table-column prop="points" label="积分" width="100" align="center" sortable />
        <el-table-column prop="memberLevel" label="会员等级" width="100" align="center">
          <template #default="{ row }">
            <el-tag>{{ row.memberLevel || '普通' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-switch v-model="row.status" :active-value="1" :inactive-value="0" @change="handleStatusChange(row)" />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="180" />
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="text" @click="handleView(row)">查看</el-button>
            <el-button type="text" @click="handleEdit(row)">编辑</el-button>
            <el-button type="text" @click="handleResetPassword(row)">重置密码</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="queryParams.pageNum"
        v-model:page-size="queryParams.pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        class="pagination"
      />
    </el-card>

    <!-- 查看/编辑对话框 -->
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" :disabled="!!form.id" />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="form.nickname" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item label="性别">
          <el-radio-group v-model="form.gender">
            <el-radio :label="0">未知</el-radio>
            <el-radio :label="1">男</el-radio>
            <el-radio :label="2">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const loading = ref(false)
const userList = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref()

const queryParams = ref({
  pageNum: 1,
  pageSize: 10,
  username: '',
  nickname: '',
  phone: '',
  status: null
})

const form = ref({
  id: null,
  username: '',
  nickname: '',
  phone: '',
  email: '',
  gender: 0,
  status: 1
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }]
}

const loadUserList = async () => {
  loading.value = true
  try {
    const res = await request({
      url: '/admin/user/list',
      method: 'GET',
      params: queryParams.value
    })
    if (res.code === 200) {
      userList.value = res.data.list
      total.value = res.data.total
    }
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  queryParams.value.pageNum = 1
  loadUserList()
}

const resetQuery = () => {
  queryParams.value = {
    pageNum: 1,
    pageSize: 10,
    username: '',
    nickname: '',
    phone: '',
    status: null
  }
  loadUserList()
}

const handleView = (row) => {
  form.value = { ...row }
  dialogTitle.value = '查看用户'
  dialogVisible.value = true
}

const handleEdit = (row) => {
  form.value = { ...row }
  dialogTitle.value = '编辑用户'
  dialogVisible.value = true
}

const handleStatusChange = async (row) => {
  try {
    await request({
      url: '/admin/user/update',
      method: 'PUT',
      data: row
    })
    ElMessage.success('更新成功')
  } catch (error) {
    row.status = row.status === 1 ? 0 : 1
    ElMessage.error('更新失败')
  }
}

const handleResetPassword = async (row) => {
  try {
    await ElMessageBox.confirm('确定重置该用户的密码吗？重置后密码为123456', '提示', { type: 'warning' })
    const res = await request({
      url: `/admin/user/reset-password/${row.id}`,
      method: 'PUT'
    })
    if (res.code === 200) {
      ElMessage.success('重置成功，新密码为123456')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('重置失败')
    }
  }
}

const handleSubmit = async () => {
  await formRef.value.validate(async (valid) => {
    if (valid) {
      const url = form.value.id ? '/admin/user/update' : '/admin/user/add'
      const method = form.value.id ? 'PUT' : 'POST'
      const res = await request({ url, method, data: form.value })
      if (res.code === 200) {
        ElMessage.success(form.value.id ? '更新成功' : '新增成功')
        dialogVisible.value = false
        loadUserList()
      }
    }
  })
}

const handleSizeChange = (val) => {
  queryParams.value.pageSize = val
  loadUserList()
}

const handleCurrentChange = (val) => {
  queryParams.value.pageNum = val
  loadUserList()
}

onMounted(() => {
  loadUserList()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-form {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  text-align: right;
}
</style>
