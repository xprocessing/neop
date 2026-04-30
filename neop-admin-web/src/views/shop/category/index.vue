<template>
  <div class="app-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>商品分类管理</span>
          <el-button type="primary" @click="handleAdd">新增分类</el-button>
        </div>
      </template>

      <el-table
        v-loading="loading"
        :data="categoryList"
        row-key="id"
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        border
      >
        <el-table-column prop="categoryName" label="分类名称" min-width="150" />
        <el-table-column prop="icon" label="图标" width="80" align="center">
          <template #default="{ row }">
            <i v-if="row.icon" :class="row.icon"></i>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="80" align="center" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-switch v-model="row.status" :active-value="1" :inactive-value="0" @change="handleStatusChange(row)" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="text" @click="handleAddChild(row)" v-if="!row.children">新增子分类</el-button>
            <el-button type="text" @click="handleEdit(row)">编辑</el-button>
            <el-button type="text" @click="handleDelete(row)" style="color: #f56c6c">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="上级分类">
          <el-tree-select
            v-model="form.parentId"
            :data="categoryTree"
            :props="{ label: 'categoryName', value: 'id', children: 'children' }"
            placeholder="请选择上级分类"
            check-strictly
            clearable
          />
        </el-form-item>
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="分类图标">
          <el-input v-model="form.icon" placeholder="请输入图标class" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const loading = ref(false)
const categoryList = ref([])
const categoryTree = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref()

const form = ref({
  id: null,
  parentId: null,
  name: '',
  icon: '',
  sort: 0,
  status: 1
})

const rules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }]
}

const loadCategoryList = async () => {
  loading.value = true
  try {
    const res = await request({ url: '/admin/shop/category/tree', method: 'GET' })
    if (res.code === 200) {
      categoryList.value = res.data
    }
  } finally {
    loading.value = false
  }
}

const loadCategoryTree = async () => {
  try {
    const res = await request({ url: '/admin/shop/category/tree', method: 'GET' })
    if (res.code === 200) {
      categoryTree.value = [{ id: 0, categoryName: '根分类', children: res.data }]
    }
  } catch (error) {
    console.error('加载分类树失败', error)
  }
}

const handleAdd = () => {
  form.value = { id: null, parentId: null, name: '', icon: '', sort: 0, status: 1 }
  dialogTitle.value = '新增分类'
  dialogVisible.value = true
}

const handleAddChild = (row) => {
  form.value = { id: null, parentId: row.id, name: '', icon: '', sort: 0, status: 1 }
  dialogTitle.value = '新增子分类'
  dialogVisible.value = true
}

const handleEdit = (row) => {
  form.value = {
    id: row.id,
    parentId: row.parentId,
    name: row.categoryName || row.name,
    icon: row.icon || '',
    sort: row.sort || 0,
    status: row.status != null ? row.status : 1
  }
  dialogTitle.value = '编辑分类'
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除该分类吗？', '提示', { type: 'warning' })
    const res = await request({ url: `/admin/shop/category/delete?id=${row.id}`, method: 'post' })
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadCategoryList()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleStatusChange = async (row) => {
  try {
    await request({
      url: '/admin/shop/category/update',
      method: 'post',
      data: { id: row.id, categoryName: row.categoryName, sort: row.sort, status: row.status }
    })
    ElMessage.success('更新成功')
  } catch (error) {
    row.status = row.status === 1 ? 0 : 1
    ElMessage.error('更新失败')
  }
}

const handleSubmit = async () => {
  await formRef.value.validate(async (valid) => {
    if (valid) {
      const url = form.value.id ? '/admin/shop/category/update' : '/admin/shop/category/save'
      const postData = {
        ...form.value,
        categoryName: form.value.name
      }
      delete postData.name
      delete postData.icon
      const res = await request({ url, method: 'post', data: postData })
      if (res.code === 200) {
        ElMessage.success(form.value.id ? '更新成功' : '新增成功')
        dialogVisible.value = false
        loadCategoryList()
      }
    }
  })
}

onMounted(() => {
  loadCategoryList()
  loadCategoryTree()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
