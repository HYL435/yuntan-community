<script setup lang="ts" name="MessageBoardView">
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useUserStore } from '@/stores/user'
import Header from '@/layouts/Header.vue'

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

function launchItem(item: DanmakuItem) {
  activeDanmaku.value.push(item)
  setTimeout(() => {
    const i = activeDanmaku.value.findIndex(d => d.id === item.id)
    if (i !== -1) activeDanmaku.value.splice(i, 1)
  }, (item.duration + 1) * 1000)
}

// ─── LocalStorage ─────────────────────────────────────────
const STORAGE_KEY = 'yuntan-danmaku-v1'
const MAX_STORED = 100

function saveMsg(text: string, color: string, author: string) {
  try {
    const msgs: StoredMsg[] = JSON.parse(localStorage.getItem(STORAGE_KEY) || '[]')
    msgs.push({ text, color, author, ts: Date.now() })
    if (msgs.length > MAX_STORED) msgs.splice(0, msgs.length - MAX_STORED)
    localStorage.setItem(STORAGE_KEY, JSON.stringify(msgs))
    totalSent.value = msgs.length
  } catch { /* noop */ }
}

function replayMsgs() {
  try {
    const msgs: StoredMsg[] = JSON.parse(localStorage.getItem(STORAGE_KEY) || '[]')
    totalSent.value = msgs.length
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
function sendDanmaku() {
  const text = inputText.value.trim()
  if (!text) return
  launchItem(buildItem(text, selectedColor.value, authorName.value))
  saveMsg(text, selectedColor.value, authorName.value)
  inputText.value = ''
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

let replayTimer: ReturnType<typeof setInterval> | null = null

onMounted(() => {
  const msgs: StoredMsg[] = JSON.parse(localStorage.getItem(STORAGE_KEY) || '[]')
  if (msgs.length === 0) {
    welcomeMsgs.forEach((m, i) => {
      setTimeout(() => launchItem(buildItem(m.text, m.color, m.author)), i * 1400)
    })
  } else {
    replayMsgs()
  }
  replayTimer = setInterval(replayMsgs, 33000)
  window.addEventListener('keydown', handleKeydown)
})

onUnmounted(() => {
  if (replayTimer) clearInterval(replayTimer)
  window.removeEventListener('keydown', handleKeydown)
})
</script>

<template>
  <div class="message-board fixed inset-0 overflow-hidden" style="background: #080c18;">

    <!-- Navigation bar -->
    <div class="fixed top-0 w-full z-50">
      <Header />
    </div>

    <!-- Dot grid background -->
    <div class="dots-bg fixed inset-0 z-0 pointer-events-none opacity-35"></div>

    <!-- Colored glow accents -->
    <div
      class="fixed inset-0 z-0 pointer-events-none"
      style="
        background:
          radial-gradient(ellipse 70% 60% at 15% 55%, rgba(99,102,241,0.09), transparent),
          radial-gradient(ellipse 60% 50% at 85% 25%, rgba(56,189,248,0.07), transparent),
          radial-gradient(ellipse 50% 40% at 55% 80%, rgba(168,85,247,0.06), transparent);
      "
    ></div>

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
      >{{ item.text }}</div>
    </div>

    <!-- Bottom fade overlay -->
    <div
      class="fixed bottom-0 left-0 right-0 z-20 pointer-events-none"
      style="height: 170px; background: linear-gradient(to top, #080c18 25%, transparent);"
    ></div>

    <!-- ── Input panel ── -->
    <div class="fixed bottom-0 left-0 right-0 z-30 px-4 pb-5 md:px-8">
      <div class="max-w-3xl mx-auto">

        <!-- Color picker row -->
        <div class="flex items-center gap-2 mb-2.5 pl-1">
          <span class="text-white/25 text-xs font-medium tracking-widest">颜色</span>
          <div class="w-px h-3 bg-white/15 mx-1"></div>
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
          class="flex items-center gap-3 rounded-2xl border border-white/10 px-4 py-3 shadow-2xl"
          style="background: rgba(255,255,255,0.06); backdrop-filter: blur(24px);"
        >
          <!-- Color dot indicator -->
          <span
            class="shrink-0 w-2.5 h-2.5 rounded-full ring-2 ring-white/20"
            :style="{ backgroundColor: selectedColor }"
          ></span>

          <!-- Text input -->
          <input
            v-model="inputText"
            @keyup.enter="sendDanmaku"
            class="flex-1 bg-transparent text-white placeholder-white/25 outline-none text-sm min-w-0"
            placeholder="说点什么，按 Enter 发射 🎯"
            maxlength="50"
            autocomplete="off"
          />

          <!-- Char counter -->
          <span class="text-white/20 text-xs shrink-0 tabular-nums w-10 text-right">{{ inputText.length }}/50</span>

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
          <p class="text-white/20 text-xs">
            已有 <span class="text-white/40 font-medium">{{ totalSent }}</span> 条留言沉淀于此
          </p>
          <p v-if="!isLoggedIn" class="text-white/20 text-xs">
            <RouterLink to="/login" class="text-indigo-400/70 hover:text-indigo-300 underline transition-colors">登录</RouterLink>
            后以昵称留言
          </p>
          <p v-else class="text-white/20 text-xs">
            发言人：<span class="text-white/40">{{ authorName }}</span>
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
}

/* ── Dot grid ────────────────────────────────────────────── */
.dots-bg {
  background-image: radial-gradient(rgba(255, 255, 255, 0.22) 1px, transparent 1px);
  background-size: 30px 30px;
}

/* ── Decorative character ───────────────────────────────── */
.deco-char {
  font-size: min(40vw, 40vh);
  font-weight: 900;
  color: rgba(255, 255, 255, 0.018);
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
</style>
