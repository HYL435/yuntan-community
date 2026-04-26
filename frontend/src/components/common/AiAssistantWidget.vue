<script setup lang="ts">
import { computed, nextTick, onMounted, onUnmounted, ref } from 'vue'
import MarkdownIt from 'markdown-it'
import { useRoute } from 'vue-router'

type Role = 'user' | 'ai'

type ChatMessage = {
  id: number
  role: Role
  text: string
  streaming?: boolean
}

type ModelOption = {
  label: string
  value: string
  description: string
  provider: string
  modelId: string
}

type AiStreamEvent = {
  sessionId?: string | number
  type: string
  content?: string
  delta?: string
  text?: string
  model?: string
  provider?: string
  finishReason?: string
  code?: string
  message?: string
}

type ParsedSseEvent = {
  event?: string
  data: string
}

const route = useRoute()

const BALL_SIZE = 72
const MOBILE_BREAKPOINT = 768
const PANEL_TOP_GAP = 20

const isOpen = ref(false)
const isHovering = ref(false)
const isThinking = ref(false)
const isStreaming = ref(false)
const isFullscreen = ref(false)
const isModelDropdownOpen = ref(false)
const modelPickerRef = ref<HTMLElement | null>(null)
const dropdownStyle = ref<Record<string, string>>({})
const inputText = ref('')
const listRef = ref<HTMLElement | null>(null)
const rootRef = ref<HTMLElement | null>(null)
const sessionId = ref<string | null>(null)
let abortController: AbortController | null = null
let streamFlushTimer: number | null = null
const activeStreamMessageId = ref<number | null>(null)
const pendingStreamText = ref('')

const viewportW = ref(0)
const viewportH = ref(0)
const portalOffset = ref(0)
const selectedModel = ref('mock-model-v1')

const messages = ref<ChatMessage[]>([
  {
    id: 1,
    role: 'ai',
    text: '你好，我是 AI 助手。选择模型后直接发问即可。',
  },
])

const markdown = new MarkdownIt({
  html: true,
  linkify: true,
  typographer: true,
  breaks: true,
})

const modelOptions: ModelOption[] = [
  {
    label: 'Mock Model v1 · 测试',
    value: 'mock-model-v1',
    description: '测试模型，用于开发阶段功能验证。',
    provider: 'mock',
    modelId: 'mock-model-v1',
  },
  {
    label: 'DeepSeek V4 Flash',
    value: 'deepseek-v4-flash',
    description: 'DeepSeek 快速模型，适合日常问答与低延迟场景。',
    provider: 'deepseek',
    modelId: 'deepseek-v4-flash',
  },
  {
    label: 'Qwen Plus',
    value: 'qwen-plus',
    description: '通义千问增强模型，适合通用对话和复杂任务。',
    provider: 'qwen',
    modelId: 'qwen-plus',
  },
]

const showWidget = computed(() => !route.path.startsWith('/admin'))
const showTooltip = computed(() => !isOpen.value && isHovering.value)
const isMobile = computed(() => viewportW.value <= MOBILE_BREAKPOINT)
const currentModelLabel = computed(() => {
  return modelOptions.find((model) => model.value === selectedModel.value)?.label ?? 'Mock Model v1 · 测试'
})

const collapsedBottom = computed(() => 24 + portalOffset.value)
const expandedBottom = computed(() => (isMobile.value ? 88 : 92) + portalOffset.value)

const expandedWidth = computed(() => {
  if (isFullscreen.value) return Math.min(1240, viewportW.value - 32)
  if (isMobile.value) return Math.max(280, viewportW.value - 24)
  return Math.min(460, viewportW.value - 32)
})

const expandedHeight = computed(() => {
  if (isFullscreen.value) return Math.min(900, viewportH.value - 32)
  const maxHeight = viewportH.value - expandedBottom.value - PANEL_TOP_GAP
  if (isMobile.value) return Math.max(360, maxHeight)
  return Math.max(420, Math.min(720, maxHeight))
})

const shellStyle = computed(() => ({
  width: `${isOpen.value ? expandedWidth.value : BALL_SIZE}px`,
  height: `${isOpen.value ? expandedHeight.value : BALL_SIZE}px`,
  right: `${isFullscreen.value ? 16 : isMobile.value ? 12 : 20}px`,
  bottom: `${isOpen.value ? (isFullscreen.value ? 16 : expandedBottom.value) : collapsedBottom.value}px`,
  borderRadius: isOpen.value ? `${isFullscreen.value ? 34 : isMobile.value ? 24 : 30}px` : '999px',
}))

const tooltipStyle = computed(() => ({
  right: `${(isMobile.value ? 12 : 20) + BALL_SIZE + 16}px`,
  bottom: `${collapsedBottom.value + BALL_SIZE / 2}px`,
  transform: 'translateY(50%)',
}))

const syncViewport = () => {
  viewportW.value = window.innerWidth
  viewportH.value = window.innerHeight
}

const syncPortalOffset = () => {
  const portalButton = document.querySelector<HTMLElement>('.portal-button')
  if (!portalButton) {
    portalOffset.value = 0
    return
  }
  portalOffset.value = Math.round(portalButton.getBoundingClientRect().height + 18)
}

let portalObserver: MutationObserver | null = null

const scrollToBottom = async () => {
  await nextTick()
  if (listRef.value) {
    listRef.value.scrollTop = listRef.value.scrollHeight
  }
}

const normalizeMarkdownText = (content: string) => {
  let normalized = content
  normalized = normalized.replace(/([^\n])\n(#+\s)/g, '$1\n\n$2')
  normalized = normalized.replace(/([^\n])\n(\d+\.\s)/g, '$1\n\n$2')
  normalized = normalized.replace(/([^\n])\n(-\s)/g, '$1\n\n$2')
  return normalized
}

const renderMarkdown = (content: string) => {
  if (!content) return ''
  return markdown.render(normalizeMarkdownText(content))
}

const stopTypewriter = () => {
  if (streamFlushTimer != null) {
    window.clearInterval(streamFlushTimer)
    streamFlushTimer = null
  }
}

const flushTypewriterChunk = () => {
  if (!pendingStreamText.value || activeStreamMessageId.value == null) {
    stopTypewriter()
    return
  }

  const msg = messages.value.find((item) => item.id === activeStreamMessageId.value)
  if (!msg) {
    pendingStreamText.value = ''
    stopTypewriter()
    return
  }

  const takeCount = pendingStreamText.value.startsWith('```') ? 3 : 2
  const nextPiece = pendingStreamText.value.slice(0, takeCount)
  pendingStreamText.value = pendingStreamText.value.slice(takeCount)
  msg.text += nextPiece
  void scrollToBottom()

  if (!pendingStreamText.value) {
    stopTypewriter()
  }
}

const enqueueTypewriterText = (messageId: number, content: string) => {
  activeStreamMessageId.value = messageId
  pendingStreamText.value += content
  if (streamFlushTimer == null) {
    streamFlushTimer = window.setInterval(flushTypewriterChunk, 18)
  }
}

const flushAllPendingText = () => {
  if (activeStreamMessageId.value == null || !pendingStreamText.value) {
    stopTypewriter()
    return
  }

  const msg = messages.value.find((item) => item.id === activeStreamMessageId.value)
  if (msg) {
    msg.text += pendingStreamText.value
  }
  pendingStreamText.value = ''
  stopTypewriter()
}

const normalizeStreamEvent = (event: AiStreamEvent): AiStreamEvent => {
  if (event.type === 'delta') return { ...event, type: 'stream' }
  if (event.type === 'chunk') return { ...event, type: 'stream' }
  if (event.type === 'done') return { ...event, type: 'end' }
  return event
}

const coerceAiStreamEvent = (raw: unknown, eventName?: string): AiStreamEvent | null => {
  if (typeof raw === 'string') {
    // 某些后端直接推纯文本 token，这里按 stream 处理
    const text = raw.trim()
    if (!text || text === '[DONE]') return null
    return { type: eventName || 'stream', content: raw }
  }

  if (!raw || typeof raw !== 'object') return null
  const payload = raw as Record<string, unknown>

  // 兼容 { data: { ...AiStreamEvent } } 包装
  const nested = payload.data
  if (nested && typeof nested === 'object') {
    const nestedPayload = nested as Record<string, unknown>
    if (typeof nestedPayload.type === 'string') {
      return normalizeStreamEvent(nestedPayload as unknown as AiStreamEvent)
    }
  }

  if (typeof payload.type === 'string') {
    return normalizeStreamEvent(payload as unknown as AiStreamEvent)
  }

  // 兼容 event 名称携带类型，data 仅给文本
  if (eventName && (typeof payload.content === 'string' || typeof payload.text === 'string')) {
    return {
      type: eventName,
      content: (payload.content as string | undefined) || (payload.text as string | undefined),
      message: payload.message as string | undefined,
    }
  }

  return null
}

const splitSseBlocks = (rawChunk: string) => {
  const normalized = rawChunk.replace(/\r\n/g, '\n')
  const parts = normalized.split('\n\n')
  return {
    blocks: parts.slice(0, -1),
    rest: parts.length > 0 ? parts[parts.length - 1] : '',
  }
}

const parseSseBlock = (block: string): ParsedSseEvent | null => {
  const lines = block.split('\n')
  const dataLines: string[] = []
  let eventName: string | undefined

  for (const line of lines) {
    if (!line || line.startsWith(':')) continue
    if (line.startsWith('event:')) {
      eventName = line.slice(6).trim()
      continue
    }
    if (line.startsWith('data:')) {
      dataLines.push(line.slice(5).trim())
    }
  }

  if (dataLines.length === 0) return null
  return { event: eventName, data: dataLines.join('\n') }
}

const consumeSseBlock = (block: string, aiMsgId: number) => {
  const parsed = parseSseBlock(block)
  if (!parsed) return
  if (!parsed.data || parsed.data === '[DONE]') return

  try {
    const decoded = JSON.parse(parsed.data)
    const event = coerceAiStreamEvent(decoded, parsed.event)
    if (!event) return
    if (!event.type && parsed.event) event.type = parsed.event
    handleStreamEvent(event, aiMsgId)
  } catch {
    const fallback = coerceAiStreamEvent(parsed.data, parsed.event)
    if (!fallback) return
    handleStreamEvent(fallback, aiMsgId)
  }
}

const handleStreamEvent = (event: AiStreamEvent, aiMsgId: number) => {
  const msg = messages.value.find((m) => m.id === aiMsgId)
  switch (event.type) {
    case 'session':
      if (event.sessionId != null) sessionId.value = String(event.sessionId)
      break
    case 'start':
      if (event.sessionId != null) sessionId.value = String(event.sessionId)
      isThinking.value = false
      if (msg) msg.streaming = true
      break
    case 'stream':
      if (msg) {
        const piece = event.content ?? event.delta ?? event.text ?? ''
        if (!piece) break
        enqueueTypewriterText(aiMsgId, piece)
      }
      break
    case 'end':
      flushAllPendingText()
      if (msg) {
        msg.streaming = false
        if (!msg.text) msg.text = '（已完成，无返回内容）'
      }
      break
    case 'error':
      flushAllPendingText()
      if (msg) {
        msg.streaming = false
        msg.text = `错误：${event.message ?? '服务异常，请稍后重试。'}`
      }
      break
  }
}

const openPanel = async () => {
  isOpen.value = true
  await scrollToBottom()
}

const closePanel = () => {
  isOpen.value = false
  isFullscreen.value = false
}

const togglePanel = () => {
  if (isOpen.value) {
    closePanel()
  } else {
    void openPanel()
  }
}

const toggleFullscreen = () => {
  isFullscreen.value = !isFullscreen.value
}

const sendMessage = async () => {
  const content = inputText.value.trim()
  if (!content || isStreaming.value) return

  const token = localStorage.getItem('auth_token')
  if (!token) {
    messages.value.push({ id: Date.now(), role: 'ai', text: '请先登录后再使用 AI 功能。' })
    void scrollToBottom()
    return
  }

  messages.value.push({ id: Date.now(), role: 'user', text: content })
  inputText.value = ''
  await scrollToBottom()

  const aiMsgId = Date.now() + 1
  messages.value.push({ id: aiMsgId, role: 'ai', text: '', streaming: false })
  isThinking.value = true
  isStreaming.value = true
  await scrollToBottom()

  const currentModel = modelOptions.find((m) => m.value === selectedModel.value)
  abortController = new AbortController()

  try {
    const res = await fetch('/api/front/ai/chat/stream', {
      method: 'POST',
      headers: {
        Accept: 'text/event-stream',
        'Content-Type': 'application/json',
        'Cache-Control': 'no-cache',
        ...(token ? { Authorization: token } : {}),
      },
      body: JSON.stringify({
        sessionId: sessionId.value || undefined,
        provider: currentModel?.provider,
        model: currentModel?.modelId,
        content,
        requestId: crypto.randomUUID(),
      }),
      signal: abortController.signal,
    })

    if (!res.ok || !res.body) throw new Error(`HTTP ${res.status}`)
    const contentType = res.headers.get('content-type') || ''
    if (!contentType.includes('text/event-stream')) {
      throw new Error(`响应类型异常: ${contentType || 'unknown'}`)
    }

    const reader = res.body.getReader()
    const decoder = new TextDecoder()
    let buffer = ''

    while (true) {
      const { done, value } = await reader.read()
      if (done) {
        buffer += decoder.decode()
        break
      }
      buffer += decoder.decode(value, { stream: true })
      const { blocks: eventBlocks, rest } = splitSseBlocks(buffer)
      buffer = rest

      for (const block of eventBlocks) {
        consumeSseBlock(block, aiMsgId)
      }
    }

    if (buffer.trim()) {
      consumeSseBlock(buffer.trim(), aiMsgId)
    }

    flushAllPendingText()

    const msg = messages.value.find((m) => m.id === aiMsgId)
    if (msg && !msg.text) {
      msg.text = '已完成，但未收到可展示的回复内容。'
    }
  } catch (err: unknown) {
    if (err instanceof Error && err.name !== 'AbortError') {
      flushAllPendingText()
      console.error('[AI] 流式请求异常:', err)
      const msg = messages.value.find((m) => m.id === aiMsgId)
      if (msg) {
        msg.streaming = false
        msg.text = err.message.includes('network error')
          ? '流连接被中断（ERR_INCOMPLETE_CHUNKED_ENCODING）。通常是网关/代理提前断开了 SSE 连接。'
          : '请求失败，请稍后重试。'
      }
    }
  } finally {
    flushAllPendingText()
    const msg = messages.value.find((m) => m.id === aiMsgId)
    if (msg) msg.streaming = false
    isThinking.value = false
    isStreaming.value = false
    activeStreamMessageId.value = null
    abortController = null
    await scrollToBottom()
  }
}

const newConversation = () => {
  abortController?.abort()
  abortController = null
  flushAllPendingText()
  activeStreamMessageId.value = null
  sessionId.value = null
  isStreaming.value = false
  isThinking.value = false
  messages.value = [{ id: Date.now(), role: 'ai', text: '新对话已开始，请提问。' }]
  inputText.value = ''
  void scrollToBottom()
}

const toggleModelDropdown = () => {
  if (!isModelDropdownOpen.value && modelPickerRef.value) {
    const rect = modelPickerRef.value.getBoundingClientRect()
    dropdownStyle.value = {
      top: `${rect.bottom + 8}px`,
      right: `${window.innerWidth - rect.right}px`,
    }
  }
  isModelDropdownOpen.value = !isModelDropdownOpen.value
}

const selectModel = (value: string) => {
  selectedModel.value = value
  isModelDropdownOpen.value = false
}

const onWindowPointerDown = (event: PointerEvent) => {
  const target = event.target
  const targetElement = target instanceof Element ? target : null
  const clickedModelPicker = !!(targetElement && modelPickerRef.value?.contains(targetElement))
  const clickedModelDropdown = !!targetElement?.closest('.ai-model-dropdown')

  if (clickedModelPicker || clickedModelDropdown) {
    return
  }

  if (isModelDropdownOpen.value) {
    isModelDropdownOpen.value = false
  }
  if (!isOpen.value) return
  if (target instanceof Node && rootRef.value?.contains(target)) return
  closePanel()
}

const onWindowKeydown = (event: KeyboardEvent) => {
  if (event.key === 'Escape') {
    if (isFullscreen.value) {
      isFullscreen.value = false
      return
    }
    closePanel()
  }
}

onMounted(() => {
  syncViewport()
  syncPortalOffset()
  window.addEventListener('resize', syncViewport)
  window.addEventListener('resize', syncPortalOffset)
  window.addEventListener('pointerdown', onWindowPointerDown)
  window.addEventListener('keydown', onWindowKeydown)
  portalObserver = new MutationObserver(syncPortalOffset)
  portalObserver.observe(document.body, { childList: true, subtree: true, attributes: true })
})

onUnmounted(() => {
  abortController?.abort()
  abortController = null
  stopTypewriter()
  window.removeEventListener('resize', syncViewport)
  window.removeEventListener('resize', syncPortalOffset)
  window.removeEventListener('pointerdown', onWindowPointerDown)
  window.removeEventListener('keydown', onWindowKeydown)
  portalObserver?.disconnect()
  portalObserver = null
})
</script>

<template>
  <div v-if="showWidget" ref="rootRef" class="ai-widget">
    <div
      class="ai-shell"
      :class="{
        'ai-shell--open': isOpen,
        'ai-shell--fullscreen': isFullscreen,
      }"
      :style="shellStyle"
      @wheel.stop
    >
      <div class="ai-shell__surface">
        <div class="ai-shell__bg" aria-hidden="true" />
        <div class="ai-shell__glow ai-shell__glow--one" aria-hidden="true" />
        <div class="ai-shell__glow ai-shell__glow--two" aria-hidden="true" />

        <button
          class="ai-ball"
          type="button"
          :aria-expanded="isOpen"
          aria-label="打开 AI 聊天"
          @mouseenter="isHovering = true"
          @mouseleave="isHovering = false"
          @click="togglePanel"
        >
          <!-- 声纳外环 -->
          <span class="ai-ball__sonar ai-ball__sonar--one" aria-hidden="true" />
          <span class="ai-ball__sonar ai-ball__sonar--two" aria-hidden="true" />
          <span class="ai-ball__orbit ai-ball__orbit--one" aria-hidden="true" />
          <span class="ai-ball__orbit ai-ball__orbit--two" aria-hidden="true" />
          <span class="ai-ball__halo" aria-hidden="true" />
          <span class="ai-ball__specular" aria-hidden="true" />
          <span class="ai-ball__core-glow" aria-hidden="true" />

          <!-- 3D 线框球 SVG -->
          <svg class="ai-globe" viewBox="0 0 72 72" fill="none" xmlns="http://www.w3.org/2000/svg" aria-hidden="true">
            <!-- 外圆 -->
            <circle cx="36" cy="36" r="30" stroke="url(#globeRim)" stroke-width="1.2"/>
            <!-- 赤道环 -->
            <ellipse cx="36" cy="36" rx="30" ry="8.5" stroke="url(#equator)" stroke-width="1"/>
            <!-- 纬线1 -->
            <ellipse cx="36" cy="24" rx="22" ry="6" stroke="rgba(216,180,254,0.38)" stroke-width="0.9"/>
            <!-- 纬线2 -->
            <ellipse cx="36" cy="48" rx="22" ry="6" stroke="rgba(216,180,254,0.38)" stroke-width="0.9"/>
            <!-- 竖向经线 -->
            <ellipse cx="36" cy="36" rx="8" ry="30" stroke="url(#meridian1)" stroke-width="1"/>
            <ellipse cx="36" cy="36" rx="20" ry="30" stroke="rgba(192,132,252,0.22)" stroke-width="0.8"/>
            <ellipse cx="36" cy="36" rx="27" ry="17" stroke="rgba(243,232,255,0.18)" stroke-width="0.8" transform="rotate(-24 36 36)"/>
            <ellipse cx="36" cy="36" rx="27" ry="17" stroke="rgba(168,85,247,0.16)" stroke-width="0.8" transform="rotate(24 36 36)"/>
            <circle cx="36" cy="36" r="12" stroke="rgba(243,232,255,0.16)" stroke-width="0.8" stroke-dasharray="2.2 3.2"/>
            <!-- 波浪线 A -->
            <path class="globe-wave globe-wave--a" d="M 6 36 Q 20 22 36 36 Q 52 50 66 36" stroke="url(#waveA)" stroke-width="1.3" stroke-linecap="round"/>
            <!-- 波浪线 B -->
            <path class="globe-wave globe-wave--b" d="M 6 36 Q 20 50 36 36 Q 52 22 66 36" stroke="url(#waveB)" stroke-width="1" stroke-linecap="round" opacity="0.65"/>
            <path d="M 14 26 Q 36 10 58 26" stroke="rgba(243,232,255,0.18)" stroke-width="0.9" stroke-linecap="round"/>
            <path d="M 14 46 Q 36 62 58 46" stroke="rgba(192,132,252,0.18)" stroke-width="0.9" stroke-linecap="round"/>
            <!-- 极光点 -->
            <circle cx="36" cy="6" r="2.2" fill="rgba(233,213,255,0.8)"/>
            <circle cx="36" cy="66" r="2.2" fill="rgba(192,132,252,0.6)"/>
            <circle cx="36" cy="36" r="4.8" fill="url(#energyCore)" opacity="0.9"/>
            <ellipse class="globe-scan" cx="36" cy="36" rx="25" ry="10" stroke="url(#scanRing)" stroke-width="1" stroke-linecap="round"/>
            <!-- 渐变定义 -->
            <defs>
              <linearGradient id="globeRim" x1="6" y1="6" x2="66" y2="66" gradientUnits="userSpaceOnUse">
                <stop offset="0%" stop-color="#e9d5ff" stop-opacity="0.9"/>
                <stop offset="50%" stop-color="#a855f7" stop-opacity="0.7"/>
                <stop offset="100%" stop-color="#6d28d9" stop-opacity="0.5"/>
              </linearGradient>
              <linearGradient id="equator" x1="6" y1="36" x2="66" y2="36" gradientUnits="userSpaceOnUse">
                <stop offset="0%" stop-color="#c084fc" stop-opacity="0.2"/>
                <stop offset="50%" stop-color="#e9d5ff" stop-opacity="0.9"/>
                <stop offset="100%" stop-color="#c084fc" stop-opacity="0.2"/>
              </linearGradient>
              <linearGradient id="meridian1" x1="36" y1="6" x2="36" y2="66" gradientUnits="userSpaceOnUse">
                <stop offset="0%" stop-color="#f3e8ff" stop-opacity="0.7"/>
                <stop offset="50%" stop-color="#a855f7" stop-opacity="0.5"/>
                <stop offset="100%" stop-color="#6d28d9" stop-opacity="0.3"/>
              </linearGradient>
              <linearGradient id="waveA" x1="6" y1="36" x2="66" y2="36" gradientUnits="userSpaceOnUse">
                <stop offset="0%" stop-color="#e9d5ff" stop-opacity="0.3"/>
                <stop offset="40%" stop-color="#d8b4fe" stop-opacity="0.95"/>
                <stop offset="100%" stop-color="#a855f7" stop-opacity="0.4"/>
              </linearGradient>
              <linearGradient id="waveB" x1="6" y1="36" x2="66" y2="36" gradientUnits="userSpaceOnUse">
                <stop offset="0%" stop-color="#c084fc" stop-opacity="0.2"/>
                <stop offset="50%" stop-color="#f3e8ff" stop-opacity="0.7"/>
                <stop offset="100%" stop-color="#c084fc" stop-opacity="0.2"/>
              </linearGradient>
              <radialGradient id="energyCore" cx="0" cy="0" r="1" gradientUnits="userSpaceOnUse" gradientTransform="translate(36 36) rotate(90) scale(6)">
                <stop offset="0%" stop-color="#ffffff" stop-opacity="0.95"/>
                <stop offset="45%" stop-color="#e9d5ff" stop-opacity="0.8"/>
                <stop offset="100%" stop-color="#a855f7" stop-opacity="0"/>
              </radialGradient>
              <linearGradient id="scanRing" x1="11" y1="36" x2="61" y2="36" gradientUnits="userSpaceOnUse">
                <stop offset="0%" stop-color="#c084fc" stop-opacity="0"/>
                <stop offset="50%" stop-color="#f5f3ff" stop-opacity="0.9"/>
                <stop offset="100%" stop-color="#c084fc" stop-opacity="0"/>
              </linearGradient>
            </defs>
          </svg>

          <span class="ai-ball__inner">AI</span>
        </button>

        <div class="ai-panel">
          <header class="ai-panel__header">
            <!-- 左：徽章 + 标题 -->
            <div class="ai-panel__title-wrap">
              <div class="ai-panel__badge">
                <span class="ai-panel__badge-core" />
                <span class="ai-panel__badge-ring" />
              </div>
              <div class="ai-panel__title-block">
                <p class="ai-panel__kicker">Purple Core</p>
                <h3 class="ai-panel__title">云坛智能助手</h3>
              </div>
            </div>

            <!-- 右：模型选择 + 操作按钮 -->
            <div class="ai-panel__header-right">
              <div
                ref="modelPickerRef"
                class="model-picker"
                :class="{ 'model-picker--open': isModelDropdownOpen }"
                @click.stop="toggleModelDropdown"
              >
                <span class="model-picker__label">模型</span>
                <span class="model-picker__value">{{ currentModelLabel }}</span>
                <span class="model-picker__arrow" aria-hidden="true">›</span>
              </div>

              <Teleport to="body">
                <Transition name="dropdown-fade">
                  <div
                    v-if="isModelDropdownOpen"
                    class="ai-model-dropdown"
                    :style="dropdownStyle"
                    role="listbox"
                    @click.stop
                  >
                    <button
                      v-for="model in modelOptions"
                      :key="model.value"
                      class="ai-model-dropdown__item"
                      :class="{ 'ai-model-dropdown__item--active': selectedModel === model.value }"
                      type="button"
                      role="option"
                      :aria-selected="selectedModel === model.value"
                      @click="selectModel(model.value)"
                    >
                      <span class="ai-model-dropdown__name">{{ model.label }}</span>
                      <span class="ai-model-dropdown__desc">{{ model.description }}</span>
                    </button>
                  </div>
                </Transition>
              </Teleport>

              <button
                class="panel-btn panel-btn--new"
                type="button"
                aria-label="新对话"
                title="新对话"
                @click="newConversation"
              >
                <svg viewBox="0 0 16 16" width="15" height="15" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M8 2.5v11M2.5 8h11"/>
                </svg>
              </button>

              <button
                class="panel-btn"
                type="button"
                :aria-label="isFullscreen ? '退出全屏' : '全屏显示'"
                @click="toggleFullscreen"
              >
                <span v-if="!isFullscreen">⛶</span>
                <span v-else>⤢</span>
              </button>

              <button class="panel-btn panel-btn--close" type="button" aria-label="关闭 AI 聊天窗口" @click="closePanel">
                ×
              </button>
            </div>
          </header>

          <!-- 模型描述条 -->
          <div class="ai-panel__model-desc">
            <span class="ai-panel__signal" aria-hidden="true">
              <span />
              <span />
              <span />
            </span>
            <p>{{ modelOptions.find((m) => m.value === selectedModel)?.description }}</p>
          </div>

        <div ref="listRef" class="ai-panel__messages">
          <TransitionGroup name="msg-rise" tag="div" class="message-group">
            <article
              v-for="msg in messages"
              :key="msg.id"
              class="msg"
              :class="[msg.role === 'user' ? 'msg--user' : 'msg--ai', { 'msg--streaming': msg.streaming }]"
            >
              <p v-if="msg.role === 'ai'" class="msg__model">{{ currentModelLabel }}</p>
              <div v-if="msg.role === 'ai'" class="msg__markdown markdown-body" v-html="renderMarkdown(msg.text)" />
              <span v-else class="msg__text">{{ msg.text }}</span>
              <span v-if="msg.streaming" class="msg__cursor" aria-hidden="true" />
            </article>
          </TransitionGroup>
          <p v-if="isThinking && !isStreaming" class="ai-thinking">AI 正在整理回答...</p>
        </div>

        <div class="ai-panel__tips"></div>

        <footer class="ai-panel__footer">
          <input
            v-model="inputText"
            class="ai-input"
            type="text"
            placeholder="输入你的问题..."
            :disabled="isStreaming"
            @keydown.enter.prevent="sendMessage"
          />
          <button class="send-btn" type="button" :disabled="isStreaming" @click="sendMessage">
            <span v-if="isStreaming" class="send-btn__loading" aria-hidden="true" />
            <span v-else>发送</span>
          </button>
          </footer>
        </div>
      </div>
    </div>

    <Transition name="ai-tooltip-fade">
      <div v-if="showTooltip" class="ai-tooltip" :style="tooltipStyle">点击悬浮球直接展开成对话窗</div>
    </Transition>
  </div>
</template>

<style scoped>
.ai-widget {
  position: fixed;
  right: 0;
  bottom: 0;
  z-index: 90;
  --ai-ink: #f5f3ff;
  --ai-muted: #d8b4fe;
  --ai-border: rgba(216, 180, 254, 0.18);
  --ai-panel-bg: rgba(37, 18, 64, 0.9);
  --ai-panel-bg-strong: rgba(24, 11, 44, 0.96);
  --ai-shadow: 0 28px 60px rgba(46, 16, 101, 0.42);
}

.ai-shell {
  position: fixed;
  overflow: visible;
  transition:
    width 0.34s cubic-bezier(0.22, 1, 0.36, 1),
    height 0.34s cubic-bezier(0.22, 1, 0.36, 1),
    right 0.34s cubic-bezier(0.22, 1, 0.36, 1),
    bottom 0.34s cubic-bezier(0.22, 1, 0.36, 1),
    border-radius 0.34s cubic-bezier(0.22, 1, 0.36, 1),
    box-shadow 0.34s ease,
    background 0.34s ease;
}

.ai-shell__surface {
  position: absolute;
  inset: 0;
  overflow: hidden;
  border: 1px solid rgba(216, 180, 254, 0.2);
  background: linear-gradient(135deg, rgba(233, 213, 255, 0.18), rgba(76, 29, 149, 0.16));
  box-shadow: 0 18px 40px rgba(76, 29, 149, 0.38);
  backdrop-filter: blur(24px);
  border-radius: inherit;
  transition:
    border-radius 0.34s cubic-bezier(0.22, 1, 0.36, 1),
    box-shadow 0.34s ease,
    background 0.34s ease;
}

.ai-shell--open {
}

.ai-shell--open .ai-shell__surface {
  background:
    linear-gradient(180deg, rgba(76, 29, 149, 0.25), rgba(24, 11, 44, 0.16)),
    linear-gradient(135deg, var(--ai-panel-bg), var(--ai-panel-bg-strong));
  box-shadow: var(--ai-shadow);
}

.ai-shell__bg,
.ai-shell__glow {
  position: absolute;
  pointer-events: none;
}

.ai-shell__bg {
  inset: 0;
  opacity: 0;
  background-image:
    linear-gradient(rgba(255, 255, 255, 0.04) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 255, 255, 0.04) 1px, transparent 1px);
  background-size: 26px 26px;
  transition: opacity 0.28s ease;
}

.ai-shell__glow {
  border-radius: 999px;
  filter: blur(12px);
  opacity: 0;
  transition: opacity 0.28s ease;
}

.ai-shell__glow--one {
  top: -56px;
  left: -40px;
  width: 260px;
  height: 260px;
  background: radial-gradient(circle, rgba(192, 132, 252, 0.26) 0%, transparent 72%);
}

.ai-shell__glow--two {
  right: -70px;
  top: 120px;
  width: 240px;
  height: 240px;
  background: radial-gradient(circle, rgba(91, 33, 182, 0.34) 0%, transparent 74%);
}

.ai-shell--open .ai-shell__bg,
.ai-shell--open .ai-shell__glow {
  opacity: 1;
}

.ai-ball {
  position: absolute;
  right: 0;
  bottom: 0;
  width: 72px;
  height: 72px;
  display: grid;
  place-items: center;
  border: 0;
  border-radius: 999px;
  cursor: pointer;
  overflow: visible;
  isolation: isolate;
  transform-style: preserve-3d;
  perspective: 160px;
  background: transparent;
  color: white;
  box-shadow:
    0 18px 32px rgba(67, 29, 137, 0.22),
    0 0 0 1px rgba(216, 180, 254, 0.08);
  transition:
    opacity 0.18s ease,
    transform 0.3s ease,
    filter 0.25s ease,
    box-shadow 0.25s ease;
  z-index: 3;
}

.ai-ball::before,
.ai-ball::after {
  content: '';
  position: absolute;
  inset: 0;
  border-radius: 999px;
  pointer-events: none;
}

.ai-ball::before {
  background:
    radial-gradient(circle at 32% 28%, rgba(255, 255, 255, 0.3) 0%, rgba(255, 255, 255, 0) 24%),
    radial-gradient(circle at 50% 50%, rgba(168, 85, 247, 0.28) 0%, rgba(91, 33, 182, 0.18) 40%, rgba(46, 16, 101, 0) 74%);
  filter: blur(0.5px);
  opacity: 0.95;
}

.ai-ball::after {
  inset: 8px;
  border: 1px solid rgba(243, 232, 255, 0.08);
  box-shadow:
    inset 0 0 18px rgba(196, 181, 253, 0.1),
    0 0 16px rgba(168, 85, 247, 0.08);
}

.ai-ball:hover {
  transform: translateY(-4px) scale(1.07) rotateX(10deg) rotateY(-10deg);
  filter: brightness(1.14) saturate(1.14);
  box-shadow:
    0 24px 40px rgba(67, 29, 137, 0.32),
    0 0 0 1px rgba(216, 180, 254, 0.14);
}

.ai-shell--open .ai-ball {
  opacity: 0;
  transform: scale(0.72);
  pointer-events: none;
}

.ai-ball__inner {
  position: absolute;
  z-index: 4;
  font-size: 14px;
  font-weight: 800;
  letter-spacing: 0.12em;
  text-shadow: 0 0 14px rgba(233, 213, 255, 0.95), 0 0 4px rgba(168, 85, 247, 0.8);
  color: #f3e8ff;
  transform: translateZ(18px);
}

.ai-ball__orbit,
.ai-ball__halo,
.ai-ball__specular,
.ai-ball__core-glow {
  position: absolute;
  border-radius: 999px;
  pointer-events: none;
}

.ai-ball__orbit {
  inset: 4px;
  border: 1px solid rgba(226, 200, 255, 0.16);
  opacity: 0.72;
  mix-blend-mode: screen;
}

.ai-ball__orbit--one {
  transform: rotate(18deg) scaleY(0.58);
  animation: orbitSpin 8.5s linear infinite;
}

.ai-ball__orbit--two {
  inset: 10px;
  border-color: rgba(168, 85, 247, 0.18);
  transform: rotate(-26deg) scaleY(0.72);
  animation: orbitSpinReverse 6.8s linear infinite;
}

.ai-ball__halo {
  inset: 6px;
  border: 1px solid rgba(243, 232, 255, 0.14);
  box-shadow:
    0 0 0 1px rgba(168, 85, 247, 0.08),
    inset 0 0 14px rgba(196, 181, 253, 0.1);
  opacity: 0.9;
}

.ai-ball__specular {
  left: 16px;
  top: 10px;
  width: 24px;
  height: 14px;
  background: radial-gradient(circle at center, rgba(255, 255, 255, 0.65), rgba(255, 255, 255, 0) 72%);
  filter: blur(2px);
  transform: rotate(-18deg);
  opacity: 0.8;
}

.ai-ball__core-glow {
  inset: 24px;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.72) 0%, rgba(216, 180, 254, 0.38) 30%, rgba(168, 85, 247, 0) 74%);
  filter: blur(4px);
  animation: corePulse 2.8s ease-in-out infinite;
}

/* 3D 线框球 SVG */
.ai-globe {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  z-index: 2;
  animation: globeSpin 18s linear infinite;
  filter: drop-shadow(0 0 8px rgba(168, 85, 247, 0.55));
  transform: translateZ(10px);
}

.globe-scan {
  animation: scanFloat 3.2s ease-in-out infinite;
}

.globe-wave {
  transform-origin: center;
}

.globe-wave--a {
  animation: waveFloatA 3.2s ease-in-out infinite;
}

.globe-wave--b {
  animation: waveFloatB 4.1s ease-in-out infinite;
}

.ai-ball__sonar {
  position: absolute;
  inset: -8px;
  border-radius: 999px;
  border: 1px solid rgba(216, 180, 254, 0.28);
  opacity: 0;
  pointer-events: none;
  box-shadow: 0 0 18px rgba(196, 181, 253, 0.12);
}

.ai-ball:hover .ai-ball__sonar--one {
  animation: sonarPulse 2.2s ease-out infinite;
}

.ai-ball:hover .ai-ball__sonar--two {
  animation: sonarPulse 2.2s ease-out 0.55s infinite;
}

.ai-ball:hover .ai-ball__orbit--one {
  border-color: rgba(243, 232, 255, 0.28);
}

.ai-ball:hover .ai-ball__orbit--two {
  border-color: rgba(216, 180, 254, 0.3);
}

.ai-ball:hover .ai-ball__halo {
  opacity: 1;
  box-shadow:
    0 0 0 1px rgba(216, 180, 254, 0.16),
    inset 0 0 20px rgba(216, 180, 254, 0.16);
}

.ai-ball:hover .ai-ball__specular {
  opacity: 1;
  transform: rotate(-20deg) translateY(-1px);
}

@keyframes globeSpin {
  from { transform: rotate(0deg); }
  to   { transform: rotate(360deg); }
}

@keyframes orbitSpin {
  from { transform: rotate(18deg) scaleY(0.58); }
  to { transform: rotate(378deg) scaleY(0.58); }
}

@keyframes orbitSpinReverse {
  from { transform: rotate(-26deg) scaleY(0.72); }
  to { transform: rotate(-386deg) scaleY(0.72); }
}

@keyframes corePulse {
  0%, 100% {
    opacity: 0.72;
    transform: scale(0.9);
    filter: blur(4px);
  }
  50% {
    opacity: 1;
    transform: scale(1.16);
    filter: blur(5px);
  }
}

@keyframes scanFloat {
  0%, 100% {
    opacity: 0.26;
    transform: rotate(0deg) scaleX(0.94);
  }
  50% {
    opacity: 0.82;
    transform: rotate(8deg) scaleX(1.04);
  }
}

@keyframes waveFloatA {
  0%, 100% { d: path("M 6 36 Q 20 22 36 36 Q 52 50 66 36"); }
  50%       { d: path("M 6 36 Q 20 28 36 36 Q 52 44 66 36"); }
}

@keyframes waveFloatB {
  0%, 100% { d: path("M 6 36 Q 20 50 36 36 Q 52 22 66 36"); }
  50%       { d: path("M 6 36 Q 20 44 36 36 Q 52 28 66 36"); }
}

.ai-panel {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  opacity: 0;
  transform: scale(0.92);
  pointer-events: none;
  transition: opacity 0.2s ease 0.1s, transform 0.32s cubic-bezier(0.22, 1, 0.36, 1);
}

.ai-shell--open .ai-panel {
  opacity: 1;
  transform: scale(1);
  pointer-events: auto;
}

.ai-panel__header,
.ai-panel__model-desc,
.ai-panel__messages,
.ai-panel__tips,
.ai-panel__footer {
  position: relative;
  z-index: 1;
}

.ai-panel__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  padding: 14px 16px 12px;
  border-bottom: 1px solid rgba(216, 180, 254, 0.1);
  flex-shrink: 0;
}

.ai-panel__title-wrap {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
  flex: 1;
}

.ai-panel__badge {
  position: relative;
  width: 22px;
  height: 22px;
  flex: 0 0 auto;
}

.ai-panel__badge-core,
.ai-panel__badge-ring {
  position: absolute;
  inset: 0;
  border-radius: 999px;
}

.ai-panel__badge-core {
  background: linear-gradient(135deg, #e9d5ff, #a855f7);
  box-shadow: 0 0 20px rgba(192, 132, 252, 0.4);
}

.ai-panel__badge-ring {
  background: rgba(216, 180, 254, 0.18);
  animation: badgePulse 1.8s ease-out infinite;
}

.ai-panel__title-block {
  min-width: 0;
}

.ai-panel__kicker {
  margin: 0;
  color: #c4b5fd;
  font-size: 10px;
  font-weight: 800;
  letter-spacing: 0.16em;
  text-transform: uppercase;
  line-height: 1;
}

.ai-panel__title {
  margin: 2px 0 0;
  color: var(--ai-ink);
  font-size: 16px;
  line-height: 1.2;
  font-weight: 800;
}

.ai-panel__header-right {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

/* 模型描述条 */
.ai-panel__model-desc {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 16px;
  background: rgba(168, 85, 247, 0.08);
  border-bottom: 1px solid rgba(216, 180, 254, 0.08);
  flex-shrink: 0;
}

.ai-panel__model-desc p {
  margin: 0;
  color: #c4b5fd;
  font-size: 12px;
  line-height: 1.4;
}

.model-picker {
  position: relative;
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 5px 10px 5px 11px;
  border-radius: 999px;
  border: 1px solid rgba(216, 180, 254, 0.2);
  background: rgba(168, 85, 247, 0.12);
  cursor: pointer;
  user-select: none;
  transition: background 0.18s ease, border-color 0.18s ease;
}

.model-picker:hover,
.model-picker--open {
  background: rgba(168, 85, 247, 0.22);
  border-color: rgba(216, 180, 254, 0.36);
}

.model-picker__label {
  color: #c4b5fd;
  font-size: 11px;
  font-weight: 700;
  white-space: nowrap;
}

.model-picker__value {
  color: #f5f3ff;
  font-size: 13px;
  font-weight: 700;
  white-space: nowrap;
}

.model-picker__arrow {
  color: #c4b5fd;
  font-size: 14px;
  line-height: 1;
  transform: rotate(90deg);
  transition: transform 0.2s ease;
  display: block;
}

.model-picker--open .model-picker__arrow {
  transform: rotate(-90deg);
}

/* 自定义下拉面板（嵌入式，废弃） */
.model-dropdown,
.model-dropdown__item,
.model-dropdown__name,
.model-dropdown__desc { display: none; }

/* Teleport 到 body 的全局下拉面板 */
:global(.ai-model-dropdown) {
  position: fixed;
  min-width: 220px;
  border-radius: 16px;
  border: 1px solid rgba(216, 180, 254, 0.2);
  background: rgba(30, 10, 60, 0.96);
  backdrop-filter: blur(20px);
  box-shadow: 0 16px 40px rgba(46, 16, 101, 0.55), 0 0 0 1px rgba(216, 180, 254, 0.06);
  padding: 6px;
  z-index: 9999;
}

:global(.ai-model-dropdown__item) {
  display: flex;
  flex-direction: column;
  gap: 3px;
  width: 100%;
  padding: 10px 12px;
  border: 0;
  border-radius: 10px;
  background: transparent;
  text-align: left;
  cursor: pointer;
  transition: background 0.15s ease;
}

:global(.ai-model-dropdown__item:hover) {
  background: rgba(168, 85, 247, 0.18);
}

:global(.ai-model-dropdown__item--active) {
  background: rgba(168, 85, 247, 0.28);
}

:global(.ai-model-dropdown__name) {
  color: #f5f3ff;
  font-size: 13px;
  font-weight: 700;
  line-height: 1.3;
}

:global(.ai-model-dropdown__desc) {
  color: #c4b5fd;
  font-size: 11px;
  line-height: 1.5;
}

:global(.dropdown-fade-enter-active),
:global(.dropdown-fade-leave-active) {
  transition: opacity 0.15s ease, transform 0.18s cubic-bezier(0.22, 1, 0.36, 1);
}

:global(.dropdown-fade-enter-from),
:global(.dropdown-fade-leave-to) {
  opacity: 0;
  transform: translateY(-6px) scale(0.97);
}

.panel-btn {
  width: 34px;
  height: 34px;
  display: flex;
  align-items: center;
  justify-content: center;
  line-height: 1;
  border: 1px solid rgba(216, 180, 254, 0.14);
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.06);
  color: #f5f3ff;
  cursor: pointer;
  font-size: 16px;
  padding: 0;
  transition: transform 0.2s ease, background 0.2s ease, border-color 0.2s ease;
}

.panel-btn:hover {
  transform: translateY(-1px);
  background: rgba(192, 132, 252, 0.16);
  border-color: rgba(216, 180, 254, 0.28);
}

.panel-btn--close {
  font-size: 20px;
}

.ai-panel__signal {
  display: flex;
  align-items: flex-end;
  gap: 3px;
  flex-shrink: 0;
}

.ai-panel__signal span {
  width: 4px;
  border-radius: 999px;
  background: linear-gradient(180deg, #e9d5ff, #a855f7);
  animation: signalBounce 1.4s ease-in-out infinite;
}

.ai-panel__signal span:nth-child(1) { height: 10px; }
.ai-panel__signal span:nth-child(2) { height: 16px; animation-delay: 0.2s; }
.ai-panel__signal span:nth-child(3) { height: 12px; animation-delay: 0.4s; }

.ai-panel__messages {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 14px 16px;
  scrollbar-width: thin;
  scrollbar-color: rgba(168, 85, 247, 0.3) transparent;
}

.message-group {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.msg {
  max-width: 88%;
  padding: 13px 14px;
  border-radius: 20px;
  font-size: 13px;
  line-height: 1.65;
  word-break: break-word;
}

.msg--ai {
  align-self: flex-start;
  color: #f5f3ff;
  text-align: left;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.08), rgba(216, 180, 254, 0.04)),
    rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(216, 180, 254, 0.12);
}

.msg--streaming {
  box-shadow: 0 0 0 1px rgba(216, 180, 254, 0.08), 0 10px 24px rgba(76, 29, 149, 0.12);
}

.msg--user {
  align-self: flex-end;
  color: #ffffff;
  background: linear-gradient(135deg, #a855f7 0%, #6d28d9 100%);
  box-shadow: 0 16px 32px rgba(109, 40, 217, 0.28);
}

.msg__model {
  margin: 0 0 4px;
  color: #d8b4fe;
  font-size: 11px;
  font-weight: 700;
}

.ai-thinking {
  margin: 14px 0 0;
  color: #c4b5fd;
  font-size: 12px;
  animation: thinkingPulse 1.2s ease-in-out infinite;
}

.ai-panel__tips {
  display: flex;
  flex-wrap: wrap;
  gap: 7px;
  padding: 10px 16px;
  border-top: 1px solid rgba(216, 180, 254, 0.08);
  flex-shrink: 0;
}

.tip-btn {
  padding: 8px 12px;
  border-radius: 999px;
  border: 1px solid rgba(216, 180, 254, 0.14);
  background: rgba(255, 255, 255, 0.05);
  color: #ede9fe;
  font-size: 12px;
  cursor: pointer;
  transition: transform 0.18s ease, background 0.18s ease, border-color 0.18s ease;
}

.tip-btn:hover {
  transform: translateY(-1px);
  background: rgba(168, 85, 247, 0.18);
  border-color: rgba(216, 180, 254, 0.26);
}

.ai-panel__footer {
  display: flex;
  gap: 10px;
  padding: 12px 16px 16px;
  border-top: 1px solid rgba(216, 180, 254, 0.1);
  flex-shrink: 0;
}

.ai-input {
  flex: 1;
  min-width: 0;
  height: 46px;
  padding: 0 14px;
  border-radius: 16px;
  border: 1px solid rgba(216, 180, 254, 0.14);
  background: rgba(255, 255, 255, 0.06);
  color: white;
  outline: none;
}

.ai-input::placeholder {
  color: #c4b5fd;
}

.ai-input:focus {
  border-color: rgba(216, 180, 254, 0.34);
  box-shadow: 0 0 0 4px rgba(168, 85, 247, 0.14);
}

.send-btn {
  min-width: 86px;
  height: 46px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 0;
  border-radius: 16px;
  color: white;
  font-weight: 700;
  cursor: pointer;
  background: linear-gradient(135deg, #c084fc 0%, #7c3aed 100%);
  box-shadow: 0 16px 30px rgba(109, 40, 217, 0.28);
  transition: opacity 0.18s ease;
}

.send-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.send-btn__loading {
  display: inline-block;
  width: 18px;
  height: 18px;
  border: 2px solid rgba(255, 255, 255, 0.35);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
}

.msg__text {
  white-space: pre-wrap;
  word-break: break-word;
}

.msg__markdown {
  color: #f5f3ff;
  text-align: left;
}

.msg__markdown :deep(*) {
  max-width: 100%;
}

.msg__markdown :deep(p),
.msg__markdown :deep(ul),
.msg__markdown :deep(ol),
.msg__markdown :deep(pre),
.msg__markdown :deep(blockquote) {
  margin: 0 0 0.8em;
}

.msg__markdown :deep(p:last-child),
.msg__markdown :deep(ul:last-child),
.msg__markdown :deep(ol:last-child),
.msg__markdown :deep(pre:last-child),
.msg__markdown :deep(blockquote:last-child) {
  margin-bottom: 0;
}

.msg__markdown :deep(h1),
.msg__markdown :deep(h2),
.msg__markdown :deep(h3),
.msg__markdown :deep(h4) {
  margin: 0 0 0.65em;
  line-height: 1.35;
  color: #f8f4ff;
}

.msg__markdown :deep(h1) { font-size: 1.2rem; }
.msg__markdown :deep(h2) { font-size: 1.1rem; }
.msg__markdown :deep(h3) { font-size: 1rem; }

.msg__markdown :deep(p),
.msg__markdown :deep(li) {
  line-height: 1.72;
}

.msg__markdown :deep(ul),
.msg__markdown :deep(ol) {
  padding-left: 1.4em;
}

.msg__markdown :deep(blockquote) {
  padding: 0.7em 0.9em;
  border-left: 3px solid rgba(216, 180, 254, 0.6);
  background: rgba(255, 255, 255, 0.04);
  border-radius: 0 10px 10px 0;
  color: #ddd6fe;
}

.msg__markdown :deep(code) {
  font-family: Consolas, Monaco, 'Courier New', monospace;
}

.msg__markdown :deep(:not(pre) > code) {
  padding: 0.14em 0.38em;
  border-radius: 6px;
  background: rgba(17, 24, 39, 0.45);
  color: #f5d0fe;
}

.msg__markdown :deep(pre) {
  overflow-x: auto;
  padding: 0.9em 1em;
  border-radius: 14px;
  background: rgba(10, 14, 30, 0.6);
  border: 1px solid rgba(216, 180, 254, 0.12);
}

.msg__markdown :deep(pre code) {
  color: #f5f3ff;
  white-space: pre;
}

.msg__markdown :deep(a) {
  color: #e9d5ff;
  text-decoration: underline;
  text-decoration-color: rgba(233, 213, 255, 0.45);
}

.msg__markdown :deep(hr) {
  border: 0;
  border-top: 1px solid rgba(216, 180, 254, 0.2);
  margin: 1em 0;
}

.msg__cursor {
  display: inline-block;
  width: 2px;
  height: 1em;
  background: #d8b4fe;
  border-radius: 1px;
  margin-left: 2px;
  vertical-align: text-bottom;
  animation: cursorBlink 0.9s step-end infinite;
}

/* 新对话按钮 */
.panel-btn--new {
  border-color: rgba(168, 85, 247, 0.28);
  color: #c4b5fd;
}

.panel-btn--new:hover {
  background: rgba(168, 85, 247, 0.2);
  border-color: rgba(216, 180, 254, 0.4);
  color: #f3e8ff;
}

.ai-input:disabled {
  opacity: 0.55;
  cursor: not-allowed;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

@keyframes cursorBlink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0; }
}

.ai-tooltip {
  position: fixed;
  max-width: min(280px, calc(100vw - 132px));
  white-space: nowrap;
  padding: 10px 14px;
  border-radius: 999px;
  background: rgba(46, 16, 101, 0.92);
  color: #f5f3ff;
  border: 1px solid rgba(216, 180, 254, 0.18);
  box-shadow: 0 18px 40px rgba(46, 16, 101, 0.28);
  font-size: 12px;
  pointer-events: none;
}

.ai-tooltip-fade-enter-active,
.ai-tooltip-fade-leave-active,
.msg-rise-enter-active,
.msg-rise-leave-active {
  transition: all 0.2s ease;
}

.ai-tooltip-fade-enter-from,
.ai-tooltip-fade-leave-to {
  opacity: 0;
  transform: translateX(8px);
}

.msg-rise-enter-from,
.msg-rise-leave-to {
  opacity: 0;
  transform: translateY(10px);
}

@media (max-width: 768px) {
  .ai-tooltip {
    display: none;
  }

  .model-picker__label {
    display: none;
  }
}

@media (max-width: 480px) {
  .ai-panel__header {
    padding: 10px 12px;
  }

  .ai-panel__footer {
    flex-direction: column;
  }

  .send-btn {
    width: 100%;
  }

  .msg {
    max-width: 94%;
  }
}

@keyframes sonarPulse {
  0% { transform: scale(1); opacity: 0.5; }
  100% { transform: scale(1.9); opacity: 0; }
}


@keyframes badgePulse {
  0% { transform: scale(1); opacity: 0.45; }
  100% { transform: scale(2.2); opacity: 0; }
}

@keyframes signalBounce {
  0%, 100% { transform: scaleY(0.65); opacity: 0.72; }
  50% { transform: scaleY(1.15); opacity: 1; }
}

@keyframes thinkingPulse {
  0%, 100% { opacity: 0.45; }
  50% { opacity: 1; }
}
</style>
