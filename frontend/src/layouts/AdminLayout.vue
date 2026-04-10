<template>
  <div class="common-layout admin-theme h-screen w-full overflow-hidden flex bg-[#f6f7fb] dark:bg-[#070707] text-gray-700 dark:text-gray-200 transition-colors duration-300">
    <!-- 左侧菜单栏 -->
    <el-aside 
      :width="isCollapse ? '64px' : '240px'" 
      class="h-full bg-white dark:bg-[#0b0b0b] border-r border-gray-200 dark:border-[#2f0d14] transition-all duration-300 flex flex-col"
    >
      <!-- Logo -->
      <div class="h-16 flex items-center justify-center border-b border-gray-100 dark:border-[#2f0d14]">
        <div class="flex items-center gap-2 font-bold text-xl text-primary truncate px-4">
          <div class="w-8 h-8 rounded bg-teal-600 dark:bg-[#d20f3b] flex items-center justify-center text-white shrink-0">
            <el-icon><Management /></el-icon>
          </div>
          <span v-show="!isCollapse" class="transition-opacity duration-300">后台管理</span>
        </div>
      </div>
      <!-- 菜单 -->
      <el-scrollbar class="flex-1">
        <el-menu
          :default-active="activeMenu"
          :collapse="isCollapse"
          class="border-none bg-transparent! py-2"
          :text-color="isDark ? '#b8b8b8' : '#5b6272'"
          :active-text-color="isDark ? '#ff4d73' : '#0f766e'"
          unique-opened
          router
        >
          <el-menu-item index="/admin/dashboard">
            <el-icon><Odometer /></el-icon>
            <template #title>仪表盘</template>
          </el-menu-item>
          <el-menu-item index="/admin/stats">
            <el-icon><DataAnalysis /></el-icon>
            <template #title>统计</template>
          </el-menu-item>
          <el-sub-menu index="content">
            <template #title>
              <el-icon><Document /></el-icon>
              <span>内容管理</span>
            </template>
            <el-menu-item index="/admin/articles">文章管理</el-menu-item>
            <el-menu-item index="/admin/categories">分类管理</el-menu-item>
            <el-menu-item index="/admin/site-timeline">建站历程</el-menu-item>
            <el-menu-item index="/admin/tag">标签管理</el-menu-item>
          </el-sub-menu>
          <el-sub-menu index="interaction">
            <template #title>
              <el-icon><ChatLineRound /></el-icon>
              <span>互动管理</span>
            </template>
            <el-menu-item index="/admin/comment">评论管理</el-menu-item>
            <el-menu-item index="/admin/guestbook">留言管理</el-menu-item>
          </el-sub-menu>
          <el-menu-item index="/admin/users">
            <el-icon><User /></el-icon>
            <template #title>用户管理</template>
          </el-menu-item>
          <el-menu-item index="/admin/settings">
            <el-icon><Setting /></el-icon>
            <template #title>系统设置</template>
          </el-menu-item>
        </el-menu>
      </el-scrollbar>
    </el-aside>

    <!-- 右侧主内容 -->
    <div class="flex-1 flex flex-col min-w-0">
      <!-- 顶部 Header -->
      <header class="h-16 bg-white dark:bg-[#0b0b0b] border-b border-gray-200 dark:border-[#2f0d14] flex items-center justify-between px-6 transition-colors duration-300">
        <div class="flex items-center gap-4">
          <el-button circle text @click="isCollapse = !isCollapse">
            <el-icon :size="20"><component :is="isCollapse ? Expand : Fold" /></el-icon>
          </el-button>
          <el-breadcrumb separator="/" class="hidden md:flex">
            <el-breadcrumb-item>首页</el-breadcrumb-item>
            <el-breadcrumb-item>{{ breadcrumbName }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="flex items-center gap-4">
          <el-button class="admin-front-btn" type="primary" link @click="goFront">
            <el-icon class="mr-1"><HomeFilled /></el-icon> 返回前台
          </el-button>
          <el-divider direction="vertical" />
          <el-button circle text @click="toggleTheme">
            <el-icon :size="20" class="text-gray-600 dark:text-red-400">
              <component :is="isDark ? Sunny : Moon" />
            </el-icon>
          </el-button>
          <el-dropdown>
            <div class="flex items-center gap-2 cursor-pointer">
              <el-avatar :size="32" src="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png" />
              <span class="text-sm font-medium hidden sm:block">管理员</span>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item>个人中心</el-dropdown-item>
                <el-dropdown-item divided class="text-red-500" @click="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>
      <!-- 主内容区 -->
      <main class="flex-1 overflow-auto p-6 relative">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup lang="ts" name="AdminLayout">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import {
  Management, Expand, Fold, HomeFilled, Odometer, Document, ChatLineRound, User, Setting, Sunny, Moon, DataAnalysis
} from '@element-plus/icons-vue'

const isCollapse = ref(false)
const isDark = ref(false)
const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)
const breadcrumbName = computed(() => {
  // 可根据 route.name 或 path 定制面包屑
    const map: Record<string, string> = {
    '/admin/dashboard': '仪表盘',
    '/admin/articles': '文章管理',
    '/admin/categories': '分类管理',
    '/admin/site-timeline': '建站历程管理',
    '/admin/tag': '标签管理',
    '/admin/comment': '评论管理',
    '/admin/guestbook': '留言管理',
    '/admin/stats': '统计',
    '/admin/users': '用户管理',
    '/admin/settings': '系统设置'
  }
  return map[route.path] || '仪表盘'
})

const logout = () => {
  userStore.logout()
  router.push('/').catch(() => {})
}
const goFront = () => {
  router.push('/').catch(() => {})
}
const toggleTheme = () => {
  isDark.value = !isDark.value
  applyTheme()
}
const applyTheme = () => {
  const html = document.documentElement
  if (isDark.value) {
    html.classList.add('dark')
    localStorage.setItem('theme', 'dark')
  } else {
    html.classList.remove('dark')
    localStorage.setItem('theme', 'light')
  }
}
onMounted(() => {
  const savedTheme = localStorage.getItem('theme')
  const systemDark = window.matchMedia('(prefers-color-scheme: dark)').matches
  isDark.value = savedTheme === 'dark' || (!savedTheme && systemDark)
  applyTheme()
})
</script>

<style scoped>
/* 定义淡入动画 */
.animate-fade-in {
  animation: fadeIn 0.3s ease-out;
}
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.admin-theme {
  position: relative;
}

.admin-theme::before {
  content: '';
  position: absolute;
  inset: 0;
  pointer-events: none;
  background: radial-gradient(circle at 10% -20%, rgba(20, 184, 166, 0.12), transparent 42%);
}

.dark .admin-theme::before {
  background: radial-gradient(circle at 10% -20%, rgba(239, 68, 68, 0.1), transparent 42%);
}

.admin-front-btn {
  color: #0f766e;
}

.dark .admin-front-btn {
  color: #ff5e82;
}

.admin-theme :deep(.el-main),
.admin-theme :deep(main) {
  background: transparent;
}

.admin-theme :deep(.el-card) {
  border-radius: 12px;
  border: 1px solid rgba(15, 23, 42, 0.06);
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.06);
}

.admin-theme :deep(.el-menu-item.is-active) {
  background: linear-gradient(90deg, rgba(13, 148, 136, 0.14), rgba(13, 148, 136, 0.04));
}

.admin-theme :deep(.el-button--primary) {
  --el-button-bg-color: #0f766e;
  --el-button-border-color: #0f766e;
  --el-button-hover-bg-color: #0d9488;
  --el-button-hover-border-color: #0d9488;
  --el-button-active-bg-color: #115e59;
  --el-button-active-border-color: #115e59;
}

.admin-theme :deep(.el-pagination .is-active) {
  background-color: #0f766e;
  border-color: #0f766e;
}

.dark .admin-theme :deep(.el-aside) {
  box-shadow: inset -1px 0 0 rgba(255, 68, 105, 0.18);
}

.dark .admin-theme :deep(.el-card) {
  background: linear-gradient(180deg, #101010 0%, #0b0b0b 100%);
  border: 1px solid rgba(255, 68, 105, 0.16);
  box-shadow: 0 10px 28px rgba(0, 0, 0, 0.42), inset 0 1px 0 rgba(255, 68, 105, 0.08);
}

.dark .admin-theme :deep(.el-table) {
  --el-table-tr-bg-color: #0f0f10;
  --el-table-header-bg-color: #131314;
  --el-table-border-color: rgba(255, 68, 105, 0.18);
  --el-table-row-hover-bg-color: rgba(255, 68, 105, 0.08);
  color: #ececec;
}

.dark .admin-theme :deep(.el-table th.el-table__cell) {
  color: #e7e7e7;
}

.dark .admin-theme :deep(.el-table td.el-table__cell) {
  border-bottom-color: rgba(255, 68, 105, 0.14);
}

.dark .admin-theme :deep(.el-button--primary) {
  --el-button-bg-color: #d20f3b;
  --el-button-border-color: #d20f3b;
  --el-button-hover-bg-color: #ea1f4f;
  --el-button-hover-border-color: #ea1f4f;
  --el-button-active-bg-color: #af0a30;
  --el-button-active-border-color: #af0a30;
}

.dark .admin-theme :deep(.el-pagination .is-active) {
  background-color: #d20f3b;
  border-color: #d20f3b;
}

.dark .admin-theme :deep(.el-menu-item.is-active) {
  background: linear-gradient(90deg, rgba(255, 68, 105, 0.22), rgba(255, 68, 105, 0.06));
  box-shadow: inset 2px 0 0 #ff4469;
}

.dark .admin-theme :deep(.el-sub-menu__title:hover),
.dark .admin-theme :deep(.el-menu-item:hover) {
  background-color: rgba(255, 68, 105, 0.1) !important;
}

.dark .admin-theme :deep(.el-input__wrapper),
.dark .admin-theme :deep(.el-textarea__inner),
.dark .admin-theme :deep(.el-select__wrapper) {
  background-color: #111112;
  box-shadow: 0 0 0 1px rgba(255, 68, 105, 0.14) inset;
}

.dark .admin-theme :deep(.el-dialog) {
  background: #0f0f10;
  border: 1px solid rgba(255, 68, 105, 0.2);
}

.dark .admin-theme :deep(.el-dialog__title),
.dark .admin-theme :deep(.el-form-item__label) {
  color: #f1f1f1;
}
</style>
