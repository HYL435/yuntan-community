<template>
  <article class="category-card group overflow-hidden rounded-2xl border border-slate-200 bg-white transition dark:border-slate-800 dark:bg-slate-900">
    <button class="category-card__btn w-full text-left" type="button" @click="$emit('open', article.id)">
      <div class="grid gap-0 md:grid-cols-[300px_1fr]">
        <div class="h-56 overflow-hidden bg-slate-100 md:h-full dark:bg-slate-800">
          <img
            v-if="article.coverImg"
            :src="article.coverImg"
            :alt="article.title"
            class="category-card__image h-full w-full object-cover"
          />
          <div v-else class="flex h-full items-center justify-center text-sm text-slate-400 dark:text-slate-500">
            暂无封面
          </div>
        </div>

        <div class="p-6 md:p-7">
          <div class="flex flex-wrap items-center justify-between gap-3">
            <span class="rounded-full bg-slate-100 px-3 py-1 text-xs font-medium text-slate-600 dark:bg-slate-800 dark:text-slate-300">
              {{ article.category || '未分类' }}
            </span>
            <span class="time-pill">{{ formatDate(article.publishTime) }}</span>
          </div>

          <h3 class="mt-4 line-clamp-2 text-2xl font-semibold text-slate-900 dark:text-white">{{ article.title }}</h3>
          <p class="mt-3 line-clamp-3 text-sm leading-7 text-slate-600 dark:text-slate-400">
            {{ article.summary || '暂无摘要内容' }}
          </p>

          <div class="mt-5 flex flex-wrap gap-2" v-if="article.tags.length">
            <span
              v-for="tag in article.tags.slice(0, 4)"
              :key="tag"
              class="rounded-full border border-slate-200 px-2.5 py-1 text-xs text-slate-600 dark:border-slate-700 dark:text-slate-300"
            >
              # {{ tag }}
            </span>
          </div>

          <div class="mt-6 flex flex-wrap items-center gap-x-4 gap-y-2 text-xs text-slate-500 dark:text-slate-400">
            <span>阅读 {{ article.viewCount }}</span>
            <span>点赞 {{ article.likeCount }}</span>
            <span>评论 {{ article.commentCount }}</span>
            <span>收藏 {{ article.collectCount }}</span>
          </div>
        </div>
      </div>
    </button>
  </article>
</template>

<script setup lang="ts">
export interface CategoryArticleCardModel {
  id: string | number
  title: string
  summary: string
  coverImg: string
  category: string
  tags: string[]
  publishTime: string | number[]
  likeCount: number
  commentCount: number
  collectCount: number
  viewCount: number
}

defineEmits<{
  (e: 'open', id: string | number): void
}>()

defineProps<{
  article: CategoryArticleCardModel
}>()

const formatDate = (value: string | number[] | any) => {
  if (!value) return '--'

  // 兼容后端返回真实数组: [2026, 3, 28, 23, 48, 49]
  if (Array.isArray(value)) {
    const [y, m, d, hh = 0, mm = 0] = value as number[]
    return `${y}-${String(m).padStart(2, '0')}-${String(d).padStart(2, '0')} ${String(hh).padStart(2, '0')}:${String(mm).padStart(2, '0')}`
  }

  // 兼容后端时间数组字符串: "[2026, 3, 24, 23, 48, 44]"
  const arrLike = /^\s*\[\s*\d+\s*,/.test(value)
  if (arrLike) {
    try {
      const parts = value
        .replace(/[\[\]\s]/g, '')
        .split(',')
        .map((n: string) => Number(n))
      if (parts.length >= 3 && parts.every((n: number) => Number.isFinite(n))) {
        const [y, m, d, hh = 0, mm = 0] = parts
        const month = String(m).padStart(2, '0')
        const day = String(d).padStart(2, '0')
        const hour = String(hh).padStart(2, '0')
        const minute = String(mm).padStart(2, '0')
        return `${y}-${month}-${day} ${hour}:${minute}`
      }
    } catch {
      // ignore parse error and fallback
    }
  }

  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  const hh = String(date.getHours()).padStart(2, '0')
  const mm = String(date.getMinutes()).padStart(2, '0')
  return `${y}-${m}-${d} ${hh}:${mm}`
}
</script>

<style scoped>
.category-card {
  position: relative;
  isolation: isolate;
}

.category-card__btn {
  position: relative;
  z-index: 1;
}

.category-card__image {
  transition: transform 0.55s ease;
}

.category-card::after {
  content: '';
  position: absolute;
  inset: -1px;
  border-radius: 1rem;
  padding: 1px;
  background: linear-gradient(120deg, rgba(56, 189, 248, 0.05), rgba(56, 189, 248, 0.8), rgba(99, 102, 241, 0.2), rgba(244, 63, 94, 0.55), rgba(56, 189, 248, 0.05));
  background-size: 220% 220%;
  opacity: 0;
  pointer-events: none;
  transition: opacity 0.25s ease;
  -webkit-mask: linear-gradient(#fff 0 0) content-box, linear-gradient(#fff 0 0);
  -webkit-mask-composite: xor;
  mask: linear-gradient(#fff 0 0) content-box, linear-gradient(#fff 0 0);
  mask-composite: exclude;
}

.category-card:hover {
  box-shadow: 0 14px 34px rgba(15, 23, 42, 0.12);
}

.category-card:hover::after {
  opacity: 1;
  animation: border-flow 2.6s linear infinite;
}

.category-card:hover .category-card__image {
  transform: scale(1.1);
}

.time-pill {
  display: inline-flex;
  align-items: center;
  padding: 0.3rem 0.7rem;
  border-radius: 999px;
  border: 1px solid rgba(148, 163, 184, 0.35);
  background: rgba(248, 250, 252, 0.85);
  color: #475569;
  font-size: 0.75rem;
  font-family: ui-monospace, 'Cascadia Code', 'Source Code Pro', Menlo, Monaco, Consolas, monospace;
}

:global(html.dark) .category-card:hover {
  box-shadow: 0 16px 36px rgba(2, 6, 23, 0.48);
}

:global(html.dark) .time-pill,
:global(body.dark) .time-pill,
:global(html.dark-mode) .time-pill,
:global(body.dark-mode) .time-pill {
  border-color: rgba(71, 85, 105, 0.7);
  background: rgba(15, 23, 42, 0.85);
  color: #cbd5e1;
}

@keyframes border-flow {
  0% {
    background-position: 0% 50%;
  }
  100% {
    background-position: 100% 50%;
  }
}
</style>
