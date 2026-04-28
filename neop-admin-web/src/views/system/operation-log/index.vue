<template>
  <div class="operation-log-container">
    <!-- 搜索表单 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="操作人">
          <el-input v-model="searchForm.username" placeholder="请输入操作人" clearable />
        </el-form-item>
        
        <el-form-item label="操作模块">
          <el-select v-model="searchForm.module" placeholder="请选择操作模块" clearable>
            <el-option label="用户管理" value="user" />
            <el-option label="角色管理" value="role" />
            <el-option label="菜单管理" value="menu" />
            <el-option label="商品管理" value="goods" />
            <el-option label="订单管理" value="order" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="操作类型">
          <el-select v-model="searchForm.operationType" placeholder="请选择操作类型" clearable>
            <el-option label="查询" value="query" />
            <el-option label="新增" value="add" />
            <el-option label="修改" value="update" />
            <el-option label="删除" value="delete" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="操作时间">
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
        <el-table-column prop="username" label="操作人" width="120" />
        <el-table-column prop="module" label="操作模块" width="120">
          <template #default="{ row }">
            <el-tag>{{ getModuleName(row.module) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operationType" label="操作类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getOperationTypeColor(row.operationType)">
              {{ getOperationTypeName(row.operationType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operationContent" label="操作内容" min-width="200" />
        <el-table-column prop="requestMethod" label="请求方式" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.requestMethod === 'GET' ? '' : 'success'">
              {{ row.requestMethod }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="requestUrl" label="请求URL" width="250" />
        <el-table-column prop="operationTime" label="操作时间" width="180" />
        <el-table-column prop="ipAddress" label="IP地址" width="150" />
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
  module: '',
  operationType: '',
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

// 获取模块名称
const getModuleName = (module) => {
  const moduleMap = {
    'user': '用户管理',
    'role': '角色管理',
    'menu': '菜单管理',
    'goods': '商品管理',
    'order': '订单管理'
  }
  return moduleMap[module] || module
}

// 获取操作类型名称
const getOperationTypeName = (type) => {
  const typeMap = {
    'query': '查询',
    'add': '新增',
    'update': '修改',
    'delete': '删除'
  }
  return typeMap[type] || type
}

// 获取操作类型颜色
const getOperationTypeColor = (type) => {
  const colorMap = {
    'query': '',
    'add': 'success',
    'update': 'warning',
    'delete': 'danger'
  }
  return colorMap[type] || ''
}

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
      params.startTime = searchForm.timeRange[0]
      params.endTime = searchForm.timeRange[1]
      delete params.timeRange
    }
    
    const res = await request({
      url: '/api/system/operation-log/list',
      method: 'get',
      params
    })
    
    tableData.value = res.data.list
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
.operation-log-container {
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
