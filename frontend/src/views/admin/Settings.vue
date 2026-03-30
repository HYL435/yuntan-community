<template>
  <div class="p-4 bg-white dark:bg-[#0f172a] rounded-lg shadow-sm transition-colors duration-300">
    <h2 class="text-2xl font-bold mb-4 text-gray-800 dark:text-white">系统设置</h2>

    <el-collapse v-model="activePanel" accordion class="settings-collapse">
      <el-collapse-item name="announcement" title="公告管理">
        <el-alert
          title="在这里发布、修改或关闭首页公告。关闭后前台首页将不显示公告。"
          type="info"
          :closable="false"
          class="mb-4"
        />

        <el-form label-width="120px" v-loading="announcementLoading">
          <el-form-item label="公告标题">
            <el-input v-model="announcementForm.title" maxlength="60" show-word-limit placeholder="请输入公告标题" />
          </el-form-item>

          <el-form-item label="公告内容">
            <el-input
              v-model="announcementForm.content"
              type="textarea"
              :rows="5"
              maxlength="500"
              show-word-limit
              placeholder="请输入公告正文"
            />
          </el-form-item>

          <el-form-item label="跳转链接">
            <el-input v-model="announcementForm.link" placeholder="可选，例如 https://example.com/news" />
          </el-form-item>

          <el-form-item label="当前状态">
            <el-tag :type="announcementForm.enabled ? 'success' : 'info'">
              {{ announcementForm.enabled ? '已发布' : '已关闭' }}
            </el-tag>
          </el-form-item>

          <el-form-item>
            <div class="flex flex-wrap gap-3">
              <el-button type="primary" :loading="announcementSaving" @click="publishAnnouncement">发布公告</el-button>
              <el-button :loading="announcementSaving" @click="saveAnnouncement">保存修改</el-button>
              <el-button type="danger" plain :loading="announcementSaving" @click="closeAnnouncement">关闭公告</el-button>
              <el-button :loading="announcementLoading" @click="loadAnnouncement">刷新</el-button>
            </div>
          </el-form-item>
        </el-form>
      </el-collapse-item>

      <el-collapse-item name="events" title="活动事件管理">
        <div class="mb-4 flex flex-wrap items-center gap-3">
          <el-button type="primary" @click="openCreateEvent">新增活动</el-button>
          <el-button @click="resetEvents">重置示例数据</el-button>
        </div>

        <el-table :data="eventList" border stripe style="width: 100%">
          <el-table-column prop="name" label="活动名称" min-width="180" />
          <el-table-column prop="timeRange" label="活动时间" min-width="180" />
          <el-table-column prop="description" label="活动说明" min-width="260" show-overflow-tooltip />
          <el-table-column label="状态" width="120">
            <template #default="{ row }">
              <el-tag :type="row.enabled ? 'success' : 'info'">{{ row.enabled ? '启用' : '关闭' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="260" fixed="right">
            <template #default="{ row }">
              <div class="flex items-center gap-2">
                <el-button link type="primary" @click="editEvent(row.id)">编辑</el-button>
                <el-button link @click="toggleEvent(row.id)">{{ row.enabled ? '关闭' : '启用' }}</el-button>
                <el-button link type="danger" @click="deleteEvent(row.id)">删除</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>

        <el-dialog v-model="eventDialogVisible" :title="eventDraft.id ? '编辑活动' : '新增活动'" width="560px">
          <el-form label-width="90px">
            <el-form-item label="活动名称">
              <el-input v-model="eventDraft.name" maxlength="50" placeholder="请输入活动名称" />
            </el-form-item>
            <el-form-item label="活动时间">
              <el-input v-model="eventDraft.timeRange" maxlength="80" placeholder="例如 2026-04-01 ~ 2026-04-15" />
            </el-form-item>
            <el-form-item label="活动说明">
              <el-input v-model="eventDraft.description" type="textarea" :rows="4" maxlength="200" show-word-limit />
            </el-form-item>
            <el-form-item label="默认启用">
              <el-switch v-model="eventDraft.enabled" />
            </el-form-item>
          </el-form>
          <template #footer>
            <el-button @click="eventDialogVisible = false">取消</el-button>
            <el-button type="primary" @click="saveEvent">保存</el-button>
          </template>
        </el-dialog>
      </el-collapse-item>

      <el-collapse-item name="site" title="站点基础设置">
        <el-form label-width="120px">
          <el-form-item label="站点标题">
            <el-input v-model="siteSettings.siteTitle" maxlength="60" />
          </el-form-item>
          <el-form-item label="开放注册">
            <el-switch v-model="siteSettings.openRegister" />
          </el-form-item>
          <el-form-item label="评论先审核">
            <el-switch v-model="siteSettings.commentNeedAudit" />
          </el-form-item>
          <el-form-item label="维护模式">
            <el-switch v-model="siteSettings.maintenanceMode" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="saveSiteSettings">保存站点设置</el-button>
          </el-form-item>
        </el-form>
      </el-collapse-item>
    </el-collapse>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getAdminAnnouncement, saveAdminAnnouncement } from '@/api/announcement'

type EventItem = {
  id: number
  name: string
  timeRange: string
  description: string
  enabled: boolean
}

const SETTINGS_EVENTS_KEY = 'admin_settings_events_v1'
const SETTINGS_SITE_KEY = 'admin_settings_site_v1'

const activePanel = ref('announcement')

const announcementLoading = ref(false)
const announcementSaving = ref(false)
const announcementForm = reactive({
  id: undefined as number | undefined,
  title: '',
  content: '',
  link: '',
  enabled: false,
})

const eventList = ref<EventItem[]>([])
const eventDialogVisible = ref(false)
const eventDraft = reactive<EventItem>({
  id: 0,
  name: '',
  timeRange: '',
  description: '',
  enabled: true,
})

const siteSettings = reactive({
  siteTitle: '云坛社区',
  openRegister: true,
  commentNeedAudit: true,
  maintenanceMode: false,
})

const applyAnnouncement = (data: {
  id?: number
  title?: string
  content?: string
  link?: string
  enabled?: boolean
}) => {
  announcementForm.id = data.id
  announcementForm.title = data.title || '网站公告'
  announcementForm.content = data.content || ''
  announcementForm.link = data.link || ''
  announcementForm.enabled = !!data.enabled
}

const loadAnnouncement = async () => {
  announcementLoading.value = true
  try {
    const data = await getAdminAnnouncement()
    applyAnnouncement(data)
  } catch (error) {
    ElMessage.error('读取公告失败')
  } finally {
    announcementLoading.value = false
  }
}

const persistAnnouncement = async (enabled: boolean, successText: string) => {
  if (!announcementForm.title.trim()) {
    ElMessage.warning('请先填写公告标题')
    return
  }
  if (!announcementForm.content.trim()) {
    ElMessage.warning('请先填写公告内容')
    return
  }

  announcementSaving.value = true
  try {
    const result = await saveAdminAnnouncement({
      id: announcementForm.id,
      title: announcementForm.title.trim(),
      content: announcementForm.content.trim(),
      link: announcementForm.link.trim(),
      enabled,
    })
    applyAnnouncement(result)
    ElMessage.success(successText)
  } catch (error) {
    ElMessage.error('保存公告失败')
  } finally {
    announcementSaving.value = false
  }
}

const publishAnnouncement = async () => {
  await persistAnnouncement(true, '公告已发布')
}

const saveAnnouncement = async () => {
  await persistAnnouncement(announcementForm.enabled, '公告已保存')
}

const closeAnnouncement = async () => {
  await persistAnnouncement(false, '公告已关闭')
}

const seedEvents = (): EventItem[] => ([
  { id: 1, name: '春季技术分享会', timeRange: '2026-04-01 ~ 2026-04-07', description: '邀请社区作者在线直播分享。', enabled: true },
  { id: 2, name: '评论互动周', timeRange: '2026-04-10 ~ 2026-04-16', description: '精选优质评论上墙展示。', enabled: false },
])

const saveEventsToStorage = () => {
  localStorage.setItem(SETTINGS_EVENTS_KEY, JSON.stringify(eventList.value))
}

const loadEvents = () => {
  const raw = localStorage.getItem(SETTINGS_EVENTS_KEY)
  if (!raw) {
    eventList.value = seedEvents()
    saveEventsToStorage()
    return
  }
  try {
    const parsed = JSON.parse(raw)
    eventList.value = Array.isArray(parsed) ? parsed : seedEvents()
  } catch {
    eventList.value = seedEvents()
  }
}

const resetEventDraft = () => {
  eventDraft.id = 0
  eventDraft.name = ''
  eventDraft.timeRange = ''
  eventDraft.description = ''
  eventDraft.enabled = true
}

const openCreateEvent = () => {
  resetEventDraft()
  eventDialogVisible.value = true
}

const editEvent = (id: number) => {
  const item = eventList.value.find(e => e.id === id)
  if (!item) return
  eventDraft.id = item.id
  eventDraft.name = item.name
  eventDraft.timeRange = item.timeRange
  eventDraft.description = item.description
  eventDraft.enabled = item.enabled
  eventDialogVisible.value = true
}

const saveEvent = () => {
  if (!eventDraft.name.trim()) {
    ElMessage.warning('请填写活动名称')
    return
  }
  if (!eventDraft.timeRange.trim()) {
    ElMessage.warning('请填写活动时间')
    return
  }

  if (eventDraft.id) {
    const index = eventList.value.findIndex(e => e.id === eventDraft.id)
    if (index >= 0) {
      eventList.value[index] = {
        id: eventDraft.id,
        name: eventDraft.name.trim(),
        timeRange: eventDraft.timeRange.trim(),
        description: eventDraft.description.trim(),
        enabled: eventDraft.enabled,
      }
    }
  } else {
    const nextId = Math.max(0, ...eventList.value.map(e => e.id)) + 1
    eventList.value.unshift({
      id: nextId,
      name: eventDraft.name.trim(),
      timeRange: eventDraft.timeRange.trim(),
      description: eventDraft.description.trim(),
      enabled: eventDraft.enabled,
    })
  }

  saveEventsToStorage()
  eventDialogVisible.value = false
  ElMessage.success('活动已保存')
}

const toggleEvent = (id: number) => {
  const item = eventList.value.find(e => e.id === id)
  if (!item) return
  item.enabled = !item.enabled
  saveEventsToStorage()
  ElMessage.success(item.enabled ? '活动已启用' : '活动已关闭')
}

const deleteEvent = (id: number) => {
  eventList.value = eventList.value.filter(e => e.id !== id)
  saveEventsToStorage()
  ElMessage.success('活动已删除')
}

const resetEvents = () => {
  eventList.value = seedEvents()
  saveEventsToStorage()
  ElMessage.success('已恢复示例活动')
}

const saveSiteSettings = () => {
  localStorage.setItem(SETTINGS_SITE_KEY, JSON.stringify(siteSettings))
  ElMessage.success('站点设置已保存')
}

const loadSiteSettings = () => {
  const raw = localStorage.getItem(SETTINGS_SITE_KEY)
  if (!raw) return
  try {
    const parsed = JSON.parse(raw)
    siteSettings.siteTitle = parsed.siteTitle || siteSettings.siteTitle
    siteSettings.openRegister = !!parsed.openRegister
    siteSettings.commentNeedAudit = !!parsed.commentNeedAudit
    siteSettings.maintenanceMode = !!parsed.maintenanceMode
  } catch {
    // ignore invalid local data
  }
}

onMounted(() => {
  loadAnnouncement()
  loadEvents()
  loadSiteSettings()
})
</script>

<style scoped>
.settings-collapse :deep(.el-collapse-item__header) {
  font-weight: 600;
}
</style>
