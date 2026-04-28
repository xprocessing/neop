<template>
  <el-container class="layout-container">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapse ? '64px' : '200px'" class="layout-aside">
      <div class="logo">
        <h3 v-if="!isCollapse">NEOP后台</h3>
        <h3 v-else>N</h3>
      </div>
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        router
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409eff"
      >
        <template v-for="menu in menuList" :key="menu.path">
          <el-submenu v-if="menu.children && menu.children.length > 0" :index="menu.path">
            <template #title>
              <el-icon><component :is="menu.icon" /></el-icon>
              <span>{{ menu.title }}</span>
            </template>
            <el-menu-item v-for="child in menu.children" :key="child.path" :index="child.path">
              <el-icon><component :is="child.icon" /></el-icon>
              <span>{{ child.title }}</span>
            </el-menu-item>
          </el-submenu>
          <el-menu-item v-else :index="menu.path">
            <el-icon><component :is="menu.icon" /></el-icon>
            <span>{{ menu.title }}</span>
          </el-menu-item>
        </template>
      </el-menu>
    </el-aside>
    
    <!-- 主内容区 -->
    <el-container>
      <!-- 头部 -->
      <el-header class="layout-header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="isCollapse = !isCollapse">
            <Fold v-if="!isCollapse" />
            <Expand v-else />
          </el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item>{{ currentRoute.meta.title }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-avatar :size="32">{{ userInfo.nickname?.charAt(0) }}</el-avatar>
              <span>{{ userInfo.nickname }}</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      
      <!-- 内容区 -->
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Fold, Expand } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const isCollapse = ref(false)
const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'))

const activeMenu = computed(() => route.path)
const currentRoute = computed(() => route)

// 菜单列表（简化版，实际应从后端动态获取）
const menuList = ref([
  {
    path: '/dashboard',
    title: '数据看板',
    icon: 'DataBoard'
  },
  {
    path: '/system',
    title: '系统管理',
    icon: 'Setting',
    children: [
      { path: '/system/menu', title: '菜单管理', icon: 'Menu' },
      { path: '/system/role', title: '角色管理', icon: 'UserFilled' },
      { path: '/system/admin', title: '管理员管理', icon: 'Avatar' },
      { path: '/system/dict', title: '字典管理', icon: 'Notebook' },
      { path: '/system/config', title: '配置管理', icon: 'Setting' },
      { path: '/system/log', title: '操作日志', icon: 'Document' }
    ]
  },
  {
    path: '/user/list',
    title: '用户管理',
    icon: 'User'
  },
  {
    path: '/marketing',
    title: '营销管理',
    icon: 'Promotion',
    children: [
      { path: '/marketing/package', title: '会员套餐', icon: 'Box' }
    ]
  },
  {
    path: '/shop',
    title: '电商管理',
    icon: 'ShoppingCart',
    children: [
      { path: '/shop/category', title: '商品分类', icon: 'Folder' },
      { path: '/shop/product', title: '商品管理', icon: 'Goods' },
      { path: '/shop/order', title: '订单管理', icon: 'List' }
    ]
  },
  {
    path: '/task',
    title: '任务管理',
    icon: 'Tickets',
    children: [
      { path: '/task/config', title: '任务配置', icon: 'Setting' },
      { path: '/task/audit', title: '任务审核', icon: 'Checked' },
      { path: '/task/paylog', title: '打款日志', icon: 'Money' }
    ]
  }
])

const handleCommand = (command) => {
  if (command === 'logout') {
    localStorage.removeItem('token')
    localStorage.removeItem('refreshToken')
    localStorage.removeItem('userInfo')
    localStorage.removeItem('permissions')
    router.push('/login')
  }
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
}

.layout-aside {
  background-color: #304156;
  transition: width 0.3s;
  overflow: hidden;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  background-color: #263445;
}

.logo h3 {
  margin: 0;
  font-size: 18px;
}

.layout-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 15px;
}

.collapse-btn {
  font-size: 20px;
  cursor: pointer;
}

.header-right {
  margin-right: 20px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
}
</style>
