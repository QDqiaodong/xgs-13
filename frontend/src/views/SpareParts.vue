<template>
  <div>
    <el-card shadow="never" style="margin-bottom: 16px">
      <el-form :inline="true" :model="searchForm" @submit.prevent="handleSearch">
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="型号/名称/供应商" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="searchForm.categoryId" placeholder="全部分类" clearable style="width: 160px">
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button type="success" @click="handleAdd">新增备件</el-button>
          <el-button type="warning" @click="handleLowStock">库存预警</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <el-table :data="tableData" border stripe style="width: 100%">
        <el-table-column prop="partModel" label="型号" min-width="120" />
        <el-table-column prop="partName" label="名称" min-width="120" />
        <el-table-column label="分类" min-width="100">
          <template #default="{ row }">{{ resolveCategoryName(row.categoryId) }}</template>
        </el-table-column>
        <el-table-column prop="spec" label="规格" min-width="100" />
        <el-table-column prop="unit" label="单位" width="70" align="center" />
        <el-table-column label="库存数量" width="110" align="center">
          <template #default="{ row }">
            <span
              v-if="row.stockQuantity <= row.safetyStock"
              style="color: #f56c6c; font-weight: bold"
            >
              <el-icon style="vertical-align: middle; margin-right: 2px"><Warning /></el-icon>
              {{ row.stockQuantity }}
            </span>
            <span v-else>{{ row.stockQuantity }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="safetyStock" label="安全库存" width="90" align="center" />
        <el-table-column prop="unitPrice" label="单价" width="90" align="right">
          <template #default="{ row }">{{ row.unitPrice != null ? `¥${row.unitPrice}` : '-' }}</template>
        </el-table-column>
        <el-table-column prop="supplier" label="供应商" min-width="120" show-overflow-tooltip>
          <template #default="{ row }">{{ row.supplier || '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="warning" size="small" @click="handleAdjustStock(row)">调库存</el-button>
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

    <el-dialog v-model="formVisible" :title="isEdit ? '编辑备件' : '新增备件'" width="600px" destroy-on-close>
      <el-form :model="formData" :rules="formRules" ref="formRef" label-width="90px">
        <el-form-item label="分类" prop="categoryId">
          <el-select v-model="formData.categoryId" placeholder="请选择分类" style="width: 100%">
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="型号" prop="partModel">
          <el-input v-model="formData.partModel" placeholder="请输入型号" />
        </el-form-item>
        <el-form-item label="名称" prop="partName">
          <el-input v-model="formData.partName" placeholder="请输入名称" />
        </el-form-item>
        <el-form-item label="规格" prop="spec">
          <el-input v-model="formData.spec" placeholder="请输入规格" />
        </el-form-item>
        <el-form-item label="单位" prop="unit">
          <el-input v-model="formData.unit" placeholder="请输入单位" />
        </el-form-item>
        <el-form-item label="库存数量" prop="stockQuantity">
          <el-input-number v-model="formData.stockQuantity" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="安全库存" prop="safetyStock">
          <el-input-number v-model="formData.safetyStock" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="单价" prop="unitPrice">
          <el-input-number v-model="formData.unitPrice" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="供应商" prop="supplier">
          <el-input v-model="formData.supplier" placeholder="请输入供应商" />
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

    <el-dialog v-model="stockVisible" title="库存调整" width="450px" destroy-on-close>
      <el-form :model="stockForm" label-width="90px">
        <el-form-item label="当前库存">
          <span style="font-weight: bold; font-size: 16px">{{ stockForm.currentStock }}</span>
        </el-form-item>
        <el-form-item label="调整数量" prop="quantity">
          <el-input-number v-model="stockForm.quantity" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="调整类型" prop="direction">
          <el-radio-group v-model="stockForm.direction">
            <el-radio value="in">入库</el-radio>
            <el-radio value="out">出库</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="stockVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitStock">确认调整</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="lowStockVisible" title="库存预警" width="750px" destroy-on-close>
      <el-table :data="lowStockData" border stripe>
        <el-table-column prop="partModel" label="型号" min-width="120" />
        <el-table-column prop="partName" label="名称" min-width="120" />
        <el-table-column label="分类" min-width="100">
          <template #default="{ row }">{{ resolveCategoryName(row.categoryId) }}</template>
        </el-table-column>
        <el-table-column prop="stockQuantity" label="当前库存" width="100" align="center">
          <template #default="{ row }">
            <span style="color: #f56c6c; font-weight: bold">{{ row.stockQuantity }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="safetyStock" label="安全库存" width="100" align="center" />
        <el-table-column prop="supplier" label="供应商" min-width="120">
          <template #default="{ row }">{{ row.supplier || '-' }}</template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Warning } from '@element-plus/icons-vue'
import {
  getSpareParts, createSparePart, updateSparePart, deleteSparePart,
  getSparePartCategories, getLowStockParts, adjustSparePartStock
} from '../api'

const categories = ref([])
const tableData = ref([])
const pagination = reactive({ pageNum: 1, pageSize: 10, total: 0 })

const searchForm = reactive({
  keyword: '',
  categoryId: null
})

const resolveCategoryName = (id) => {
  const c = categories.value.find(item => item.id === id)
  return c ? c.name : '-'
}

async function loadData() {
  const params = {
    pageNum: pagination.pageNum,
    pageSize: pagination.pageSize,
    keyword: searchForm.keyword || undefined,
    categoryId: searchForm.categoryId || undefined
  }
  const res = await getSpareParts(params)
  tableData.value = res.data.list || res.data.records || []
  pagination.total = res.data.total || 0
}

function handleSearch() {
  pagination.pageNum = 1
  loadData()
}

function handleReset() {
  searchForm.keyword = ''
  searchForm.categoryId = null
  handleSearch()
}

const formVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const formData = reactive({
  id: null,
  categoryId: null,
  partModel: '',
  partName: '',
  spec: '',
  unit: '',
  stockQuantity: 0,
  safetyStock: 0,
  unitPrice: null,
  supplier: '',
  remark: ''
})

const formRules = {
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  partModel: [{ required: true, message: '请输入型号', trigger: 'blur' }],
  partName: [{ required: true, message: '请输入名称', trigger: 'blur' }]
}

function handleAdd() {
  isEdit.value = false
  Object.assign(formData, {
    id: null, categoryId: null, partModel: '', partName: '',
    spec: '', unit: '', stockQuantity: 0, safetyStock: 0,
    unitPrice: null, supplier: '', remark: ''
  })
  formVisible.value = true
}

async function handleEdit(row) {
  isEdit.value = true
  Object.assign(formData, {
    id: row.id,
    categoryId: row.categoryId,
    partModel: row.partModel,
    partName: row.partName,
    spec: row.spec || '',
    unit: row.unit || '',
    stockQuantity: row.stockQuantity,
    safetyStock: row.safetyStock,
    unitPrice: row.unitPrice,
    supplier: row.supplier || '',
    remark: row.remark || ''
  })
  formVisible.value = true
}

async function handleSubmitForm() {
  await formRef.value.validate()
  if (isEdit.value) {
    await updateSparePart(formData.id, { ...formData })
    ElMessage.success('修改成功')
  } else {
    await createSparePart({ ...formData })
    ElMessage.success('新增成功')
  }
  formVisible.value = false
  loadData()
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确定删除备件「${row.partName}」吗？`, '提示', { type: 'warning' })
  await deleteSparePart(row.id)
  ElMessage.success('删除成功')
  loadData()
}

const stockVisible = ref(false)
const stockForm = reactive({
  partId: null,
  currentStock: 0,
  quantity: 1,
  direction: 'in'
})

function handleAdjustStock(row) {
  stockForm.partId = row.id
  stockForm.currentStock = row.stockQuantity
  stockForm.quantity = 1
  stockForm.direction = 'in'
  stockVisible.value = true
}

async function handleSubmitStock() {
  if (stockForm.quantity <= 0) {
    ElMessage.warning('请输入有效的调整数量')
    return
  }
  const isAdd = stockForm.direction === 'in'
  try {
    const res = await adjustSparePartStock(stockForm.partId, stockForm.quantity, isAdd)
    const updated = res.data
    const idx = tableData.value.findIndex(p => p.id === stockForm.partId)
    if (idx !== -1 && updated) {
      tableData.value.splice(idx, 1, updated)
    }
    ElMessage.success(isAdd ? '入库成功' : '出库成功')
    stockVisible.value = false
  } catch (e) {
    if (e.response && e.response.data && e.response.data.message) {
      ElMessage.warning(e.response.data.message)
    }
  }
}

const lowStockVisible = ref(false)
const lowStockData = ref([])

async function handleLowStock() {
  const res = await getLowStockParts()
  lowStockData.value = res.data || []
  lowStockVisible.value = true
}

onMounted(async () => {
  const catRes = await getSparePartCategories()
  categories.value = catRes.data || []
  loadData()
})
</script>
