<template>
  <view class="favorite-page">
    <UiNavbar :back="true" title="我的收藏" />

    <view class="favorite-body">
      <UiSkeleton v-if="loading && !loaded" variant="card-list" :count="3" />

      <UiEmpty
        v-else-if="!list.length"
        icon="heart"
        title="还没有收藏服务"
        description="在服务详情页点击收藏，方便下次快速预约"
      />

      <view
        v-for="item in list"
        :key="item.favoriteId"
        class="favorite-card card"
        :class="{ 'favorite-card--off': item.serviceStatus !== '0' }"
        @click="openService(item)"
      >
        <view class="favorite-cover">
          <image
            v-if="coverOf(item)"
            :src="assetUrl(coverOf(item))"
            mode="aspectFit"
          />
          <view
            v-else
            class="favorite-cover__fallback"
          >
            <uni-icons type="home" size="22" color="#f9d5c5" />
          </view>
        </view>

        <view class="favorite-copy">
          <text class="favorite-name">{{ item.serviceName || '服务已下架' }}</text>
          <text class="favorite-intro">{{ introOf(item) }}</text>
          <view class="favorite-foot">
            <UiPrice
              v-if="item.serviceStatus === '0'"
              :value="item.price"
              :unit="item.unitName || '次'"
              size="sm"
            />
            <text v-else class="favorite-off">已下架</text>
          </view>
        </view>

        <view
          class="favorite-remove"
          @click.stop="remove(item)"
        >
          <uni-icons type="closeempty" size="16" color="#a39b95" />
        </view>
      </view>
    </view>

    <AppFeedback />
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import UiNavbar from '@/components/ui/UiNavbar.vue'
import UiEmpty from '@/components/ui/UiEmpty.vue'
import UiSkeleton from '@/components/ui/UiSkeleton.vue'
import UiPrice from '@/components/ui/UiPrice.vue'
import AppFeedback from '@/components/AppFeedback.vue'
import { listFavorites, removeFavorite } from '@/api/favorite'
import { assetUrl } from '@/utils/assets'
import { currentPageUrl, ensureLoggedIn } from '@/utils/guard'
import { showDialog, showToast, toastSuccess } from '@/utils/feedback'

const list = ref([])
const loading = ref(false)
const loaded = ref(false)

onShow(async () => {
  if (!ensureLoggedIn(currentPageUrl())) return
  await load()
})

async function load () {
  loading.value = true
  try {
    const response = await listFavorites()
    list.value = response?.data || []
  } catch (e) {
    showToast(e.message || '收藏加载失败')
  } finally {
    loading.value = false
    loaded.value = true
  }
}

function coverOf (item) {
  const images = item?.imageUrl || ''
  return (Array.isArray(images) ? images : String(images).split(',')).map(value => String(value).trim()).find(Boolean) || ''
}

function introOf (item) {
  if (item.serviceStatus !== '0') return '该服务已下架，可取消收藏'
  return item.serviceIntro || '标准上门服务流程'
}

function openService (item) {
  if (!item.serviceId || item.serviceStatus !== '0') return
  uni.navigateTo({ url: `/pages/service-detail/index?id=${encodeURIComponent(item.serviceId)}` })
}

async function remove (item) {
  const confirmed = await showDialog({
    title: '取消收藏',
    content: `确定不再收藏「${item.serviceName || '该服务'}」吗？`,
    confirmText: '取消收藏',
    danger: true
  })
  if (!confirmed) return
  try {
    await removeFavorite(item.serviceId)
    list.value = list.value.filter(row => row.favoriteId !== item.favoriteId)
    toastSuccess('已取消收藏')
  } catch (e) {
    showToast(e.message || '操作失败')
  }
}
</script>

<style lang="scss" scoped>
@import '@/styles/tokens.scss';

.favorite-page { min-height: 100vh; background: $bg; }

.favorite-body { padding: 0 $page-gutter $space-8; }

.favorite-card {
  position: relative;
  display: flex;
  gap: $space-3;
  padding: $space-3;
  margin-bottom: $space-3;
}
.favorite-card:active { opacity: 0.9; }
.favorite-card--off { opacity: 0.6; }

.favorite-cover {
  flex: 0 0 160rpx;
  width: 160rpx;
  height: 160rpx;
  overflow: hidden;
  border-radius: $radius-md;
  background: $brand-500;
}
.favorite-cover image { width: 100%; height: 100%; }

.favorite-cover__fallback {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
}

.favorite-copy {
  display: flex;
  flex: 1;
  min-width: 0;
  flex-direction: column;
}

.favorite-name {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: $font-body;
  font-weight: $weight-semibold;
  color: $ink-900;
}

.favorite-intro {
  margin-top: 8rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  font-size: $font-micro;
  color: $ink-400;
  line-height: 1.5;
}

.favorite-foot { margin-top: auto; }

.favorite-off { font-size: $font-micro; color: $ink-400; }

.favorite-remove { padding: 4rpx; }
</style>
