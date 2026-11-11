<template>
  <view class="address-list-page">
    <UiNavbar :back="true" title="服务地址" />

    <view
      class="address-body"
      :class="{ 'address-body--empty': !addressStore.loading && addressStore.isEmpty }"
    >
      <view v-if="!addressStore.isEmpty" class="address-intro">
        <text class="address-intro__title">常用服务地址</text>
        <text class="address-intro__hint">{{ selectMode ? '选择地址后将自动带入预约信息' : '管理常用地址，下单时可快速选择' }}</text>
      </view>

      <UiSkeleton v-if="addressStore.loading && !addressStore.loaded" variant="card-list" :count="2" />

      <UiEmpty
        v-else-if="addressStore.isEmpty"
        class="address-empty"
        icon="location"
        title="还没有服务地址"
        description="添加常用地址后，下单时可直接选用"
      >
        <button class="primary-button empty-add-button" @click="openForm()">
          添加地址
        </button>
      </UiEmpty>

      <template v-else>
        <view
          v-for="item in addressStore.list"
          :key="item.addressId"
          class="address-card card"
          @click="handleSelect(item)"
        >
          <view class="address-head">
            <view class="address-contact">
              <view class="address-pin">
                <uni-icons type="location-filled" size="18" color="#e06a3d" />
              </view>
              <view class="address-contact__copy">
                <view class="address-name-row">
                  <text class="address-name">{{ item.contactName || '联系人' }}</text>
                  <text v-if="item.isDefault === '1'" class="address-tag">默认地址</text>
                  <text v-else-if="item.label" class="address-tag address-tag--plain">{{ item.label }}</text>
                </view>
                <text class="address-phone">{{ item.contactPhone }}</text>
              </view>
            </view>
          </view>
          <view class="address-location">
            <text class="address-detail">{{ fullAddress(item) }}</text>
            <uni-icons v-if="selectMode" type="right" size="16" color="#c4bcb6" />
          </view>

          <view class="address-actions" @click.stop>
            <text
              v-if="item.isDefault !== '1'"
              class="action-link action-link--default"
              @click="makeDefault(item)"
            >
              设为默认
            </text>
            <text class="action-link" @click="openForm(item)">
              编辑
            </text>
            <text class="action-link action-link--danger" @click="remove(item)">
              删除
            </text>
          </view>
        </view>

        <button class="primary-button add-button" @click="openForm()">
          添加新地址
        </button>
      </template>
    </view>

    <AppFeedback />
  </view>
</template>

<script setup>
import { onShow } from '@dcloudio/uni-app'
import UiNavbar from '@/components/ui/UiNavbar.vue'
import UiEmpty from '@/components/ui/UiEmpty.vue'
import UiSkeleton from '@/components/ui/UiSkeleton.vue'
import uniIcons from '@dcloudio/uni-ui/lib/uni-icons/uni-icons.vue'
import AppFeedback from '@/components/AppFeedback.vue'
import { useAddressStore } from '@/stores/address'
import { currentPageUrl, ensureLoggedIn } from '@/utils/guard'
import { showDialog, showToast, toastSuccess } from '@/utils/feedback'
import { ref } from 'vue'

const addressStore = useAddressStore()
const selectMode = ref(false)

// 从编辑页返回或首次进入都要刷新，避免列表停留在旧数据
onShow(async () => {
  if (!ensureLoggedIn(currentPageUrl())) return
  const pages = getCurrentPages()
  selectMode.value = pages[pages.length - 2]?.route === 'pages/booking/index'
  await addressStore.refresh().catch(e => showToast(e.message || '地址加载失败'))
})

function fullAddress (item) {
  return `${item.region || ''}${item.detail || ''}` || '未填写详细地址'
}

function openForm (item) {
  const id = item?.addressId
  uni.navigateTo({ url: `/pages/address/edit/index${id ? `?id=${encodeURIComponent(id)}` : ''}` })
}

function handleSelect (item) {
  // 仅在下单页唤起地址簿时把选中结果回传；在「我的」里管理地址时点击卡片不跳转
  const pages = getCurrentPages()
  const previous = pages[pages.length - 2]
  if (!previous || previous.route !== 'pages/booking/index') return
  uni.$emit('address-selected', item)
  uni.navigateBack()
}

async function makeDefault (item) {
  try {
    await addressStore.setDefault(item.addressId)
    toastSuccess('已设为默认地址')
  } catch (e) {
    showToast(e.message || '设置失败')
  }
}

async function remove (item) {
  const confirmed = await showDialog({
    title: '删除地址',
    content: `确定删除「${item.contactName} ${fullAddress(item)}」吗？`,
    confirmText: '删除',
    danger: true
  })
  if (!confirmed) return
  try {
    await addressStore.remove(item.addressId)
    toastSuccess('已删除')
  } catch (e) {
    showToast(e.message || '删除失败')
  }
}
</script>

<style lang="scss" scoped>
@import '@/styles/tokens.scss';

.address-list-page { display: flex; min-height: 100vh; flex-direction: column; background: $bg; }

.address-body { flex: 1; padding: $space-2 $page-gutter $space-8; }
.address-body--empty { display: flex; align-items: center; justify-content: center; }
.address-empty { width: 100%; transform: translateY(-60rpx); }

.address-intro { display: flex; flex-direction: column; gap: 6rpx; margin-bottom: $space-3; padding: 0 4rpx; }
.address-intro__title { color: $ink-900; font-size: $font-title; font-weight: $weight-bold; }
.address-intro__hint { color: $ink-400; font-size: $font-micro; }

.address-card { margin-bottom: $space-3; padding: $space-3 $space-4 $space-2; border: 1rpx solid rgba(236, 230, 225, 0.75); }
.address-card:active { transform: scale(0.99); }

.address-contact { display: flex; align-items: center; gap: $space-2; }
.address-pin { display: flex; width: 64rpx; height: 64rpx; flex: 0 0 64rpx; align-items: center; justify-content: center; border-radius: 50%; background: $brand-50; }
.address-contact__copy { display: flex; min-width: 0; flex: 1; flex-direction: column; gap: 6rpx; }
.address-name-row { display: flex; align-items: center; gap: $space-2; }
.address-name { color: $ink-900; font-size: $font-body; font-weight: $weight-semibold; }

.address-phone { color: $ink-400; font-size: $font-micro; }

.address-tag {
  padding: 4rpx 12rpx;
  border-radius: $radius-sm;
  background: $brand-50;
  color: $brand-600;
  font-size: $font-micro;
}
.address-tag--plain { background: $bg-sunken; color: $ink-400; }

.address-location { display: flex; align-items: center; gap: $space-1; margin: $space-3 0 $space-2 80rpx; }
.address-detail {
  display: -webkit-box;
  flex: 1;
  overflow: hidden;
  color: $ink-700;
  font-size: $font-caption;
  line-height: 1.6;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.address-actions {
  display: flex;
  justify-content: flex-end;
  gap: $space-3;
  margin-top: $space-1;
  padding-top: $space-2;
  border-top: 1rpx solid $line-soft;
}

.action-link { padding: 8rpx 4rpx; color: $ink-500; font-size: $font-micro; }
.action-link--default { margin-right: auto; color: $brand-600; }
.action-link--danger { color: $danger; }

.empty-add-button,
.add-button { margin-top: $space-4; }

.empty-add-button {
  width: 320rpx;
  height: 96rpx;
  padding: 0 $space-4;
  line-height: 96rpx;
  font-size: 30rpx;
}
</style>
