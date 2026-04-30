<template>
  <div class="app-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>商品管理</span>
          <el-button type="primary" @click="handleAdd">新增商品</el-button>
        </div>
      </template>

      <!-- 搜索栏 -->
      <el-form :inline="true" :model="queryParams" class="search-form">
        <el-form-item label="商品名称">
          <el-input v-model="queryParams.title" placeholder="请输入商品名称" clearable />
        </el-form-item>
        <el-form-item label="商品分类">
          <el-select v-model="queryParams.categoryId" placeholder="请选择分类" clearable>
            <el-option v-for="cat in categoryList" :key="cat.id" :label="cat.name" :value="cat.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
            <el-option label="上架" :value="1" />
            <el-option label="下架" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 商品列表 -->
      <el-table v-loading="loading" :data="goodsList" border>
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column label="商品图片" width="100" align="center">
          <template #default="{ row }">
            <el-image v-if="row.mainImage" :src="row.mainImage" style="width: 60px; height: 60px" />
          </template>
        </el-table-column>
        <el-table-column prop="title" label="商品名称" min-width="200" show-overflow-tooltip />
        <el-table-column prop="categoryName" label="分类" width="120" align="center" />
        <el-table-column prop="price" label="售价" width="120" align="center">
          <template #default="{ row }">
            <span style="color: #f5222d; font-weight: bold">¥{{ row.price }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="originalPrice" label="原价" width="120" align="center">
          <template #default="{ row }">
            <span style="text-decoration: line-through; color: #999">¥{{ row.originalPrice }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="100" align="center" />
        <el-table-column prop="salesCount" label="销量" width="100" align="center" sortable />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-switch v-model="row.status" :active-value="1" :inactive-value="0" @change="handleStatusChange(row)" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="text" @click="handleView(row)">查看</el-button>
            <el-button type="text" @click="handleEdit(row)">编辑</el-button>
            <el-button type="text" @click="handleDelete(row)" style="color: #f56c6c">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
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

    <!-- 新增/编辑对话框 -->
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="800px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="商品名称" prop="title">
          <el-input v-model="form.title" placeholder="请输入商品名称" />
        </el-form-item>
        <el-form-item label="商品分类" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="请选择分类">
            <el-option v-for="cat in categoryList" :key="cat.id" :label="cat.name" :value="cat.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="商品主图" prop="mainImage">
          <el-input v-model="form.mainImage" placeholder="请输入主图URL" />
        </el-form-item>
        <el-form-item label="商品图片">
          <el-input v-model="imagesInput" placeholder="请输入图片URL，多个用逗号分隔" />
        </el-form-item>
        <el-form-item label="售价" prop="price">
          <el-input-number v-model="form.price" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="原价" prop="originalPrice">
          <el-input-number v-model="form.originalPrice" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="库存" prop="stock">
          <el-input-number v-model="form.stock" :min="0" />
        </el-form-item>
        <el-form-item label="商品描述">
          <el-input v-model="form.description" type="textarea" placeholder="请输入商品描述" />
        </el-form-item>
        <el-form-item label="商品详情">
          <el-input v-model="form.detail" type="textarea" :rows="6" placeholder="请输入商品详情" />
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
const goodsList = ref([])
const categoryList = ref([])
const categoryNameMap = ref({})
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref()
const imagesInput = ref('')

const queryParams = ref({
  pageNum: 1,
  pageSize: 10,
  title: '',
  categoryId: null,
  status: null
})

const form = ref({
  id: null,
  title: '',
  categoryId: null,
  mainImage: '',
  images: [],
  price: 0,
  originalPrice: 0,
  stock: 0,
  description: '',
  detail: '',
  status: 1
})

const rules = {
  title: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择商品分类', trigger: 'change' }],
  price: [{ required: true, message: '请输入售价', trigger: 'blur' }]
}

const loadGoodsList = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: queryParams.value.pageNum,
      pageSize: queryParams.value.pageSize,
      productName: queryParams.value.title || undefined,
      status: queryParams.value.status !== null ? queryParams.value.status : undefined
    }
    const res = await request({
      url: '/admin/shop/goods/list',
      method: 'GET',
      params
    })
    if (res.code === 200) {
      goodsList.value = (res.data.records || []).map(item => ({
        id: item.id,
        title: item.productName,
        categoryId: item.categoryId,
        categoryName: categoryNameMap.value[item.categoryId] || '',
        mainImage: item.productImg || '',
        images: item.productImages ? item.productImages.split(',') : [],
        price: item.price,
        originalPrice: item.originalPrice,
        stock: item.stock,
        salesCount: item.sales || 0,
        description: '',
        detail: item.content || '',
        status: item.status,
        createTime: item.createTime
      }))
      total.value = res.data.total
    }
  } finally {
    loading.value = false
  }
}

const flattenTree = (nodes) => {
  let result = []
  for (const node of nodes) {
    result.push(node)
    if (node.children && node.children.length) {
      result = result.concat(flattenTree(node.children))
    }
  }
  return result
}

const loadCategoryList = async () => {
  try {
    const res = await request({ url: '/admin/shop/category/tree', method: 'GET' })
    if (res.code === 200) {
      const flat = flattenTree(res.data || [])
      categoryList.value = flat
      flat.forEach(cat => { categoryNameMap.value[cat.id] = cat.name })
    }
  } catch (error) {
    console.error('加载分类失败', error)
  }
}

const handleAdd = () => {
  form.value = { id: null, title: '', categoryId: null, mainImage: '', images: [], price: 0, originalPrice: 0, stock: 0, description: '', detail: '', status: 1 }
  imagesInput.value = ''
  dialogTitle.value = '新增商品'
  dialogVisible.value = true
}

const handleEdit = (row) => {
  form.value = { ...row }
  imagesInput.value = row.images ? row.images.join(',') : ''
  dialogTitle.value = '编辑商品'
  dialogVisible.value = true
}

const handleView = (row) => {
  // 查看商品详情，可以跳转到详情页
  console.log('查看商品', row)
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除该商品吗？', '提示', { type: 'warning' })
    const res = await request({ url: `/admin/shop/goods/delete?id=${row.id}`, method: 'post' })
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadGoodsList()
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
      url: '/admin/shop/goods/update',
      method: 'post',
      data: row
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
      const url = form.value.id ? '/admin/shop/goods/update' : '/admin/shop/goods/save'
      const postData = {
        ...form.value,
        productName: form.value.title,
        productImg: form.value.mainImage,
        productImages: imagesInput.value ? imagesInput.value.split(',').join(',') : '',
        content: form.value.detail
      }
      delete postData.title
      delete postData.mainImage
      delete postData.images
      delete postData.description
      delete postData.detail
      delete postData.salesCount
      delete postData.categoryName
      delete postData.createTime
      const res = await request({ url, method: 'post', data: postData })
      if (res.code === 200) {
        ElMessage.success(form.value.id ? '更新成功' : '新增成功')
        dialogVisible.value = false
        loadGoodsList()
      }
    }
  })
}

const handleQuery = () => {
  queryParams.value.pageNum = 1
  loadGoodsList()
}

const resetQuery = () => {
  queryParams.value = {
    pageNum: 1,
    pageSize: 10,
    title: '',
    categoryId: null,
    status: null
  }
  loadGoodsList()
}

const handleSizeChange = (val) => {
  queryParams.value.pageSize = val
  loadGoodsList()
}

const handleCurrentChange = (val) => {
  queryParams.value.pageNum = val
  loadGoodsList()
}

onMounted(async () => {
  await loadCategoryList()
  loadGoodsList()
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
</style>
