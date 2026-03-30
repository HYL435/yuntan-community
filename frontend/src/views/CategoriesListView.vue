<template>
  <div class="category-page min-h-screen text-slate-900 transition-colors duration-300 dark:text-white pb-16" :class="{ 'category-page--dark': isDarkPage }">
    <div class="mx-auto max-w-7xl px-6 py-16 lg:px-8">
      <p class="text-xs uppercase tracking-[0.24em] text-slate-500 dark:text-slate-400">Categories</p>
      <h1 class="mt-4 text-4xl font-semibold">分类页面</h1>

      <div class="mt-12">
        <nav
          ref="navRef"
          class="category-nav dark:rounded-xl dark:border dark:border-slate-700/60 dark:bg-slate-900/55 dark:px-4 dark:pt-3 dark:pb-5 dark:backdrop-blur-sm"
          aria-label="分类导航"
        >
          <ul>
            <li
              v-for="(item, index) in categories"
              :key="item.id"
              :class="{ active: index === activeIndex }"
            >
              <a
                href="#"
                class="category-nav-link"
                :class="index === activeIndex
                  ? 'text-slate-900 dark:text-white'
                  : 'text-slate-500 hover:text-slate-800 dark:text-white/75 dark:hover:text-white/95'"
                @click.prevent="onTabClick(index)"
              >
                {{ item.categoryName }}
              </a>
            </li>
          </ul>
          <div class="line" :style="lineStyle"></div>
        </nav>
      </div>

      <div class="mt-10 grid gap-6 lg:grid-cols-[1fr_310px]">
        <section>
          <div v-if="loading" class="rounded-2xl border border-slate-200 bg-white p-8 text-sm text-slate-600 dark:border-slate-800 dark:bg-slate-900/50 dark:text-slate-300">
            正在加载分类文章...
          </div>

          <div v-else-if="!articles.length" class="rounded-2xl border border-slate-200 bg-white p-8 text-sm text-slate-600 dark:border-slate-800 dark:bg-slate-900/50 dark:text-slate-300">
            当前分类暂无文章。
          </div>

          <div v-else class="space-y-5">
            <CategoryArticleCard
              v-for="article in articles"
              :key="article.id"
              :article="article"
              @open="goToArticle"
            />
          </div>

          <div class="mt-8 flex items-center justify-center" v-if="total > pageSize">
            <button
              class="load-more-btn"
              type="button"
              :disabled="loadingMore || !hasMore"
              @click="loadMore"
            >
              {{ loadingMore ? '加载中...' : hasMore ? '加载更多' : '没有更多了' }}
            </button>
          </div>
        </section>

        <aside class="space-y-4 lg:sticky lg:top-24 lg:self-start">
          <CategoryStatCard label="当前分类" :value="currentCategory?.categoryName || '--'" />
          <CategoryStatCard label="文章总数" :value="total" desc="来自分类分页接口" />
          <CategoryStatCard label="当前页码" :value="pageNo" :desc="`每页 ${pageSize} 篇`" />
        </aside>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts" name="CategoriesListView">
import { computed, nextTick, onMounted, onUnmounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  getCategories,
  getArticlesByCategory,
  type CategoryFrontVO,
  type CategoryArticleVO,
} from '@/api/category'
import CategoryArticleCard from '@/components/cards/CategoryArticleCard.vue'
import CategoryStatCard from '@/components/cards/CategoryStatCard.vue'

const route = useRoute()
const router = useRouter()

const categories = ref<CategoryFrontVO[]>([])
const activeIndex = ref(0)
const navRef = ref<HTMLElement | null>(null)
const lineStyle = ref<Record<string, string>>({ left: '0px', width: '0px' })
const articles = ref<CategoryArticleVO[]>([])
const loading = ref(false)
const loadingMore = ref(false)
const total = ref(0)
const pageNo = ref(1)
const pageSize = 8
const hasMore = computed(() => pageNo.value * pageSize < total.value)
const isDarkPage = ref(false)
let darkObserver: MutationObserver | null = null

const currentCategory = computed(() => categories.value[activeIndex.value])

const syncDarkState = () => {
  const html = document.documentElement
  isDarkPage.value =
    html.classList.contains('dark') ||
    html.classList.contains('dark-mode') ||
    localStorage.getItem('dark-mode') === 'true'
}

const syncLine = () => {
  const nav = navRef.value
  if (!nav) return
  const activeLi = nav.querySelector('li.active') as HTMLElement | null
  if (!activeLi) return
  lineStyle.value = {
    left: `${activeLi.offsetLeft}px`,
    width: `${activeLi.offsetWidth}px`,
  }
}

const setActiveByName = async (name: string) => {
  if (!name || categories.value.length === 0) return
  const idx = categories.value.findIndex((c) => c.categoryName === name)
  if (idx >= 0) {
    activeIndex.value = idx
    await nextTick()
    syncLine()
  }
}

const fetchArticles = async (reset = true) => {
  if (!currentCategory.value?.id) {
    articles.value = []
    total.value = 0
    return
  }

  if (reset) {
    loading.value = true
    pageNo.value = 1
  } else {
    loadingMore.value = true
  }

  try {
    const result = await getArticlesByCategory(currentCategory.value.id, pageNo.value, pageSize)
    total.value = result.total
    articles.value = reset ? result.records : [...articles.value, ...result.records]
  } catch (error) {
    console.error('获取分类文章失败:', error)
    if (reset) {
      articles.value = []
      total.value = 0
    }
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

const onTabClick = async (index: number) => {
  if (index < 0 || index >= categories.value.length) return
  activeIndex.value = index
  const category = categories.value[index]
  await router.replace({
    path: '/categories',
    query: {
      id: String(category.id),
      name: category.categoryName,
    },
  })
  await nextTick()
  syncLine()
}

const loadMore = async () => {
  if (!hasMore.value || loadingMore.value) return
  pageNo.value += 1
  await fetchArticles(false)
}

const goToArticle = (id: string | number) => {
  router.push(`/article/${id}`)
}

const handleResize = () => syncLine()

onMounted(async () => {
  syncDarkState()
  darkObserver = new MutationObserver(() => syncDarkState())
  darkObserver.observe(document.documentElement, { attributes: true, attributeFilter: ['class'] })

  categories.value = await getCategories()
  const queryName = typeof route.query.name === 'string' ? route.query.name : ''
  if (queryName) {
    await setActiveByName(queryName)
  } else if (categories.value.length > 0) {
    activeIndex.value = 0
    const first = categories.value[0]
    await router.replace({
      path: '/categories',
      query: {
        id: String(first.id),
        name: first.categoryName,
      },
    })
  }
  await nextTick()
  syncLine()
  await fetchArticles(true)
  window.addEventListener('resize', handleResize)
})

watch(
  () => route.query.name,
  async (name) => {
    if (typeof name === 'string') {
      await setActiveByName(name)
      await fetchArticles(true)
    }
  }
)

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  if (darkObserver) {
    darkObserver.disconnect()
    darkObserver = null
  }
})
</script>

<style scoped>
.category-page {
  background-color: #86efac;
  background-image: linear-gradient(180deg, #86efac 0%, #bbf7d0 52%, #ecfdf5 100%);
}

:global(html.dark) .category-page,
:global(body.dark) .category-page,
:global(html.dark-mode) .category-page,
:global(body.dark-mode) .category-page,
.category-page--dark {
  background-color: #0a1530;
  background-image: linear-gradient(180deg, #050b18 0%, #0a1530 48%, #1e3a5f 100%);
}

.category-nav {
  position: relative;
  padding-bottom: 12px;
}

.category-nav .line {
  height: 2px;
  position: absolute;
  bottom: 0;
  background: #ff1847;
  transition: left 0.35s ease, width 0.35s ease;
}

.category-nav ul {
  padding: 0;
  margin: 0;
  list-style: none;
  display: flex;
  flex-wrap: wrap;
  gap: 24px;
}

.category-nav ul li {
  opacity: 1;
  transition: transform 0.2s ease;
}

.category-nav ul li.active {
  transform: translateY(-1px);
}

.category-nav-link {
  text-decoration: none;
  text-transform: uppercase;
  display: block;
  font-weight: 600;
  letter-spacing: 0.2em;
  font-size: 14px;
  transition: color 0.25s ease, text-shadow 0.25s ease;
}

.category-nav ul li.active .category-nav-link {
  text-shadow: 0 0 0 transparent;
}

.load-more-btn {
  border: 1px solid rgba(148, 163, 184, 0.35);
  background: #ffffff;
  color: #334155;
  border-radius: 999px;
  padding: 0.55rem 1.4rem;
  font-size: 0.875rem;
  transition: all 0.2s ease;
}

.load-more-btn:hover:not(:disabled) {
  border-color: rgba(56, 189, 248, 0.6);
  color: #7dd3fc;
}

.load-more-btn:disabled {
  opacity: 0.55;
  cursor: not-allowed;
}

:global(html.dark) .category-nav ul li.active a {
  color: #ffffff;
  text-shadow: 0 0 10px rgba(125, 211, 252, 0.35);
}

:global(body.dark) .category-nav ul li.active a,
:global(html.dark-mode) .category-nav ul li.active a,
:global(body.dark-mode) .category-nav ul li.active a {
  color: #ffffff;
  text-shadow: 0 0 12px rgba(125, 211, 252, 0.42);
}

:global(html.dark) .load-more-btn {
  background: rgba(15, 23, 42, 0.7);
  color: #e2e8f0;
}
</style>
