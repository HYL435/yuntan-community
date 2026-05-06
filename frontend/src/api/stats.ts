import http from './http'

export interface AdminStatsResponse {
  // 通用结构：x 轴标签，series 为多个数据列
  x?: string[];
  series?: { name: string; data: number[] }[];
  // 兼容性：某些后端直接返回多个命名数组
  dates?: string[];
  visits?: number[];
  posts?: number[];
  users?: number[];
}

export const getAdminStats = async (): Promise<AdminStatsResponse> => {
  const res = await http.get('/admin/stats/overview')
  return res.data || {}
}

export const getArticleCount = async (): Promise<number | null> => {
  const res = await http.get('/front/articles/count')
  const body = res?.data ?? res

  const direct = Number(body)
  if (Number.isFinite(direct)) return direct

  const payload = Number(body?.data)
  if (Number.isFinite(payload)) return payload

  return null
}

export const getTodayHot = async (): Promise<number | null> => {
  const res = await http.get('/front/stat/hot/today')
  const body = res?.data ?? res

  const direct = Number(body)
  if (Number.isFinite(direct)) return direct

  const payload = Number(body?.data)
  if (Number.isFinite(payload)) return payload

  return null
}

export const recordPv = async (page: string): Promise<void> => {
  try {
    await http.post('/front/stat/pv', { page })
  } catch {
    // 静默忽略 PV 记录失败，不影响用户体验
  }
}

export default {
  getAdminStats,
  getArticleCount,
  getTodayHot,
  recordPv
}
