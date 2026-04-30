<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="page-header">
          <span>打款日志</span>
        </div>
      </template>
      <el-table :data="[]" border stripe>
        <el-table-column prop="tradeNo" label="交易编号" />
        <el-table-column prop="amount" label="金额" />
        <el-table-column prop="status" label="状态" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
</script>
