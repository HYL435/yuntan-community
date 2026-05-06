import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { recordPv } from '@/api/stats'
import { loadAdminSiteSettings } from '@/utils/adminSiteSettings'

// JWT 解析助手（仅解析 payload）
function stripBearer(t: string) {
  return t.replace(/^Bearer\s+/i, '')
}

function parseJwtPayload(token: string): any | null {
  try {
    const parts = token.split('.')
    if (parts.length < 2) return null
    const payload = parts[1]
    const base64 = payload.replace(/-/g, '+').replace(/_/g, '/')
    const padded = base64 + '='.repeat((4 - (base64.length % 4)) % 4)
    const json = atob(padded)
    return JSON.parse(json)
  } catch (e) {
    return null
  }
}

function getRoleFromPayload(payload: any): number {
  if (!payload) return Number.NaN
  let roleVal: any = null
  if (payload.role !== undefined) roleVal = payload.role
  else if (payload.roles !== undefined) roleVal = payload.roles
  else if (payload.authority !== undefined) roleVal = payload.authority
  else if (payload.authorities !== undefined) roleVal = payload.authorities

  if (Array.isArray(roleVal) && roleVal.length > 0) roleVal = roleVal[0]
  if (roleVal && typeof roleVal === 'object' && roleVal.role !== undefined) roleVal = roleVal.role
  return Number(roleVal)
}

function isAdminFromLocalToken(): boolean {
  const raw = localStorage.getItem('auth_token') || ''
  if (!raw) return false
  const token = stripBearer(raw)
  const payload = parseJwtPayload(token)
  const roleNum = getRoleFromPayload(payload)
  return !Number.isNaN(roleNum) && (roleNum === 0 || roleNum === 1)
}

const routes: Array<RouteRecordRaw> = [
  {
    path: '/',
    name: 'HomeView',
    component: () => import('@/views/HomeView.vue') // 懒加载首页组件
  },
  {
    path: '/login',
    name: 'LoginView',
    component: () => import('@/views/LoginView.vue')
  },
  {
    path: '/forgot-password',
    name: 'ForgotPasswordView',
    component: () => import('@/views/ForgotPasswordView.vue')
  },
  {
    path: '/register',
    name: 'RegisterView',
    component: () => import('@/views/RegisterView.vue')
  },
  {
    path: '/profile',
    name: 'ProfileView',
    component: () => import('@/views/ProfileView.vue')
  },
  {
    path: '/profile/edit',
    name: 'EditProfileView',
    component: () => import('@/views/EditProfileView.vue')
  },
  {
    path: '/settings',
    name: 'SettingsView',
    component: () => import('@/views/SettingsView.vue')
  },
  {
    path: '/bookshelf',
    name: 'BookshelfView',
    component: () => import('@/views/BookshelfView.vue')
  },
  {
    path: '/friend-links',
    name: 'FriendLinksView',
    component: () => import('@/views/FriendLinksView.vue')
  },
  {
    path: '/about/author',
    name: 'AboutAuthorView',
    component: () => import('@/views/AboutAuthorView.vue')
  },
  {
    path: '/about/site',
    name: 'AboutSiteView',
    component: () => import('@/views/AboutSiteView.vue')
  },
  {
    path: '/toolbox',
    name: 'ToolboxView',
    component: () => import('@/views/ToolboxView.vue')
  },
  {
    path: '/article/:id',
    name: 'ArticleDetailView',
    component: () => import('@/views/ArticleDetailView.vue')
  },
  {
    path: '/tag/:name',
    name: 'TagView',
    component: () => import('@/views/TagView.vue')
  },
  {
    path: '/message-board',
    name: 'MessageBoard',
    component: () => import('@/views/MessageBoardView.vue')
  },
  {
    path: '/topics',
    name: 'TopicsView',
    component: () => import('@/views/TopicsView.vue')
  },
  {
    path: '/network-error',
    name: 'NetworkError',
    component: () => import('@/views/NetworkErrorView.vue')
  },
  {
    path: '/categories',
    name: 'Categories',
    component: () => import('@/views/CategoriesListView.vue')
  },
  // 可以添加更多路由配置
  {
    path: '/admin',
    name: 'Admin',
    component: () => import('@/layouts/AdminLayout.vue'),
    redirect: '/admin/dashboard',
    children: [
      { path: 'dashboard', name: 'AdminDashboard', component: () => import('@/views/admin/Dashboard.vue') },
    { path: 'stats', name: 'AdminStats', component: () => import('@/views/admin/Stats.vue') },
      { path: 'articles', name: 'AdminArticles', component: () => import('@/views/admin/Articles.vue') },
      { path: 'comment', name: 'AdminComments', component: () => import('@/views/admin/Comments.vue') },
      { path: 'guestbook', name: 'AdminDanmaku', component: () => import('@/views/admin/Danmaku.vue') },
      { path: 'categories', name: 'AdminCategories', component: () => import('@/views/admin/Categories.vue') },
      { path: 'site-timeline', name: 'AdminSiteTimeline', component: () => import('@/views/admin/SiteTimeline.vue') },
      { path: 'tag', name: 'AdminTag', component: () => import('@/views/admin/Tags.vue') },
      { path: 'articles/edit', name: 'AdminArticleEdit', component: () => import('@/views/admin/ArticleEditView.vue') },
      { path: 'users', name: 'AdminUsers', component: () => import('@/views/admin/Users.vue') },
      { path: 'settings', name: 'AdminSettings', component: () => import('@/views/admin/Settings.vue') },
      { path: 'services', name: 'AdminServices', component: () => import('@/views/admin/SiteNotification.vue') }
    ]
  },
  // 必须放最后——匹配所有未定义路由，显示 404 页面
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFoundView.vue')
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(_to, _from, savedPosition) {
    if (savedPosition) return savedPosition
    return { top: 0, left: 0 }
  }
})

// 前台页面名称映射
const FRONT_PAGE_SET = new Set([
  'home', 'article', 'tag', 'message-board', 'about',
  'category', 'topics', 'search', 'profile', 'bookshelf', 'toolbox', 'settings'
])

function resolvePageName(path: string): string | null {
  if (path === '/') return 'home'
  if (path.startsWith('/article/')) return 'article'
  if (path.startsWith('/tag/')) return 'tag'
  if (path === '/message-board') return 'message-board'
  if (path.startsWith('/about')) return 'about'
  if (path === '/categories') return 'category'
  if (path === '/topics') return 'topics'
  if (path === '/search') return 'search'
  if (path === '/profile') return 'profile'
  if (path === '/bookshelf') return 'bookshelf'
  if (path === '/toolbox') return 'toolbox'
  if (path === '/settings') return 'settings'
  return null
}

router.afterEach((to) => {
  const page = resolvePageName(to.path)
  if (page && FRONT_PAGE_SET.has(page)) {
    recordPv(page)
  }
})

// 全局路由守卫：限制后台与关于博主页面，仅管理员可访问
router.beforeEach((to, _from, next) => {
  if (to.path.startsWith('/admin') || to.path === '/about/author') {
    if (isAdminFromLocalToken()) {
      const services = loadAdminSiteSettings().services
      if (to.path === '/admin/users' && !services.userManagementEnabled) {
        next({ path: '/admin/services' })
        return
      }
      if (to.path === '/admin/stats' && !services.statsServiceEnabled) {
        next({ path: '/admin/services' })
        return
      }
      if ((to.path === '/admin/comment' || to.path === '/admin/guestbook') && !services.interactionServiceEnabled) {
        next({ path: '/admin/services' })
        return
      }
      next()
    } else {
      next({ path: '/' })
    }
    return
  }
  next()
})

export default router