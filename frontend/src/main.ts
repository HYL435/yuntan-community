import { createApp } from 'vue'
import { createPinia } from 'pinia'
import './style.css'
import '@fontsource/ma-shan-zheng/400.css'
import '@fontsource/noto-serif-sc/400.css'
import '@fontsource/noto-serif-sc/700.css'
import App from './App.vue'
import router from './router'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
// 启用 Element Plus 暗色主题 CSS 变量，使所有 El-Plus 组件响应 html.dark 类切换
import 'element-plus/theme-chalk/dark/css-vars.css'
import { useUserStore } from './stores/user'

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.use(router)
app.use(ElementPlus)

const BOOT_LOADING_MIN_MS = 420
const BOOT_LOADING_FADE_MS = 220

const hideBootLoading = (startAt: number) => {
  const el = document.getElementById('app-boot-loading')
  if (!el) return
  const elapsed = Date.now() - startAt
  const wait = Math.max(0, BOOT_LOADING_MIN_MS - elapsed)
  window.setTimeout(() => {
    el.classList.add('is-exit')
    window.setTimeout(() => {
      el.remove()
    }, BOOT_LOADING_FADE_MS)
  }, wait)
}

// 应用启动时恢复登录状态
const userStore = useUserStore()
const bootStartAt = Date.now()

userStore.restoreLogin().catch(() => {
  // 启动恢复失败不阻塞挂载，交给后续页面逻辑处理
}).finally(() => {
  app.mount('#app')
  hideBootLoading(bootStartAt)
})

