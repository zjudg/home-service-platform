<template>
  <div
    v-loading="loading"
    class="app-container finance-page"
  >
    <section class="summary-panel">
      <div class="section-heading">
        <div>
          <h2>数据看板</h2>
          <p>
            统计区间 {{ rangeText }}，营业额按支付成功时间、退款按申请时间归集
          </p>
        </div>
        <div class="range-picker">
          <el-date-picker
            v-model="range"
            type="daterange"
            value-format="YYYY-MM-DD"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            :shortcuts="shortcuts"
            :clearable="false"
            @change="load"
          />
          <el-button
            :loading="loading"
            @click="load"
          >
            刷新
          </el-button>
        </div>
      </div>

      <div class="summary-grid">
        <div
          v-for="item in summaryCards"
          :key="item.label"
          class="summary-item"
        >
          <div
            class="summary-icon"
            :class="item.color"
          >
            <el-icon><component :is="item.icon" /></el-icon>
          </div>
          <div class="summary-copy">
            <strong>{{ item.value }}</strong>
            <span>{{ item.label }}</span>
          </div>
        </div>
      </div>
    </section>

    <section class="chart-grid">
      <div
        v-for="chart in charts"
        :key="chart.key"
        class="chart-panel"
      >
        <div class="panel-heading">
          <div>
            <h3>{{ chart.title }}</h3>
            <span>{{ chart.description }}</span>
          </div>
          <el-tag
            size="small"
            effect="plain"
          >
            {{ dayCount }} 天
          </el-tag>
        </div>
        <div
          :ref="el => setChartRef(chart.key, el)"
          class="chart"
        />
      </div>
    </section>
  </div>
</template>

<script setup name="ZjudgFinance">
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Tickets, TrendCharts, Wallet, WarningFilled } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { getFinanceDashboard } from '@/api/zjudg/finance'
import { axisLabels, formatMoney, recentRange } from './range'

const loading = ref(false)
const range = ref(defaultRange())

const data = reactive({
  days: [],
  summary: {},
  sales: [],
  orders: [],
  newUsers: [],
  activeUsers: [],
  cashflow: { revenue: [], refund: [] },
  refunds: []
})

const charts = [
  { key: 'sales', title: '销售金额趋势', description: '每日支付成功金额' },
  { key: 'orders', title: '成交订单趋势', description: '每日支付成功订单数' },
  { key: 'newUsers', title: '每日新增', description: '每日新增用户数' },
  { key: 'activeUsers', title: '每日活跃', description: '每日活跃用户数' },
  { key: 'cashflow', title: '营业额与退款对比', description: '每日收款与退款金额' },
  { key: 'refunds', title: '退款状态分布', description: '退款记录状态占比' }
]

const shortcuts = [
  { text: '近 7 天', value: () => recentRange(7) },
  { text: '近 15 天', value: () => recentRange(15) },
  { text: '近 30 天', value: () => recentRange(30) }
]

const chartRefs = {}
const chartInstances = {}

const summaryCards = computed(() => [
  { label: '累计营业额（元）', value: formatMoney(data.summary.revenue), icon: Wallet, color: 'blue' },
  { label: '净营业额（元）', value: formatMoney(data.summary.netRevenue), icon: TrendCharts, color: 'green' },
  { label: '成交订单（笔）', value: String(data.summary.orders ?? 0), icon: Tickets, color: 'orange' },
  { label: '已退款金额（元）', value: formatMoney(data.summary.refunded), icon: WarningFilled, color: 'red' },
  { label: '待退款金额（元）', value: formatMoney(data.summary.pendingRefund), icon: WarningFilled, color: 'orange' },
  { label: '师傅结算（元）', value: formatMoney(data.summary.masterIncome), icon: Wallet, color: 'green' },
  { label: '平台毛利（元）', value: formatMoney(data.summary.platformIncome), icon: TrendCharts, color: 'blue' },
  { label: '客单价（元）', value: formatMoney(data.summary.avgOrderAmount), icon: Tickets, color: 'blue' }
])

const dayCount = computed(() => data.days.length || 0)
const rangeText = computed(() => `${data.days[0] || '-'} 至 ${data.days[data.days.length - 1] || '-'}`)

function defaultRange () {
  return recentRange(15)
}

function axisDays () {
  return axisLabels(data.days)
}

function setChartRef (key, element) {
  if (element) chartRefs[key] = element
}

function lineOption (name, color, values) {
  return {
    tooltip: { trigger: 'axis' },
    grid: { left: 46, right: 22, top: 24, bottom: 30 },
    xAxis: { type: 'category', boundaryGap: false, data: axisDays(), axisLabel: { color: '#a39b95' }, axisLine: { lineStyle: { color: '#ece6e1' } } },
    yAxis: { type: 'value', min: 0, minInterval: 1, axisLabel: { color: '#a39b95' }, splitLine: { lineStyle: { color: '#f5f1ee' } } },
    series: [{ name, type: 'line', smooth: true, symbol: 'circle', symbolSize: 5, data: values, lineStyle: { color, width: 2 }, itemStyle: { color }, areaStyle: { color: `${color}18` } }]
  }
}

function renderChart (key) {
  const element = chartRefs[key]
  if (!element) return
  const instance = chartInstances[key] || echarts.init(element)
  chartInstances[key] = instance

  if (key === 'sales') instance.setOption(lineOption('销售金额', '#e06a3d', data.sales), true)
  if (key === 'orders') instance.setOption(lineOption('订单量', '#2f9e6b', data.orders), true)
  if (key === 'newUsers') instance.setOption(lineOption('新增用户', '#168bd0', data.newUsers), true)
  if (key === 'activeUsers') instance.setOption(lineOption('活跃用户', '#7c5cff', data.activeUsers), true)
  if (key === 'cashflow') {
    instance.setOption({
      tooltip: { trigger: 'axis' },
      legend: { data: ['营业额', '退款'], textStyle: { color: '#7a726c' } },
      grid: { left: 46, right: 22, top: 42, bottom: 30 },
      xAxis: { type: 'category', data: axisDays(), axisLabel: { color: '#a39b95' }, axisLine: { lineStyle: { color: '#ece6e1' } } },
      yAxis: { type: 'value', min: 0, minInterval: 1, axisLabel: { color: '#a39b95' }, splitLine: { lineStyle: { color: '#f5f1ee' } } },
      series: [
        { name: '营业额', type: 'bar', barMaxWidth: 14, data: data.cashflow.revenue, itemStyle: { color: '#e06a3d', borderRadius: [3, 3, 0, 0] } },
        { name: '退款', type: 'bar', barMaxWidth: 14, data: data.cashflow.refund, itemStyle: { color: '#d9503f', borderRadius: [3, 3, 0, 0] } }
      ]
    }, true)
  }
  if (key === 'refunds') {
    instance.setOption({
      tooltip: { trigger: 'item' },
      legend: { bottom: 8, textStyle: { color: '#7a726c' } },
      series: [{
        type: 'pie',
        radius: ['46%', '70%'],
        center: ['50%', '44%'],
        label: { color: '#7a726c' },
        data: data.refunds.map(item => ({ value: item.value, name: item.name })),
        itemStyle: { borderColor: '#fff', borderWidth: 3 }
      }]
    }, true)
  }
}

function renderAll () {
  charts.forEach(chart => renderChart(chart.key))
}

function resize () {
  Object.values(chartInstances).forEach(instance => instance.resize())
}

async function load () {
  loading.value = true
  try {
    const [beginDate, endDate] = range.value || defaultRange()
    const response = await getFinanceDashboard({ beginDate, endDate })
    const payload = response.data || {}
    data.days = payload.days || []
    data.summary = payload.summary || {}
    data.sales = payload.sales || []
    data.orders = payload.orders || []
    data.newUsers = payload.newUsers || []
    data.activeUsers = payload.activeUsers || []
    data.cashflow = payload.cashflow || { revenue: [], refund: [] }
    data.refunds = payload.refunds || []
    await nextTick()
    renderAll()
  } catch (e) {
    ElMessage.error(e?.message || '数据看板加载失败')
  } finally {
    loading.value = false
  }
}

watch(() => data.days.length, () => nextTick(renderAll))

onMounted(() => {
  load()
  window.addEventListener('resize', resize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', resize)
  Object.values(chartInstances).forEach(instance => instance.dispose())
})
</script>

<style scoped>
.finance-page { min-height: calc(100vh - 112px); color: var(--el-text-color-primary); }
.summary-panel, .chart-panel { border: 1px solid var(--el-border-color-light); border-radius: 10px; background: var(--el-bg-color-overlay); box-shadow: 0 8px 24px rgba(0, 0, 0, .08); }
.summary-panel { padding: 22px 24px 24px; }
.section-heading, .panel-heading { display: flex; align-items: flex-start; justify-content: space-between; gap: 16px; }
.section-heading h2 { margin: 0; font-size: 20px; font-weight: 600; }
.section-heading p { margin: 7px 0 0; color: var(--el-text-color-secondary); font-size: 13px; }
.range-picker { display: flex; align-items: center; gap: 10px; }
.summary-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 18px; margin-top: 26px; }
.summary-item { display: flex; min-width: 0; align-items: center; gap: 13px; padding: 16px; border: 1px solid var(--el-border-color-light); border-radius: 8px; background: color-mix(in srgb, var(--el-bg-color) 70%, transparent); }
.summary-icon { display: grid; flex: 0 0 42px; width: 42px; height: 42px; place-items: center; border-radius: 12px; color: #fff; font-size: 20px; }
.summary-icon.blue { background: #168bd0; }.summary-icon.orange { background: #df8d23; }.summary-icon.red { background: #df536b; }.summary-icon.green { background: #1ba56f; }
.summary-copy { min-width: 0; }.summary-copy strong { display: block; font-size: 24px; font-weight: 600; }.summary-copy span { display: block; margin-top: 5px; overflow: hidden; color: var(--el-text-color-secondary); font-size: 12px; text-overflow: ellipsis; white-space: nowrap; }
.chart-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 16px; margin-top: 16px; }
.chart-panel { min-width: 0; padding: 18px 20px 14px; }
.panel-heading h3 { margin: 0; font-size: 15px; font-weight: 600; }.panel-heading span { display: block; margin-top: 5px; color: var(--el-text-color-secondary); font-size: 12px; }
.chart { height: 285px; margin-top: 7px; }
@media (max-width: 1000px) { .summary-grid { grid-template-columns: repeat(2, 1fr); } .section-heading { flex-direction: column; } }
@media (max-width: 680px) { .summary-grid, .chart-grid { grid-template-columns: 1fr; }.summary-panel { padding: 18px; }.chart { height: 250px; } }
</style>
