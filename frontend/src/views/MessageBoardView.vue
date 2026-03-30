<script setup lang="ts" name="MessageBoardView">
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useUserStore } from '@/stores/user'
import Header from '@/layouts/Header.vue'
import http from '@/api/http'

// ─── Types ────────────────────────────────────────────────
interface DanmakuItem {
  id: number
  text: string
  color: string
  top: number
  duration: number
  fontSize: number
}

interface StoredMsg {
  id?: number | string
  text: string
  color: string
  author: string
  ts: number
}

// ─── Store ────────────────────────────────────────────────
const userStore = useUserStore()
const authorName = computed(() =>
  userStore.user?.nickname || userStore.user?.username || '游客'
)
const isLoggedIn = computed(() => !!userStore.token)

// ─── State ────────────────────────────────────────────────
const inputText = ref('')
const selectedColor = ref('#ffffff')
const activeDanmaku = ref<DanmakuItem[]>([])
const totalSent = ref(0)
let nextId = 1

// ─── Theme ────────────────────────────────────────────────
const isDark = ref(
  localStorage.getItem('dark-mode') === 'true' ||
  (!localStorage.getItem('dark-mode') && window.matchMedia('(prefers-color-scheme: dark)').matches)
)
let themeObserver: MutationObserver | null = null
const boardBg = computed(() => isDark.value ? '#03040a' : '#eef2ff')

// ─── Color presets ────────────────────────────────────────
const colorPresets = [
  '#ffffff',
  '#93c5fd',
  '#6ee7b7',
  '#f9a8d4',
  '#fbbf24',
  '#fb923c',
  '#c4b5fd',
  '#f87171',
]

// ─── Track management ─────────────────────────────────────
const NUM_TRACKS = 12
const HEADER_H = 110
const BOTTOM_H = 140

const trackEndTimes: number[] = new Array(NUM_TRACKS).fill(0)

function getFreeTrack(duration: number): number {
  const now = Date.now()
  const freeIdx = trackEndTimes.findIndex(t => t <= now)
  const idx = freeIdx >= 0 ? freeIdx : (nextId % NUM_TRACKS)
  trackEndTimes[idx] = now + duration * 1000
  return idx
}

function buildItem(text: string, color: string, author: string): DanmakuItem {
  const duration = 11 + Math.random() * 8   // 11–19 s
  const track = getFreeTrack(duration)
  const usableH = window.innerHeight - HEADER_H - BOTTOM_H
  const laneH = usableH / NUM_TRACKS
  const top = HEADER_H + track * laneH + laneH * 0.1
  const displayText = author && author !== '游客' ? `[${author}] ${text}` : text
  return {
    id: nextId++,
    text: displayText,
    color,
    top,
    duration,
    fontSize: Math.random() > 0.8 ? 22 : 16,
  }
}

function removeDanmaku(id: number) {
  const i = activeDanmaku.value.findIndex(d => d.id === id)
  if (i !== -1) activeDanmaku.value.splice(i, 1)
}

function launchItem(item: DanmakuItem) {
  activeDanmaku.value.push(item)
  // 安全兜底：animationend 未触发时（如页面卸载）最终清理
  setTimeout(() => removeDanmaku(item.id), (item.duration + 60) * 1000)
}

// ─── LocalStorage ─────────────────────────────────────────
const STORAGE_KEY = 'yuntan-danmaku-v1'
const MAX_STORED = 100
const DANMAKU_API = '/front/danmaku'
const seenDanmakuKeys = new Set<string>()
const loopPool = ref<StoredMsg[]>([])
const loopCursor = ref(0)
const seedPool = ref<StoredMsg[]>([])
const seedCursor = ref(0)
let emitTick = 0
let keepAliveTimer: ReturnType<typeof setInterval> | null = null

function updateLoopPool(msgs: StoredMsg[]) {
  if (!msgs.length) return
  loopPool.value = msgs.slice(-60)
  if (loopCursor.value >= loopPool.value.length) loopCursor.value = 0
}

function emitOneFromPool() {
  const main = loopPool.value
  const seeds = seedPool.value
  if (!main.length && !seeds.length) return

  // Emit one seed message every 2 ticks so test messages are always visible.
  const preferSeed = emitTick % 2 === 0
  emitTick += 1

  let msg: StoredMsg
  if (preferSeed && seeds.length) {
    const idx = seedCursor.value % seeds.length
    seedCursor.value = (seedCursor.value + 1) % seeds.length
    msg = seeds[idx]
  } else if (main.length) {
    const idx = loopCursor.value % main.length
    loopCursor.value = (loopCursor.value + 1) % main.length
    msg = main[idx]
  } else {
    const idx = seedCursor.value % seeds.length
    seedCursor.value = (seedCursor.value + 1) % seeds.length
    msg = seeds[idx]
  }

  launchItem(buildItem(msg.text, msg.color, msg.author))
}

function startKeepAliveEmitter() {
  if (keepAliveTimer) clearInterval(keepAliveTimer)

  // Bootstrap burst to avoid initial blank screen.
  for (let i = 0; i < 3; i++) {
    setTimeout(() => emitOneFromPool(), i * 360)
  }

  keepAliveTimer = setInterval(() => {
    // Always keep emitting. Only skip when screen is already very dense.
    if (activeDanmaku.value.length > 18) return
    emitOneFromPool()
  }, 1400)
}

function parseDanmakuListResponse(payload: any): StoredMsg[] {
  const data = payload?.data ?? payload
  const list = Array.isArray(data) ? data : []
  return list
    .map((item: any, idx: number) => ({
      id: item?.id,
      text: String(item?.content ?? item?.text ?? '').trim(),
      color: String(item?.color ?? '#ffffff'),
      author: String(item?.author ?? '游客'),
      ts: Date.now() - (list.length - idx) * 100,
    }))
    .filter(msg => !!msg.text && !msg.text.includes('[系统测试]') && msg.author !== '系统测试')
}

async function loadDanmakuFromBackend(onlyNew: boolean): Promise<boolean> {
  try {
    const res = await http.get(DANMAKU_API)
    const msgs = parseDanmakuListResponse(res?.data || res)
    totalSent.value = msgs.length
    if (!msgs.length) return false
    updateLoopPool(msgs)

    const recent = msgs.slice(-20)
    recent.forEach((msg, i) => {
      const key = String(msg.id ?? `${msg.author}-${msg.color}-${msg.text}`)
      if (onlyNew && seenDanmakuKeys.has(key)) return
      seenDanmakuKeys.add(key)
      setTimeout(
        () => launchItem(buildItem(msg.text, msg.color, msg.author)),
        i * 500 + Math.random() * 200
      )
    })
    return true
  } catch (err) {
    console.warn('danmaku backend unavailable, fallback to local storage')
    return false
  }
}

function saveMsg(text: string, color: string, author: string) {
  try {
    const msgs: StoredMsg[] = JSON.parse(localStorage.getItem(STORAGE_KEY) || '[]')
    msgs.push({ text, color, author, ts: Date.now() })
    if (msgs.length > MAX_STORED) msgs.splice(0, msgs.length - MAX_STORED)
    localStorage.setItem(STORAGE_KEY, JSON.stringify(msgs))
    totalSent.value = msgs.length
    updateLoopPool(msgs)
  } catch { /* noop */ }
}

function replayMsgs() {
  try {
    const msgs: StoredMsg[] = JSON.parse(localStorage.getItem(STORAGE_KEY) || '[]')
    totalSent.value = msgs.length
    updateLoopPool(msgs)
    const recent = msgs.slice(-20)
    recent.forEach((msg, i) => {
      setTimeout(
        () => launchItem(buildItem(msg.text, msg.color, msg.author)),
        i * 600 + Math.random() * 300
      )
    })
  } catch { /* noop */ }
}

// ─── Send ─────────────────────────────────────────────────
async function sendDanmaku() {
  const text = inputText.value.trim()
  if (!text) return

  const optimisticMsg: StoredMsg = {
    text,
    color: selectedColor.value,
    author: authorName.value,
    ts: Date.now(),
  }
  updateLoopPool([...loopPool.value, optimisticMsg])
  launchItem(buildItem(text, selectedColor.value, authorName.value))
  inputText.value = ''

  try {
    await http.post(DANMAKU_API, {
      content: text,
      color: selectedColor.value,
    })
    totalSent.value += 1
  } catch (err) {
    // Keep offline behavior for local preview when backend is unavailable.
    saveMsg(text, selectedColor.value, authorName.value)
  }
}

function handleKeydown(e: KeyboardEvent) {
  if (e.key === 'Escape') inputText.value = ''
}

// ─── Welcome messages ─────────────────────────────────────
const welcomeMsgs = [
  { text: '欢迎来到云坛留言板 👋', color: '#93c5fd', author: '云坛' },
  { text: '发条弹幕，留下你的足迹！', color: '#6ee7b7', author: '云坛' },
  { text: '技术 · 创意 · 分享 🚀', color: '#c4b5fd', author: '云坛' },
  { text: 'Java | SpringBoot | Redis | MySQL ✨', color: '#fbbf24', author: '云坛' },
  { text: '感谢你来到这里，欢迎留下你的想法 💭', color: '#f9a8d4', author: '云坛' },
]

function getSeedMsgs(): StoredMsg[] {
  const now = Date.now()
  return welcomeMsgs.map((m, i) => ({ ...m, ts: now + i }))
}

let replayTimer: ReturnType<typeof setInterval> | null = null

onMounted(async () => {
  themeObserver = new MutationObserver(() => {
    isDark.value = document.documentElement.classList.contains('dark')
  })
  themeObserver.observe(document.documentElement, { attributes: true, attributeFilter: ['class'] })

  seedPool.value = getSeedMsgs()
  seedCursor.value = 0
  const loadedFromBackend = await loadDanmakuFromBackend(false)

  if (!loadedFromBackend) {
    const msgs: StoredMsg[] = JSON.parse(localStorage.getItem(STORAGE_KEY) || '[]')
    if (msgs.length === 0) {
      seedPool.value = getSeedMsgs()
      seedCursor.value = 0
      updateLoopPool([])
      welcomeMsgs.forEach((m, i) => {
        setTimeout(() => launchItem(buildItem(m.text, m.color, m.author)), i * 1400)
      })
    } else {
      replayMsgs()
    }
  }

  replayTimer = setInterval(async () => {
    const ok = await loadDanmakuFromBackend(true)
    if (!ok) replayMsgs()
  }, 33000)

  startKeepAliveEmitter()

  window.addEventListener('keydown', handleKeydown)
})

onUnmounted(() => {
  if (themeObserver) themeObserver.disconnect()
  if (replayTimer) clearInterval(replayTimer)
  if (keepAliveTimer) clearInterval(keepAliveTimer)
  window.removeEventListener('keydown', handleKeydown)
})
</script>

<template>
  <div class="message-board fixed inset-0 overflow-hidden" :class="{ 'light-mode': !isDark }">

    <!-- Navigation bar -->
    <div class="fixed top-0 w-full z-50">
      <Header />
    </div>

    <!-- Dot grid background -->
    <div class="dots-bg fixed inset-0 z-0 pointer-events-none"></div>

    <!-- Colored glow accents -->
    <div class="glow-accents fixed inset-0 z-0 pointer-events-none"></div>

    <!-- Decorative large "弹" character in background -->
    <div class="fixed inset-0 z-0 flex items-center justify-center pointer-events-none select-none" aria-hidden="true">
      <span class="deco-char">弹</span>
    </div>

    <!-- ── Danmaku canvas ── -->
    <div class="fixed inset-0 z-10 pointer-events-none overflow-hidden">
      <div
        v-for="item in activeDanmaku"
        :key="item.id"
        class="danmaku-item absolute left-0 whitespace-nowrap font-bold leading-none"
        :style="{
          top: item.top + 'px',
          color: item.color,
          fontSize: item.fontSize + 'px',
          '--dur': item.duration + 's',
        }"
        @animationend="removeDanmaku(item.id)"
      >{{ item.text }}</div>
    </div>

    <!-- Bottom fade overlay -->
    <div
      class="fixed bottom-0 left-0 right-0 z-20 pointer-events-none"
      :style="{ height: '170px', background: `linear-gradient(to top, ${boardBg} 25%, transparent)` }"
    ></div>

    <!-- ── Input panel ── -->
    <div class="fixed bottom-0 left-0 right-0 z-30 px-4 pb-5 md:px-8">
      <div class="max-w-3xl mx-auto">

        <!-- Color picker row -->
        <div class="flex items-center gap-2 mb-2.5 pl-1">
          <span class="ui-label text-xs font-medium tracking-widest">颜色</span>
          <div class="ui-divider w-px h-3 mx-1"></div>
          <button
            v-for="c in colorPresets"
            :key="c"
            @click="selectedColor = c"
            class="color-dot"
            :class="{ active: selectedColor === c }"
            :style="{ '--c': c, backgroundColor: c }"
          ></button>
        </div>

        <!-- Input row -->
        <div
          class="flex items-center gap-3 rounded-2xl border px-4 py-3 shadow-2xl"
          :class="isDark ? 'border-white/10' : 'border-indigo-200/60'"
          :style="{ background: isDark ? 'rgba(255,255,255,0.06)' : 'rgba(255,255,255,0.8)', backdropFilter: 'blur(24px)' }"
        >
          <!-- Color dot indicator -->
          <span
            class="shrink-0 w-2.5 h-2.5 rounded-full ring-2"
            :class="isDark ? 'ring-white/20' : 'ring-slate-300'"
            :style="{ backgroundColor: selectedColor }"
          ></span>

          <!-- Text input -->
          <input
            v-model="inputText"
            @keyup.enter="sendDanmaku"
            class="flex-1 bg-transparent outline-none text-sm min-w-0"
            :class="isDark ? 'text-white placeholder-white/25' : 'text-slate-700 placeholder-slate-400'"
            placeholder="说点什么，按 Enter 发射 🎯"
            maxlength="50"
            autocomplete="off"
          />

          <!-- Char counter -->
          <span class="ui-muted text-xs shrink-0 tabular-nums w-10 text-right">{{ inputText.length }}/50</span>

          <!-- Send button -->
          <button
            @click="sendDanmaku"
            :disabled="!inputText.trim()"
            class="send-btn shrink-0"
            :class="inputText.trim() ? 'send-active' : 'send-disabled'"
          >
            发射 ✈️
          </button>
        </div>

        <!-- Status row -->
        <div class="flex items-center justify-between mt-1.5 px-1">
          <p class="ui-muted text-xs">
            已有 <span class="ui-muted-em font-medium">{{ totalSent }}</span> 条留言沉淀于此
          </p>
          <p v-if="!isLoggedIn" class="ui-muted text-xs">
            <RouterLink to="/login" class="text-indigo-400/70 hover:text-indigo-300 underline transition-colors">登录</RouterLink>
            后以昵称留言
          </p>
          <p v-else class="ui-muted text-xs">
            发言人：<span class="ui-muted-em">{{ authorName }}</span>
          </p>
        </div>
      </div>
    </div>

  </div>
</template>

<style scoped>
/* ── Danmaku fly animation ───────────────────────────────── */
@keyframes danmaku-fly {
  from { transform: translateX(calc(100vw + 400px)); }
  to   { transform: translateX(calc(-100% - 20px)); }
}

.danmaku-item {
  animation: danmaku-fly var(--dur, 14s) linear forwards;
  text-shadow:
    0 0 6px rgba(0, 0, 0, 1),
    0 0 16px rgba(0, 0, 0, 0.9),
    1px 1px 3px rgba(0, 0, 0, 0.95),
    -1px -1px 3px rgba(0, 0, 0, 0.95);
  filter: drop-shadow(0 0 5px currentColor);
  will-change: transform;
  /* 允许鼠标事件穿透容器的 pointer-events-none */
  pointer-events: auto;
  cursor: default;
  transition: filter 0.2s ease, opacity 0.2s ease;
}

.danmaku-item:hover {
  animation-play-state: paused;
  filter: drop-shadow(0 0 12px currentColor) brightness(1.3);
  opacity: 0.92;
  z-index: 1;
}

/* ── Dot grid ────────────────────────────────────────────── */
.dots-bg {
  background-image: radial-gradient(rgba(255, 255, 255, 0.18) 1px, transparent 1px);
  background-size: 30px 30px;
  opacity: 0.12;
}

/* ── Decorative character ───────────────────────────────── */
.deco-char {
  font-size: min(40vw, 40vh);
  font-weight: 900;
  color: rgba(255, 255, 255, 0.07);
  line-height: 1;
  letter-spacing: -0.05em;
  user-select: none;
}

/* ── Color dot buttons ───────────────────────────────────── */
.color-dot {
  width: 18px;
  height: 18px;
  border-radius: 50%;
  border: 2px solid transparent;
  transition: transform 0.18s ease, border-color 0.18s ease, box-shadow 0.18s ease;
  cursor: pointer;
  flex-shrink: 0;
}
.color-dot:hover {
  transform: scale(1.2);
  border-color: rgba(255, 255, 255, 0.45);
}
.color-dot.active {
  transform: scale(1.35);
  border-color: white;
  box-shadow: 0 0 8px var(--c, white), 0 0 16px var(--c, white);
}

/* ── Send button ─────────────────────────────────────────── */
.send-btn {
  padding: 0.4rem 1.1rem;
  border-radius: 0.75rem;
  font-size: 0.8rem;
  font-weight: 700;
  transition: all 0.2s ease;
  white-space: nowrap;
  border: none;
  cursor: pointer;
}
.send-active {
  background: linear-gradient(135deg, #6366f1, #818cf8);
  color: white;
  box-shadow: 0 0 16px rgba(99, 102, 241, 0.5);
}
.send-active:hover {
  background: linear-gradient(135deg, #4f46e5, #6366f1);
  transform: scale(1.04);
  box-shadow: 0 0 22px rgba(99, 102, 241, 0.7);
}
.send-disabled {
  background: rgba(255, 255, 255, 0.07);
  color: rgba(255, 255, 255, 0.22);
  cursor: not-allowed;
}

/* ── Board background (theme-aware) ─────────────────────── */
.message-board {
  background: v-bind(boardBg);
}

/* ── Glow accents (dark default) ─────────────────────────── */
.glow-accents {
  background:
    radial-gradient(ellipse 70% 60% at 15% 55%, rgba(99,102,241,0.05), transparent),
    radial-gradient(ellipse 60% 50% at 85% 25%, rgba(56,189,248,0.04), transparent),
    radial-gradient(ellipse 50% 40% at 55% 80%, rgba(168,85,247,0.04), transparent);
}

/* ── Themed UI text elements (dark default) ─────────────── */
.ui-label {
  color: rgba(255, 255, 255, 0.25);
}
.ui-divider {
  background-color: rgba(255, 255, 255, 0.15);
}
.ui-muted {
  color: rgba(255, 255, 255, 0.2);
}
.ui-muted-em {
  color: rgba(255, 255, 255, 0.4);
}

/* ── Light mode overrides ────────────────────────────────── */
.light-mode .dots-bg {
  background-image: radial-gradient(rgba(30, 30, 80, 0.12) 1px, transparent 1px);
  opacity: 0.65;
}
.light-mode .glow-accents {
  background:
    radial-gradient(ellipse 70% 60% at 15% 55%, rgba(99,102,241,0.14), transparent),
    radial-gradient(ellipse 60% 50% at 85% 25%, rgba(56,189,248,0.11), transparent),
    radial-gradient(ellipse 50% 40% at 55% 80%, rgba(168,85,247,0.09), transparent);
}
.light-mode .deco-char {
  color: rgba(20, 20, 80, 0.04);
}
.light-mode .danmaku-item {
  text-shadow:
    0 0 4px rgba(0, 0, 0, 0.85),
    0 0 12px rgba(0, 0, 0, 0.65),
    1px 1px 2px rgba(0, 0, 0, 0.95),
    -1px -1px 2px rgba(0, 0, 0, 0.9),
    2px 2px 4px rgba(0, 0, 0, 0.7);
  filter: drop-shadow(0 0 3px currentColor);
}
.light-mode .send-disabled {
  background: rgba(30, 30, 80, 0.06);
  color: rgba(30, 30, 80, 0.3);
}
.light-mode .ui-label {
  color: rgba(30, 30, 80, 0.45);
}
.light-mode .ui-divider {
  background-color: rgba(30, 30, 80, 0.15);
}
.light-mode .ui-muted {
  color: rgba(30, 30, 80, 0.5);
}
.light-mode .ui-muted-em {
  color: rgba(30, 30, 80, 0.75);
}
</style>
