import http from './http'

export interface SiteAnnouncement {
  id?: number
  title: string
  content: string
  link?: string
  enabled: boolean
  status?: number
  publishTime?: string
  updateTime?: string
}

export interface CreateAnnouncementPayload {
  title: string
  content: string
  link?: string
}

export interface UpdateAnnouncementPayload {
  id: number
  title: string
  content: string
  link?: string
}

export interface UpdateAnnouncementStatusPayload {
  id: number
  status: 0 | 1 | 2
}

const LOCAL_ANNOUNCEMENT_KEY = 'site_announcement_fallback_v1'

const parseMaybeJson = (value: unknown): any => {
  if (typeof value !== 'string') return value
  try {
    return JSON.parse(value)
  } catch {
    return value
  }
}

const unwrapData = (response: any): any => {
  const payload = parseMaybeJson(response?.data ?? response)
  return parseMaybeJson(payload?.data ?? payload)
}

const toBoolean = (value: unknown): boolean => {
  if (typeof value === 'boolean') return value
  if (typeof value === 'number') return value === 1
  if (typeof value === 'string') return value === '1' || value.toLowerCase() === 'true'
  return false
}

const normalizeAnnouncement = (raw: any): SiteAnnouncement => {
  const source = raw && typeof raw === 'object' ? raw : {}
  const enabledRaw = source.enabled ?? source.isEnabled ?? source.open ?? source.status
  const statusRaw = source.status
  const status = typeof statusRaw === 'number'
    ? statusRaw
    : typeof statusRaw === 'string' && statusRaw.trim() !== '' && !Number.isNaN(Number(statusRaw))
      ? Number(statusRaw)
      : undefined
  return {
    id: source.id,
    title: source.title || source.announcementTitle || '网站公告',
    content: source.content || source.announcementContent || '',
    link: source.link || source.linkUrl || source.announcementLink || '',
    enabled: status !== undefined ? status === 1 : enabledRaw === undefined ? true : toBoolean(enabledRaw),
    status,
    publishTime: source.publishTime || source.releaseTime || source.publishedAt,
    updateTime: source.updateTime || source.updatedAt || source.createTime || source.publishTime,
  }
}

const readLocalAnnouncement = (): SiteAnnouncement => {
  const raw = localStorage.getItem(LOCAL_ANNOUNCEMENT_KEY)
  if (!raw) {
    return {
      title: '网站公告',
      content: '欢迎来到云坛。',
      link: '',
      enabled: false,
    }
  }
  return normalizeAnnouncement(parseMaybeJson(raw))
}

const saveLocalAnnouncement = (announcement: SiteAnnouncement) => {
  localStorage.setItem(LOCAL_ANNOUNCEMENT_KEY, JSON.stringify(announcement))
}

export const getAdminAnnouncement = async (): Promise<SiteAnnouncement> => {
  try {
    const response = await http.get('/admin/announcements', { _silent: true } as any)
    const data = unwrapData(response)
    const normalized = normalizeAnnouncement(data)
    saveLocalAnnouncement(normalized)
    return normalized
  } catch (error) {
    console.warn('[announcement] 使用本地公告配置（admin）', error)
    return readLocalAnnouncement()
  }
}

export const addAdminAnnouncement = async (payload: CreateAnnouncementPayload): Promise<SiteAnnouncement> => {
  const requestBody = {
    title: payload.title,
    content: payload.content,
    linkUrl: payload.link || '',
  }

  try {
    await http.post('/admin/announcements', requestBody, { _silent: true } as any)
    const normalized = await getAdminAnnouncement()
    saveLocalAnnouncement(normalized)
    return normalized
  } catch (error) {
    console.warn('[announcement] 添加到后端失败，已写入本地公告配置', error)
    const normalized = normalizeAnnouncement({
      ...requestBody,
      status: 0,
      enabled: false,
    })
    saveLocalAnnouncement(normalized)
    return normalized
  }
}

export const updateAdminAnnouncement = async (payload: UpdateAnnouncementPayload): Promise<SiteAnnouncement> => {
  const requestBody = {
    id: payload.id,
    title: payload.title,
    content: payload.content,
    linkUrl: payload.link || '',
  }

  try {
    await http.put('/admin/announcements', requestBody, { _silent: true } as any)
    const normalized = await getAdminAnnouncement()
    saveLocalAnnouncement(normalized)
    return normalized
  } catch (error) {
    console.warn('[announcement] 更新到后端失败，已写入本地公告配置', error)
    const normalized = normalizeAnnouncement({
      ...readLocalAnnouncement(),
      ...requestBody,
    })
    saveLocalAnnouncement(normalized)
    return normalized
  }
}

export const changeAdminAnnouncementStatus = async (payload: UpdateAnnouncementStatusPayload): Promise<SiteAnnouncement> => {
  try {
    await http.put('/admin/announcements/status', payload, { _silent: true } as any)
    const normalized = await getAdminAnnouncement()
    saveLocalAnnouncement(normalized)
    return normalized
  } catch (error) {
    console.warn('[announcement] 修改公告状态失败，已写入本地公告配置', error)
    const localAnnouncement = normalizeAnnouncement({
      ...readLocalAnnouncement(),
      id: payload.id,
      status: payload.status,
      enabled: payload.status === 1,
    })
    saveLocalAnnouncement(localAnnouncement)
    return localAnnouncement
  }
}

export const getFrontAnnouncement = async (): Promise<SiteAnnouncement | null> => {
  try {
    const response = await http.get('/front/announcements', { _silent: true } as any)
    const data = unwrapData(response)
    const normalized = normalizeAnnouncement(data)
    if (!normalized.enabled || !normalized.content) {
      return null
    }
    saveLocalAnnouncement(normalized)
    return normalized
  } catch (error) {
    console.warn('[announcement] 使用本地公告配置（front）', error)
    const localAnnouncement = readLocalAnnouncement()
    if (!localAnnouncement.enabled || !localAnnouncement.content) {
      return null
    }
    return localAnnouncement
  }
}
