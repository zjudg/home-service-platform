<template>
  <view class="master-profile has-tabbar">
    <UiNavbar
      :back="false"
      title="我的"
    />

    <view class="profile-body">
      <AppStatus
        v-if="loading"
        loading
        loading-text="正在加载"
      />

      <template v-else>
        <view class="card head-card">
          <view class="avatar">
            <image
              v-if="avatarUrl"
              :src="assetUrl(avatarUrl)"
              mode="aspectFill"
            />
            <text v-else>
              {{ initials }}
            </text>
          </view>
          <view class="head-copy">
            <text class="head-name">
              {{ masterStore.profile?.masterName || '未入驻' }}
            </text>
            <text class="head-status">
              {{ masterStore.statusText }}
            </text>
          </view>
        </view>

        <view
          v-if="masterStore.isApproved"
          class="card stats-card"
        >
          <view class="stat">
            <text class="stat-value">
              {{ masterStore.profile?.rating || '0.0' }}
            </text>
            <text class="stat-label">
              服务评分
            </text>
          </view>
          <view class="stat-divider" />
          <view class="stat">
            <text class="stat-value">
              {{ masterStore.profile?.orderCount || 0 }}
            </text>
            <text class="stat-label">
              累计完单
            </text>
          </view>
          <view class="stat-divider" />
          <view class="stat">
            <text class="stat-value">
              {{ masterStore.accepting ? '接单中' : '已停接' }}
            </text>
            <text class="stat-label">
              接单状态
            </text>
          </view>
        </view>

        <view class="card entry-card">
          <view
            class="entry-row"
            @click="goApply"
          >
            <uni-icons
              type="compose"
              size="18"
              color="#e06a3d"
            />
            <text class="entry-text">
              {{ masterStore.applied ? '修改入驻资料' : '申请成为师傅' }}
            </text>
            <uni-icons
              type="right"
              size="14"
              color="#c4bcb6"
            />
          </view>
          <view
            v-if="masterStore.isApproved"
            class="entry-row"
            @click="toggleAccepting"
          >
            <uni-icons
              type="sound"
              size="18"
              color="#4a7fb5"
            />
            <text class="entry-text">
              {{ masterStore.accepting ? '暂停接单' : '开启接单' }}
            </text>
            <uni-icons
              type="right"
              size="14"
              color="#c4bcb6"
            />
          </view>
          <view
            v-if="masterStore.isApproved"
            class="entry-row"
            @click="openWallet"
          >
            <uni-icons
              type="wallet"
              size="18"
              color="#d98a1f"
            />
            <text class="entry-text">
              我的收入
            </text>
            <uni-icons
              type="right"
              size="14"
              color="#c4bcb6"
            />
          </view>
          <view
            class="entry-row"
            @click="openSupport"
          >
            <uni-icons
              type="phone"
              size="18"
              color="#4a7fb5"
            />
            <text class="entry-text">
              联系客服
            </text>
            <uni-icons
              type="right"
              size="14"
              color="#c4bcb6"
            />
          </view>
          <view
            class="entry-row entry-row--last"
            @click="backToUser"
          >
            <uni-icons
              type="home"
              size="18"
              color="#7a726c"
            />
            <text class="entry-text">
              切换到用户端
            </text>
            <uni-icons
              type="right"
              size="14"
              color="#c4bcb6"
            />
          </view>
          <view
            class="entry-row entry-row--last entry-row--danger"
            @click="logout"
          >
            <uni-icons
              type="contact"
              size="18"
              color="#d9503f"
            />
            <text class="entry-text">
              退出登录
            </text>
            <uni-icons
              type="right"
              size="14"
              color="#c4bcb6"
            />
          </view>
        </view>
      </template>
    </view>

    <AppTabBar
      current-key="master-profile"
      :items="tabItems"
    />
    <AppFeedback />
    <ContactSheet
      v-model:visible="contactVisible"
      :phone="supportPhone"
      :hours="supportHours"
      :qr-code="supportQrCode"
    />
  </view>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import uniIcons from '@dcloudio/uni-ui/lib/uni-icons/uni-icons.vue'
import UiNavbar from '@/components/ui/UiNavbar.vue'
import AppTabBar from '@/components/AppTabBar.vue'
import AppFeedback from '@/components/AppFeedback.vue'
import ContactSheet from '@/components/ContactSheet.vue'
import AppStatus from '@/components/AppStatus.vue'
import { useAuthStore } from '@/stores/auth'
import { useMasterStore } from '@/stores/master'
import { useConfigStore } from '@/stores/config'
import { MASTER_TAB_ITEMS } from '@/constants/master-navigation'
import { MINI_PROGRAM_MODE_CUSTOMER, setMiniProgramMode } from '@/utils/mini-program-mode'
import { assetUrl } from '@/utils/assets'
import { showDialog, showToast } from '@/utils/feedback'

const masterStore = useMasterStore()
const authStore = useAuthStore()
const configStore = useConfigStore()
const loading = ref(false)
const contactVisible = ref(false)

const tabItems = MASTER_TAB_ITEMS

const initials = computed(() => (masterStore.profile?.masterName || '师').slice(0, 1))
const avatarUrl = computed(() =>
  authStore.user?.avatar || authStore.user?.avatarUrl || masterStore.profile?.avatarUrl || ''
)
const supportPhone = computed(() => String(configStore.site?.servicePhone || '').trim())
const supportHours = computed(() => String(configStore.site?.serviceHours || '').trim())
const supportQrCode = computed(() => assetUrl(configStore.site?.serviceQrCode || ''))

function goApply () {
  uni.navigateTo({ url: '/pages/master/apply/index' })
}

function openWallet () {
  uni.navigateTo({ url: '/pages/master/wallet/index' })
}

async function toggleAccepting () {
  const next = masterStore.accepting ? '0' : '1'
  try {
    await masterStore.toggleAccepting(next)
    showToast(next === '1' ? '已开启接单' : '已暂停接单', { type: 'success' })
  } catch (e) {
    showToast(e.message || '操作失败')
  }
}

function backToUser () {
  setMiniProgramMode(MINI_PROGRAM_MODE_CUSTOMER)
  uni.reLaunch({ url: '/pages/home/index' })
}

function openSupport () {
  contactVisible.value = true
}

async function logout () {
  const confirmed = await showDialog({
    title: '退出登录',
    content: '确定要退出当前师傅账号吗？',
    confirmText: '退出登录',
    danger: true
  })
  if (!confirmed) return
  try {
    await authStore.signOut()
  } catch (error) {
    // 本地会话由 signOut 的 finally 清除；网络异常不阻止退出。
  }
  masterStore.reset()
  setMiniProgramMode('')
  uni.reLaunch({ url: '/pages/user/index' })
}

onMounted(async () => {
  loading.value = true
  try {
    await Promise.all([
      masterStore.loadProfile(),
      configStore.load().catch(() => {})
    ])
  } finally {
    loading.value = false
  }
})
</script>

<style lang="scss" scoped>
@import '@/styles/tokens.scss';

.master-profile { min-height: 100vh; background: $bg; }

.profile-body { padding: $space-3 $page-gutter $space-8; }

.head-card { display: flex; align-items: center; padding: $space-4; }

.avatar {
  width: 112rpx; height: 112rpx; border-radius: 50%; overflow: hidden;
  background: $brand-100; color: $brand-600;
  display: flex; align-items: center; justify-content: center;
  font-size: $font-display; font-weight: $weight-bold;
  image { width: 100%; height: 100%; }
}

.head-copy { margin-left: $space-3; }

.head-name { display: block; font-size: $font-title; font-weight: $weight-semibold; color: $ink-900; }

.head-status { display: block; margin-top: 8rpx; font-size: $font-caption; color: $ink-500; }

.stats-card {
  display: flex; align-items: center; padding: $space-4; margin-top: $space-3;
}

.stat { flex: 1; display: flex; flex-direction: column; align-items: center; }

.stat-divider { width: 2rpx; height: 56rpx; background: $line-soft; }

.stat-value { font-size: $font-title; font-weight: $weight-bold; color: $ink-900; }

.stat-label { margin-top: 8rpx; font-size: $font-micro; color: $ink-400; }

.entry-card { margin-top: $space-3; padding: 0 $space-4; }

.entry-row {
  display: flex; align-items: center; gap: $space-3;
  padding: $space-3 0; border-bottom: 2rpx solid $line-soft;
  &--last { border-bottom: none; }
}

.entry-text { flex: 1; font-size: $font-body; color: $ink-700; }
.entry-row--danger .entry-text { color: $danger; }
</style>
