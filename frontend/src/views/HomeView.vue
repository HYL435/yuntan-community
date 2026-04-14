<script setup lang="ts" name="HomeView">
import { ref, onMounted, onUnmounted, reactive, nextTick } from 'vue';
import { useRouter } from 'vue-router';
import Header from '@/layouts/Header.vue';
import GridBackground from '@/components/background/GridBackground.vue';
import Rain from '@/components/background/Rain.vue';
import BloggerCard from '@/components/cards/BloggerCard.vue';
import SeparateLine from '@/components/separate/SeparateLine.vue';
import ArticleTwoCard from '@/components/cards/ArticleTwoCard.vue';
import LatestCarousel from '@/components/separate/LatestCarousel.vue';
import { getCategories, CategoryFrontVO } from '@/api/category';
import { getFrontAnnouncement } from '@/api/announcement'
import http from '@/api/http';
import AnnouncementBar from '@/components/common/AnnouncementBar.vue';
import StatsSidebar from '@/components/common/StatsSidebar.vue';
import { ElMessage } from 'element-plus'
import { ArrowRight } from '@element-plus/icons-vue'
// 标签类型
interface TagFrontVO {
  id: number;
  tagName: string;
  articleCount?: number;
}

const tags = ref<TagFrontVO[]>([]);

// 公告栏文本
const announcementTitle = ref('网站公告')
const announcementContent = ref('欢迎来到云坛 — 我们已升级界面与评论管理功能，体验更顺畅。')
const announcementLink = ref('')
const announcementPublishTime = ref('')
const showAnnouncement = ref(false)

const titleScale = ref(1);
const titleOffset = ref(0);
const isDarkMode = ref(false);
const heroIntroVisible = ref(false)
const router = useRouter();

const categories = ref<CategoryFrontVO[]>([]);
const categoryArticles = ref<Record<string, any[]>>({});
const rightSidebarRef = ref<HTMLElement | null>(null)
const sidebarStickyTop = ref(96)
let rightSidebarResizeObserver: ResizeObserver | null = null
let themeMutationObserver: MutationObserver | null = null
let scrollRafId: number | null = null
// 分页设置：主页面每个分类每页显示数量
const PAGE_SIZE = 3
const categoryPage = reactive<Record<string, number>>({})
const categoryTotal = reactive<Record<string, number>>({})
const categoryHasMore = reactive<Record<string, boolean>>({})

const poetryPool = [
  '山重水复疑无路，柳暗花明又一村。',
  '长风破浪会有时，直挂云帆济沧海。',
  '海内存知己，天涯若比邻。',
  '且将新火试新茶，诗酒趁年华。',
  '纸上得来终觉浅，绝知此事要躬行。',
  '苔花如米小，也学牡丹开。',
  '星垂平野阔，月涌大江流。',
  '会当凌绝顶，一览众山小。',
  '欲买桂花同载酒，终不似，少年游。',
  '云想衣裳花想容，春风拂槛露华浓。',
]

const displayedPoetry = ref('')
let lastPoetry = ''
let poetryTypeTimer: number | null = null
let poetrySwitchTimer: number | null = null
let heroIntroTimer: number | null = null

const getRandomPoetry = (): string => {
  if (poetryPool.length <= 1) return poetryPool[0] || ''
  let next = poetryPool[Math.floor(Math.random() * poetryPool.length)]
  while (next === lastPoetry) {
    next = poetryPool[Math.floor(Math.random() * poetryPool.length)]
  }
  lastPoetry = next
  return next
}

const clearPoetryTimers = () => {
  if (poetryTypeTimer != null) {
    window.clearTimeout(poetryTypeTimer)
    poetryTypeTimer = null
  }
  if (poetrySwitchTimer != null) {
    window.clearTimeout(poetrySwitchTimer)
    poetrySwitchTimer = null
  }
}

const typePoetry = (line: string, index = 0) => {
  if (index === 0) displayedPoetry.value = ''
  if (index < line.length) {
    displayedPoetry.value += line[index]
    poetryTypeTimer = window.setTimeout(() => typePoetry(line, index + 1), 200)
    return
  }
  poetrySwitchTimer = window.setTimeout(() => {
    playNextPoetry()
  }, 1400)
}

const playNextPoetry = () => {
  const nextLine = getRandomPoetry()
  typePoetry(nextLine, 0)
}

const startPoetryLoop = () => {
  clearPoetryTimers()
  playNextPoetry()
}

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

const normalizeArticle = (raw: any) => ({
  ...raw,
  id: raw?.id ?? raw?.articleId,
  title: raw?.title ?? raw?.articleTitle ?? '',
  summary: raw?.summary ?? raw?.description ?? raw?.intro ?? '',
  coverImg: raw?.coverImg ?? raw?.cover ?? raw?.coverUrl ?? '',
  category: raw?.category ?? raw?.categoryName ?? '',
  tags: normalizeTags(raw?.tags || raw?.tagNames || raw?.tagList || raw?.tagName),
  publishTime: raw?.publishTime ?? raw?.createTime ?? raw?.createdAt ?? '',
  likeCount: Number(raw?.likeCount ?? raw?.likes ?? 0),
  commentCount: Number(raw?.commentCount ?? raw?.comments ?? 0),
  collectCount: Number(raw?.collectCount ?? raw?.bookmarks ?? 0),
  viewCount: Number(raw?.viewCount ?? raw?.views ?? raw?.heat ?? 0),
})

const extractTotal = (payload: any, recordsLen: number): number => {
  const normalized = normalizePayload(payload)
  const totalRaw = normalized?.data?.data?.total
    ?? normalized?.data?.total
    ?? normalized?.total
    ?? normalized?.data?.data?.totalCount
    ?? normalized?.data?.totalCount
    ?? normalized?.totalCount
  const total = Number(totalRaw)
  return Number.isFinite(total) && total >= 0 ? total : recordsLen
}

const updateSidebarStickyTop = () => {
  const sidebarEl = rightSidebarRef.value
  if (!sidebarEl) return
  const viewportHeight = window.innerHeight || document.documentElement.clientHeight
  const minTop = -120
  const bottomGap = 4
  const squeezeOffset = 62
  const sidebarHeight = Math.ceil(sidebarEl.getBoundingClientRect().height)
  const top = viewportHeight - sidebarHeight - bottomGap - squeezeOffset
  sidebarStickyTop.value = Math.max(minTop, top)
}

const fetchCategoryPage = async (catId: number | string, pageNo = 1) => {
  const catKey = String(catId)
  if (!catKey || catKey === 'undefined' || catKey === 'null') {
    return []
  }
  try {
    const articlesRes = await http.get('/front/articles/page/categoryOrTags', {
      params: {
        pageNo,
        pageSize: PAGE_SIZE,
        // Keep long id precision by sending string directly.
        categoryId: catKey,
      },
    })
    const payload = normalizePayload(articlesRes?.data || articlesRes || {})
    const records = extractList(payload).map(normalizeArticle)
    const totalCount = extractTotal(payload, records.length)
    categoryTotal[catKey] = Number(totalCount) || 0
    categoryHasMore[catKey] = pageNo * PAGE_SIZE < (categoryTotal[catKey] || records.length)
    return records
  } catch (err) {
    console.error('获取分类文章失败', err)
    ElMessage.error('加载文章失败')
    return []
  }
}

const loadNext = async (catId: number | string) => {
  const catKey = String(catId)
  const nextPage = (categoryPage[catKey] || 1) + 1
  const records = await fetchCategoryPage(catId, nextPage)
  if (!records || records.length === 0) {
    ElMessage.info('没有更多文章了')
    return
  }
  categoryPage[catKey] = nextPage
  categoryArticles.value[catKey] = [
    ...(categoryArticles.value[catKey] || []),
    ...records,
  ]
}

const fetchHomeAnnouncement = async () => {
  const announcement = await getFrontAnnouncement()
  if (!announcement) {
    announcementTitle.value = '欢迎来到云坛'
    announcementContent.value = '欢迎访问云坛社区，愿你在这里发现灵感、分享经验、结识同路人。'
    announcementLink.value = ''
    announcementPublishTime.value = ''
    showAnnouncement.value = true
    return
  }
  announcementTitle.value = announcement.title || '网站公告'
  announcementContent.value = announcement.content || ''
  announcementLink.value = announcement.link || ''
  announcementPublishTime.value = announcement.publishTime || announcement.updateTime || ''
  showAnnouncement.value = !!announcement.content
}

onMounted(async () => {
  await fetchHomeAnnouncement()
  // 分类及文章（每页显示 PAGE_SIZE 条）
  const res = await getCategories();
  categories.value = res;
  await Promise.all(
    res.map(async (cat) => {
      const catKey = String(cat.id)
      categoryPage[catKey] = 1
      const records = await fetchCategoryPage(cat.id, 1)
      categoryArticles.value[catKey] = records || []
    })
  )

  // 标签及每个标签的文章总数
  try {
    const tagRes = await http.get('/front/tags');
    if (Array.isArray(tagRes.data?.data)) {
      const tagList = tagRes.data.data;
      // 并发请求每个标签的文章总数
      const tagWithCount = await Promise.all(tagList.map(async (tag: TagFrontVO) => {
        try {
          const res = await http.get('/front/articles/page/categoryOrTags', {
            params: {
              pageNo: 1,
              pageSize: 1,
              tagId: tag.id,
            },
          });
          let total = 0;
          if (res.data?.data) {
            if (typeof res.data.data.total === 'number') {
              total = res.data.data.total;
            } else if (Array.isArray(res.data.data)) {
              total = res.data.data.length;
            }
          }
          return { ...tag, articleCount: total };
        } catch {
          return { ...tag, articleCount: 0 };
        }
      }));
      tags.value = tagWithCount;
    } else {
      tags.value = [];
    }
  } catch (e) {
    tags.value = [];
  }

  await nextTick()
  updateSidebarStickyTop()
});

const applyScrollState = () => {
  const scrollTop = window.scrollY || document.documentElement.scrollTop;
  const startScroll = 0;
  
  if (scrollTop > startScroll) {
    titleScale.value = Math.max(0.6, 1 - scrollTop / 800);
    titleOffset.value = scrollTop;
  } else {
    titleScale.value = 1;
    titleOffset.value = 0;
  }
};

const handleScroll = () => {
  if (scrollRafId != null) return
  scrollRafId = window.requestAnimationFrame(() => {
    scrollRafId = null
    applyScrollState()
  })
}

const goToArticle = (id?: string | number) => {
  if (!id) return;
  router.push(`/article/${id}`);
};

const goToTag = (tag?: string) => {
  if (!tag) return;
  router.push(`/tag/${encodeURIComponent(tag)}`);
};

onMounted(() => {
  startPoetryLoop()
  heroIntroTimer = window.setTimeout(() => {
    heroIntroVisible.value = true
  }, 120)
  applyScrollState();
  updateSidebarStickyTop()
  window.addEventListener('scroll', handleScroll);
  window.addEventListener('resize', updateSidebarStickyTop)
  const checkDark = () => {
    const html = document.documentElement.classList;
    const body = document.body.classList;
    return (
      html.contains('dark') ||
      html.contains('dark-mode') ||
      body.contains('dark') ||
      body.contains('dark-mode')
    );
  };
  isDarkMode.value = checkDark();
  themeMutationObserver = new MutationObserver(() => isDarkMode.value = checkDark());
  themeMutationObserver.observe(document.documentElement, { attributes: true, attributeFilter: ['class'] });
  themeMutationObserver.observe(document.body, { attributes: true, attributeFilter: ['class'] });

  if (typeof ResizeObserver !== 'undefined') {
    rightSidebarResizeObserver = new ResizeObserver(() => updateSidebarStickyTop())
    if (rightSidebarRef.value) {
      rightSidebarResizeObserver.observe(rightSidebarRef.value)
    }
  }
});

onUnmounted(() => {
  clearPoetryTimers()
  if (heroIntroTimer != null) {
    window.clearTimeout(heroIntroTimer)
    heroIntroTimer = null
  }
  window.removeEventListener('scroll', handleScroll);
  window.removeEventListener('resize', updateSidebarStickyTop)
  if (scrollRafId != null) {
    cancelAnimationFrame(scrollRafId)
    scrollRafId = null
  }
  if (rightSidebarResizeObserver) {
    rightSidebarResizeObserver.disconnect()
    rightSidebarResizeObserver = null
  }
  if (themeMutationObserver) {
    themeMutationObserver.disconnect()
    themeMutationObserver = null
  }
});
</script>

<template>
  <!-- 外层容器：控制整体背景色 -->
  <div class="home-view min-h-screen transition-colors duration-300 bg-[#F7F9FE] dark:bg-[#121212]">
    
    <!-- 0. 顶部导航 -->
    <div class="fixed top-0 w-full z-50">
      <Header />
    </div>

    <!-- 1. 背景与标题区域 -->
    <GridBackground>
      <main 
        class="container mx-auto px-4 text-center min-h-screen flex flex-col justify-center items-center" 
        :style="{ 
          position: 'fixed', 
          top: 0, 
          left: 0, 
          right: 0, 
          width: '100%', 
          transform: `scale(${titleScale})`, 
          opacity: Math.max(0, 1 - (titleOffset / 500)), 
          transformOrigin: 'center 40%', 
          transition: 'transform 0.1s ease-out', 
          pointerEvents: 'none', 
          zIndex: 0 
        }"
      >
        <div class="hero-intro" :class="{ 'is-visible': heroIntroVisible }">
          <!-- 【修复】标题文字颜色优化 -->
          <h1 class="text-6xl md:text-7xl font-bold mb-6 tracking-tight">
            <span class="hero-title-ink">
              欢迎来到云坛
            </span>
          </h1>
          <!-- 【修复】副标题颜色提高对比度 -->
          <p class="hero-subtitle text-xl md:text-2xl text-gray-600 dark:text-slate-300 font-medium">
            探索技术的无限可能
          </p>
          <div class="hero-line-wrap" aria-hidden="true">
            <span class="hero-line hero-line-left"></span>
            <span class="hero-dot"></span>
            <span class="hero-line hero-line-right"></span>
          </div>
          <div class="hero-meta mt-8" aria-live="polite">
            <p class="hero-poetry-line">
              <span class="hero-poetry-text">{{ displayedPoetry }}</span>
              <span class="hero-poetry-caret" aria-hidden="true"></span>
            </p>
          </div>
        </div>
      </main>
    </GridBackground>

    <!-- 2. 雨滴层 -->
    <Rain class="fixed inset-0 z-0 pointer-events-none opacity-60" />

    <!-- 2.1 氛围光斑 -->
    <div class="ambient ambient-a"></div>
    <div class="ambient ambient-b"></div>
    <div class="ambient ambient-c"></div>

    <!-- 3. 主要内容区域 -->
    <div class="relative z-10 w-full pb-16">
      
      <!-- 占位符：取消间隔，让内容紧贴雨滴层 -->
      <div class="w-full h-0"></div>

      <!-- 
         【修复】卡片样式优化
         1. 深色模式使用不透明深灰，避免文字模糊
         2. 增加毛玻璃效果，保持一致性
         3. 优化边框和阴影
      -->
      <div 
        class="home-panel max-w-[1400px] mx-auto p-6 md:p-10 lg:p-14 
               rounded-t-[2.5rem] md:rounded-[2.5rem] 
               shadow-2xl transition-all duration-300
               border-t border-x border-b-0 
               border-[#F7F9FE] dark:border-[#121212]
               bg-[#F7F9FE] dark:bg-[#1E1E1E]
               backdrop-blur-md"
      >
        <div class="max-w-7xl mx-auto">
          <div class="flow-ornaments mb-8" aria-hidden="true">
            <span class="flow-line flow-line-a"></span>
            <span class="flow-line flow-line-b"></span>
            <span class="flow-dot flow-dot-a"></span>
            <span class="flow-dot flow-dot-b"></span>
          </div>

          <div class="home-content-grid grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 lg:gap-10">
            
            <!-- 左边：文章列表 -->
            <div class="order-2 md:col-span-2 lg:order-1 lg:col-span-3 space-y-12">
              
              <!-- 第一部分：最新发布 -->
              <section>
                <AnnouncementBar
                  v-if="showAnnouncement"
                  :title="announcementTitle"
                  :content="announcementContent"
                  :link="announcementLink"
                  :publish-time="announcementPublishTime"
                  :closable="false"
                  class="mb-10"
                />
                <LatestCarousel title="推荐文章" />
              </section>

              <!-- 第二部分：深度精选 -->
              <section>
                <template v-if="categories.length">
                  <div v-for="cat in categories" :key="cat.id">
                    <SeparateLine :title="cat" :data-category-id="cat.id" class="mb-8 elegant-separator" />
                    <div class="grid grid-cols-1 gap-8">
                      <div v-if="!categoryArticles[cat.id] || !categoryArticles[cat.id].length" class="text-center text-gray-400 py-8">
                        暂无文章
                      </div>
                      <div v-else>
                        <div v-for="article in categoryArticles[cat.id]" :key="article.id" class="mb-8 last:mb-0">
                          <ArticleTwoCard
                            :articleId="article.id"
                            :title="article.title"
                            :coverUrl="article.coverImg"
                            :publishTime="article.publishTime"
                            :heat="Number(article.viewCount)"
                            :comments="Number(article.commentCount)"
                            :likes="Number(article.likeCount)"
                            :collectCount="Number(article.collectCount)"
                            :excerpt="article.summary"
                            :primaryTag="article.category || cat.categoryName"
                            :secondaryTag="article.tags && article.tags.length ? article.tags[0] : ''"
                            :imagePosition="'right'"
                            @click="goToArticle"
                          />
                        </div>
                      </div>
                      <div class="flex justify-end mt-2">
                        <el-button
                          size="medium"
                          :disabled="!categoryHasMore[cat.id]"
                          @click="loadNext(cat.id)"
                          class="rounded-full px-6 py-2 flex items-center justify-center gap-3 text-base font-medium transition-shadow duration-200"
                          :style="{
                            backgroundColor: isDarkMode ? '#1E1E1E' : '#F7F9FE',
                            border: '1px solid ' + (isDarkMode ? '#2b2b2b' : '#F7F9FE'),
                            minWidth: '180px',
                            borderRadius: '9999px'
                          }"
                          :class="categoryHasMore[cat.id] ? 'shadow-md hover:shadow-lg' : 'opacity-60 cursor-not-allowed'"
                        >
                          <span>下一页</span>
                          <el-icon><ArrowRight /></el-icon>
                        </el-button>
                      </div>
                    </div>
                  </div>
                </template>
              </section>
              
            </div>
            
            <!-- 右边：侧边栏 -->
            <div class="order-1 md:col-span-2 lg:order-2 lg:col-span-1">
              <div
                ref="rightSidebarRef"
                class="home-sidebar lg:sticky lg:space-y-6 transition-[top] duration-200"
                :style="{ top: `${sidebarStickyTop}px` }"
              >
                <div class="home-sidebar-blogger">
                  <BloggerCard />
                </div>
                
                <!-- 【修复】侧边栏标签云文字颜色 -->
                <div class="home-sidebar-topics p-5 rounded-2xl bg-gray-50 dark:bg-[#252525] 
                           border border-gray-100 dark:border-slate-700/50">
                  <h3 class="font-bold mb-3 flex items-center gap-2
                             text-gray-800 dark:text-slate-200">
                    <span>🏷️</span> 热门话题
                  </h3>
                  <div class="flex flex-wrap gap-2">
                    <template v-if="tags.length">
                      <span
                        v-for="tag in tags"
                        :key="tag.id"
                        class="px-3 py-1 bg-white dark:bg-[#333] 
                               border border-gray-200 dark:border-slate-600 
                               rounded-full text-xs 
                               text-gray-700 dark:text-slate-300
                               hover:bg-indigo-500 hover:text-white 
                               dark:hover:bg-indigo-600 transition-colors cursor-pointer"
                        @click="goToTag(tag.tagName)"
                      >
                        #{{ tag.tagName }}
                      </span>
                    </template>
                    <template v-else>
                      <span class="text-gray-400 text-xs">暂无标签</span>
                    </template>
                  </div>
                </div>
                <div class="home-sidebar-stats">
                  <StatsSidebar />
                </div>
              </div>
            </div>

          </div>
        </div>
      </div>

    </div>
  </div>
</template>

<style scoped>
.home-view {
  position: relative;
  overflow: hidden;
}

.ambient {
  position: fixed;
  z-index: 0;
  pointer-events: none;
  filter: blur(48px);
  opacity: 0.42;
}

.ambient-a {
  top: 15%;
  left: -6%;
  width: 320px;
  height: 320px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(13, 148, 136, 0.32), transparent 70%);
}

.ambient-b {
  top: 46%;
  right: -4%;
  width: 360px;
  height: 360px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(59, 130, 246, 0.26), transparent 72%);
}

.ambient-c {
  bottom: 6%;
  left: 34%;
  width: 280px;
  height: 280px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(16, 185, 129, 0.2), transparent 72%);
}

.hero-meta {
  display: flex;
  justify-content: center;
  min-height: 38px;
}

.hero-intro {
  opacity: 0;
  transform: translateY(84px);
  transition: opacity 1.45s ease, transform 1.45s cubic-bezier(0.22, 1, 0.36, 1);
}

.hero-intro.is-visible {
  opacity: 1;
  transform: translateY(0);
}

.hero-title-ink {
  font-family: 'Ma Shan Zheng', 'STXingkai', 'FZShuTi', 'HanziPen SC', 'KaiTi', 'Kaiti SC', cursive;
  letter-spacing: 0.08em;
  font-weight: 500;
  background: linear-gradient(120deg, #0f172a 0%, #0f766e 50%, #0369a1 100%);
  background-clip: text;
  -webkit-background-clip: text;
  color: transparent;
  text-shadow: 0 16px 34px rgba(15, 23, 42, 0.16);
  animation: ink-breathe 3.2s ease-in-out infinite;
}

.dark .hero-title-ink {
  background: linear-gradient(120deg, #fda4af 0%, #fb7185 42%, #fecdd3 100%);
  background-clip: text;
  -webkit-background-clip: text;
  color: transparent;
  text-shadow: 0 16px 34px rgba(244, 63, 94, 0.18);
}

.hero-subtitle {
  letter-spacing: 0.08em;
}

.hero-line-wrap {
  margin-top: 18px;
  display: inline-flex;
  align-items: center;
  gap: 10px;
}

.hero-line {
  display: block;
  width: 90px;
  height: 2px;
  border-radius: 999px;
  background: linear-gradient(90deg, rgba(15, 118, 110, 0), rgba(15, 118, 110, 0.8), rgba(15, 118, 110, 0));
  animation: line-pulse 2.2s ease-in-out infinite;
}

.hero-line-right {
  animation-delay: 0.45s;
}

.hero-dot {
  width: 9px;
  height: 9px;
  border-radius: 50%;
  background: #0f766e;
  box-shadow: 0 0 0 0 rgba(15, 118, 110, 0.45);
  animation: dot-jump 1.4s ease-in-out infinite;
}

.dark .hero-line {
  background: linear-gradient(90deg, rgba(244, 63, 94, 0), rgba(244, 63, 94, 0.82), rgba(244, 63, 94, 0));
}

.dark .hero-dot {
  background: #fb7185;
  box-shadow: 0 0 0 0 rgba(251, 113, 133, 0.5);
}

.hero-poetry-line {
  margin: 0;
  max-width: min(88vw, 760px);
  text-align: center;
  line-height: 1.55;
  font-size: 18px;
  letter-spacing: 0.06em;
  color: rgba(15, 23, 42, 0.88);
  min-height: 1.7em;
  display: inline-flex;
  align-items: baseline;
  justify-content: center;
}

.hero-poetry-text {
  font-family: 'Noto Serif SC', 'Songti SC', 'STSong', serif;
}

.hero-poetry-caret {
  width: 1px;
  height: 1em;
  margin-left: 4px;
  background: rgba(15, 23, 42, 0.6);
  animation: caret-blink 0.9s steps(1, end) infinite;
}

.dark .hero-poetry-line {
  color: rgba(248, 250, 252, 0.9);
}

.dark .hero-poetry-caret {
  background: rgba(248, 250, 252, 0.7);
}

@keyframes caret-blink {
  0% {
    opacity: 1;
  }
  50% {
    opacity: 0;
  }
}

.flow-ornaments {
  position: relative;
  height: 24px;
}

.flow-line {
  position: absolute;
  left: 0;
  right: 0;
  margin: 0 auto;
  height: 2px;
  border-radius: 999px;
  background: linear-gradient(90deg, rgba(13, 148, 136, 0), rgba(13, 148, 136, 0.8), rgba(13, 148, 136, 0));
}

.flow-line-a {
  top: 4px;
  width: 86%;
  animation: flow-run 3s ease-in-out infinite;
}

.flow-line-b {
  top: 14px;
  width: 62%;
  opacity: 0.66;
  animation: flow-run 3.4s ease-in-out infinite reverse;
}

.flow-dot {
  position: absolute;
  top: 0;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #0d9488;
}

.flow-dot-a {
  left: 18%;
  top: 1px;
  animation: dot-jump 1.2s ease-in-out infinite;
}

.flow-dot-b {
  right: 18%;
  top: 12px;
  animation: dot-jump 1.2s ease-in-out infinite 0.35s;
}

.dark .flow-line {
  background: linear-gradient(90deg, rgba(244, 63, 94, 0), rgba(244, 63, 94, 0.84), rgba(244, 63, 94, 0));
}

.dark .flow-dot {
  background: #fb7185;
}

/* 保障深色模式下主面板背景与边框生效 */
:global(html.dark) .home-panel,
:global(.dark) .home-panel,
:global(html.dark-mode) .home-panel,
:global(.dark-mode) .home-panel {
  background-color: #1E1E1E !important;
  border-color: #121212 !important;
}

/* 
  【修复】分割线配色优化
  1. 亮色模式：使用深靛青色，背景白色
  2. 深色模式：使用浅靛青色，背景深灰，增加对比度
*/

/* 亮色模式分割线 */
.elegant-separator :deep(.separator-line .line:first-child) {
  background: linear-gradient(to right, transparent, #4f46e5);
}
.elegant-separator :deep(.separator-line .line:last-child) {
  background: linear-gradient(to right, #4f46e5, transparent);
}
.elegant-separator :deep(.separator-title) {
  color: #3730a3; /* Indigo-800 */
  font-weight: 700;
  letter-spacing: 0.05em;
}

/* 深色模式分割线 - 提高对比度 */
:global(.dark) .elegant-separator :deep(.separator-line .line:first-child) {
  background: linear-gradient(to right, transparent, #a5b4fc);
}
:global(.dark) .elegant-separator :deep(.separator-line .line:last-child) {
  background: linear-gradient(to right, #a5b4fc, transparent);
}
:global(.dark) .elegant-separator :deep(.separator-title) {
  color: #c7d2fe; /* Indigo-100 */
  font-weight: 700;
}

/* 分割线装饰点 */
.elegant-separator :deep(.separator-title::before),
.elegant-separator :deep(.separator-title::after) {
  background: #7c3aed; /* Violet-600 */
  border-radius: 50%;
  width: 6px;
  height: 6px;
}
:global(.dark) .elegant-separator :deep(.separator-title::before),
:global(.dark) .elegant-separator :deep(.separator-title::after) {
  background: #a78bfa; /* Violet-400 */
}

/* 标题渐变动画 */
@keyframes gradient-move {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}
.animate-gradient {
  background-size: 200% auto;
  animation: gradient-move 4s linear infinite;
}

@keyframes ink-breathe {
  0%,
  100% { transform: translateY(0); filter: saturate(1); }
  50% { transform: translateY(-2px); filter: saturate(1.15); }
}

@keyframes line-pulse {
  0%,
  100% { opacity: 0.55; transform: scaleX(0.85); }
  50% { opacity: 1; transform: scaleX(1); }
}

@keyframes flow-run {
  0%,
  100% { transform: scaleX(0.92); opacity: 0.45; }
  50% { transform: scaleX(1); opacity: 1; }
}

@keyframes dot-jump {
  0%,
  100% { transform: translateY(0) scale(1); box-shadow: 0 0 0 0 rgba(13, 148, 136, 0.35); }
  50% { transform: translateY(-5px) scale(1.08); box-shadow: 0 0 0 8px rgba(13, 148, 136, 0); }
}

@media (max-width: 1024px) {
  .home-panel {
    padding: 1.5rem 1rem 2rem;
    border-radius: 2rem 2rem 0 0;
  }

  .home-content-grid {
    gap: 1.25rem;
  }

  .home-sidebar {
    display: grid;
    gap: 1rem;
  }

  .home-sidebar-blogger,
  .home-sidebar-topics,
  .home-sidebar-stats {
    min-width: 0;
  }

  .home-sidebar-topics {
    padding: 1rem;
    border-radius: 1.5rem;
    background: linear-gradient(135deg, rgba(255, 255, 255, 0.88), rgba(241, 245, 249, 0.94));
    box-shadow: 0 20px 40px rgba(15, 23, 42, 0.06);
  }

  :global(.dark) .home-sidebar-topics {
    background: linear-gradient(135deg, rgba(37, 37, 37, 0.96), rgba(30, 41, 59, 0.86));
    box-shadow: 0 22px 42px rgba(0, 0, 0, 0.22);
  }

  .home-sidebar-topics h3 {
    margin-bottom: 0.875rem;
  }

  .home-sidebar-topics > div:last-child {
    gap: 0.5rem;
  }

  .home-sidebar-topics > div:last-child > span {
    padding: 0.5rem 0.85rem;
    border-radius: 999px;
    font-size: 0.75rem;
  }

  .hero-line {
    width: 64px;
  }
}

@media (min-width: 768px) and (max-width: 1024px) {
  .home-panel {
    padding: 1.75rem 1.25rem 2.25rem;
    border-radius: 2.2rem 2.2rem 0 0;
  }

  .home-sidebar {
    grid-template-columns: minmax(0, 1.2fr) minmax(0, 0.8fr);
    align-items: start;
    gap: 1.1rem;
  }

  .home-sidebar-blogger {
    grid-column: 1 / -1;
  }

  .home-sidebar-blogger,
  .home-sidebar-topics,
  .home-sidebar-stats {
    height: 100%;
    position: relative;
    overflow: hidden;
    border-radius: 1.65rem;
  }

  .home-sidebar-topics,
  .home-sidebar-stats {
    border: 1px solid rgba(148, 163, 184, 0.22);
    background: rgba(255, 255, 255, 0.56);
    box-shadow: 0 14px 32px rgba(15, 23, 42, 0.05);
  }

  .home-sidebar-topics::before,
  .home-sidebar-stats::before {
    content: '';
    position: absolute;
    inset: 0 auto auto 0;
    width: 100%;
    height: 4px;
    background: linear-gradient(90deg, #0f766e, #2563eb, #06b6d4);
    opacity: 0.9;
  }

  :global(html.dark) .home-sidebar-topics,
  :global(html.dark) .home-sidebar-stats,
  :global(.dark) .home-sidebar-topics,
  :global(.dark) .home-sidebar-stats,
  :global(html.dark-mode) .home-sidebar-topics,
  :global(html.dark-mode) .home-sidebar-stats,
  :global(.dark-mode) .home-sidebar-topics,
  :global(.dark-mode) .home-sidebar-stats {
    border-color: rgba(71, 85, 105, 0.48);
    background: rgba(30, 41, 59, 0.32);
    box-shadow: 0 18px 36px rgba(0, 0, 0, 0.2);
  }

  :global(html.dark) .home-sidebar-topics::before,
  :global(html.dark) .home-sidebar-stats::before,
  :global(.dark) .home-sidebar-topics::before,
  :global(.dark) .home-sidebar-stats::before,
  :global(html.dark-mode) .home-sidebar-topics::before,
  :global(html.dark-mode) .home-sidebar-stats::before,
  :global(.dark-mode) .home-sidebar-topics::before,
  :global(.dark-mode) .home-sidebar-stats::before {
    background: linear-gradient(90deg, #fb7185, #f97316, #f59e0b);
  }
}

@media (max-width: 767px) {
  .home-panel {
    padding: 1.1rem 0.8rem 1.7rem;
    border-radius: 1.75rem 1.75rem 0 0;
  }

  .flow-ornaments {
    margin-bottom: 1rem;
    height: 18px;
  }

  .home-sidebar {
    gap: 0.875rem;
  }

  .home-sidebar-blogger,
  .home-sidebar-topics {
    position: relative;
    overflow: hidden;
    border-radius: 1.4rem;
    backdrop-filter: blur(18px);
  }

  .home-sidebar-topics,
  .home-sidebar-stats {
    box-shadow: 0 18px 36px rgba(15, 23, 42, 0.09);
  }

  .home-sidebar-topics {
    padding: 0.95rem 0.9rem 1rem;
    background: rgba(255, 255, 255, 0.64);
    border: 1px solid rgba(226, 232, 240, 0.82);
  }

  .home-sidebar-topics::after {
    content: '';
    position: absolute;
    top: -24px;
    right: -16px;
    width: 96px;
    height: 96px;
    border-radius: 999px;
    background: radial-gradient(circle, rgba(59, 130, 246, 0.2), transparent 68%);
    pointer-events: none;
  }

  :global(html.dark) .home-sidebar-topics,
  :global(html.dark) .home-sidebar-stats,
  :global(.dark) .home-sidebar-topics,
  :global(.dark) .home-sidebar-stats,
  :global(html.dark-mode) .home-sidebar-topics,
  :global(html.dark-mode) .home-sidebar-stats,
  :global(.dark-mode) .home-sidebar-topics,
  :global(.dark-mode) .home-sidebar-stats {
    box-shadow: 0 18px 36px rgba(0, 0, 0, 0.24);
  }

  :global(html.dark) .home-sidebar-topics,
  :global(.dark) .home-sidebar-topics,
  :global(html.dark-mode) .home-sidebar-topics,
  :global(.dark-mode) .home-sidebar-topics {
    background: rgba(30, 41, 59, 0.34);
    border-color: rgba(148, 163, 184, 0.18);
  }

  .home-sidebar-topics h3 {
    font-size: 0.95rem;
    letter-spacing: 0.04em;
  }

  .home-sidebar-topics > div:last-child {
    gap: 0.45rem;
  }

  .home-sidebar-topics > div:last-child > span {
    padding: 0.42rem 0.78rem;
    border-radius: 0.95rem;
    font-size: 0.72rem;
    background: rgba(248, 250, 252, 0.78);
    border: 1px solid rgba(226, 232, 240, 0.9);
  }

  :global(html.dark) .home-sidebar-topics > div:last-child > span,
  :global(.dark) .home-sidebar-topics > div:last-child > span,
  :global(html.dark-mode) .home-sidebar-topics > div:last-child > span,
  :global(.dark-mode) .home-sidebar-topics > div:last-child > span {
    background: rgba(51, 65, 85, 0.48);
    border-color: rgba(100, 116, 139, 0.42);
  }
}
</style>
