<template>
  <div class="orders-page">
    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane label="工单列表" name="list">
        <el-card shadow="never" style="margin-bottom: 16px">
          <el-form :model="searchForm" inline>
            <el-form-item label="关键词">
              <el-input v-model="searchForm.keyword" placeholder="工单号/工程师" clearable style="width: 160px" />
            </el-form-item>
            <el-form-item label="客户">
              <el-select v-model="searchForm.customerId" placeholder="请选择" clearable style="width: 160px">
                <el-option v-for="c in customers" :key="c.id" :label="c.name" :value="c.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="searchForm.orderStatus" placeholder="请选择" clearable style="width: 130px">
                <el-option label="待处理" value="PENDING" />
                <el-option label="进行中" value="IN_PROGRESS" />
                <el-option label="已完成" value="COMPLETED" />
                <el-option label="已取消" value="CANCELLED" />
              </el-select>
            </el-form-item>
            <el-form-item label="类型">
              <el-select v-model="searchForm.orderType" placeholder="请选择" clearable style="width: 130px">
                <el-option label="定期维保" value="PERIODIC" />
                <el-option label="故障维修" value="FAULT" />
                <el-option label="清洗保养" value="CLEANING" />
              </el-select>
            </el-form-item>
            <el-form-item label="日期范围">
              <el-date-picker
                v-model="searchForm.dateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                value-format="YYYY-MM-DD"
                style="width: 260px"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSearch">搜索</el-button>
              <el-button @click="handleResetSearch">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <el-card shadow="never">
          <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px">
            <div style="display: flex; align-items: center; gap: 12px">
              <el-button type="primary" @click="handleAdd">新增工单</el-button>
              <el-button
                type="warning"
                :disabled="selectedRows.length === 0"
                @click="handleBatchStatus"
              >
                批量修改状态 ({{ selectedRows.length }})
              </el-button>
            </div>
          </div>

          <el-table
            :data="tableData"
            border
            stripe
            @selection-change="handleSelectionChange"
            style="width: 100%"
          >
            <el-table-column type="selection" width="50" align="center" />
            <el-table-column prop="orderNo" label="工单号" width="170" />
            <el-table-column prop="equipmentModel" label="设备型号" width="140">
              <template #default="{ row }">
                {{ getEquipmentModel(row.equipmentId) }}
              </template>
            </el-table-column>
            <el-table-column prop="customerName" label="客户" width="140">
              <template #default="{ row }">
                {{ getCustomerName(row.customerId) }}
              </template>
            </el-table-column>
            <el-table-column prop="orderType" label="工单类型" width="110" align="center">
              <template #default="{ row }">
                <el-tag :type="orderTypeTagType(row.orderType)">{{ orderTypeLabel(row.orderType) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="orderStatus" label="状态" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="statusTagType(row.orderStatus)">{{ statusLabel(row.orderStatus) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="planDate" label="计划日期" width="120" align="center" />
            <el-table-column prop="actualDate" label="实际日期" width="120" align="center">
              <template #default="{ row }">{{ row.actualDate || '-' }}</template>
            </el-table-column>
            <el-table-column prop="engineer" label="工程师" width="100">
              <template #default="{ row }">{{ row.engineer || '-' }}</template>
            </el-table-column>
            <el-table-column label="操作" min-width="260" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" size="small" @click="handleView(row)">详情</el-button>
                <el-button link type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
                <el-button
                  link type="warning" size="small"
                  :disabled="row.orderStatus === 'COMPLETED' || row.orderStatus === 'CANCELLED'"
                  @click="handleStatusChange(row)"
                >改状态</el-button>
                <el-button
                  link type="success" size="small"
                  :disabled="row.orderStatus === 'COMPLETED'"
                  @click="handleComplete(row)"
                >完工</el-button>
                <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>

          <el-pagination
            style="margin-top: 16px; justify-content: flex-end"
            v-model:current-page="pagination.pageNum"
            v-model:page-size="pagination.pageSize"
            :page-sizes="[10, 20, 50]"
            :total="pagination.total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="loadOrders"
            @current-change="loadOrders"
          />
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="维保日历" name="calendar">
        <el-card shadow="never">
          <div style="display: flex; align-items: center; justify-content: center; margin-bottom: 16px; gap: 20px">
            <el-button @click="calPrevMonth" :icon="ArrowLeft">上月</el-button>
            <span style="font-size: 18px; font-weight: bold; min-width: 140px; text-align: center">
              {{ calYear }}年{{ calMonth }}月
            </span>
            <el-button @click="calNextMonth">下月<el-icon style="margin-left: 4px"><ArrowRight /></el-icon></el-button>
          </div>
          <div class="calendar-grid">
            <div class="calendar-header" v-for="d in weekDays" :key="d">{{ d }}</div>
            <div
              v-for="(cell, idx) in calCells"
              :key="idx"
              class="calendar-cell"
              :class="{ 'other-month': !cell.currentMonth, today: cell.isToday }"
            >
              <div class="cell-date">{{ cell.day }}</div>
              <div class="cell-orders">
                <el-tag
                  v-for="o in cell.orders"
                  :key="o.id"
                  size="small"
                  :type="statusTagType(o.orderStatus)"
                  style="margin: 1px 0; cursor: pointer; max-width: 100%; overflow: hidden; text-overflow: ellipsis; white-space: nowrap"
                  @click="handleView(o)"
                >
                  {{ o.orderNo }}
                </el-tag>
              </div>
            </div>
          </div>
        </el-card>
      </el-tab-pane>
    </el-tabs>

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

      <el-divider content-position="left">备件消耗</el-divider>
      <div style="margin-bottom: 10px; display: flex; gap: 8px; align-items: center">
        <el-select v-model="addPartForm.partId" placeholder="选择备件" filterable style="width: 200px">
          <el-option v-for="p in spareParts" :key="p.id" :label="`${p.partName}(${p.partModel})`" :value="p.id" />
        </el-select>
        <el-input-number v-model="addPartForm.quantity" :min="1" placeholder="数量" style="width: 120px" />
        <el-button type="primary" size="small" @click="handleAddPart">添加</el-button>
      </div>
      <el-table :data="detailParts" border size="small">
        <el-table-column prop="partName" label="备件名称" />
        <el-table-column prop="partModel" label="型号" />
        <el-table-column prop="spec" label="规格" />
        <el-table-column prop="unit" label="单位" width="70" />
        <el-table-column prop="quantity" label="数量" width="70" />
        <el-table-column prop="unitPrice" label="单价" width="90" />
        <el-table-column label="操作" width="70">
          <template #default="{ row }">
            <el-button link type="danger" size="small" @click="handleRemovePart(row)">移除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <el-dialog v-model="formVisible" :title="isEdit ? '编辑工单' : '新增工单'" width="600px" destroy-on-close>
      <el-alert
        v-if="isEdit && isOrderCompleted"
        title="工单已完成，仅可修改备注字段"
        type="warning"
        :closable="false"
        style="margin-bottom: 16px"
      />
      <el-form :model="formData" :rules="formRules" ref="formRef" label-width="90px">
        <el-form-item label="客户" prop="customerId">
          <el-select
            v-model="formData.customerId"
            placeholder="请选择客户"
            @change="handleCustomerChange"
            style="width: 100%"
            :disabled="isEdit && isOrderCompleted"
          >
            <el-option v-for="c in customers" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="设备" prop="equipmentId">
          <el-select
            v-model="formData.equipmentId"
            placeholder="请选择设备"
            style="width: 100%"
            :disabled="isEdit && isOrderCompleted"
          >
            <el-option v-for="e in filteredEquipments" :key="e.id" :label="e.equipmentModel" :value="e.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="工单类型" prop="orderType">
          <el-select
            v-model="formData.orderType"
            placeholder="请选择"
            style="width: 100%"
            :disabled="isEdit && isOrderCompleted"
          >
            <el-option label="定期维保" value="PERIODIC" />
            <el-option label="故障维修" value="FAULT" />
            <el-option label="清洗保养" value="CLEANING" />
          </el-select>
        </el-form-item>
        <el-form-item label="计划日期" prop="planDate">
          <el-date-picker
            v-model="formData.planDate"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="选择日期"
            style="width: 100%"
            :disabled="isEdit && isOrderCompleted"
          />
        </el-form-item>
        <el-form-item label="工程师" prop="engineer">
          <el-input
            v-model="formData.engineer"
            placeholder="请输入工程师"
            :disabled="isEdit && isOrderCompleted"
          />
        </el-form-item>
        <el-form-item label="工时" prop="workHours">
          <el-input-number
            v-model="formData.workHours"
            :min="0"
            :precision="1"
            style="width: 100%"
            :disabled="isEdit && isOrderCompleted"
          />
        </el-form-item>
        <el-form-item label="故障描述" prop="faultDescription">
          <el-input
            v-model="formData.faultDescription"
            type="textarea"
            :rows="3"
            :disabled="isEdit && isOrderCompleted"
          />
        </el-form-item>
        <template v-if="isEdit">
          <el-form-item label="工作内容" prop="workContent">
            <el-input
              v-model="formData.workContent"
              type="textarea"
              :rows="3"
              :disabled="isOrderCompleted"
            />
          </el-form-item>
          <el-form-item label="解决方案" prop="solution">
            <el-input
              v-model="formData.solution"
              type="textarea"
              :rows="3"
              :disabled="isOrderCompleted"
            />
          </el-form-item>
        </template>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="formData.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitForm">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="completeVisible" title="完工登记" width="550px" destroy-on-close>
      <el-form :model="completeForm" :rules="completeRules" ref="completeFormRef" label-width="90px">
        <el-form-item label="实际日期" prop="actualDate">
          <el-date-picker v-model="completeForm.actualDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" />
        </el-form-item>
        <el-form-item label="工程师" prop="engineer">
          <el-input v-model="completeForm.engineer" />
        </el-form-item>
        <el-form-item label="工时" prop="workHours">
          <el-input-number v-model="completeForm.workHours" :min="0" :precision="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="工作内容" prop="workContent">
          <el-input v-model="completeForm.workContent" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="故障描述" prop="faultDescription">
          <el-input v-model="completeForm.faultDescription" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="解决方案" prop="solution">
          <el-input v-model="completeForm.solution" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="completeVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitComplete">确认完工</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="statusChangeVisible" title="修改状态" width="400px" destroy-on-close>
      <el-form label-width="80px">
        <el-form-item label="当前状态">
          <el-tag :type="statusTagType(statusChangeRow?.orderStatus)">{{ statusLabel(statusChangeRow?.orderStatus) }}</el-tag>
        </el-form-item>
        <el-form-item label="新状态">
          <el-select v-model="statusChangeValue" placeholder="请选择" style="width: 100%">
            <el-option label="待处理" value="PENDING" />
            <el-option label="进行中" value="IN_PROGRESS" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="已取消" value="CANCELLED" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="statusChangeVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitStatusChange">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="batchStatusVisible" title="批量修改状态" width="700px" destroy-on-close>
      <el-form label-width="80px">
        <el-form-item label="目标状态">
          <el-select v-model="batchStatusValue" placeholder="请选择" style="width: 200px" @change="handleBatchStatusChange">
            <el-option label="待处理" value="PENDING" />
            <el-option label="进行中" value="IN_PROGRESS" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="已取消" value="CANCELLED" />
          </el-select>
        </el-form-item>
      </el-form>

      <div v-if="batchStatusCheckResult" style="margin-top: 8px">
        <div style="margin-bottom: 8px; display: flex; gap: 16px">
          <span>共 <strong>{{ batchStatusCheckResult.totalCount }}</strong> 条</span>
          <span style="color: #67c23a">可流转 <strong>{{ batchStatusCheckResult.validCount }}</strong> 条</span>
          <span style="color: #f56c6c">不可流转 <strong>{{ batchStatusCheckResult.invalidCount }}</strong> 条</span>
        </div>

        <el-tabs v-model="batchStatusTab" type="border-card" style="margin-top: 12px">
          <el-tab-pane label="可流转工单" name="valid">
            <el-table :data="batchStatusCheckResult.validItems" border size="small" max-height="250">
              <el-table-column prop="orderNo" label="工单号" width="150" />
              <el-table-column prop="customerName" label="客户" min-width="100" />
              <el-table-column prop="equipmentModel" label="设备型号" min-width="100" />
              <el-table-column prop="currentStatusLabel" label="当前状态" width="90" align="center">
                <template #default="{ row }">
                  <el-tag :type="statusTagType(row.currentStatus)" size="small">{{ row.currentStatusLabel }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="targetStatusLabel" label="目标状态" width="90" align="center">
                <template #default="{ row }">
                  <el-tag :type="statusTagType(row.targetStatus)" size="small">{{ row.targetStatusLabel }}</el-tag>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>
          <el-tab-pane label="不可流转工单" name="invalid">
            <el-table :data="batchStatusCheckResult.invalidItems" border size="small" max-height="250">
              <el-table-column prop="orderNo" label="工单号" width="150" />
              <el-table-column prop="customerName" label="客户" min-width="100" />
              <el-table-column prop="equipmentModel" label="设备型号" min-width="100" />
              <el-table-column prop="currentStatusLabel" label="当前状态" width="90" align="center">
                <template #default="{ row }">
                  <el-tag :type="statusTagType(row.currentStatus)" size="small">{{ row.currentStatusLabel }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="targetStatusLabel" label="目标状态" width="90" align="center">
                <template #default="{ row }">
                  <el-tag :type="statusTagType(row.targetStatus)" size="small">{{ row.targetStatusLabel }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="reason" label="原因" min-width="120" />
            </el-table>
          </el-tab-pane>
        </el-tabs>
      </div>

      <template #footer>
        <el-button @click="batchStatusVisible = false">取消</el-button>
        <el-button
          type="primary"
          :disabled="!batchStatusCheckResult || batchStatusCheckResult.validCount === 0"
          @click="handleSubmitBatchStatus"
        >
          确定修改 ({{ batchStatusCheckResult?.validCount || 0 }})
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, ArrowRight } from '@element-plus/icons-vue'
import {
  getOrders, getOrder, createOrder, updateOrder, deleteOrder,
  updateOrderStatus, completeOrder, checkBatchOrderStatus, batchUpdateOrderStatus,
  getOrdersByDateRange, getOrderParts, addOrderPart, removeOrderPart,
  getAllCustomers, getEquipmentCategories, getSpareParts
} from '../api'
import { getEquipments } from '../api'

const activeTab = ref('list')

const customers = ref([])
const categories = ref([])
const equipments = ref([])
const spareParts = ref([])

const searchForm = reactive({
  keyword: '',
  customerId: null,
  orderStatus: '',
  orderType: '',
  dateRange: null
})

const tableData = ref([])
const pagination = reactive({ pageNum: 1, pageSize: 10, total: 0 })
const selectedRows = ref([])

const statusMap = { PENDING: '待处理', IN_PROGRESS: '进行中', COMPLETED: '已完成', CANCELLED: '已取消' }
const orderTypeMap = { PERIODIC: '定期维保', FAULT: '故障维修', CLEANING: '清洗保养' }
const statusTagMap = { PENDING: 'info', IN_PROGRESS: 'warning', COMPLETED: 'success', CANCELLED: 'danger' }
const orderTypeTagMap = { PERIODIC: '', FAULT: 'danger', CLEANING: 'success' }

const statusLabel = (s) => statusMap[s] || s
const orderTypeLabel = (t) => orderTypeMap[t] || t
const statusTagType = (s) => statusTagMap[s] || 'info'
const orderTypeTagType = (t) => orderTypeTagMap[t] || ''

const getCustomerName = (id) => customers.value.find(c => c.id === id)?.name || '-'
const getEquipmentModel = (id) => equipments.value.find(e => e.id === id)?.equipmentModel || '-'

async function loadOrders() {
  const params = {
    pageNum: pagination.pageNum,
    pageSize: pagination.pageSize,
    keyword: searchForm.keyword || undefined,
    customerId: searchForm.customerId || undefined,
    orderStatus: searchForm.orderStatus || undefined,
    orderType: searchForm.orderType || undefined
  }
  if (searchForm.dateRange && searchForm.dateRange.length === 2) {
    params.startDate = searchForm.dateRange[0]
    params.endDate = searchForm.dateRange[1]
  }
  const res = await getOrders(params)
  tableData.value = res.data.records || []
  pagination.total = res.data.total || 0
}

function handleSearch() {
  pagination.pageNum = 1
  loadOrders()
}

function handleResetSearch() {
  Object.assign(searchForm, { keyword: '', customerId: null, orderStatus: '', orderType: '', dateRange: null })
  pagination.pageNum = 1
  loadOrders()
}

function handleSelectionChange(rows) {
  selectedRows.value = rows
}

const formVisible = ref(false)
const isEdit = ref(false)
const isOrderCompleted = ref(false)
const formRef = ref(null)
const formData = reactive({
  id: null,
  customerId: null,
  equipmentId: null,
  orderType: 'PERIODIC',
  planDate: '',
  engineer: '',
  workHours: null,
  faultDescription: '',
  workContent: '',
  solution: '',
  remark: ''
})

const formRules = {
  customerId: [{ required: true, message: '请选择客户', trigger: 'change' }],
  equipmentId: [{ required: true, message: '请选择设备', trigger: 'change' }],
  orderType: [{ required: true, message: '请选择工单类型', trigger: 'change' }],
  planDate: [{ required: true, message: '请选择计划日期', trigger: 'change' }]
}

const filteredEquipments = computed(() => {
  if (!formData.customerId) return []
  return equipments.value.filter(e => e.customerId === formData.customerId)
})

function handleCustomerChange() {
  formData.equipmentId = null
}

function handleAdd() {
  isEdit.value = false
  isOrderCompleted.value = false
  Object.assign(formData, {
    id: null, customerId: null, equipmentId: null, orderType: 'PERIODIC',
    planDate: '', engineer: '', workHours: null, faultDescription: '',
    workContent: '', solution: '', remark: ''
  })
  formVisible.value = true
}

async function handleEdit(row) {
  isEdit.value = true
  isOrderCompleted.value = row.orderStatus === 'COMPLETED'
  const res = await getOrder(row.id)
  const o = res.data
  Object.assign(formData, {
    id: o.id, customerId: o.customerId, equipmentId: o.equipmentId,
    orderType: o.orderType, planDate: o.planDate, engineer: o.engineer,
    workHours: o.workHours, faultDescription: o.faultDescription || '',
    workContent: o.workContent || '', solution: o.solution || '', remark: o.remark || ''
  })
  formVisible.value = true
}

async function handleSubmitForm() {
  await formRef.value.validate()
  if (isEdit.value) {
    await updateOrder(formData.id, { ...formData })
    ElMessage.success('修改成功')
  } else {
    const orderNo = 'WO' + Date.now()
    await createOrder({ ...formData, orderNo })
    ElMessage.success('新增成功')
  }
  formVisible.value = false
  loadOrders()
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确定删除工单 ${row.orderNo}？`, '提示', { type: 'warning' })
  await deleteOrder(row.id)
  ElMessage.success('删除成功')
  loadOrders()
}

const detailVisible = ref(false)
const detailData = ref(null)
const detailParts = ref([])
const addPartForm = reactive({ partId: null, quantity: 1 })

async function handleView(row) {
  const res = await getOrder(row.id)
  detailData.value = res.data
  detailVisible.value = true
  loadDetailParts(row.id)
}

async function loadDetailParts(orderId) {
  const res = await getOrderParts(orderId)
  detailParts.value = res.data || []
}

async function handleAddPart() {
  if (!addPartForm.partId || !addPartForm.quantity) {
    ElMessage.warning('请选择备件并输入数量')
    return
  }
  const part = spareParts.value.find(p => p.id === addPartForm.partId)
  await addOrderPart(detailData.value.id, {
    partId: addPartForm.partId,
    partName: part.partName,
    partModel: part.partModel,
    spec: part.spec,
    unit: part.unit,
    quantity: addPartForm.quantity,
    unitPrice: part.unitPrice
  })
  ElMessage.success('添加成功')
  addPartForm.partId = null
  addPartForm.quantity = 1
  loadDetailParts(detailData.value.id)
}

async function handleRemovePart(row) {
  await removeOrderPart(detailData.value.id, row.id)
  ElMessage.success('移除成功')
  loadDetailParts(detailData.value.id)
}

const completeVisible = ref(false)
const completeFormRef = ref(null)
const completeForm = reactive({
  orderId: null,
  actualDate: '',
  engineer: '',
  workHours: null,
  workContent: '',
  faultDescription: '',
  solution: ''
})

const completeRules = {
  actualDate: [{ required: true, message: '请选择实际日期', trigger: 'change' }],
  engineer: [{ required: true, message: '请输入工程师', trigger: 'blur' }]
}

function handleComplete(row) {
  completeForm.orderId = row.id
  Object.assign(completeForm, {
    actualDate: '', engineer: row.engineer || '',
    workHours: row.workHours || null, workContent: '',
    faultDescription: row.faultDescription || '', solution: ''
  })
  completeVisible.value = true
}

async function handleSubmitComplete() {
  await completeFormRef.value.validate()
  await completeOrder(completeForm.orderId, { ...completeForm })
  ElMessage.success('完工登记成功')
  completeVisible.value = false
  loadOrders()
}

const statusChangeVisible = ref(false)
const statusChangeRow = ref(null)
const statusChangeValue = ref('')

function handleStatusChange(row) {
  statusChangeRow.value = row
  statusChangeValue.value = ''
  statusChangeVisible.value = true
}

async function handleSubmitStatusChange() {
  if (!statusChangeValue.value) {
    ElMessage.warning('请选择新状态')
    return
  }
  await updateOrderStatus(statusChangeRow.value.id, statusChangeValue.value)
  ElMessage.success('状态修改成功')
  statusChangeVisible.value = false
  loadOrders()
}

const batchStatusVisible = ref(false)
const batchStatusValue = ref('')
const batchStatusTab = ref('valid')
const batchStatusCheckResult = ref(null)

function handleBatchStatus() {
  batchStatusValue.value = ''
  batchStatusCheckResult.value = null
  batchStatusVisible.value = true
}

async function handleBatchStatusChange() {
  if (!batchStatusValue.value || selectedRows.value.length === 0) {
    batchStatusCheckResult.value = null
    return
  }
  try {
    const res = await checkBatchOrderStatus({
      ids: selectedRows.value.map(r => r.id),
      targetStatus: batchStatusValue.value
    })
    batchStatusCheckResult.value = res.data
  } catch (e) {
    ElMessage.error('状态检查失败')
  }
}

async function handleSubmitBatchStatus() {
  if (!batchStatusCheckResult.value || batchStatusCheckResult.value.validCount === 0) {
    ElMessage.warning('没有可流转的工单')
    return
  }
  const validIds = batchStatusCheckResult.value.validItems.map(item => item.orderId)
  await batchUpdateOrderStatus({
    ids: validIds,
    status: batchStatusValue.value
  })
  ElMessage.success(`成功修改 ${validIds.length} 条工单状态`)
  batchStatusVisible.value = false
  loadOrders()
}

const weekDays = ['一', '二', '三', '四', '五', '六', '日']
const calYear = ref(new Date().getFullYear())
const calMonth = ref(new Date().getMonth() + 1)
const calOrders = ref([])

const calCells = computed(() => {
  const firstDay = new Date(calYear.value, calMonth.value - 1, 1)
  let startWeekday = firstDay.getDay()
  startWeekday = startWeekday === 0 ? 6 : startWeekday - 1

  const daysInMonth = new Date(calYear.value, calMonth.value, 0).getDate()
  const prevMonthDays = new Date(calYear.value, calMonth.value - 1, 0).getDate()

  const today = new Date()
  const todayStr = `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, '0')}-${String(today.getDate()).padStart(2, '0')}`

  const cells = []
  const totalCells = Math.ceil((startWeekday + daysInMonth) / 7) * 7

  for (let i = 0; i < totalCells; i++) {
    const dayOffset = i - startWeekday + 1
    let dateStr, currentMonth, day

    if (dayOffset < 1) {
      day = prevMonthDays + dayOffset
      const pm = calMonth.value === 1 ? 12 : calMonth.value - 1
      const py = calMonth.value === 1 ? calYear.value - 1 : calYear.value
      dateStr = `${py}-${String(pm).padStart(2, '0')}-${String(day).padStart(2, '0')}`
      currentMonth = false
    } else if (dayOffset > daysInMonth) {
      day = dayOffset - daysInMonth
      const nm = calMonth.value === 12 ? 1 : calMonth.value + 1
      const ny = calMonth.value === 12 ? calYear.value + 1 : calYear.value
      dateStr = `${ny}-${String(nm).padStart(2, '0')}-${String(day).padStart(2, '0')}`
      currentMonth = false
    } else {
      day = dayOffset
      dateStr = `${calYear.value}-${String(calMonth.value).padStart(2, '0')}-${String(day).padStart(2, '0')}`
      currentMonth = true
    }

    const orders = calOrders.value.filter(o => o.planDate === dateStr)

    cells.push({
      day,
      dateStr,
      currentMonth,
      isToday: dateStr === todayStr,
      orders
    })
  }

  return cells
})

function calPrevMonth() {
  if (calMonth.value === 1) {
    calMonth.value = 12
    calYear.value--
  } else {
    calMonth.value--
  }
  loadCalendarOrders()
}

function calNextMonth() {
  if (calMonth.value === 12) {
    calMonth.value = 1
    calYear.value++
  } else {
    calMonth.value++
  }
  loadCalendarOrders()
}

async function loadCalendarOrders() {
  const startDate = `${calYear.value}-${String(calMonth.value).padStart(2, '0')}-01`
  const endDate = `${calYear.value}-${String(calMonth.value).padStart(2, '0')}-${String(new Date(calYear.value, calMonth.value, 0).getDate()).padStart(2, '0')}`
  const res = await getOrdersByDateRange(startDate, endDate)
  calOrders.value = res.data || []
}

function handleTabChange(tab) {
  if (tab === 'calendar') {
    loadCalendarOrders()
  }
}

onMounted(async () => {
  const [custRes, catRes, eqRes, spRes] = await Promise.all([
    getAllCustomers(),
    getEquipmentCategories(),
    getEquipments({ pageSize: 9999 }),
    getSpareParts({ pageSize: 9999 })
  ])
  customers.value = custRes.data || []
  categories.value = catRes.data || []
  equipments.value = eqRes.data?.list || eqRes.data || []
  spareParts.value = spRes.data?.list || spRes.data || []
  loadOrders()
})
</script>

<style scoped>
.orders-page { padding: 0; }
.calendar-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  border-top: 1px solid #ebeef5;
  border-left: 1px solid #ebeef5;
}
.calendar-header {
  padding: 8px;
  text-align: center;
  font-weight: bold;
  background: #f5f7fa;
  border-right: 1px solid #ebeef5;
  border-bottom: 1px solid #ebeef5;
}
.calendar-cell {
  min-height: 90px;
  padding: 4px 6px;
  border-right: 1px solid #ebeef5;
  border-bottom: 1px solid #ebeef5;
  overflow: hidden;
}
.calendar-cell.other-month {
  background: #fafafa;
}
.calendar-cell.today .cell-date {
  background: #409eff;
  color: #fff;
  border-radius: 50%;
  width: 22px;
  height: 22px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.cell-date {
  font-size: 13px;
  font-weight: 500;
  margin-bottom: 2px;
}
.cell-orders {
  display: flex;
  flex-direction: column;
}
</style>
