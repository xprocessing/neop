<template>
  <div class="dashboard">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stat-cards">
      <el-col :span="6" v-for="card in statCards" :key="card.title">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" :style="{ background: card.color }">
            <el-icon :size="30"><component :is="card.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ card.value }}</div>
            <div class="stat-title">{{ card.title }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 图表区域 -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="16">
        <el-card header="趋势图">
          <div class="chart-placeholder">趋势图区域（使用ECharts）</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card header="任务审核状态">
          <div class="chart-placeholder">饼图区域（使用ECharts）</div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 最近数据 -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card header="最新用户">
          <el-table :data="latestUsers" size="small">
            <el-table-column prop="nickname" label="昵称" />
            <el-table-column prop="createTime" label="注册时间" />
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card header="最新订单">
          <el-table :data="latestOrders" size="small">
            <el-table-column prop="orderNo" label="订单号" />
            <el-table-column prop="totalAmount" label="金额" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { DataBoard, User, ShoppingCart, Money } from '@element-plus/icons-vue'
import request from '@/utils/request'

const statCards = ref([
  { title: '新增用户', value: 0, icon: 'User', color: '#409eff' },
  { title: '今日GMV', value: '¥0', icon: 'Money', color: '#67c23a' },
  { title: '任务领取', value: 0, icon: 'Tickets', color: '#e6a23c' },
  { title: '提现金额', value: '¥0', icon: 'Wallet', color: '#f56c6c' }
])

const latestUsers = ref([])
const latestOrders = ref([])

onMounted(() => {
  loadDashboardData()
})

const loadDashboardData = async () => {
  try {
    const res = await request.get('/sys/dashboard/overview')
    if (res.code === 200) {
      const data = res.data
      statCards.value[0].value = data.today.newUserCount || 0
      statCards.value[1].value = `¥${data.today.gmv || 0}`
      statCards.value[2].value = data.today.taskReceiveCount || 0
      statCards.value[3].value = `¥${data.today.withdrawAmount || 0}`
      
      latestUsers.value = data.latestUsers || []
      latestOrders.value = data.latestOrders || []
    }
  } catch (err) {
    console.error('加载数据看板失败', err)
  }
}
</script>

<style scoped>
.stat-cards {
  margin-bottom: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 20px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  margin-right: 15px;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #333;
}

.stat-title {
  font-size: 14px;
  color: #999;
  margin-top: 5px;
}

.chart-placeholder {
  height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #999;
  background: #f5f5f5;
  border-radius: 10px;
}
</style>
