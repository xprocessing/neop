<template>
  <div class="config-container">
    <!-- 搜索表单 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="配置名称">
          <el-input v-model="searchForm.configName" placeholder="请输入配置名称" clearable />
        </el-form-item>
        
        <el-form-item label="配置键">
          <el-input v-model="searchForm.configKey" placeholder="请输入配置键" clearable />
        </el-form-item>
        
        <el-form-item label="配置类型">
          <el-select v-model="searchForm.configType" placeholder="请选择配置类型" clearable>
            <el-option label="系统配置" value="system" />
            <el-option label="用户配置" value="user" />
            <el-option label="支付配置" value="pay" />
            <el-option label="短信配置" value="sms" />
          </el-select>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
          <el-button type="success" @click="handleAdd">新增配置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 数据表格 -->
    <el-card class="table-card">
      <el-table :data="tableData" border style="width: 100%">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="configName" label="配置名称" width="150" />
        <el-table-column prop="configKey" label="配置键" width="200" />
        <el-table-column prop="configValue" label="配置值" min-width="200">
          <template #default="{ row }">
            <el-tooltip :content="row.configValue" placement="top">
              <span class="config-value">{{ row.configValue }}</span>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column prop="configType" label="配置类型" width="120">
          <template #default="{ row }">
            <el-tag>{{ getConfigTypeName(row.configType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" width="150" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
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
    
    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form ref="dialogFormRef" :model="dialogForm" :rules="dialogRules" label-width="100px">
        <el-form-item label="配置名称" prop="configName">
          <el-input v-model="dialogForm.configName" placeholder="请输入配置名称" />
        </el-form-item>
        
        <el-form-item label="配置键" prop="configKey">
          <el-input v-model="dialogForm.configKey" placeholder="请输入配置键" />
        </el-form-item>
        
        <el-form-item label="配置值" prop="configValue">
          <el-input 
            v-model="dialogForm.configValue" 
            type="textarea" 
            :rows="3"
            placeholder="请输入配置值" 
          />
        </el-form-item>
        
        <el-form-item label="配置类型" prop="configType">
          <el-select v-model="dialogForm.configType" placeholder="请选择配置类型">
            <el-option label="系统配置" value="system" />
            <el-option label="用户配置" value="user" />
            <el-option label="支付配置" value="pay" />
            <el-option label="短信配置" value="sms" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="备注">
          <el-input 
            v-model="dialogForm.remark" 
            type="textarea" 
            :rows="2"
            placeholder="请输入备注" 
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确认</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

// 搜索表单
const searchForm = reactive({
  configName: '',
  configKey: '',
  configType: ''
})

// 表格数据
const tableData = ref([])

// 分页
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 对话框
const dialogVisible = ref(false)
const dialogTitle = ref('新增配置')
const dialogFormRef = ref()
const dialogForm = reactive({
  id: '',
  configName: '',
  configKey: '',
  configValue: '',
  configType: '',
  remark: ''
})

// 对话框表单验证规则
const dialogRules = {
  configName: [
    { required: true, message: '请输入配置名称', trigger: 'blur' }
  ],
  configKey: [
    { required: true, message: '请输入配置键', trigger: 'blur' }
  ],
  configValue: [
    { required: true, message: '请输入配置值', trigger: 'blur' }
  ],
  configType: [
    { required: true, message: '请选择配置类型', trigger: 'change' }
  ]
}

// 获取配置类型名称
const getConfigTypeName = (type) => {
  const typeMap = {
    'system': '系统配置',
    'user': '用户配置',
    'pay': '支付配置',
    'sms': '短信配置'
  }
  return typeMap[type] || type
}

// 加载数据
const loadData = async () => {
  try {
    const params = {
      page: pagination.currentPage,
      pageSize: pagination.pageSize,
      ...searchForm
    }
    
    const res = await request({
      url: '/api/system/config/list',
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
  pagination.currentPage = 1
  loadData()
}

// 新增
const handleAdd = () => {
  dialogTitle.value = '新增配置'
  resetDialogForm()
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row) => {
  dialogTitle.value = '编辑配置'
  Object.assign(dialogForm, row)
  dialogVisible.value = true
}

// 删除
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该配置吗？', '提示', {
      type: 'warning'
    })
    
    await request({
      url: `/api/system/config/delete/${row.id}`,
      method: 'delete'
    })
    
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

// 提交表单
const handleSubmit = async () => {
  try {
    await dialogFormRef.value.validate()
    
    if (dialogForm.id) {
      // 编辑
      await request({
        url: '/api/system/config/update',
        method: 'put',
        data: dialogForm
      })
      ElMessage.success('修改成功')
    } else {
      // 新增
      await request({
        url: '/api/system/config/add',
        method: 'post',
        data: dialogForm
      })
      ElMessage.success('新增成功')
    }
    
    dialogVisible.value = false
    loadData()
  } catch (error) {
    if (error.message) {
      ElMessage.error(error.message)
    }
  }
}

// 重置对话框表单
const resetDialogForm = () => {
  Object.keys(dialogForm).forEach(key => {
    dialogForm[key] = ''
  })
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
.config-container {
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

.config-value {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 200px;
  display: inline-block;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>
