<template>
  <view class="city-page">
    <UiNavbar
      :back="true"
      title="选择城市"
    />

    <view class="search-field">
      <uni-icons
        type="search"
        size="16"
        color="#a39b95"
      />
      <input
        v-model="keyword"
        class="search-input"
        placeholder="输入城市名称"
        placeholder-class="search-placeholder"
        confirm-type="search"
      >
    </view>

    <view class="current-card card">
      <view class="current-copy">
        <text class="current-label">
          当前定位
        </text>
        <text class="current-city">
          {{ currentCity }}
        </text>
      </view>
      <view
        class="relocate"
        :class="{ 'relocate--disabled': locating || loading }"
        @click="locate"
      >
        <uni-icons
          type="location-filled"
          size="14"
          color="#e06a3d"
        />
        <text>{{ locating ? '定位中…' : '重新定位' }}</text>
      </view>
    </view>

    <view
      v-if="hotCities.length"
      class="city-section"
    >
      <text class="section-label">
        热门城市
      </text>
      <view class="hot-grid">
        <view
          v-for="city in hotCities"
          :key="city.cityId"
          class="hot-city"
          @click="selectCity(city)"
        >
          {{ city.cityName }}
        </view>
      </view>
    </view>

    <view
      v-for="group in filteredGroups"
      :key="group.letter"
      class="city-section"
    >
      <text class="letter">
        {{ group.letter }}
      </text>
      <view class="city-list card">
        <view
          v-for="city in group.cities"
          :key="city.cityId"
          class="city-row"
          :class="{ 'city-row--active': city.cityName === currentCity }"
          @click="selectCity(city)"
        >
          <text>{{ city.cityName }}</text>
          <uni-icons
            v-if="city.cityName === currentCity"
            type="checkmarkempty"
            size="16"
            color="#e06a3d"
          />
        </view>
      </view>
    </view>

    <view
      v-if="!filteredGroups.length && !loading"
      class="city-empty"
    >
      <text>没有找到相关城市</text>
    </view>
  </view>

  <AppFeedback />
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import uniIcons from '@dcloudio/uni-ui/lib/uni-icons/uni-icons.vue'
import UiNavbar from '@/components/ui/UiNavbar.vue'
import AppFeedback from '@/components/AppFeedback.vue'
import { listCities, reverseGeocodeLocation } from '@/api/service'
import { showToast } from '@/utils/feedback'
import { getCurrentCoordinates } from '@/utils/location'
import { useCityStore } from '@/stores/city'

const cityStore = useCityStore()
const keyword = ref('')
const currentCity = ref('')
const cities = ref([])
const loading = ref(true)
const locating = ref(false)

const hotCities = computed(() => cities.value.filter(city => city.isHot === 'Y'))
const filteredGroups = computed(() => {
  const value = keyword.value.trim()
  const filtered = value ? cities.value.filter(city => `${city.cityName}${city.provinceName}`.includes(value)) : cities.value
  const grouped = new Map()
  filtered.forEach(city => {
    const letter = city.initialLetter
    if (!grouped.has(letter)) grouped.set(letter, [])
    grouped.get(letter).push(city)
  })
  return [...grouped.entries()].sort((a, b) => a[0].localeCompare(b[0])).map(([letter, cityList]) => ({ letter, cities: cityList }))
})

onMounted(async () => {
  const options = getCurrentPages().at(-1)?.options || {}
  await cityStore.initialize()
  currentCity.value = options.current ? decodeURIComponent(options.current) : cityStore.cityName
  loading.value = true
  try {
    const response = await listCities()
    cities.value = response.data
  } catch (error) {
    const message = error?.message || error?.errMsg || error?.msg
    if (message) showToast(message)
  } finally {
    loading.value = false
  }
})

function selectCity (city) {
  currentCity.value = city.cityName
  cityStore.select(city)
  uni.navigateBack()
}

async function locate () {
  if (locating.value || loading.value) return
  locating.value = true
  try {
    const coordinates = await getCurrentCoordinates()
    const response = await reverseGeocodeLocation(coordinates)
    const city = response.data

    currentCity.value = city.cityName
    cityStore.select(city)
    uni.navigateBack()
  } catch (error) {
    const message = error?.message || error?.errMsg || error?.msg
    if (message) showToast(message)
  } finally {
    locating.value = false
  }
}
</script>

<style lang="scss" scoped>
@import '@/styles/tokens.scss';

.city-page {
  min-height: 100vh;
  padding: 0 $page-gutter $space-6;
  background: $bg;
}

.city-head {
  margin: 0 (-$page-gutter);
  padding: $space-4 $page-gutter $space-6;
  background: linear-gradient(180deg, $brand-100 0%, $brand-50 100%);
}

.head-title {
  display: block;
  font-size: 42rpx;
  font-weight: $weight-heavy;
  color: $ink-900;
  letter-spacing: -0.5rpx;
}

.head-sub { display: block; margin-top: 6rpx; font-size: $font-caption; color: $ink-500; }

.search-field {
  display: flex;
  align-items: center;
  gap: $space-2;
  height: 84rpx;
  margin-top: $space-3;
  padding: 0 $space-3;
  border-radius: $radius-pill;
  background: $surface;
  box-shadow: $shadow-card;
}

.search-input { flex: 1; min-width: 0; font-size: $font-caption; color: $ink-900; }
.search-placeholder { color: $ink-400; }

.current-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: $space-3;
  padding: $space-3 $space-4;
}

.current-copy { display: flex; flex-direction: column; }

.current-label { font-size: $font-micro; color: $ink-400; }
.current-city { margin-top: 4rpx; font-size: $font-body; font-weight: $weight-semibold; color: $ink-900; }

.relocate {
  display: flex;
  align-items: center;
  gap: 4rpx;
  padding: 0 $space-2;
  height: 56rpx;
  border-radius: $radius-pill;
  background: $brand-50;
  font-size: $font-micro;
  color: $brand-600;
}
.relocate--disabled { opacity: 0.55; }

.city-section { margin-top: $space-4; }

.section-label { display: block; margin-bottom: $space-2; font-size: $font-micro; color: $ink-400; }

.hot-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: $space-1; }

.hot-city {
  height: 76rpx;
  line-height: 76rpx;
  border-radius: $radius-md;
  background: $surface;
  text-align: center;
  font-size: $font-caption;
  color: $ink-700;
}
.hot-city:active { background: $brand-50; color: $brand-600; }

.letter { display: block; margin-bottom: $space-1; padding-left: $space-1; font-size: $font-micro; font-weight: $weight-semibold; color: $ink-400; }

.city-list { padding: 0 $space-4; }

.city-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: $space-3 0;
  font-size: $font-caption;
  color: $ink-700;
  border-bottom: 1rpx solid $line-soft;
}
.city-row:last-child { border-bottom: 0; }
.city-row--active { color: $brand-600; font-weight: $weight-semibold; }

.city-empty { padding: $space-8 0; text-align: center; font-size: $font-caption; color: $ink-400; }
</style>
