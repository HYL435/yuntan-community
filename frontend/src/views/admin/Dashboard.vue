<template>
  <div class="dashboard-page">
    <div class="dashboard-topbar">
      <div>
        <h2 class="dashboard-title">管理仪表盘</h2>
        <p class="dashboard-subtitle">快速查看站点状态、待办事项与近期内容更新</p>
      </div>
      <el-button :loading="refreshing" @click="refreshDashboard">刷新数据</el-button>
    </div>

    <el-row :gutter="16" class="metric-grid">
      <el-col v-for="item in metricCards" :key="item.label" :xs="24" :sm="12" :lg="6">
        <el-card shadow="hover" class="metric-card" v-loading="loading">
          <div class="metric-label">{{ item.label }}</div>
          <div class="metric-value">{{ item.value }}</div>
          <div class="metric-hint">{{ item.hint }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="mt-4">
      <el-col :xs="24" :lg="16" class="mb-4">
        <el-card class="panel-card" shadow="never">
          <template #header>
            <div class="panel-header">
              <span>快捷操作</span>
            </div>
          </template>
          <div class="quick-actions">
            <el-button class="quick-primary-btn" type="primary" plain @click="go('/admin/articles/edit')">写新文章</el-button>
            <el-button plain @click="go('/admin/articles')">管理文章</el-button>
            <el-button plain @click="go('/admin/comment')">评论审核</el-button>
            <el-button plain @click="go('/admin/guestbook')">留言管理</el-button>
            <el-button plain @click="go('/admin/site-timeline')">建站历程</el-button>
            <el-button plain @click="go('/admin/users')">用户管理</el-button>
          </div>
        </el-card>

        <el-card class="panel-card mt-4" shadow="never">
          <template #header>
            <div class="panel-header">
              <span>最近更新文章</span>
              <el-button text @click="go('/admin/articles')">查看全部</el-button>
            </div>
          </template>
          <el-table
            :data="recentArticles"
            style="width: 100%"
            size="small"
            v-loading="loading"
            :header-cell-style="{ background: isDark ? '#1e293b' : '#f8fafc', color: isDark ? '#e2e8f0' : '#475569' }"
          >
            <el-table-column prop="title" label="标题" min-width="260" show-overflow-tooltip />
            <el-table-column prop="category" label="分类" width="120" />
            <el-table-column prop="status" label="状态" width="90" align="center">
              <template #default="{ row }">
                <el-tag :type="statusTagType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="updateTime" label="更新时间" width="180" align="center">
              <template #default="{ row }">{{ formatDate(row.updateTime) }}</template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="8" class="mb-4">
        <el-card class="panel-card" shadow="never">
          <template #header>
            <div class="panel-header">
              <span>待处理事项</span>
            </div>
          </template>
          <div class="todo-list" v-loading="loading">
            <div class="todo-item">
              <span>待审核评论</span>
              <el-tag type="warning">{{ pending.comment }}</el-tag>
            </div>
            <div class="todo-item">
              <span>待审核留言</span>
              <el-tag type="warning">{{ pending.danmaku }}</el-tag>
            </div>
            <div class="todo-item">
              <span>禁用历程节点</span>
              <el-tag type="info">{{ pending.timelineDisabled }}</el-tag>
            </div>
          </div>
        </el-card>

        <el-card class="panel-card mt-4" shadow="never">
          <template #header>
            <div class="panel-header">
              <span>系统信息</span>
            </div>
          </template>
          <div class="sys-info">
            <div class="sys-row"><span>主题模式</span><strong>{{ isDark ? '深色' : '浅色' }}</strong></div>
            <div class="sys-row"><span>最近刷新</span><strong>{{ lastRefreshText }}</strong></div>
            <div class="sys-row"><span>接口状态</span><strong>{{ apiHealthy ? '正常' : '部分异常' }}</strong></div>
          </div>
        </el-card>

        <el-card class="panel-card mt-4" shadow="never">
          <template #header>
            <div class="panel-header">
              <span>公告摘要</span>
            </div>
          </template>
          <p class="announcement-title">{{ announcement.title || '暂无公告' }}</p>
          <p class="announcement-content">{{ announcement.content || '当前没有可展示的公告内容。' }}</p>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import http from '@/api/http'
import { useIsDark } from '@/composables/useIsDark'
import { getAdminStats } from '@/api/stats'
import { getAdminAnnouncement } from '@/api/announcement'

type ArticleRow = {
  id?: number
  title?: string
  category?: string
  status?: number
  updateTime?: any
}

const router = useRouter()
const isDark = useIsDark()
const loading = ref(false)
const refreshing = ref(false)
const apiHealthy = ref(true)
const lastRefreshTime = ref<Date | null>(null)

const articleTotal = ref<number | null>(null)
const userTotal = ref<number | null>(null)
const weekVisits = ref<number | null>(null)
const recentArticles = ref<ArticleRow[]>([])
const announcement = reactive({ title: '', content: '' })
const pending = reactive({
  comment: 0,
  danmaku: 0,
  timelineDisabled: 0,
})

const formatNumber = (value: number | null) => (value == null ? '--' : value.toLocaleString())

const metricCards = computed(() => [
  { label: '总文章数', value: formatNumber(articleTotal.value), hint: '已创建文章总量' },
  { label: '用户总数', value: formatNumber(userTotal.value), hint: '后台分页统计结果' },
  { label: '近7日访问', value: formatNumber(weekVisits.value), hint: '来源于统计总览接口' },
  { label: '待处理总数', value: formatNumber(pending.comment + pending.danmaku), hint: '评论与留言待审核' },
])

const lastRefreshText = computed(() => {
  if (!lastRefreshTime.value) return '--'
  return lastRefreshTime.value.toLocaleString()
})

const go = (path: string) => {
  router.push(path).catch(() => {})
}

const statusText = (status?: number) => {
  if (status === 1) return '已发布'
  if (status === 0) return '草稿'
  if (status === 2) return '私密'
  return '-'
}

const statusTagType = (status?: number) => {
  if (status === 1) return 'success'
  if (status === 0) return 'warning'
  if (status === 2) return 'info'
  return ''
}

const formatDate = (v: any) => {
  if (!v && v !== 0) return '-'
  try {
    let d: Date
    if (Array.isArray(v)) {
      const [year, month = 1, day = 1, hour = 0, minute = 0, second = 0, nano = 0] = v.map((n: any) => Number(n) || 0)
      d = new Date(year, Math.max(month - 1, 0), day || 1, hour, minute, second, Math.floor(nano / 1_000_000))
    } else if (typeof v === 'object') {
      const year = Number(v?.year)
      const month = Number(v?.monthValue ?? v?.month ?? 1)
      const day = Number(v?.dayOfMonth ?? v?.day ?? 1)
      const hour = Number(v?.hour ?? 0)
      const minute = Number(v?.minute ?? 0)
      const second = Number(v?.second ?? 0)
      d = new Date(year, Math.max(month - 1, 0), day, hour, minute, second)
    } else if (typeof v === 'number') {
      d = new Date(v)
    } else {
      const raw = String(v).trim()
      const normalized = /^\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}/.test(raw) ? raw.replace(' ', 'T') : raw
      d = new Date(normalized)
    }
    if (Number.isNaN(d.getTime())) return '-'
    return d.toLocaleString()
  } catch {
    return '-'
  }
}

const getPageListAndTotal = (payload: any) => {
  const body = payload?.data ?? payload
  const records = body?.records ?? body?.list ?? body?.rows ?? []
  const total = Number(body?.total ?? body?.totalCount ?? (Array.isArray(records) ? records.length : 0)) || 0
  return {
    records: Array.isArray(records) ? records : [],
    total,
  }
}

const loadDashboardData = async () => {
  apiHealthy.value = true

  const tasks = await Promise.allSettled([
    http.get('/admin/articles/page', { params: { pageNo: 1, pageSize: 6 } }),
    http.get('/admin/users/page', { params: { current: 1, size: 1 } }),
    http.get('/admin/comments', { params: { pageNo: 1, pageSize: 1, status: 0 } }),
    http.get('/admin/danmaku/page', { params: { pageNo: 1, pageSize: 1, approved: 0 } }),
    http.get('/admin/timeline/list', { params: { pageNo: 1, pageSize: 1, status: 0 } }),
    getAdminStats(),
    getAdminAnnouncement(),
  ])

  const [articlesRes, usersRes, commentsRes, danmakuRes, timelineRes, statsRes, announcementRes] = tasks

  if (articlesRes.status === 'fulfilled') {
    const page = getPageListAndTotal(articlesRes.value.data || articlesRes.value)
    articleTotal.value = page.total
    recentArticles.value = page.records.map((item: any) => ({
      id: item?.id,
      title: item?.title,
      category: item?.category ?? '-',
      status: item?.status,
      updateTime: item?.updateTime,
    }))
  } else {
    apiHealthy.value = false
    articleTotal.value = null
    recentArticles.value = []
  }

  if (usersRes.status === 'fulfilled') {
    const page = getPageListAndTotal(usersRes.value.data || usersRes.value)
    userTotal.value = page.total
  } else {
    apiHealthy.value = false
    userTotal.value = null
  }

  if (commentsRes.status === 'fulfilled') {
    const page = getPageListAndTotal(commentsRes.value.data || commentsRes.value)
    pending.comment = page.total
  } else {
    apiHealthy.value = false
    pending.comment = 0
  }

  if (danmakuRes.status === 'fulfilled') {
    const page = getPageListAndTotal(danmakuRes.value.data || danmakuRes.value)
    pending.danmaku = page.total
  } else {
    apiHealthy.value = false
    pending.danmaku = 0
  }

  if (timelineRes.status === 'fulfilled') {
    const page = getPageListAndTotal(timelineRes.value.data || timelineRes.value)
    pending.timelineDisabled = page.total
  } else {
    apiHealthy.value = false
    pending.timelineDisabled = 0
  }

  if (statsRes.status === 'fulfilled') {
    const stats = statsRes.value || {}
    const visits = Array.isArray(stats?.visits) ? stats.visits : Array.isArray(stats?.series?.[0]?.data) ? stats.series[0].data : []
    weekVisits.value = visits.slice(-7).reduce((sum: number, n: any) => sum + (Number(n) || 0), 0)
  } else {
    apiHealthy.value = false
    weekVisits.value = null
  }

  if (announcementRes.status === 'fulfilled') {
    announcement.title = announcementRes.value?.title || ''
    announcement.content = announcementRes.value?.content || ''
  } else {
    apiHealthy.value = false
    announcement.title = ''
    announcement.content = ''
  }

  lastRefreshTime.value = new Date()
}

const refreshDashboard = async () => {
  refreshing.value = true
  loading.value = true
  try {
    await loadDashboardData()
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

onMounted(() => {
  refreshDashboard()
})
</script>

<style scoped>
.dashboard-page {
  padding: 12px;
  perspective: 1400px;
}

.dashboard-topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
}

.dashboard-title {
  margin: 0;
  font-size: 26px;
  font-weight: 800;
  color: #1f2937;
}

.dark .dashboard-title {
  color: #f1f5f9;
}

.dashboard-subtitle {
  margin: 4px 0 0;
  color: #64748b;
  font-size: 13px;
}

.dark .dashboard-subtitle {
  color: #94a3b8;
}

.metric-grid {
  margin-top: 2px;
}

.metric-card {
  border-radius: 12px;
  position: relative;
  overflow: hidden;
  transform-style: preserve-3d;
  transition: transform 0.34s cubic-bezier(0.22, 1, 0.36, 1), box-shadow 0.34s ease, border-color 0.34s ease;
  box-shadow: 0 12px 26px rgba(15, 23, 42, 0.08), 0 2px 0 rgba(255, 255, 255, 0.7) inset;
}

.metric-card::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.32), rgba(255, 255, 255, 0));
  opacity: 0;
  transition: opacity 0.28s ease;
  pointer-events: none;
}

.metric-card:hover {
  transform: translateY(-8px) rotateX(4deg) rotateY(-2deg);
  box-shadow: 0 18px 36px rgba(15, 23, 42, 0.14), 0 2px 0 rgba(255, 255, 255, 0.76) inset;
}

.metric-card:hover::before {
  opacity: 1;
}

.metric-label {
  font-size: 13px;
  color: #64748b;
}

.dark .metric-label {
  color: #94a3b8;
}

.metric-value {
  margin-top: 10px;
  font-size: 30px;
  line-height: 1.1;
  font-weight: 800;
  color: #0f172a;
}

.dark .metric-value {
  color: #f8fafc;
}

.metric-hint {
  margin-top: 8px;
  font-size: 12px;
  color: #94a3b8;
}

.panel-card {
  border-radius: 12px;
  position: relative;
  overflow: hidden;
  transform-style: preserve-3d;
  transition: transform 0.34s cubic-bezier(0.22, 1, 0.36, 1), box-shadow 0.34s ease;
  box-shadow: 0 10px 24px rgba(15, 23, 42, 0.06);
}

.panel-card::after {
  content: '';
  position: absolute;
  left: -22%;
  top: 0;
  width: 38%;
  height: 100%;
  background: linear-gradient(90deg, rgba(255, 255, 255, 0), rgba(255, 255, 255, 0.16), rgba(255, 255, 255, 0));
  transform: translateX(-180%);
  transition: transform 0.55s ease;
  pointer-events: none;
}

.panel-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 16px 34px rgba(15, 23, 42, 0.12);
}

.panel-card:hover::after {
  transform: translateX(420%);
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-weight: 700;
}

.quick-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.quick-actions :deep(.quick-primary-btn.el-button--primary.is-plain) {
  border-color: rgba(13, 148, 136, 0.4);
  color: #0f766e;
  background: rgba(13, 148, 136, 0.08);
}

.quick-actions :deep(.quick-primary-btn.el-button--primary.is-plain:hover) {
  background: rgba(13, 148, 136, 0.14);
  border-color: rgba(13, 148, 136, 0.52);
  color: #0f766e;
}

.dark .quick-actions :deep(.quick-primary-btn.el-button--primary) {
  --el-button-bg-color: #8b0f2f;
  --el-button-border-color: #8b0f2f;
  --el-button-hover-bg-color: #a41439;
  --el-button-hover-border-color: #a41439;
  --el-button-active-bg-color: #720925;
  --el-button-active-border-color: #720925;
  color: #f6e9ed;
  box-shadow: none;
}

.todo-list {
  display: grid;
  gap: 10px;
}

.todo-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 12px;
  border-radius: 10px;
  background: rgba(148, 163, 184, 0.08);
  transition: transform 0.26s ease, box-shadow 0.26s ease, background-color 0.26s ease;
}

.todo-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 20px rgba(15, 23, 42, 0.08);
  background: rgba(148, 163, 184, 0.14);
}

.dark .todo-item {
  background: rgba(148, 163, 184, 0.12);
}

.dark .metric-card {
  box-shadow: 0 16px 30px rgba(0, 0, 0, 0.44), inset 0 1px 0 rgba(255, 88, 120, 0.08);
}

.dark .metric-card:hover {
  box-shadow: 0 22px 38px rgba(0, 0, 0, 0.5), 0 0 0 1px rgba(255, 88, 120, 0.16) inset;
}

.dark .panel-card {
  box-shadow: 0 14px 30px rgba(0, 0, 0, 0.42), inset 0 1px 0 rgba(255, 88, 120, 0.06);
}

.dark .panel-card:hover {
  box-shadow: 0 20px 36px rgba(0, 0, 0, 0.5), 0 0 0 1px rgba(255, 88, 120, 0.14) inset;
}

.dark .todo-item:hover {
  background: rgba(255, 88, 120, 0.14);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.36);
}

@media (prefers-reduced-motion: reduce) {
  .metric-card,
  .panel-card,
  .todo-item {
    transition: none;
  }

  .metric-card:hover,
  .panel-card:hover,
  .todo-item:hover {
    transform: none;
  }
}

.sys-info {
  display: grid;
  gap: 10px;
}

.sys-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
}

.announcement-title {
  font-size: 15px;
  font-weight: 700;
  margin: 0 0 8px;
}

.announcement-content {
  font-size: 13px;
  line-height: 1.7;
  color: #64748b;
  margin: 0;
}

.dark .announcement-content {
  color: #94a3b8;
}
</style>
