<template>
  <view class="city-page page safe-bottom">
    <view class="city-search"><uni-icons type="search" size="17" color="#9ca9a5" /><input v-model="keyword" placeholder="请输入城市名称" confirm-type="search" /></view>
    <view class="current-city"><text>当前定位城市</text><view class="current-value"><uni-icons type="location" size="15" color="#2e806d" />{{ currentCity }}<text class="relocate" @click="locate">重新定位</text></view></view>
    <view v-if="hotCities.length" class="city-section"><text class="section-label">热门城市</text><view class="hot-city-grid"><text v-for="city in hotCities" :key="city.cityId" @click="selectCity(city.cityName)">{{ city.cityName }}</text></view></view>
    <view v-for="group in filteredGroups" :key="group.letter" class="city-section"><text class="letter">{{ group.letter }}</text><view class="city-grid"><text v-for="city in group.cities" :key="city" @click="selectCity(city)">{{ city }}</text></view></view>
    <view v-if="!filteredGroups.length" class="empty-city">没有找到相关城市</view>
    <view class="letters"><text v-for="letter in letters" :key="letter">{{ letter }}</text></view>
  </view>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import uniIcons from '@dcloudio/uni-ui/lib/uni-icons/uni-icons.vue'
import { getMiniCities } from '@/api/mini'
const keyword = ref(''); const currentCity = ref('汉中市'); const cities = ref([]); const loading = ref(false)
const hotNames = ['北京市', '上海市', '广州市', '深圳市', '重庆市', '成都市', '杭州市', '武汉市', '西安市']
const hotCities = computed(() => hotNames.map(name => cities.value.find(city => city.cityName === name)).filter(Boolean))
const letters = computed(() => [...new Set(cities.value.map(city => city.initialLetter).filter(Boolean))])
const filteredGroups = computed(() => { const value = keyword.value.trim(); const filtered = value ? cities.value.filter(city => `${city.cityName}${city.provinceName}`.includes(value)) : cities.value; const grouped = new Map(); filtered.forEach(city => { const letter = city.initialLetter || '#'; if (!grouped.has(letter)) grouped.set(letter, []); grouped.get(letter).push(city.cityName) }); return [...grouped.entries()].map(([letter, cityList]) => ({ letter, cities: cityList })) })
onMounted(async () => { const options = getCurrentPages().at(-1)?.options || {}; currentCity.value = options.current ? decodeURIComponent(options.current) : currentCity.value; loading.value = true; try { const response = await getMiniCities(); cities.value = response?.data || response?.rows || [] } catch (error) { uni.showToast({ title: error.message || '城市数据加载失败', icon: 'none' }) } finally { loading.value = false } })
function selectCity (city) { currentCity.value = city; uni.$emit('city-changed', city); uni.navigateBack() }
function locate () { uni.showToast({ title: '已定位到汉中市', icon: 'none' }); currentCity.value = '汉中市' }
</script>

<style lang="scss" scoped>
.city-page{background:$app-bg;padding:18rpx 24rpx 40rpx}.city-search{display:flex;align-items:center;gap:10rpx;height:68rpx;padding:0 18rpx;border-radius:8rpx;background:#fff}.city-search input{flex:1;color:#344740;font-size:23rpx}.current-city{display:flex;align-items:center;justify-content:space-between;padding:22rpx 0;color:#7d8d88;font-size:21rpx}.current-value{display:flex;align-items:center;gap:5rpx;color:#365750}.relocate{margin-left:26rpx;color:$app-primary;font-size:20rpx}.city-section{margin-top:10rpx}.section-label,.letter{display:block;color:#7d8d88;font-size:21rpx}.hot-city-grid,.city-grid{display:grid;grid-template-columns:repeat(3,1fr);margin-top:10rpx;background:#fff}.hot-city-grid text,.city-grid text{padding:20rpx 6rpx;color:#3c504b;font-size:21rpx;text-align:center;border-right:1rpx solid #f1f3f4;border-bottom:1rpx solid #f1f3f4}.letter{margin:24rpx 0 8rpx}.empty-city{padding:100rpx 0;color:#9ca9a5;text-align:center}.letters{position:fixed;right:8rpx;top:240rpx;display:flex;flex-direction:column;align-items:center;gap:8rpx;color:#5d857a;font-size:17rpx}
</style>
