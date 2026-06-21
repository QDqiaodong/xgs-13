<template>
  <div class="schedule-page">
    <el-card shadow="never" style="margin-bottom: 16px">
      <el-form :model="filterForm" inline>
        <el-form-item label="视图">
          <el-radio-group v-model="viewMode" @change="handleViewModeChange">
            <el-radio-button label="week">周视图</el-radio-button>
            <el-radio-button label="month">月视图</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="日期范围">
          <el-date-picker
            v-model="filterForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 260px"
            @change="handleDateRangeChange"
          />
        </el-form-item>
        <el-form-item label="工程师">
          <el-select
            v-model="filterForm.engineer"
            placeholder="全部工程师"
            clearable
            filterable
            style="width: 160px"
            @change="loadData"
          >
            <el-option
              v-for="eng in engineerList"
              :key="eng"
              :label="eng"
              :value="eng"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select
            v-model="filterForm.orderStatus"
            placeholder="全部状态"
            clearable
            style="width: 140px"
            @change="loadData"
          >
            <el-option label="待执行" value="PENDING" />
            <el-option label="执行中" value="IN_PROGRESS" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="已取消" value="CANCELLED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button @click="handlePrevPeriod" :icon="ArrowLeft">上一周期</el-button>
          <el-button @click="handleToday">今天</el-button>
          <el-button @click="handleNextPeriod">下一周期<el-icon style="margin-left: 4px"><ArrowRight /></el-icon></el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <div style="display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px">
        <div style="font-size: 16px; font-weight: bold; color: #303133">
          {{ periodLabel }}
        </div>
        <div style="display: flex; align-items: center; gap: 16px">
          <div class="legend-item"><span class="legend-dot status-pending"></span>待执行</div>
          <div class="legend-item"><span class="legend-dot status-in-progress"></span>执行中</div>
          <div class="legend-item"><span class="legend-dot status-completed"></span>已完成</div>
          <div class="legend-item"><span class="legend-dot status-conflict"></span>时间冲突</div>
        </div>
      </div>

      <div v-loading="loading" class="gantt-wrapper">
        <div v-if="engineers.length === 0" class="empty-tip">
          <el-empty description="暂无工程师工单数据" />
        </div>

        <div v-else class="gantt-container">
          <div class="gantt-header">
            <div class="gantt-corner">工程师</div>
            <div
              class="gantt-day-header"
              v-for="day in dateList"
              :key="day.dateStr"
              :class="{ today: day.isToday, weekend: day.isWeekend }"
            >
              <div class="day-week">{{ day.weekLabel }}</div>
              <div class="day-date">{{ day.month }}/{{ day.day }}</div>
            </div>
          </div>

          <div class="gantt-body">
            <div
              class="gantt-row"
              v-for="eng in engineers"
              :key="eng.name"
            >
              <div class="gantt-engineer">
                <span class="eng-name">{{ eng.name }}</span>
                <el-tooltip v-if="eng.conflictCount > 0" content="存在时间冲突" placement="top">
                  <el-badge
                    :value="eng.conflictCount"
                    :max="99"
                    class="conflict-badge"
                    type="danger"
                  />
                </el-tooltip>
                <span class="eng-load" :style="{ color: loadColor(eng.loadRate) }">
                  负载 {{ eng.loadRate }}%
                </span>
              </div>
              <div
                class="gantt-day-cell"
                v-for="day in dateList"
                :key="eng.name + '-' + day.dateStr"
                :class="{ weekend: day.isWeekend, today: day.isToday }"
              >
                <div
                  v-for="(task, idx) in eng.tasksByDate[day.dateStr] || []"
                  :key="task.id"
                  class="gantt-task"
                  :class="[
                    'status-' + task.orderStatus.toLowerCase(),
                    { conflict: task.conflict }
                  ]"
                  :style="{ top: (idx * 30) + 'px' }"
                  @click="handleTaskClick(task)"
                >
                  <el-tooltip placement="top" :show-after="200">
                    <template #content>
                      <div class="task-tooltip">
                        <div><strong>{{ task.orderNo }}</strong></div>
                        <div>客户: {{ getCustomerName(task.customerId) }}</div>
                        <div>设备: {{ getEquipmentModel(task.equipmentId) }}</div>
                        <div>类型: {{ orderTypeLabel(task.orderType) }}</div>
                        <div>状态: {{ statusLabel(task.orderStatus) }}</div>
                        <div v-if="task.workHours">工时: {{ task.workHours }}h</div>
                        <div v-if="task.engineer">工程师: {{ task.engineer }}</div>
                      </div>
                    </template>
                    <span class="task-content">
                      <span class="task-order-no">{{ task.orderNo }}</span>
                      <span v-if="task.workHours" class="task-hours">{{ task.workHours }}h</span>
                    </span>
                  </el-tooltip>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-card>

    <el-dialog v-model="detailVisible" title="工单详情" width="750px" destroy-on-close>
      <el-descriptions :column="2" border v-if="detailData">
        <el-descriptions-item label="工单号">{{ detailData.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusTagType(detailData.orderStatus)">{{ statusLabel(detailData.orderStatus) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="工单类型">
          <el-tag :type="orderTypeTagType(detailData.orderType)">{{ orderTypeLabel(detailData.orderType) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="客户">{{ getCustomerName(detailData.customerId) }}</el-descriptions-item>
        <el-descriptions-item label="设备型号">{{ getEquipmentModel(detailData.equipmentId) }}</el-descriptions-item>
        <el-descriptions-item label="工程师">{{ detailData.engineer || '-' }}</el-descriptions-item>
        <el-descriptions-item label="计划日期">{{ detailData.planDate }}</el-descriptions-item>
        <el-descriptions-item label="实际日期">{{ detailData.actualDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="工时">{{ detailData.workHours ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ detailData.createdAt }}</el-descriptions-item>
        <el-descriptions-item label="故障描述" :span="2">{{ detailData.faultDescription || '-' }}</el-descriptions-item>
        <el-descriptions-item label="工作内容" :span="2">{{ detailData.workContent || '-' }}</el-descriptions-item>
        <el-descriptions-item label="解决方案" :span="2">{{ detailData.solution || '-' }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detailData.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ArrowLeft, ArrowRight } from '@element-plus/icons-vue'
import {
  getOrdersByDateRange,
  getOrder,
  getAllCustomers,
  getEquipments
} from '../api'

const loading = ref(false)
const viewMode = ref('week')

const today = new Date()
const startOfWeek = (date) => {
  const d = new Date(date)
  const day = d.getDay() || 7
  d.setDate(d.getDate() - day + 1)
  return d
}
const endOfWeek = (date) => {
  const d = startOfWeek(date)
  d.setDate(d.getDate() + 6)
  return d
}
const startOfMonth = (date) => new Date(date.getFullYear(), date.getMonth(), 1)
const endOfMonth = (date) => new Date(date.getFullYear(), date.getMonth() + 1, 0)
const formatDate = (d) => `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`

const filterForm = reactive({
  dateRange: [],
  engineer: '',
  orderStatus: ''
})

const initDefaultRange = () => {
  const start = startOfWeek(today)
  const end = endOfWeek(today)
  filterForm.dateRange = [formatDate(start), formatDate(end)]
}

const customers = ref([])
const equipments = ref([])
const allOrders = ref([])

const dateList = computed(() => {
  if (!filterForm.dateRange || filterForm.dateRange.length !== 2) return []
  const start = new Date(filterForm.dateRange[0])
  const end = new Date(filterForm.dateRange[1])
  const list = []
  const todayStr = formatDate(new Date())
  const weekMap = ['日', '一', '二', '三', '四', '五', '六']
  for (let d = new Date(start); d <= end; d.setDate(d.getDate() + 1)) {
    const dateStr = formatDate(d)
    const dayOfWeek = d.getDay()
    list.push({
      dateStr,
      day: d.getDate(),
      month: d.getMonth() + 1,
      weekLabel: '周' + weekMap[dayOfWeek],
      isWeekend: dayOfWeek === 0 || dayOfWeek === 6,
      isToday: dateStr === todayStr
    })
  }
  return list
})

const periodLabel = computed(() => {
  if (!filterForm.dateRange || filterForm.dateRange.length !== 2) return ''
  const [s, e] = filterForm.dateRange
  if (s === e) return s
  return `${s} 至 ${e}`
})

const engineerList = computed(() => {
  const set = new Set()
  allOrders.value.forEach(o => {
    if (o.engineer) set.add(o.engineer)
  })
  return Array.from(set).sort()
})

const filteredOrders = computed(() => {
  let orders = allOrders.value
  if (filterForm.engineer) {
    orders = orders.filter(o => o.engineer === filterForm.engineer)
  }
  if (filterForm.orderStatus) {
    orders = orders.filter(o => o.orderStatus === filterForm.orderStatus)
  }
  return orders
})

const engineers = computed(() => {
  const map = new Map()
  filteredOrders.value.forEach(order => {
    const engName = order.engineer || '未分配'
    if (!map.has(engName)) {
      map.set(engName, {
        name: engName,
        tasks: [],
        tasksByDate: {},
        conflictCount: 0,
        loadRate: 0
      })
    }
    map.get(engName).tasks.push(order)
  })

  const result = []
  map.forEach(eng => {
    const tasksByDate = {}
    dateList.value.forEach(d => {
      tasksByDate[d.dateStr] = []
    })
    eng.tasks.forEach(task => {
      const date = task.planDate
      if (tasksByDate[date]) {
        tasksByDate[date].push(task)
      }
    })

    let conflictCount = 0
    Object.keys(tasksByDate).forEach(date => {
      const dayTasks = tasksByDate[date]
      if (dayTasks.length > 1) {
        conflictCount++
        dayTasks.forEach(t => { t.conflict = true })
      } else {
        dayTasks.forEach(t => { t.conflict = false })
      }
    })

    const totalDays = dateList.value.length
    const busyDays = Object.values(tasksByDate).filter(list => list.length > 0).length
    const loadRate = totalDays > 0 ? Math.round((busyDays / totalDays) * 100) : 0

    eng.tasksByDate = tasksByDate
    eng.conflictCount = conflictCount
    eng.loadRate = loadRate
    result.push(eng)
  })

  result.sort((a, b) => {
    if (a.name === '未分配') return 1
    if (b.name === '未分配') return -1
    return a.name.localeCompare(b.name)
  })

  return result
})

const statusMap = { PENDING: '待执行', IN_PROGRESS: '执行中', COMPLETED: '已完成', CANCELLED: '已取消' }
const orderTypeMap = { PERIODIC: '定期维保', FAULT: '故障维修', CLEANING: '清洗保养' }
const statusTagMap = { PENDING: 'info', IN_PROGRESS: 'warning', COMPLETED: 'success', CANCELLED: 'danger' }
const orderTypeTagMap = { PERIODIC: '', FAULT: 'danger', CLEANING: 'success' }

const statusLabel = (s) => statusMap[s] || s
const orderTypeLabel = (t) => orderTypeMap[t] || t
const statusTagType = (s) => statusTagMap[s] || 'info'
const orderTypeTagType = (t) => orderTypeTagMap[t] || ''

const getCustomerName = (id) => customers.value.find(c => c.id === id)?.name || '-'
const getEquipmentModel = (id) => equipments.value.find(e => e.id === id)?.equipmentModel || '-'

const loadColor = (rate) => {
  if (rate >= 80) return '#f56c6c'
  if (rate >= 50) return '#e6a23c'
  return '#67c23a'
}

async function loadData() {
  if (!filterForm.dateRange || filterForm.dateRange.length !== 2) return
  loading.value = true
  try {
    const res = await getOrdersByDateRange(filterForm.dateRange[0], filterForm.dateRange[1])
    allOrders.value = (res.data || []).filter(o => o.orderStatus !== 'CANCELLED')
  } finally {
    loading.value = false
  }
}

const detailVisible = ref(false)
const detailData = ref(null)

async function handleTaskClick(task) {
  const res = await getOrder(task.id)
  detailData.value = res.data
  detailVisible.value = true
}

function handleViewModeChange() {
  if (viewMode.value === 'week') {
    const base = new Date(filterForm.dateRange?.[0] || today)
    filterForm.dateRange = [formatDate(startOfWeek(base)), formatDate(endOfWeek(base))]
  } else {
    const base = new Date(filterForm.dateRange?.[0] || today)
    filterForm.dateRange = [formatDate(startOfMonth(base)), formatDate(endOfMonth(base))]
  }
  loadData()
}

function handleDateRangeChange() {
  loadData()
}

function handlePrevPeriod() {
  if (!filterForm.dateRange || filterForm.dateRange.length !== 2) return
  const start = new Date(filterForm.dateRange[0])
  if (viewMode.value === 'week') {
    start.setDate(start.getDate() - 7)
    const end = new Date(start)
    end.setDate(end.getDate() + 6)
    filterForm.dateRange = [formatDate(start), formatDate(end)]
  } else {
    start.setMonth(start.getMonth() - 1)
    const end = endOfMonth(start)
    filterForm.dateRange = [formatDate(startOfMonth(start)), formatDate(end)]
  }
  loadData()
}

function handleNextPeriod() {
  if (!filterForm.dateRange || filterForm.dateRange.length !== 2) return
  const start = new Date(filterForm.dateRange[0])
  if (viewMode.value === 'week') {
    start.setDate(start.getDate() + 7)
    const end = new Date(start)
    end.setDate(end.getDate() + 6)
    filterForm.dateRange = [formatDate(start), formatDate(end)]
  } else {
    start.setMonth(start.getMonth() + 1)
    const end = endOfMonth(start)
    filterForm.dateRange = [formatDate(startOfMonth(start)), formatDate(end)]
  }
  loadData()
}

function handleToday() {
  if (viewMode.value === 'week') {
    filterForm.dateRange = [formatDate(startOfWeek(today)), formatDate(endOfWeek(today))]
  } else {
    filterForm.dateRange = [formatDate(startOfMonth(today)), formatDate(endOfMonth(today))]
  }
  loadData()
}

onMounted(async () => {
  initDefaultRange()
  const [custRes, eqRes] = await Promise.all([
    getAllCustomers(),
    getEquipments({ pageSize: 9999 })
  ])
  customers.value = custRes.data || []
  equipments.value = eqRes.data?.list || eqRes.data || []
  loadData()
})
</script>

<style scoped>
.schedule-page { padding: 0; }

.legend-item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #606266;
}
.legend-dot {
  display: inline-block;
  width: 12px;
  height: 12px;
  border-radius: 2px;
}
.legend-dot.status-pending { background: #909399; }
.legend-dot.status-in-progress { background: #e6a23c; }
.legend-dot.status-completed { background: #67c23a; }
.legend-dot.status-conflict { background: #f56c6c; }

.gantt-wrapper { min-height: 300px; }
.empty-tip { padding: 40px 0; }

.gantt-container {
  display: flex;
  flex-direction: column;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  overflow: auto;
  max-height: calc(100vh - 340px);
}

.gantt-header {
  display: flex;
  position: sticky;
  top: 0;
  z-index: 10;
  background: #f5f7fa;
  border-bottom: 1px solid #ebeef5;
}
.gantt-corner {
  flex: 0 0 180px;
  min-width: 180px;
  padding: 10px 12px;
  font-weight: bold;
  color: #303133;
  border-right: 1px solid #ebeef5;
  text-align: center;
  position: sticky;
  left: 0;
  background: #f5f7fa;
  z-index: 11;
}
.gantt-day-header {
  flex: 0 0 110px;
  min-width: 110px;
  padding: 8px 4px;
  text-align: center;
  border-right: 1px solid #ebeef5;
  font-size: 13px;
}
.gantt-day-header.today {
  background: #ecf5ff;
}
.gantt-day-header.weekend {
  background: #fafafa;
}
.day-week {
  font-size: 12px;
  color: #909399;
}
.day-date {
  font-weight: bold;
  color: #303133;
  font-size: 14px;
}
.gantt-day-header.today .day-date {
  color: #409eff;
}

.gantt-body { display: flex; flex-direction: column; }

.gantt-row {
  display: flex;
  border-bottom: 1px solid #ebeef5;
  min-height: 60px;
}
.gantt-row:hover { background: #fafafa; }

.gantt-engineer {
  flex: 0 0 180px;
  min-width: 180px;
  padding: 10px 12px;
  border-right: 1px solid #ebeef5;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  position: sticky;
  left: 0;
  background: #fff;
  z-index: 5;
}
.gantt-row:hover .gantt-engineer { background: #fafafa; }
.eng-name {
  font-weight: 500;
  color: #303133;
  font-size: 14px;
}
.conflict-badge { margin-left: auto; }
.eng-load {
  font-size: 12px;
  font-weight: 500;
}

.gantt-day-cell {
  flex: 0 0 110px;
  min-width: 110px;
  border-right: 1px solid #ebeef5;
  position: relative;
  min-height: 60px;
}
.gantt-day-cell.weekend {
  background: #fcfcfc;
}
.gantt-day-cell.today {
  background: #f5faff;
}

.gantt-task {
  position: absolute;
  left: 4px;
  right: 4px;
  height: 24px;
  border-radius: 4px;
  padding: 0 8px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 12px;
  color: #fff;
  cursor: pointer;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
  transition: transform 0.15s ease, box-shadow 0.15s ease;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
}
.gantt-task:hover {
  transform: translateY(-1px);
  box-shadow: 0 3px 8px rgba(0,0,0,0.2);
  z-index: 1;
}
.gantt-task.status-pending { background: #909399; }
.gantt-task.status-in_progress { background: #e6a23c; }
.gantt-task.status-completed { background: #67c23a; }
.gantt-task.status-conflict,
.gantt-task.conflict {
  background: repeating-linear-gradient(
    45deg,
    #f56c6c,
    #f56c6c 6px,
    #f89898 6px,
    #f89898 12px
  );
}
.task-content {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  overflow: hidden;
}
.task-order-no {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.task-hours {
  flex-shrink: 0;
  background: rgba(255,255,255,0.2);
  padding: 0 4px;
  border-radius: 2px;
  font-size: 11px;
  margin-left: 4px;
}

.task-tooltip {
  line-height: 1.8;
  font-size: 13px;
}
</style>
