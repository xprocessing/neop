<template>
  <div class="app-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>打款日志</span>
          <el-button type="primary" @click="handleExport">导出日志</el-button>
        </div>
      </template>

      <!-- 统计卡片 -->
      <el-row :gutter="20" class="stats-row">
        <el-col :span="6">
          <el-card shadow="hover" class="stats-card">
            <div class="stats-value">{{ stats.totalCount || 0 }}</div>
            <div class="stats-label">总打款笔数</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stats-card">
            <div class="stats-value" style="color: #f5222d">¥{{ stats.totalAmount || 0 }}</div>
            <div class="stats-label">总打款金额</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stats-card">
            <div class="stats-value" style="color: #52c41a">{{ stats.successCount || 0 }}</div>
            <div class="stats-label">成功笔数</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stats-card">
            <div class="stats-value" style="color: #fa8c16">{{ stats.failedCount || 0 }}</div>
            <div class="stats-label">失败笔数</div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 搜索栏 -->
      <el-form :inline="true" :model="queryParams" class="search-form">
        <el-form-item label="用户ID">
          <el-input v-model="queryParams.userId" placeholder="请输入用户ID" clearable />
        </el-form-item>
        <el-form-item label="打款状态">
          <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
            <el-option label="处理中" :value="1" />
            <el-option label="成功" :value="2" />
            <el-option label="失败" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="queryParams.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 日志列表 -->
      <el-table v-loading="loading" :data="logList" border>
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="withdrawId" label="提现ID" width="100" align="center" />
        <el-table-column prop="userId" label="用户ID" width="100" align="center" />
        <el-table-column prop="userName" label="用户名" min-width="120" />
        <el-table-column prop="amount" label="打款金额" width="120" align="center">
          <template #default="{ row }">
            <span style="color: #f5222d; font-weight: bold">¥{{ row.amount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="partnerTradeNo" label="商户单号" min-width="200" show-overflow-tooltip />
        <el-table-column prop="paymentNo" label="微信单号" min-width="200" show-overflow-tooltip />
        <el-table-column prop="status" label="打款状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 2 ? 'success' : row.status === 1 ? 'warning' : 'danger'">
              {{ row.status === 2 ? '成功' : row.status === 1 ? '处理中' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="retryCount" label="重试次数" width="100" align="center" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column prop="completeTime" label="完成时间" width="180" />
        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="text" @click="handleView(row)">查看</el-button>
            <el-button type="text" v-if="row.status === 3" @click="handleRetry(row)">重试</el-button>
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

    <!-- 详情对话框 -->
    <el-dialog title="打款详情" v-model="detailVisible" width="600px">
      <el-descriptions :column="2" border v-if="logDetail">
        <el-descriptions-item label="ID">{{ logDetail.id }}</el-descriptions-item>
        <el-descriptions-item label="提现ID">{{ logDetail.withdrawId }}</el-descriptions-item>
        <el-descriptions-item label="用户ID">{{ logDetail.userId }}</el-descriptions-item>
        <el-descriptions-item label="用户名">{{ logDetail.userName }}</el-descriptions-item>
        <el-descriptions-item label="打款金额">¥{{ logDetail.amount }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="logDetail.status === 2 ? 'success' : logDetail.status === 1 ? 'warning' : 'danger'">
            {{ logDetail.status === 2 ? '成功' : logDetail.status === 1 ? '处理中' : '失败' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="商户单号">{{ logDetail.partnerTradeNo }}</el-descriptions-item>
        <el-descriptions-item label="微信单号">{{ logDetail.paymentNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="重试次数">{{ logDetail.retryCount || 0 }}</el-descriptions-item>
        <el-descriptions-item label="错误信息" :span="2">
          {{ logDetail.errorMsg || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="创建时间" :span="2">{{ logDetail.createTime }}</el-descriptions-item>
        <el-descriptions-item label="完成时间" :span="2">{{ logDetail.completeTime || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const loading = ref(false)
const logList = ref([])
const total = ref(0)
const stats = ref({})
const detailVisible = ref(false)
const logDetail = ref(null)

const queryParams = ref({
  pageNum: 1,
  pageSize: 10,
  userId: '',
  status: null,
  dateRange: []
})

const loadStats = async () => {
  try {
    const res = await request({ url: '/task/payment-log/stats', method: 'GET' })
    if (res.code === 200) {
      stats.value = res.data
    }
  } catch (error) {
    console.error('加载统计失败', error)
  }
}

const loadLogList = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: queryParams.value.pageNum,
      pageSize: queryParams.value.pageSize
    }
    if (queryParams.value.userId) params.userId = queryParams.value.userId
    if (queryParams.value.status) params.status = queryParams.value.status
    if (queryParams.value.dateRange && queryParams.value.dateRange.length === 2) {
      params.startDate = queryParams.value.dateRange[0]
      params.endDate = queryParams.value.dateRange[1]
    }
    
    const res = await request({ url: '/task/payment-log/list', method: 'GET', params })
    if (res.code === 200) {
      logList.value = res.data.list
      total.value = res.data.total
    }
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  queryParams.value.pageNum = 1
  loadLogList()
}

const resetQuery = () => {
  queryParams.value = {
    pageNum: 1,
    pageSize: 10,
    userId: '',
    status: null,
    dateRange: []
  }
  loadLogList()
}

const handleView = async (row) => {
  try {
    const res = await request({ url: `/task/payment-log/detail/${row.id}`, method: 'GET' })
    if (res.code === 200) {
      logDetail.value = res.data
      detailVisible.value = true
    }
  } catch (error) {
    ElMessage.error('加载详情失败')
  }
}

const handleRetry = async (row) => {
  try {
    await ElMessage.confirm('确定重试该打款吗？', '提示', { type: 'warning' })
    const res = await request({ url: `/task/payment-log/retry/${row.id}`, method: 'POST' })
    if (res.code === 200) {
      ElMessage.success('重试成功')
      loadLogList()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('重试失败')
    }
  }
}

const handleExport = async () => {
  try {
    const res = await request({ url: '/task/payment-log/export', method: 'GET', responseType: 'blob' })
    const blob = new Blob([res.data], { type: 'application/vnd.ms-excel' })
    const link = document.createElement('a')
    link.href = URL.createObjectURL(blob)
    link.download = `打款日志_${new Date().getTime()}.xlsx`
    link.click()
    URL.revokeObjectURL(link.href)
    ElMessage.success('导出成功')
  } catch (error) {
    ElMessage.error('导出失败')
  }
}

const handleSizeChange = (val) => {
  queryParams.value.pageSize = val
  loadLogList()
}

const handleCurrentChange = (val) => {
  queryParams.value.pageNum = val
  loadLogList()
}

onMounted(() => {
  loadStats()
  loadLogList()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stats-row {
  margin-bottom: 20px;
}

.stats-card {
  text-align: center;
  padding: 20px;
}

.stats-value {
  font-size: 32px;
  font-weight: bold;
  color: #333;
  margin-bottom: 10px;
}

.stats-label {
  font-size: 14px;
  color: #999;
}

.search-form {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  text-align: right;
}
</style>
