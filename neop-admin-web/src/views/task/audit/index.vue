<template>
  <div class="app-container">
    <el-card>
      <template #header>
        <span>任务审核</span>
      </template>

      <!-- 搜索栏 -->
      <el-form :inline="true" :model="queryParams" class="search-form">
        <el-form-item label="任务ID">
          <el-input v-model="queryParams.taskId" placeholder="请输入任务ID" clearable />
        </el-form-item>
        <el-form-item label="用户ID">
          <el-input v-model="queryParams.userId" placeholder="请输入用户ID" clearable />
        </el-form-item>
        <el-form-item label="审核状态">
          <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
            <el-option label="进行中" :value="1" />
            <el-option label="审核失败" :value="2" />
            <el-option label="审核通过" :value="3" />
            <el-option label="已结算" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 任务列表 -->
      <el-table v-loading="loading" :data="auditList" border>
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="taskId" label="任务ID" width="100" align="center" />
        <el-table-column prop="taskTitle" label="任务名称" min-width="200" show-overflow-tooltip />
        <el-table-column prop="userId" label="用户ID" width="100" align="center" />
        <el-table-column prop="completedCount" label="完成数量" width="100" align="center" />
        <el-table-column prop="completedLink" label="完成链接" min-width="200" show-overflow-tooltip />
        <el-table-column label="完成截图" width="120" align="center">
          <template #default="{ row }">
            <el-button type="text" v-if="row.images" @click="viewImages(row.images)">查看截图</el-button>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="submitTime" label="提交时间" width="180" />
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="text" v-if="row.status === 1" @click="handleAudit(row)">审核</el-button>
            <el-button type="text" v-if="row.status === 3" @click="handleSettle(row)">结算</el-button>
            <el-button type="text" @click="handleView(row)">查看</el-button>
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

    <!-- 审核对话框 -->
    <el-dialog title="任务审核" v-model="auditVisible" width="600px">
      <el-form ref="auditFormRef" :model="auditForm" label-width="100px">
        <el-form-item label="任务名称">
          <span>{{ currentTask.taskTitle }}</span>
        </el-form-item>
        <el-form-item label="完成数量">
          <span>{{ currentTask.completedCount }}</span>
        </el-form-item>
        <el-form-item label="完成链接">
          <a :href="currentTask.completedLink" target="_blank">{{ currentTask.completedLink }}</a>
        </el-form-item>
        <el-form-item label="完成截图">
          <div v-if="currentTask.images">
            <el-image
              v-for="(img, index) in currentTask.images.split(',')"
              :key="index"
              :src="img"
              style="width: 100px; height: 100px; margin-right: 10px"
              :preview-src-list="currentTask.images.split(',')"
            />
          </div>
        </el-form-item>
        <el-form-item label="审核结果" prop="auditStatus">
          <el-radio-group v-model="auditForm.auditStatus">
            <el-radio :label="3">通过</el-radio>
            <el-radio :label="2">不通过</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审核备注" prop="auditRemark">
          <el-input v-model="auditForm.auditRemark" type="textarea" placeholder="请输入审核备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="auditVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAuditSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 结算对话框 -->
    <el-dialog title="任务结算" v-model="settleVisible" width="500px">
      <el-form ref="settleFormRef" :model="settleForm" label-width="100px">
        <el-form-item label="任务名称">
          <span>{{ currentTask.taskTitle }}</span>
        </el-form-item>
        <el-form-item label="完成数量">
          <span>{{ currentTask.completedCount }}</span>
        </el-form-item>
        <el-form-item label="结算金额">
          <span style="color: #f5222d; font-weight: bold">¥{{ settleAmount }}</span>
        </el-form-item>
        <el-form-item label="结算备注">
          <el-input v-model="settleForm.remark" type="textarea" placeholder="请输入结算备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="settleVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSettleSubmit">确定结算</el-button>
      </template>
    </el-dialog>

    <!-- 图片预览 -->
    <el-dialog title="完成截图" v-model="imageVisible" width="800px">
      <div style="text-align: center">
        <el-image
          v-for="(img, index) in previewImages"
          :key="index"
          :src="img"
          style="max-width: 100%; margin-bottom: 10px"
          :preview-src-list="previewImages"
        />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const loading = ref(false)
const auditList = ref([])
const total = ref(0)
const auditVisible = ref(false)
const settleVisible = ref(false)
const imageVisible = ref(false)
const auditFormRef = ref()
const settleFormRef = ref()
const currentTask = ref({})
const previewImages = ref([])

const queryParams = ref({
  pageNum: 1,
  pageSize: 10,
  taskId: '',
  userId: '',
  status: null
})

const auditForm = ref({
  receiveId: null,
  auditStatus: 3,
  auditRemark: ''
})

const settleForm = ref({
  receiveId: null,
  remark: ''
})

const settleAmount = computed(() => {
  if (!currentTask.value.unitPrice || !currentTask.value.completedCount) return '0.00'
  return (currentTask.value.unitPrice * currentTask.value.completedCount).toFixed(2)
})

const loadAuditList = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: queryParams.value.pageNum,
      pageSize: queryParams.value.pageSize
    }
    if (queryParams.value.taskId) params.taskId = queryParams.value.taskId
    if (queryParams.value.userId) params.userId = queryParams.value.userId
    if (queryParams.value.status) params.status = queryParams.value.status
    
    const res = await request({ url: '/task/receive/list', method: 'GET', params })
    if (res.code === 200) {
      auditList.value = res.data.list
      total.value = res.data.total
    }
  } finally {
    loading.value = false
  }
}

const getStatusType = (status) => {
  const map = { 1: 'warning', 2: 'danger', 3: 'success', 4: 'info' }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = { 1: '进行中', 2: '审核失败', 3: '审核通过', 4: '已结算' }
  return map[status] || '未知'
}

const handleQuery = () => {
  queryParams.value.pageNum = 1
  loadAuditList()
}

const resetQuery = () => {
  queryParams.value = {
    pageNum: 1,
    pageSize: 10,
    taskId: '',
    userId: '',
    status: null
  }
  loadAuditList()
}

const handleView = async (row) => {
  currentTask.value = row
  auditForm.value = {
    receiveId: row.id,
    auditStatus: 3,
    auditRemark: ''
  }
  auditVisible.value = true
}

const handleAudit = (row) => {
  currentTask.value = row
  auditForm.value = {
    receiveId: row.id,
    auditStatus: 3,
    auditRemark: ''
  }
  auditVisible.value = true
}

const handleAuditSubmit = async () => {
  try {
    const res = await request({
      url: '/task/receive/audit',
      method: 'PUT',
      data: auditForm.value
    })
    if (res.code === 200) {
      ElMessage.success('审核成功')
      auditVisible.value = false
      loadAuditList()
    }
  } catch (error) {
    ElMessage.error('审核失败')
  }
}

const handleSettle = (row) => {
  currentTask.value = row
  settleForm.value = {
    receiveId: row.id,
    remark: ''
  }
  settleVisible.value = true
}

const handleSettleSubmit = async () => {
  try {
    const res = await request({
      url: '/task/receive/settle',
      method: 'PUT',
      data: settleForm.value
    })
    if (res.code === 200) {
      ElMessage.success('结算成功')
      settleVisible.value = false
      loadAuditList()
    }
  } catch (error) {
    ElMessage.error('结算失败')
  }
}

const viewImages = (images) => {
  previewImages.value = images.split(',')
  imageVisible.value = true
}

const handleSizeChange = (val) => {
  queryParams.value.pageSize = val
  loadAuditList()
}

const handleCurrentChange = (val) => {
  queryParams.value.pageNum = val
  loadAuditList()
}

onMounted(() => {
  loadAuditList()
})
</script>

<style scoped>
.search-form {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  text-align: right;
}
</style>
