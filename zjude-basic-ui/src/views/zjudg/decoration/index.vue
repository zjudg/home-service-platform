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
      </aside><main class="decoration-editor">
        <h3>{{ title }}</h3><template v-if="scope === 'home'">
          <el-form label-width="90px">
            <el-form-item label="是否启用">
              <el-radio-group v-model="home.enabled">
                <el-radio value="1">
                  开启
                </el-radio><el-radio value="0">
                  停用
                </el-radio>
              </el-radio-group>
            </el-form-item><el-form-item label="首页轮播图">
              <div
                v-for="(banner, index) in home.banners"
                :key="index"
                class="banner-row"
              >
                <image-upload
                  v-model="banner.image"
                  :limit="1"
                /><el-input
                  v-model="banner.name"
                  placeholder="图片名称"
                /><el-input
                  v-model="banner.link"
                  placeholder="图片链接"
                /><el-button
                  link
                  type="danger"
                  @click="home.banners.splice(index, 1)"
                >
                  删除
                </el-button>
              </div><el-button @click="home.banners.push({ image: '', name: '', link: '' })">
                添加轮播图
              </el-button>
            </el-form-item>
          </el-form>
        </template><template v-else-if="scope === 'profile'">
          <el-form label-width="100px">
            <el-form-item label="标题名称">
              <el-input v-model="profile.title" />
            </el-form-item><el-form-item
              v-for="(menu, index) in profile.menus"
              :key="index"
              label="菜单设置"
            >
              <div class="menu-row">
                <image-upload
                  v-model="menu.icon"
                  :limit="1"
                /><el-input
                  v-model="menu.name"
                  placeholder="菜单名称"
                /><el-checkbox v-model="menu.visible">
                  显示
                </el-checkbox>
              </div>
            </el-form-item><el-button @click="profile.menus.push({ name: '', icon: '', visible: true })">
              添加菜单
            </el-button>
          </el-form>
        </template><template v-else>
          <el-form label-width="100px">
            <el-form-item label="客服标题">
              <el-input v-model="customer.title" />
            </el-form-item><el-form-item label="服务时间">
              <el-input v-model="customer.time" />
            </el-form-item><el-form-item label="联系电话">
              <el-input v-model="customer.phone" />
            </el-form-item><el-form-item label="客服二维码">
              <image-upload
                v-model="customer.qrCode"
                :limit="1"
              />
            </el-form-item>
          </el-form>
        </template><el-button
          type="primary"
          class="save-button"
          @click="save"
        >
          保存设置
        </el-button>
      </main>
    </div>
  </div>
</template>
<script setup>
import { computed, reactive } from 'vue'; import { useRoute, useRouter } from 'vue-router'; import { ElMessage } from 'element-plus'
const route = useRoute(); const router = useRouter(); const scope = computed(() => route.query.scope || 'home'); const navItems = [{ key: 'home', label: '首页装修' }, { key: 'profile', label: '个人中心' }, { key: 'customer-service', label: '客服设置' }]; const title = computed(() => navItems.find(item => item.key === scope.value)?.label || '首页装修'); const home = reactive({ enabled: '1', banners: [{ image: '', name: '轮播图', link: '' }] }); const profile = reactive({ title: '我的预约', menus: [{ name: '待支付', icon: '', visible: true }, { name: '预约中', icon: '', visible: true }, { name: '服务中', icon: '', visible: true }, { name: '已完成', icon: '', visible: true }] }); const customer = reactive({ title: '添加客服二维码', time: '早上 9:00 - 22:00', phone: '', qrCode: '' }); function switchScope (value) { router.push({ query: { scope: value } }) } function save () { ElMessage.success('保存成功') }
</script>
<style scoped>.decoration-layout{display:flex;min-height:650px}.decoration-nav{width:145px;border-right:1px solid #dfe4ef;padding-top:8px}.decoration-nav div{padding:13px 16px;cursor:pointer}.decoration-nav .active{color:#405dff;background:#eef0ff;border-right:2px solid #405dff}.decoration-editor{flex:1;padding:0 30px;position:relative}.decoration-editor h3{font-weight:500;border-left:3px solid #405dff;padding-left:10px}.banner-row,.menu-row{display:flex;align-items:center;gap:12px;padding:14px;background:#fafafa;margin-bottom:12px;max-width:760px}.banner-row .image-upload,.menu-row .image-upload{width:100px}.save-button{margin-top:20px}.decoration-editor :deep(.el-input){max-width:360px}
</style>
