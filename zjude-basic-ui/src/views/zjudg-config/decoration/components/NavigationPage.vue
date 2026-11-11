<template>
  <div class="app-container decoration-page">
    <NoticePanel>
      <div>{{ scope === 'home' ? '设置移动端首页导航区的菜单。' : '设置移动端个人中心->我的服务的菜单。' }}支持选择系统模块、填写自定义链接</div>
    </NoticePanel>

    <el-tabs v-model="activeTab">
      <el-tab-pane
        label="导航设置"
        name="navigation"
      >
        <div class="toolbar">
          <el-button
            type="primary"
            icon="Plus"
            @click="openDialog()"
          >
            新增
          </el-button>
          <el-button
            type="primary"
            :loading="saving"
            @click="saveSettings"
          >
            保存设置
          </el-button>
        </div>
        <el-table
          :data="sortedItems"
          border
        >
          <el-table-column
            label="菜单名称"
            prop="name"
            min-width="160"
          />
          <el-table-column
            label="菜单图标"
            width="120"
            align="center"
          >
            <template #default="{ row }">
              <el-image
                v-if="row.icon"
                :src="imageUrl(row.icon)"
                class="nav-icon"
                fit="contain"
              />
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column
            label="链接地址"
            prop="link"
            min-width="240"
            show-overflow-tooltip
          />
          <el-table-column
            label="排序"
            prop="sortNum"
            width="90"
            align="center"
          />
          <el-table-column
            label="显示"
            width="110"
            align="center"
          >
            <template #default="{ row }">
              <el-switch
                v-model="row.isVisible"
                active-value="1"
                inactive-value="0"
                inline-prompt
                active-text="显示"
                inactive-text="隐藏"
              />
            </template>
          </el-table-column>
          <el-table-column
            label="操作"
            width="150"
            align="center"
          >
            <template #default="{ row }">
              <el-button
                link
                type="primary"
                @click="openDialog(row)"
              >
                编辑
              </el-button>
              <el-button
                link
                type="danger"
                @click="removeItem(row)"
              >
                删除
              </el-button>
            </template>
          </el-table-column>
          <template #empty>
            <el-empty description="暂无导航菜单" />
          </template>
        </el-table>
      </el-tab-pane>

      <el-tab-pane
        label="其它设置"
        name="other"
      >
        <el-form
          :model="form"
          label-width="130px"
          class="other-form"
        >
          <el-alert
            v-if="loadError"
            class="config-warning"
            type="warning"
            :title="loadError"
            :closable="false"
            show-icon
          />
          <template v-if="scope === 'home'">
            <el-form-item label="首页搜索提示">
              <el-input
                v-model="form.searchPlaceholder"
                maxlength="50"
                placeholder="请输入搜索框提示文案"
              />
            </el-form-item>
            <el-form-item label="应季商品标题">
              <el-input
                v-model="form.seasonTitle"
                maxlength="30"
              />
            </el-form-item>
            <el-form-item label="应季商品副标题">
              <el-input
                v-model="form.seasonSubtitle"
                maxlength="50"
              />
            </el-form-item>
            <el-form-item label="销量榜标题">
              <el-input
                v-model="form.rankingTitle"
                maxlength="30"
              />
            </el-form-item>
            <el-form-item label="销量榜副标题">
              <el-input
                v-model="form.rankingSubtitle"
                maxlength="50"
              />
            </el-form-item>
            <el-form-item label="查看更多文案">
              <el-input
                v-model="form.moreText"
                maxlength="20"
              />
            </el-form-item>
            <el-form-item label="购买按钮文案">
              <el-input
                v-model="form.buyText"
                maxlength="20"
              />
            </el-form-item>
            <el-form-item label="加购成功提示">
              <el-input
                v-model="form.cartSuccessText"
                maxlength="50"
              />
            </el-form-item>
            <el-form-item label="暂不可用提示">
              <el-input
                v-model="form.unavailableText"
                maxlength="50"
              />
            </el-form-item>
            <el-form-item label="首页轮播配置">
              <div class="repeatable-editor">
                <div class="editor-toolbar">
                  <span>配置首页轮播卡片，图片优先于背景色和文字。</span>
                  <el-button
                    type="primary"
                    plain
                    icon="Plus"
                    @click="addBanner"
                  >
                    新增轮播
                  </el-button>
                </div>
                <el-card
                  v-for="(banner, index) in form.banners"
                  :key="`banner-${index}`"
                  shadow="never"
                  class="editor-card"
                >
                  <template #header>
                    <div class="editor-card-header">
                      <span>轮播 {{ index + 1 }}</span>
                      <el-button
                        link
                        type="danger"
                        @click="removeBanner(index)"
                      >
                        删除
                      </el-button>
                    </div>
                  </template>
                  <el-row :gutter="14">
                    <el-col :span="12">
                      <div class="field-label">
                        标题
                      </div>
                      <el-input
                        v-model="banner.title"
                        maxlength="40"
                        placeholder="请输入主标题"
                      />
                    </el-col>
                    <el-col :span="12">
                      <div class="field-label">
                        标签
                      </div>
                      <el-input
                        v-model="banner.tag"
                        maxlength="20"
                        placeholder="例如：会员专享"
                      />
                    </el-col>
                    <el-col :span="24">
                      <div class="field-label">
                        副标题
                      </div>
                      <el-input
                        v-model="banner.sub"
                        maxlength="60"
                        placeholder="请输入副标题"
                      />
                    </el-col>
                    <el-col :span="12">
                      <div class="field-label">
                        背景色
                      </div>
                      <el-color-picker v-model="banner.bg" />
                    </el-col>
                    <el-col :span="12">
                      <div class="field-label">
                        无图片时显示的文字
                      </div>
                      <el-input
                        v-model="banner.char"
                        maxlength="2"
                        placeholder="例如：鲜"
                      />
                    </el-col>
                    <el-col :span="24">
                      <div class="field-label">
                        轮播图片
                      </div>
                      <MaterialImageField
                        v-model="banner.image"
                        :limit="1"
                        :is-show-tip="false"
                      />
                    </el-col>
                    <el-col :span="24">
                      <div class="field-label">
                        跳转链接（可选）
                      </div>
                      <el-input
                        v-model="banner.link"
                        placeholder="请输入关联页面或活动地址"
                      />
                    </el-col>
                  </el-row>
                </el-card>
                <el-empty
                  v-if="!form.banners.length"
                  description="暂无轮播，点击“新增轮播”添加"
                  :image-size="56"
                />
              </div>
            </el-form-item>
            <el-form-item label="站点 logo">
              <el-radio-group v-model="form.shopLogo">
                <el-radio value="1">
                  开启
                </el-radio><el-radio value="0">
                  关闭
                </el-radio>
              </el-radio-group>
              <div class="setting-tip">
                开启或关闭站点 logo在首页的显示，默认开启
              </div>
            </el-form-item>
            <el-form-item label="热销榜单">
              <el-radio-group v-model="form.hotRanking">
                <el-radio value="1">
                  开启
                </el-radio><el-radio value="0">
                  关闭
                </el-radio>
              </el-radio-group>
              <div class="setting-tip">
                开启或关闭热销榜单在首页的显示，默认开启
              </div>
            </el-form-item>
            <el-form-item label="新品推荐">
              <el-radio-group v-model="form.newRecommend">
                <el-radio value="1">
                  开启
                </el-radio><el-radio value="0">
                  关闭
                </el-radio>
              </el-radio-group>
              <div class="setting-tip">
                开启或关闭新品推荐在首页的显示，默认开启
              </div>
            </el-form-item>
          </template>
          <template v-else>
            <el-form-item label="个人中心标题">
              <el-input
                v-model="form.pageTitle"
                maxlength="30"
              />
            </el-form-item>
            <el-form-item label="欢迎语">
              <el-input
                v-model="form.welcomeText"
                maxlength="80"
              />
            </el-form-item>
            <el-form-item label="订单区标题">
              <el-input
                v-model="form.orderTitle"
                maxlength="30"
              />
            </el-form-item>
            <el-form-item label="全部订单文案">
              <el-input
                v-model="form.allOrdersText"
                maxlength="30"
              />
            </el-form-item>
            <el-form-item label="服务区标题">
              <el-input
                v-model="form.serviceTitle"
                maxlength="30"
              />
            </el-form-item>
            <el-form-item label="订单状态配置">
              <div class="repeatable-editor">
                <div class="editor-toolbar">
                  <span>设置个人中心显示的订单入口及点击后打开的订单分类。</span>
                  <el-button
                    type="primary"
                    plain
                    icon="Plus"
                    @click="addOrderItem"
                  >
                    新增订单入口
                  </el-button>
                </div>
                <el-card
                  v-for="(item, index) in form.orderItems"
                  :key="`order-item-${index}`"
                  shadow="never"
                  class="editor-card"
                >
                  <template #header>
                    <div class="editor-card-header">
                      <span>订单入口 {{ index + 1 }}</span>
                      <el-button
                        link
                        type="danger"
                        @click="removeOrderItem(index)"
                      >
                        删除
                      </el-button>
                    </div>
                  </template>
                  <el-row :gutter="14">
                    <el-col :span="12">
                      <div class="field-label">
                        入口名称
                      </div>
                      <el-input
                        v-model="item.label"
                        maxlength="16"
                        placeholder="例如：待付款"
                      />
                    </el-col>
                    <el-col :span="12">
                      <div class="field-label">
                        图标类型
                      </div>
                      <el-input
                        v-model="item.icon"
                        maxlength="30"
                        placeholder="例如：wallet"
                      />
                    </el-col>
                    <el-col :span="12">
                      <div class="field-label">
                        点击后显示
                      </div>
                      <el-select
                        v-model="item.tab"
                        style="width: 100%"
                      >
                        <el-option
                          v-for="tab in orderTabs"
                          :key="tab.value"
                          :label="tab.label"
                          :value="tab.value"
                        />
                      </el-select>
                    </el-col>
                    <el-col :span="12">
                      <div class="field-label">
                        是否显示
                      </div>
                      <el-switch
                        v-model="item.isVisible"
                        active-value="1"
                        inactive-value="0"
                        inline-prompt
                        active-text="显示"
                        inactive-text="隐藏"
                      />
                    </el-col>
                  </el-row>
                </el-card>
                <el-empty
                  v-if="!form.orderItems.length"
                  description="暂无订单入口，点击“新增订单入口”添加"
                  :image-size="56"
                />
              </div>
            </el-form-item>
            <el-form-item label="暂不可用提示">
              <el-input
                v-model="form.unavailableText"
                maxlength="50"
              />
            </el-form-item>
          </template>
          <el-form-item label="顶部背景图">
            <MaterialImageField
              v-model="form.topBackground"
              :limit="1"
              :is-show-tip="false"
            />
            <div class="setting-tip">
              页面顶部背景图，建议尺寸：宽400 × 高400像素，支持 jpg、jpeg、png
            </div>
          </el-form-item>
          <el-button
            type="primary"
            :loading="saving"
            @click="saveSettings"
          >
            确定
          </el-button>
        </el-form>
      </el-tab-pane>
    </el-tabs>

    <el-dialog
      v-model="dialogVisible"
      :title="editingIndex === -1 ? '新增' : '编辑导航'"
      width="650px"
      append-to-body
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="itemForm"
        :rules="rules"
        label-width="110px"
      >
        <el-form-item
          label="名称"
          prop="name"
        >
          <el-input
            v-model="itemForm.name"
            maxlength="30"
            placeholder="请输入名称"
          />
        </el-form-item>
        <el-form-item label="图标">
          <MaterialImageField
            v-model="itemForm.icon"
            :limit="1"
            :is-show-tip="false"
            compact
          />
          <div class="setting-tip">
            建议尺寸：宽200像素 × 高200像素的 jpg、jpeg、png、gif 图片
          </div>
        </el-form-item>
        <el-form-item
          label="链接地址"
          prop="linkType"
        >
          <el-radio-group v-model="itemForm.linkType">
            <el-radio value="module">
              业务模块
            </el-radio>
            <el-radio value="custom">
              自定义链接
            </el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item
          v-if="itemForm.linkType === 'module'"
          label="系统模块"
          prop="modulePath"
        >
          <el-select
            v-model="itemForm.modulePath"
            class="module-select"
            placeholder="请选择业务模块"
          >
            <el-option
              v-for="item in modules"
              :key="item.path"
              :label="item.name"
              :value="item.path"
            />
          </el-select>
        </el-form-item>
        <el-form-item
          v-else
          label="自定义链接"
          prop="customLink"
        >
          <el-input
            v-model="itemForm.customLink"
            placeholder="请输入完整链接地址"
          />
          <div class="setting-tip">
            例如：https://www.example.com/；可配置关联公众号文章或外部业务页面
          </div>
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number
            v-model="itemForm.sortNum"
            :min="0"
            :max="999999"
          />
          <div class="setting-tip">
            只能填写大于等于0的整数，数字越大排序越靠前
          </div>
        </el-form-item>
        <el-form-item label="显示">
          <el-radio-group v-model="itemForm.isVisible">
            <el-radio value="1">
              显示
            </el-radio><el-radio value="0">
              隐藏
            </el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button
          type="primary"
          @click="submitItem"
        >
          确定
        </el-button>
        <el-button @click="dialogVisible = false">
          取消
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import NoticePanel from '@/components/Settings/NoticePanel'
import MaterialImageField from '@/components/MaterialImageField'
import { homeDecorationApi, profileDecorationApi } from '@/api/settings/decoration'

const props = defineProps({ scope: { type: String, required: true } })
const { proxy } = getCurrentInstance()
const api = computed(() => props.scope === 'home' ? homeDecorationApi : profileDecorationApi)
const activeTab = ref('navigation')
const saving = ref(false)
const loadError = ref('')
const dialogVisible = ref(false)
const editingIndex = ref(-1)
const formRef = ref()
const form = reactive(createSettings())
const itemForm = reactive(createItem())
const sortedItems = computed(() => [...form.items].sort((left, right) => Number(right.sortNum) - Number(left.sortNum)))
const orderTabs = [
  { label: '全部订单', value: 0 },
  { label: '待付款', value: 1 },
  { label: '待发货', value: 2 },
  { label: '待收货', value: 3 },
  { label: '已完成', value: 4 },
  { label: '已关闭', value: 5 }
]
const modules = [
  { name: '限时秒杀', path: '/bundle/pages/goods_seckill/index' },
  { name: '拼团活动', path: '/bundle/pages/group_buying/index' },
  { name: '热销榜单', path: '/pages/goods_rank/index' },
  { name: '领券中心', path: '/pages/coupon_list/index' },
  { name: '积分签到', path: '/pages/sign/index' },
  { name: '会员中心', path: '/pages/user_vip/user_vip' },
  { name: '我的收藏', path: '/pages/collect/index' },
  { name: '家政资讯', path: '/pages/news_list/news_list' },
  { name: '帮助中心', path: '/bundle/pages/live_broadcast/help' },
  { name: '砍价活动', path: '/bundle/pages/bargain/index' },
  { name: '抽奖活动', path: '/bundle/pages/lucky_draw/index' },
  { name: '核销订单', path: '/pages/verify/index' },
  { name: '砍价记录', path: '/pages/bargain/record' },
  { name: '联系客服', path: '/pages/customer_service/index' },
  { name: '我的拼团', path: '/pages/group_buying/my' },
  { name: '收货地址', path: '/pages/address/list' }
]
const rules = computed(() => ({
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  modulePath: [{ required: itemForm.linkType === 'module', message: '请选择业务模块', trigger: 'change' }],
  customLink: [{ required: itemForm.linkType === 'custom', message: '请输入链接地址', trigger: 'blur' }]
}))

function createSettings () {
  return { items: [], banners: [], orderItems: [], searchPlaceholder: '', seasonTitle: '', seasonSubtitle: '', rankingTitle: '', rankingSubtitle: '', moreText: '', buyText: '', cartSuccessText: '', unavailableText: '', pageTitle: '', welcomeText: '', orderTitle: '', allOrdersText: '', serviceTitle: '', shopLogo: '1', hotRanking: '1', newRecommend: '1', topBackground: '' }
}

function createBanner () {
  return { title: '', sub: '', tag: '', bg: '#FFF1E7', char: '', image: '', link: '' }
}

function createOrderItem () {
  return { icon: 'wallet', label: '', tab: 1, isVisible: '1' }
}

function createItem () {
  return { name: '', icon: '', linkType: 'module', modulePath: '', customLink: '', link: '', sortNum: 0, isVisible: '1' }
}

function defaultItems () {
  const names = props.scope === 'home'
    ? ['砍价活动', '限时秒杀', '帮助中心', '家政资讯', '抽奖活动', '会员中心']
    : ['核销订单', '砍价记录', '联系客服', '我的拼团', '帮助中心', '收货地址']
  return names.map((name, index) => {
    const module = modules.find(item => item.name === name)
    return {
      ...createItem(),
      id: `${props.scope}-nav-${index + 1}`,
      name,
      link: module?.path || '',
      modulePath: module?.path || '',
      sortNum: names.length - index
    }
  })
}

function getConfig (rows, key) {
  const record = rows.find(item => item.configKey === key)
  if (!record) return undefined
  try { return JSON.parse(record.configValue) } catch { return record.configValue }
}

function loadSettings () {
  api.value.listConfig({ pageNum: 1, pageSize: 100 }).then(response => {
    const rows = response.rows || []
    const errors = []
    const prefix = props.scope === 'home' ? 'decorate.home.' : 'decorate.profile.'
    const items = getConfig(rows, prefix + 'navItems')
    form.items = Array.isArray(items) ? items : defaultItems()
    for (const key of ['shopLogo', 'hotRanking', 'newRecommend', 'topBackground']) {
      const value = getConfig(rows, prefix + key)
      if (value !== undefined) form[key] = String(value)
    }
    const banners = getConfig(rows, prefix + 'banners')
    if (banners !== undefined) {
      if (Array.isArray(banners) && banners.every(item => item && typeof item === 'object' && !Array.isArray(item))) {
        form.banners = banners.map(item => ({ ...createBanner(), ...item }))
      } else {
        errors.push('首页轮播')
      }
    }
    const orderItems = getConfig(rows, prefix + 'orderItems')
    if (orderItems !== undefined) {
      if (Array.isArray(orderItems) && orderItems.every(item => item && typeof item === 'object' && !Array.isArray(item))) {
        form.orderItems = orderItems.map(item => ({ ...createOrderItem(), ...item, tab: Number(item.tab ?? 0), isVisible: String(item.isVisible ?? '1') }))
      } else {
        errors.push('订单入口')
      }
    }
    loadError.value = errors.length ? `${errors.join('、')}配置格式异常。为避免覆盖原数据，当前页面已禁用保存；请先修复配置后刷新。` : ''
    for (const key of ['searchPlaceholder', 'seasonTitle', 'seasonSubtitle', 'rankingTitle', 'rankingSubtitle', 'moreText', 'buyText', 'cartSuccessText', 'unavailableText', 'pageTitle', 'welcomeText', 'orderTitle', 'allOrdersText', 'serviceTitle']) {
      const value = getConfig(rows, prefix + key)
      if (value !== undefined) form[key] = String(value)
    }
  })
}

function addBanner () {
  form.banners.push(createBanner())
}

function removeBanner (index) {
  form.banners.splice(index, 1)
}

function addOrderItem () {
  form.orderItems.push(createOrderItem())
}

function removeOrderItem (index) {
  form.orderItems.splice(index, 1)
}

function openDialog (row) {
  editingIndex.value = row ? form.items.findIndex(item => item.id === row.id) : -1
  Object.assign(itemForm, createItem(), row || {})
  if (row?.link) {
    const selected = modules.find(item => item.path === row.link)
    itemForm.linkType = selected ? 'module' : 'custom'
    itemForm.modulePath = selected ? row.link : ''
    itemForm.customLink = selected ? '' : row.link
  }
  dialogVisible.value = true
  nextTick(() => formRef.value?.clearValidate())
}

function submitItem () {
  formRef.value.validate(valid => {
    if (!valid) return
    const link = itemForm.linkType === 'module' ? itemForm.modulePath : itemForm.customLink
    const item = { ...itemForm, link, id: itemForm.id || `nav-${Date.now()}` }
    if (editingIndex.value === -1) form.items.push(item)
    else form.items.splice(editingIndex.value, 1, item)
    dialogVisible.value = false
  })
}

function removeItem (row) {
  proxy.$modal.confirm('确认删除该导航菜单吗？').then(() => {
    const index = form.items.findIndex(item => item.id === row.id)
    if (index !== -1) form.items.splice(index, 1)
  }).catch(() => {})
}

async function saveSettings () {
  if (loadError.value) {
    proxy.$modal.msgError(loadError.value)
    return
  }
  saving.value = true
  const prefix = props.scope === 'home' ? 'decorate.home.' : 'decorate.profile.'
  const settings = { [prefix + 'navItems']: JSON.stringify(form.items) }
  if (props.scope === 'home') {
    settings[prefix + 'banners'] = JSON.stringify(form.banners)
    settings[prefix + 'searchPlaceholder'] = form.searchPlaceholder
    settings[prefix + 'seasonTitle'] = form.seasonTitle
    settings[prefix + 'seasonSubtitle'] = form.seasonSubtitle
    settings[prefix + 'rankingTitle'] = form.rankingTitle
    settings[prefix + 'rankingSubtitle'] = form.rankingSubtitle
    settings[prefix + 'moreText'] = form.moreText
    settings[prefix + 'buyText'] = form.buyText
    settings[prefix + 'cartSuccessText'] = form.cartSuccessText
    settings[prefix + 'unavailableText'] = form.unavailableText
    settings[prefix + 'shopLogo'] = form.shopLogo
    settings[prefix + 'hotRanking'] = form.hotRanking
    settings[prefix + 'newRecommend'] = form.newRecommend
  }
  if (props.scope === 'profile') {
    settings[prefix + 'orderItems'] = JSON.stringify(form.orderItems)
    for (const key of ['pageTitle', 'welcomeText', 'orderTitle', 'allOrdersText', 'serviceTitle']) {
      settings[prefix + key] = form[key]
    }
    settings[prefix + 'unavailableText'] = form.unavailableText
  }
  settings[prefix + 'topBackground'] = form.topBackground
  try {
    await api.value.saveConfig(settings)
    proxy.$modal.msgSuccess('保存成功')
  } finally {
    saving.value = false
  }
}

function imageUrl (value) {
  return value && value.startsWith('http') ? value : (import.meta.env.VITE_APP_BASE_API || '') + (value || '')
}

loadSettings()
</script>

<style scoped lang="scss">
.decoration-page {
  .toolbar { display: flex; justify-content: space-between; margin: 10px 0; }
  .nav-icon { width: 64px; height: 64px; }
  .other-form { padding: 20px 12px; max-width: 800px; }
  .other-form :deep(.el-form-item__content) { display: block; }
  .setting-tip { margin-top: 8px; color: var(--el-text-color-secondary); font-size: 12px; line-height: 1.6; }
  .module-select { width: 220px; }
  .repeatable-editor { width: 100%; }
  .editor-toolbar, .editor-card-header { display: flex; align-items: center; justify-content: space-between; gap: 12px; }
  .editor-toolbar { margin-bottom: 12px; color: var(--el-text-color-secondary); font-size: 13px; }
  .editor-card { margin-bottom: 12px; }
  .field-label { margin-bottom: 6px; color: var(--el-text-color-regular); font-size: 13px; }
  .config-warning { margin-bottom: 16px; }
}
</style>
