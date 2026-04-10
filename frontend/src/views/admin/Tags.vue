<template>
  <div class="p-4 bg-white dark:bg-[#0f172a] rounded-lg shadow-sm transition-colors duration-300">
    <div class="mb-4 flex justify-between items-center">
      <h2 class="text-xl font-bold text-gray-800 dark:text-white">标签管理</h2>
      <div>
        <el-button type="primary" @click="openCreate">新建标签</el-button>
      </div>
    </div>

    <el-table :data="tags" style="width:100%" v-loading="loading"
      :header-cell-style="{ background: isDark ? '#0b1220' : '#f8fafc', color: isDark ? '#e2e8f0' : '#475569' }">
      <el-table-column label="序号" width="80" align="center">
        <template #default="{ $index }">{{ $index + 1 }}</template>
      </el-table-column>
      <el-table-column prop="tagName" label="标签名称" min-width="220" />
      <el-table-column prop="createTime" label="创建时间" width="180" align="center">
        <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
      </el-table-column>
      <el-table-column prop="updateTime" label="更新时间" width="180" align="center">
        <template #default="{ row }">{{ formatDate(row.updateTime) }}</template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="140" align="center">
        <template #default="{ row }">
          <el-switch v-model="row.status" :active-value="1" :inactive-value="0" @change="onToggleTagStatus(row, $event)" />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" align="center">
        <template #default="{ row }">
          <el-button type="text" size="small" @click="openEdit(row)">编辑</el-button>
          <el-button type="text" size="small" @click="confirmDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="formVisible" :title="isEditing ? '编辑标签' : '新建标签'" width="480px" destroy-on-close>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="标签名称" prop="tagName">
          <el-input v-model="form.tagName" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="状态">
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
import { ref, reactive, onMounted } from 'vue'
import http from '@/api/http'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useIsDark } from '@/composables/useIsDark'

const loading = ref(false)
const tags = ref<Array<any>>([])
const formVisible = ref(false)
const isEditing = ref(false)
const formRef = ref()
const form = reactive({ id: undefined as unknown as string | number | undefined, tagName: '', status: 1 })

const rules = {
  tagName: [{ required: true, message: '请输入标签名称', trigger: 'blur' }]
}

const isDark = useIsDark()

const fetchList = async () => {
  loading.value = true
  try {
    const res = await http.get('/admin/tags')
    const body = res.data || res
    const data = body.data || body
    const list = Array.isArray(data) ? data : (data.list || data.records || data)
    tags.value = Array.isArray(list)
      ? list.map((item: any) => ({
          ...item,
          createTime: item?.createTime ?? item?.createDate ?? item?.createdAt ?? item?.create_date ?? item?.gmtCreate,
          updateTime: item?.updateTime ?? item?.updateDate ?? item?.updatedAt ?? item?.update_date ?? item?.gmtModified
        }))
      : []
  } catch (err) {
    console.error('获取标签失败', err)
    ElMessage.error('获取标签失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchList()
})

const openCreate = () => {
  isEditing.value = false
  form.id = undefined
  form.tagName = ''
  form.status = 1
  formVisible.value = true
}

const openEdit = (row: any) => {
  isEditing.value = true
  form.id = row.id
  form.tagName = row.tagName || row.name || ''
  form.status = (row.status !== undefined && row.status !== null) ? Number(row.status) : 1
  formVisible.value = true
}

const submitForm = async () => {
  try {
    // @ts-ignore
    await formRef.value.validate()
  } catch (e) {
    return
  }
  try {
    const statusVal = Number(form.status ?? 1)
    if (isEditing.value) {
      const payload = { id: String(form.id), tagName: form.tagName, status: statusVal }
      await http.put('/admin/tags', payload)
      ElMessage.success('更新成功')
    } else {
      const payload = { tagName: form.tagName, status: statusVal }
      await http.post('/admin/tags', payload)
      ElMessage.success('创建成功')
    }
    formVisible.value = false
    fetchList()
  } catch (err: any) {
    console.error('提交标签失败', err)
    ElMessage.error(err?.message || '提交失败')
  }
}

const confirmDelete = (row: any) => {
  ElMessageBox.confirm(`确定删除标签 "${row.tagName || row.name}" 吗？`, '删除确认', { type: 'warning' })
    .then(async () => {
      try {
        const idStr = String(row.id)
        await http.put(`/admin/tags/deleted/${idStr}`)
        ElMessage.success('删除成功')
        fetchList()
      } catch (err: any) {
        ElMessage.error(err?.message || '删除失败')
      }
    }).catch(() => {})
}

const onToggleTagStatus = async (row: any, newStatus?: any) => {
  // 强制把 newStatus 归一为 0/1
  const statusVal = (newStatus === true || newStatus === '1' || newStatus === 1) ? 1 : 0
  if (!row || (row.id === undefined || row.id === null)) {
    ElMessage.error('标签 ID 缺失，无法修改状态')
    return
  }
  const idStr = String(row.id)
  const old = Number(row.status)
  try {
    row.status = statusVal
    const payload = { id: idStr, status: statusVal }
    console.debug('[Tags] change status payload', payload)
    await http.put('/admin/tags/status', payload)
    ElMessage.success('状态已更新')
    fetchList()
  } catch (err: any) {
    try { row.status = old } catch (e) {}
    ElMessage.error(err?.message || '更新状态失败')
  }
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
</script>

<style scoped>
:deep(.el-table) {
  background: transparent;
}
</style>
