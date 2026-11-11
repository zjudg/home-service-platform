<template>
  <view class="wallet-page has-tabbar">
    <UiNavbar
      :back="true"
      title="我的收入"
    />

    <view class="wallet-body">
      <!-- 首次加载：整页骨架，避免余额先显示 0.00 再跳变 -->
      <UiSkeleton
        v-if="loading"
        variant="wallet"
        :count="3"
      />
      <template v-else>
        <view class="balance-card">
          <text class="balance-label">
            可用余额（元）
          </text>
          <text class="balance-value">
            {{ wallet.balance }}
          </text>
          <view class="balance-meta">
            <view class="meta-item">
              <text class="meta-value">
                {{ wallet.frozen }}
              </text>
              <text class="meta-label">
                提现中
              </text>
            </view>
            <view class="meta-divider" />
            <view class="meta-item">
              <text class="meta-value">
                {{ wallet.totalIncome }}
              </text>
              <text class="meta-label">
                累计收入
              </text>
            </view>
            <view class="meta-divider" />
            <view class="meta-item">
              <text class="meta-value">
                {{ wallet.totalWithdraw }}
              </text>
              <text class="meta-label">
                累计提现
              </text>
            </view>
          </view>
        </view>

        <button
          class="withdraw-btn"
          :disabled="!canWithdraw"
          @click="openWithdraw"
        >
          申请提现
        </button>
        <text
          v-if="!canWithdraw"
          class="withdraw-tip"
        >
          {{ withdrawDisabledReason }}
        </text>

        <UiTabs
          v-model="active"
          :items="tabs"
          centered
          class="tabs-wrap"
        />

        <template v-if="active === 'logs'">
          <view
            v-if="logs.length"
            class="log-list"
          >
            <view
              v-for="log in logs"
              :key="log.logId"
              class="log-row card"
            >
              <view class="log-main">
                <text class="log-title">
                  {{ bizLabel(log.bizType) }}
                </text>
                <text class="log-remark">
                  {{ log.remark || log.bizNo || '-' }}
                </text>
                <text class="log-time">
                  {{ formatTime(log.createTime) }}
                </text>
              </view>
              <view
                class="log-amount"
                :class="{ 'log-amount--out': Number(log.amount) < 0 }"
              >
                {{ Number(log.amount) > 0 ? '+' : '' }}{{ log.amount }}
              </view>
            </view>
          </view>
          <UiEmpty
            v-else
            icon="wallet"
            title="暂无收入记录"
            description="完成订单后收入会显示在这里"
          />
        </template>

        <template v-else>
          <view
            v-if="withdraws.length"
            class="log-list"
          >
            <view
              v-for="item in withdraws"
              :key="item.withdrawId"
              class="log-row card"
            >
              <view class="log-main">
                <text class="log-title">
                  {{ withdrawStatusLabel(item.withdrawStatus) }}
                </text>
                <text class="log-remark">
                  单号 {{ item.withdrawNo }}
                </text>
                <text class="log-time">
                  {{ formatTime(item.createTime) }}
                </text>
                <text
                  v-if="item.failReason || item.auditRemark"
                  class="log-fail"
                >
                  {{ item.failReason || item.auditRemark }}
                </text>
              </view>
              <view class="log-amount log-amount--out">
                -{{ fenToYuan(item.amount) }}
              </view>
            </view>
          </view>
          <UiEmpty
            v-else
            icon="wallet"
            title="暂无提现记录"
          />
        </template>
      </template>
    </view>

    <AppTabBar
      current-key="master-wallet"
      :items="tabItems"
    />

    <!-- 提现弹窗 -->
    <view
      v-if="dialogVisible"
      class="dialog-mask"
      @click="closeWithdraw"
    >
      <view
        class="dialog-card"
        @click.stop
      >
        <text class="dialog-title">
          申请提现
        </text>
        <view class="dialog-field">
          <text class="dialog-label">
            提现金额（元）
          </text>
          <input
            v-model="form.amountYuan"
            class="dialog-input"
            type="digit"
            placeholder="最低 10 元"
          >
        </view>
        <view class="dialog-field">
          <text class="dialog-label">
            收款账号
          </text>
          <input
            v-model="form.payAccount"
            class="dialog-input"
            placeholder="微信号或手机号"
            maxlength="128"
          >
        </view>
        <text class="dialog-tip">
          提交后需平台审核，审核通过将转账至该账号。
        </text>
        <view class="dialog-actions">
          <button
            class="dialog-btn"
            @click="closeWithdraw"
          >
            取消
          </button>
          <button
            class="dialog-btn dialog-btn--primary"
            :disabled="submitting"
            @click="submitWithdraw"
          >
            {{ submitting ? '提交中...' : '确认提交' }}
          </button>
        </view>
      </view>
    </view>

    <AppFeedback />
  </view>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import UiNavbar from '@/components/ui/UiNavbar.vue'
import UiTabs from '@/components/ui/UiTabs.vue'
import UiEmpty from '@/components/ui/UiEmpty.vue'
import AppTabBar from '@/components/AppTabBar.vue'
import AppFeedback from '@/components/AppFeedback.vue'
import UiSkeleton from '@/components/ui/UiSkeleton.vue'
import { applyWithdraw, getWallet, listMyWithdraws, listWalletLogs } from '@/api/wallet'
import { useMasterStore } from '@/stores/master'
import { MASTER_TAB_ITEMS } from '@/constants/master-navigation'
import { showToast, toastSuccess } from '@/utils/feedback'

const masterStore = useMasterStore()
const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const active = ref('logs')
const logs = ref([])
const withdraws = ref([])
const wallet = reactive({ balance: '0.00', frozen: '0.00', totalIncome: '0.00', totalWithdraw: '0.00' })
const form = reactive({ amountYuan: '', payAccount: '' })

// 最低提现金额与后端配置保持一致，仅用于前端提示
const MIN_WITHDRAW = 10

const tabs = [
  { label: '收入明细', value: 'logs' },
  { label: '提现记录', value: 'withdraws' }
]

const tabItems = MASTER_TAB_ITEMS

const canWithdraw = computed(() => masterStore.isApproved && Number(wallet.balance) >= MIN_WITHDRAW)

const withdrawDisabledReason = computed(() =>
  masterStore.isApproved ? `余额满 ${MIN_WITHDRAW} 元后可提现` : '完成师傅入驻并通过审核后可提现'
)

const BIZ_LABELS = {
  order_income: '订单收入',
  order_reverse: '订单冲回',
  withdraw: '提现冻结',
  withdraw_refund: '提现退回'
}

const WITHDRAW_LABELS = {
  0: '待审核',
  1: '待打款',
  2: '已打款',
  3: '已驳回',
  4: '打款失败'
}

function bizLabel (type) {
  return BIZ_LABELS[type] || '收入变动'
}

function withdrawStatusLabel (status) {
  return WITHDRAW_LABELS[String(status)] || '-'
}

function fenToYuan (fen) {
  return (Number(fen || 0) / 100).toFixed(2)
}

function formatTime (value) {
  if (!value) return ''
  return String(value).replace('T', ' ').slice(0, 16)
}

async function load () {
  if (!masterStore.isApproved) return
  loading.value = true
  try {
    const [walletRes, logRes, withdrawRes] = await Promise.all([
      getWallet(),
      listWalletLogs(),
      listMyWithdraws()
    ])
    const data = walletRes?.data || walletRes || {}
    Object.assign(wallet, {
      balance: data.balance || '0.00',
      frozen: data.frozen || '0.00',
      totalIncome: data.totalIncome || '0.00',
      totalWithdraw: data.totalWithdraw || '0.00'
    })
    logs.value = logRes?.data || []
    withdraws.value = withdrawRes?.data || []
  } catch (e) {
    showToast(e.message || '加载失败')
  } finally {
    loading.value = false
  }
}

function openWithdraw () {
  form.amountYuan = ''
  form.payAccount = ''
  dialogVisible.value = true
}

function closeWithdraw () {
  dialogVisible.value = false
}

async function submitWithdraw () {
  const amount = Number(form.amountYuan)
  if (!amount || amount <= 0) return showToast('请输入提现金额')
  if (amount < MIN_WITHDRAW) return showToast(`提现金额不能低于 ${MIN_WITHDRAW} 元`)
  if (amount > Number(wallet.balance)) return showToast('提现金额超过可用余额')
  if (!form.payAccount.trim()) return showToast('请填写收款账号')

  submitting.value = true
  try {
    await applyWithdraw(form.amountYuan.trim(), form.payAccount.trim())
    toastSuccess('提现申请已提交')
    dialogVisible.value = false
    await load()
  } catch (e) {
    showToast(e.message || '提交失败')
  } finally {
    submitting.value = false
  }
}

onShow(async () => {
  await masterStore.loadProfile()
  await load()
})
</script>

<style lang="scss" scoped>
@import '@/styles/tokens.scss';

.wallet-page { min-height: 100vh; background: $bg; }

.wallet-body { padding: 0 $page-gutter $space-6; }

.balance-card {
  padding: $space-6 $space-4;
  border-radius: $radius-lg;
  background: linear-gradient(135deg, $brand-500, $brand-600);
  color: #fff;
}

.balance-label { font-size: $font-caption; opacity: 0.85; }

.balance-value {
  display: block; margin-top: $space-2;
  font-size: 64rpx; font-weight: $weight-bold; letter-spacing: 2rpx;
}

.balance-meta {
  display: flex; align-items: center;
  margin-top: $space-4; padding-top: $space-3;
  border-top: 2rpx solid rgba(255, 255, 255, 0.25);
}

.meta-item { flex: 1; display: flex; flex-direction: column; align-items: center; }

.meta-divider { width: 2rpx; height: 40rpx; background: rgba(255, 255, 255, 0.25); }

.meta-value { font-size: $font-body; font-weight: $weight-semibold; }

.meta-label { margin-top: 4rpx; font-size: $font-micro; opacity: 0.8; }

.withdraw-btn {
  margin-top: $space-4; height: 88rpx; line-height: 88rpx;
  border-radius: $radius-md; background: $brand-500; color: #fff;
  font-size: $font-button; font-weight: $weight-semibold;
  &[disabled] { opacity: 0.5; }
}

.withdraw-tip {
  display: block; margin-top: $space-2; text-align: center;
  font-size: $font-micro; color: $ink-400;
}

.tabs-wrap { margin-top: $space-4; }

.log-list { margin-top: $space-3; display: flex; flex-direction: column; gap: $space-2; }

.log-row { display: flex; align-items: center; padding: $space-3 $space-4; }

.log-main { flex: 1; display: flex; flex-direction: column; gap: 6rpx; }

.log-title { font-size: $font-body; font-weight: $weight-medium; color: $ink-900; }

.log-remark { font-size: $font-caption; color: $ink-400; }

.log-time { font-size: $font-micro; color: $ink-300; }

.log-fail { font-size: $font-micro; color: $danger; }

.log-amount {
  font-size: $font-title; font-weight: $weight-semibold; color: $success;
  &--out { color: $ink-700; }
}

.dialog-mask {
  position: fixed; inset: 0; z-index: 100;
  background: rgba(31, 28, 26, 0.45);
  display: flex; align-items: flex-end;
}

.dialog-card {
  width: 100%; padding: $space-6 $space-4;
  background: $surface; border-radius: $radius-lg $radius-lg 0 0;
}

.dialog-title { display: block; font-size: $font-title; font-weight: $weight-semibold; }

.dialog-field { margin-top: $space-4; }

.dialog-label { display: block; font-size: $font-caption; color: $ink-500; margin-bottom: 12rpx; }

.dialog-input {
  height: 80rpx; padding: 0 $space-3;
  background: $bg-sunken; border-radius: $radius-md;
  font-size: $font-body;
}

.dialog-tip { display: block; margin-top: $space-3; font-size: $font-micro; color: $ink-400; }

.dialog-actions { display: flex; gap: $space-3; margin-top: $space-4; }

.dialog-btn {
  flex: 1; height: 84rpx; line-height: 84rpx;
  border-radius: $radius-md; background: $bg-sunken; color: $ink-700;
  font-size: $font-button;
  &--primary { background: $brand-500; color: #fff; }
  &[disabled] { opacity: 0.6; }
}
</style>
