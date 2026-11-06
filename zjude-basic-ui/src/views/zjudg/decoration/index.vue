<template>
  <div class="app-container decoration-page">
    <div class="decoration-layout">
      <aside class="decoration-nav">
        <div
          v-for="item in navItems"
          :key="item.key"
          :class="{ active: scope === item.key }"
          @click="switchScope(item.key)"
        >
          {{ item.label }}
        </div>
      </aside>

      <main
        v-loading="loading"
        class="decoration-editor"
      >
        <h3>{{ title }}</h3>

        <!-- 首页装修 -->
        <template v-if="scope === 'home'">
          <el-form label-width="110px">
            <el-alert
              type="warning"
              :closable="false"
              show-icon
              class="banner-alert"
              title="这里的轮播是兜底数据：只要「轮播图管理」中有任何一条已上架轮播，小程序就只显示那边的，此处配置不会生效。"
            />
            <el-form-item label="首页轮播图">
              <div
                v-for="(banner, index) in home.banners"
                :key="index"
                class="banner-row"
              >
                <image-upload
                  v-model="banner.image"
                  :limit="1"
                />
                <el-input
                  v-model="banner.title"
                  placeholder="标题"
                />
                <el-input
                  v-model="banner.subtitle"
                  placeholder="副标题"
                />
                <el-input
                  v-model="banner.serviceId"
                  placeholder="跳转服务 ID（可留空）"
                />
                <el-button
                  link
                  type="danger"
                  @click="home.banners.splice(index, 1)"
                >
                  删除
                </el-button>
              </div>
              <el-button @click="home.banners.push(emptyBanner())">
                添加轮播图
              </el-button>
            </el-form-item>

            <el-form-item label="搜索占位文案">
              <el-input
                v-model="home.searchPlaceholder"
                placeholder="如 搜索保洁、家电清洗、陪护"
                maxlength="64"
              />
            </el-form-item>

            <el-form-item label="区块标题">
              <el-input
                v-model="home.seasonTitle"
                placeholder="首页主推区块标题"
                maxlength="32"
              />
            </el-form-item>

            <el-form-item label="服务不可用提示">
              <el-input
                v-model="home.unavailableText"
                placeholder="如 该服务暂未开放"
                maxlength="64"
              />
            </el-form-item>
          </el-form>

          <p class="decoration-tip">
            轮播读取顺序：先取「轮播图管理」中已上架数据，无可用数据时才回退到本页配置。
          </p>
        </template>

        <!-- 个人中心 -->
        <template v-else-if="scope === 'profile'">
          <el-form label-width="110px">
            <el-form-item label="页面标题">
              <el-input
                v-model="profile.pageTitle"
                placeholder="如 我的"
                maxlength="32"
              />
            </el-form-item>

            <el-form-item label="订单区块标题">
              <el-input
                v-model="profile.orderTitle"
                placeholder="如 预约订单"
                maxlength="32"
              />
            </el-form-item>

            <el-form-item label="全部订单文案">
              <el-input
                v-model="profile.allOrdersText"
                placeholder="如 全部"
                maxlength="16"
              />
            </el-form-item>

            <el-form-item label="服务区块标题">
              <el-input
                v-model="profile.serviceTitle"
                placeholder="如 我的服务"
                maxlength="32"
              />
            </el-form-item>
          </el-form>

          <p class="decoration-tip">
            小程序「我的」页菜单由代码维护，这里只调整区块标题类文案，避免和实际功能不一致。
          </p>
        </template>

        <!-- 客服设置 -->
        <template v-else>
          <el-form label-width="110px">
            <el-form-item label="客服电话">
              <el-input
                v-model="customer.servicePhone"
                placeholder="如 400-000-0000"
                maxlength="32"
              />
            </el-form-item>

            <el-form-item label="服务时间">
              <el-input
                v-model="customer.serviceHours"
                placeholder="如 工作日 9:00 - 21:00"
                maxlength="64"
              />
            </el-form-item>
          </el-form>

          <p class="decoration-tip">
            与「系统设置 - 网站设置」中的客服配置是同一份数据，修改后小程序「我的 - 联系客服」立即生效。
          </p>
        </template>

        <el-button
          type="primary"
          class="save-button"
          :loading="saving"
          @click="save"
        >
          保存设置
        </el-button>
      </main>
    </div>
  </div>
</template>

<script setup name="ZjudgDecoration">
import { computed, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import ImageUpload from '@/components/ImageUpload'
import {
  customerDecorationApi,
  homeDecorationApi,
  parseJsonConfig,
  profileDecorationApi,
  toConfigMap
} from '@/api/settings/zjudgDecoration'

const route = useRoute()
const router = useRouter()

const navItems = [
  { key: 'home', label: '首页装修' },
  { key: 'profile', label: '个人中心' },
  { key: 'customer-service', label: '客服设置' }
]

const scope = computed(() => route.query.scope || 'home')
const title = computed(() => navItems.find(item => item.key === scope.value)?.label || '首页装修')

const loading = ref(false)
const saving = ref(false)

const home = reactive({
  banners: [],
  searchPlaceholder: '',
  seasonTitle: '',
  unavailableText: ''
})

const profile = reactive({
  pageTitle: '',
  orderTitle: '',
  allOrdersText: '',
  serviceTitle: ''
})

const customer = reactive({
  servicePhone: '',
  serviceHours: ''
})

// 只在没有配置时使用的默认值，保证首次进入表单不是空的
const HOME_DEFAULTS = {
  searchPlaceholder: '搜索保洁、家电清洗、陪护',
  seasonTitle: '热门服务',
  unavailableText: '该服务暂未开放'
}

const PROFILE_DEFAULTS = {
  pageTitle: '我的',
  orderTitle: '预约订单',
  allOrdersText: '全部',
  serviceTitle: '我的服务'
}

function emptyBanner () {
  return { image: '', title: '', subtitle: '', serviceId: '' }
}

// 轮播配置里可能残留旧结构（name/link），统一映射到当前字段
function normalizeBanners (list) {
  if (!Array.isArray(list)) return []
  return list.map(item => ({
    image: item?.image || '',
    title: item?.title || item?.name || '',
    subtitle: item?.subtitle || '',
    serviceId: item?.serviceId || item?.link || ''
  }))
}

async function loadHome () {
  const response = await homeDecorationApi.listConfig()
  const map = toConfigMap(response.rows || response.data?.rows)
  home.banners = normalizeBanners(parseJsonConfig(map['decorate.home.banners'], []))
  home.searchPlaceholder = map['decorate.home.searchPlaceholder'] || HOME_DEFAULTS.searchPlaceholder
  home.seasonTitle = map['decorate.home.seasonTitle'] || HOME_DEFAULTS.seasonTitle
  home.unavailableText = map['decorate.home.unavailableText'] || HOME_DEFAULTS.unavailableText
}

async function loadProfile () {
  const response = await profileDecorationApi.listConfig()
  const map = toConfigMap(response.rows || response.data?.rows)
  profile.pageTitle = map['decorate.profile.pageTitle'] || PROFILE_DEFAULTS.pageTitle
  profile.orderTitle = map['decorate.profile.orderTitle'] || PROFILE_DEFAULTS.orderTitle
  profile.allOrdersText = map['decorate.profile.allOrdersText'] || PROFILE_DEFAULTS.allOrdersText
  profile.serviceTitle = map['decorate.profile.serviceTitle'] || PROFILE_DEFAULTS.serviceTitle
}

async function loadCustomer () {
  const response = await customerDecorationApi.get()
  const data = response.data || {}
  customer.servicePhone = data.servicePhone || ''
  customer.serviceHours = data.serviceHours || ''
}

async function load () {
  loading.value = true
  try {
    if (scope.value === 'home') await loadHome()
    else if (scope.value === 'profile') await loadProfile()
    else await loadCustomer()
  } catch (e) {
    ElMessage.error(e?.message || '装修配置加载失败')
  } finally {
    loading.value = false
  }
}

async function saveHome () {
  await homeDecorationApi.saveConfig({
    'decorate.home.banners': JSON.stringify(home.banners.filter(item => item.image)),
    'decorate.home.searchPlaceholder': home.searchPlaceholder,
    'decorate.home.seasonTitle': home.seasonTitle,
    'decorate.home.unavailableText': home.unavailableText
  })
}

async function saveProfile () {
  await profileDecorationApi.saveConfig({
    'decorate.profile.pageTitle': profile.pageTitle,
    'decorate.profile.orderTitle': profile.orderTitle,
    'decorate.profile.allOrdersText': profile.allOrdersText,
    'decorate.profile.serviceTitle': profile.serviceTitle
  })
}

async function saveCustomer () {
  // 网站设置是整表提交，先取回其余字段再覆盖客服两项，避免把 Logo 等配置清空
  const response = await customerDecorationApi.get()
  const current = response.data || {}
  await customerDecorationApi.save({
    ...current,
    servicePhone: customer.servicePhone,
    serviceHours: customer.serviceHours
  })
}

async function save () {
  saving.value = true
  try {
    if (scope.value === 'home') await saveHome()
    else if (scope.value === 'profile') await saveProfile()
    else await saveCustomer()
    ElMessage.success('保存成功')
  } catch (e) {
    ElMessage.error(e?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

function switchScope (value) {
  if (scope.value === value) return
  router.push({ query: { scope: value } })
}

watch(scope, load, { immediate: true })
</script>

<style scoped>
.decoration-layout { display: flex; min-height: 650px; }
.decoration-nav { width: 145px; border-right: 1px solid #dfe4ef; padding-top: 8px; }
.decoration-nav div { padding: 13px 16px; cursor: pointer; }
.decoration-nav .active { color: #405dff; background: #eef0ff; border-right: 2px solid #405dff; }
.decoration-editor { flex: 1; padding: 0 30px; position: relative; }
.decoration-editor h3 { font-weight: 500; border-left: 3px solid #405dff; padding-left: 10px; }
.banner-row { display: flex; align-items: center; gap: 12px; padding: 14px; background: #fafafa; margin-bottom: 12px; max-width: 900px; width: 100%; }
.banner-row .image-upload { width: 100px; }
.decoration-tip { max-width: 760px; margin: 8px 0 0; color: var(--el-text-color-secondary); font-size: 13px; line-height: 1.6; }
.banner-alert { max-width: 900px; margin-bottom: 16px; }
.save-button { margin-top: 20px; }
.decoration-editor :deep(.el-input) { max-width: 360px; }
</style>
