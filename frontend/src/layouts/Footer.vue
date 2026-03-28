<template>
  <footer class="h-auto bg-gray-100 dark:bg-slate-900 text-gray-900 dark:text-[rgba(255,255,255,0.87)] transition-colors duration-300 relative z-50">
    <div class="max-w-6xl mx-auto px-4 py-8">
      <!-- icons row removed -->

      <!-- three columns -->
      <div class="grid grid-cols-1 md:grid-cols-3 gap-8 text-center md:text-left">
        <div>
          <h4 class="text-lg font-semibold mb-3">直达</h4>
          <ul class="space-y-2">
            <li><a href="#" class="hover:underline">藏宝阁</a></li>
            <li><a href="#" class="hover:underline">友链</a></li>
            <li><a href="#" class="hover:underline">留言厅</a></li>
            <li><a href="#" class="hover:underline">个人相册</a></li>
            <li><a href="#" class="hover:underline">幻音坊</a></li>
          </ul>
        </div>

        <div>
          <h4 class="text-lg font-semibold mb-3">分类</h4>
          <ul class="space-y-2">
            <li><a href="#" class="hover:underline">学习笔记</a></li>
            <li><a href="#" class="hover:underline">我的项目</a></li>
            <li><a href="#" class="hover:underline">游戏</a></li>
            <li><a href="#" class="hover:underline">动漫</a></li>
            <li><a href="#" class="hover:underline">随想</a></li>
            <li><a href="#" class="hover:underline">查看全部</a></li>
          </ul>
        </div>

        <div class="md:text-right">
          <h4 class="text-lg font-semibold mb-3">友链</h4>
          <ul class="space-y-2">
            <li><a href="#" class="hover:underline">POETIZE</a></li>
            <li><a href="#" class="hover:underline">轻笑Chuckle</a></li>
            <li><a href="#" class="hover:underline">Ezuxoay</a></li>
            <li><a href="#" class="hover:underline">380AM-0204</a></li>
            <li><a href="#" class="hover:underline">查看更多</a></li>
          </ul>
        </div>
      </div>

      <!-- bottom line -->
      <div class="mt-8 border-t border-gray-200 dark:border-slate-700/50 pt-4 flex flex-col md:flex-row items-center justify-between text-sm">
        <div class="font-semibold">Copyright © 2026 By Yuntan</div>
        <div class="mt-3 md:mt-0 flex items-center gap-1 flex-wrap justify-center md:justify-end">
          <span class="text-gray-500 dark:text-gray-400">本站已运行：</span>
          <span class="timer-seg">{{ rt.days }}</span><span class="timer-unit">天</span>
          <span class="timer-seg">{{ rt.hours }}</span><span class="timer-unit">时</span>
          <span class="timer-seg">{{ rt.minutes }}</span><span class="timer-unit">分</span>
          <span class="timer-seg">{{ rt.seconds }}</span><span class="timer-unit">秒</span>
        </div>
      </div>
    </div>
  </footer>
</template>

<script setup lang="ts" name="Footer">
import { ref, onMounted, onUnmounted } from 'vue'

const rt = ref({ days: '0', hours: '00', minutes: '00', seconds: '00' })
let timer: number | undefined

// 建站日期：以今天（2026-03-29）为第一天，持续累计
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
})

onUnmounted(() => {
  if (timer) window.clearInterval(timer)
})
</script>

<style scoped>
/* 保持主色不变，仅微调间距与文字 */

/* 时间数字段：固定宽度 + tabular 数字，彻底防止跳动 */
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
  opacity: 0.6;
  margin-right: 2px;
}
</style>