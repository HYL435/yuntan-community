<template>
  <div class="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm dark:border-slate-800 dark:bg-slate-900">
    <p class="text-[11px] uppercase tracking-[0.2em] text-slate-400 dark:text-slate-500">{{ label }}</p>
    <p class="mt-2 text-3xl font-semibold text-slate-900 dark:text-white">{{ value }}</p>
    <p v-if="desc" class="mt-2 text-xs text-slate-500 dark:text-slate-400">{{ desc }}</p>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  label: string
  value: string | number
  desc?: string
}>()
</script>
