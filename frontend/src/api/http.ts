import axios, { AxiosInstance, AxiosResponse } from 'axios';

const IS_DEV = import.meta.env.DEV;

// 创建 axios 实例
// 所有请求统一走 /api 前缀，由网关或反向代理转发到后端
const baseURL = '/api';

const http: AxiosInstance = axios.create({
  baseURL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// 请求拦截器
http.interceptors.request.use(
  (config) => {
    // 从本地存储获取 token 并添加到请求头
    const token = localStorage.getItem('auth_token');
    try {
      // 将 token 注入到请求头，并打印调试信息，方便定位拦截器是否生效
      if (token) {
        ;(config.headers as any).Authorization = token;
        ;(config.headers as any)['authorization'] = token;
      }
      // 如果请求体是 FormData，删除默认 Content-Type，让浏览器自动设置带 boundary 的 multipart header
      try {
        if (config.data && typeof FormData !== 'undefined' && config.data instanceof FormData) {
          if (config.headers) {
            delete (config.headers as any)['Content-Type'];
            delete (config.headers as any)['content-type'];
          }
        }
      } catch (e) {}

      if (IS_DEV) {
        console.debug('[http] 请求:', config.method, config.url, 'HasAuthorization=', !!token)
      }
      // 如果是 /front/comments 相关请求，打印调用栈帮助定位谁发起了 GET /front/comments
      try {
        if (IS_DEV && typeof config.url === 'string' && config.url.includes('/front/comments')) {
          console.groupCollapsed('[http] 调试 /front/comments 调用栈]')
          console.trace()
          console.groupEnd()
        }
      } catch (e) {}
    } catch (err) {
      // 忽略日志错误，继续请求
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// 响应拦截器
http.interceptors.response.use(
  (response: AxiosResponse) => {
    // 调试响应，方便排查后端返回与前端解析不一致的问题
    try {
      if (IS_DEV) {
        console.debug('[http] 响应:', response.config?.url, response.status)
      }
    } catch (err) {}
    return response;
  },
  (error) => {
    // ── 网络层错误（服务器完全不可达、超时、CORS 预检失败等）──
    if (!error.response) {
      // 标记为网络错误，方便上层区分
      ;(error as any).isNetworkError = true
      error.message = '网络连接失败，请检查您的网络或稍后再试'

      // 只要不是静默请求（_silent: true），就广播事件让 App.vue 决定是否跳转
      if (!(error.config as any)?._silent) {
        try {
          window.dispatchEvent(new CustomEvent('app:network-error', {
            detail: { url: error.config?.url }
          }))
        } catch (_) {}
      }

      return Promise.reject(error)
    }

    // ── 有 HTTP 响应，提取后端真实 message 覆盖 Axios 的通用错误描述 ──
    const backendMsg: string | undefined =
      error.response?.data?.message ||
      error.response?.data?.msg ||
      error.response?.data?.error
    if (backendMsg) {
      // 让所有 catch(e) { e.message } 直接拿到后端中文提示
      error.message = backendMsg
      // 挂载便于区分 Axios 原始状态码
      ;(error as any).backendMessage = backendMsg
    }

    // 只在某些特定情况下处理 401 错误
    if (error.response?.status === 401) {
      console.warn('[http] 401 响应，移除 auth_token')
      localStorage.removeItem('auth_token');
    }
    return Promise.reject(error);
  }
);

export default http;
