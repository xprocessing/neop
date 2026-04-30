<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="page-header">
          <span>会员套餐管理</span>
          <el-button type="primary" @click="handleAdd">新增套餐</el-button>
        </div>
      </template>
      <el-table :data="packageList" v-loading="loading" border stripe>
        <el-table-column type="index" label="#" width="60" align="center" />
        <el-table-column prop="packageName" label="套餐名称" min-width="150" />
        <el-table-column prop="price" label="价格" width="120" align="center">
          <template #default="{ row }">¥{{ row.price }}</template>
        </el-table-column>
        <el-table-column prop="dayNum" label="有效期(天)" width="120" align="center" />
        <el-table-column prop="givePoint" label="赠送积分" width="120" align="center" />
        <el-table-column prop="sort" label="排序" width="80" align="center" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-switch v-model="row.status" :active-value="1" :inactive-value="0" @change="handleStatusChange(row)" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="套餐名称" prop="packageName">
          <el-input v-model="form.packageName" placeholder="请输入套餐名称" />
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number v-model="form.price" :min="0" :precision="2" :step="10" style="width: 200px" />
        </el-form-item>
        <el-form-item label="有效期(天)" prop="dayNum">
          <el-input-number v-model="form.dayNum" :min="1" :step="30" style="width: 200px" />
        </el-form-item>
        <el-form-item label="赠送积分" prop="givePoint">
          <el-input-number v-model="form.givePoint" :min="0" :step="100" style="width: 200px" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" style="width: 200px" />
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
const packageList = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref()

const form = ref({
  id: null,
  packageName: '',
  price: 0,
  dayNum: 30,
  givePoint: 0,
  sort: 0,
  status: 1
})

const rules = {
  packageName: [{ required: true, message: '请输入套餐名称', trigger: 'blur' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
  dayNum: [{ required: true, message: '请输入有效期', trigger: 'blur' }],
  givePoint: [{ required: true, message: '请输入赠送积分', trigger: 'blur' }]
}

const loadList = async () => {
  loading.value = true
  try {
    const res = await request({ url: '/admin/member/package/list', method: 'GET' })
    if (res.code === 200) {
      packageList.value = res.data.records || []
    }
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  form.value = { id: null, packageName: '', price: 0, dayNum: 30, givePoint: 0, sort: 0, status: 1 }
  dialogTitle.value = '新增套餐'
  dialogVisible.value = true
}

const handleEdit = (row) => {
  form.value = { ...row }
  dialogTitle.value = '编辑套餐'
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除该套餐吗？', '提示', { type: 'warning' })
    const res = await request({ url: `/admin/member/package/delete?id=${row.id}`, method: 'post' })
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadList()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleStatusChange = async (row) => {
  try {
    await request({ url: `/admin/member/package/status?id=${row.id}&status=${row.status}`, method: 'post' })
    ElMessage.success('更新成功')
  } catch (error) {
    row.status = row.status === 1 ? 0 : 1
    ElMessage.error('更新失败')
  }
}

const handleSubmit = async () => {
  await formRef.value.validate(async (valid) => {
    if (valid) {
      const url = form.value.id ? '/admin/member/package/update' : '/admin/member/package/save'
      const res = await request({ url, method: 'post', data: form.value })
      if (res.code === 200) {
        ElMessage.success(form.value.id ? '更新成功' : '新增成功')
        dialogVisible.value = false
        loadList()
      }
    }
  })
}

onMounted(() => {
  loadList()
})
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
