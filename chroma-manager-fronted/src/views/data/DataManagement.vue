<template>
  <div class="data-management">
    <el-card class="selector-card" style="margin-bottom: 20px;">
      <div slot="header">
        <span>选择集合</span>
      </div>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-select v-model="selectedDatabase" placeholder="请选择数据库" @change="onDatabaseChange" style="width: 100%;">
            <el-option
              v-for="database in databases"
              :key="database.name"
              :label="database.name"
              :value="database.name">
            </el-option>
          </el-select>
        </el-col>
        <el-col :span="12">
          <el-select v-model="selectedCollection" placeholder="请选择集合" @change="onCollectionChange" style="width: 100%;" :disabled="!selectedDatabase">
            <el-option
              v-for="collection in collections"
              :key="collection.id"
              :label="collection.name"
              :value="collection.id">
            </el-option>
          </el-select>
        </el-col>
      </el-row>
    </el-card>

    <div class="operation-bar" v-if="selectedCollection">
      <el-button type="primary" @click="showAddDialog = true">
        <i class="el-icon-plus"></i>
        添加记录
      </el-button>
      <el-button type="success" @click="showQueryDialog = true">
        <i class="el-icon-search"></i>
        查询记录
      </el-button>
      <el-button @click="loadRecords">
        <i class="el-icon-refresh"></i>
        刷新
      </el-button>
    </div>

    <el-card class="records-list-card" v-if="selectedCollection">
      <div slot="header" class="card-header">
        <span>记录列表 ({{ currentTenant }} / {{ selectedDatabase }} / {{ getCollectionName() }})</span>
        <span class="records-count">共 {{ totalRecords }} 条记录</span>
      </div>
      
      <el-table :data="records" v-loading="loading" stripe>
        <el-table-column prop="id" label="记录ID" width="200">
          <template slot-scope="scope">
            <div class="cell-with-copy">
              <el-tag type="info">{{ scope.row.id }}</el-tag>
              <copy-button :text="scope.row.id"></copy-button>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="document" label="文档内容" min-width="300">
          <template slot-scope="scope">
            <div class="cell-with-copy" v-if="scope.row.document">
              <span>{{ scope.row.document }}</span>
              <copy-button :text="scope.row.document"></copy-button>
            </div>
            <span v-else class="no-content">无</span>
          </template>
        </el-table-column>
        <el-table-column prop="metadata" label="元数据" min-width="200">
          <template slot-scope="scope">
            <div class="cell-with-copy" v-if="scope.row.metadata">
              <el-tag type="success">{{ JSON.stringify(scope.row.metadata) }}</el-tag>
              <copy-button :text="JSON.stringify(scope.row.metadata)"></copy-button>
            </div>
            <span v-else class="no-content">无</span>
          </template>
        </el-table-column>
        <el-table-column prop="embedding" label="嵌入向量" width="150">
          <template slot-scope="scope">
            <div v-if="scope.row.embedding" class="embedding-cell">
              <el-tooltip 
                effect="dark" 
                placement="top" 
                :content="formatEmbeddingForTooltip(scope.row.embedding)"
                :disabled="!scope.row.embedding">
                <div class="embedding-preview">
                  <i class="el-icon-data-line"></i>
                  <span class="embedding-info">
                    [{{ scope.row.embedding.length }}维]
                  </span>
                </div>
              </el-tooltip>
              <copy-button :text="scope.row.embedding"></copy-button>
            </div>
            <span v-else class="no-content">无</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template slot-scope="scope">
            <el-button size="mini" type="danger" @click="deleteRecord(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination-container">
        <el-pagination
          @size-change="onPageSizeChange"
          @current-change="onCurrentPageChange"
          :current-page="currentPage"
          :page-sizes="[10, 20, 50, 100]"
          :page-size="pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="totalRecords">
        </el-pagination>
      </div>
    </el-card>

    <el-dialog title="添加记录" :visible.sync="showAddDialog" width="600px">
      <el-form :model="addForm" :rules="addRules" ref="addForm" label-width="120px">
        <el-form-item label="记录ID" prop="ids">
          <el-input v-model="addForm.ids" placeholder="请输入记录ID，多个ID用逗号分隔"></el-input>
        </el-form-item>
        <el-form-item label="文档内容" prop="documents">
          <el-input v-model="addForm.documents" type="textarea" :rows="3" placeholder="请输入文档内容，多个文档用逗号分隔"></el-input>
        </el-form-item>
        <el-form-item label="嵌入向量" prop="embeddings">
          <el-input v-model="addForm.embeddings" type="textarea" :rows="4" placeholder="请输入嵌入向量，JSON数组格式，如[[0.1]]"></el-input>
        </el-form-item>
        <el-form-item label="元数据">
          <el-input v-model="addForm.metadatas" type="textarea" :rows="3" placeholder="请输入元数据，JSON数组格式（可选）"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" @click="addRecords" :loading="adding">确定</el-button>
      </div>
    </el-dialog>

    <el-dialog title="查询记录" :visible.sync="showQueryDialog" width="500px">
      <el-form :model="queryForm" ref="queryForm" label-width="120px">
        <el-form-item label="查询向量">
          <el-input v-model="queryForm.query_embeddings" type="textarea" :rows="4" placeholder="请输入查询向量，JSON数组格式，如[[0.1]]"></el-input>
        </el-form-item>
        <el-form-item label="返回数量">
          <el-input-number v-model="queryForm.n_results" :min="1" :max="100" placeholder="返回记录数量"></el-input-number>
        </el-form-item>
        <el-form-item label="包含内容">
          <el-checkbox-group v-model="queryForm.include">
            <el-checkbox label="documents">文档</el-checkbox>
            <el-checkbox label="metadatas">元数据</el-checkbox>
            <el-checkbox label="embeddings">向量</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="showQueryDialog = false">取消</el-button>
        <el-button type="primary" @click="queryRecords" :loading="querying">查询</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { dataAPI, collectionAPI, databaseAPI } from '@/services/api'
import { mapGetters } from 'vuex'
import CopyButton from '@/components/common/CopyButton.vue'

export default {
  components: {
    CopyButton
  },
  name: 'DataManagement',
  data() {
    return {
      databases: [],
      selectedDatabase: '',
      collections: [],
      selectedCollection: '',
      records: [],
      loading: false,
      adding: false,
      querying: false,
      showAddDialog: false,
      showQueryDialog: false,
      addForm: {
        ids: '',
        documents: '',
        embeddings: '',
        metadatas: ''
      },
      queryForm: {
        query_embeddings: '',
        n_results: 10,
        include: ['documents', 'metadatas']
      },
      addRules: {
        ids: [
          { required: true, message: '请输入记录ID', trigger: 'blur' }
        ],
        embeddings: [
          { required: true, message: '请输入嵌入向量', trigger: 'blur' }
        ]
      },
      currentPage: 1,
      pageSize: 10,
      totalRecords: 0
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
    saveState() {
      const state = {
        selectedDatabase: this.selectedDatabase,
        selectedCollection: this.selectedCollection,
        currentPage: this.currentPage,
        pageSize: this.pageSize,
        records: this.records,
        totalRecords: this.totalRecords
      }
      localStorage.setItem('data-management-state', JSON.stringify(state))
    },
    
    restoreState() {
      try {
        const savedState = localStorage.getItem('data-management-state')
        if (savedState) {
          const state = JSON.parse(savedState)
          this.selectedDatabase = state.selectedDatabase || ''
          this.selectedCollection = state.selectedCollection || ''
          this.currentPage = state.currentPage || 1
          this.pageSize = state.pageSize || 10
          this.records = state.records || []
          this.totalRecords = state.totalRecords || 0
          
          if (this.selectedDatabase) {
            this.loadCollections()
          }
          
          if (this.selectedCollection) {
            this.loadRecords()
          }
        }
      } catch (error) {
        console.error('恢复数据管理页面状态失败:', error)
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
      this.selectedCollection = ''
      this.records = []
      this.saveState()
      if (this.selectedDatabase) {
        this.loadCollections()
      }
    },
    
    async loadCollections() {
      if (!this.currentTenant || !this.selectedDatabase) return
      
      try {
        const response = await collectionAPI.listCollections(this.currentTenant, this.selectedDatabase)
        this.collections = response.data || []
      } catch (error) {
        this.$message.error('加载集合列表失败')
      }
    },
    
    onCollectionChange() {
      this.records = []
      this.currentPage = 1
      this.saveState()
      if (this.selectedCollection) {
        this.loadRecords()
        this.getRecordCount()
      }
    },
    
    async loadRecords() {
      if (!this.currentTenant || !this.selectedDatabase || !this.selectedCollection) return
      
      this.loading = true
      try {
        const offset = (this.currentPage - 1) * this.pageSize
        const requestData = {
          include: ['documents', 'metadatas', 'embeddings'],
          limit: this.pageSize,
          offset: offset
        }
        
        const response = await dataAPI.getRecords(this.currentTenant, this.selectedDatabase, this.selectedCollection, requestData)
        
        if (response.data && response.data.ids) {
          this.records = this.formatRecordsData(response.data)
        } else {
          this.records = []
        }
        
        this.saveState()
      } catch (error) {
        console.error('加载记录失败:', error)
        this.$message.error('加载记录列表失败')
        this.records = []
      } finally {
        this.loading = false
      }
    },
    
    formatRecordsData(data) {
      const records = []
      const { ids, documents, metadatas, embeddings } = data
      
      for (let i = 0; i < ids.length; i++) {
        records.push({
          id: ids[i],
          document: documents && documents[i] ? documents[i] : null,
          metadata: metadatas && metadatas[i] ? metadatas[i] : null,
          embedding: embeddings && embeddings[i] ? embeddings[i] : null
        })
      }
      
      return records
    },
    
    formatEmbeddingForTooltip(embedding) {
      if (!embedding || !Array.isArray(embedding)) {
        return '无嵌入向量'
      }
      
      const maxDisplay = 20
      if (embedding.length <= maxDisplay) {
        return `[${embedding.map(v => v.toFixed(4)).join(', ')}]`
      } else {
        const start = embedding.slice(0, 10).map(v => v.toFixed(4)).join(', ')
        const end = embedding.slice(-10).map(v => v.toFixed(4)).join(', ')
        return `[${start}, ..., ${end}] (共${embedding.length}维)`
      }
    },
    
    formatEmbeddingForCopy(embedding) {
      if (!embedding || !Array.isArray(embedding)) {
        return ''
      }
      return JSON.stringify(embedding)
    },
    
    async getRecordCount() {
      if (!this.currentTenant || !this.selectedDatabase || !this.selectedCollection) return
      
      try {
        const response = await dataAPI.countRecords(this.currentTenant, this.selectedDatabase, this.selectedCollection)
        this.totalRecords = response.data || 0
        this.saveState()
      } catch (error) {
        this.totalRecords = 0
      }
    },
    
    onPageSizeChange(size) {
      this.pageSize = size
      this.currentPage = 1
      this.loadRecords()
    },
    
    onCurrentPageChange(page) {
      this.currentPage = page
      this.loadRecords()
    },
    
    getCollectionName() {
      const collection = this.collections.find(c => c.id === this.selectedCollection)
      return collection ? collection.name : this.selectedCollection
    },
    
    async addRecords() {
      try {
        await this.$refs.addForm.validate()
        this.adding = true
        
        const data = {
          ids: this.addForm.ids.split(',').map(id => id.trim()),
          documents: this.addForm.documents.split(',').map(doc => doc.trim()),
          embeddings: JSON.parse(this.addForm.embeddings)
        }
        
        if (this.addForm.metadatas) {
          data.metadatas = JSON.parse(this.addForm.metadatas)
        }
        
        await dataAPI.addRecords(this.currentTenant, this.selectedDatabase, this.selectedCollection, data)
        this.$message.success('记录添加成功')
        this.showAddDialog = false
        this.addForm = { ids: '', documents: '', embeddings: '', metadatas: '' }
        this.loadRecords()
        this.getRecordCount()
      } catch (error) {
        if (error !== false) {
          this.$message.error('添加记录失败')
        }
      } finally {
        this.adding = false
      }
    },
    
    async queryRecords() {
      let response;
      try {
        this.querying = true
        
        const data = {
          query_embeddings: JSON.parse(this.queryForm.query_embeddings),
          n_results: this.queryForm.n_results,
          include: this.queryForm.include
        }
        
        response = await dataAPI.queryRecords(this.currentTenant, this.selectedDatabase, this.selectedCollection, data)
        
        this.$alert(JSON.stringify(response.data, null, 2), '查询结果', {
          confirmButtonText: '确定',
          type: 'info'
        })
      } catch (error) {
        console.log(response)
        this.$message.error('查询记录失败：' + error.response.data)
      } finally {
        this.querying = false
      }
    },
    
    async deleteRecord(record) {
      try {
        await this.$confirm(`确定要删除记录 "${record.id}" 吗？`, '确认删除', {
          type: 'warning'
        })
        
        await dataAPI.deleteRecords(this.currentTenant, this.selectedDatabase, this.selectedCollection, {
          ids: [record.id]
        })
        this.$message.success('记录删除成功')
        this.loadRecords()
        this.getRecordCount()
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error('删除记录失败')
        }
      }
    }
  }
}
</script>

<style scoped>
.data-management {
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

.records-list-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.records-count {
  color: #909399;
  font-size: 14px;
}

.no-content {
  color: #c0c4cc;
  font-style: italic;
}

.embedding-cell {
  display: flex;
  align-items: center;
  justify-content: center;
}

.embedding-preview {
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 4px 8px;
  background-color: #f0f9ff;
  border: 1px solid #b3d8ff;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.embedding-preview:hover {
  background-color: #e1f5fe;
  border-color: #409eff;
}

.embedding-preview i {
  color: #409eff;
  font-size: 14px;
}

.embedding-info {
  font-size: 12px;
  color: #606266;
  font-weight: 500;
}

.cell-with-copy {
  display: flex;
  align-items: center;
  gap: 8px;
}

.pagination-container {
  margin-top: 20px;
  text-align: center;
}

.dialog-footer {
  text-align: right;
}
</style>
