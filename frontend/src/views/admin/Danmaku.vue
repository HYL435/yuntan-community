<template>
  <div class="p-4 bg-white dark:bg-[#1e293b] rounded-lg shadow-sm transition-colors duration-300">
    <div class="mb-4 flex justify-between items-center">
      <h2 class="text-xl font-bold text-gray-800 dark:text-white">弹幕管理</h2>
    </div>

    <el-form :inline="true" :model="queryParams" class="mb-4 flex flex-wrap gap-2" @submit.prevent>
      <el-form-item label="作者">
        <el-input v-model="queryParams.author" placeholder="作者昵称" clearable class="w-48!" />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="queryParams.approved" placeholder="全部" clearable class="w-36!">
          <el-option label="待审核/未通过" :value="0" />
          <el-option label="已通过" :value="1" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table
      :data="tableData"
      border
      stripe
      style="width: 100%"
      v-loading="loading"
      :header-cell-style="{ background: isDark ? '#1e293b' : '#f8fafc', color: isDark ? '#e2e8f0' : '#475569' }"
      @row-contextmenu="handleRowContextmenu"
    >
      <el-table-column label="序号" width="80" align="center">
        <template #default="{ $index }">
          {{ (queryParams.current - 1) * queryParams.size + ($index + 1) }}
        </template>
      </el-table-column>
      <el-table-column prop="content" label="弹幕内容" min-width="280" show-overflow-tooltip />
      <el-table-column label="颜色" width="96" align="center">
        <template #default="{ row }">
          <div class="mx-auto h-6 w-10 rounded border border-gray-200 dark:border-gray-600" :style="{ backgroundColor: safeColor(row.color) }" />
        </template>
      </el-table-column>
      <el-table-column prop="author" label="作者" width="140" show-overflow-tooltip>
        <template #default="{ row }">
          {{ row.author || '游客' }}
        </template>
      </el-table-column>
      <el-table-column prop="userId" label="用户ID" width="120" align="center">
        <template #default="{ row }">
          {{ row.userId ?? '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="timePoint" label="时间点" width="120" align="center">
        <template #default="{ row }">
          {{ formatTimePoint(row.timePoint) }}
        </template>
      </el-table-column>
      <el-table-column prop="ipAddress" label="IP" width="150" show-overflow-tooltip />
      <el-table-column prop="approved" label="状态" width="120" align="center">
        <template #default="{ row }">
          <el-tag :type="row.approved === 1 ? 'success' : 'warning'" effect="dark">
            {{ statusText(row.approved) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" show-overflow-tooltip>
        <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
      </el-table-column>
    </el-table>

    <div
      v-if="contextMenuVisible"
      ref="menuRef"
      :style="contextMenuStyle"
      :class="['ctx-menu', menuIsDark ? 'ctx-menu--dark' : 'ctx-menu--light']"
      @click.stop
    >
      <button class="ctx-item" @click="onContextApprove">审核通过</button>
      <button class="ctx-item" @click="onContextReject">标记待审核</button>
      <button class="ctx-item ctx-item--danger" @click="onContextDelete">删除弹幕</button>
    </div>

    <div class="mt-6 flex justify-end">
      <el-pagination
        background
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        v-model:current-page="queryParams.current"
        v-model:page-size="queryParams.size"
        :page-sizes="[10, 20, 50]"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { nextTick, onMounted, onUnmounted, reactive, ref, type CSSProperties } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { deleteDanmakuAdmin, listDanmakuAdmin, updateDanmakuStatus } from '@/api/danmaku'
import { useIsDark } from '@/composables/useIsDark'

type AdminDanmakuItem = {
  id?: number | string
  content?: string
  color?: string
  author?: string
  userId?: number | string | null
  timePoint?: number | null
  createTime?: string
  ipAddress?: string
  approved?: number
}

const loading = ref(false)
const tableData = ref<AdminDanmakuItem[]>([])
const total = ref(0)
const isDark = useIsDark()

const queryParams = reactive({
  current: 1,
  size: 10,
  author: '',
  approved: undefined as number | undefined,
})

const menuRef = ref<HTMLElement | null>(null)
const contextMenuVisible = ref(false)
const contextMenuRow = ref<AdminDanmakuItem | null>(null)
const contextMenuStyle = reactive<CSSProperties>({ left: '0px', top: '0px', position: 'fixed' })
const menuIsDark = ref(false)

const normalizePageResponse = (payload: any) => {
  const body = payload?.data ?? payload
  const records = body?.records ?? body?.list ?? body?.rows ?? []
  const totalCount = body?.total ?? body?.totalCount ?? records.length ?? 0
  return {
    records: Array.isArray(records) ? records : [],
    total: Number(totalCount) || 0,
  }
}

const fetchList = async () => {
  loading.value = true
  try {
    const res = await listDanmakuAdmin({
      pageNo: queryParams.current,
      pageSize: queryParams.size,
      author: queryParams.author || undefined,
      approved: queryParams.approved,
      sortBy: 'createTime',
      isAsc: false,
    })
    const { records, total: totalCount } = normalizePageResponse(res.data || res)
    tableData.value = records.map((item: any) => ({
      ...item,
      createTime: item?.createTime ?? item?.createDate ?? item?.createdAt ?? item?.create_date ?? item?.gmtCreate
    }))
    total.value = totalCount
  } catch (error) {
    console.error('获取弹幕列表失败', error)
    ElMessage.error('获取弹幕列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  queryParams.current = 1
  fetchList()
}

const handleReset = () => {
  queryParams.author = ''
  queryParams.approved = undefined
  handleSearch()
}

const handleSizeChange = (value: number) => {
  queryParams.size = value
  handleSearch()
}

const handlePageChange = (value: number) => {
  queryParams.current = value
  fetchList()
}

const statusText = (approved: number | undefined) => {
  if (approved === 1) return '已通过'
  if (approved === 0) return '待审核/未通过'
  return '-'
}

const safeColor = (color?: string) => {
  if (!color) return '#ffffff'
  return /^#([0-9a-fA-F]{3}|[0-9a-fA-F]{6})$/.test(color) ? color : '#ffffff'
}

const formatTimePoint = (timePoint?: number | null) => {
  if (typeof timePoint !== 'number' || Number.isNaN(timePoint) || timePoint < 0) return '-'
  const totalSeconds = Math.floor(timePoint / 1000)
  const minutes = Math.floor(totalSeconds / 60)
  const seconds = totalSeconds % 60
  return `${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`
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
  } catch (e) {
    return '-'
  }
}

const hideContextMenu = () => {
  contextMenuVisible.value = false
  contextMenuRow.value = null
}

const documentClickHandler = () => hideContextMenu()

const handleRowContextmenu = (row: AdminDanmakuItem, _column: unknown, event: MouseEvent) => {
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
    const menuElement = menuRef.value
    if (!menuElement) return

    const rect = menuElement.getBoundingClientRect()
    const padding = 8
    if (left + rect.width + padding > window.innerWidth) {
      left = Math.max(padding, window.innerWidth - rect.width - padding)
    }
    if (top + rect.height + padding > window.innerHeight) {
      top = Math.max(padding, window.innerHeight - rect.height - padding)
    }
    contextMenuStyle.left = `${Math.max(padding, left)}px`
    contextMenuStyle.top = `${Math.max(padding, top)}px`
  })
}

const submitStatus = async (approved: number, successMessage: string) => {
  const row = contextMenuRow.value
  if (!row?.id) return
  try {
    await updateDanmakuStatus({ id: row.id, approved })
    ElMessage.success(successMessage)
    fetchList()
  } catch (error: any) {
    ElMessage.error(error?.message || '操作失败')
  } finally {
    hideContextMenu()
  }
}

const onContextApprove = async () => {
  await submitStatus(1, '已通过弹幕审核')
}

const onContextReject = async () => {
  await submitStatus(0, '已标记为待审核')
}

const onContextDelete = async () => {
  const row = contextMenuRow.value
  if (!row?.id) return

  ElMessageBox.confirm('确定要删除该弹幕吗？', '删除弹幕', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    try {
      await deleteDanmakuAdmin(row.id as string | number)
      ElMessage.success('删除成功')
      if (tableData.value.length === 1 && queryParams.current > 1) {
        queryParams.current -= 1
      }
      fetchList()
    } catch (error: any) {
      ElMessage.error(error?.message || '删除失败')
    }
  }).catch(() => {})

  hideContextMenu()
}

onMounted(() => {
  fetchList()
  document.addEventListener('click', documentClickHandler)
})

onUnmounted(() => {
  document.removeEventListener('click', documentClickHandler)
})
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
  width: 10rem;
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