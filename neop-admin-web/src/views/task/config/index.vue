<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="page-header">
          <span>任务配置</span>
          <el-button type="primary" @click="handleAdd">新增任务</el-button>
        </div>
      </template>
      <el-table :data="taskList" v-loading="loading" border stripe>
        <el-table-column type="index" label="#" width="60" align="center" />
        <el-table-column prop="taskTitle" label="任务标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="rewardAmount" label="奖励金额" width="120" align="center">
          <template #default="{ row }">¥{{ row.rewardAmount }}</template>
        </el-table-column>
        <el-table-column prop="totalNum" label="总次数" width="100" align="center" />
        <el-table-column prop="dayNum" label="每日限领" width="100" align="center" />
        <el-table-column prop="expireMinute" label="过期(分钟)" width="120" align="center" />
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
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="任务标题" prop="taskTitle">
          <el-input v-model="form.taskTitle" placeholder="请输入任务标题" />
        </el-form-item>
        <el-form-item label="任务封面">
          <el-input v-model="form.taskCover" placeholder="请输入封面图片URL" />
        </el-form-item>
        <el-form-item label="任务简介">
          <el-input v-model="form.taskIntro" type="textarea" placeholder="请输入任务简介" />
        </el-form-item>
        <el-form-item label="任务内容">
          <el-input v-model="form.taskContent" type="textarea" :rows="4" placeholder="请输入任务内容" />
        </el-form-item>
        <el-form-item label="奖励金额" prop="rewardAmount">
          <el-input-number v-model="form.rewardAmount" :min="0.01" :precision="2" :step="1" style="width: 200px" />
        </el-form-item>
        <el-form-item label="总次数" prop="totalNum">
          <el-input-number v-model="form.totalNum" :min="0" style="width: 200px" />
        </el-form-item>
        <el-form-item label="每日限领" prop="dayNum">
          <el-input-number v-model="form.dayNum" :min="0" style="width: 200px" />
        </el-form-item>
        <el-form-item label="过期时间(分钟)" prop="expireMinute">
          <el-input-number v-model="form.expireMinute" :min="1" style="width: 200px" />
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
const taskList = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref()

const form = ref({
  id: null,
  taskTitle: '',
  taskCover: '',
  taskIntro: '',
  taskContent: '',
  rewardAmount: 0,
  totalNum: 0,
  dayNum: 0,
  expireMinute: 30,
  sort: 0,
  status: 1
})

const rules = {
  taskTitle: [{ required: true, message: '请输入任务标题', trigger: 'blur' }],
  rewardAmount: [{ required: true, message: '请输入奖励金额', trigger: 'blur' }],
  totalNum: [{ required: true, message: '请输入总次数', trigger: 'blur' }],
  dayNum: [{ required: true, message: '请输入每日限领', trigger: 'blur' }],
  expireMinute: [{ required: true, message: '请输入过期时间', trigger: 'blur' }]
}

const loadList = async () => {
  loading.value = true
  try {
    const res = await request({ url: '/admin/task/list', method: 'GET' })
    if (res.code === 200) {
      taskList.value = res.data.records || []
    }
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  form.value = { id: null, taskTitle: '', taskCover: '', taskIntro: '', taskContent: '', rewardAmount: 0, totalNum: 0, dayNum: 0, expireMinute: 30, sort: 0, status: 1 }
  dialogTitle.value = '新增任务'
  dialogVisible.value = true
}

const handleEdit = (row) => {
  form.value = { ...row }
  dialogTitle.value = '编辑任务'
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除该任务吗？', '提示', { type: 'warning' })
    const res = await request({ url: `/admin/task/delete?id=${row.id}`, method: 'post' })
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
    await request({ url: `/admin/task/status?id=${row.id}&status=${row.status}`, method: 'post' })
    ElMessage.success('更新成功')
  } catch (error) {
    row.status = row.status === 1 ? 0 : 1
    ElMessage.error('更新失败')
  }
}

const handleSubmit = async () => {
  await formRef.value.validate(async (valid) => {
    if (valid) {
      const url = form.value.id ? '/admin/task/update' : '/admin/task/save'
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
