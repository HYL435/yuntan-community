import { ref, onMounted, onUnmounted } from 'vue'

/**
 * 响应式暗色模式检测，监听 <html> class 变化自动更新。
 * 适用于 AdminLayout 内的所有子页面，替代不具响应性的
 * `computed(() => document.documentElement.classList.contains('dark'))`。
 */
export function useIsDark() {
  const isDark = ref(document.documentElement.classList.contains('dark'))
  let observer: MutationObserver | null = null

  onMounted(() => {
    observer = new MutationObserver(() => {
      isDark.value = document.documentElement.classList.contains('dark')
    })
    observer.observe(document.documentElement, {
      attributes: true,
      attributeFilter: ['class'],
    })
  })

  onUnmounted(() => {
    observer?.disconnect()
  })

  return isDark
}
