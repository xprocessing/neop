import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'

// 基础布局
const Layout = () => import('@/layout/Index.vue')

// 路由配置
const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录', requiresAuth: false }
  },
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '数据看板', requiresAuth: true, permissions: ['dashboard:view'] }
      },
      // 个人中心
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/profile/index.vue'),
        meta: { title: '个人信息', requiresAuth: true }
      },
      {
        path: 'profile/password',
        name: 'ProfilePassword',
        component: () => import('@/views/profile/password.vue'),
        meta: { title: '修改密码', requiresAuth: true }
      },
      // 系统管理
      {
        path: 'system/menu',
        name: 'SysMenu',
        component: () => import('@/views/system/menu/index.vue'),
        meta: { title: '菜单管理', requiresAuth: true, permissions: ['system:menu:list'] }
      },
      {
        path: 'system/role',
        name: 'SysRole',
        component: () => import('@/views/system/role/index.vue'),
        meta: { title: '角色管理', requiresAuth: true, permissions: ['system:role:list'] }
      },
      {
        path: 'system/admin',
        name: 'SysAdmin',
        component: () => import('@/views/system/admin/index.vue'),
        meta: { title: '管理员管理', requiresAuth: true, permissions: ['system:admin:list'] }
      },
      {
        path: 'system/dict',
        name: 'SysDict',
        component: () => import('@/views/system/dict/index.vue'),
        meta: { title: '字典管理', requiresAuth: true, permissions: ['system:dict:list'] }
      },
      {
        path: 'system/config',
        name: 'SysConfig',
        component: () => import('@/views/system/config/index.vue'),
        meta: { title: '配置管理', requiresAuth: true, permissions: ['system:config:list'] }
      },
      {
        path: 'system/log',
        name: 'SysLog',
        component: () => import('@/views/system/log/index.vue'),
        meta: { title: '操作日志', requiresAuth: true, permissions: ['system:log:list'] }
      },
      {
        path: 'system/operation-log',
        name: 'SysOperationLog',
        component: () => import('@/views/system/operation-log/index.vue'),
        meta: { title: '操作日志明细', requiresAuth: true, permissions: ['system:operation-log:list'] }
      },
      {
        path: 'system/login-log',
        name: 'SysLoginLog',
        component: () => import('@/views/system/login-log/index.vue'),
        meta: { title: '登录日志', requiresAuth: true, permissions: ['system:login-log:list'] }
      },
      // 用户管理
      {
        path: 'user/list',
        name: 'UserList',
        component: () => import('@/views/user/index.vue'),
        meta: { title: '用户管理', requiresAuth: true, permissions: ['user:list'] }
      },
      // 营销管理
      {
        path: 'marketing/package',
        name: 'MemberPackage',
        component: () => import('@/views/marketing/package/index.vue'),
        meta: { title: '会员套餐', requiresAuth: true, permissions: ['marketing:package:list'] }
      },
      // 电商管理
      {
        path: 'shop/category',
        name: 'ProductCategory',
        component: () => import('@/views/shop/category/index.vue'),
        meta: { title: '商品分类', requiresAuth: true, permissions: ['shop:category:list'] }
      },
      {
        path: 'shop/product',
        name: 'Product',
        component: () => import('@/views/shop/product/index.vue'),
        meta: { title: '商品管理', requiresAuth: true, permissions: ['shop:product:list'] }
      },
      {
        path: 'shop/order',
        name: 'Order',
        component: () => import('@/views/shop/order/index.vue'),
        meta: { title: '订单管理', requiresAuth: true, permissions: ['shop:order:list'] }
      },
      // 任务管理
      {
        path: 'task/config',
        name: 'TaskConfig',
        component: () => import('@/views/task/config/index.vue'),
        meta: { title: '任务配置', requiresAuth: true, permissions: ['task:config:list'] }
      },
      {
        path: 'task/audit',
        name: 'TaskAudit',
        component: () => import('@/views/task/audit/index.vue'),
        meta: { title: '任务审核', requiresAuth: true, permissions: ['task:audit:list'] }
      },
      {
        path: 'task/paylog',
        name: 'TaskPayLog',
        component: () => import('@/views/task/paylog/index.vue'),
        meta: { title: '打款日志', requiresAuth: true, permissions: ['task:paylog:list'] }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - NEOP后台` : 'NEOP后台管理系统'
  
  // 判断是否需要登录
  if (to.meta.requiresAuth !== false) {
    const token = localStorage.getItem('token')
    if (!token) {
      next('/login')
      return
    }
    
    // 权限校验（简化版）
    const permissions = JSON.parse(localStorage.getItem('permissions') || '[]')
    if (to.meta.permissions && to.meta.permissions.length > 0) {
      const hasPermission = to.meta.permissions.some(p => permissions.includes(p))
      if (!hasPermission) {
        ElMessage.error('没有权限访问')
        next('/dashboard')
        return
      }
    }
  }
  
  next()
})

export default router
