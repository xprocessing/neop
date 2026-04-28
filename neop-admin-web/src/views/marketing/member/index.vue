<template>
  <div class="app-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>会员管理</span>
          <el-button type="primary" @click="handleAdd">新增会员等级</el-button>
        </div>
      </template>

      <el-table v-loading="loading" :data="memberList" border style="width: 100%">
        <el-table-column prop="level" label="等级" width="80" align="center" sortable />
        <el-table-column prop="name" label="等级名称" min-width="120" />
        <el-table-column label="会员图标" width="100" align="center">
          <template #default="{ row }">
            <el-image v-if="row.icon" :src="row.icon" style="width: 40px; height: 40px" />
          </template>
        </el-table-column>
        <el-table-column prop="minPoints" label="最低积分" width="120" align="center" sortable />
        <el-table-column prop="maxPoints" label="最高积分" width="120" align="center" />
        <el-table-column label="折扣" width="100" align="center">
          <template #default="{ row }">
            {{ (row.discount / 10).toFixed(1) }}折
          </template>
        </el-table-column>
        <el-table-column label="特权" min-width="200">
          <template #default="{ row }">
            <el-tag v-for="privilege in row.privileges" :key="privilege" style="margin-right: 5px">
              {{ privilege }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-switch v-model="row.status" :active-value="1" :inactive-value="0" @change="handleStatusChange(row)" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="text" @click="handleEdit(row)">编辑</el-button>
            <el-button type="text" @click="handleDelete(row)" style="color: #f56c6c">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="等级" prop="level">
          <el-input-number v-model="form.level" :min="1" :max="100" />
        </el-form-item>
        <el-form-item label="等级名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入等级名称" />
        </el-form-item>
        <el-form-item label="会员图标">
          <el-input v-model="form.icon" placeholder="请输入图标URL" />
        </el-form-item>
        <el-form-item label="最低积分" prop="minPoints">
          <el-input-number v-model="form.minPoints" :min="0" />
        </el-form-item>
        <el-form-item label="最高积分" prop="maxPoints">
          <el-input-number v-model="form.maxPoints" :min="0" />
        </el-form-item>
        <el-form-item label="折扣" prop="discount">
          <el-input-number v-model="form.discount" :min="1" :max="100" /> 折
          <div class="form-tip">1-100，表示1折到10折</div>
        </el-form-item>
        <el-form-item label="特权">
          <el-checkbox-group v-model="form.privileges">
            <el-checkbox label="专属客服">专属客服</el-checkbox>
            <el-checkbox label="免费配送">免费配送</el-checkbox>
            <el-checkbox label="专属活动">专属活动</el-checkbox>
            <el-checkbox label="积分加速">积分加速</el-checkbox>
          </el-checkbox-group>
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
const memberList = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref()

const form = ref({
  id: null,
  level: 1,
  name: '',
  icon: '',
  minPoints: 0,
  maxPoints: 0,
  discount: 100,
  privileges: [],
  status: 1
})

const rules = {
  level: [{ required: true, message: '请输入等级', trigger: 'blur' }],
  name: [{ required: true, message: '请输入等级名称', trigger: 'blur' }],
  minPoints: [{ required: true, message: '请输入最低积分', trigger: 'blur' }],
  maxPoints: [{ required: true, message: '请输入最高积分', trigger: 'blur' }]
}

const loadMemberList = async () => {
  loading.value = true
  try {
    const res = await request({ url: '/marketing/member/list', method: 'GET' })
    if (res.code === 200) {
      memberList.value = res.data
    }
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  form.value = { id: null, level: 1, name: '', icon: '', minPoints: 0, maxPoints: 0, discount: 100, privileges: [], status: 1 }
  dialogTitle.value = '新增会员等级'
  dialogVisible.value = true
}

const handleEdit = (row) => {
  form.value = { ...row, privileges: row.privileges || [] }
  dialogTitle.value = '编辑会员等级'
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除该会员等级吗？', '提示', { type: 'warning' })
    const res = await request({ url: `/marketing/member/delete/${row.id}`, method: 'DELETE' })
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadMemberList()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleStatusChange = async (row) => {
  try {
    await request({
      url: '/marketing/member/update',
      method: 'PUT',
      data: row
    })
    ElMessage.success('更新成功')
  } catch (error) {
    row.status = row.status === 1 ? 0 : 1
    ElMessage.error('更新失败')
  }
}

const handleSubmit = async () => {
  await formRef.value.validate(async (valid) => {
    if (valid) {
      const url = form.value.id ? '/marketing/member/update' : '/marketing/member/add'
      const method = form.value.id ? 'PUT' : 'POST'
      const res = await request({ url, method, data: form.value })
      if (res.code === 200) {
        ElMessage.success(form.value.id ? '更新成功' : '新增成功')
        dialogVisible.value = false
        loadMemberList()
      }
    }
  })
}

onMounted(() => {
  loadMemberList()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.form-tip {
  font-size: 12px;
  color: #999;
  margin-top: 5px;
}
</style>
