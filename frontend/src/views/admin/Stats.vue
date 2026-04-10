<template>
  <div class="stats-page">
    <div class="stats-toolbar">
      <div>
        <h2 class="stats-title">站点统计看板</h2>
        <p class="stats-subtitle">基于测试数据的多维统计视图，便于后续平滑切换真实接口</p>
      </div>
      <el-button type="primary" :loading="refreshing" @click="refreshMockData">刷新测试数据</el-button>
    </div>

    <el-row :gutter="16" class="mb-4">
      <el-col v-for="item in kpis" :key="item.label" :xs="24" :sm="12" :lg="6" class="mb-3">
        <el-card shadow="hover" class="kpi-card">
          <div class="kpi-label">{{ item.label }}</div>
          <div class="kpi-value">{{ item.value }}</div>
          <div class="kpi-trend" :class="item.trend >= 0 ? 'up' : 'down'">
            {{ item.trend >= 0 ? '+' : '' }}{{ item.trend }}% 较上周
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <el-col :xs="24" :lg="16" class="mb-4">
        <el-card class="chart-card" shadow="never">
          <template #header>
            <div class="card-head">流量趋势（近14日）</div>
          </template>
          <div ref="trendRef" class="chart-lg"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="8" class="mb-4">
        <el-card class="chart-card" shadow="never">
          <template #header>
            <div class="card-head">访问来源占比</div>
          </template>
          <div ref="sourceRef" class="chart-sm"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <el-col :xs="24" :lg="8" class="mb-4">
        <el-card class="chart-card" shadow="never">
          <template #header>
            <div class="card-head">设备分布</div>
          </template>
          <div ref="deviceRef" class="chart-sm"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="8" class="mb-4">
        <el-card class="chart-card" shadow="never">
          <template #header>
            <div class="card-head">内容质量雷达</div>
          </template>
          <div ref="radarRef" class="chart-sm"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="8" class="mb-4">
        <el-card class="chart-card" shadow="never">
          <template #header>
            <div class="card-head">频道贡献度</div>
          </template>
          <div ref="channelRef" class="chart-sm"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import * as echarts from 'echarts'
import { useIsDark } from '@/composables/useIsDark'

type TrendPoint = { day: string; pv: number; uv: number }

const isDark = useIsDark()
const refreshing = ref(false)
const trendRef = ref<HTMLDivElement | null>(null)
const sourceRef = ref<HTMLDivElement | null>(null)
const deviceRef = ref<HTMLDivElement | null>(null)
const radarRef = ref<HTMLDivElement | null>(null)
const channelRef = ref<HTMLDivElement | null>(null)

const trendData = ref<TrendPoint[]>([])
const sourceData = ref<{ name: string; value: number }[]>([])
const deviceData = ref<{ name: string; value: number }[]>([])
const channelData = ref<{ name: string; value: number }[]>([])
const qualityData = ref<number[]>([])

let trendChart: echarts.ECharts | null = null
let sourceChart: echarts.ECharts | null = null
let deviceChart: echarts.ECharts | null = null
let radarChart: echarts.ECharts | null = null
let channelChart: echarts.ECharts | null = null

const random = (min: number, max: number) => Math.floor(Math.random() * (max - min + 1)) + min

const buildMockData = () => {
  const today = new Date()
  trendData.value = Array.from({ length: 14 }).map((_, i) => {
    const d = new Date(today)
    d.setDate(today.getDate() - (13 - i))
    const day = `${d.getMonth() + 1}/${d.getDate()}`
    const pv = random(1200, 4200)
    const uv = Math.floor(pv * (0.45 + Math.random() * 0.15))
    return { day, pv, uv }
  })

  sourceData.value = [
    { name: '搜索引擎', value: random(30, 45) },
    { name: '直接访问', value: random(15, 25) },
    { name: '外部链接', value: random(12, 22) },
    { name: '社交平台', value: random(10, 20) },
    { name: '站内推荐', value: random(8, 16) },
  ]

  deviceData.value = [
    { name: '移动端', value: random(45, 62) },
    { name: '桌面端', value: random(30, 45) },
    { name: '平板端', value: random(3, 12) },
  ]

  channelData.value = [
    { name: '技术随笔', value: random(120, 320) },
    { name: '实验开发', value: random(90, 280) },
    { name: '数据库', value: random(80, 240) },
    { name: '架构思考', value: random(60, 210) },
    { name: '运维部署', value: random(50, 180) },
  ]

  qualityData.value = [
    random(72, 95),
    random(68, 90),
    random(70, 94),
    random(64, 88),
    random(73, 96),
    random(66, 89),
  ]
}

const kpis = computed(() => {
  const totalPv = trendData.value.reduce((sum, item) => sum + item.pv, 0)
  const totalUv = trendData.value.reduce((sum, item) => sum + item.uv, 0)
  const avgPv = trendData.value.length ? Math.round(totalPv / trendData.value.length) : 0
  const bounce = random(22, 36)
  return [
    { label: '总访问量(PV)', value: totalPv.toLocaleString(), trend: random(8, 18) },
    { label: '独立访客(UV)', value: totalUv.toLocaleString(), trend: random(6, 15) },
    { label: '日均访问', value: avgPv.toLocaleString(), trend: random(2, 12) },
    { label: '跳出率', value: `${bounce}%`, trend: -random(2, 8) },
  ]
})

const axisColor = computed(() => (isDark.value ? '#a3a3a3' : '#64748b'))
const splitColor = computed(() => (isDark.value ? 'rgba(255,255,255,0.08)' : 'rgba(15,23,42,0.08)'))

const setTrendOption = () => {
  if (!trendChart) return
  trendChart.setOption({
    color: ['#f43f5e', '#38bdf8'],
    tooltip: { trigger: 'axis' },
    legend: { top: 0, textStyle: { color: axisColor.value } },
    grid: { left: '3%', right: '3%', bottom: '3%', top: 34, containLabel: true },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      axisLine: { lineStyle: { color: splitColor.value } },
      axisLabel: { color: axisColor.value },
      data: trendData.value.map((d) => d.day),
    },
    yAxis: {
      type: 'value',
      axisLine: { lineStyle: { color: splitColor.value } },
      axisLabel: { color: axisColor.value },
      splitLine: { lineStyle: { color: splitColor.value } },
    },
    series: [
      {
        name: 'PV',
        type: 'line',
        smooth: true,
        symbol: 'none',
        areaStyle: { opacity: 0.16 },
        lineStyle: { width: 3 },
        data: trendData.value.map((d) => d.pv),
      },
      {
        name: 'UV',
        type: 'line',
        smooth: true,
        symbol: 'none',
        areaStyle: { opacity: 0.1 },
        lineStyle: { width: 2 },
        data: trendData.value.map((d) => d.uv),
      },
    ],
  })
}

const setSourceOption = () => {
  if (!sourceChart) return
  sourceChart.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: 0, textStyle: { color: axisColor.value } },
    series: [{
      type: 'pie',
      radius: ['44%', '72%'],
      center: ['50%', '44%'],
      avoidLabelOverlap: true,
      itemStyle: { borderRadius: 8, borderColor: isDark.value ? '#111' : '#fff', borderWidth: 2 },
      label: { color: axisColor.value, formatter: '{b}\n{d}%' },
      data: sourceData.value,
    }],
  })
}

const setDeviceOption = () => {
  if (!deviceChart) return
  deviceChart.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: 0, textStyle: { color: axisColor.value } },
    series: [{
      type: 'pie',
      radius: ['40%', '68%'],
      center: ['50%', '42%'],
      label: { color: axisColor.value },
      itemStyle: { borderRadius: 10, borderColor: isDark.value ? '#111' : '#fff', borderWidth: 2 },
      data: deviceData.value,
    }],
  })
}

const setRadarOption = () => {
  if (!radarChart) return
  radarChart.setOption({
    tooltip: { trigger: 'item' },
    radar: {
      indicator: [
        { name: '阅读完成率', max: 100 },
        { name: '互动率', max: 100 },
        { name: '收藏率', max: 100 },
        { name: '分享率', max: 100 },
        { name: '复访率', max: 100 },
        { name: '停留时长', max: 100 },
      ],
      axisName: { color: axisColor.value },
      splitLine: { lineStyle: { color: splitColor.value } },
      splitArea: { areaStyle: { color: ['transparent'] } },
      axisLine: { lineStyle: { color: splitColor.value } },
    },
    series: [{
      type: 'radar',
      areaStyle: { color: 'rgba(244,63,94,0.2)' },
      lineStyle: { color: '#f43f5e' },
      itemStyle: { color: '#f43f5e' },
      data: [{ value: qualityData.value, name: '质量评分' }],
    }],
  })
}

const setChannelOption = () => {
  if (!channelChart) return
  channelChart.setOption({
    color: ['#fb7185'],
    tooltip: { trigger: 'axis' },
    grid: { left: '4%', right: '4%', top: 14, bottom: 6, containLabel: true },
    xAxis: {
      type: 'value',
      axisLabel: { color: axisColor.value },
      splitLine: { lineStyle: { color: splitColor.value } },
    },
    yAxis: {
      type: 'category',
      axisLabel: { color: axisColor.value },
      data: channelData.value.map((d) => d.name),
    },
    series: [{
      type: 'bar',
      data: channelData.value.map((d) => d.value),
      barWidth: 16,
      itemStyle: { borderRadius: [0, 8, 8, 0] },
    }],
  })
}

const renderAllCharts = () => {
  setTrendOption()
  setSourceOption()
  setDeviceOption()
  setRadarOption()
  setChannelOption()
}

const initCharts = async () => {
  await nextTick()
  if (trendRef.value) trendChart = echarts.init(trendRef.value)
  if (sourceRef.value) sourceChart = echarts.init(sourceRef.value)
  if (deviceRef.value) deviceChart = echarts.init(deviceRef.value)
  if (radarRef.value) radarChart = echarts.init(radarRef.value)
  if (channelRef.value) channelChart = echarts.init(channelRef.value)
  renderAllCharts()
}

const resizeAll = () => {
  trendChart?.resize()
  sourceChart?.resize()
  deviceChart?.resize()
  radarChart?.resize()
  channelChart?.resize()
}

const refreshMockData = async () => {
  refreshing.value = true
  buildMockData()
  await nextTick()
  renderAllCharts()
  refreshing.value = false
}

onMounted(async () => {
  buildMockData()
  await initCharts()
  window.addEventListener('resize', resizeAll)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeAll)
  trendChart?.dispose()
  sourceChart?.dispose()
  deviceChart?.dispose()
  radarChart?.dispose()
  channelChart?.dispose()
  trendChart = null
  sourceChart = null
  deviceChart = null
  radarChart = null
  channelChart = null
})
</script>

<style scoped>
.stats-page {
  padding: 16px;
}

.stats-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.stats-title {
  margin: 0;
  font-size: 26px;
  font-weight: 800;
  color: #0f172a;
}

.dark .stats-title {
  color: #f8fafc;
}

.stats-subtitle {
  margin: 4px 0 0;
  font-size: 13px;
  color: #64748b;
}

.dark .stats-subtitle {
  color: #9ca3af;
}

.kpi-card {
  border-radius: 12px;
}

.kpi-label {
  font-size: 13px;
  color: #64748b;
}

.kpi-value {
  margin-top: 8px;
  font-size: 30px;
  font-weight: 800;
  color: #0f172a;
}

.dark .kpi-value {
  color: #f8fafc;
}

.kpi-trend {
  margin-top: 8px;
  font-size: 12px;
}

.kpi-trend.up {
  color: #16a34a;
}

.kpi-trend.down {
  color: #dc2626;
}

.chart-card {
  border-radius: 12px;
}

.card-head {
  font-weight: 700;
}

.chart-lg {
  width: 100%;
  height: 340px;
}

.chart-sm {
  width: 100%;
  height: 300px;
}

@media (max-width: 1024px) {
  .chart-lg,
  .chart-sm {
    height: 280px;
  }
}
</style>
