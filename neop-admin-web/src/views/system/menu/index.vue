<template>
  <div class="app-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>菜单管理</span>
          <el-button type="primary" @click="handleAdd">新增菜单</el-button>
        </div>
      </template>

      <el-table
        v-loading="loading"
        :data="menuList"
        row-key="id"
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        border
      >
        <el-table-column prop="title" label="菜单名称" min-width="150"></el-table-column>
        <el-table-column prop="icon" label="图标" width="80" align="center">
          <template #default="{ row }">
            <i :class="row.icon"></i>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="80" align="center"></el-table-column>
        <el-table-column prop="permission" label="权限标识" min-width="150"></el-table-column>
        <el-table-column prop="component" label="组件路径" min-width="200" show-overflow-tooltip></el-table-column>
        <el-table-column prop="type" label="类型" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.type === 1 ? '' : row.type === 2 ? 'success' : 'warning'">
              {{ row.type === 1 ? '目录' : row.type === 2 ? '菜单' : '按钮' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="visible" label="可见" width="80" align="center">
          <template #default="{ row }">
            <el-switch v-model="row.visible" @change="handleVisibleChange(row)" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="text" @click="handleAddChild(row)" v-if="row.type !== 3">新增</el-button>
            <el-button type="text" @click="handleEdit(row)">编辑</el-button>
            <el-button type="text" @click="handleDelete(row)" style="color: #f56c6c">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="上级菜单">
          <el-tree-select
            v-model="form.parentId"
            :data="menuTree"
            :props="{ label: 'title', value: 'id', children: 'children' }"
            placeholder="请选择上级菜单"
            check-strictly
            clearable
          />
        </el-form-item>
        <el-form-item label="菜单类型" prop="type">
          <el-radio-group v-model="form.type">
            <el-radio :label="1">目录</el-radio>
            <el-radio :label="2">菜单</el-radio>
            <el-radio :label="3">按钮</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="菜单名称" prop="title">
          <el-input v-model="form.title" placeholder="请输入菜单名称" />
        </el-form-item>
        <el-form-item label="图标" v-if="form.type !== 3">
          <el-input v-model="form.icon" placeholder="请输入图标class" />
        </el-form-item>
        <el-form-item label="组件路径" v-if="form.type === 2">
          <el-input v-model="form.component" placeholder="请输入组件路径" />
        </el-form-item>
        <el-form-item label="路由路径" v-if="form.type !== 3">
          <el-input v-model="form.path" placeholder="请输入路由路径" />
        </el-form-item>
        <el-form-item label="权限标识" prop="permission">
          <el-input v-model="form.permission" placeholder="请输入权限标识" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" />
        </el-form-item>
        <el-form-item label="是否可见">
          <el-switch v-model="form.visible" />
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
const menuList = ref([])
const menuTree = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref()

const form = ref({
  id: null,
  parentId: null,
  type: 1,
  title: '',
  icon: '',
  component: '',
  path: '',
  permission: '',
  sort: 0,
  visible: true
})

const rules = {
  type: [{ required: true, message: '请选择菜单类型', trigger: 'change' }],
  title: [{ required: true, message: '请输入菜单名称', trigger: 'blur' }]
}

const loadMenuList = async () => {
  loading.value = true
  try {
    const res = await request({ url: '/system/menu/list', method: 'GET' })
    if (res.code === 200) {
      menuList.value = res.data
    }
  } finally {
    loading.value = false
  }
}

const loadMenuTree = async () => {
  try {
    const res = await request({ url: '/system/menu/tree', method: 'GET' })
    if (res.code === 200) {
      menuTree.value = [{ id: 0, title: '根菜单', children: res.data }]
    }
  } catch (error) {
    console.error('加载菜单树失败', error)
  }
}

const handleAdd = () => {
  form.value = { id: null, parentId: null, type: 1, title: '', icon: '', component: '', path: '', permission: '', sort: 0, visible: true }
  dialogTitle.value = '新增菜单'
  dialogVisible.value = true
}

const handleAddChild = (row) => {
  form.value = { id: null, parentId: row.id, type: 2, title: '', icon: '', component: '', path: '', permission: '', sort: 0, visible: true }
  dialogTitle.value = '新增子菜单'
  dialogVisible.value = true
}

const handleEdit = (row) => {
  form.value = { ...row }
  dialogTitle.value = '编辑菜单'
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除该菜单吗？', '提示', { type: 'warning' })
    const res = await request({ url: `/system/menu/delete/${row.id}`, method: 'DELETE' })
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadMenuList()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleVisibleChange = async (row) => {
  try {
    await request({
      url: '/system/menu/update',
      method: 'PUT',
      data: row
    })
    ElMessage.success('更新成功')
  } catch (error) {
    row.visible = !row.visible
    ElMessage.error('更新失败')
  }
}

const handleSubmit = async () => {
  await formRef.value.validate(async (valid) => {
    if (valid) {
      const url = form.value.id ? '/system/menu/update' : '/system/menu/add'
      const method = form.value.id ? 'PUT' : 'POST'
      const res = await request({ url, method, data: form.value })
      if (res.code === 200) {
        ElMessage.success(form.value.id ? '更新成功' : '新增成功')
        dialogVisible.value = false
        loadMenuList()
      }
    }
  })
}

onMounted(() => {
  loadMenuList()
  loadMenuTree()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
