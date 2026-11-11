<template>
  <view class="master-home has-tabbar">
    <UiNavbar
      :back="false"
      title="师傅工作台"
    >
      <template #right>
        <view
          class="back-entry"
          @click="backToUser"
        >
          <uni-icons
            type="home"
            size="18"
            color="#7a726c"
          />
        </view>
      </template>
    </UiNavbar>

    <view class="home-body">
      <view
        v-if="!loaded"
        class="state-block"
      >
        <UiSkeleton
          variant="detail"
          :count="2"
        />
      </view>

      <template v-else-if="!masterStore.isApproved">
        <view class="card profile-card">
          <view class="profile-head">
            <view class="avatar">
              {{ initials }}
            </view>
            <view class="profile-copy">
              <text class="profile-name">
                {{ masterStore.profile?.masterName || '待完善资料' }}
              </text>
              <text
                class="profile-status"
                :class="`profile-status--${masterStore.auditStatus || 'none'}`"
              >
                {{ masterStore.statusText }}
              </text>
            </view>
          </view>

          <view
            v-if="masterStore.isRejected"
            class="reject-box"
          >
            <text class="reject-label">
              驳回原因
            </text>
            <text class="reject-text">
              {{ masterStore.profile?.auditRemark || '平台未填写原因，请联系客服' }}
            </text>
          </view>
          <view
            v-else-if="masterStore.isPending"
            class="pending-box"
          >
            <text class="pending-text">
              资料已提交，平台将在 1-2 个工作日内完成审核。审核通过后即可开始接单。
            </text>
          </view>
          <view
            v-else
            class="pending-box"
          >
            <text class="pending-text">
              完成实名与资质认证后即可接单，平台会核验您的服务能力。
            </text>
          </view>

          <button
            class="primary-button"
            @click="goApply"
          >
            {{ masterStore.applied ? '修改入驻资料' : '立即申请成为师傅' }}
          </button>
        </view>
      </template>

      <template v-else>
        <view class="card profile-card">
          <view class="profile-head">
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
            <view class="profile-copy">
              <text class="profile-name">
                {{ masterStore.profile?.masterName }}
              </text>
              <view class="profile-meta">
                <uni-icons
                  type="star-filled"
                  size="13"
                  color="#d98a1f"
                />
                <text class="meta-text">
                  {{ masterStore.profile?.rating || '0.0' }}
                </text>
                <text class="meta-divider">
                  ·
                </text>
                <text class="meta-text">
                  完单 {{ masterStore.profile?.orderCount || 0 }}
                </text>
              </view>
            </view>
            <view
              class="accept-toggle"
              @click="toggleAccepting"
            >
              <view
                class="switch"
                :class="{ 'switch--on': masterStore.accepting }"
              >
                <view class="switch-dot" />
              </view>
              <text class="switch-label">
                {{ masterStore.accepting ? '接单中' : '已停接' }}
              </text>
            </view>
          </view>
        </view>

        <view class="stat-grid">
          <view
            v-for="item in stats"
            :key="item.key"
            class="stat-card card"
            @click="openOrders(item.status)"
          >
            <text class="stat-value">
              {{ item.value }}
            </text>
            <text class="stat-label">
              {{ item.label }}
            </text>
          </view>
        </view>

        <view class="action-list">
          <view
            class="action-row card"
            @click="openPool"
          >
            <view class="action-icon action-icon--brand">
              <uni-icons
                type="paperplane"
                size="20"
                color="#e06a3d"
              />
            </view>
            <view class="action-copy">
              <text class="action-title">
                接单大厅
              </text>
              <text class="action-desc">
                查看附近待接单的预约
              </text>
            </view>
            <view
              v-if="poolCount > 0"
              class="pool-badge"
            >
              {{ poolCount }}
            </view>
            <uni-icons
              type="right"
              size="14"
              color="#c4bcb6"
            />
          </view>

          <view
            class="action-row card"
            @click="openOrders('')"
          >
            <view class="action-icon action-icon--info">
              <uni-icons
                type="list"
                size="20"
                color="#4a7fb5"
              />
            </view>
            <view class="action-copy">
              <text class="action-title">
                我的订单
              </text>
              <text class="action-desc">
                开工、完工与历史记录
              </text>
            </view>
            <uni-icons
              type="right"
              size="14"
              color="#c4bcb6"
            />
          </view>

          <view
            class="action-row card"
            @click="goApply"
          >
            <view class="action-icon action-icon--muted">
              <uni-icons
                type="person"
                size="20"
                color="#7a726c"
              />
            </view>
            <view class="action-copy">
              <text class="action-title">
                资料与技能
              </text>
              <text class="action-desc">
                修改入驻资料，补充服务技能
              </text>
            </view>
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
      current-key="master-home"
      :items="tabItems"
    />
    <AppFeedback />
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import uniIcons from '@dcloudio/uni-ui/lib/uni-icons/uni-icons.vue'
import UiNavbar from '@/components/ui/UiNavbar.vue'
import AppTabBar from '@/components/AppTabBar.vue'
import AppFeedback from '@/components/AppFeedback.vue'
import AppStatus from '@/components/AppStatus.vue'
import UiSkeleton from '@/components/ui/UiSkeleton.vue'
import { useAuthStore } from '@/stores/auth'
import { useMasterStore } from '@/stores/master'
import { listMasterOrders, listPoolOrders } from '@/api/master'
import { assetUrl } from '@/utils/assets'
import { ORDER_STATUS } from '@/constants/order'
import { MASTER_TAB_ITEMS } from '@/constants/master-navigation'
import { MINI_PROGRAM_MODE_CUSTOMER, setMiniProgramMode } from '@/utils/mini-program-mode'
import { showToast } from '@/utils/feedback'

const masterStore = useMasterStore()
const authStore = useAuthStore()
const loaded = ref(false)
const counts = ref({ accepted: 0, serving: 0, finished: 0 })
const poolCount = ref(0)

const tabItems = MASTER_TAB_ITEMS

const initials = computed(() => (masterStore.profile?.masterName || '师').slice(0, 1))
const avatarUrl = computed(() =>
  authStore.user?.avatar || authStore.user?.avatarUrl || masterStore.profile?.avatarUrl || ''
)

const stats = computed(() => [
  { key: 'accepted', label: '待开工', value: counts.value.accepted, status: ORDER_STATUS.ACCEPTED },
  { key: 'serving', label: '服务中', value: counts.value.serving, status: ORDER_STATUS.SERVING },
  { key: 'finished', label: '已完成', value: counts.value.finished, status: ORDER_STATUS.FINISHED }
])

async function loadCounts () {
  if (!masterStore.isApproved) return
  try {
    const [accepted, serving, finished, pool] = await Promise.all([
      listMasterOrders({ status: ORDER_STATUS.ACCEPTED }),
      listMasterOrders({ status: ORDER_STATUS.SERVING }),
      listMasterOrders({ status: ORDER_STATUS.FINISHED }),
      listPoolOrders()
    ])
    counts.value = {
      accepted: accepted?.total || 0,
      serving: serving?.total || 0,
      finished: finished?.total || 0
    }
    poolCount.value = pool?.total || 0
  } catch (error) {
    counts.value = { accepted: 0, serving: 0, finished: 0 }
    poolCount.value = 0
  }
}

async function toggleAccepting () {
  const next = masterStore.accepting ? '0' : '1'
  try {
    await masterStore.toggleAccepting(next)
    showToast(next === '1' ? '已开启接单' : '已暂停接单', { type: 'success' })
  } catch (error) {
    showToast(error.message || '操作失败')
  }
}

function goApply () {
  uni.navigateTo({ url: '/pages/master/apply/index' })
}

function openPool () {
  uni.navigateTo({ url: '/pages/master/pool/index' })
}

function openOrders (status) {
  const query = status ? `?status=${status}` : ''
  uni.navigateTo({ url: `/pages/master/orders/index${query}` })
}

function backToUser () {
  setMiniProgramMode(MINI_PROGRAM_MODE_CUSTOMER)
  uni.reLaunch({ url: '/pages/home/index' })
}

// 每次回到工作台都从服务端刷新，确保后台审核结果立即生效。
onShow(async () => {
  try {
    await masterStore.loadProfile()
    await loadCounts()
  } finally {
    loaded.value = true
  }
})
</script>

<style lang="scss" scoped>
@import '@/styles/tokens.scss';

.master-home { min-height: 100vh; background: $bg; }

.home-body { padding: 0 $page-gutter $space-6; }

.state-block { padding-top: 120rpx; }

.profile-card { padding: $space-4; margin-bottom: $space-4; }

.profile-head { display: flex; align-items: center; }

.avatar {
  width: 96rpx; height: 96rpx; border-radius: 50%; overflow: hidden;
  background: $brand-100; color: $brand-600;
  display: flex; align-items: center; justify-content: center;
  font-size: $font-title; font-weight: $weight-bold;
  image { width: 100%; height: 100%; }
}

.profile-copy { flex: 1; margin-left: $space-3; }

.profile-name { display: block; font-size: $font-title; font-weight: $weight-semibold; color: $ink-900; }

.profile-status {
  display: inline-block; margin-top: 8rpx; font-size: $font-caption;
  padding: 4rpx 16rpx; border-radius: 999rpx;
  background: $bg-sunken; color: $ink-500;
  &--1 { background: $success-bg; color: $success; }
  &--0 { background: $warning-bg; color: $warning; }
  &--2 { background: $danger-bg; color: $danger; }
}

.profile-meta { display: flex; align-items: center; margin-top: 8rpx; }

.meta-text { font-size: $font-caption; color: $ink-500; margin-left: 6rpx; }

.meta-divider { margin: 0 10rpx; color: $ink-300; font-size: $font-caption; }

.accept-toggle { display: flex; flex-direction: column; align-items: center; }

.switch {
  width: 84rpx; height: 46rpx; border-radius: 999rpx; background: $line;
  position: relative; transition: background 0.2s;
  &--on { background: $brand-500; }
}

.switch-dot {
  position: absolute; top: 4rpx; left: 4rpx; width: 38rpx; height: 38rpx;
  border-radius: 50%; background: $surface; transition: transform 0.2s;
}

.switch--on .switch-dot { transform: translateX(38rpx); }

.switch-label { margin-top: 8rpx; font-size: $font-micro; color: $ink-500; }

.reject-box, .pending-box {
  margin-top: $space-3; padding: $space-3; border-radius: $radius-md;
}

.reject-box { background: $danger-bg; }
.pending-box { background: $bg-sunken; }

.reject-label { display: block; font-size: $font-caption; color: $danger; font-weight: $weight-semibold; }

.reject-text { display: block; margin-top: 8rpx; font-size: $font-body; color: $ink-700; line-height: 1.6; }

.pending-text { font-size: $font-body; color: $ink-500; line-height: 1.7; }

.primary-button {
  margin-top: $space-4; height: 88rpx; border-radius: $radius-md;
  background: $brand-500; color: #fff; font-size: $font-button; font-weight: $weight-semibold;
}

.stat-grid { display: flex; gap: $space-2; margin-bottom: $space-4; }

.stat-card {
  flex: 1; padding: $space-3 0; display: flex; flex-direction: column; align-items: center;
}

.stat-value { font-size: $font-display; font-weight: $weight-bold; color: $ink-900; }

.stat-label { margin-top: 6rpx; font-size: $font-caption; color: $ink-500; }

.action-list { display: flex; flex-direction: column; gap: $space-2; }

.action-row { display: flex; align-items: center; padding: $space-3 $space-4; }

.action-icon {
  width: 64rpx; height: 64rpx; border-radius: $radius-md;
  display: flex; align-items: center; justify-content: center;
  &--brand { background: $brand-tint; }
  &--info { background: $info-bg; }
  &--muted { background: $bg-sunken; }
}

.action-copy { flex: 1; margin-left: $space-3; }

.action-title { display: block; font-size: $font-body; font-weight: $weight-medium; color: $ink-900; }

.action-desc { display: block; margin-top: 6rpx; font-size: $font-caption; color: $ink-400; }

.pool-badge {
  min-width: 36rpx; height: 36rpx; padding: 0 10rpx; margin-right: $space-2;
  border-radius: 999rpx; background: $brand-500; color: #fff;
  font-size: $font-micro; display: flex; align-items: center; justify-content: center;
}

.back-entry { padding: 8rpx; }
</style>
