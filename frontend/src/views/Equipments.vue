<template>
  <div>
    <el-card shadow="never" style="margin-bottom: 16px">
      <el-form :inline="true" :model="queryForm" @submit.prevent="handleSearch">
        <el-form-item label="关键词">
          <el-input v-model="queryForm.keyword" placeholder="设备型号/序列号" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="客户">
          <el-select v-model="queryForm.customerId" placeholder="全部" clearable style="width: 160px">
            <el-option v-for="c in customers" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="queryForm.categoryId" placeholder="全部" clearable style="width: 160px">
            <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="全部" clearable style="width: 130px">
            <el-option label="正常" value="NORMAL" />
            <el-option label="故障" value="FAULT" />
            <el-option label="维保中" value="MAINTENANCE" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button type="success" @click="openDialog(null)">新增设备</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <el-table :data="tableData" v-loading="loading" border stripe style="width: 100%">
        <el-table-column prop="equipmentModel" label="设备型号" min-width="130" />
        <el-table-column prop="serialNumber" label="序列号" min-width="130" />
        <el-table-column label="设备分类" min-width="110">
          <template #default="{ row }">{{ resolveCategoryName(row.categoryId) }}</template>
        </el-table-column>
        <el-table-column label="所属客户" min-width="120">
          <template #default="{ row }">{{ resolveCustomerName(row.customerId) }}</template>
        </el-table-column>
        <el-table-column prop="installAddress" label="安装地址" min-width="160" show-overflow-tooltip />
        <el-table-column prop="lastMaintenanceDate" label="上次维保日期" min-width="120" />
        <el-table-column prop="nextMaintenanceDate" label="下次维保日期" min-width="120" />
        <el-table-column label="状态" min-width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="openDialog(row)">编辑</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        style="margin-top: 16px; justify-content: flex-end"
        v-model:current-page="queryForm.pageNum"
        v-model:page-size="queryForm.pageSize"
        :page-sizes="[10, 20, 50]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadData"
        @current-change="loadData"
      />
    </el-card>

    <el-dialog :title="dialogForm.id ? '编辑设备' : '新增设备'" v-model="dialogVisible" width="600px" destroy-on-close>
      <el-form :model="dialogForm" label-width="100px" style="padding-right: 20px">
        <el-form-item label="所属客户" required>
          <el-select v-model="dialogForm.customerId" placeholder="请选择客户" style="width: 100%">
            <el-option v-for="c in customers" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="设备分类" required>
          <el-select v-model="dialogForm.categoryId" placeholder="请选择分类" style="width: 100%">
            <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="设备型号" required>
          <el-input v-model="dialogForm.equipmentModel" placeholder="请输入设备型号" />
        </el-form-item>
        <el-form-item label="序列号">
          <el-input v-model="dialogForm.serialNumber" placeholder="请输入序列号" />
        </el-form-item>
        <el-form-item label="安装地址" required>
          <el-input v-model="dialogForm.installAddress" placeholder="请输入安装地址" />
        </el-form-item>
        <el-form-item label="安装日期">
          <el-date-picker v-model="dialogForm.installDate" type="date" value-format="YYYY-MM-DD" placeholder="请选择安装日期" style="width: 100%" />
        </el-form-item>
        <el-form-item label="上次维保日期">
          <el-date-picker v-model="dialogForm.lastMaintenanceDate" type="date" value-format="YYYY-MM-DD" placeholder="请选择上次维保日期" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="dialogForm.status" placeholder="请选择状态" style="width: 100%">
            <el-option label="正常" value="NORMAL" />
            <el-option label="故障" value="FAULT" />
            <el-option label="维保中" value="MAINTENANCE" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="dialogForm.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getEquipments, createEquipment, updateEquipment, deleteEquipment, getEquipmentCategories, getAllCustomers } from '../api'

const loading = ref(false)
const submitting = ref(false)
const tableData = ref([])
const total = ref(0)
const categories = ref([])
const customers = ref([])

const queryForm = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: '',
  customerId: null,
  categoryId: null,
  status: ''
})

const dialogVisible = ref(false)
const dialogForm = reactive({
  id: null,
  customerId: null,
  categoryId: null,
  equipmentModel: '',
  serialNumber: '',
  installAddress: '',
  installDate: '',
  lastMaintenanceDate: '',
  nextMaintenanceDate: '',
  status: 'NORMAL',
  remark: ''
})

const statusTagType = (status) => {
  const map = { NORMAL: 'success', FAULT: 'danger', MAINTENANCE: 'warning' }
  return map[status] || 'info'
}

const statusLabel = (status) => {
  const map = { NORMAL: '正常', FAULT: '故障', MAINTENANCE: '维保中' }
  return map[status] || status
}

const resolveCategoryName = (categoryId) => {
  const cat = categories.value.find(c => c.id === categoryId)
  return cat ? cat.name : categoryId
}

const resolveCustomerName = (customerId) => {
  const c = customers.value.find(c => c.id === customerId)
  return c ? c.name : customerId
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getEquipments(queryForm)
    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const loadCategories = async () => {
  const res = await getEquipmentCategories()
  categories.value = res.data
}

const loadCustomers = async () => {
  const res = await getAllCustomers()
  customers.value = res.data
}

const handleSearch = () => {
  queryForm.pageNum = 1
  loadData()
}

const handleReset = () => {
  queryForm.keyword = ''
  queryForm.customerId = null
  queryForm.categoryId = null
  queryForm.status = ''
  handleSearch()
}

const openDialog = (row) => {
  if (row) {
    Object.assign(dialogForm, {
      id: row.id,
      customerId: row.customerId,
      categoryId: row.categoryId,
      equipmentModel: row.equipmentModel,
      serialNumber: row.serialNumber,
      installAddress: row.installAddress,
      installDate: row.installDate,
      lastMaintenanceDate: row.lastMaintenanceDate,
      nextMaintenanceDate: row.nextMaintenanceDate,
      status: row.status,
      remark: row.remark
    })
  } else {
    Object.assign(dialogForm, {
      id: null,
      customerId: null,
      categoryId: null,
      equipmentModel: '',
      serialNumber: '',
      installAddress: '',
      installDate: '',
      lastMaintenanceDate: '',
      nextMaintenanceDate: '',
      status: 'NORMAL',
      remark: ''
    })
  }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  submitting.value = true
  try {
    const { id, ...data } = dialogForm
    if (id) {
      await updateEquipment(id, data)
      ElMessage.success('更新成功')
    } else {
      await createEquipment(data)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadData()
  } finally {
    submitting.value = false
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除设备「${row.equipmentModel}」吗？`, '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch {
    return
  }
  try {
    await deleteEquipment(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    // 错误已由request拦截器提示
  }
}

onMounted(() => {
  loadCategories()
  loadCustomers()
  loadData()
})
</script>
