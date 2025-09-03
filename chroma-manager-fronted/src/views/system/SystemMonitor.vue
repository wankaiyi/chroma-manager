<template>
  <div class="system-monitor">
    <el-row :gutter="20">
      <el-col :span="6" v-for="(item, index) in systemStatus" :key="index">
        <el-card class="status-card" :class="item.status">
          <div class="status-content">
            <div class="status-icon">
              <i :class="item.icon"></i>
            </div>
            <div class="status-info">
              <h3>{{ item.title }}</h3>
              <p>{{ item.value }}</p>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="info-card" style="margin-top: 20px;">
      <div slot="header" class="card-header">
        <span>系统信息</span>
      </div>
      
      <el-row :gutter="20">
        <el-col :span="12">
          <div class="info-section">
            <h4>版本信息</h4>
            <p><strong>服务器版本:</strong> {{ systemInfo.version || '未知' }}</p>
          </div>
        </el-col>
        <el-col :span="12">
          <div class="info-section">
            <h4>预检查信息</h4>
            <p><strong>最大批次大小:</strong> {{ systemInfo.maxBatchSize || '未知' }}</p>
            <p><strong>支持Base64编码:</strong> {{ systemInfo.supportsBase64 ? '是' : '否' }}</p>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <el-card class="info-card" style="margin-top: 20px;">
      <div slot="header" class="card-header">
        <span>系统操作</span>
      </div>
      
      <div class="operation-buttons">
        <el-button type="primary" @click="showCreateTenantDialog = true">
          <i class="el-icon-plus"></i>
          创建租户
        </el-button>
        <el-button type="danger" @click="resetDatabase" :loading="resetting">
          <i class="el-icon-delete"></i>
          重置数据库
        </el-button>
      </div>
    </el-card>

    <el-dialog title="创建租户" :visible.sync="showCreateTenantDialog" width="500px">
      <el-form :model="tenantForm" :rules="tenantRules" ref="tenantForm" label-width="120px">
        <el-form-item label="租户名称" prop="name">
          <el-input v-model="tenantForm.name" placeholder="请输入租户名称"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="showCreateTenantDialog = false">取消</el-button>
        <el-button type="primary" @click="createTenant" :loading="creating">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { systemAPI, tenantAPI } from '@/services/api'

export default {
  name: 'SystemMonitor',
  data() {
    return {
      systemInfo: {
        version: '',
        maxBatchSize: '',
        supportsBase64: false,
        heartbeat: '',
        healthy: false
      },
      heartbeatTime: '',
      resetting: false,
      creating: false,
      showCreateTenantDialog: false,
      tenantForm: {
        name: ''
      },
      tenantRules: {
        name: [
          { required: true, message: '请输入租户名称', trigger: 'blur' },
          { min: 1, max: 50, message: '长度在 1 到 50 个字符', trigger: 'blur' }
        ]
      },
      heartbeatTimer: null,
      healthCheckTimer: null
    }
  },
  computed: {
    systemStatus() {
      return [
        {
          title: '健康状态',
          value: this.systemInfo.healthy ? '正常' : '异常',
          status: this.systemInfo.healthy ? 'success' : 'danger',
          icon: 'el-icon-success'
        },
        {
          title: '服务器版本',
          value: this.systemInfo.version || '未知',
          status: 'info',
          icon: 'el-icon-info'
        },
        {
          title: '最大批次大小',
          value: this.systemInfo.maxBatchSize || '未知',
          status: 'warning',
          icon: 'el-icon-warning'
        },
        {
          title: '心跳状态',
          value: this.heartbeatTime ? '正常' : '未知',
          status: this.heartbeatTime ? 'success' : 'info',
          icon: 'el-icon-time'
        }
      ]
    }
  },
  mounted() {
    this.loadSystemInfo()
    this.startAutoUpdate()
  },
  beforeDestroy() {
    this.stopAutoUpdate()
  },
  methods: {
    async loadSystemInfo() {
      try {
        await Promise.all([
          this.getVersion(),
          this.getPreFlightChecks(),
          this.getHeartbeat(),
          this.runHealthCheck()
        ])
      } catch (error) {
        this.$message.error('加载系统信息失败')
      }
    },
    
    async getVersion() {
      try {
        const response = await systemAPI.getVersion()
        this.systemInfo.version = response.data
      } catch (error) {
        console.error('获取版本信息失败:', error)
      }
    },
    
    async getPreFlightChecks() {
      try {
        const response = await systemAPI.preFlightChecks()
        const data = response.data
        this.systemInfo.maxBatchSize = data.max_batch_size
        this.systemInfo.supportsBase64 = data.supports_base64_encoding
      } catch (error) {
        console.error('获取预检查信息失败:', error)
      }
    },
    
    async getHeartbeat() {
      try {
        const response = await systemAPI.heartbeat()
        this.systemInfo.heartbeat = response.data['nanosecond heartbeat']
        this.heartbeatTime = new Date().toLocaleString()
      } catch (error) {
        console.error('获取心跳信息失败:', error)
        this.heartbeatTime = ''
      }
    },
    
    async runHealthCheck() {
      try {
          await systemAPI.healthcheck()
        this.systemInfo.healthy = true
      } catch (error) {
        this.systemInfo.healthy = false
      }
    },
    
    async resetDatabase() {
      try {
        await this.$confirm('此操作将重置整个数据库，是否继续？', '警告', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        
        this.resetting = true
        await systemAPI.reset()
        this.$message.success('数据库重置成功')
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error('数据库重置失败')
        }
      } finally {
        this.resetting = false
      }
    },
    
    async createTenant() {
      try {
        await this.$refs.tenantForm.validate()
        this.creating = true
        
        await tenantAPI.createTenant({"name": this.tenantForm.name})
        this.$message.success('租户创建成功')
        this.showCreateTenantDialog = false
        this.tenantForm.name = ''
      } catch (error) {
        if (error !== false) {
          this.$message.error('租户创建失败')
        }
      } finally {
        this.creating = false
      }
    },
    
    startAutoUpdate() {
      this.heartbeatTimer = setInterval(() => {
        this.getHeartbeat()
      }, 5000)
      
      this.healthCheckTimer = setInterval(() => {
        this.runHealthCheck()
      }, 5000)
    },
    
    stopAutoUpdate() {
      if (this.heartbeatTimer) {
        clearInterval(this.heartbeatTimer)
        this.heartbeatTimer = null
      }
      if (this.healthCheckTimer) {
        clearInterval(this.healthCheckTimer)
        this.healthCheckTimer = null
      }
    }
  }
}
</script>

<style scoped>
.system-monitor {
  padding: 20px;
}

.status-card {
  margin-bottom: 20px;
  transition: all 0.3s;
}

.status-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.status-card.success {
  border-left: 4px solid #67c23a;
}

.status-card.danger {
  border-left: 4px solid #f56c6c;
}

.status-card.warning {
  border-left: 4px solid #e6a23c;
}

.status-card.info {
  border-left: 4px solid #409eff;
}

.status-content {
  display: flex;
  align-items: center;
}

.status-icon {
  font-size: 24px;
  margin-right: 15px;
}

.status-icon .el-icon-success {
  color: #67c23a;
}

.status-icon .el-icon-danger {
  color: #f56c6c;
}

.status-icon .el-icon-warning {
  color: #e6a23c;
}

.status-icon .el-icon-info {
  color: #409eff;
}

.status-info h3 {
  margin: 0 0 5px 0;
  font-size: 14px;
  color: #606266;
}

.status-info p {
  margin: 0;
  font-size: 18px;
  font-weight: bold;
  color: #303133;
}

.info-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.info-section {
  margin-bottom: 20px;
}

.info-section h4 {
  margin: 0 0 10px 0;
  color: #303133;
  font-size: 16px;
}

.info-section p {
  margin: 5px 0;
  color: #606266;
}

.operation-buttons {
  display: flex;
  gap: 10px;
}

.dialog-footer {
  text-align: right;
}
</style>
