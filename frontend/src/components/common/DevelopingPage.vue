<template>
  <div class="min-h-screen bg-slate-50 text-slate-800 dark:bg-slate-950 dark:text-slate-100">
    <div class="mx-auto max-w-5xl px-6 py-16 lg:px-8">
      <div class="rounded-3xl border border-slate-200 bg-white p-8 shadow-sm dark:border-slate-800 dark:bg-slate-900/70">
        <div class="inline-flex items-center rounded-full bg-amber-100 px-3 py-1 text-xs font-medium tracking-wide text-amber-700 dark:bg-amber-500/15 dark:text-amber-300">
          开发中
        </div>

        <h1 class="mt-5 text-3xl font-semibold text-slate-900 dark:text-white sm:text-4xl">{{ title }}</h1>
        <p class="mt-4 max-w-2xl text-sm leading-7 text-slate-600 dark:text-slate-400">{{ description }}</p>

        <p v-if="hint" class="mt-4 text-sm text-sky-700 dark:text-sky-300">{{ hint }}</p>

        <div class="mt-10 grid gap-4 sm:grid-cols-3">
          <div v-for="item in todoList" :key="item" class="rounded-2xl border border-slate-200 bg-slate-50 px-4 py-4 text-sm text-slate-600 dark:border-slate-800 dark:bg-slate-900 dark:text-slate-300">
            {{ item }}
          </div>
        </div>

        <p class="mt-8 text-xs text-slate-500 dark:text-slate-500">页面已创建，后续可直接在当前结构上补充真实业务内容。</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
interface Props {
  title: string
  description: string
  hint?: string
  todoList?: string[]
}

withDefaults(defineProps<Props>(), {
  hint: '',
  todoList: () => ['功能逻辑开发中', '接口联调开发中', '交互优化开发中'],
})
</script>
