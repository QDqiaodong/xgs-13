<template>
  <div class="customers">
    <el-card shadow="hover">
      <div class="toolbar">
        <el-input
          v-model="keyword"
          placeholder="搜索客户名称"
          clearable
          style="width: 260px"
          @clear="loadData"
          @keyup.enter="loadData"
        >
          <template #append>
            <el-button @click="loadData">搜索</el-button>
          </template>
        </el-input>
        <el-button type="primary" @click="handleAdd">新增客户</el-button>
      </div>

      <el-table :data="tableData" stripe style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="客户名称" />
        <el-table-column prop="contactPerson" label="联系人" />
        <el-table-column prop="contactPhone" label="联系电话" />
        <el-table-column prop="address" label="地址" show-overflow-tooltip />
        <el-table-column prop="remark" label="备注" show-overflow-tooltip />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑客户' : '新增客户'"
      width="520px"
      destroy-on-close
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="90px">
        <el-form-item label="客户名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入客户名称" />
        </el-form-item>
        <el-form-item label="联系人" prop="contactPerson">
          <el-input v-model="form.contactPerson" placeholder="请输入联系人" />
        </el-form-item>
        <el-form-item label="联系电话" prop="contactPhone">
          <el-input v-model="form.contactPhone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="form.address" placeholder="请输入地址" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCustomers, createCustomer, updateCustomer, deleteCustomer } from '../api'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const keyword = ref('')

const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const form = reactive({
  id: null,
  name: '',
  contactPerson: '',
  contactPhone: '',
  address: '',
  remark: ''
})

const rules = {
  name: [{ required: true, message: '请输入客户名称', trigger: 'blur' }]
}

const resetForm = () => {
  form.id = null
  form.name = ''
  form.contactPerson = ''
  form.contactPhone = ''
  form.address = ''
  form.remark = ''
}

const loadData = async () => {
  loading.value = true
  try {
    const params = { pageNum: pageNum.value, pageSize: pageSize.value }
    if (keyword.value) params.keyword = keyword.value
    const res = await getCustomers(params)
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  resetForm()
  isEdit.value = false
  dialogVisible.value = true
}

const handleEdit = (row) => {
  resetForm()
  isEdit.value = true
  Object.assign(form, {
    id: row.id,
    name: row.name,
    contactPerson: row.contactPerson,
    contactPhone: row.contactPhone,
    address: row.address,
    remark: row.remark
  })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value.validate()
  const payload = {
    name: form.name,
    contactPerson: form.contactPerson,
    contactPhone: form.contactPhone,
    address: form.address,
    remark: form.remark
  }
  if (isEdit.value) {
    await updateCustomer(form.id, payload)
    ElMessage.success('修改成功')
  } else {
    await createCustomer(payload)
    ElMessage.success('新增成功')
  }
  dialogVisible.value = false
  loadData()
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除客户「${row.name}」吗？`, '提示', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }
  try {
    await deleteCustomer(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    // 错误已由request拦截器提示
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
