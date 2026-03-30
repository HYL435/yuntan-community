<script setup lang="ts" name="NotFoundView">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

interface Star {
  x: number
  y: number
  size: number
  delay: number
  duration: number
  opacity: number
}

const stars = ref<Star[]>([])
const floatY = ref(0)
let animFrame: number | null = null
let startTime = 0

function animate(ts: number) {
  if (!startTime) startTime = ts
  floatY.value = Math.sin((ts - startTime) / 1600) * 14
  animFrame = requestAnimationFrame(animate)
}

onMounted(() => {
  stars.value = Array.from({ length: 70 }, () => ({
    x: Math.random() * 100,
    y: Math.random() * 100,
    size: Math.random() * 2.2 + 0.4,
    delay: Math.random() * 5,
    duration: Math.random() * 3 + 2,
    opacity: Math.random() * 0.5 + 0.3,
  }))
  animFrame = requestAnimationFrame(animate)
})

onUnmounted(() => {
  if (animFrame) cancelAnimationFrame(animFrame)
})
</script>

<template>
  <div class="nf-wrap">
    <!-- 星空背景 -->
    <div class="star-field" aria-hidden="true">
      <span
        v-for="(s, i) in stars"
        :key="i"
        class="star"
        :style="{
          left: s.x + '%',
          top: s.y + '%',
          width: s.size + 'px',
          height: s.size + 'px',
          opacity: s.opacity,
          animationDelay: s.delay + 's',
          animationDuration: s.duration + 's',
        }"
      />
    </div>

    <!-- 主体内容 -->
    <div class="nf-card">
      <!-- 悬浮插图 -->
      <div class="illustration" :style="{ transform: `translateY(${floatY}px)` }" aria-hidden="true">
        <svg viewBox="0 0 240 220" fill="none" xmlns="http://www.w3.org/2000/svg">
          <!-- 行星主体 -->
          <ellipse cx="120" cy="140" rx="68" ry="60" fill="url(#pGrad)" />
          <!-- 行星阴影 -->
          <ellipse cx="120" cy="140" rx="68" ry="60" fill="url(#pShadow)" />
          <!-- 行星表面纹理 -->
          <ellipse cx="105" cy="118" rx="14" ry="9" fill="rgba(255,255,255,0.07)" />
          <ellipse cx="138" cy="128" rx="8" ry="5" fill="rgba(255,255,255,0.05)" />
          <ellipse cx="95" cy="142" rx="18" ry="7" fill="rgba(255,255,255,0.06)" />
          <!-- 轨道环 -->
          <ellipse cx="120" cy="140" rx="96" ry="22" stroke="url(#ringGrad)" stroke-width="3.5" fill="none" opacity="0.6"/>
          <!-- 小月亮 -->
          <circle cx="38" cy="128" r="12" fill="#c4b5fd" />
          <circle cx="44" cy="124" r="4" fill="rgba(255,255,255,0.15)" />

          <!-- 旗杆 -->
          <line x1="120" y1="82" x2="120" y2="55" stroke="#e2e8f0" stroke-width="2.2" stroke-linecap="round"/>
          <!-- 旗帜 -->
          <path d="M120 55 L144 63 L120 71 Z" fill="#818cf8"/>
          <path d="M120 55 L144 63 L120 71 Z" fill="url(#flagGrad)" opacity="0.8"/>
          <!-- 404 文字在旗子上 -->
          <text x="128" y="67" font-size="8.5" fill="white" font-family="monospace" font-weight="bold">404</text>

          <!-- 宇航员 -->
          <g transform="translate(72, 60)">
            <!-- 头盔 -->
            <circle cx="22" cy="20" r="17" fill="#e2e8f0" />
            <circle cx="22" cy="20" r="13" fill="#1e1b4b" />
            <!-- 头盔玻璃高光 -->
            <ellipse cx="17" cy="15" rx="5" ry="4" fill="rgba(139,92,246,0.35)" />
            <ellipse cx="26" cy="13" rx="2.5" ry="2" fill="rgba(255,255,255,0.2)" />
            <!-- 宇航服身体 -->
            <rect x="10" y="36" width="24" height="22" rx="7" fill="#cbd5e1"/>
            <!-- 胸部徽章 -->
            <rect x="17" y="41" width="10" height="7" rx="2" fill="#818cf8" opacity="0.7"/>
            <!-- 手臂 -->
            <rect x="0" y="37" width="11" height="7" rx="4" fill="#cbd5e1"/>
            <rect x="33" y="37" width="11" height="7" rx="4" fill="#cbd5e1"/>
            <!-- 手套 -->
            <circle cx="4" cy="44" r="5" fill="#94a3b8"/>
            <circle cx="40" cy="44" r="5" fill="#94a3b8"/>
            <!-- 腿 -->
            <rect x="12" y="56" width="8" height="14" rx="4" fill="#94a3b8"/>
            <rect x="24" y="56" width="8" height="14" rx="4" fill="#94a3b8"/>
            <!-- 靴子 -->
            <rect x="10" y="66" width="12" height="6" rx="3" fill="#64748b"/>
            <rect x="22" y="66" width="12" height="6" rx="3" fill="#64748b"/>
          </g>

          <defs>
            <linearGradient id="pGrad" x1="60" y1="80" x2="185" y2="200" gradientUnits="userSpaceOnUse">
              <stop offset="0%" stop-color="#4c1d95"/>
              <stop offset="100%" stop-color="#1e3a5f"/>
            </linearGradient>
            <radialGradient id="pShadow" cx="40%" cy="35%" r="60%">
              <stop offset="0%" stop-color="rgba(139,92,246,0.22)"/>
              <stop offset="100%" stop-color="rgba(0,0,0,0.38)"/>
            </radialGradient>
            <linearGradient id="ringGrad" x1="24" y1="140" x2="216" y2="140" gradientUnits="userSpaceOnUse">
              <stop offset="0%" stop-color="#7c3aed" stop-opacity="0"/>
              <stop offset="30%" stop-color="#a78bfa"/>
              <stop offset="70%" stop-color="#818cf8"/>
              <stop offset="100%" stop-color="#7c3aed" stop-opacity="0"/>
            </linearGradient>
            <linearGradient id="flagGrad" x1="120" y1="55" x2="144" y2="71" gradientUnits="userSpaceOnUse">
              <stop offset="0%" stop-color="#c4b5fd"/>
              <stop offset="100%" stop-color="#4c1d95"/>
            </linearGradient>
          </defs>
        </svg>
      </div>

      <!-- 404 大字 -->
      <h1 class="nf-code" aria-label="404">404</h1>
      <p class="nf-title">这个页面不见了</p>
      <p class="nf-desc">您访问的路径不存在，可能已被删除或从未出现过。</p>

      <div class="nf-actions">
        <button class="btn-primary" @click="router.push('/')">
          <svg viewBox="0 0 20 20" fill="currentColor"><path d="M10.707 2.293a1 1 0 00-1.414 0l-7 7A1 1 0 003 11h1v6a1 1 0 001 1h4v-4h2v4h4a1 1 0 001-1v-6h1a1 1 0 00.707-1.707l-7-7z"/></svg>
          回到首页
        </button>
        <button class="btn-ghost" @click="router.back()">
          <svg viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M9.707 14.707a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414l4-4a1 1 0 011.414 1.414L7.414 9H15a1 1 0 110 2H7.414l2.293 2.293a1 1 0 010 1.414z" clip-rule="evenodd"/></svg>
          返回上页
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* ─── 整体容器 ─── */
.nf-wrap {
  position: fixed;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: radial-gradient(ellipse at 30% 40%, #1e1b4b 0%, #0f0f1e 55%, #0a0a0f 100%);
  overflow: hidden;
  z-index: 100;
  font-family: system-ui, 'PingFang SC', sans-serif;
}

/* ─── 星空 ─── */
.star-field {
  position: absolute;
  inset: 0;
  pointer-events: none;
}
.star {
  position: absolute;
  border-radius: 50%;
  background: #fff;
  animation: twinkle linear infinite;
}
@keyframes twinkle {
  0%, 100% { opacity: var(--op, 0.5); transform: scale(1); }
  50% { opacity: calc(var(--op, 0.5) * 0.25); transform: scale(0.6); }
}

/* ─── 主卡片 ─── */
.nf-card {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  padding: 40px 48px 44px;
  max-width: 480px;
  width: calc(100% - 40px);
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(139, 92, 246, 0.22);
  border-radius: 28px;
  backdrop-filter: blur(18px) saturate(140%);
  box-shadow:
    0 0 60px rgba(109, 40, 217, 0.14),
    0 24px 48px rgba(0, 0, 0, 0.44);
}

/* ─── 插图 ─── */
.illustration {
  width: 220px;
  height: auto;
  margin-bottom: 4px;
  transition: transform 0.05s linear;
  will-change: transform;
}
.illustration svg {
  width: 100%;
  height: auto;
  filter: drop-shadow(0 8px 24px rgba(139, 92, 246, 0.38));
}

/* ─── 404 数字 ─── */
.nf-code {
  font-size: 5rem;
  font-weight: 900;
  line-height: 1;
  margin: 0 0 8px;
  letter-spacing: -3px;
  background: linear-gradient(135deg, #c4b5fd 20%, #818cf8 60%, #38bdf8 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  filter: drop-shadow(0 2px 16px rgba(139, 92, 246, 0.5));
}

/* ─── 标题 & 描述 ─── */
.nf-title {
  font-size: 1.35rem;
  font-weight: 700;
  color: #e2e8f0;
  margin: 0 0 8px;
}
.nf-desc {
  font-size: 0.9rem;
  color: rgba(203, 213, 225, 0.65);
  margin: 0 0 28px;
  line-height: 1.6;
}

/* ─── 按钮组 ─── */
.nf-actions {
  display: flex;
  gap: 14px;
  flex-wrap: wrap;
  justify-content: center;
}

.btn-primary,
.btn-ghost {
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
.btn-primary svg,
.btn-ghost svg { width: 17px; height: 17px; }

.btn-primary {
  background: linear-gradient(135deg, #7c3aed, #6366f1);
  color: #fff;
  box-shadow: 0 4px 16px rgba(124, 58, 237, 0.38);
}
.btn-primary:hover {
  background: linear-gradient(135deg, #8b5cf6, #818cf8);
  transform: translateY(-2px);
  box-shadow: 0 6px 22px rgba(139, 92, 246, 0.5);
}
.btn-primary:active { transform: translateY(0); }

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
.btn-ghost:active { transform: translateY(0); }

/* ─── 响应式 ─── */
@media (max-width: 480px) {
  .nf-card { padding: 28px 24px 32px; }
  .nf-code { font-size: 4rem; }
  .illustration { width: 170px; }
}
</style>
