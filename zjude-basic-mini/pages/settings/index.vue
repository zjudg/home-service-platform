<template>
  <view class="settings-page">
    <UiNavbar :back="true" title="设置" />

    <view class="settings-card card">
      <view class="setting-row" @click="showAbout">
        <view class="row-copy">
          <text class="setting-title">关于应用</text>
          <text class="setting-desc">{{ appStore.appName }} 基础移动端</text>
        </view>
        <uni-icons type="right" size="14" color="#c4bcb6" />
      </view>
      <view class="setting-row" @click="clearCache">
        <view class="row-copy">
          <text class="setting-title">清理缓存</text>
          <text class="setting-desc">清理本地临时数据</text>
        </view>
        <uni-icons type="right" size="14" color="#c4bcb6" />
      </view>
      <view class="setting-row setting-row--static">
        <view class="row-copy">
          <text class="setting-title">运行环境</text>
          <text class="setting-desc">{{ appStore.platform }}</text>
        </view>
        <text class="version">v1.0.0</text>
      </view>
    </view>

    <text class="footer">© {{ appStore.appName }}</text>
  </view>
  <AppFeedback />
</template>

<script setup>
import uniIcons from '@dcloudio/uni-ui/lib/uni-icons/uni-icons.vue'
import UiNavbar from '@/components/ui/UiNavbar.vue'
import AppFeedback from '@/components/AppFeedback.vue'
import { useAppStore } from '@/stores/app'
import { clearCache as clearLocalCache } from '@/utils/cache'
import { showDialog, showToast, toastSuccess } from '@/utils/feedback'

const appStore = useAppStore()

function showAbout () {
  showDialog({ title: '关于应用', content: `${appStore.appName} 通用移动端基础框架`, showCancel: false })
}

async function clearCache () {
  const confirmed = await showDialog({
    title: '清理缓存',
    content: '将清空本地表单草稿与临时数据，登录状态和所选城市会保留。',
    confirmText: '清理'
  })
  if (!confirmed) return
  try {
    const result = await clearLocalCache()
    if (result.cleared === 0) {
      showToast('没有可清理的缓存')
      return
    }
    toastSuccess(`已清理 ${result.cleared} 项缓存`)
  } catch (e) {
    showToast('清理失败，请稍后重试')
  }
}
</script>

<style lang="scss" scoped>
@import '@/styles/tokens.scss';

.settings-page {
  min-height: 100vh;
  padding: 0 $page-gutter $space-6;
  background: $bg;
}

.settings-head {
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

.settings-card {
  margin-top: $space-3;
  padding: 0 $space-4;
}

.setting-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: $space-3;
  padding: $space-4 0;
  border-bottom: 1rpx solid $line-soft;
}
.setting-row:last-child { border-bottom: 0; }
.setting-row:active { opacity: 0.75; }

.row-copy { display: flex; flex-direction: column; }

.setting-title { font-size: $font-caption; font-weight: $weight-semibold; color: $ink-900; }
.setting-desc { margin-top: 4rpx; font-size: $font-micro; color: $ink-400; }

.version { font-size: $font-micro; color: $ink-400; }

.footer {
  display: block;
  margin-top: $space-6;
  text-align: center;
  font-size: $font-micro;
  color: $ink-300;
}
</style>
