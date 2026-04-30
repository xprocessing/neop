<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="page-header">
          <span>任务配置</span>
          <el-button type="primary" size="small">新增任务</el-button>
        </div>
      </template>
      <el-table :data="[]" border stripe>
        <el-table-column prop="title" label="任务标题" />
        <el-table-column prop="reward" label="奖励" />
        <el-table-column prop="totalCount" label="总次数" />
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
