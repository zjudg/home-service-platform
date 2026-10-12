<template>
  <div class="app-container">
    <el-form
      :inline="true"
      :model="query"
    >
      <el-form-item label="订单编号">
        <el-input
          v-model="query.orderNo"
          clearable
          placeholder="请输入订单编号"
        />
      </el-form-item><el-form-item label="服务名称">
        <el-input
          v-model="query.serviceName"
          clearable
          placeholder="请输入服务名称"
        />
      </el-form-item><el-form-item label="用户信息">
        <el-input
          v-model="query.userInfo"
          clearable
          placeholder="昵称/手机号"
        />
      </el-form-item><el-form-item label="订单状态">
        <el-select
          v-model="query.orderStatus"
          clearable
        >
          <el-option
            label="待确认"
            value="0"
          /><el-option
            label="服务中"
            value="1"
          /><el-option
            label="已完成"
            value="2"
          /><el-option
            label="已取消"
            value="3"
          />
        </el-select>
      </el-form-item><el-form-item>
        <el-button
          type="primary"
          @click="load"
        >
          查询
        </el-button><el-button @click="reset">
          重置
        </el-button>
      </el-form-item>
    </el-form><el-table
      v-loading="loading"
      :data="rows"
    >
      <el-table-column
        label="订单编号"
        prop="orderNo"
        min-width="160"
      /><el-table-column
        label="服务名称"
        prop="serviceName"
      /><el-table-column
        label="用户信息"
        prop="userInfo"
      /><el-table-column
        label="预约日期"
        prop="appointmentDate"
      /><el-table-column
        label="预约时间段"
        prop="slotName"
      /><el-table-column
        label="订单金额"
        prop="amount"
      /><el-table-column label="订单状态">
        <template #default="{row}">
          {{ states[row.orderStatus]||row.orderStatus }}
        </template>
      </el-table-column><el-table-column label="支付状态">
        <template #default="{row}">
          {{ payments[row.paymentStatus]||row.paymentStatus }}
        </template>
      </el-table-column><el-table-column
        label="下单时间"
        prop="createTime"
      />
    </el-table><pagination
      v-show="total>0"
      v-model:page="query.pageNum"
      v-model:limit="query.pageSize"
      :total="total"
      @pagination="load"
    />
  </div>
</template>
<script setup>
import { ref, reactive, onMounted } from 'vue'; import { listOrder } from '@/api/zjudg/order'; const rows = ref([]); const total = ref(0); const loading = ref(false); const query = reactive({ pageNum: 1, pageSize: 10, orderNo: '', serviceName: '', userInfo: '', orderStatus: '' }); const states = { 0: '待确认', 1: '服务中', 2: '已完成', 3: '已取消' }; const payments = { 0: '未支付', 1: '已支付', 2: '已退款' }; const load = () => { loading.value = true; listOrder(query).then(r => { rows.value = r.rows || []; total.value = r.total || 0 }).finally(() => { loading.value = false }) }; const reset = () => { Object.assign(query, { pageNum: 1, orderNo: '', serviceName: '', userInfo: '', orderStatus: '' }); load() }; onMounted(load)
</script>
