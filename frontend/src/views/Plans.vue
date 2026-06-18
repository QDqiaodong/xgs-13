<template>
  <div>
    <el-card shadow="never" style="margin-bottom: 16px">
      <el-form :inline="true" :model="searchForm" @submit.prevent="handleSearch">
        <el-form-item label="设备">
          <el-select v-model="searchForm.equipmentId" placeholder="全部设备" clearable style="width: 180px">
            <el-option v-for="e in equipments" :key="e.id" :label="e.equipmentModel" :value="e.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="计划状态">
          <el-select v-model="searchForm.planStatus" placeholder="全部" clearable style="width: 130px">
            <el-option label="待执行" value="PENDING" />
            <el-option label="已完成" value="COMPLETED" />
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
          <el-button @click="handleReset">重置</el-button>
          <el-button type="success" @click="handleAdd">新增计划</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <el-table :data="tableData" border stripe style="width: 100%">
        <el-table-column prop="planDate" label="计划日期" width="120" align="center" />
        <el-table-column label="设备型号" min-width="140">
          <template #default="{ row }">{{ resolveEquipmentModel(row.equipmentId) }}</template>
        </el-table-column>
        <el-table-column prop="planType" label="计划类型" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="row.planType === 'INSPECTION' ? 'warning' : ''">
              {{ planTypeLabel(row.planType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="cycleMonths" label="周期" width="100" align="center">
          <template #default="{ row }">{{ cycleLabel(row.cycleMonths) }}</template>
        </el-table-column>
        <el-table-column prop="planStatus" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.planStatus === 'PENDING' ? 'warning' : 'success'">
              {{ planStatusLabel(row.planStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="150" show-overflow-tooltip>
          <template #default="{ row }">{{ row.remark || '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right" align="center">
          <template #default="{ row }">
            <el-button
              link type="success" size="small"
              :disabled="row.planStatus === 'COMPLETED'"
              @click="handleComplete(row)"
            >完成</el-button>
            <el-button link type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
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
        @size-change="loadData"
        @current-change="loadData"
      />
    </el-card>

    <el-dialog v-model="formVisible" :title="isEdit ? '编辑计划' : '新增计划'" width="550px" destroy-on-close>
      <el-form :model="formData" :rules="formRules" ref="formRef" label-width="90px">
        <el-form-item label="设备" prop="equipmentId">
          <el-select v-model="formData.equipmentId" placeholder="请选择设备" style="width: 100%">
            <el-option v-for="e in equipments" :key="e.id" :label="e.equipmentModel" :value="e.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="计划日期" prop="planDate">
          <el-date-picker v-model="formData.planDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" />
        </el-form-item>
        <el-form-item label="计划类型" prop="planType">
          <el-select v-model="formData.planType" placeholder="请选择" style="width: 100%">
            <el-option label="定期维保" value="PERIODIC" />
            <el-option label="巡检" value="INSPECTION" />
          </el-select>
        </el-form-item>
        <el-form-item label="维保周期" prop="cycleMonths">
          <el-select v-model="formData.cycleMonths" placeholder="请选择" style="width: 100%">
            <el-option label="季度" :value="3" />
            <el-option label="半年度" :value="6" />
            <el-option label="年度" :value="12" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="formData.remark" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitForm">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="completeVisible" title="完成计划" width="550px" destroy-on-close>
      <el-form :model="completeForm" ref="completeFormRef" label-width="90px">
        <el-form-item label="关联工单" prop="orderOption">
          <el-radio-group v-model="completeForm.orderOption">
            <el-radio value="select">选择已有工单</el-radio>
            <el-radio value="create">新建工单</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="completeForm.orderOption === 'select'" label="选择工单" prop="orderId">
          <el-select v-model="completeForm.orderId" placeholder="请选择工单" filterable style="width: 100%">
            <el-option v-for="o in orders" :key="o.id" :label="`${o.orderNo} - ${o.planDate}`" :value="o.id" />
          </el-select>
        </el-form-item>
        <template v-if="completeForm.orderOption === 'create'">
          <el-form-item label="工单类型" prop="orderType">
            <el-select v-model="completeForm.orderType" placeholder="请选择" style="width: 100%">
              <el-option label="定期维保" value="PERIODIC" />
              <el-option label="故障维修" value="FAULT" />
              <el-option label="清洗保养" value="CLEANING" />
            </el-select>
          </el-form-item>
          <el-form-item label="计划日期" prop="planDate">
            <el-date-picker v-model="completeForm.planDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" />
          </el-form-item>
          <el-form-item label="工程师" prop="engineer">
            <el-input v-model="completeForm.engineer" placeholder="请输入工程师" />
          </el-form-item>
        </template>
      </el-form>
      <template #footer>
        <el-button @click="completeVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitComplete">确认完成</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getPlans, createPlan, updatePlan, deletePlan, completePlan, getEquipments, getOrders, createOrder } from '../api'

const equipments = ref([])
const orders = ref([])
const tableData = ref([])
const pagination = reactive({ pageNum: 1, pageSize: 10, total: 0 })

const searchForm = reactive({
  equipmentId: null,
  planStatus: '',
  dateRange: null
})

const planTypeLabel = (t) => {
  const map = { PERIODIC: '定期维保', INSPECTION: '巡检' }
  return map[t] || t
}

const cycleLabel = (m) => {
  const map = { 3: '季度', 6: '半年度', 12: '年度' }
  return map[m] || `${m}个月`
}

const planStatusLabel = (s) => {
  const map = { PENDING: '待执行', COMPLETED: '已完成' }
  return map[s] || s
}

const resolveEquipmentModel = (id) => {
  const e = equipments.value.find(item => item.id === id)
  return e ? e.equipmentModel : '-'
}

async function loadData() {
  const params = {
    pageNum: pagination.pageNum,
    pageSize: pagination.pageSize,
    equipmentId: searchForm.equipmentId || undefined,
    planStatus: searchForm.planStatus || undefined
  }
  if (searchForm.dateRange && searchForm.dateRange.length === 2) {
    params.startDate = searchForm.dateRange[0]
    params.endDate = searchForm.dateRange[1]
  }
  const res = await getPlans(params)
  tableData.value = res.data.list || res.data.records || []
  pagination.total = res.data.total || 0
}

function handleSearch() {
  pagination.pageNum = 1
  loadData()
}

function handleReset() {
  searchForm.equipmentId = null
  searchForm.planStatus = ''
  searchForm.dateRange = null
  handleSearch()
}

const formVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const formData = reactive({
  id: null,
  equipmentId: null,
  planDate: '',
  planType: 'PERIODIC',
  cycleMonths: 3,
  remark: ''
})

const formRules = {
  equipmentId: [{ required: true, message: '请选择设备', trigger: 'change' }],
  planDate: [{ required: true, message: '请选择计划日期', trigger: 'change' }],
  planType: [{ required: true, message: '请选择计划类型', trigger: 'change' }],
  cycleMonths: [{ required: true, message: '请选择维保周期', trigger: 'change' }]
}

function handleAdd() {
  isEdit.value = false
  Object.assign(formData, {
    id: null, equipmentId: null, planDate: '',
    planType: 'PERIODIC', cycleMonths: 3, remark: ''
  })
  formVisible.value = true
}

async function handleEdit(row) {
  isEdit.value = true
  Object.assign(formData, {
    id: row.id,
    equipmentId: row.equipmentId,
    planDate: row.planDate,
    planType: row.planType,
    cycleMonths: row.cycleMonths,
    remark: row.remark || ''
  })
  formVisible.value = true
}

async function handleSubmitForm() {
  await formRef.value.validate()
  if (isEdit.value) {
    await updatePlan(formData.id, { ...formData })
    ElMessage.success('修改成功')
  } else {
    await createPlan({ ...formData })
    ElMessage.success('新增成功')
  }
  formVisible.value = false
  loadData()
}

async function handleDelete(row) {
  await ElMessageBox.confirm('确定删除该维保计划吗？', '提示', { type: 'warning' })
  await deletePlan(row.id)
  ElMessage.success('删除成功')
  loadData()
}

const completeVisible = ref(false)
const currentPlanId = ref(null)
const completeForm = reactive({
  orderOption: 'select',
  orderId: null,
  orderType: 'PERIODIC',
  planDate: '',
  engineer: ''
})

async function handleComplete(row) {
  currentPlanId.value = row.id
  completeForm.orderOption = 'select'
  completeForm.orderId = null
  completeForm.orderType = 'PERIODIC'
  completeForm.planDate = row.planDate
  completeForm.engineer = ''
  const res = await getOrders({ pageSize: 9999, equipmentId: row.equipmentId })
  orders.value = res.data.list || res.data.records || res.data || []
  completeVisible.value = true
}

async function handleSubmitComplete() {
  let orderId = completeForm.orderId
  if (completeForm.orderOption === 'create') {
    const plan = tableData.value.find(p => p.id === currentPlanId.value)
    const orderNo = 'WO' + Date.now()
    const res = await createOrder({
      orderNo,
      equipmentId: plan.equipmentId,
      customerId: plan.customerId || equipments.value.find(e => e.id === plan.equipmentId)?.customerId,
      orderType: completeForm.orderType,
      planDate: completeForm.planDate,
      engineer: completeForm.engineer,
      orderStatus: 'COMPLETED'
    })
    orderId = res.data?.id || res.data
  }
  if (!orderId) {
    ElMessage.warning('请选择或创建工单')
    return
  }
  await completePlan(currentPlanId.value, orderId)
  ElMessage.success('计划已完成')
  completeVisible.value = false
  loadData()
}

onMounted(async () => {
  const eqRes = await getEquipments({ pageSize: 9999 })
  equipments.value = eqRes.data?.list || eqRes.data?.records || eqRes.data || []
  loadData()
})
</script>
