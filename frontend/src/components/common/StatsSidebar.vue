<template>
  <div class="stats-sidebar space-y-3 sm:space-y-4">
    <!-- 标题 -->
    <div class="stats-sidebar-head flex items-center space-x-2 px-1">
      <span class="w-1 h-4 bg-indigo-500 rounded-full"></span>
      <h3 class="font-bold text-gray-800 dark:text-gray-200">数据概览</h3>
    </div>

    <div class="stats-sidebar-grid grid grid-cols-2 gap-3 sm:gap-4">
      <!-- 文章 (占据较大空间) -->
      <div class="stats-sidebar-hero col-span-2 relative overflow-hidden rounded-2xl bg-linear-to-br from-indigo-500 to-purple-600 p-4 sm:p-5 text-white shadow-lg shadow-indigo-500/20">
        <div class="absolute right-0 top-0 opacity-10 transform translate-x-2 -translate-y-2">
          <svg class="w-24 h-24" fill="currentColor" viewBox="0 0 24 24"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path></svg>
        </div>
        <div class="relative z-10">
          <p class="text-indigo-100 text-xs font-medium mb-1">文章沉淀</p>
          <div class="flex items-baseline space-x-1">
            <span class="text-3xl sm:text-[2rem] font-black tracking-tight">{{ articleCountDisplay }}</span>
            <span class="text-xs opacity-80">篇</span>
          </div>
        </div>
      </div>

      <!-- 今日访问 -->
      <div class="stats-sidebar-card rounded-2xl bg-white dark:bg-[#252525] p-3 sm:p-4 border border-gray-100 dark:border-slate-700 shadow-sm flex flex-col justify-between">
        <div class="w-8 h-8 rounded-lg bg-orange-100 dark:bg-orange-900/30 text-orange-500 flex items-center justify-center mb-2">
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 7h8m0 0v8m0-8l-8 8-4-4-6 6"></path></svg>
        </div>
        <div>
           <span class="text-xl font-bold text-gray-800 dark:text-gray-100 block">{{ todayVisitsDisplay }}</span>
           <span class="text-[10px] text-gray-400">今日热度</span>
        </div>
      </div>

      <!-- 分类 -->
      <div class="stats-sidebar-card rounded-2xl bg-white dark:bg-[#252525] p-3 sm:p-4 border border-gray-100 dark:border-slate-700 shadow-sm flex flex-col justify-between">
        <div class="w-8 h-8 rounded-lg bg-teal-100 dark:bg-teal-900/30 text-teal-500 flex items-center justify-center mb-2">
           <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 7h.01M7 3h5c.512 0 1.024.195 1.414.586l7 7a2 2 0 010 2.828l-7 7a2 2 0 01-2.828 0l-7-7A1.994 1.994 0 013 12V7a4 4 0 014-4z"></path></svg>
        </div>
        <div>
           <span class="text-xl font-bold text-gray-800 dark:text-gray-100 block">{{ categoryCountDisplay }}</span>
           <span class="text-[10px] text-gray-400">内容分类</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { getAdminStats, getArticleCount, getTodayHot } from '@/api/stats'
import { getCategories } from '@/api/category'

const articleCount = ref<number | null>(null)
const categoryCount = ref<number | null>(null)
const todayVisits = ref<number | null>(null)

const articleCountDisplay = computed(() => (articleCount.value === null ? '—' : articleCount.value))
const categoryCountDisplay = computed(() => (categoryCount.value === null ? '—' : categoryCount.value))
const todayVisitsDisplay = computed(() => (todayVisits.value === null ? '—' : todayVisits.value))

async function fetchArticleCount() {
  try {
    articleCount.value = await getArticleCount()
  } catch (e) {
    articleCount.value = null
  }
}

async function fetchCategoryCount() {
  try {
    const res = await getCategories()
    if (Array.isArray(res)) categoryCount.value = res.length
    else if (res && Array.isArray((res as any).data)) categoryCount.value = (res as any).data.length
    else categoryCount.value = null
  } catch (e) {
    categoryCount.value = null
  }
}

async function fetchTodayVisits() {
  try {
    const hot = await getTodayHot()
    if (typeof hot === 'number') {
      todayVisits.value = hot
      return
    }

    const res = await getAdminStats()
    if (res) {
      if (Array.isArray(res.visits) && res.visits.length) {
        todayVisits.value = res.visits[res.visits.length - 1]
        return
      }
      if (Array.isArray(res.series)) {
        const vSeries = res.series.find(s => /visit|visits|pv|访问/i.test(String(s.name)))
        if (vSeries && Array.isArray(vSeries.data) && vSeries.data.length) {
          todayVisits.value = vSeries.data[vSeries.data.length - 1]
          return
        }
      }
    }
    todayVisits.value = null
  } catch (e) {
    todayVisits.value = null
  }
}

onMounted(() => {
  fetchArticleCount()
  fetchCategoryCount()
  fetchTodayVisits()
})
</script>

<style scoped>
@media (max-width: 1024px) {
  .stats-sidebar {
    padding: 1rem;
    border-radius: 1.75rem;
    background: rgba(255, 255, 255, 0.74);
    border: 1px solid rgba(226, 232, 240, 0.85);
    box-shadow: 0 24px 48px rgba(15, 23, 42, 0.07);
  }

  :global(html.dark) .stats-sidebar,
  :global(.dark) .stats-sidebar,
  :global(html.dark-mode) .stats-sidebar,
  :global(.dark-mode) .stats-sidebar {
    background: rgba(30, 41, 59, 0.38);
    border-color: rgba(71, 85, 105, 0.45);
    box-shadow: 0 24px 48px rgba(0, 0, 0, 0.2);
  }

  .stats-sidebar-head {
    margin-bottom: 0.25rem;
  }

  .stats-sidebar-card {
    min-height: 124px;
  }
}

@media (min-width: 768px) and (max-width: 1024px) {
  .stats-sidebar {
    padding: 1.15rem;
    border-radius: 1.7rem;
  }

  .stats-sidebar-head {
    padding: 0.1rem 0.15rem 0.25rem;
  }

  .stats-sidebar-grid {
    gap: 0.85rem;
  }

  .stats-sidebar-hero {
    border-radius: 1.55rem;
    min-height: 124px;
    background: linear-gradient(135deg, #1d4ed8, #0f766e 55%, #06b6d4);
  }

  .stats-sidebar-card {
    border-radius: 1.35rem;
    background: rgba(255, 255, 255, 0.88);
    box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.8);
  }

  :global(html.dark) .stats-sidebar-card,
  :global(.dark) .stats-sidebar-card,
  :global(html.dark-mode) .stats-sidebar-card,
  :global(.dark-mode) .stats-sidebar-card {
    background: rgba(30, 41, 59, 0.52);
    box-shadow: inset 0 1px 0 rgba(148, 163, 184, 0.12);
  }
}

@media (max-width: 767px) {
  .stats-sidebar {
    padding: 0.9rem;
    border-radius: 1.5rem;
    background: rgba(255, 255, 255, 0.68);
    border-color: rgba(226, 232, 240, 0.82);
  }

  .stats-sidebar-grid {
    gap: 0.75rem;
  }

  .stats-sidebar-hero {
    border-radius: 1.35rem;
    background: linear-gradient(135deg, #4338ca, #7c3aed 55%, #ec4899);
    box-shadow: 0 18px 36px rgba(124, 58, 237, 0.22);
  }

  .stats-sidebar-card {
    min-height: 112px;
    border-radius: 1.2rem;
    background: rgba(255, 255, 255, 0.86);
    border-color: rgba(226, 232, 240, 0.82);
    box-shadow: 0 14px 28px rgba(15, 23, 42, 0.08);
  }

  :global(html.dark) .stats-sidebar,
  :global(.dark) .stats-sidebar,
  :global(html.dark-mode) .stats-sidebar,
  :global(.dark-mode) .stats-sidebar {
    background: rgba(30, 41, 59, 0.34);
    border-color: rgba(148, 163, 184, 0.18);
  }

  :global(html.dark) .stats-sidebar-hero,
  :global(.dark) .stats-sidebar-hero,
  :global(html.dark-mode) .stats-sidebar-hero,
  :global(.dark-mode) .stats-sidebar-hero {
    box-shadow: 0 18px 36px rgba(244, 63, 94, 0.18);
  }

  :global(html.dark) .stats-sidebar-card,
  :global(.dark) .stats-sidebar-card,
  :global(html.dark-mode) .stats-sidebar-card,
  :global(.dark-mode) .stats-sidebar-card {
    background: rgba(30, 41, 59, 0.46);
    border-color: rgba(148, 163, 184, 0.14);
    box-shadow: 0 16px 30px rgba(0, 0, 0, 0.18);
  }
}
</style>
