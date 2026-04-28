<template>
  <div class="app-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>角色管理</span>
          <el-button type="primary" @click="handleAdd">新增角色</el-button>
        </div>
      </template>

      <el-table v-loading="loading" :data="roleList" border style="width: 100%">
        <el-table-column prop="roleName" label="角色名称" min-width="150"></el-table-column>
        <el-table-column prop="roleCode" label="角色编码" min-width="150"></el-table-column>
        <el-table-column prop="description" label="描述" min-width="200"></el-table-column>
        <el-table-column prop="sort" label="排序" width="80" align="center"></el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-switch v-model="row.status" :active-value="1" :inactive-value="0" @change="handleStatusChange(row)" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="text" @click="handlePermission(row)">分配权限</el-button>
            <el-button type="text" @click="handleEdit(row)">编辑</el-button>
            <el-button type="text" @click="handleDelete(row)" style="color: #f56c6c">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="form.roleName" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="角色编码" prop="roleCode">
          <el-input v-model="form.roleCode" placeholder="请输入角色编码" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" placeholder="请输入描述" />
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

    <!-- 分配权限对话框 -->
    <el-dialog title="分配权限" v-model="permissionDialogVisible" width="600px">
      <el-tree
        ref="permissionTreeRef"
        :data="menuTree"
        :props="{ label: 'title', value: 'id', children: 'children' }"
        show-checkbox
        node-key="id"
        default-expand-all
        :default-checked-keys="checkedKeys"
      />
      <template #footer>
        <el-button @click="permissionDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handlePermissionSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const loading = ref(false)
const roleList = ref([])
const menuTree = ref([])
const dialogVisible = ref(false)
const permissionDialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref()
const permissionTreeRef = ref()
const checkedKeys = ref([])
const currentRoleId = ref(null)

const form = ref({
  id: null,
  roleName: '',
  roleCode: '',
  description: '',
  sort: 0,
  status: 1
})

const rules = {
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  roleCode: [{ required: true, message: '请输入角色编码', trigger: 'blur' }]
}

const loadRoleList = async () => {
  loading.value = true
  try {
    const res = await request({ url: '/system/role/list', method: 'GET' })
    if (res.code === 200) {
      roleList.value = res.data
    }
  } finally {
    loading.value = false
  }
}

const loadMenuTree = async () => {
  try {
    const res = await request({ url: '/system/menu/tree', method: 'GET' })
    if (res.code === 200) {
      menuTree.value = res.data
    }
  } catch (error) {
    console.error('加载菜单树失败', error)
  }
}

const handleAdd = () => {
  form.value = { id: null, roleName: '', roleCode: '', description: '', sort: 0, status: 1 }
  dialogTitle.value = '新增角色'
  dialogVisible.value = true
}

const handleEdit = (row) => {
  form.value = { ...row }
  dialogTitle.value = '编辑角色'
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除该角色吗？', '提示', { type: 'warning' })
    const res = await request({ url: `/system/role/delete/${row.id}`, method: 'DELETE' })
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadRoleList()
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
      url: '/system/role/update',
      method: 'PUT',
      data: row
    })
    ElMessage.success('更新成功')
  } catch (error) {
    row.status = row.status === 1 ? 0 : 1
    ElMessage.error('更新失败')
  }
}

const handlePermission = async (row) => {
  currentRoleId.value = row.id
  try {
    const res = await request({ url: `/system/role/permissions/${row.id}`, method: 'GET' })
    if (res.code === 200) {
      checkedKeys.value = res.data || []
    }
  } catch (error) {
    console.error('加载权限失败', error)
  }
  permissionDialogVisible.value = true
}

const handlePermissionSubmit = async () => {
  const checkedNodes = permissionTreeRef.value.getCheckedNodes()
  const halfCheckedNodes = permissionTreeRef.value.getHalfCheckedNodes()
  const allChecked = [...checkedNodes, ...halfCheckedNodes].map(node => node.id)
  
  try {
    const res = await request({
      url: '/system/role/assign-permissions',
      method: 'POST',
      data: {
        roleId: currentRoleId.value,
        menuIds: allChecked
      }
    })
    if (res.code === 200) {
      ElMessage.success('分配成功')
      permissionDialogVisible.value = false
    }
  } catch (error) {
    ElMessage.error('分配失败')
  }
}

const handleSubmit = async () => {
  await formRef.value.validate(async (valid) => {
    if (valid) {
      const url = form.value.id ? '/system/role/update' : '/system/role/add'
      const method = form.value.id ? 'PUT' : 'POST'
      const res = await request({ url, method, data: form.value })
      if (res.code === 200) {
        ElMessage.success(form.value.id ? '更新成功' : '新增成功')
        dialogVisible.value = false
        loadRoleList()
      }
    }
  })
}

onMounted(() => {
  loadRoleList()
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
