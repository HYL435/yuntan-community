export const ADMIN_SITE_SETTINGS_KEY = 'admin_settings_site_v2'

export type AdminServiceSettings = {
  userManagementEnabled: boolean
  statsServiceEnabled: boolean
  interactionServiceEnabled: boolean
}

export type AdminSiteSettings = {
  siteTitle: string
  openRegister: boolean
  commentNeedAudit: boolean
  maintenanceMode: boolean
  services: AdminServiceSettings
}

export const createDefaultAdminSiteSettings = (): AdminSiteSettings => ({
  siteTitle: '云坛社区',
  openRegister: true,
  commentNeedAudit: true,
  maintenanceMode: false,
  services: {
    userManagementEnabled: true,
    statsServiceEnabled: true,
    interactionServiceEnabled: true,
  },
})

export const normalizeAdminSiteSettings = (value: unknown): AdminSiteSettings => {
  const defaults = createDefaultAdminSiteSettings()
  const source = value && typeof value === 'object' ? value as Record<string, any> : {}
  const services = source.services && typeof source.services === 'object'
    ? source.services as Record<string, any>
    : {}

  return {
    siteTitle: typeof source.siteTitle === 'string' && source.siteTitle.trim()
      ? source.siteTitle
      : defaults.siteTitle,
    openRegister: source.openRegister === undefined ? defaults.openRegister : !!source.openRegister,
    commentNeedAudit: source.commentNeedAudit === undefined ? defaults.commentNeedAudit : !!source.commentNeedAudit,
    maintenanceMode: source.maintenanceMode === undefined ? defaults.maintenanceMode : !!source.maintenanceMode,
    services: {
      userManagementEnabled: services.userManagementEnabled === undefined ? defaults.services.userManagementEnabled : !!services.userManagementEnabled,
      statsServiceEnabled: services.statsServiceEnabled === undefined ? defaults.services.statsServiceEnabled : !!services.statsServiceEnabled,
      interactionServiceEnabled: services.interactionServiceEnabled === undefined ? defaults.services.interactionServiceEnabled : !!services.interactionServiceEnabled,
    },
  }
}

export const loadAdminSiteSettings = (): AdminSiteSettings => {
  if (typeof window === 'undefined') {
    return createDefaultAdminSiteSettings()
  }

  const raw = window.localStorage.getItem(ADMIN_SITE_SETTINGS_KEY)
  if (!raw) {
    return createDefaultAdminSiteSettings()
  }

  try {
    return normalizeAdminSiteSettings(JSON.parse(raw))
  } catch {
    return createDefaultAdminSiteSettings()
  }
}

export const saveAdminSiteSettings = (settings: AdminSiteSettings) => {
  if (typeof window === 'undefined') {
    return
  }

  window.localStorage.setItem(ADMIN_SITE_SETTINGS_KEY, JSON.stringify(settings))
  window.dispatchEvent(new CustomEvent('admin:site-settings-updated', { detail: settings }))
}