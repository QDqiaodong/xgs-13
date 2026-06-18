import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/', redirect: '/dashboard' },
  { path: '/dashboard', name: 'Dashboard', component: () => import('../views/Dashboard.vue') },
  { path: '/equipments', name: 'Equipments', component: () => import('../views/Equipments.vue') },
  { path: '/orders', name: 'Orders', component: () => import('../views/Orders.vue') },
  { path: '/plans', name: 'Plans', component: () => import('../views/Plans.vue') },
  { path: '/spare-parts', name: 'SpareParts', component: () => import('../views/SpareParts.vue') },
  { path: '/customers', name: 'Customers', component: () => import('../views/Customers.vue') }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
