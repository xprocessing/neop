<template>
  <div class="app-container">
    <el-card>
      <template #header>
        <span>提现管理</span>
      </template>

      <!-- 搜索栏 -->
      <el-form :inline="true" :model="queryParams" class="search-form">
        <el-form-item label="用户ID">
          <el-input v-model="queryParams.userId" placeholder="请输入用户ID" clearable />
        </el-form-item>
        <el-form-item label="审核状态">
          <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
            <el-option label="待审核" :value="1" />
            <el-option label="审核通过" :value="2" />
            <el-option label="审核拒绝" :value="3" />
            <el-option label="打款中" :value="4" />
            <el-option label="打款成功" :value="5" />
            <el-option label="打款失败" :value="6" />
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

      <!-- 提现列表 -->
      <el-table v-loading="loading" :data="withdrawList" border>
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="userId" label="用户ID" width="100" align="center" />
        <el-table-column prop="userName" label="用户名" min-width="120" />
        <el-table-column prop="amount" label="提现金额" width="120" align="center">
          <template #default="{ row }">
            <span style="color: #f5222d; font-weight: bold">¥{{ row.amount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="fee" label="手续费" width="100" align="center">
          <template #default="{ row }">
            <span>¥{{ row.fee || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="actualAmount" label="实际到账" width="120" align="center">
          <template #default="{ row }">
            <span style="color: #52c41a; font-weight: bold">¥{{ row.actualAmount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="accountType" label="账户类型" width="100" align="center">
          <template #default="{ row }">
            {{ row.accountType === 'WECHAT' ? '微信' : '支付宝' }}
          </template>
        </el-table-column>
        <el-table-column prop="accountName" label="账户姓名" width="120" align="center" />
        <el-table-column prop="accountNo" label="账户号码" min-width="180" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="申请时间" width="180" />
        <el-table-column label="操作" width="250" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="text" v-if="row.status === 1" @click="handleAudit(row)">审核</el-button>
            <el-button type="text" v-if="row.status === 2" @click="handlePay(row)">打款</el-button>
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
    <el-dialog :title="auditTitle" v-model="auditVisible" width="500px">
      <el-form ref="auditFormRef" :model="auditForm" label-width="100px">
        <el-form-item label="提现金额">
          <span style="color: #f5222d; font-weight: bold">¥{{ currentWithdraw.amount }}</span>
        </el-form-item>
        <el-form-item label="实际到账">
          <span style="color: #52c41a; font-weight: bold">¥{{ currentWithdraw.actualAmount }}</span>
        </el-form-item>
        <el-form-item label="账户类型">
          <span>{{ currentWithdraw.accountType === 'WECHAT' ? '微信' : '支付宝' }}</span>
        </el-form-item>
        <el-form-item label="账户姓名">
          <span>{{ currentWithdraw.accountName }}</span>
        </el-form-item>
        <el-form-item label="账户号码">
          <span>{{ currentWithdraw.accountNo }}</span>
        </el-form-item>
        <el-form-item label="审核结果" prop="auditStatus">
          <el-radio-group v-model="auditForm.auditStatus">
            <el-radio :label="2">通过</el-radio>
            <el-radio :label="3">拒绝</el-radio>
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

    <!-- 打款对话框 -->
    <el-dialog title="确认打款" v-model="payVisible" width="500px">
      <el-form ref="payFormRef" :model="payForm" label-width="100px">
        <el-form-item label="提现金额">
          <span style="color: #f5222d; font-weight: bold">¥{{ currentWithdraw.amount }}</span>
        </el-form-item>
        <el-form-item label="实际到账">
          <span style="color: #52c41a; font-weight: bold">¥{{ currentWithdraw.actualAmount }}</span>
        </el-form-item>
        <el-form-item label="打款备注">
          <el-input v-model="payForm.remark" type="textarea" placeholder="请输入打款备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="payVisible = false">取消</el-button>
        <el-button type="primary" @click="handlePaySubmit">确认打款</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const loading = ref(false)
const withdrawList = ref([])
const total = ref(0)
const auditVisible = ref(false)
const payVisible = ref(false)
const auditFormRef = ref()
const payFormRef = ref()
const currentWithdraw = ref({})
const auditTitle = ref('')

const queryParams = ref({
  pageNum: 1,
  pageSize: 10,
  userId: '',
  status: null,
  dateRange: []
})

const auditForm = ref({
  withdrawId: null,
  auditStatus: 2,
  auditRemark: ''
})

const payForm = ref({
  withdrawId: null,
  remark: ''
})

const loadWithdrawList = async () => {
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
    
    const res = await request({ url: '/task/withdraw/list', method: 'GET', params })
    if (res.code === 200) {
      withdrawList.value = res.data.list
      total.value = res.data.total
    }
  } finally {
    loading.value = false
  }
}

const getStatusType = (status) => {
  const map = { 1: 'warning', 2: 'success', 3: 'danger', 4: 'warning', 5: 'success', 6: 'danger' }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = { 1: '待审核', 2: '审核通过', 3: '审核拒绝', 4: '打款中', 5: '打款成功', 6: '打款失败' }
  return map[status] || '未知'
}

const handleQuery = () => {
  queryParams.value.pageNum = 1
  loadWithdrawList()
}

const resetQuery = () => {
  queryParams.value = {
    pageNum: 1,
    pageSize: 10,
    userId: '',
    status: null,
    dateRange: []
  }
  loadWithdrawList()
}

const handleView = (row) => {
  currentWithdraw.value = row
  auditTitle.value = '查看提现详情'
  auditForm.value = {
    withdrawId: row.id,
    auditStatus: 2,
    auditRemark: row.auditRemark || ''
  }
  auditVisible.value = true
}

const handleAudit = (row) => {
  currentWithdraw.value = row
  auditTitle.value = '提现审核'
  auditForm.value = {
    withdrawId: row.id,
    auditStatus: 2,
    auditRemark: ''
  }
  auditVisible.value = true
}

const handleAuditSubmit = async () => {
  try {
    const res = await request({
      url: '/task/withdraw/audit',
      method: 'PUT',
      data: auditForm.value
    })
    if (res.code === 200) {
      ElMessage.success('审核成功')
      auditVisible.value = false
      loadWithdrawList()
    }
  } catch (error) {
    ElMessage.error('审核失败')
  }
}

const handlePay = (row) => {
  currentWithdraw.value = row
  payForm.value = {
    withdrawId: row.id,
    remark: ''
  }
  payVisible.value = true
}

const handlePaySubmit = async () => {
  try {
    const res = await request({
      url: '/task/withdraw/pay',
      method: 'PUT',
      data: payForm.value
    })
    if (res.code === 200) {
      ElMessage.success('打款成功')
      payVisible.value = false
      loadWithdrawList()
    }
  } catch (error) {
    ElMessage.error('打款失败')
  }
}

const handleSizeChange = (val) => {
  queryParams.value.pageSize = val
  loadWithdrawList()
}

const handleCurrentChange = (val) => {
  queryParams.value.pageNum = val
  loadWithdrawList()
}

onMounted(() => {
  loadWithdrawList()
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
