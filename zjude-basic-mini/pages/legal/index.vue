<template>
  <view class="legal-page">
    <UiNavbar
      :back="true"
      :title="pageTitle"
    />

    <scroll-view
      class="legal-scroll"
      scroll-y
    >
      <view class="legal-content">
        <text class="legal-title">{{ pageTitle }}</text>
        <text class="legal-meta">更新日期：2026年11月7日</text>
        <text class="legal-intro">感谢您使用{{ appName }}。请在使用服务前仔细阅读并理解以下内容。</text>

        <view
          v-for="section in sections"
          :key="section.title"
          class="legal-section"
        >
          <text class="section-title">{{ section.title }}</text>
          <text
            v-for="(paragraph, index) in section.paragraphs"
            :key="`${section.title}-${index}`"
            class="section-text"
          >{{ paragraph }}</text>
        </view>

        <view class="legal-contact">
          <text class="contact-title">联系我们</text>
          <text class="contact-text">如对本政策或用户协议有疑问，可通过小程序内“联系客服”与我们联系。</text>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import UiNavbar from '@/components/ui/UiNavbar.vue'
import { useConfigStore } from '@/stores/config'

const configStore = useConfigStore()
const type = ref('privacy')

const isPrivacy = computed(() => type.value !== 'agreement')
const pageTitle = computed(() => (isPrivacy.value ? '隐私政策' : '用户协议'))
const appName = computed(() => configStore.appTitle || '本小程序')

const privacySections = [
  {
    title: '一、我们如何收集和使用信息',
    paragraphs: [
      '为向您提供家政服务浏览、预约、订单履约和售后服务，我们会在必要范围内收集您主动提供的信息，包括手机号、联系人姓名、服务地址、预约信息和订单信息。',
      '您可以匿名浏览首页、分类、服务列表和服务详情。只有在预约、下单、查询订单或使用其他需要账号的功能时，我们才会提示您登录并收集完成该功能所必需的信息。'
    ]
  },
  {
    title: '二、微信授权信息',
    paragraphs: [
      '当您主动选择微信授权登录时，我们会根据您的授权获取微信登录凭证，以及您主动提交的头像和昵称，用于创建或关联账号、展示订单服务信息。您可以拒绝授权，拒绝后仍可继续浏览公开服务内容。'
    ]
  },
  {
    title: '三、信息的存储与保护',
    paragraphs: [
      '我们会采取访问控制、传输加密、脱敏展示等措施保护您的信息，并仅在实现服务目的所需的期限内保存。订单、支付和售后相关记录会按照法律法规及业务需要留存。'
    ]
  },
  {
    title: '四、信息的共享与披露',
    paragraphs: [
      '为完成您发起的服务预约，我们可能向为您提供服务的师傅展示必要的预约信息。我们不会出售您的个人信息，也不会将信息用于本政策未说明的目的；法律法规要求或为保护用户及平台安全时除外。'
    ]
  },
  {
    title: '五、您的权利',
    paragraphs: [
      '您可以通过小程序中的相关功能查看、修改或删除部分个人信息，也可以退出登录、拒绝非必要授权或联系客服处理信息查询、更正和删除请求。'
    ]
  },
  {
    title: '六、未成年人保护与政策更新',
    paragraphs: [
      '如您是未成年人，请在监护人同意和陪同下使用本小程序。我们可能根据法律法规、服务变化或安全需要更新本政策，更新后的内容会在本页面展示。'
    ]
  }
]

const agreementSections = [
  {
    title: '一、服务说明',
    paragraphs: [
      '我们为您提供家政服务信息浏览、预约、订单管理及售后沟通服务。您应根据页面提示提供真实、准确、完整的信息。'
    ]
  },
  {
    title: '二、账号与使用规范',
    paragraphs: [
      '您应妥善保管账号，不得冒用他人身份、发布违法违规内容或利用平台从事损害他人及平台权益的活动。必要时，我们会为保障服务安全暂停相关功能并与您联系。'
    ]
  },
  {
    title: '三、预约与履约',
    paragraphs: [
      '服务价格、可预约时间和服务范围以提交订单时页面展示为准。订单提交后请保持联系方式畅通，并按照订单约定完成服务确认、支付和评价。'
    ]
  },
  {
    title: '四、协议生效与变更',
    paragraphs: [
      '当您勾选同意并使用登录、预约或下单功能时，即表示您已阅读并同意本协议及隐私政策。我们会在本页面公布协议变更内容，重大变更会通过适当方式提示您。'
    ]
  }
]

const sections = computed(() => (isPrivacy.value ? privacySections : agreementSections))

onLoad((options = {}) => {
  type.value = options.type === 'agreement' ? 'agreement' : 'privacy'
  configStore.load().catch(() => {})
})
</script>

<style lang="scss" scoped>
@import '@/styles/tokens.scss';

.legal-page {
  min-height: 100vh;
  background: $bg;
}

.legal-scroll {
  height: calc(100vh - 88rpx);
}

.legal-content {
  padding: $space-6 $page-gutter calc(120rpx + env(safe-area-inset-bottom));
}

.legal-title {
  display: block;
  font-size: 40rpx;
  font-weight: $weight-bold;
  line-height: 1.45;
  color: $ink-900;
}

.legal-meta,
.legal-intro {
  display: block;
  margin-top: $space-2;
  font-size: $font-micro;
  line-height: 1.7;
  color: $ink-400;
}

.legal-intro {
  margin-top: $space-4;
  color: $ink-500;
}

.legal-section {
  margin-top: $space-6;
}

.section-title,
.contact-title {
  display: block;
  font-size: $font-body;
  font-weight: $weight-semibold;
  line-height: 1.5;
  color: $ink-900;
}

.section-text,
.contact-text {
  display: block;
  margin-top: $space-2;
  font-size: $font-caption;
  line-height: 1.85;
  color: $ink-500;
}

.legal-contact {
  margin-top: $space-6;
  padding: $space-4;
  border-radius: $radius-md;
  background: $surface;
}
</style>
