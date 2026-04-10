<script setup lang="ts" name="LatestCarousel">
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import ArticleCard from '@/components/cards/ArticleCard.vue'
import ThreeBodyLoader from '@/components/loaders/ThreeBodyLoader.vue'
import http from '@/api/http'

const router = useRouter()
const row = ref<HTMLElement | null>(null)

const props = defineProps<{ title?: string }>()

let scrollRaf: number | null = null
let velocity = 0
let animating = false
let wheelListenerAttached = false
const friction = 0.92
const minVelocity = 0.1
const maxSpeed = 25

// 文章列表数据
const articles = ref<any[]>([])
const loading = ref(false)
const currentPage = ref(1)
const totalPages = ref(1)
const isScrolledToBottom = ref(false)

const pickFirstArray = (obj: any): any[] => {
  if (!obj || typeof obj !== 'object') return []
  const key = Object.keys(obj).find(k => Array.isArray(obj[k]))
  return key ? obj[key] : []
}

const parseJsonSafely = (v: any): any => {
  if (typeof v !== 'string') return v
  try {
    return JSON.parse(v)
  } catch {
    return v
  }
}

const normalizePayload = (payload: any): any => {
  const p = parseJsonSafely(payload)
  if (typeof p === 'object' && p) {
    return {
      ...p,
      data: parseJsonSafely((p as any).data),
    }
  }
  return p
}

const extractList = (payload: any): any[] => {
  const normalized = normalizePayload(payload)

  const parseArrayLike = (v: any): any[] => {
    const value = parseJsonSafely(v)
    if (Array.isArray(value)) return value
    if (value && typeof value === 'object') {
      const values = Object.values(value)
      if (values.length && values.every(item => typeof item === 'object' && item !== null)) {
        return values as any[]
      }
    }
    if (typeof v === 'string') {
      try {
        const parsed = JSON.parse(v)
        return Array.isArray(parsed) ? parsed : []
      } catch {
        return []
      }
    }
    return []
  }

  const candidates = [
    normalized?.data?.data?.records,
    normalized?.data?.data?.list,
    normalized?.data?.data?.rows,
    normalized?.data?.data?.items,
    normalized?.data?.data?.result,
    normalized?.data?.records,
    normalized?.data?.list,
    normalized?.data?.rows,
    normalized?.data?.items,
    normalized?.data?.result,
    normalized?.records,
    normalized?.list,
    normalized?.rows,
    normalized?.items,
    normalized?.result,
    normalized?.data,
    normalized,
  ]
  for (const c of candidates) {
    const arr = parseArrayLike(c)
    if (arr.length) return arr
  }
  const nested = pickFirstArray(normalized?.data)
  if (nested.length) return nested
  const nestedData = pickFirstArray(normalized?.data?.data)
  if (nestedData.length) return nestedData
  return pickFirstArray(normalized)
}

const normalizeTags = (rawTags: any): string[] => {
  if (!rawTags) return []
  if (Array.isArray(rawTags)) {
    return rawTags
      .map(t => {
        if (typeof t === 'string') return t
        if (typeof t === 'object' && t) return t.tagName || t.name || String(t.label || '')
        return String(t)
      })
      .filter(Boolean)
  }
  if (typeof rawTags === 'string') return [rawTags]
  return []
}

const normalizeArticle = (raw: any) => {
  const tags = normalizeTags(raw?.tags || raw?.tagNames || raw?.tagList || raw?.tagName)
  return {
    ...raw,
    id: raw?.id ?? raw?.articleId,
    title: raw?.title ?? raw?.articleTitle ?? '',
    summary: raw?.summary ?? raw?.description ?? raw?.intro ?? '',
    coverImg: raw?.coverImg ?? raw?.cover ?? raw?.coverUrl ?? '',
    category: raw?.category ?? raw?.categoryName ?? '',
    tags,
    publishTime: raw?.publishTime ?? raw?.createTime ?? raw?.createdAt ?? '',
    likeCount: Number(raw?.likeCount ?? raw?.likes ?? 0),
    commentCount: Number(raw?.commentCount ?? raw?.comments ?? 0),
    collectCount: Number(raw?.collectCount ?? raw?.bookmarks ?? 0),
    viewCount: Number(raw?.viewCount ?? raw?.views ?? raw?.heat ?? 0),
  }
}

// 获取推荐文章
const fetchRecommendedArticles = async (pageNo: number = 1) => {
  loading.value = true
  try {
    const response = await http.get('/front/articles/page', {
      params: {
        pageNo,
        pageSize: 5,
      },
    })

    const payload = normalizePayload(response?.data || response || {})
    const result = extractList(payload).map(normalizeArticle)
    
    // 如果是第一页，替换数据；否则追加数据
    if (pageNo === 1) {
      articles.value = result
    } else {
      articles.value.push(...result)
    }
    
    // 记录分页信息
    currentPage.value = pageNo
    const pagesRaw = payload?.data?.data?.pages
      ?? payload?.data?.pages
      ?? payload?.pages
      ?? payload?.data?.data?.pageTotal
      ?? payload?.data?.pageTotal
      ?? payload?.pageTotal
    totalPages.value = Number(pagesRaw) > 0 ? Number(pagesRaw) : totalPages.value
    
  } catch (err) {
    console.error('Failed to fetch articles:', err)
    if (currentPage.value === 1) {
      articles.value = []
    }
  } finally {
    loading.value = false
  }
}

// 加载下一页
const loadNextPage = async () => {
  if (currentPage.value < totalPages.value) {
    isScrolledToBottom.value = false  // 重置滚动状态
    await fetchRecommendedArticles(currentPage.value + 1)
  }
}

const handleArticleClick = (id?: string | number) => {
  if (!id) return
  router.push(`/article/${id}`)
}

const handleTagClick = (tag?: string) => {
  if (!tag) return
  router.push(`/tag/${encodeURIComponent(tag)}`)
}

const animateScroll = () => {
  if (!row.value) return

  const step = () => {
    if (!row.value) return
    
    const maxScroll = row.value.scrollWidth - row.value.clientWidth
    if (maxScroll <= 0) {
      animating = false
      velocity = 0
      return
    }

    row.value.scrollLeft += velocity

    if (row.value.scrollLeft <= 0 || row.value.scrollLeft >= maxScroll) {
      velocity = 0
      animating = false
      return
    }

    velocity *= friction

    if (Math.abs(velocity) > minVelocity && animating) {
      scrollRaf = requestAnimationFrame(step)
    } else {
      animating = false
    }
  }

  if (!animating) {
    animating = true
    scrollRaf = requestAnimationFrame(step)
  }
}

const handleWheel = (event: WheelEvent) => {
  if (!row.value) return
  
  const maxScroll = row.value.scrollWidth - row.value.clientWidth
  
  if (maxScroll <= 0) return

  event.preventDefault()
  event.stopPropagation()
  
  // 使用 deltaX，如果没有则使用 deltaY
  const delta = event.deltaX !== 0 ? event.deltaX : event.deltaY
  
  velocity = delta * 0.15
  velocity = Math.min(Math.max(velocity, -maxSpeed), maxSpeed)
  
  animateScroll()
}

const attachWheelListener = () => {
  if (!row.value || wheelListenerAttached) return
  row.value.addEventListener('wheel', handleWheel, { passive: false })
  row.value.addEventListener('scroll', handleScroll)
  wheelListenerAttached = true
}

const detachWheelListener = () => {
  if (row.value && wheelListenerAttached) {
    row.value.removeEventListener('wheel', handleWheel)
    row.value.removeEventListener('scroll', handleScroll)
  }
  wheelListenerAttached = false
}

// 检测是否滚动到底部
const handleScroll = () => {
  if (!row.value) return
  
  const scrollLeft = row.value.scrollLeft
  const scrollWidth = row.value.scrollWidth
  const clientWidth = row.value.clientWidth
  
  // 判断是否滚动到底部（留20px的误差范围）
  isScrolledToBottom.value = scrollWidth - scrollLeft - clientWidth <= 20
}

// 监听文章数据变化，数据加载完后添加事件监听
watch(articles, async (newArticles) => {
  if (newArticles.length > 0) {
    await nextTick()
    detachWheelListener()
    attachWheelListener()
    // 延迟检测滚动状态，确保 DOM 已完全渲染
    setTimeout(() => {
      handleScroll()
    }, 100)
  }
}, { immediate: true })

onMounted(() => {
  // 获取推荐文章
  fetchRecommendedArticles()
})

onUnmounted(() => {
  if (scrollRaf) {
    cancelAnimationFrame(scrollRaf)
  }
  detachWheelListener()
})
</script>

<template>
  <div class="latest-carousel">
    <button v-if="props.title" class="btn" type="button">
      <span>{{ props.title }}</span>
    </button>
    <div v-if="loading && articles.length === 0" class="carousel-row" style="display: flex; align-items: center; justify-content: center; min-height: 300px;">
      <ThreeBodyLoader />
    </div>
    <div v-else-if="articles.length === 0" class="carousel-row" style="display: flex; align-items: center; justify-content: center; min-height: 300px;">
      <span>无数据</span>
    </div>
    <div v-else class="carousel-wrapper">
      <div ref="row" class="carousel-row">
        <ArticleCard
          v-for="article in articles"
          :key="article.id"
          :article="article"
          class="flex-none w-80 snap-start"
          @click="handleArticleClick(article.id)"
          @tag-click="handleTagClick"
        />
      </div>
      <button 
        v-show="currentPage < totalPages && isScrolledToBottom" 
        class="load-more-btn" 
        @click="loadNextPage"
        :disabled="loading"
      >
        <span class="btn-text">查看更多</span>
        <svg class="btn-icon" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <polyline points="9 18 15 12 9 6"></polyline>
        </svg>
      </button>
    </div>
  </div>
</template>

<style scoped>
.latest-carousel {
  border: none;
  border-radius: 18px;
  padding: 12px 12px 16px;
  position: relative;
  transition: all 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  overflow: hidden;
}

.latest-carousel::before,
.latest-carousel::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  border-radius: 18px;
  padding: 1px;
  pointer-events: none;
  z-index: 1;
  animation-timing-function: linear;
  animation-iteration-count: infinite;
}

.latest-carousel::before {
  background: conic-gradient(from 0deg, #6366f1, #8b5cf6, #6366f1);
  -webkit-mask: linear-gradient(#fff 0 0) content-box, linear-gradient(#fff 0 0);
  mask: linear-gradient(#fff 0 0) content-box, linear-gradient(#fff 0 0);
  -webkit-mask-composite: xor;
  mask-composite: exclude;
  animation: rotateGlowLeft 6s infinite;
}

.latest-carousel::after {
  background: conic-gradient(from 180deg, #8b5cf6, #6366f1, #8b5cf6);
  -webkit-mask: linear-gradient(#fff 0 0) content-box, linear-gradient(#fff 0 0);
  mask: linear-gradient(#fff 0 0) content-box, linear-gradient(#fff 0 0);
  -webkit-mask-composite: xor;
  mask-composite: exclude;
  animation: rotateGlowRight 8s infinite reverse;
}

.carousel-row {
  position: relative;
  z-index: 2;
}

.carousel-wrapper {
  position: relative;
  z-index: 2;
  display: flex;
  gap: 16px;
  align-items: center;
}

.load-more-btn {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  padding: 16px 12px;
  min-width: 70px;
  height: 200px;
  border: none;
  border-radius: 12px;
  background: transparent;
  color: #6b7280;
  font-weight: 300;
  cursor: pointer;
  transition: all 0.3s ease;
  flex-direction: column;
  writing-mode: vertical-rl;
  text-orientation: mixed;
}

.load-more-btn .btn-text {
  animation: heartbeat 1.5s ease-in-out infinite;
  letter-spacing: 6px;
}

.load-more-btn .btn-icon {
  width: 20px;
  height: 20px;
  animation: heartbeat 1.5s ease-in-out infinite;
  transform: rotate(90deg);
}

.load-more-btn:hover:not(:disabled) {
  color: #6366f1;
}

.load-more-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

@keyframes heartbeat {
  0% {
    transform: scale(1);
    opacity: 1;
  }
  14% {
    transform: scale(1.1);
    opacity: 1;
  }
  28% {
    transform: scale(1);
    opacity: 1;
  }
  42% {
    transform: scale(1.1);
    opacity: 1;
  }
  70% {
    transform: scale(1);
    opacity: 0.8;
  }
  100% {
    transform: scale(1);
    opacity: 0.8;
  }
}

:global(.dark) .load-more-btn {
  color: #d1d5db;
}

:global(.dark) .load-more-btn:hover:not(:disabled) {
  color: #c084fc;
}

.latest-carousel:hover {
  box-shadow: 0 0 20px rgba(99, 102, 241, 0.3),
              0 0 40px rgba(99, 102, 241, 0.15);
  transition: box-shadow 0.5s ease-in-out;
}

.latest-carousel:hover::before {
  background: conic-gradient(from 0deg, #8b5cf6, #6366f1, #8b5cf6);
  animation: rotateGlowLeftHover 6s infinite;
}

.latest-carousel:hover::after {
  background: conic-gradient(from 180deg, #6366f1, #8b5cf6, #6366f1);
  animation: rotateGlowRightHover 8s infinite reverse;
}

@keyframes rotateGlowLeft {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

@keyframes rotateGlowRight {
  from {
    transform: rotate(360deg);
  }
  to {
    transform: rotate(0deg);
  }
}

@keyframes rotateGlowLeftHover {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

@keyframes rotateGlowRightHover {
  from {
    transform: rotate(360deg);
  }
  to {
    transform: rotate(0deg);
  }
}

.carousel-row {
  display: flex;
  gap: 24px;
  overflow-x: auto;
  overflow-y: hidden;
  padding: 2px 2px 10px;
  position: relative;
  cursor: grab;
  user-select: none;
  scroll-behavior: auto;
  scrollbar-width: none;
  transition: all 0.3s ease;
  flex: 1;
  min-width: 0;
}

.carousel-row::-webkit-scrollbar {
  display: none;
}

.carousel-row:active {
  cursor: grabbing;
}

:global(.dark) .latest-carousel,
:global(.dark-mode) .latest-carousel {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
}

:global(.dark) .latest-carousel::before,
:global(.dark-mode) .latest-carousel::before {
  background: conic-gradient(from 0deg, #a78bfa, #c084fc, #a78bfa);
}

:global(.dark) .latest-carousel::after,
:global(.dark-mode) .latest-carousel::after {
  background: conic-gradient(from 180deg, #c084fc, #a78bfa, #c084fc);
}

:global(.dark) .latest-carousel:hover,
:global(.dark-mode) .latest-carousel:hover {
  box-shadow: 0 0 20px rgba(168, 85, 247, 0.4),
              0 0 40px rgba(168, 85, 247, 0.2);
}

:global(.dark) .latest-carousel:hover::before,
:global(.dark-mode) .latest-carousel:hover::before {
  background: conic-gradient(from 0deg, #c084fc, #a78bfa, #c084fc);
}

:global(.dark) .latest-carousel:hover::after,
:global(.dark-mode) .latest-carousel:hover::after {
  background: conic-gradient(from 180deg, #a78bfa, #c084fc, #a78bfa);
}

:global(.dark) .btn,
:global(.dark-mode) .btn {
  color: #d1d5db;
}

:global(.dark) .btn:hover,
:global(.dark-mode) .btn:hover {
  color: #f3f4f6;
}

.btn {
  font-size: 17px;
  background: transparent;
  border: none;
  padding: 0.75em 1.5em;
  color: #6b7280;
  text-transform: uppercase;
  position: relative;
  transition: 0.5s ease;
  cursor: pointer;
  margin: 8px 0 12px;
  flex-shrink: 0;
  font-weight: 600;
  border-radius: 8px;
}

.btn::before {
  content: "";
  position: absolute;
  left: 0;
  bottom: 0;
  height: 2px;
  width: 0;
  background-color: #ffc506;
  transition: 0.5s ease;
}

.btn:hover {
  color: #1f2937;
  transition-delay: 0.5s;
}

.btn:hover::before {
  width: 100%;
}

.btn::after {
  content: "";
  position: absolute;
  left: 0;
  bottom: 0;
  height: 0;
  width: 100%;
  background-color: #ffc506;
  transition: 0.4s ease;
  z-index: -1;
}

.btn:hover::after {
  height: 100%;
  transition-delay: 0.4s;
  color: aliceblue;
}
</style>
