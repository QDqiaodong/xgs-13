<template>
  <div class="dashboard">
    <el-row :gutter="20" class="stat-row">
      <el-col :span="6">
        <el-card shadow="hover">
          <el-statistic title="设备总数" :value="stats.equipmentCount">
            <template #prefix>
              <el-icon><Monitor /></el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <el-statistic title="待处理工单" :value="stats.pendingOrderCount">
            <template #prefix>
              <el-icon><Document /></el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <el-statistic title="库存不足备件" :value="stats.lowStockCount">
            <template #prefix>
              <el-icon><Box /></el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <el-statistic title="即将到期维保" :value="stats.expiringCount">
            <template #prefix>
              <el-icon><AlarmClock /></el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="action-row">
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header>
            <span>快捷操作</span>
          </template>
          <div class="quick-actions">
            <el-button type="primary" @click="$router.push('/equipments')">设备档案</el-button>
            <el-button type="warning" @click="$router.push('/orders')">维保工单</el-button>
            <el-button type="success" @click="$router.push('/plans')">维保计划</el-button>
            <el-button type="info" @click="$router.push('/spare-parts')">备件库存</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="hover" class="table-card">
      <template #header>
        <span>即将到期维保设备</span>
      </template>
      <el-table :data="expiringEquipments" stripe style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="equipmentModel" label="设备型号" />
        <el-table-column prop="serialNumber" label="序列号" />
        <el-table-column prop="installAddress" label="安装地址" show-overflow-tooltip />
        <el-table-column prop="nextMaintenanceDate" label="下次维保日期" width="140" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Monitor, Document, Box, AlarmClock } from '@element-plus/icons-vue'
import { getEquipments, getOrders, getExpiringEquipments, getLowStockParts } from '../api'

const loading = ref(false)
const expiringEquipments = ref([])
const stats = reactive({
  equipmentCount: 0,
  pendingOrderCount: 0,
  lowStockCount: 0,
  expiringCount: 0
})

const statusTagType = (status) => {
  const map = { NORMAL: 'success', FAULT: 'danger', MAINTENANCE: 'warning' }
  return map[status] || 'info'
}

const statusLabel = (status) => {
  const map = { NORMAL: '正常', FAULT: '故障', MAINTENANCE: '维保中' }
  return map[status] || status
}

const loadStats = async () => {
  const [equipRes, orderRes, lowStockRes, expiringRes] = await Promise.all([
    getEquipments({ pageNum: 1, pageSize: 1 }),
    getOrders({ pageNum: 1, pageSize: 1, status: 'PENDING' }),
    getLowStockParts(),
    getExpiringEquipments()
  ])
  stats.equipmentCount = equipRes.data.total || 0
  stats.pendingOrderCount = orderRes.data.total || 0
  stats.lowStockCount = lowStockRes.data?.length || 0
  stats.expiringCount = expiringRes.data?.length || 0
  expiringEquipments.value = expiringRes.data || []
}

onMounted(() => {
  loading.value = true
  loadStats().finally(() => { loading.value = false })
})
</script>

<style scoped>
.dashboard {
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.stat-row :deep(.el-card__body) {
  text-align: center;
}
.quick-actions {
  display: flex;
  gap: 12px;
}
</style>
