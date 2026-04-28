<template>
  <div class="app-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>邀请规则配置</span>
          <el-button type="primary" @click="handleEditConfig">编辑配置</el-button>
        </div>
      </template>

      <!-- 配置概览 -->
      <el-descriptions :column="2" border>
        <el-descriptions-item label="邀请人奖励(积分)">{{ config.inviterPoints }}</el-descriptions-item>
        <el-descriptions-item label="被邀请人奖励(积分)">{{ config.inviteePoints }}</el-descriptions-item>
        <el-descriptions-item label="好友首次消费返利(%)">{{ config.consumeRebate }}</el-descriptions-item>
        <el-descriptions-item label="返利有效期(天)">{{ config.rebateDays }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="config.status === 1 ? 'success' : 'danger'">
            {{ config.status === 1 ? '启用' : '停用' }}
          </el-tag>
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 邀请记录 -->
    <el-card style="margin-top: 20px">
      <template #header>
        <span>邀请记录</span>
      </template>

      <el-form :inline="true" :model="queryParams" class="search-form">
        <el-form-item label="邀请人ID">
          <el-input v-model="queryParams.inviterId" placeholder="请输入邀请人ID" clearable />
        </el-form-item>
        <el-form-item label="被邀请人ID">
          <el-input v-model="queryParams.inviteeId" placeholder="请输入被邀请人ID" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
            <el-option label="待奖励" :value="1" />
            <el-option label="已奖励" :value="2" />
            <el-option label="已失效" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" :data="inviteList" border>
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="inviterId" label="邀请人ID" width="100" align="center" />
        <el-table-column prop="inviterName" label="邀请人" min-width="120" />
        <el-table-column prop="inviteeId" label="被邀请人ID" width="100" align="center" />
        <el-table-column prop="inviteeName" label="被邀请人" min-width="120" />
        <el-table-column prop="inviterReward" label="邀请人奖励" width="120" align="center">
          <template #default="{ row }">
            <span style="color: #67c23a">+{{ row.inviterReward }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="inviteeReward" label="被邀请人奖励" width="120" align="center">
          <template #default="{ row }">
            <span style="color: #67c23a">+{{ row.inviteeReward }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 2 ? 'success' : row.status === 1 ? 'warning' : 'danger'">
              {{ row.status === 2 ? '已奖励' : row.status === 1 ? '待奖励' : '已失效' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="邀请时间" width="180" />
      </el-table>

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

    <!-- 编辑配置对话框 -->
    <el-dialog title="编辑邀请配置" v-model="configDialogVisible" width="500px">
      <el-form ref="configFormRef" :model="configForm" label-width="150px">
        <el-form-item label="邀请人奖励(积分)" prop="inviterPoints">
          <el-input-number v-model="configForm.inviterPoints" :min="0" />
        </el-form-item>
        <el-form-item label="被邀请人奖励(积分)" prop="inviteePoints">
          <el-input-number v-model="configForm.inviteePoints" :min="0" />
        </el-form-item>
        <el-form-item label="好友首次消费返利(%)" prop="consumeRebate">
          <el-input-number v-model="configForm.consumeRebate" :min="0" :max="100" />
        </el-form-item>
        <el-form-item label="返利有效期(天)" prop="rebateDays">
          <el-input-number v-model="configForm.rebateDays" :min="0" />
          <div class="form-tip">0表示永久有效</div>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="configForm.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="configDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleConfigSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const config = ref({
  inviterPoints: 100,
  inviteePoints: 50,
  consumeRebate: 5,
  rebateDays: 0,
  status: 1
})

const configForm = ref({})
const configDialogVisible = ref(false)
const configFormRef = ref()

const loading = ref(false)
const inviteList = ref([])
const total = ref(0)

const queryParams = ref({
  pageNum: 1,
  pageSize: 10,
  inviterId: '',
  inviteeId: '',
  status: null
})

const loadConfig = async () => {
  try {
    const res = await request({ url: '/marketing/invite/config', method: 'GET' })
    if (res.code === 200) {
      config.value = res.data
    }
  } catch (error) {
    console.error('加载配置失败', error)
  }
}

const loadInviteList = async () => {
  loading.value = true
  try {
    const res = await request({
      url: '/marketing/invite/list',
      method: 'GET',
      params: queryParams.value
    })
    if (res.code === 200) {
      inviteList.value = res.data.list
      total.value = res.data.total
    }
  } finally {
    loading.value = false
  }
}

const handleEditConfig = () => {
  configForm.value = { ...config.value }
  configDialogVisible.value = true
}

const handleConfigSubmit = async () => {
  try {
    const res = await request({
      url: '/marketing/invite/config-update',
      method: 'PUT',
      data: configForm.value
    })
    if (res.code === 200) {
      ElMessage.success('更新成功')
      configDialogVisible.value = false
      loadConfig()
    }
  } catch (error) {
    ElMessage.error('更新失败')
  }
}

const handleQuery = () => {
  queryParams.value.pageNum = 1
  loadInviteList()
}

const resetQuery = () => {
  queryParams.value = {
    pageNum: 1,
    pageSize: 10,
    inviterId: '',
    inviteeId: '',
    status: null
  }
  loadInviteList()
}

const handleSizeChange = (val) => {
  queryParams.value.pageSize = val
  loadInviteList()
}

const handleCurrentChange = (val) => {
  queryParams.value.pageNum = val
  loadInviteList()
}

onMounted(() => {
  loadConfig()
  loadInviteList()
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
