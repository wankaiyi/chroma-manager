import Vue from 'vue'
import VueRouter from 'vue-router'
import LayoutView from '../components/common/LayoutView.vue'
import store from "@/store";
Vue.use(VueRouter)
const routes = [
  {
    path: '/login',
    name: 'login',
    component: () => import('../views/LoginView.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    component: LayoutView,
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        redirect: '/system'
      },
      {
        path: 'system',
        name: 'system',
        component: () => import('../views/system/SystemMonitor.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'database',
        name: 'database',
        component: () => import('../views/database/DatabaseManagement.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'collection',
        name: 'collection',
        component: () => import('../views/collection/CollectionManagement.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'data',
        name: 'data',
        component: () => import('../views/data/DataManagement.vue'),
        meta: { requiresAuth: true }
      }
    ]
  }
]
const router = new VueRouter({
  mode: 'history',
  base: process.env.BASE_URL,
  routes
})
// Â·ÓÉÊØÎÀ
router.beforeEach((to, from, next) => {
  const isLoggedIn = store.getters.isLoggedIn
  if (to.meta.requiresAuth && !isLoggedIn) {
    next('/login')
  } else {
    next()
  }
})
export default router

