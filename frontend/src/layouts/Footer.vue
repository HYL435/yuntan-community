<template>
  <footer class="relative isolate z-40 border-t border-slate-200 bg-white text-slate-700 dark:border-slate-800 dark:bg-[#050816] dark:text-slate-300">
    <div class="absolute inset-0 -z-10 bg-white dark:bg-[#050816]"></div>
    <div class="mx-auto max-w-7xl px-6 py-14 lg:px-8">
      <div class="grid gap-10 border-b border-slate-200 pb-10 dark:border-slate-800 lg:grid-cols-[1.1fr_1fr] lg:gap-14">
        <section class="max-w-lg">
          <p class="text-xs font-medium uppercase tracking-[0.24em] text-slate-400 dark:text-slate-500">Yuntan Community</p>
          <h2 class="mt-3 text-2xl font-semibold text-slate-950 dark:text-white">雲 壇</h2>
          <p class="mt-4 text-sm leading-7 text-slate-600 dark:text-slate-400">
            云坛是一个个人博客，记录个人知识，分享知识和兴趣。
          </p>
        </section>

        <section class="grid gap-8 sm:grid-cols-2 sm:items-start">
          <div>
            <h3 class="text-sm font-semibold text-slate-950 dark:text-white">导航</h3>
            <ul class="mt-4 space-y-3 text-sm text-slate-600 dark:text-slate-400">
              <li><a href="#" class="footer-link" @click.prevent="goToBookshelf">藏书阁</a></li>
              <li><a href="#" class="footer-link" @click.prevent="goToToolbox">工具箱</a></li>
              <li><a href="#" class="footer-link" @click.prevent="goMessageBoard">留言板</a></li>
              <li><a href="#" class="footer-link" @click.prevent="goToFriendLinks">话题</a></li>
              <li><a href="#" class="footer-link" @click.prevent="goToAboutSite">关于本站</a></li>
              <li><a href="#" class="footer-link" @click.prevent="goToAboutAuthor">关于博主</a></li>
            </ul>
          </div>

          <div>
            <h3 class="text-sm font-semibold text-slate-950 dark:text-white">分类</h3>

            <ul v-if="featuredCategories.length > 0" class="mt-4 space-y-3 text-sm text-slate-600 dark:text-slate-400">
              <li v-for="category in featuredCategories" :key="category.id">
                <a href="#" class="footer-link" @click.prevent="goToCategories(category)">
                  {{ category.categoryName }}
                </a>
              </li>
            </ul>
            <p v-else class="mt-4 text-sm text-slate-500 dark:text-slate-400">分类数据加载中。</p>
          </div>
        </section>
      </div>

      <div class="flex flex-col gap-4 pt-6 text-sm text-slate-500 dark:text-slate-400 sm:flex-row sm:items-center sm:justify-between">
        <p>© 2026 云坛社区 All Rights Reserved</p>
        <div class="flex flex-wrap items-center gap-2">
          <span>分类 {{ categories.length }}</span>
          <span class="text-slate-300 dark:text-slate-700">/</span>
          <span>运行</span>
          <span class="timer-seg text-slate-700 dark:text-slate-200">{{ rt.days }}</span><span class="timer-unit">天</span>
          <span class="timer-seg text-slate-700 dark:text-slate-200">{{ rt.hours }}</span><span class="timer-unit">时</span>
          <span class="timer-seg text-slate-700 dark:text-slate-200">{{ rt.minutes }}</span><span class="timer-unit">分</span>
          <span class="timer-seg text-slate-700 dark:text-slate-200">{{ rt.seconds }}</span><span class="timer-unit">秒</span>
        </div>
      </div>
    </div>
  </footer>
</template>

<script setup lang="ts" name="Footer">
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getCategories } from '@/api/category'

type CategoryItem = {
  id: number | string
  categoryName: string
}

const rt = ref({ days: '0', hours: '00', minutes: '00', seconds: '00' })
const categories = ref<CategoryItem[]>([])
const router = useRouter()
const featuredCategories = computed(() => categories.value.slice(0, 4))
let timer: number | undefined

const goToBookshelf = () => {
  router.push('/bookshelf')
}

const goToToolbox = () => {
  router.push('/toolbox')
}

const goToFriendLinks = () => {
  router.push('/friend-links')
}

const goToAboutSite = () => {
  router.push('/about/site')
}

const goToAboutAuthor = () => {
  router.push('/about/author')
}

const goMessageBoard = () => {
  router.push('/message-board')
}

const goToCategories = (category?: CategoryItem) => {
  router.push({
    path: '/categories',
    query: category
      ? {
          id: String(category.id),
          name: category.categoryName,
        }
      : undefined,
  })
}

const loadCategories = async () => {
  try {
    const data = await getCategories()
    categories.value = Array.isArray(data) ? data : []
  } catch (error) {
    console.error('获取分类失败:', error)
  }
}

const SITE_START = new Date('2026-03-29T00:00:00+08:00')

function updateRuntime() {
  const now = new Date()
  let diff = Math.max(0, Math.floor((now.getTime() - SITE_START.getTime()) / 1000))
  const pad = (n: number) => String(n).padStart(2, '0')
  const days = Math.floor(diff / 86400)
  diff %= 86400
  rt.value = {
    days: String(days),
    hours: pad(Math.floor(diff / 3600)),
    minutes: pad(Math.floor((diff % 3600) / 60)),
    seconds: pad(diff % 60),
  }
}

onMounted(() => {
  updateRuntime()
  timer = window.setInterval(updateRuntime, 1000)
  loadCategories()
})

onUnmounted(() => {
  if (timer) window.clearInterval(timer)
})
</script>

<style scoped>
.footer-link {
  display: inline-block;
  border: 0;
  background: transparent;
  padding: 0;
  color: inherit;
  text-decoration: none;
  transition: color 0.2s ease, opacity 0.2s ease;
}

.footer-link:hover {
  color: rgb(15 23 42);
}

.dark .footer-link:hover {
  color: rgb(255 255 255);
}

.timer-seg {
  display: inline-block;
  min-width: 2ch;
  text-align: right;
  font-variant-numeric: tabular-nums;
  font-feature-settings: 'tnum';
  font-weight: 600;
  font-family: ui-monospace, 'Cascadia Code', 'Source Code Pro', Menlo, Monaco, Consolas, monospace;
}

.timer-unit {
  font-size: 0.75rem;
  opacity: 0.62;
  margin-right: 2px;
}
</style>