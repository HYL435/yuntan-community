<template>
  <div class="p-4 bg-white dark:bg-[#1e293b] rounded-lg shadow-sm transition-colors duration-300">
    <div class="mb-4 flex justify-between items-center gap-4 flex-wrap">
      <h2 class="text-xl font-bold text-gray-800 dark:text-white">建站历程管理</h2>
      <el-button type="primary" @click="openCreate">新增节点</el-button>
    </div>

    <el-form :inline="true" :model="queryParams" class="mb-4 flex flex-wrap gap-2" @submit.prevent>
      <el-form-item label="标题">
        <el-input v-model="queryParams.title" placeholder="请输入节点标题" clearable class="w-56!" />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="queryParams.status" placeholder="全部" clearable class="w-32!">
          <el-option label="启用" :value="1" />
          <el-option label="禁用" :value="0" />
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
          {{ (queryParams.current - 1) * queryParams.size + $index + 1 }}
        </template>
      </el-table-column>
      <el-table-column prop="sortOrder" label="排序" width="90" align="center" />
      <el-table-column prop="eventDate" label="日期" width="140" align="center">
        <template #default="{ row }">{{ formatDay(row.eventDate) }}</template>
      </el-table-column>
      <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip />
      <el-table-column prop="description" label="描述" min-width="320" show-overflow-tooltip />
      <el-table-column prop="status" label="状态" width="140" align="center">
        <template #default="{ row }">
          <el-switch v-model="row.status" :active-value="1" :inactive-value="0" @change="onToggleStatus(row)" />
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" align="center">
        <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
      </el-table-column>
      <el-table-column prop="updateTime" label="更新时间" width="180" align="center">
        <template #default="{ row }">{{ formatDate(row.updateTime) }}</template>
      </el-table-column>
    </el-table>

    <div
      v-if="contextMenuVisible"
      ref="menuRef"
      :style="contextMenuStyle"
      :class="['ctx-menu', menuIsDark ? 'ctx-menu--dark' : 'ctx-menu--light']"
      @click.stop
    >
      <button class="ctx-item" @click="onContextEdit">编辑节点</button>
      <button class="ctx-item" @click="onContextToggleStatus">
        {{ contextMenuRow?.status === 1 ? '设为禁用' : '设为启用' }}
      </button>
      <button class="ctx-item ctx-item--danger" @click="onContextDelete">删除节点</button>
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

    <el-dialog v-model="formVisible" :title="isEditing ? '编辑建站节点' : '新增建站节点'" width="620px" destroy-on-close>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="96px">
        <el-form-item label="节点日期" prop="eventDate">
          <el-date-picker
            v-model="form.eventDate"
            type="date"
            placeholder="请选择日期"
            value-format="YYYY-MM-DD"
            format="YYYY-MM-DD"
            class="w-full!"
          />
        </el-form-item>
        <el-form-item label="节点标题" prop="title">
          <el-input v-model="form.title" maxlength="128" show-word-limit />
        </el-form-item>
        <el-form-item label="节点描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="4" maxlength="1000" show-word-limit />
        </el-form-item>
        <el-form-item label="排序值" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" :max="9999" class="w-full!" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted, onUnmounted, nextTick, type CSSProperties } from 'vue'
import http from '@/api/http'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { useIsDark } from '@/composables/useIsDark'

type TimelineRecord = {
  id?: number | string
  eventDate?: string
  title?: string
  description?: string
  sortOrder?: number
  status?: number
  createTime?: any
  updateTime?: any
}

const loading = ref(false)
const formVisible = ref(false)
const isEditing = ref(false)
const total = ref(0)
const tableData = ref<TimelineRecord[]>([])
const formRef = ref<FormInstance>()
const isDark = useIsDark()
const menuRef = ref<HTMLElement | null>(null)
const contextMenuVisible = ref(false)
const contextMenuRow = ref<TimelineRecord | null>(null)
const contextMenuStyle = reactive<CSSProperties>({ left: '0px', top: '0px', position: 'fixed' })
const menuIsDark = ref(false)

const queryParams = reactive({
  current: 1,
  size: 10,
  title: '',
  status: undefined as number | undefined,
})

const form = reactive({
  id: undefined as number | string | undefined,
  eventDate: '',
  title: '',
  description: '',
  sortOrder: 0,
  status: 1,
})

const rules: FormRules = {
  eventDate: [{ required: true, message: '请选择节点日期', trigger: 'change' }],
  title: [{ required: true, message: '请输入节点标题', trigger: 'blur' }],
  description: [{ required: true, message: '请输入节点描述', trigger: 'blur' }],
}

const normalizeRecord = (item: any): TimelineRecord => ({
  ...item,
  eventDate: item?.eventDate ?? item?.date ?? item?.timelineDate ?? item?.statDate,
  title: item?.title ?? item?.nodeTitle ?? item?.timelineTitle,
  description: item?.description ?? item?.desc ?? item?.content ?? item?.timelineDesc,
  sortOrder: Number(item?.sortOrder ?? item?.sort ?? item?.orderNum ?? 0),
  status: Number(item?.status ?? 1),
  createTime: item?.createTime ?? item?.createdAt ?? item?.createDate,
  updateTime: item?.updateTime ?? item?.updatedAt ?? item?.updateDate,
})

const formatDate = (value: any) => {
  if (!value && value !== 0) return '-'
  try {
    let date: Date
    if (Array.isArray(value)) {
      const [year, month = 1, day = 1, hour = 0, minute = 0, second = 0, nano = 0] = value.map((n: any) => Number(n) || 0)
      date = new Date(year, Math.max(month - 1, 0), day || 1, hour, minute, second, Math.floor(nano / 1_000_000))
    } else if (typeof value === 'object') {
      const year = Number(value?.year)
      const month = Number(value?.monthValue ?? value?.month ?? 1)
      const day = Number(value?.dayOfMonth ?? value?.day ?? 1)
      const hour = Number(value?.hour ?? 0)
      const minute = Number(value?.minute ?? 0)
      const second = Number(value?.second ?? 0)
      date = new Date(year, Math.max(month - 1, 0), day, hour, minute, second)
    } else if (typeof value === 'number') {
      date = new Date(value)
    } else {
      const raw = String(value).trim()
      const normalized = /^\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}/.test(raw) ? raw.replace(' ', 'T') : raw
      date = new Date(normalized)
    }
    if (Number.isNaN(date.getTime())) return '-'
    return date.toLocaleString()
  } catch (_error) {
    return '-'
  }
}

const formatDay = (value: any) => {
  if (!value && value !== 0) return '-'
  if (Array.isArray(value)) {
    const [year, month = 1, day = 1] = value.map((n: any) => Number(n) || 0)
    return `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')}`
  }
  if (typeof value === 'object') {
    const year = Number(value?.year)
    const month = Number(value?.monthValue ?? value?.month ?? 1)
    const day = Number(value?.dayOfMonth ?? value?.day ?? 1)
    if (year) {
      return `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')}`
    }
  }
  const text = String(value)
  return text.includes('T') ? text.slice(0, 10) : text.slice(0, 10)
}

const fetchList = async () => {
  loading.value = true
  try {
    const res = await http.get('/admin/timeline/list', {
      params: {
        pageNo: queryParams.current,
        pageSize: queryParams.size,
        title: queryParams.title || undefined,
        status: queryParams.status,
      },
    })
    const body = res.data || res
    const data = body.data || body
    const records = data.records || data.list || data.rows || data || []
    const list = Array.isArray(records) ? records : []
    tableData.value = list.map(normalizeRecord)
    total.value = Number(data.total || data.totalCount || list.length || 0)
  } catch (error: any) {
    tableData.value = []
    total.value = 0
    ElMessage.error(error?.message || '获取建站历程失败')
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
  queryParams.status = undefined
  handleSearch()
}

const handlePageChange = (page: number) => {
  queryParams.current = page
  fetchList()
}

const handleSizeChange = (size: number) => {
  queryParams.size = size
  queryParams.current = 1
  fetchList()
}

const hideContextMenu = () => {
  contextMenuVisible.value = false
  contextMenuRow.value = null
}

const handleRowContextmenu = (row: TimelineRecord, _column: any, event: MouseEvent) => {
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
    const menuEl = menuRef.value
    if (!menuEl) return
    const rect = menuEl.getBoundingClientRect()
    const padding = 8

    if (left + rect.width + padding > window.innerWidth) {
      left = Math.max(padding, window.innerWidth - rect.width - padding)
    }
    if (top + rect.height + padding > window.innerHeight) {
      top = Math.max(padding, window.innerHeight - rect.height - padding)
    }

    contextMenuStyle.left = `${left}px`
    contextMenuStyle.top = `${top}px`
  })
}

const onContextEdit = () => {
  if (contextMenuRow.value) {
    openEdit(contextMenuRow.value)
  }
  hideContextMenu()
}

const onContextToggleStatus = async () => {
  if (!contextMenuRow.value) return
  const row = contextMenuRow.value
  const prev = row.status ?? 1
  row.status = prev === 1 ? 0 : 1
  await onToggleStatus(row)
  hideContextMenu()
}

const onContextDelete = () => {
  if (contextMenuRow.value) {
    confirmDelete(contextMenuRow.value)
  }
  hideContextMenu()
}

const resetForm = () => {
  form.id = undefined
  form.eventDate = ''
  form.title = ''
  form.description = ''
  form.sortOrder = 0
  form.status = 1
}

const openCreate = () => {
  isEditing.value = false
  resetForm()
  formVisible.value = true
}

const openEdit = (row: TimelineRecord) => {
  isEditing.value = true
  form.id = row.id
  form.eventDate = formatDay(row.eventDate)
  form.title = row.title || ''
  form.description = row.description || ''
  form.sortOrder = row.sortOrder ?? 0
  form.status = row.status ?? 1
  formVisible.value = true
}

const submitForm = async () => {
  try {
    await formRef.value?.validate()
  } catch (_error) {
    return
  }

  const payloadBase = {
    eventDate: form.eventDate,
    title: form.title,
    description: form.description,
    sortOrder: form.sortOrder,
    status: form.status,
  }

  try {
    if (isEditing.value) {
      await http.put('/admin/timeline', {
        id: form.id,
        ...payloadBase,
      })
      ElMessage.success('更新成功')
    } else {
      await http.post('/admin/timeline', payloadBase)
      ElMessage.success('创建成功')
    }
    formVisible.value = false
    fetchList()
  } catch (error: any) {
    ElMessage.error(error?.message || '保存失败')
  }
}

const confirmDelete = (row: TimelineRecord) => {
  ElMessageBox.confirm(`确定删除节点“${row.title || '-'}”吗？`, '删除确认', { type: 'warning' })
    .then(async () => {
      try {
        await http.put(`/admin/timeline/deleted/${row.id}`)
        ElMessage.success('删除成功')
        fetchList()
      } catch (error: any) {
        ElMessage.error(error?.message || '删除失败')
      }
    })
    .catch(() => {})
}

const onToggleStatus = async (row: TimelineRecord) => {
  try {
    await http.put('/admin/timeline/status', {
      id: row.id,
      status: row.status,
    })
    ElMessage.success('状态已更新')
  } catch (error) {
    row.status = row.status === 1 ? 0 : 1
    ElMessage.error((error as any)?.message || '状态更新失败')
  }
}

onMounted(() => {
  fetchList()
  document.addEventListener('click', hideContextMenu)
})

onUnmounted(() => {
  document.removeEventListener('click', hideContextMenu)
})
</script>

<style scoped>
:deep(.el-table) {
  background: transparent;
}

.ctx-menu {
  position: fixed;
  z-index: 60;
  min-width: 132px;
  border-radius: 10px;
  padding: 6px;
  box-shadow: 0 12px 28px rgba(15, 23, 42, 0.28);
  border: 1px solid rgba(148, 163, 184, 0.24);
  backdrop-filter: blur(8px);
}

.ctx-menu--light {
  background: rgba(255, 255, 255, 0.95);
}

.ctx-menu--dark {
  background: rgba(15, 23, 42, 0.96);
}

.ctx-item {
  width: 100%;
  border: none;
  background: transparent;
  text-align: left;
  border-radius: 8px;
  padding: 8px 10px;
  font-size: 13px;
  cursor: pointer;
  color: inherit;
}

.ctx-menu--light .ctx-item:hover {
  background: rgba(37, 99, 235, 0.1);
}

.ctx-menu--dark .ctx-item:hover {
  background: rgba(59, 130, 246, 0.2);
}

.ctx-item--danger {
  color: #ef4444;
}
</style>