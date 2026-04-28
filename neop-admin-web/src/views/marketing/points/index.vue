<template>
  <div class="app-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>积分规则配置</span>
        </div>
      </template>

      <el-tabs v-model="activeTab">
        <el-tab-pane label="获取规则" name="earn">
          <el-table :data="earnRules" border>
            <el-table-column prop="ruleName" label="规则名称" min-width="150" />
            <el-table-column prop="ruleCode" label="规则编码" width="150" />
            <el-table-column prop="points" label="积分数量" width="100" align="center" />
            <el-table-column prop="maxPerDay" label="每日上限" width="100" align="center">
              <template #default="{ row }">
                {{ row.maxPerDay || '不限' }}
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100" align="center">
              <template #default="{ row }">
                <el-switch v-model="row.status" :active-value="1" :inactive-value="0" @change="handleRuleStatusChange(row)" />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150" align="center">
              <template #default="{ row }">
                <el-button type="text" @click="handleEditRule(row)">编辑</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="消费规则" name="consume">
          <el-table :data="consumeRules" border>
            <el-table-column prop="ruleName" label="规则名称" min-width="150" />
            <el-table-column prop="ruleCode" label="规则编码" width="150" />
            <el-table-column prop="points" label="积分数量" width="100" align="center" />
            <el-table-column prop="status" label="状态" width="100" align="center">
              <template #default="{ row }">
                <el-switch v-model="row.status" :active-value="1" :inactive-value="0" @change="handleRuleStatusChange(row)" />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150" align="center">
              <template #default="{ row }">
                <el-button type="text" @click="handleEditRule(row)">编辑</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="积分明细" name="detail">
          <el-form :inline="true" :model="queryParams" class="search-form">
            <el-form-item label="用户ID">
              <el-input v-model="queryParams.userId" placeholder="请输入用户ID" clearable />
            </el-form-item>
            <el-form-item label="类型">
              <el-select v-model="queryParams.type" placeholder="请选择类型" clearable>
                <el-option label="获取" value="EARN" />
                <el-option label="消费" value="CONSUME" />
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

          <el-table v-loading="detailLoading" :data="detailList" border>
            <el-table-column prop="id" label="ID" width="80" align="center" />
            <el-table-column prop="userId" label="用户ID" width="100" align="center" />
            <el-table-column prop="userName" label="用户名" min-width="120" />
            <el-table-column prop="type" label="类型" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="row.type === 'EARN' ? 'success' : 'danger'">
                  {{ row.type === 'EARN' ? '获取' : '消费' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="amount" label="积分数量" width="120" align="center">
              <template #default="{ row }">
                <span :style="{ color: row.amount > 0 ? '#67c23a' : '#f56c6c' }">
                  {{ row.amount > 0 ? '+' + row.amount : row.amount }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="balance" label="剩余积分" width="120" align="center" />
            <el-table-column prop="ruleName" label="规则名称" min-width="150" />
            <el-table-column prop="createTime" label="创建时间" width="180" />
          </el-table>

          <el-pagination
            v-model:current-page="queryParams.pageNum"
            v-model:page-size="queryParams.pageSize"
            :total="detailTotal"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            class="pagination"
          />
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 编辑规则对话框 -->
    <el-dialog title="编辑规则" v-model="ruleDialogVisible" width="500px">
      <el-form ref="ruleFormRef" :model="ruleForm" label-width="120px">
        <el-form-item label="规则名称">
          <el-input v-model="ruleForm.ruleName" disabled />
        </el-form-item>
        <el-form-item label="积分数量" prop="points">
          <el-input-number v-model="ruleForm.points" :min="0" />
        </el-form-item>
        <el-form-item label="每日上限">
          <el-input-number v-model="ruleForm.maxPerDay" :min="0" />
          <div class="form-tip">0表示不限制</div>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="ruleForm.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="ruleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleRuleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const activeTab = ref('earn')
const earnRules = ref([])
const consumeRules = ref([])
const detailList = ref([])
const detailTotal = ref(0)
const detailLoading = ref(false)
const ruleDialogVisible = ref(false)
const ruleFormRef = ref()
const ruleForm = ref({})

const queryParams = ref({
  pageNum: 1,
  pageSize: 10,
  userId: '',
  type: '',
  dateRange: []
})

const loadRules = async () => {
  try {
    const res = await request({ url: '/marketing/points/rules', method: 'GET' })
    if (res.code === 200) {
      earnRules.value = res.data.filter(r => r.type === 'EARN')
      consumeRules.value = res.data.filter(r => r.type === 'CONSUME')
    }
  } catch (error) {
    console.error('加载规则失败', error)
  }
}

const loadDetailList = async () => {
  detailLoading.value = true
  try {
    const params = {
      pageNum: queryParams.value.pageNum,
      pageSize: queryParams.value.pageSize
    }
    if (queryParams.value.userId) params.userId = queryParams.value.userId
    if (queryParams.value.type) params.type = queryParams.value.type
    if (queryParams.value.dateRange && queryParams.value.dateRange.length === 2) {
      params.startDate = queryParams.value.dateRange[0]
      params.endDate = queryParams.value.dateRange[1]
    }
    
    const res = await request({ url: '/marketing/points/detail-list', method: 'GET', params })
    if (res.code === 200) {
      detailList.value = res.data.list
      detailTotal.value = res.data.total
    }
  } finally {
    detailLoading.value = false
  }
}

const handleEditRule = (row) => {
  ruleForm.value = { ...row }
  ruleDialogVisible.value = true
}

const handleRuleStatusChange = async (row) => {
  try {
    await request({
      url: '/marketing/points/rule-update',
      method: 'PUT',
      data: row
    })
    ElMessage.success('更新成功')
  } catch (error) {
    row.status = row.status === 1 ? 0 : 1
    ElMessage.error('更新失败')
  }
}

const handleRuleSubmit = async () => {
  try {
    const res = await request({
      url: '/marketing/points/rule-update',
      method: 'PUT',
      data: ruleForm.value
    })
    if (res.code === 200) {
      ElMessage.success('更新成功')
      ruleDialogVisible.value = false
      loadRules()
    }
  } catch (error) {
    ElMessage.error('更新失败')
  }
}

const handleQuery = () => {
  queryParams.value.pageNum = 1
  loadDetailList()
}

const resetQuery = () => {
  queryParams.value = {
    pageNum: 1,
    pageSize: 10,
    userId: '',
    type: '',
    dateRange: []
  }
  loadDetailList()
}

const handleSizeChange = (val) => {
  queryParams.value.pageSize = val
  loadDetailList()
}

const handleCurrentChange = (val) => {
  queryParams.value.pageNum = val
  loadDetailList()
}

onMounted(() => {
  loadRules()
  loadDetailList()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-form {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  text-align: right;
}

.form-tip {
  font-size: 12px;
  color: #999;
  margin-top: 5px;
}
</style>
