<template>
  <div class="login-log-container">
    <!-- 搜索表单 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="用户名">
          <el-input v-model="searchForm.username" placeholder="请输入用户名" clearable />
        </el-form-item>
        
        <el-form-item label="登录状态">
          <el-select v-model="searchForm.status" placeholder="请选择登录状态" clearable>
            <el-option label="成功" value="success" />
            <el-option label="失败" value="fail" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="登录时间">
          <el-date-picker
            v-model="searchForm.timeRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 数据表格 -->
    <el-card class="table-card">
      <el-table :data="tableData" border style="width: 100%">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="ipAddress" label="IP地址" width="150" />
        <el-table-column prop="ipLocation" label="登录地点" width="150" />
        <el-table-column prop="browser" label="浏览器" width="150" />
        <el-table-column prop="os" label="操作系统" width="150" />
        <el-table-column prop="status" label="登录状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 'success' ? 'success' : 'danger'">
              {{ row.status === 'success' ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="message" label="提示信息" width="200" />
        <el-table-column prop="loginTime" label="登录时间" width="180" />
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.currentPage"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

// 搜索表单
const searchForm = reactive({
  username: '',
  status: '',
  timeRange: []
})

// 表格数据
const tableData = ref([])

// 分页
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 加载数据
const loadData = async () => {
  try {
    const params = {
      page: pagination.currentPage,
      pageSize: pagination.pageSize,
      ...searchForm
    }
    
    // 处理时间范围
    if (searchForm.timeRange && searchForm.timeRange.length === 2) {
      params.startDate = searchForm.timeRange[0]
      params.endDate = searchForm.timeRange[1]
      delete params.timeRange
    }
    
    const res = await request({
      url: '/admin/log/list',
      method: 'get',
      params
    })
    
    tableData.value = (res.data.records || []).map(item => ({
      username: item.operatorName || '',
      ipAddress: item.ipAddress || '',
      ipLocation: item.ipLocation || '',
      browser: item.browser || '',
      os: item.os || '',
      status: item.status === 1 ? 'success' : 'fail',
      message: item.description || '',
      loginTime: item.createTime || ''
    }))
    pagination.total = res.data.total
  } catch (error) {
    ElMessage.error(error.message || '加载失败')
  }
}

// 搜索
const handleSearch = () => {
  pagination.currentPage = 1
  loadData()
}

// 重置搜索
const resetSearch = () => {
  Object.keys(searchForm).forEach(key => {
    searchForm[key] = ''
  })
  searchForm.timeRange = []
  pagination.currentPage = 1
  loadData()
}

// 每页数量变化
const handleSizeChange = (val) => {
  pagination.pageSize = val
  pagination.currentPage = 1
  loadData()
}

// 当前页变化
const handleCurrentChange = (val) => {
  pagination.currentPage = val
  loadData()
}

// 初始化
onMounted(() => {
  loadData()
})
</script>

<style scoped>
.login-log-container {
  padding: 20px;
}

.search-card {
  margin-bottom: 20px;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
}

.table-card {
  margin-bottom: 20px;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>
