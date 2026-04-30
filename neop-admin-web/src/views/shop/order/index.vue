<template>
  <div class="app-container">
    <el-card>
      <template #header>
        <span>订单管理</span>
      </template>

      <!-- 搜索栏 -->
      <el-form :inline="true" :model="queryParams" class="search-form">
        <el-form-item label="订单号">
          <el-input v-model="queryParams.orderNo" placeholder="请输入订单号" clearable />
        </el-form-item>
        <el-form-item label="用户ID">
          <el-input v-model="queryParams.userId" placeholder="请输入用户ID" clearable />
        </el-form-item>
        <el-form-item label="订单状态">
          <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
            <el-option label="待支付" :value="1" />
            <el-option label="待发货" :value="2" />
            <el-option label="待收货" :value="3" />
            <el-option label="已完成" :value="4" />
            <el-option label="已取消" :value="5" />
            <el-option label="已退款" :value="9" />
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

      <!-- 订单列表 -->
      <el-table v-loading="loading" :data="orderList" border>
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="orderNo" label="订单号" min-width="180" />
        <el-table-column prop="userId" label="用户ID" width="100" align="center" />
        <el-table-column prop="goodsTitle" label="商品名称" min-width="200" show-overflow-tooltip />
        <el-table-column prop="specInfo" label="规格" min-width="150" />
        <el-table-column prop="quantity" label="数量" width="80" align="center" />
        <el-table-column prop="totalAmount" label="订单金额" width="120" align="center">
          <template #default="{ row }">
            <span style="color: #f5222d; font-weight: bold">¥{{ row.totalAmount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="订单状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="text" @click="handleView(row)">查看</el-button>
            <el-button type="text" v-if="row.status === 2" @click="handleShip(row)">发货</el-button>
            <el-button type="text" v-if="row.status === 4" @click="handleViewLogistics(row)">物流</el-button>
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

    <!-- 订单详情对话框 -->
    <el-dialog title="订单详情" v-model="detailVisible" width="800px">
      <el-descriptions :column="2" border v-if="orderDetail">
        <el-descriptions-item label="订单号">{{ orderDetail.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="订单状态">
          <el-tag :type="getStatusType(orderDetail.status)">
            {{ getStatusText(orderDetail.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="用户ID">{{ orderDetail.userId }}</el-descriptions-item>
        <el-descriptions-item label="订单金额">¥{{ orderDetail.totalAmount }}</el-descriptions-item>
        <el-descriptions-item label="收货人">{{ orderDetail.receiverName }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ orderDetail.receiverPhone }}</el-descriptions-item>
        <el-descriptions-item label="收货地址" :span="2">
          {{ orderDetail.receiverAddress }}
        </el-descriptions-item>
        <el-descriptions-item label="快递公司">{{ orderDetail.expressCompany || '-' }}</el-descriptions-item>
        <el-descriptions-item label="快递单号">{{ orderDetail.expressNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间" :span="2">{{ orderDetail.createTime }}</el-descriptions-item>
      </el-descriptions>

      <el-table :data="orderDetail.items || []" border style="margin-top: 20px">
        <el-table-column prop="goodsTitle" label="商品名称" min-width="200" />
        <el-table-column prop="specInfo" label="规格" width="150" />
        <el-table-column prop="quantity" label="数量" width="80" align="center" />
        <el-table-column prop="unitPrice" label="单价" width="100" align="center" />
        <el-table-column prop="totalPrice" label="小计" width="100" align="center" />
      </el-table>
    </el-dialog>

    <!-- 发货对话框 -->
    <el-dialog title="发货" v-model="shipVisible" width="500px">
      <el-form ref="shipFormRef" :model="shipForm" :rules="shipRules" label-width="100px">
        <el-form-item label="快递公司" prop="expressCompany">
          <el-select v-model="shipForm.expressCompany" placeholder="请选择快递公司">
            <el-option label="顺丰速运" value="顺丰速运" />
            <el-option label="中通快递" value="中通快递" />
            <el-option label="圆通速递" value="圆通速递" />
            <el-option label="韵达快递" value="韵达快递" />
            <el-option label="申通快递" value="申通快递" />
            <el-option label="京东物流" value="京东物流" />
          </el-select>
        </el-form-item>
        <el-form-item label="快递单号" prop="expressNo">
          <el-input v-model="shipForm.expressNo" placeholder="请输入快递单号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="shipVisible = false">取消</el-button>
        <el-button type="primary" @click="handleShipSubmit">确定发货</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const loading = ref(false)
const orderList = ref([])
const total = ref(0)
const detailVisible = ref(false)
const orderDetail = ref(null)
const shipVisible = ref(false)
const shipFormRef = ref()
const currentOrderId = ref(null)

const queryParams = ref({
  pageNum: 1,
  pageSize: 10,
  orderNo: '',
  userId: '',
  status: null,
  dateRange: []
})

const shipForm = ref({
  expressCompany: '',
  expressNo: ''
})

const shipRules = {
  expressCompany: [{ required: true, message: '请选择快递公司', trigger: 'change' }],
  expressNo: [{ required: true, message: '请输入快递单号', trigger: 'blur' }]
}

const loadOrderList = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: queryParams.value.pageNum,
      pageSize: queryParams.value.pageSize
    }
    if (queryParams.value.orderNo) params.orderNo = queryParams.value.orderNo
    if (queryParams.value.userId) params.userId = queryParams.value.userId
    if (queryParams.value.status) params.status = queryParams.value.status
    if (queryParams.value.dateRange && queryParams.value.dateRange.length === 2) {
      params.startDate = queryParams.value.dateRange[0]
      params.endDate = queryParams.value.dateRange[1]
    }
    
    const res = await request({ url: '/admin/shop/order/list', method: 'GET', params })
    if (res.code === 200) {
      orderList.value = res.data.list
      total.value = res.data.total
    }
  } finally {
    loading.value = false
  }
}

const getStatusType = (status) => {
  const map = { 1: 'warning', 2: 'primary', 3: 'success', 4: 'info', 5: 'info', 9: 'danger' }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = { 1: '待支付', 2: '待发货', 3: '待收货', 4: '已完成', 5: '已取消', 9: '已退款' }
  return map[status] || '未知'
}

const handleQuery = () => {
  queryParams.value.pageNum = 1
  loadOrderList()
}

const resetQuery = () => {
  queryParams.value = {
    pageNum: 1,
    pageSize: 10,
    orderNo: '',
    userId: '',
    status: null,
    dateRange: []
  }
  loadOrderList()
}

const handleView = async (row) => {
  try {
    const res = await request({ url: `/admin/shop/order/list/${row.id}`, method: 'GET' })
    if (res.code === 200) {
      orderDetail.value = res.data
      detailVisible.value = true
    }
  } catch (error) {
    ElMessage.error('加载订单详情失败')
  }
}

const handleShip = (row) => {
  currentOrderId.value = row.id
  shipForm.value = { expressCompany: '', expressNo: '' }
  shipVisible.value = true
}

const handleShipSubmit = async () => {
  await shipFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const res = await request({
          url: '/admin/shop/order/send',
          method: 'post',
          data: {
            orderId: currentOrderId.value,
            ...shipForm.value
          }
        })
        if (res.code === 200) {
          ElMessage.success('发货成功')
          shipVisible.value = false
          loadOrderList()
        }
      } catch (error) {
        ElMessage.error('发货失败')
      }
    }
  })
}

const handleViewLogistics = (row) => {
  // 查看物流信息
  console.log('查看物流', row)
}

const handleSizeChange = (val) => {
  queryParams.value.pageSize = val
  loadOrderList()
}

const handleCurrentChange = (val) => {
  queryParams.value.pageNum = val
  loadOrderList()
}

onMounted(() => {
  loadOrderList()
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
