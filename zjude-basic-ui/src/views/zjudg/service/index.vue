<template>
  <div class="app-container">
    <el-form
      :inline="true"
      :model="query"
    >
      <el-form-item label="服务名称">
        <el-input
          v-model="query.serviceName"
          clearable
          placeholder="请输入服务名称"
        />
      </el-form-item>
      <el-form-item label="服务分类">
        <el-select
          v-model="query.categoryId"
          clearable
          placeholder="请选择"
        >
          <el-option
            v-for="item in categories"
            :key="item.categoryId"
            :label="item.categoryName"
            :value="item.categoryId"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="发布城市">
        <el-select
          v-model="query.cityId"
          clearable
          filterable
          placeholder="全部城市"
        >
          <el-option
            v-for="item in cityOptions"
            :key="item.cityId"
            :label="item.cityName"
            :value="item.cityId"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button
          type="primary"
          @click="load"
        >
          查询
        </el-button><el-button @click="reset">
          重置
        </el-button>
      </el-form-item>
    </el-form>
    <el-tabs
      v-model="query.status"
      @tab-change="load"
    >
      <el-tab-pane
        label="全部"
        name=""
      /><el-tab-pane
        label="销售中"
        name="0"
      /><el-tab-pane
        label="仓库中"
        name="1"
      />
    </el-tabs>
    <div class="mb8">
      <el-button
        type="primary"
        @click="openForm()"
      >
        添加服务
      </el-button><el-button
        plain
        :disabled="!selected.length"
      >
        批量上架
      </el-button><el-button
        plain
        :disabled="!selected.length"
      >
        批量下架
      </el-button><el-button
        plain
        :disabled="!selected.length"
        @click="removeSelected"
      >
        批量删除
      </el-button>
    </div>
    <el-table
      v-loading="loading"
      :data="rows"
      @selection-change="selected = $event"
    >
      <el-table-column
        type="selection"
        width="50"
      />
      <el-table-column
        label="服务名称"
        prop="serviceName"
        min-width="180"
      />
      <el-table-column
        label="商品用途"
        width="110"
      >
        <template #default="{ row }">
          <el-tag :type="Number(row.extraTimeOnly) === 1 ? 'warning' : 'info'">
            {{ Number(row.extraTimeOnly) === 1 ? '补时专用' : '普通服务' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column
        label="商品图"
        width="100"
      >
        <template #default="{ row }">
          <image-preview
            v-if="getServiceImage(row.productImage || row.carouselImages)"
            :src="getServiceImage(row.productImage || row.carouselImages)"
            width="64"
            height="64"
          />
        </template>
      </el-table-column>
      <el-table-column
        label="服务分类"
        prop="categoryName"
      />
      <el-table-column
        label="价格"
        prop="price"
      />
      <el-table-column
        label="中介费"
      >
        <template #default="{ row }">
          {{ formatMoney(getCommissionFee(row)) }}
        </template>
      </el-table-column>
      <el-table-column label="扣款方式">
        <template #default="{ row }">
          {{ row.commissionType === 'RATE' ? `按比例 ${formatPercent(row.commissionRate)}` : '固定金额' }}
        </template>
      </el-table-column>
      <el-table-column
        label="单位"
        prop="unitName"
      />
      <el-table-column
        label="预约人数"
        prop="reserveCount"
      />
      <el-table-column label="销售状态">
        <template #default="{ row }">
          <el-tag :type="row.status === '0' ? 'success' : 'info'">
            {{ row.status === '0' ? '销售中' : '仓库中' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column
        label="发布城市"
        width="110"
      >
        <template #default="{ row }">
          {{ row.cityName || '全国' }}
        </template>
      </el-table-column>
      <el-table-column
        label="排序"
        prop="sortNum"
      />
      <el-table-column
        label="添加时间"
        prop="createTime"
        width="170"
      />
      <el-table-column
        label="操作"
        width="150"
      >
        <template #default="{ row }">
          <el-button
            link
            type="primary"
            @click="openForm(row.serviceId)"
          >
            编辑
          </el-button>
          <el-button
            link
            type="danger"
            @click="remove([row.serviceId])"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination
      v-show="total > 0"
      v-model:page="query.pageNum"
      v-model:limit="query.pageSize"
      :total="total"
      @pagination="load"
    />
    <el-dialog
      v-model="formVisible"
      :title="form.serviceId ? '编辑服务' : '新增服务'"
      width="760px"
    >
      <el-tabs v-model="activeTab">
        <el-tab-pane
          label="基础设置"
          name="base"
        >
          <el-form
            ref="formRef"
            class="service-form"
            :model="form"
            label-width="130px"
          >
            <el-form-item
              label="服务名称"
              required
            >
              <el-input
                v-model="form.serviceName"
                maxlength="64"
                show-word-limit
              />
            </el-form-item><el-form-item label="服务简介">
              <el-input v-model="form.serviceIntro" />
            </el-form-item><el-form-item
              label="服务分类"
              required
            >
              <el-select v-model="form.categoryId">
                <el-option
                  v-for="item in categories"
                  :key="item.categoryId"
                  :label="item.categoryName"
                  :value="item.categoryId"
                />
              </el-select>
            </el-form-item><el-form-item label="发布城市">
              <el-select
                v-model="form.cityId"
                clearable
                filterable
                placeholder="留空表示全国可用"
              >
                <el-option
                  v-for="item in cityOptions"
                  :key="item.cityId"
                  :label="`${item.provinceName} ${item.cityName}`"
                  :value="item.cityId"
                />
              </el-select>
              <div class="form-tip">
                留空时该服务对所有城市可见；选择城市后仅该城市可见
              </div>
            </el-form-item><el-form-item
              class="form-item-wide"
              label="详情主图"
              required
            >
              <MaterialImageField
                v-model="form.detailImage"
                :limit="1"
              />
              <div class="form-tip">
                服务详情页顶部展示，建议上传清晰横图或方图
              </div>
            </el-form-item><el-form-item
              class="form-item-wide"
              label="商品图"
              required
            >
              <MaterialImageField
                v-model="form.productImage"
                :limit="1"
              />
              <div class="form-tip">
                商品列表、首页推荐、搜索和预约摘要中展示
              </div>
            </el-form-item><el-form-item
              label="价格"
              required
            >
              <el-input-number
                v-model="form.price"
                :min="0"
                :precision="2"
                @change="syncPayoutPrice"
              />
            </el-form-item><el-form-item label="划线价">
              <el-input-number
                v-model="form.originalPrice"
                :min="0"
                :precision="2"
              />
            </el-form-item><el-form-item
              class="form-item-wide"
              label="平台扣款方式"
              required
            >
              <el-radio-group
                v-model="form.commissionType"
                @change="syncPayoutPrice"
              >
                <el-radio-button value="FIXED">
                  固定金额
                </el-radio-button>
                <el-radio-button value="RATE">
                  按百分比
                </el-radio-button>
              </el-radio-group>
            </el-form-item><el-form-item
              v-if="form.commissionType === 'FIXED'"
              label="中介费"
              required
            >
              <el-input-number
                v-model="form.commissionFee"
                :min="0"
                :max="Number(form.price || 0)"
                :precision="2"
                @change="syncPayoutPrice"
              />
              <div class="form-tip">
                每个服务单位扣固定金额；师傅到手 = 卖价 − 中介费。例如 90 − 15 = 75
              </div>
            </el-form-item><el-form-item
              v-else
              label="中介费比例"
              required
            >
              <el-input-number
                v-model="form.commissionRatePercent"
                :min="0"
                :max="100"
                :precision="2"
                :step="1"
                @change="syncPayoutPrice"
              />%
              <div class="form-tip">
                按服务卖价计算平台中介费，师傅到手 = 卖价 × (1 − 扣款比例)
              </div>
            </el-form-item><el-form-item
              label="师傅到手"
              required
            >
              <el-input-number
                :model-value="calculatePayout(form)"
                :min="0"
                :precision="2"
                disabled
              />
            </el-form-item><el-form-item
              label="服务单位"
              required
            >
              <el-select v-model="form.unitId">
                <el-option
                  v-for="item in units"
                  :key="item.unitId"
                  :label="item.unitName"
                  :value="item.unitId"
                />
              </el-select>
            </el-form-item><el-form-item label="排序">
              <el-input-number
                v-model="form.sortNum"
                :min="0"
              />
            </el-form-item><el-form-item label="商品状态">
              <el-radio-group v-model="form.status">
                <el-radio value="0">
                  立即上架
                </el-radio><el-radio value="1">
                  放入仓库
                </el-radio>
              </el-radio-group>
            </el-form-item>
          </el-form>
        </el-tab-pane><el-tab-pane
          label="服务详情"
          name="detail"
          lazy
        >
          <editor
            v-model="form.detailContent"
            :min-height="350"
            :auto-focus="true"
          /><el-divider /><div
            class="detail-preview"
            v-html="form.detailContent"
          />
        </el-tab-pane>
      </el-tabs><template #footer>
        <el-button @click="formVisible = false">
          取消
        </el-button><el-button
          type="primary"
          @click="save"
        >
          保存
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import MaterialImageField from '@/components/MaterialImageField'
import { listService, getService, addService, updateService, deleteService } from '@/api/zjudg/service'
import { listCategory } from '@/api/zjudg/category'
import { listUnit } from '@/api/zjudg/unit'
import { listCity } from '@/api/zjudg/city'

const loading = ref(false); const rows = ref([]); const total = ref(0); const selected = ref([]); const categories = ref([]); const units = ref([]); const cityOptions = ref([]); const formVisible = ref(false); const activeTab = ref('base')
const query = reactive({ pageNum: 1, pageSize: 10, serviceName: '', categoryId: '', cityId: '', status: '' }); const form = reactive({})
function load () { loading.value = true; listService(query).then(res => { rows.value = res.rows || []; total.value = res.total || 0 }).finally(() => { loading.value = false }) }
function reset () { Object.assign(query, { pageNum: 1, serviceName: '', categoryId: '', cityId: '', status: '' }); load() }
function blank () { Object.keys(form).forEach(key => delete form[key]); Object.assign(form, { serviceName: '', serviceIntro: '', categoryId: '', cityId: '', carouselImages: '', detailImage: '', productImage: '', price: 0, commissionType: 'FIXED', commissionRate: 0, commissionRatePercent: 0, commissionFee: 0, masterPrice: 0, originalPrice: null, unitId: '', extraTimeOnly: 0, sortNum: 0, status: '1', detailContent: '' }) }
function formatMoney (value) { return `¥${Number(value || 0).toFixed(2)}` }
function formatPercent (value) { return `${(Number(value || 0) * 100).toFixed(2).replace(/\\.00$/, '')}%` }
function getCommissionFee (row) { return row.commissionType === 'RATE' ? Number(row.price || 0) * Number(row.commissionRate || 0) : Number(row.price || 0) - Number(row.masterPrice || 0) }
function calculatePayout (item) { const price = Number(item.price || 0); return item.commissionType === 'RATE' ? Number((price * (1 - Number(item.commissionRatePercent || 0) / 100)).toFixed(2)) : Number((price - Number(item.commissionFee || 0)).toFixed(2)) }
function syncPayoutPrice () { form.masterPrice = calculatePayout(form); form.commissionRate = form.commissionType === 'RATE' ? Number((Number(form.commissionRatePercent || 0) / 100).toFixed(4)) : 0 }
function getServiceImage (images) {
  if (Array.isArray(images)) return images[0] || ''
  if (typeof images !== 'string') return ''
  return images.split(',').map(image => image.trim()).find(Boolean) || ''
}
function openForm (id) { blank(); activeTab.value = 'base'; formVisible.value = true; if (id) getService(id).then(res => { Object.assign(form, res.data); const legacyImage = getServiceImage(res.data.carouselImages); form.detailImage = res.data.detailImage || legacyImage; form.productImage = res.data.productImage || legacyImage; form.commissionType = res.data.commissionType || 'RATE'; form.commissionRatePercent = Number((Number(res.data.commissionRate || 0) * 100).toFixed(2)); form.commissionFee = Number((Number(res.data.price || 0) - Number(res.data.masterPrice || 0)).toFixed(2)); syncPayoutPrice() }) }
function save () { const price = Number(form.price || 0); const rate = Number(form.commissionRatePercent || 0); const fee = Number(form.commissionFee || 0); const selectedUnit = units.value.find(item => item.unitId === form.unitId); const isExtraTimeProduct = String(form.serviceName || '').trim() === '补时专用'; if (isExtraTimeProduct && selectedUnit?.measureType !== 'HOUR') { ElMessage.error('补时专用服务必须使用按小时计量的服务单位'); return }; if (!form.detailImage) { ElMessage.error('请上传服务详情主图'); return }; if (!form.productImage) { ElMessage.error('请上传商品列表图'); return }; if (form.commissionType === 'RATE' && (rate < 0 || rate > 100)) { ElMessage.error('中介费比例需在0%到100%之间'); return }; if (form.commissionType === 'FIXED' && (fee < 0 || fee > price)) { ElMessage.error('中介费不能高于服务卖价'); return }; syncPayoutPrice(); const payload = { ...form, extraTimeOnly: isExtraTimeProduct ? 1 : 0 }; delete payload.commissionFee; delete payload.commissionRatePercent; const action = form.serviceId ? updateService : addService; action(payload).then(() => { ElMessage.success('保存成功'); formVisible.value = false; load() }) }
function remove (ids) { ElMessageBox.confirm('确认删除选中的服务吗？', '提示', { type: 'warning' }).then(() => deleteService(ids.join(',')).then(() => { ElMessage.success('删除成功'); load() })) }
function removeSelected () { remove(selected.value.map(item => item.serviceId)) }
onMounted(() => { Promise.all([listCategory({ pageSize: 100 }), listUnit({ pageSize: 100 }), listCity({ pageSize: 500 })]).then(([c, u, ct]) => { categories.value = c.rows || []; units.value = u.rows || []; cityOptions.value = ct.rows || [] }); load() })
</script>

<style scoped>
.service-form {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  column-gap: 24px;
  width: 100%;
}

.service-form :deep(.el-form-item) {
  min-width: 0;
  margin-right: 0;
}

.service-form :deep(.form-item-wide) {
  grid-column: 1 / -1;
}

.service-form :deep(.el-form-item__label) {
  white-space: nowrap;
}

.service-form :deep(.el-form-item__content) {
  min-width: 0;
}

.service-form :deep(.el-select) {
  max-width: 100%;
}

.detail-preview{border:1px solid #ebeef5;padding:16px;min-height:80px}
.form-tip{color:#909399;font-size:12px}
.mb8{margin-bottom:8px}

@media (max-width: 760px) {
  .service-form {
    grid-template-columns: minmax(0, 1fr);
  }

  .service-form :deep(.form-item-wide) {
    grid-column: auto;
  }

}
</style>
