<script setup lang="ts" name="NetworkErrorView">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()

// 从 query 参数接收来源路由，用于重试
const from = computed(() => (route.query.from as string) || '/')

// 倒计时重试
const countdown = ref(0)
const retrying = ref(false)
let timer: ReturnType<typeof setInterval> | null = null

function startRetryCountdown(seconds = 15) {
  countdown.value = seconds
  timer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(timer!)
      timer = null
      doRetry()
    }
  }, 1000)
}

async function doRetry() {
  retrying.value = true
  countdown.value = 0
  if (timer) { clearInterval(timer); timer = null }
  try {
    // 尝试 ping 后端根路径判断是否恢复
    const resp = await fetch('/api/front/articles?page=1&size=1', {
      signal: AbortSignal.timeout(5000),
      cache: 'no-store'
    })
    if (resp.ok || resp.status < 500) {
      // 服务已恢复，跳回来源页
      router.replace(from.value)
      return
    }
  } catch {
    // 仍然不通
  }
  retrying.value = false
  startRetryCountdown(15)
}

// 波纹动画相位（3 根信号柱的延迟）
const pulse = ref(0)
let pulseFn: ReturnType<typeof setInterval> | null = null

onMounted(() => {
  startRetryCountdown(15)
  pulseFn = setInterval(() => { pulse.value = (pulse.value + 1) % 3 }, 700)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
  if (pulseFn) clearInterval(pulseFn)
})

import { computed } from 'vue'
</script>

<template>
  <div class="ne-wrap">
    <!-- 流动粒子背景 -->
    <div class="particle-bg" aria-hidden="true">
      <span v-for="n in 18" :key="n" class="particle" :style="{ '--i': n }" />
    </div>

    <div class="ne-card">
      <!-- 断连插图 -->
      <div class="illustration" aria-hidden="true">
        <svg viewBox="0 0 260 200" fill="none" xmlns="http://www.w3.org/2000/svg">
          <!-- 服务器机箱 -->
          <rect x="80" y="30" width="100" height="70" rx="10" fill="url(#srvGrad)" />
          <rect x="80" y="30" width="100" height="18" rx="10" fill="rgba(255,255,255,0.1)" />
          <!-- 服务器指示灯 -->
          <circle cx="96" cy="39" r="4" fill="#ef4444" class="blink-red"/>
          <circle cx="110" cy="39" r="4" fill="#f59e0b" />
          <circle cx="124" cy="39" r="4" fill="#374151" />
          <!-- 磁盘槽 -->
          <rect x="92" y="54" width="76" height="8" rx="4" fill="rgba(255,255,255,0.08)" />
          <rect x="92" y="66" width="76" height="8" rx="4" fill="rgba(255,255,255,0.06)" />
          <rect x="92" y="78" width="50" height="8" rx="4" fill="rgba(255,255,255,0.06)" />

          <!-- 断裂的网线上半段 -->
          <path d="M130 100 L130 130 Q130 135 138 137" stroke="url(#cableGrad)" stroke-width="4.5" stroke-linecap="round" fill="none"/>
          <!-- 断口闪电 -->
          <path d="M136 137 L143 141 L138 145 L148 150" stroke="#fbbf24" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
          <!-- 断裂的网线下半段 -->
          <path d="M148 150 Q155 153 155 158 L155 185" stroke="url(#cableGrad2)" stroke-width="4.5" stroke-linecap="round" fill="none"/>

          <!-- WiFi 信号圆弧（断开状态） -->
          <g transform="translate(160, 50)">
            <!-- 第3圈（最弱，已断） -->
            <path d="M-28,20 A36,36 0 0,1 28,20" stroke="#374151" stroke-width="3.5" stroke-linecap="round" fill="none"/>
            <!-- 第2圈（断开） -->
            <path d="M-18,14 A24,24 0 0,1 18,14" stroke="#374151" stroke-width="3.5" stroke-linecap="round" fill="none"/>
            <!-- 第1圈（有微弱信号） -->
            <path d="M-9,8 A12,12 0 0,1 9,8" stroke="#f59e0b" stroke-width="3.5" stroke-linecap="round" fill="none" class="flicker"/>
            <!-- 信号点 -->
            <circle cx="0" cy="20" r="4" fill="#f59e0b" class="flicker"/>
            <!-- X 叉号 -->
            <line x1="-10" y1="-8" x2="10" y2="8" stroke="#ef4444" stroke-width="3" stroke-linecap="round"/>
            <line x1="10" y1="-8" x2="-10" y2="8" stroke="#ef4444" stroke-width="3" stroke-linecap="round"/>
          </g>

          <!-- 地球/客户端图标（底部） -->
          <circle cx="155" cy="185" r="13" fill="url(#earthGrad)" />
          <ellipse cx="155" cy="185" rx="13" ry="6" stroke="rgba(255,255,255,0.2)" stroke-width="1.5" fill="none"/>
          <line x1="155" y1="172" x2="155" y2="198" stroke="rgba(255,255,255,0.2)" stroke-width="1.5"/>

          <defs>
            <linearGradient id="srvGrad" x1="80" y1="30" x2="180" y2="100" gradientUnits="userSpaceOnUse">
              <stop offset="0%" stop-color="#1e293b"/>
              <stop offset="100%" stop-color="#0f172a"/>
            </linearGradient>
            <linearGradient id="cableGrad" x1="130" y1="100" x2="138" y2="137" gradientUnits="userSpaceOnUse">
              <stop offset="0%" stop-color="#475569"/>
              <stop offset="100%" stop-color="#334155"/>
            </linearGradient>
            <linearGradient id="cableGrad2" x1="148" y1="150" x2="155" y2="185" gradientUnits="userSpaceOnUse">
              <stop offset="0%" stop-color="#334155"/>
              <stop offset="100%" stop-color="#475569"/>
            </linearGradient>
            <linearGradient id="earthGrad" x1="142" y1="172" x2="168" y2="198" gradientUnits="userSpaceOnUse">
              <stop offset="0%" stop-color="#1d4ed8"/>
              <stop offset="100%" stop-color="#1e3a5f"/>
            </linearGradient>
          </defs>
        </svg>
      </div>

      <!-- 状态标签 -->
      <div class="status-badge">
        <span class="badge-dot"></span>
        无法连接到服务器
      </div>

      <h1 class="ne-title">服务连接中断</h1>
      <p class="ne-desc">
        未能与服务器建立连接，可能是网络不稳定或服务器暂时不可用。<br/>
        请检查您的网络，稍后将自动重试。
      </p>

      <!-- 倒计时 / 重试状态 -->
      <div class="retry-status">
        <template v-if="retrying">
          <svg class="spin" viewBox="0 0 24 24" fill="none">
            <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2.5" stroke-dasharray="56" stroke-dashoffset="18"/>
          </svg>
          正在尝试重新连接…
        </template>
        <template v-else-if="countdown > 0">
          <svg viewBox="0 0 24 24" fill="currentColor"><path d="M12 2a10 10 0 100 20A10 10 0 0012 2zm1 5v5.59l3.71 3.7-1.42 1.42L11 13.41V7h2z"/></svg>
          {{ countdown }} 秒后自动重试
        </template>
      </div>

      <!-- 操作按钮 -->
      <div class="ne-actions">
        <button class="btn-primary" :disabled="retrying" @click="doRetry">
          <svg viewBox="0 0 20 20" fill="currentColor">
            <path fill-rule="evenodd" d="M4 2a1 1 0 011 1v2.101a7.002 7.002 0 0111.601 2.566 1 1 0 11-1.885.666A5.002 5.002 0 005.999 7H9a1 1 0 010 2H4a1 1 0 01-1-1V3a1 1 0 011-1zm.008 9.057a1 1 0 011.276.61A5.002 5.002 0 0014.001 13H11a1 1 0 110-2h5a1 1 0 011 1v5a1 1 0 11-2 0v-2.101a7.002 7.002 0 01-11.601-2.566 1 1 0 01.61-1.276z" clip-rule="evenodd"/>
          </svg>
          {{ retrying ? '连接中…' : '立即重试' }}
        </button>
        <button class="btn-ghost" @click="router.push('/')">
          <svg viewBox="0 0 20 20" fill="currentColor"><path d="M10.707 2.293a1 1 0 00-1.414 0l-7 7A1 1 0 003 11h1v6a1 1 0 001 1h4v-4h2v4h4a1 1 0 001-1v-6h1a1 1 0 00.707-1.707l-7-7z"/></svg>
          回到首页
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.ne-wrap {
  position: fixed;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: radial-gradient(ellipse at 60% 30%, #1a1a2e 0%, #0d1117 55%, #090c10 100%);
  overflow: hidden;
  z-index: 100;
  font-family: system-ui, 'PingFang SC', sans-serif;
}

/* ─── 粒子背景 ─── */
.particle-bg { position: absolute; inset: 0; pointer-events: none; }
.particle {
  position: absolute;
  width: 3px;
  height: 3px;
  border-radius: 50%;
  background: rgba(99, 102, 241, 0.55);
  left: calc(var(--i) * 5.6%);
  animation: floatUp calc(6s + var(--i) * 0.4s) linear infinite;
  animation-delay: calc(var(--i) * -0.35s);
}
@keyframes floatUp {
  0%   { top: 105%; opacity: 0; transform: translateX(0) scale(0.7); }
  10%  { opacity: 0.8; }
  90%  { opacity: 0.5; }
  100% { top: -5%; opacity: 0; transform: translateX(calc(sin(var(--i)) * 30px)) scale(1.1); }
}

/* ─── 卡片 ─── */
.ne-card {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  padding: 36px 48px 40px;
  max-width: 500px;
  width: calc(100% - 40px);
  background: rgba(255, 255, 255, 0.035);
  border: 1px solid rgba(99, 102, 241, 0.2);
  border-radius: 28px;
  backdrop-filter: blur(20px) saturate(140%);
  box-shadow:
    0 0 80px rgba(79, 70, 229, 0.12),
    0 24px 48px rgba(0, 0, 0, 0.5);
}

/* ─── 插图 ─── */
.illustration {
  width: 200px;
  margin-bottom: 6px;
}
.illustration svg {
  width: 100%;
  height: auto;
  filter: drop-shadow(0 6px 20px rgba(99, 102, 241, 0.28));
}

/* ─── 插图动画 ─── */
.blink-red { animation: blinkRed 1.2s ease-in-out infinite; }
@keyframes blinkRed {
  0%, 100% { fill: #ef4444; }
  50%       { fill: #7f1d1d; }
}
.flicker { animation: flicker 1.8s ease-in-out infinite; }
@keyframes flicker {
  0%, 100% { opacity: 1; }
  40%       { opacity: 0.15; }
  60%       { opacity: 0.7; }
}

/* ─── 状态徽章 ─── */
.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  padding: 5px 14px;
  border-radius: 999px;
  background: rgba(239, 68, 68, 0.12);
  border: 1px solid rgba(239, 68, 68, 0.3);
  color: #fca5a5;
  font-size: 0.8rem;
  font-weight: 600;
  margin-bottom: 14px;
}
.badge-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #ef4444;
  animation: blinkRed 1s infinite;
}

/* ─── 文字 ─── */
.ne-title {
  font-size: 1.7rem;
  font-weight: 800;
  color: #f1f5f9;
  margin: 0 0 10px;
  letter-spacing: -0.5px;
}
.ne-desc {
  font-size: 0.88rem;
  color: rgba(203, 213, 225, 0.6);
  line-height: 1.7;
  margin: 0 0 20px;
}

/* ─── 重试状态 ─── */
.retry-status {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  font-size: 0.85rem;
  color: #94a3b8;
  margin-bottom: 24px;
  min-height: 24px;
}
.retry-status svg { width: 17px; height: 17px; color: #6366f1; }
.spin {
  animation: rotating 1.1s linear infinite;
  color: #818cf8;
}
@keyframes rotating { to { transform: rotate(360deg); } }

/* ─── 按钮组 ─── */
.ne-actions {
  display: flex;
  gap: 14px;
  flex-wrap: wrap;
  justify-content: center;
}
.btn-primary, .btn-ghost {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  padding: 10px 22px;
  border-radius: 12px;
  font-size: 0.9rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.22s ease;
  border: none;
  outline: none;
}
.btn-primary svg, .btn-ghost svg { width: 17px; height: 17px; }

.btn-primary {
  background: linear-gradient(135deg, #4f46e5, #6366f1);
  color: #fff;
  box-shadow: 0 4px 16px rgba(79, 70, 229, 0.38);
}
.btn-primary:hover:not(:disabled) {
  background: linear-gradient(135deg, #6366f1, #818cf8);
  transform: translateY(-2px);
  box-shadow: 0 6px 22px rgba(99, 102, 241, 0.5);
}
.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
.btn-ghost {
  background: rgba(255, 255, 255, 0.07);
  color: #cbd5e1;
  border: 1px solid rgba(148, 163, 184, 0.22);
}
.btn-ghost:hover {
  background: rgba(255, 255, 255, 0.13);
  color: #e2e8f0;
  transform: translateY(-2px);
}

@media (max-width: 480px) {
  .ne-card { padding: 24px 20px 28px; }
  .ne-title { font-size: 1.4rem; }
  .illustration { width: 160px; }
}
</style>
