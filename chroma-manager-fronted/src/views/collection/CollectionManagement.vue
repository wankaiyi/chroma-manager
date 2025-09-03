<template>
  <div class="collection-management">
    <el-card class="selector-card" style="margin-bottom: 20px;">
      <div slot="header">
        <span>选择数据库</span>
      </div>
      <el-select v-model="selectedDatabase" placeholder="请选择数据库" @change="onDatabaseChange" style="width: 300px;">
        <el-option
          v-for="database in databases"
          :key="database.name"
          :label="database.name"
          :value="database.name">
        </el-option>
      </el-select>
    </el-card>

    <div class="operation-bar" v-if="selectedDatabase">
      <el-button type="primary" @click="showCreateDialog = true">
        <i class="el-icon-plus"></i>
        创建集合
      </el-button>
      <el-button @click="loadCollections">
        <i class="el-icon-refresh"></i>
        刷新
      </el-button>
    </div>

    <el-card class="collection-list-card" v-if="selectedDatabase">
      <div slot="header" class="card-header">
        <span>集合列表 ({{ currentTenant }} / {{ selectedDatabase }})</span>
        <span class="collection-count">共 {{ collections.length }} 个集合</span>
      </div>
      
      <el-table :data="collections" v-loading="loading" stripe>
        <el-table-column prop="id" label="集合ID" width="300">
          <template slot-scope="scope">
            <el-tag type="info">{{ scope.row.id }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="集合名称" min-width="200">
          <template slot-scope="scope">
            <el-tag type="primary">{{ scope.row.name }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="metadata" label="元数据" min-width="200">
          <template slot-scope="scope">
            <el-tag v-if="scope.row.metadata" type="success">{{ JSON.stringify(scope.row.metadata) }}</el-tag>
            <span v-else class="no-content">无</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template slot-scope="scope">
            <el-dropdown @command="(command) => handleAction(command, scope.row)" trigger="click">
              <el-button type="text" size="small">
                操作<i class="el-icon-arrow-down el-icon--right"></i>
              </el-button>
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item command="detail">
                  <i class="el-icon-view"></i>
                  查看详情
                </el-dropdown-item>
                <el-dropdown-item command="edit">
                  <i class="el-icon-edit"></i>
                  编辑集合
                </el-dropdown-item>
                <el-dropdown-item command="delete" divided>
                  <i class="el-icon-delete"></i>
                  删除集合
                </el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog title="创建集合" :visible.sync="showCreateDialog" width="500px">
      <el-form :model="createForm" :rules="createRules" ref="createForm" label-width="120px">
        <el-form-item label="集合名称" prop="name">
          <el-input v-model="createForm.name" placeholder="请输入集合名称"></el-input>
        </el-form-item>
        <el-form-item label="元数据">
          <el-input 
            v-model="createForm.metadata" 
            type="textarea" 
            :rows="3"
            placeholder="请输入JSON格式元数据（可选）">
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-checkbox v-model="createForm.get_or_create">如果存在则获取，不存在则创建</el-checkbox>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="createCollection" :loading="creating">确定</el-button>
      </div>
    </el-dialog>

    <el-dialog title="编辑集合" :visible.sync="showEditDialog" width="500px">
      <el-form :model="editForm" :rules="editRules" ref="editForm" label-width="120px">
        <el-form-item label="集合ID">
          <el-input v-model="editForm.id" disabled></el-input>
        </el-form-item>
        <el-form-item label="新名称" prop="new_name">
          <el-input v-model="editForm.new_name" placeholder="请输入新的集合名称"></el-input>
        </el-form-item>
        <el-form-item label="新元数据">
          <el-input 
            v-model="editForm.new_metadata" 
            type="textarea" 
            :rows="3"
            placeholder="请输入新的JSON格式元数据">
          </el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button type="primary" @click="updateCollection" :loading="updating">确定</el-button>
      </div>
    </el-dialog>

    <el-dialog title="集合详情" :visible.sync="showDetailDialog" width="600px">
      <div v-if="selectedCollection" class="collection-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="集合ID">
            <el-tag type="info">{{ selectedCollection.id }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="集合名称">
            <el-tag type="primary">{{ selectedCollection.name }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="租户">
            {{ currentTenant }}
          </el-descriptions-item>
          <el-descriptions-item label="数据库">
            {{ selectedDatabase }}
          </el-descriptions-item>
          <el-descriptions-item label="元数据" span="2">
            <pre v-if="selectedCollection.metadata">{{ JSON.stringify(selectedCollection.metadata, null, 2) }}</pre>
            <span v-else class="no-metadata">无</span>
          </el-descriptions-item>
        </el-descriptions>
        
        <div class="collection-actions" style="margin-top: 20px;">
          <el-button type="primary" @click="viewData(selectedCollection)">
            <i class="el-icon-document"></i>
            查看数据
          </el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { collectionAPI, databaseAPI } from '@/services/api'
import { mapGetters } from 'vuex'

export default {
  name: 'CollectionManagement',
  data() {
    return {
      databases: [],
      selectedDatabase: '',
      collections: [],
      loading: false,
      creating: false,
      updating: false,
      showCreateDialog: false,
      showEditDialog: false,
      showDetailDialog: false,
      selectedCollection: null,
      createForm: {
        name: '',
        get_or_create: false,
        metadata: ''
      },
      editForm: {
        id: '',
        new_name: '',
        new_metadata: ''
      },
      createRules: {
        name: [
          { required: true, message: '请输入集合名称', trigger: 'blur' },
          { min: 1, max: 50, message: '长度在 1 到 50 个字符', trigger: 'blur' }
        ]
      },
      editRules: {
        new_name: [
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
    this.restoreState()
  },
  beforeDestroy() {
    this.saveState()
  },
  methods: {
    handleAction(command, collection) {
      switch (command) {
        case 'detail':
          this.viewDetail(collection)
          break
        case 'edit':
          this.editCollection(collection)
          break
        case 'delete':
          this.deleteCollection(collection)
          break
      }
    },
    
    saveState() {
      const state = {
        selectedDatabase: this.selectedDatabase,
        collections: this.collections
      }
      localStorage.setItem('collection-management-state', JSON.stringify(state))
    },
    
    restoreState() {
      try {
        const savedState = localStorage.getItem('collection-management-state')
        if (savedState) {
          const state = JSON.parse(savedState)
          this.selectedDatabase = state.selectedDatabase || ''
          this.collections = state.collections || []
          
          if (this.selectedDatabase) {
            this.loadCollections()
          }
        }
      } catch (error) {
        console.error('恢复集合管理页面状态失败:', error)
      }
    },
    
    async loadDatabases() {
      if (!this.currentTenant) {
        this.$message.error('未获取到租户信息')
        return
      }
      
      try {
        const response = await databaseAPI.listDatabases(this.currentTenant)
        this.databases = response.data || []
      } catch (error) {
        this.$message.error('加载数据库列表失败')
      }
    },
    
    onDatabaseChange() {
      this.collections = []
      this.saveState()
      if (this.selectedDatabase) {
        this.loadCollections()
      }
    },
    
    async loadCollections() {
      if (!this.currentTenant || !this.selectedDatabase) return
      
      try {
        this.loading = true
        const response = await collectionAPI.listCollections(this.currentTenant, this.selectedDatabase)
        this.collections = response.data || []
        this.saveState()
      } catch (error) {
        this.$message.error('加载集合列表失败')
      } finally {
        this.loading = false
      }
    },
    
    async createCollection() {
      try {
        await this.$refs.createForm.validate()
        this.creating = true
        
        const data = {
          name: this.createForm.name,
          get_or_create: this.createForm.get_or_create
        }
        
        if (this.createForm.metadata) {
          try {
            data.metadata = JSON.parse(this.createForm.metadata)
          } catch (error) {
            this.$message.error('元数据格式错误，请输入有效的JSON')
            return
          }
        }
        
        await collectionAPI.createCollection(this.currentTenant, this.selectedDatabase, data)
        this.$message.success('集合创建成功')
        this.showCreateDialog = false
        this.createForm = { name: '', get_or_create: false, metadata: '' }
        this.loadCollections()
      } catch (error) {
        if (error !== false) {
          this.$message.error('集合创建失败')
        }
      } finally {
        this.creating = false
      }
    },
    
    editCollection(collection) {
      this.editForm = {
        id: collection.id,
        new_name: collection.name,
        new_metadata: collection.metadata ? JSON.stringify(collection.metadata, null, 2) : ''
      }
      this.showEditDialog = true
    },
    
    async updateCollection() {
      try {
        await this.$refs.editForm.validate()
        this.updating = true
        
        const data = {}
        
        if (this.editForm.new_name) {
          data.new_name = this.editForm.new_name
        }
        
        if (this.editForm.new_metadata) {
          try {
            data.new_metadata = JSON.parse(this.editForm.new_metadata)
          } catch (error) {
            this.$message.error('元数据格式错误，请输入有效的JSON')
            return
          }
        }
        
        await collectionAPI.updateCollection(this.currentTenant, this.selectedDatabase, this.editForm.id, data)
        this.$message.success('集合更新成功')
        this.showEditDialog = false
        this.loadCollections()
      } catch (error) {
        if (error !== false) {
          this.$message.error('集合更新失败')
        }
      } finally {
        this.updating = false
      }
    },
    
    async deleteCollection(collection) {
      try {
        await this.$confirm(`确定要删除集合 "${collection.name}" 吗？`, '确认删除', {
          type: 'warning'
        })
        
        await collectionAPI.deleteCollection(this.currentTenant, this.selectedDatabase, collection.id)
        this.$message.success('集合删除成功')
        this.loadCollections()
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error('集合删除失败')
        }
      }
    },
    
    viewDetail(collection) {
      this.selectedCollection = collection
      this.showDetailDialog = true
    },
    
    viewData(collection) {
      this.showDetailDialog = false
      this.$router.push({
        path: '/data',
        query: {
          database: this.selectedDatabase,
          collection: collection.id
        }
      })
    }
  }
}
</script>

<style scoped>
.collection-management {
  padding: 20px;
}

.selector-card {
  margin-bottom: 20px;
}

.operation-bar {
  margin-bottom: 20px;
  display: flex;
  gap: 10px;
}

.collection-list-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.collection-count {
  color: #909399;
  font-size: 14px;
}

.no-content {
  color: #c0c4cc;
  font-style: italic;
}

.collection-detail {
  padding: 10px 0;
}

.no-metadata {
  color: #c0c4cc;
  font-style: italic;
}

.collection-actions {
  text-align: center;
}

.dialog-footer {
  text-align: right;
}
</style>
