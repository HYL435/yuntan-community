<template>
  <article 
    class="group relative w-full md:h-72 cursor-pointer rounded-2xl transition-all duration-300 select-none shadow-sm hover:-translate-y-0.5 hover:shadow-xl dark:shadow-none"
    @click="handleClick"
  >
    <!-- ================= 背景层 ================= -->
    <div class="absolute inset-0 z-0 overflow-hidden rounded-2xl transition-all duration-500
      bg-white dark:bg-[#121212]
      border border-slate-200 dark:border-[#2d3345]
      group-hover:border-cyan-300 dark:group-hover:border-[#ff2f70]"
    >
      <!-- 网格背景 -->
      <div class="absolute inset-0 opacity-[0.35] dark:opacity-20 bg-grid-pattern"></div>
      <!-- 悬停光晕 -->
      <div class="absolute inset-0 bg-linear-to-br from-transparent via-transparent to-cyan-500/0 group-hover:to-cyan-500/10 dark:to-rose-500/0 dark:group-hover:to-rose-500/10 transition-all duration-500"></div>
    </div>

    <!-- ================= 内容主体 ================= -->
    <div 
      class="relative z-10 flex flex-col md:flex-row rounded-2xl overflow-hidden transition-all md:h-full"
      :class="{ 'md:flex-row-reverse': imagePosition === 'left' }"
    >
      <!-- 1. 图片区域 -->
        <div class="w-full md:w-5/12 relative h-52 md:h-full shrink-0 overflow-hidden">
        <img 
          class="card-cover-image w-full h-full object-cover object-center" 
          :src="safeCoverUrl" 
          :alt="title" 
          loading="lazy"
        />
        <!-- 遮罩层 -->
        <div class="absolute inset-0 bg-linear-to-t from-black/15 via-transparent to-transparent dark:from-black/55 dark:to-black/20 transition-colors group-hover:from-black/5 dark:group-hover:from-black/40"></div>
        
        <!-- 左上角标签组 (Top-Left Tags) -->
        <div class="absolute top-3 left-3 z-20 flex flex-wrap gap-1.5">
          <template v-if="tags && tags.length">
            <span 
              v-for="(t, idx) in tags" :key="`tag-${idx}`" 
              class="px-2.5 py-1 text-[10px] font-semibold rounded-lg shadow-sm backdrop-blur-md 
                     bg-white/90 text-gray-800 border border-gray-100
                     dark:bg-[#1E1E1E]/90 dark:text-[#e6e6ff] dark:border-[#333355]"
            >
              {{ t }}
            </span>
          </template>
          <span v-else 
            class="px-2.5 py-1 text-[10px] font-semibold rounded-lg shadow-sm backdrop-blur-md 
                   bg-white/90 text-gray-800 border border-gray-100
                   dark:bg-[#1E1E1E]/90 dark:text-[#e6e6ff] dark:border-[#333355]"
          >
            {{ primaryTag }}
          </span>
        </div>
      </div>

      <!-- 2. 文字区域 -->
      <div class="flex-1 flex flex-col justify-between p-5 md:p-7 bg-transparent md:h-full min-w-0">
        <div>
          <!-- 头部 Meta (日期 & 热度 & 收藏) -->
          <div class="flex flex-wrap items-center gap-2.5 mb-3 text-[11px] font-medium">
            <span class="px-2 py-0.5 rounded bg-gray-100 text-gray-500 dark:bg-[#2d2d3d] dark:text-[#a0a0b0]">
              {{ formatDate(publishTime) }}
            </span>
            <span class="flex items-center gap-1 text-cyan-600 dark:text-[#ff0055]">
              <svg class="w-3.5 h-3.5" fill="currentColor" viewBox="0 0 24 24"><path d="M13.5 2.1c0 0-1.9 2.5-1.9 4.9 0 1.4.6 2.3 1.4 3.4.6.8 1.4 1.8 1.4 3.6 0 2.7-2.2 4.9-4.9 4.9S4.5 17.1 4.5 14.4c0-2.2 1.2-3.7 2.2-5.1.5-.6 1-1.1 1-2.2 0-1.6-1.4-3-1.4-3s3.7.4 6.2 1.9z"/></svg>
              {{ formatCount(heat) }}
            </span>
            <span class="flex items-center gap-1 text-gray-400 dark:text-gray-500">
              <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M5 3v16l7-5 7 5V3z"/></svg>
              {{ formatCount(collectCount || bookmarks || 0) }}
            </span>
          </div>

          <!-- 标题 -->
          <h3 class="text-lg md:text-xl font-bold leading-tight line-clamp-2 mb-2 transition-colors duration-300
            text-gray-900 group-hover:text-cyan-700
            dark:text-white dark:group-hover:text-[#ff0055]">
            {{ title }}
          </h3>

          <!-- 摘要 -->
          <p class="text-xs md:text-sm leading-relaxed line-clamp-2 md:line-clamp-3 text-gray-500 dark:text-gray-400">
            {{ displayExcerpt }}
          </p>
        </div>

        <!-- 底部数据栏 -->
        <div class="pt-4 mt-auto border-t border-gray-100 dark:border-gray-800/60">
          <div class="flex items-center justify-between gap-3">
            <div class="flex gap-2.5">
              <!-- 评论 -->
              <div class="flex items-center gap-1.5 px-2.5 py-1.5 text-xs rounded-xl bg-gray-50 dark:bg-[#1E1E1E] text-gray-600 dark:text-gray-400 border border-gray-100 dark:border-[#333355]">
                 <svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M8 10h8M8 14h5"/><path d="M21 12a9 9 0 0 1-9 9 8.96 8.96 0 0 1-4.7-1.32L3 21l1.38-4.14A9 9 0 1 1 21 12Z"/></svg>
                 {{ formatCount(comments) }}
               </div>
               <!-- 点赞 -->
               <div class="flex items-center gap-1.5 px-2.5 py-1.5 text-xs rounded-xl bg-cyan-50/70 dark:bg-[#1E1E1E] text-cyan-600 dark:text-[#ff0055] border border-cyan-100 dark:border-[#333355]">
                 <svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="currentColor"><path d="M12 21s-6.716-4.354-9.193-8.222c-1.553-2.426-.933-5.64 1.375-7.143 2.08-1.355 4.94-1.002 6.818.804L12 7.466l1-1.027c1.878-1.806 4.738-2.159 6.818-.804 2.308 1.503 2.928 4.717 1.375 7.143C18.716 16.646 12 21 12 21Z"/></svg>
                 {{ formatCount(likes) }}
               </div>
             </div>
             
             <button
               type="button"
               class="h-10 px-3.5 flex items-center justify-center gap-1.5 rounded-xl transition-all duration-300
               bg-gray-900 text-white hover:bg-cyan-600
               dark:bg-[#2d2d3d] dark:text-white dark:hover:bg-[#ff0055] dark:hover:shadow-[0_0_15px_rgba(255,0,85,0.35)]"
               @click.stop="handleClick"
             >
               <span class="text-xs font-medium">阅读全文</span>
               <svg class="w-4 h-4 transform group-hover:translate-x-0.5 transition-transform" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24">
                 <path stroke-linecap="round" stroke-linejoin="round" d="M13.5 4.5L21 12m0 0l-7.5 7.5M21 12H3"></path>
               </svg>
             </button>
          </div>
        </div>
      </div>
    </div>
  </article>
</template>

<script setup lang="ts">
import { computed } from 'vue';

defineOptions({ name: 'ArticleTwoCard' });

interface Props {
  articleId?: string | number;
  title?: string;
  publishTime?: string;
  heat?: number;
  comments?: number;
  likes?: number;
  bookmarks?: number;
  collectCount?: number;
  tags?: string[];
  excerpt?: string;
  primaryTag?: string;
  coverUrl?: string;
  imagePosition?: 'left' | 'right';
}

const props = withDefaults(defineProps<Props>(), {
  articleId: 'demo-1',
  title: '探索Vue 3组合式API的艺术：构建现代Web应用',
  publishTime: '2026-02-04',
  heat: 1205,
  comments: 24,
  likes: 186,
  tags: () => [],
  excerpt: '在现代前端开发中，Vue 3 的 Composition API 带来了革命性的变化。本文将深入探讨如何利用这些新特性来组织代码...',
  primaryTag: '前端技术',
  coverUrl: 'https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?q=80&w=1200&auto=format&fit=crop',
  imagePosition: 'right'
});

const emit = defineEmits(['click']);

const defaultCover = 'https://images.unsplash.com/photo-1557683316-973673baf926?q=80&w=1600&auto=format&fit=crop';

const safeCoverUrl = computed(() => props.coverUrl || defaultCover);

const displayExcerpt = computed(() => {
  const text = (props.excerpt || '').trim();
  return text || '这篇文章暂无摘要，点击卡片阅读全文。';
});

const handleClick = () => {
  emit('click', props.articleId);
};

const formatCount = (value?: number) => {
  const num = Number(value ?? 0);
  if (!Number.isFinite(num) || num <= 0) return 0;
  if (num >= 100000) return `${(num / 10000).toFixed(1)}w`;
  if (num >= 1000) return `${(num / 1000).toFixed(1)}k`;
  return num;
};

const formatDate = (dateStr?: any) => {
  if (dateStr === undefined || dateStr === null || dateStr === '') return '';

  if (typeof dateStr === 'string') {
    return dateStr.includes('T') ? dateStr.split('T')[0] : (dateStr.length >= 10 ? dateStr.slice(0, 10) : dateStr);
  }

  // Handle backend LocalDateTime serialized as array: [yyyy, MM, dd, HH, mm, ss]
  if (Array.isArray(dateStr) && dateStr.length >= 3) {
    const [y, m, d] = dateStr;
    const mm = String(m).padStart(2, '0');
    const dd = String(d).padStart(2, '0');
    return `${y}-${mm}-${dd}`;
  }

  if (typeof dateStr === 'object') {
    const y = dateStr.year;
    const m = dateStr.monthValue ?? dateStr.month;
    const d = dateStr.dayOfMonth ?? dateStr.day;
    if (y && m && d) {
      const mm = String(m).padStart(2, '0');
      const dd = String(d).padStart(2, '0');
      return `${y}-${mm}-${dd}`;
    }
    return '';
  }

  if (typeof dateStr === 'number') {
    const date = new Date(dateStr);
    if (!Number.isNaN(date.getTime())) return date.toISOString().slice(0, 10);
  }

  return String(dateStr);
};
</script>

<style scoped>
/* 纯 CSS 网格背景 */
.bg-grid-pattern {
  background-size: 30px 30px;
  background-image: 
    linear-gradient(to right, rgba(0, 0, 0, 0.05) 1px, transparent 1px),
    linear-gradient(to bottom, rgba(0, 0, 0, 0.05) 1px, transparent 1px);
}

:global(.dark) .bg-grid-pattern {
  background-image: 
    linear-gradient(to right, rgba(255, 0, 85, 0.1) 1px, transparent 1px),
    linear-gradient(to bottom, rgba(255, 0, 85, 0.1) 1px, transparent 1px);
}

.card-cover-image {
  transform: scale(1.01);
  transform-origin: center center;
  transition: transform 560ms cubic-bezier(0.22, 0.61, 0.36, 1);
  will-change: transform;
}

.group:hover .card-cover-image {
  transform: scale(1.08);
}

/* 多行文本省略 */
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.line-clamp-3 {
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>