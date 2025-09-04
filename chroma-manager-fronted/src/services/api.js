import axios from 'axios'
import store from '@/store'
import router from '@/router'

const api = axios.create({
  baseURL: process.env.VUE_APP_CHROMA_API_URL || 'http://localhost:8000',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

api.interceptors.request.use(
  config => {
    const apiConfig = store.getters.apiConfig
    const token = apiConfig.token || localStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = token
    }

    return config
  },
  error => Promise.reject(error)
)

api.interceptors.response.use(
  response => response,
  error => {
    console.error('API Error:', error.response?.data || error.message)

    // 处理 401 未授权错误
    if (error.response?.status === 401) {
      // 清除用户信息
      store.dispatch('logout')
      // 跳转到登录页面
      router.push('/login')
      return Promise.reject(error)
    }

    return Promise.reject(error)
  }
)

export const authAPI = {
  login: (data) => api.post('/login', data),

  getUserIdentity: () => api.get('/api/v2/auth/identity'),

  setToken: (token) => {
    localStorage.setItem('token', token)
  },

  clearToken: () => {
    localStorage.removeItem('token')
  }
}

export const systemAPI = {
  healthcheck: () => api.get('/api/v2/healthcheck'),
  heartbeat: () => api.get('/api/v2/heartbeat'),
  preFlightChecks: () => api.get('/api/v2/pre-flight-checks'),
  getVersion: () => api.get('/api/v2/version'),
  reset: () => api.post('/api/v2/reset')
}

export const tenantAPI = {
  createTenant: (data) => api.post('/api/v2/tenants', data),
  getTenant: (tenantName) => api.get(`/api/v2/tenants/${tenantName}`),
  updateTenant: (tenantName, data) => api.patch(`/api/v2/tenants/${tenantName}`, data)
}

export const databaseAPI = {
  listDatabases: (tenant, params = {}) => api.get(`/api/v2/tenants/${tenant}/databases`, { params }),
  createDatabase: (tenant, data) => api.post(`/api/v2/tenants/${tenant}/databases`, data),
  getDatabase: (tenant, database) => api.get(`/api/v2/tenants/${tenant}/databases/${database}`),
  deleteDatabase: (tenant, database) => api.delete(`/api/v2/tenants/${tenant}/databases/${database}`)
}

export const collectionAPI = {
  listCollections: (tenant, database, params = {}) => 
    api.get(`/api/v2/tenants/${tenant}/databases/${database}/collections`, { params }),
  createCollection: (tenant, database, data) => 
    api.post(`/api/v2/tenants/${tenant}/databases/${database}/collections`, data),
  getCollection: (tenant, database, collectionId) => 
    api.get(`/api/v2/tenants/${tenant}/databases/${database}/collections/${collectionId}`),
  updateCollection: (tenant, database, collectionId, data) => 
    api.put(`/api/v2/tenants/${tenant}/databases/${database}/collections/${collectionId}`, data),
  deleteCollection: (tenant, database, collectionId) => 
    api.delete(`/api/v2/tenants/${tenant}/databases/${database}/collections/${collectionId}`),
  countCollections: (tenant, database) => 
    api.get(`/api/v2/tenants/${tenant}/databases/${database}/collections_count`)
}

export const dataAPI = {
  addRecords: (tenant, database, collectionId, data) => 
    api.post(`/api/v2/tenants/${tenant}/databases/${database}/collections/${collectionId}/add`, data),
  getRecords: (tenant, database, collectionId, data) => 
    api.post(`/api/v2/tenants/${tenant}/databases/${database}/collections/${collectionId}/get`, data),
  queryRecords: (tenant, database, collectionId, data, params = {}) => 
    api.post(`/api/v2/tenants/${tenant}/databases/${database}/collections/${collectionId}/query`, data, { params }),
  updateRecords: (tenant, database, collectionId, data) => 
    api.post(`/api/v2/tenants/${tenant}/databases/${database}/collections/${collectionId}/update`, data),
  deleteRecords: (tenant, database, collectionId, data) => 
    api.post(`/api/v2/tenants/${tenant}/databases/${database}/collections/${collectionId}/delete`, data),
  upsertRecords: (tenant, database, collectionId, data) => 
    api.post(`/api/v2/tenants/${tenant}/databases/${database}/collections/${collectionId}/upsert`, data),
  countRecords: (tenant, database, collectionId) => 
    api.get(`/api/v2/tenants/${tenant}/databases/${database}/collections/${collectionId}/count`),
  forkCollection: (tenant, database, collectionId, data) => 
    api.post(`/api/v2/tenants/${tenant}/databases/${database}/collections/${collectionId}/fork`, data)
}

export default api
