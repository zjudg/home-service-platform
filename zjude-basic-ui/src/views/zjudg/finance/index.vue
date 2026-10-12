<template>
  <div class="app-container finance-page">
    <section class="summary-panel">
      <div class="section-heading">
        <div>
          <h2>财务中心</h2>
          <p>查看家政业务的经营、订单和退款概况</p>
        </div>
        <el-tag type="info">
          数据暂未接入
        </el-tag>
      </div>
      <div class="summary-grid">
        <div
          v-for="item in summary"
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
            近15日
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
<script setup>
import { nextTick, onBeforeUnmount, onMounted } from 'vue'; import { Tickets, TrendCharts, Wallet, WarningFilled } from '@element-plus/icons-vue'; import * as echarts from 'echarts'

const summary = [
  { label: '累计营业额（元）', value: '0.00', icon: Wallet, color: 'blue' },
  { label: '累计成交订单（笔）', value: '0', icon: Tickets, color: 'orange' },
  { label: '已退款金额（元）', value: '0.00', icon: WarningFilled, color: 'red' },
  { label: '待退款金额（元）', value: '0.00', icon: TrendCharts, color: 'green' }
]
const charts = [
  { key: 'sales', title: '销售金额趋势', description: '每日成交金额' },
  { key: 'orders', title: '成交订单趋势', description: '每日完成订单数' },
  { key: 'cashflow', title: '营业额与退款对比', description: '每日收入和退款金额' },
  { key: 'refunds', title: '退款状态分布', description: '退款记录状态占比' }
]
const chartRefs = {}; const chartInstances = {}
function setChartRef (key, element) { if (element) chartRefs[key] = element }
function days () {
  return Array.from({ length: 15 }, (_, index) => {
    const date = new Date()
    date.setDate(date.getDate() - (14 - index))
    return `${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
  })
}
function lineOption (name, color, values) {
  return { tooltip: { trigger: 'axis' }, grid: { left: 46, right: 22, top: 24, bottom: 30 }, xAxis: { type: 'category', boundaryGap: false, data: days(), axisLabel: { color: '#8fa6b8' }, axisLine: { lineStyle: { color: '#35526a' } } }, yAxis: { type: 'value', min: 0, minInterval: 1, axisLabel: { color: '#8fa6b8' }, splitLine: { lineStyle: { color: 'rgba(125, 211, 252, .12)' } } }, series: [{ name, type: 'line', smooth: true, symbol: 'circle', symbolSize: 5, data: values, lineStyle: { color, width: 2 }, itemStyle: { color }, areaStyle: { color: `${color}18` } }] }
}
function renderChart (key) {
  const element = chartRefs[key]; if (!element) return
  const instance = echarts.init(element); chartInstances[key] = instance
  if (key === 'sales') instance.setOption(lineOption('销售金额', '#38bdf8', Array(15).fill(0)))
  if (key === 'orders') instance.setOption(lineOption('订单量', '#34d399', Array(15).fill(0)))
  if (key === 'cashflow') instance.setOption({ tooltip: { trigger: 'axis' }, legend: { data: ['营业额', '退款'], textStyle: { color: '#9ab0c0' } }, grid: { left: 46, right: 22, top: 42, bottom: 30 }, xAxis: { type: 'category', data: days(), axisLabel: { color: '#8fa6b8' }, axisLine: { lineStyle: { color: '#35526a' } } }, yAxis: { type: 'value', min: 0, minInterval: 1, axisLabel: { color: '#8fa6b8' }, splitLine: { lineStyle: { color: 'rgba(125, 211, 252, .12)' } } }, series: [{ name: '营业额', type: 'bar', barMaxWidth: 14, data: Array(15).fill(0), itemStyle: { color: '#38bdf8', borderRadius: [3, 3, 0, 0] } }, { name: '退款', type: 'bar', barMaxWidth: 14, data: Array(15).fill(0), itemStyle: { color: '#fb7185', borderRadius: [3, 3, 0, 0] } }] })
  if (key === 'refunds') instance.setOption({ tooltip: { trigger: 'item' }, legend: { bottom: 8, textStyle: { color: '#9ab0c0' } }, series: [{ type: 'pie', radius: ['46%', '70%'], center: ['50%', '44%'], label: { color: '#b9cbd6' }, data: [{ value: 0, name: '退款中' }, { value: 0, name: '退款成功' }, { value: 0, name: '退款失败' }], itemStyle: { borderColor: '#0a263b', borderWidth: 3 } }] })
}
function render () { charts.forEach(chart => renderChart(chart.key)); window.addEventListener('resize', resize) }
function resize () { Object.values(chartInstances).forEach(instance => instance.resize()) }
onMounted(() => nextTick(render)); onBeforeUnmount(() => { window.removeEventListener('resize', resize); Object.values(chartInstances).forEach(instance => instance.dispose()) })
</script>
<style scoped>
.finance-page { min-height: calc(100vh - 112px); color: var(--el-text-color-primary); }
.summary-panel, .chart-panel { border: 1px solid var(--el-border-color-light); border-radius: 10px; background: var(--el-bg-color-overlay); box-shadow: 0 8px 24px rgba(0, 0, 0, .08); }
.summary-panel { padding: 22px 24px 24px; }
.section-heading, .panel-heading { display: flex; align-items: flex-start; justify-content: space-between; gap: 16px; }
.section-heading h2 { margin: 0; font-size: 20px; font-weight: 600; }
.section-heading p { margin: 7px 0 0; color: var(--el-text-color-secondary); font-size: 13px; }
.summary-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 18px; margin-top: 26px; }
.summary-item { display: flex; min-width: 0; align-items: center; gap: 13px; padding: 16px; border: 1px solid var(--el-border-color-light); border-radius: 8px; background: color-mix(in srgb, var(--el-bg-color) 70%, transparent); }
.summary-icon { display: grid; flex: 0 0 42px; width: 42px; height: 42px; place-items: center; border-radius: 12px; color: #fff; font-size: 20px; }
.summary-icon.blue { background: #168bd0; }.summary-icon.orange { background: #df8d23; }.summary-icon.red { background: #df536b; }.summary-icon.green { background: #1ba56f; }
.summary-copy { min-width: 0; }.summary-copy strong { display: block; font-size: 24px; font-weight: 600; }.summary-copy span { display: block; margin-top: 5px; overflow: hidden; color: var(--el-text-color-secondary); font-size: 12px; text-overflow: ellipsis; white-space: nowrap; }
.chart-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 16px; margin-top: 16px; }
.chart-panel { min-width: 0; padding: 18px 20px 14px; }
.panel-heading h3 { margin: 0; font-size: 15px; font-weight: 600; }.panel-heading span { display: block; margin-top: 5px; color: var(--el-text-color-secondary); font-size: 12px; }
.chart { height: 285px; margin-top: 7px; }
@media (max-width: 1000px) { .summary-grid { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 680px) { .summary-grid, .chart-grid { grid-template-columns: 1fr; }.summary-panel { padding: 18px; }.chart { height: 250px; } }
</style>
