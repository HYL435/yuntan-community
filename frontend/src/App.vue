<script setup lang="ts">
import { computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import Header from '@/layouts/Header.vue'
import Footer from '@/layouts/Footer.vue'
import LoadingPulse from '@/components/loaders/LoadingPulse.vue'
import NotificationsContainer from '@/layouts/NotificationsContainer.vue'
import PortalButton from '@/components/common/PortalButton.vue'

const route = useRoute()
const router = useRouter()
const hideChromeRoutes = new Set(['/login', '/register', '/message-board'])
const hideHeaderRoutes = new Set(['/about/site'])
const isAdminRoute = computed(() => route.path.startsWith('/admin'))
const showHeader = computed(() => !isAdminRoute.value && !hideChromeRoutes.has(route.path) && !hideHeaderRoutes.has(route.path))
const showFooter = computed(() => !isAdminRoute.value && !hideChromeRoutes.has(route.path) && route.path !== '/profile')

// 监听 http.ts 派发的网络错误事件
// 使用防抖避免同一时间多个请求失败时多次跳转
let networkErrTimer: ReturnType<typeof setTimeout> | null = null
function onNetworkError() {
  if (networkErrTimer) return          // 已在防抖窗口内，忽略重复事件
  const exclude = new Set(['/network-error', '/404'])
  if (exclude.has(route.path)) return  // 本身已在错误页，不再跳转
  networkErrTimer = setTimeout(() => {
    networkErrTimer = null
    // 再次确认当前路由不是错误页（防抖期间可能已手动离开）
    if (exclude.has(route.path)) return
    router.push({ path: '/network-error', query: { from: route.fullPath } })
  }, 300)
}

onMounted(() => {
  window.addEventListener('app:network-error', onNetworkError)
})
onUnmounted(() => {
  window.removeEventListener('app:network-error', onNetworkError)
  if (networkErrTimer) clearTimeout(networkErrTimer)
})
</script>

<template>
  <Header v-if="showHeader" />

  <Suspense>
    <router-view />
    <template #fallback>
      <div class="app-loading">
        <LoadingPulse />
      </div>
    </template>
  </Suspense>

  <Footer v-if="showFooter" />

  <NotificationsContainer />
  <PortalButton />
</template>

<style scoped>
.app-loading {
  min-height: 60vh;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
