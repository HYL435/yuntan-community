import http from './http'

export type AdminDanmakuQuery = {
  pageNo?: number
  pageSize?: number
  author?: string
  approved?: number
  sortBy?: string
  isAsc?: boolean
}

export type DanmakuStatusPayload = {
  id: string | number
  approved: number
}

export const listDanmakuAdmin = (params: AdminDanmakuQuery) => {
  return http.get('/admin/danmaku/page', { params })
}

export const updateDanmakuStatus = (data: DanmakuStatusPayload) => {
  return http.put('/admin/danmaku', data)
}

export const deleteDanmakuAdmin = (id: string | number) => {
  return http.put('/admin/danmaku/delete', null, { params: { id } })
}

export default {
  listDanmakuAdmin,
  updateDanmakuStatus,
  deleteDanmakuAdmin,
}