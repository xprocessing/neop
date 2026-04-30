<template>
  <div class="app-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>字典管理</span>
          <el-button type="primary" @click="handleAddType">新增字典</el-button>
        </div>
      </template>

      <el-row :gutter="20">
        <!-- 字典类型列表 -->
        <el-col :span="10">
          <el-table v-loading="typeLoading" :data="typeList" border highlight-current-row @current-change="handleTypeChange">
            <el-table-column prop="dictName" label="字典名称" min-width="120"></el-table-column>
            <el-table-column prop="dictCode" label="字典编码" min-width="120"></el-table-column>
            <el-table-column label="操作" width="150" align="center">
              <template #default="{ row }">
                <el-button type="text" @click="handleEditType(row)">编辑</el-button>
                <el-button type="text" @click="handleDeleteType(row)" style="color: #f56c6c">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-col>

        <!-- 字典数据列表 -->
        <el-col :span="14">
          <div class="dict-data-header">
            <span>字典数据 - {{ currentTypeName }}</span>
            <el-button type="primary" @click="handleAddData" :disabled="!currentTypeId">新增数据</el-button>
          </div>
          <el-table v-loading="dataLoading" :data="dataList" border>
            <el-table-column prop="dictLabel" label="字典标签" width="120"></el-table-column>
            <el-table-column prop="dictValue" label="字典值" width="120"></el-table-column>
            <el-table-column prop="sort" label="排序" width="80" align="center"></el-table-column>
            <el-table-column prop="color" label="颜色" width="80" align="center">
              <template #default="{ row }">
                <el-tag :color="row.color" v-if="row.color">&nbsp;</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="80" align="center">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'danger'">
                  {{ row.status === 1 ? '启用' : '停用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150" align="center">
              <template #default="{ row }">
                <el-button type="text" @click="handleEditData(row)">编辑</el-button>
                <el-button type="text" @click="handleDeleteData(row)" style="color: #f56c6c">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-col>
      </el-row>
    </el-card>

    <!-- 字典类型对话框 -->
    <el-dialog :title="typeDialogTitle" v-model="typeDialogVisible" width="500px">
      <el-form ref="typeFormRef" :model="typeForm" :rules="typeRules" label-width="100px">
        <el-form-item label="字典名称" prop="dictName">
          <el-input v-model="typeForm.dictName" placeholder="请输入字典名称" />
        </el-form-item>
        <el-form-item label="字典编码" prop="dictCode">
          <el-input v-model="typeForm.dictCode" placeholder="请输入字典编码" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="typeForm.description" type="textarea" placeholder="请输入描述" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="typeForm.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="typeDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleTypeSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 字典数据对话框 -->
    <el-dialog :title="dataDialogTitle" v-model="dataDialogVisible" width="500px">
      <el-form ref="dataFormRef" :model="dataForm" :rules="dataRules" label-width="100px">
        <el-form-item label="字典标签" prop="dictLabel">
          <el-input v-model="dataForm.dictLabel" placeholder="请输入字典标签" />
        </el-form-item>
        <el-form-item label="字典值" prop="dictValue">
          <el-input v-model="dataForm.dictValue" placeholder="请输入字典值" />
        </el-form-item>
        <el-form-item label="颜色">
          <el-color-picker v-model="dataForm.color" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="dataForm.sort" :min="0" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="dataForm.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dataDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleDataSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const typeLoading = ref(false)
const dataLoading = ref(false)
const typeList = ref([])
const dataList = ref([])
const currentTypeId = ref(null)
const currentTypeName = ref('')

const typeDialogVisible = ref(false)
const typeDialogTitle = ref('')
const typeFormRef = ref()
const typeForm = ref({
  id: null,
  dictName: '',
  dictCode: '',
  description: '',
  status: 1
})

const typeRules = {
  dictName: [{ required: true, message: '请输入字典名称', trigger: 'blur' }],
  dictCode: [{ required: true, message: '请输入字典编码', trigger: 'blur' }]
}

const dataDialogVisible = ref(false)
const dataDialogTitle = ref('')
const dataFormRef = ref()
const dataForm = ref({
  id: null,
  dictTypeId: null,
  dictLabel: '',
  dictValue: '',
  color: '',
  sort: 0,
  status: 1
})

const dataRules = {
  dictLabel: [{ required: true, message: '请输入字典标签', trigger: 'blur' }],
  dictValue: [{ required: true, message: '请输入字典值', trigger: 'blur' }]
}

const loadTypeList = async () => {
  typeLoading.value = true
  try {
    const res = await request({ url: '/admin/dict/list', method: 'GET' })
    if (res.code === 200) {
      typeList.value = res.data.records || []
    }
  } finally {
    typeLoading.value = false
  }
}

const loadDataList = async (typeId) => {
  dataLoading.value = true
  try {
    const res = await request({ url: `/admin/dict/data/list/${typeId}`, method: 'GET' })
    if (res.code === 200) {
      dataList.value = (res.data || []).map(item => ({
        id: item.id,
        dictTypeId: item.dictId,
        dictLabel: item.label,
        dictValue: item.value,
        color: item.color || '',
        sort: item.sort || 0,
        status: item.status != null ? item.status : 1
      }))
    }
  } finally {
    dataLoading.value = false
  }
}

const handleTypeChange = (row) => {
  if (row) {
    currentTypeId.value = row.id
    currentTypeName.value = row.dictName
    loadDataList(row.id)
  }
}

const handleAddType = () => {
  typeForm.value = { id: null, dictName: '', dictCode: '', description: '', status: 1 }
  typeDialogTitle.value = '新增字典'
  typeDialogVisible.value = true
}

const handleEditType = (row) => {
  typeForm.value = { ...row }
  typeDialogTitle.value = '编辑字典'
  typeDialogVisible.value = true
}

const handleDeleteType = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除该字典吗？', '提示', { type: 'warning' })
    const res = await request({ url: `/admin/dict/delete?id=${row.id}`, method: 'post' })
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadTypeList()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleTypeSubmit = async () => {
  await typeFormRef.value.validate(async (valid) => {
    if (valid) {
      const url = typeForm.value.id ? '/admin/dict/update' : '/admin/dict/save'
      const res = await request({ url, method: 'post', data: typeForm.value })
      if (res.code === 200) {
        ElMessage.success(typeForm.value.id ? '更新成功' : '新增成功')
        typeDialogVisible.value = false
        loadTypeList()
      }
    }
  })
}

const handleAddData = () => {
  dataForm.value = { id: null, dictTypeId: currentTypeId.value, dictLabel: '', dictValue: '', color: '', sort: 0, status: 1 }
  dataDialogTitle.value = '新增字典数据'
  dataDialogVisible.value = true
}

const handleEditData = (row) => {
  dataForm.value = { ...row }
  dataDialogTitle.value = '编辑字典数据'
  dataDialogVisible.value = true
}

const handleDeleteData = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除该字典数据吗？', '提示', { type: 'warning' })
    const res = await request({ url: `/admin/dict/data/delete?id=${row.id}`, method: 'post' })
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadDataList(currentTypeId.value)
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleDataSubmit = async () => {
  await dataFormRef.value.validate(async (valid) => {
    if (valid) {
      const url = dataForm.value.id ? '/admin/dict/data/update' : '/admin/dict/data/save'
      const res = await request({ url, method: 'post', data: dataForm.value })
      if (res.code === 200) {
        ElMessage.success(dataForm.value.id ? '更新成功' : '新增成功')
        dataDialogVisible.value = false
        loadDataList(currentTypeId.value)
      }
    }
  })
}

onMounted(() => {
  loadTypeList()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.dict-data-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  font-size: 16px;
  font-weight: bold;
}
</style>
