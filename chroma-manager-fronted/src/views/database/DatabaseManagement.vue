<template>
  <div class="database-management">
    <!-- 操作栏 -->
    <div class="operation-bar">
      <el-button type="primary" @click="showCreateDialog = true">
        <i class="el-icon-plus"></i>
        创建数据库
      </el-button>
      <el-button @click="loadDatabases">
        <i class="el-icon-refresh"></i>
        刷新
      </el-button>
    </div>

    <!-- 数据库列表 -->
    <el-card class="database-list-card">
      <div slot="header" class="card-header">
        <span>数据库列表 (租户: {{ currentTenant }})</span>
        <span class="database-count">共 {{ databases.length }} 个数据库</span>
      </div>
      
      <el-table :data="databases" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="300">
          <template slot-scope="scope">
            <el-tag type="info">{{ scope.row.id }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="数据库名称" min-width="200">
          <template slot-scope="scope">
            <el-tag type="primary">{{ scope.row.name }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="tenant" label="所属租户" min-width="150">
          <template slot-scope="scope">
            <el-tag type="success">{{ scope.row.tenant }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250">
          <template slot-scope="scope">
            <el-button size="mini" @click="viewDatabase(scope.row)">查看</el-button>
            <el-button size="mini" type="primary" @click="viewCollections(scope.row)">集合</el-button>
            <el-button size="mini" type="danger" @click="deleteDatabase(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 创建数据库对话框 -->
    <el-dialog title="创建数据库" :visible.sync="showCreateDialog" width="500px">
      <el-form :model="createForm" :rules="createRules" ref="createForm" label-width="100px">
        <el-form-item label="租户">
          <el-input v-model="currentTenant" disabled></el-input>
        </el-form-item>
        <el-form-item label="数据库名称" prop="name">
          <el-input v-model="createForm.name" placeholder="请输入数据库名称"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="createDatabase" :loading="creating">确定</el-button>
      </div>
    </el-dialog>

    <!-- 数据库详情对话框 -->
    <el-dialog title="数据库详情" :visible.sync="showDetailDialog" width="600px">
      <div v-if="selectedDatabase" class="database-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="数据库ID">
            <el-tag type="info">{{ selectedDatabase.id }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="数据库名称">
            <el-tag type="primary">{{ selectedDatabase.name }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="所属租户">
            <el-tag type="success">{{ selectedDatabase.tenant }}</el-tag>
          </el-descriptions-item>
        </el-descriptions>
        
        <div class="database-actions" style="margin-top: 20px;">
          <el-button type="primary" @click="viewCollections(selectedDatabase)">
            <i class="el-icon-folder"></i>
            查看集合
          </el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { databaseAPI } from '@/services/api'
import { mapGetters } from 'vuex'

export default {
  name: 'DatabaseManagement',
  data() {
    return {
      databases: [],
      loading: false,
      creating: false,
      showCreateDialog: false,
      showDetailDialog: false,
      selectedDatabase: null,
      createForm: {
        name: ''
      },
      createRules: {
        name: [
          { required: true, message: '请输入数据库名称', trigger: 'blur' },
          { min: 1, max: 50, message: '长度在 1 到 50 个字符', trigger: 'blur' }
        ]
      }
    }
  },
  computed: {
    ...mapGetters(['currentUser']),
    currentTenant() {
      return this.currentUser?.tenant || ''
    }
  },
  mounted() {
    this.loadDatabases()
  },
  methods: {
    async loadDatabases() {
      if (!this.currentTenant) {
        this.$message.error('未获取到租户信息')
        return
      }
      
      this.loading = true
      try {
        const response = await databaseAPI.listDatabases(this.currentTenant)
        this.databases = response.data || []
      } catch (error) {
        this.$message.error('加载数据库列表失败')
        this.databases = []
      } finally {
        this.loading = false
      }
    },
    
    async createDatabase() {
      try {
        await this.$refs.createForm.validate()
        this.creating = true
        
        await databaseAPI.createDatabase(this.currentTenant, this.createForm)
        this.$message.success('数据库创建成功')
        this.showCreateDialog = false
        this.createForm.name = ''
        this.loadDatabases()
      } catch (error) {
        if (error !== false) { // 不是表单验证错误
          this.$message.error('创建数据库失败')
        }
      } finally {
        this.creating = false
      }
    },
    
    async deleteDatabase(database) {
      try {
        await this.$confirm(`确定要删除数据库 "${database.name}" 吗？此操作不可恢复！`, '警告', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        
        await databaseAPI.deleteDatabase(this.currentTenant, database.name)
        this.$message.success('数据库删除成功')
        this.loadDatabases()
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error('删除数据库失败')
        }
      }
    },
    
    viewDatabase(database) {
      this.selectedDatabase = database
      this.showDetailDialog = true
    },
    
    viewCollections(database) {
      this.$router.push({
        path: '/collection',
        query: { 
          tenant: this.currentTenant,
          database: database.name 
        }
      })
    }
  }
}
</script>

<style scoped>
.database-management {
  padding: 20px;
}

.operation-bar {
  margin-bottom: 20px;
  display: flex;
  gap: 10px;
}

.database-list-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.database-count {
  color: #909399;
  font-size: 14px;
}

.database-detail {
  padding: 10px 0;
}

.database-actions {
  text-align: center;
}

.dialog-footer {
  text-align: right;
}
</style>
