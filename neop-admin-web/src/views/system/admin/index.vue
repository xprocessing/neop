<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="page-header">
          <span>管理员管理</span>
          <el-button type="primary" size="small">新增管理员</el-button>
        </div>
      </template>
      <el-table :data="[]" border stripe>
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="nickname" label="昵称" />
        <el-table-column prop="status" label="状态" />
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
