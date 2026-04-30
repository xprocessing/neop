<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="page-header">
          <span>商品管理</span>
          <el-button type="primary" size="small">新增商品</el-button>
        </div>
      </template>
      <el-table :data="[]" border stripe>
        <el-table-column prop="name" label="商品名称" />
        <el-table-column prop="price" label="价格" />
        <el-table-column prop="stock" label="库存" />
        <el-table-column prop="sales" label="销量" />
        <el-table-column label="操作" width="200">
          <template #default>
            <el-button type="primary" link size="small">编辑</el-button>
            <el-button type="danger" link size="small">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
</script>
