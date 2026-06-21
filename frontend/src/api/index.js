import request from '../utils/request'

export function getEquipments(params) { return request.get('/equipments', { params }) }
export function getEquipment(id) { return request.get(`/equipments/${id}`) }
export function createEquipment(data) { return request.post('/equipments', data) }
export function updateEquipment(id, data) { return request.put(`/equipments/${id}`, data) }
export function deleteEquipment(id) { return request.delete(`/equipments/${id}`) }
export function getEquipmentCategories() { return request.get('/equipments/categories') }
export function getExpiringEquipments() { return request.get('/equipments/expiring') }

export function getOrders(params) { return request.get('/maintenance-orders', { params }) }
export function getOrder(id) { return request.get(`/maintenance-orders/${id}`) }
export function createOrder(data) { return request.post('/maintenance-orders', data) }
export function updateOrder(id, data) { return request.put(`/maintenance-orders/${id}`, data) }
export function deleteOrder(id) { return request.delete(`/maintenance-orders/${id}`) }
export function updateOrderStatus(id, status) { return request.put(`/maintenance-orders/${id}/status`, null, { params: { status } }) }
export function completeOrder(id, data) { return request.put(`/maintenance-orders/${id}/complete`, data) }
export function checkBatchOrderStatus(data) { return request.post('/maintenance-orders/batch-status-check', data) }
export function batchUpdateOrderStatus(data) { return request.put('/maintenance-orders/batch-status', data) }
export function getOrdersByDateRange(startDate, endDate) { return request.get('/maintenance-orders/calendar', { params: { startDate, endDate } }) }
export function getOrderParts(orderId) { return request.get(`/maintenance-orders/${orderId}/parts`) }
export function addOrderPart(orderId, data) { return request.post(`/maintenance-orders/${orderId}/parts`, data) }
export function removeOrderPart(orderId, partId) { return request.delete(`/maintenance-orders/${orderId}/parts/${partId}`) }

export function getPlans(params) { return request.get('/maintenance-plans', { params }) }
export function getPlan(id) { return request.get(`/maintenance-plans/${id}`) }
export function createPlan(data) { return request.post('/maintenance-plans', data) }
export function updatePlan(id, data) { return request.put(`/maintenance-plans/${id}`, data) }
export function deletePlan(id) { return request.delete(`/maintenance-plans/${id}`) }
export function completePlan(id, orderId) { return request.put(`/maintenance-plans/${id}/complete`, null, { params: { orderId } }) }

export function getSpareParts(params) { return request.get('/spare-parts', { params }) }
export function getSparePart(id) { return request.get(`/spare-parts/${id}`) }
export function createSparePart(data) { return request.post('/spare-parts', data) }
export function updateSparePart(id, data) { return request.put(`/spare-parts/${id}`, data) }
export function adjustSparePartStock(id, quantity, isAdd) {
  return request.put(`/spare-parts/${id}/stock`, null, { params: { quantity, isAdd } })
}
export function deleteSparePart(id) { return request.delete(`/spare-parts/${id}`) }
export function getSparePartCategories() { return request.get('/spare-parts/categories') }
export function getLowStockParts() { return request.get('/spare-parts/low-stock') }

export function getCustomers(params) { return request.get('/customers', { params }) }
export function getCustomer(id) { return request.get(`/customers/${id}`) }
export function getAllCustomers() { return request.get('/customers/all') }
export function createCustomer(data) { return request.post('/customers', data) }
export function updateCustomer(id, data) { return request.put(`/customers/${id}`, data) }
export function deleteCustomer(id) { return request.delete(`/customers/${id}`) }
