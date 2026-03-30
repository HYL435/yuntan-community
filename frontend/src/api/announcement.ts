import http from './http'

export interface SiteAnnouncement {
  id?: number
  title: string
  content: string
  link?: string
  enabled: boolean
  updateTime?: string
}

export interface SaveAnnouncementPayload {
  id?: number
  title: string
  content: string
  link?: string
  enabled: boolean
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
  return {
    id: source.id,
    title: source.title || source.announcementTitle || '网站公告',
    content: source.content || source.announcementContent || '',
    link: source.link || source.announcementLink || '',
    enabled: toBoolean(source.enabled ?? source.isEnabled ?? source.open ?? source.status),
    updateTime: source.updateTime || source.updatedAt || source.createTime,
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
    const response = await http.get('/admin/announcement', { _silent: true } as any)
    const data = unwrapData(response)
    const normalized = normalizeAnnouncement(data)
    saveLocalAnnouncement(normalized)
    return normalized
  } catch (error) {
    console.warn('[announcement] 使用本地公告配置（admin）', error)
    return readLocalAnnouncement()
  }
}

export const saveAdminAnnouncement = async (payload: SaveAnnouncementPayload): Promise<SiteAnnouncement> => {
  const requestBody = {
    id: payload.id,
    title: payload.title,
    content: payload.content,
    link: payload.link || '',
    enabled: payload.enabled,
  }

  try {
    const response = await http.put('/admin/announcement', requestBody, { _silent: true } as any)
    const data = unwrapData(response)
    const normalized = normalizeAnnouncement(data || requestBody)
    saveLocalAnnouncement(normalized)
    return normalized
  } catch (error) {
    console.warn('[announcement] 保存到后端失败，已写入本地公告配置', error)
    const normalized = normalizeAnnouncement(requestBody)
    saveLocalAnnouncement(normalized)
    return normalized
  }
}

export const getFrontAnnouncement = async (): Promise<SiteAnnouncement | null> => {
  try {
    const response = await http.get('/front/announcement/current', { _silent: true } as any)
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
