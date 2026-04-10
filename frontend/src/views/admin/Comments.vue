<template>
  <div class="p-4 bg-white dark:bg-[#1e293b] rounded-lg shadow-sm transition-colors duration-300">
    <div class="mb-4 flex justify-between items-center">
      <h2 class="text-xl font-bold text-gray-800 dark:text-white">评论管理</h2>
    </div>

    <el-form :inline="true" :model="queryParams" class="mb-4 flex flex-wrap gap-2" @submit.prevent>
      <el-form-item label="文章标题">
        <el-input v-model="queryParams.title" placeholder="文章标题" clearable class="!w-64" />
      </el-form-item>
      <el-form-item label="昵称">
        <el-input v-model="queryParams.nickname" placeholder="昵称" clearable class="!w-40" />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="queryParams.status" placeholder="全部" clearable class="!w-32">
          <el-option label="待审核" :value="0" />
          <el-option label="审核通过" :value="1" />
          <el-option label="审核拒绝" :value="2" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="tableData" border stripe style="width:100%" v-loading="loading"
      :header-cell-style="{ background: isDark ? '#1e293b' : '#f8fafc', color: isDark ? '#e2e8f0' : '#475569' }"
      @row-contextmenu="handleRowContextmenu">
      <el-table-column label="序号" width="80" align="center">
        <template #default="{ $index }">
          {{ (queryParams.current - 1) * queryParams.size + ($index + 1) }}
        </template>
      </el-table-column>
      <el-table-column prop="title" label="文章标题" min-width="200" show-overflow-tooltip />
      <el-table-column prop="nickname" label="昵称" width="140" />
      <el-table-column prop="content" label="内容" min-width="320" show-overflow-tooltip />
      <el-table-column prop="image" label="图片" width="120" align="center">
        <template #default="{ row }">
          <el-image v-if="row.image" :src="row.image" style="width:80px;height:48px;object-fit:cover" />
        </template>
      </el-table-column>
      <el-table-column prop="likeCount" label="点赞" width="80" align="center" />
      <el-table-column prop="status" label="状态" width="120" align="center">
        <template #default="{ row }">{{ statusText(row.status) }}</template>
      </el-table-column>
      <el-table-column prop="ip" label="IP" width="140" />
      <el-table-column prop="createTime" label="创建时间" width="180">
        <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
      </el-table-column>
    </el-table>

    <!-- 右键操作迷你悬浮框 -->
    <div
      v-if="contextMenuVisible"
      ref="menuRef"
      :style="contextMenuStyle"
      :class="['ctx-menu', menuIsDark ? 'ctx-menu--dark' : 'ctx-menu--light']"
      @click.stop
    >
      <button class="ctx-item" @click="onContextApprove">审核通过</button>
      <button class="ctx-item" @click="onContextReject">拒绝</button>
      <button class="ctx-item ctx-item--danger" @click="onContextDelete">删除评论</button>
    </div>

    <div class="mt-6 flex justify-end">
      <el-pagination background layout="total, sizes, prev, pager, next, jumper" :total="total"
        v-model:current-page="queryParams.current" v-model:page-size="queryParams.size" :page-sizes="[10,20,50]"
        @size-change="handleSizeChange" @current-change="handlePageChange" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '@/api/http'
import { updateCommentStatus, deleteComment as apiDeleteComment } from '@/api/comment'
import { useIsDark } from '@/composables/useIsDark'

type CommentAdmin = {
  id?: number
  articleId?: number
  title?: string
  userId?: number
  nickname?: string
  content?: string
  image?: string
  likeCount?: number
  status?: number
  ip?: string
  createTime?: string
  updateTime?: string
}

const loading = ref(false)
const tableData = ref<CommentAdmin[]>([])
const total = ref(0)
const isDark = useIsDark()

const queryParams = reactive({ current: 1, size: 10, title: '', nickname: '', status: undefined as number | undefined })

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
  } catch (e) {
    return '-'
  }
}

const fetchList = async () => {
  loading.value = true
  try {
    const params = {
      pageNo: queryParams.current,
      pageSize: queryParams.size,
      title: queryParams.title || undefined,
      nickname: queryParams.nickname || undefined,
      status: queryParams.status
    }
    const res = await http.get('/admin/comments', { params })
    const body = res.data || res
    const dataPayload = body.data || body
    const records = dataPayload.records || dataPayload.list || dataPayload.rows || []
    const totalCount = dataPayload.total || dataPayload.totalCount || records.length || 0
    tableData.value = Array.isArray(records)
      ? records.map((item: any) => ({
          ...item,
          createTime: item?.createTime ?? item?.createDate ?? item?.createdAt ?? item?.create_date ?? item?.gmtCreate,
          updateTime: item?.updateTime ?? item?.updateDate ?? item?.updatedAt ?? item?.update_date ?? item?.gmtModified
        }))
      : []
    total.value = Number(totalCount)
  } catch (err) {
    console.error('获取评论列表失败', err)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  queryParams.current = 1
  fetchList()
}
const handleReset = () => {
  queryParams.title = ''
  queryParams.nickname = ''
  queryParams.status = undefined
  handleSearch()
}
const handleSizeChange = (val: number) => {
  queryParams.size = val
  handleSearch()
}
const handlePageChange = (val: number) => {
  queryParams.current = val
  fetchList()
}

const statusText = (s: number | undefined) => {
  if (s === 0) return '待审核'
  if (s === 1) return '审核通过'
  if (s === 2) return '审核拒绝'
  return '-'
}

onMounted(() => {
  fetchList()
})

// 右键菜单相关
const menuRef = ref<HTMLElement | null>(null)
const contextMenuVisible = ref(false)
const contextMenuRow = ref<CommentAdmin | null>(null)
const contextMenuStyle = reactive<Record<string, string>>({ left: '0px', top: '0px', position: 'fixed' })
const menuIsDark = ref(false)

const hideContextMenu = () => {
  contextMenuVisible.value = false
  contextMenuRow.value = null
}

const _docClickHandler = () => hideContextMenu()

const handleRowContextmenu = (row: CommentAdmin, _column: any, event: MouseEvent) => {
  event.preventDefault()
  event.stopPropagation()
  menuIsDark.value = document.documentElement.classList.contains('dark')
  contextMenuRow.value = row
  let left = event.clientX
  let top = event.clientY
  contextMenuStyle.left = `${left}px`
  contextMenuStyle.top = `${top}px`
  contextMenuVisible.value = true
  nextTick(() => {
    try {
      const menuEl = menuRef.value as HTMLElement | null
      if (!menuEl) return
      const mRect = menuEl.getBoundingClientRect()
      const padding = 8
      if (left + mRect.width + padding > window.innerWidth) {
        left = Math.max(padding, window.innerWidth - mRect.width - padding)
      }
      if (top + mRect.height + padding > window.innerHeight) {
        top = Math.max(padding, window.innerHeight - mRect.height - padding)
      }
      left = Math.max(padding, left)
      top = Math.max(padding, top)
      contextMenuStyle.left = `${left}px`
      contextMenuStyle.top = `${top}px`
    } catch (err) {
      // ignore
    }
  })
}

onMounted(() => {
  document.addEventListener('click', _docClickHandler)
})
onUnmounted(() => {
  document.removeEventListener('click', _docClickHandler)
})

const onContextApprove = async () => {
  if (!contextMenuRow.value) return
  const row = contextMenuRow.value
  try {
    await updateCommentStatus({ id: row.id as number, status: 1 })
    ElMessage.success('已通过评论审核')
    fetchList()
  } catch (err: any) {
    ElMessage.error(err?.message || '操作失败')
  } finally {
    hideContextMenu()
  }
}

const onContextReject = async () => {
  if (!contextMenuRow.value) return
  const row = contextMenuRow.value
  try {
    await updateCommentStatus({ id: row.id as number, status: 2 })
    ElMessage.success('已拒绝该评论')
    fetchList()
  } catch (err: any) {
    ElMessage.error(err?.message || '操作失败')
  } finally {
    hideContextMenu()
  }
}

const onContextDelete = async () => {
  if (!contextMenuRow.value) return
  const row = contextMenuRow.value
  ElMessageBox.confirm(
    `确定要删除该评论吗？`,
    '删除评论',
    { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
  ).then(async () => {
    try {
      await apiDeleteComment(row.id as number)
      ElMessage.success('删除成功')
      if (tableData.value.length === 1 && queryParams.current > 1) queryParams.current--
      fetchList()
    } catch (err: any) {
      ElMessage.error(err?.message || '删除失败')
    }
  }).catch(() => {})
  hideContextMenu()
}
</script>

<style scoped>
.dark :deep(.el-table) {
  --el-table-tr-bg-color: #1e293b;
  --el-table-header-bg-color: #1e293b;
  --el-table-border-color: #334155;
  background-color: #1e293b;
  color: #e2e8f0;
}
.ctx-menu {
  z-index: 50;
  border-radius: 6px;
  padding: 4px 0;
  width: 11rem;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}
.ctx-menu--light { background-color: #ffffff; border: 1px solid #e5e7eb; }
.ctx-menu--dark  { background-color: #1e293b; border: 1px solid #334155; }
.ctx-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  border-radius: 4px;
  width: 100%;
  text-align: left;
  font-size: 0.875rem;
  cursor: pointer;
  background: transparent;
  border: none;
  transition: background-color 0.15s;
}
.ctx-menu--light .ctx-item { color: #374151; }
.ctx-menu--light .ctx-item:hover { background-color: #f3f4f6; }
.ctx-menu--dark  .ctx-item { color: #e2e8f0; }
.ctx-menu--dark  .ctx-item:hover { background-color: #0f172a; }
.ctx-item--danger { color: #ef4444 !important; }
</style>
