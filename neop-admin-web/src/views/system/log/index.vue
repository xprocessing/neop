<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="page-header">
          <span>操作日志</span>
        </div>
      </template>
      <el-table :data="[]" border stripe>
        <el-table-column prop="operator" label="操作人" />
        <el-table-column prop="action" label="操作" />
        <el-table-column prop="target" label="操作对象" />
        <el-table-column prop="createTime" label="操作时间" width="180" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
</script>
