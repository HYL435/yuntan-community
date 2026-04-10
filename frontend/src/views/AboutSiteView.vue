<template>
  <div class="about-site-page">
    <section class="about-hero">
      <BackHomeButton />
      <div
        class="theme-switcher theme-switcher--floating"
        :class="{ 'theme-switcher--dark': isDarkMode }"
        role="group"
        aria-label="切换主题模式"
      >
        <button
          type="button"
          class="theme-chip"
          :class="{ 'theme-chip--active': !isDarkMode }"
          @click="applyTheme(false)"
        >
          <span class="theme-chip-icon" aria-hidden="true">☀</span>
          <span>浅色</span>
        </button>
        <button
          type="button"
          class="theme-chip"
          :class="{ 'theme-chip--active': isDarkMode }"
          @click="applyTheme(true)"
        >
          <span class="theme-chip-icon" aria-hidden="true">☾</span>
          <span>深色</span>
        </button>
      </div>

      <div class="hero-shell">
        <div class="hero-copy">
          <div class="hero-toolbar">
            <p class="hero-kicker">About This Site</p>
          </div>
          <h1 class="hero-title hero-title--calligraphy">建站历程</h1>
          <p class="hero-desc">
            这不是一串静态时间点，而是一段被滚动推动的建站过程。向下滑动页面，时间会向左展开，节点会逐个滑入并逐渐显现。
          </p>
        </div>

        <div class="hero-aside">
          <div class="hero-badge">
            <span class="badge-label">Timeline</span>
            <strong class="badge-value">{{ timeline.length }} 个阶段</strong>
          </div>
          <div class="hero-badge hero-badge--soft">
            <span class="badge-label">Updated</span>
            <strong class="badge-value">2026-04-10</strong>
          </div>
        </div>
      </div>
    </section>

    <section ref="timelineSectionRef" class="timeline-scroll-section">
      <div ref="timelineViewportRef" class="timeline-viewport">
        <div class="timeline-orb timeline-orb--left"></div>
        <div class="timeline-orb timeline-orb--right"></div>
        <div class="timeline-stage">
          <div class="timeline-heading">
            <p class="timeline-heading-kicker">Site Journey</p>
            <h2 class="timeline-heading-title text-gradient">从落地到打磨，每一步都可见</h2>
          </div>
          <div class="timeline-line"></div>
          <div
            ref="timelineTrackRef"
            class="timeline-track"
            :style="trackStyle"
          >
            <article
              v-for="(item, index) in timeline"
              :key="item.date + item.title"
              class="timeline-node"
              :style="getNodeStyle(index)"
            >
              <div class="node-dot"></div>
              <time class="node-date">{{ item.date }}</time>
              <h3 class="node-title node-title--ink">{{ item.title }}</h3>
              <p class="node-desc">{{ item.desc }}</p>
            </article>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts" name="AboutSiteView">
import { computed, nextTick, onMounted, onUnmounted, ref } from 'vue'
import BackHomeButton from '@/components/common/BackHomeButton.vue'
import { useIsDark } from '@/composables/useIsDark'
import http from '@/api/http'

type TimelineItem = {
  date: string
  title: string
  desc: string
}

// 将后端 LocalDate 序列化的各种格式统一为 YYYY-MM-DD 字符串
const normalizeDate = (raw: any): string => {
  if (!raw) return ''
  if (Array.isArray(raw)) {
    const [y, m = 1, d = 1] = raw.map((n: any) => Number(n) || 0)
    return `${y}-${String(m).padStart(2, '0')}-${String(d).padStart(2, '0')}`
  }
  if (typeof raw === 'object') {
    const y = Number(raw.year)
    const m = Number(raw.monthValue ?? raw.month ?? 1)
    const d = Number(raw.dayOfMonth ?? raw.day ?? 1)
    if (y) return `${y}-${String(m).padStart(2, '0')}-${String(d).padStart(2, '0')}`
  }
  return String(raw).slice(0, 10)
}

const timeline = ref<TimelineItem[]>([])
const timelineLoading = ref(false)

const fetchTimeline = async () => {
  timelineLoading.value = true
  try {
    const res = await http.get('/front/timeline')
    const body = res.data || res
    const data = body.data ?? body
    const list: any[] = Array.isArray(data) ? data : []
    timeline.value = list.map((item: any) => ({
      date: normalizeDate(item.eventDate),
      title: item.title ?? '',
      desc: item.description ?? '',
    }))
  } catch (_err) {
    // 接口暂不可用时保持空列表，不影响页面渲染
    timeline.value = []
  } finally {
    timelineLoading.value = false
  }
}

const timelineSectionRef = ref<HTMLElement | null>(null)
const timelineViewportRef = ref<HTMLElement | null>(null)
const timelineTrackRef = ref<HTMLElement | null>(null)
const scrollProgress = ref(0)
const sectionReveal = ref(0)
const maxTranslate = ref(0)
const isDark = useIsDark()
let ticking = false
let snapLock = false
let snapLockTimer: number | null = null
let snapAnimationFrame: number | null = null

const TIMELINE_START_DEADZONE = 28
const TIMELINE_SNAP_DELTA = 5

const clamp = (value: number, min = 0, max = 1) => Math.min(max, Math.max(min, value))
const easeOutCubic = (value: number) => 1 - Math.pow(1 - value, 3)
const isDarkMode = computed(() => isDark.value)

const applyTheme = (dark: boolean) => {
  const html = document.documentElement
  if (dark) {
    html.classList.add('dark')
    html.classList.add('dark-mode')
  } else {
    html.classList.remove('dark')
    html.classList.remove('dark-mode')
  }
  localStorage.setItem('dark-mode', String(dark))
}

const trackStyle = computed(() => {
  const x = -Math.round(maxTranslate.value * scrollProgress.value)
  return {
    transform: `translate3d(${x}px, 0, 0)`,
  }
})

const updateMetrics = () => {
  const trackEl = timelineTrackRef.value
  const viewportEl = timelineViewportRef.value
  if (!trackEl || !viewportEl) return
  const max = trackEl.scrollWidth - viewportEl.clientWidth
  maxTranslate.value = Math.max(0, max)
}

const updateProgress = () => {
  const sectionEl = timelineSectionRef.value
  if (!sectionEl) return

  const rect = sectionEl.getBoundingClientRect()
  const viewportH = window.innerHeight || document.documentElement.clientHeight
  const sectionScrollRange = Math.max(1, rect.height - viewportH - TIMELINE_START_DEADZONE)
  const consumedRaw = -rect.top - TIMELINE_START_DEADZONE
  const consumed = Math.min(Math.max(consumedRaw, 0), sectionScrollRange)
  scrollProgress.value = consumed / sectionScrollRange
  sectionReveal.value = clamp((viewportH - rect.top) / (viewportH * 0.78))
}

const requestUpdate = () => {
  if (ticking) return
  ticking = true
  requestAnimationFrame(() => {
    updateProgress()
    ticking = false
  })
}

const smoothSnapTo = (targetTop: number, duration = 460) => {
  if (snapAnimationFrame != null) {
    window.cancelAnimationFrame(snapAnimationFrame)
    snapAnimationFrame = null
  }

  const startTop = window.scrollY || document.documentElement.scrollTop
  const distance = targetTop - startTop

  if (Math.abs(distance) < 2) {
    window.scrollTo({ top: targetTop })
    requestUpdate()
    return
  }

  const startTime = performance.now()

  const step = (now: number) => {
    const elapsed = now - startTime
    const progress = Math.min(elapsed / duration, 1)
    const eased = easeOutCubic(progress)
    const nextTop = startTop + distance * eased

    window.scrollTo({ top: nextTop })
    requestUpdate()

    if (progress < 1) {
      snapAnimationFrame = window.requestAnimationFrame(step)
      return
    }

    snapAnimationFrame = null
  }

  snapAnimationFrame = window.requestAnimationFrame(step)
}

const getNodeStyle = (index: number) => {
  const count = Math.max(1, timeline.value.length)

  // 前两张卡片提前触发，避免“快划走了才完全出现”
  const start = index === 0
    ? -0.18
    : index === 1
      ? -0.02
      : (index - 0.18) / count
  const end = index === count - 1
    ? 1
    : index === 0
      ? 0.1
      : index === 1
        ? 0.24
        : Math.min(0.98, start + 0.22)

  const localProgress = clamp((scrollProgress.value - start) / Math.max(0.001, end - start))
  const easedProgress = easeOutCubic(localProgress)
  const finalY = index % 2 === 0 ? -124 : 124

  let reveal = clamp((0.6 + sectionReveal.value * 0.4) * easedProgress)
  if (index === 0) {
    reveal = Math.max(reveal, clamp(sectionReveal.value * 0.82))
  } else if (index === 1) {
    reveal = Math.max(reveal, clamp(sectionReveal.value * 0.56))
  }

  const slideX = (1 - reveal) * (index <= 1 ? 78 : 148)
  const slideY = finalY + (1 - reveal) * (index % 2 === 0 ? -98 : 98)
  const scale = 0.9 + reveal * 0.1

  return {
    opacity: index <= 1 ? 0.18 + reveal * 0.82 : 0.05 + reveal * 0.95,
    transform: `translate3d(${Math.round(slideX)}px, ${Math.round(slideY)}px, 0) scale(${scale.toFixed(3)})`,
    filter: `blur(${(1 - reveal) * (index <= 1 ? 4 : 9)}px)`,
  }
}

const onWheelSnapToTimeline = (event: WheelEvent) => {
  const sectionEl = timelineSectionRef.value
  if (!sectionEl) return
  if (window.innerWidth <= 860) return
  if (snapLock) return

  const currentY = window.scrollY || document.documentElement.scrollTop
  const sectionTop = sectionEl.offsetTop

  // 在时间轴入口之前，轻微向下滚轮时直接吸附到时间轴起点
  if (event.deltaY > TIMELINE_SNAP_DELTA && currentY < sectionTop - 8) {
    event.preventDefault()
    snapLock = true
    smoothSnapTo(sectionTop, 560)
    if (snapLockTimer != null) window.clearTimeout(snapLockTimer)
    snapLockTimer = window.setTimeout(() => {
      snapLock = false
      snapLockTimer = null
    }, 620)
  }
}

onMounted(async () => {
  await fetchTimeline()
  await nextTick()
  updateMetrics()
  updateProgress()
  window.addEventListener('scroll', requestUpdate, { passive: true })
  window.addEventListener('wheel', onWheelSnapToTimeline, { passive: false })
  window.addEventListener('resize', updateMetrics)
  window.addEventListener('resize', updateProgress)
})

onUnmounted(() => {
  window.removeEventListener('scroll', requestUpdate)
  window.removeEventListener('wheel', onWheelSnapToTimeline)
  window.removeEventListener('resize', updateMetrics)
  window.removeEventListener('resize', updateProgress)
  if (snapLockTimer != null) {
    window.clearTimeout(snapLockTimer)
    snapLockTimer = null
  }
  if (snapAnimationFrame != null) {
    window.cancelAnimationFrame(snapAnimationFrame)
    snapAnimationFrame = null
  }
})
</script>

<style scoped>
.about-site-page {
  min-height: 100vh;
  color: #20343d;
  font-family: 'Noto Serif SC', 'Source Han Serif SC', 'Songti SC', serif;
  background:
    radial-gradient(circle at 12% 8%, rgba(249, 168, 37, 0.28), transparent 26%),
    radial-gradient(circle at 84% 14%, rgba(34, 197, 94, 0.2), transparent 24%),
    radial-gradient(circle at 88% 76%, rgba(59, 130, 246, 0.18), transparent 26%),
    linear-gradient(180deg, #fbf4e6 0%, #edf5ee 40%, #eef4fb 100%);
}

.dark .about-site-page {
  color: #eef6f8;
  background:
    radial-gradient(circle at 12% 8%, rgba(245, 158, 11, 0.16), transparent 24%),
    radial-gradient(circle at 84% 14%, rgba(20, 184, 166, 0.14), transparent 24%),
    radial-gradient(circle at 88% 76%, rgba(96, 165, 250, 0.12), transparent 26%),
    linear-gradient(180deg, #071118 0%, #0b1c22 42%, #111829 100%);
}

.about-hero {
  position: relative;
  overflow: hidden;
  padding: 112px 24px 36px;
}

.about-hero::before,
.about-hero::after {
  content: '';
  position: absolute;
  border-radius: 9999px;
  pointer-events: none;
}

.about-hero::before {
  width: 540px;
  height: 540px;
  left: -140px;
  top: -220px;
  background: radial-gradient(circle, rgba(255, 183, 77, 0.26), transparent 65%);
}

.about-hero::after {
  width: 460px;
  height: 460px;
  right: -120px;
  top: -120px;
  background: radial-gradient(circle, rgba(45, 212, 191, 0.22), transparent 65%);
}

.hero-shell {
  position: relative;
  z-index: 1;
  max-width: 1280px;
  margin: 0 auto;
  display: flex;
  align-items: end;
  justify-content: space-between;
  gap: 32px;
}

.hero-copy {
  max-width: 760px;
}

.hero-toolbar {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  gap: 18px;
  margin-bottom: 16px;
}

.hero-kicker {
  margin: 0;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.28em;
  text-transform: uppercase;
  color: rgba(32, 52, 61, 0.58);
}

.dark .hero-kicker {
  color: rgba(226, 232, 240, 0.64);
}

.theme-switcher {
  position: relative;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  align-items: center;
  gap: 6px;
  padding: 6px;
  border-radius: 999px;
  border: 1px solid rgba(32, 52, 61, 0.08);
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.74), rgba(246, 250, 252, 0.56));
  backdrop-filter: blur(18px);
  box-shadow: 0 18px 44px rgba(15, 23, 42, 0.1), inset 0 1px 0 rgba(255, 255, 255, 0.58);
  overflow: hidden;
}

.theme-switcher::before {
  content: '';
  position: absolute;
  top: 6px;
  left: 6px;
  width: calc(50% - 3px);
  height: calc(100% - 12px);
  border-radius: 999px;
  background: linear-gradient(135deg, rgba(217, 119, 6, 0.92), rgba(13, 148, 136, 0.88));
  box-shadow: 0 12px 28px rgba(13, 148, 136, 0.22);
  transition: transform 0.38s cubic-bezier(0.22, 1, 0.36, 1), background 0.32s ease, box-shadow 0.32s ease;
}

.theme-switcher::after {
  content: '';
  position: absolute;
  top: -24%;
  left: -10%;
  width: 42%;
  height: 148%;
  border-radius: 999px;
  background: linear-gradient(90deg, rgba(255, 255, 255, 0), rgba(255, 255, 255, 0.38), rgba(255, 255, 255, 0));
  opacity: 0.7;
  mix-blend-mode: screen;
  transform: translateX(0%);
  transition: transform 0.38s cubic-bezier(0.22, 1, 0.36, 1), opacity 0.32s ease;
}

.theme-switcher--dark::before {
  transform: translateX(100%);
  background: linear-gradient(135deg, rgba(251, 191, 36, 0.88), rgba(59, 130, 246, 0.86));
  box-shadow: 0 14px 30px rgba(59, 130, 246, 0.22);
}

.theme-switcher--dark::after {
  transform: translateX(114%);
}

.dark .theme-switcher {
  border-color: rgba(148, 163, 184, 0.12);
  background: linear-gradient(180deg, rgba(9, 22, 34, 0.78), rgba(9, 22, 34, 0.56));
  box-shadow: 0 20px 46px rgba(2, 6, 23, 0.3), inset 0 1px 0 rgba(255, 255, 255, 0.04);
}

.theme-switcher--floating {
  position: absolute;
  right: 24px;
  top: 24px;
  z-index: 6;
}

.theme-chip {
  appearance: none;
  border: 0;
  border-radius: 999px;
  position: relative;
  z-index: 1;
  min-width: 104px;
  padding: 10px 15px;
  background: transparent;
  color: rgba(32, 52, 61, 0.76);
  font-size: 14px;
  font-weight: 700;
  letter-spacing: 0.04em;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: color 0.25s ease, transform 0.25s ease, opacity 0.25s ease;
}

.theme-chip-icon {
  font-size: 15px;
  line-height: 1;
  opacity: 0.82;
  transition: transform 0.28s ease, opacity 0.28s ease;
}

.theme-chip:hover {
  transform: translateY(-1px);
}

.theme-switcher:hover::after {
  opacity: 0.92;
}

.theme-chip:hover .theme-chip-icon {
  transform: scale(1.08);
  opacity: 1;
}

.theme-chip:active {
  transform: translateY(0) scale(0.985);
}

.theme-chip--active {
  color: #fffdf8;
  text-shadow: 0 1px 10px rgba(255, 255, 255, 0.14);
}

.dark .theme-chip {
  color: rgba(226, 232, 240, 0.8);
}

.dark .theme-chip--active {
  color: #08111d;
}

.hero-title {
  margin: 0;
  font-size: clamp(52px, 11vw, 118px);
  line-height: 0.92;
  letter-spacing: -0.03em;
  font-weight: 900;
}

.hero-title--calligraphy {
  font-family: 'Ma Shan Zheng', 'STXingkai', 'FZShuTi', 'HanziPen SC', 'KaiTi', 'Kaiti SC', 'STKaiti', cursive;
  letter-spacing: 0.06em;
  font-weight: 600;
  background: linear-gradient(120deg, #b45309 0%, #0f766e 46%, #2563eb 100%);
  background-clip: text;
  -webkit-background-clip: text;
  color: transparent;
  text-shadow: 0 18px 40px rgba(180, 83, 9, 0.08);
}

.dark .hero-title--calligraphy {
  background: linear-gradient(120deg, #fbbf24 0%, #5eead4 46%, #93c5fd 100%);
  background-clip: text;
  -webkit-background-clip: text;
  color: transparent;
  text-shadow: 0 18px 40px rgba(14, 165, 233, 0.1);
}

.text-gradient {
  background: linear-gradient(120deg, #9a3412 0%, #0f766e 48%, #1d4ed8 100%);
  background-clip: text;
  -webkit-background-clip: text;
  color: transparent;
}

.dark .text-gradient {
  background: linear-gradient(120deg, #fbbf24 0%, #2dd4bf 48%, #93c5fd 100%);
  background-clip: text;
  -webkit-background-clip: text;
  color: transparent;
}

.hero-desc {
  margin: 22px 0 0;
  max-width: 640px;
  font-size: clamp(17px, 2.1vw, 22px);
  line-height: 1.85;
  color: rgba(32, 52, 61, 0.76);
  text-wrap: pretty;
}

.dark .hero-desc {
  color: rgba(226, 232, 240, 0.82);
}

.hero-aside {
  display: grid;
  gap: 14px;
  min-width: 220px;
}

.hero-badge {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 18px 20px;
  border-radius: 24px;
  border: 1px solid rgba(32, 52, 61, 0.08);
  background: linear-gradient(180deg, rgba(255, 252, 244, 0.7), rgba(241, 249, 246, 0.58));
  backdrop-filter: blur(14px);
  box-shadow: 0 18px 50px rgba(30, 41, 59, 0.08);
}

.dark .hero-badge {
  border-color: rgba(148, 163, 184, 0.12);
  background: rgba(10, 26, 38, 0.54);
  box-shadow: 0 22px 54px rgba(2, 6, 23, 0.32);
}

.hero-badge--soft {
  background: linear-gradient(180deg, rgba(240, 248, 255, 0.68), rgba(236, 246, 255, 0.58));
}

.dark .hero-badge--soft {
  background: rgba(13, 35, 43, 0.62);
}

.badge-label {
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.18em;
  color: rgba(24, 49, 58, 0.55);
}

.dark .badge-label {
  color: rgba(226, 232, 240, 0.6);
}

.badge-value {
  font-size: 22px;
  line-height: 1.2;
  font-family: 'Noto Serif SC', 'KaiTi', 'Kaiti SC', 'STKaiti', serif;
}

.timeline-scroll-section {
  position: relative;
  height: 286vh;
}

.timeline-viewport {
  position: sticky;
  top: 0;
  height: 100vh;
  overflow: hidden;
  padding: 32px 0 82px;
}

.timeline-stage {
  position: relative;
  z-index: 1;
  max-width: 1320px;
  height: calc(100vh - 114px);
  min-height: 640px;
  margin: 0 auto;
  padding: 0 32px;
}

.timeline-orb {
  position: absolute;
  border-radius: 9999px;
  filter: blur(14px);
  pointer-events: none;
}

.timeline-orb--left {
  width: 460px;
  height: 460px;
  left: -90px;
  top: 90px;
  background: radial-gradient(circle, rgba(251, 191, 36, 0.3), transparent 68%);
}

.timeline-orb--right {
  width: 420px;
  height: 420px;
  right: -70px;
  bottom: 70px;
  background: radial-gradient(circle, rgba(59, 130, 246, 0.22), transparent 70%);
}

.timeline-heading {
  display: flex;
  align-items: end;
  justify-content: space-between;
  gap: 24px;
  margin-bottom: 10px;
}

.timeline-heading-kicker {
  margin: 0;
  font-size: 12px;
  letter-spacing: 0.24em;
  text-transform: uppercase;
  color: rgba(32, 52, 61, 0.54);
}

.dark .timeline-heading-kicker {
  color: rgba(226, 232, 240, 0.58);
}

.timeline-heading-title {
  margin: 0;
  font-size: clamp(28px, 3.5vw, 54px);
  line-height: 1.04;
  font-weight: 800;
}

.timeline-line {
  position: absolute;
  left: 32px;
  right: 32px;
  top: 54%;
  height: 3px;
  z-index: 0;
  transform: translateY(-50%);
  background: linear-gradient(90deg, rgba(251, 191, 36, 0.22), rgba(13, 148, 136, 0.88), rgba(37, 99, 235, 0.76));
  box-shadow: 0 0 30px rgba(13, 148, 136, 0.12);
}

.timeline-track {
  display: inline-flex;
  gap: 56px;
  height: 100%;
  align-items: center;
  transition: none;
  will-change: transform;
  padding: 0 220px 0 24px;
}

.timeline-node {
  position: relative;
  z-index: 1;
  width: min(76vw, 470px);
  min-width: 390px;
  min-height: 296px;
  border-radius: 34px;
  border: 1px solid rgba(32, 52, 61, 0.08);
  background: linear-gradient(180deg, rgba(255, 252, 244, 0.76), rgba(240, 249, 246, 0.62));
  backdrop-filter: blur(18px);
  padding: 32px 34px 28px;
  box-shadow: 0 30px 80px rgba(15, 23, 42, 0.12);
}

.dark .timeline-node {
  background: rgba(9, 24, 35, 0.72);
  border-color: rgba(148, 163, 184, 0.14);
  box-shadow: 0 28px 74px rgba(2, 6, 23, 0.44);
}

.node-dot {
  position: absolute;
  width: 18px;
  height: 18px;
  border-radius: 9999px;
  background: linear-gradient(135deg, #b45309, #0f766e);
  left: -10px;
  top: 50%;
  transform: translateY(-50%);
  box-shadow: 0 0 0 9px rgba(13, 148, 136, 0.16);
}

.node-date {
  display: inline-block;
  font-size: 14px;
  font-weight: 700;
  letter-spacing: 0.12em;
  color: #0f766e;
  text-transform: uppercase;
  margin-bottom: 8px;
}

.node-title {
  margin: 12px 0 0;
  font-size: 32px;
  line-height: 1.24;
  font-weight: 700;
}

.node-title--ink {
  font-family: 'Noto Serif SC', 'STKaiti', 'KaiTi', 'Kaiti SC', serif;
  letter-spacing: 0.04em;
}

.node-desc {
  margin-top: 18px;
  font-size: 17px;
  line-height: 2;
  color: rgba(32, 52, 61, 0.78);
}

.dark .node-desc {
  color: rgba(226, 232, 240, 0.78);
}

.about-hero :deep(.back-home-button) {
  position: absolute;
  left: 24px;
  top: 24px;
  z-index: 5;
}

.about-hero :deep(.back-btn) {
  width: 176px;
  border-radius: 18px;
  border-color: rgba(32, 52, 61, 0.08);
  background: rgba(255, 252, 244, 0.66);
  backdrop-filter: blur(14px);
  box-shadow: 0 16px 40px rgba(15, 23, 42, 0.1);
}

.dark .about-hero :deep(.back-btn) {
  border-color: rgba(148, 163, 184, 0.14);
  background: rgba(8, 24, 34, 0.62);
  color: #f8fafc;
}

.about-hero :deep(.back-fill) {
  background: linear-gradient(90deg, rgba(180, 83, 9, 0.76) 0%, rgba(13, 148, 136, 0.78) 52%, rgba(37, 99, 235, 0.78) 100%);
}

@media (max-width: 860px) {
  .about-hero {
    padding: 104px 16px 24px;
  }

  .hero-shell {
    display: block;
  }

  .hero-toolbar {
    display: block;
    margin-bottom: 14px;
  }

  .theme-switcher--floating {
    right: 16px;
    top: 18px;
    transform: scale(0.95);
    transform-origin: top right;
  }

  .hero-title {
    font-size: clamp(40px, 15vw, 68px);
  }

  .hero-desc {
    font-size: 16px;
    line-height: 1.8;
  }

  .hero-aside {
    margin-top: 20px;
    grid-template-columns: 1fr 1fr;
  }

  .timeline-scroll-section {
    height: auto;
    padding: 14px 0 56px;
  }

  .timeline-viewport {
    position: static;
    height: auto;
    padding: 18px 0 12px;
  }

  .timeline-stage {
    height: auto;
    min-height: 0;
    padding: 0 16px;
  }

  .timeline-heading {
    display: block;
    margin-bottom: 18px;
  }

  .timeline-heading-title {
    margin-top: 8px;
    font-size: 32px;
  }

  .timeline-line {
    display: none;
  }

  .timeline-orb {
    display: none;
  }

  .timeline-track {
    display: grid;
    gap: 18px;
    padding-right: 0;
    transform: none !important;
  }

  .timeline-node {
    width: 100%;
    min-width: 0;
    min-height: 0;
    padding: 24px 22px 22px;
    opacity: 1 !important;
    filter: none !important;
    transform: none !important;
  }

  .node-dot {
    display: none;
  }

  .node-title {
    font-size: 24px;
  }

  .node-desc {
    font-size: 15px;
    line-height: 1.8;
  }

  .about-hero :deep(.back-home-button) {
    left: 16px;
    top: 18px;
  }

  .about-hero :deep(.back-btn) {
    width: 152px;
    height: 50px;
    font-size: 16px;
  }
}

@media (max-width: 640px) {
  .theme-switcher--floating {
    top: 76px;
  }

  .theme-chip {
    min-width: 88px;
    padding: 9px 12px;
    font-size: 13px;
  }

  .hero-aside {
    grid-template-columns: 1fr;
  }
}
</style>
