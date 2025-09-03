<template>
  <el-container class="layout-container">
    <el-aside width="250px" class="sidebar">
      <div class="logo">
        <h2>Chroma Manager</h2>
      </div>
      <el-menu
        :default-active="$route.path"
        class="sidebar-menu"
        router
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
      >
        <el-menu-item index="/system">
          <i class="el-icon-monitor"></i>
          <span>系统监控</span>
        </el-menu-item>
        <el-menu-item index="/database">
          <i class="el-icon-s-grid"></i>
          <span>数据库管理</span>
        </el-menu-item>
        <el-menu-item index="/collection">
          <i class="el-icon-folder"></i>
          <span>集合管理</span>
        </el-menu-item>
        <el-menu-item index="/data">
          <i class="el-icon-document"></i>
          <span>数据操作</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="header-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item v-for="item in breadcrumbs" :key="item.path" :to="item.path">
              {{ item.name }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-dropdown">
              <i class="el-icon-user"></i>
              {{ currentUser?.tenant || '用户' }}
              <i class="el-icon-arrow-down el-icon--right"></i>
            </span>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item command="editTenant">
                <i class="el-icon-edit"></i>
                修改租户名称
              </el-dropdown-item>
              <el-dropdown-item command="logout" divided>
                <i class="el-icon-switch-button"></i>
                退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>

    <el-dialog title="修改租户名称" :visible.sync="showEditTenantDialog" width="500px">
      <el-form :model="tenantForm" :rules="tenantRules" ref="tenantForm" label-width="120px">
        <el-form-item label="当前租户">
          <el-input :value="currentUser?.tenant || ''" disabled></el-input>
        </el-form-item>
        <el-form-item label="新租户名称" prop="newTenantName">
          <el-input v-model="tenantForm.newTenantName" placeholder="请输入新的租户名称"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="showEditTenantDialog = false">取消</el-button>
        <el-button type="primary" @click="updateTenantName" :loading="updating">确定</el-button>
      </div>
    </el-dialog>

    <el-dialog title="系统设置" :visible.sync="showSettings" width="500px">
      <el-form :model="settings" label-width="120px">
        <el-form-item label="API地址">
          <el-input v-model="settings.apiUrl" placeholder="请输入Chroma API地址"></el-input>
        </el-form-item>
        <el-form-item label="认证Token">
          <el-input v-model="settings.token" type="password" placeholder="请输入认证Token"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="showSettings = false">取消</el-button>
        <el-button type="primary" @click="saveSettings">保存</el-button>
      </div>
    </el-dialog>
  </el-container>
</template>

<script>
import { mapGetters, mapActions } from 'vuex'
import { tenantAPI } from '@/services/api'

export default {
  name: 'LayoutView',
  data() {
    return {
      showSettings: false,
      showEditTenantDialog: false,
      updating: false,
      settings: {
        apiUrl: '',
        token: ''
      },
      tenantForm: {
        newTenantName: ''
      },
      tenantRules: {
        newTenantName: [
          { required: true, message: '请输入新的租户名称', trigger: 'blur' },
          { min: 1, max: 50, message: '长度在 1 到 50 个字符', trigger: 'blur' }
        ]
      }
    }
  },
  computed: {
    ...mapGetters(['currentUser']),
    breadcrumbs() {
      const route = this.$route
      const breadcrumbs = []
      
      if (route.path === '/system') {
        breadcrumbs.push({ name: '系统监控', path: '/system' })
      } else if (route.path === '/database') {
        breadcrumbs.push({ name: '数据库管理', path: '/database' })
      } else if (route.path === '/collection') {
        breadcrumbs.push({ name: '集合管理', path: '/collection' })
      } else if (route.path === '/data') {
        breadcrumbs.push({ name: '数据操作', path: '/data' })
      }
      
      return breadcrumbs
    }
  },
  methods: {
    ...mapActions(['logout']),
    handleCommand(command) {
      if (command === 'logout') {
        this.$confirm('确定要退出登录吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          this.logout()
          this.$router.push('/login')
        }).catch(() => {})
      } else if (command === 'editTenant') {
        this.showEditTenantDialog = true
        this.tenantForm.newTenantName = ''
      }
    },
    async updateTenantName() {
      try {
        await this.$refs.tenantForm.validate()
        this.updating = true
        
        await tenantAPI.updateTenant(this.currentUser.tenant, {
          name: this.tenantForm.newTenantName
        })
        
        const updatedUser = {
          ...this.currentUser,
          tenant: this.tenantForm.newTenantName
        }
        
        this.$store.commit('setUser', updatedUser)
        localStorage.setItem('user-info', JSON.stringify(updatedUser))
        
        this.$message.success('租户名称修改成功')
        this.showEditTenantDialog = false
        this.tenantForm.newTenantName = ''
        
      } catch (error) {
        if (error !== false) {
          console.error('修改租户名称失败:', error)
          this.$message.error('修改租户名称失败')
        }
      } finally {
        this.updating = false
      }
    },
    saveSettings() {
      localStorage.setItem('chroma-api-url', this.settings.apiUrl)
      localStorage.setItem('chroma-token', this.settings.token)
      
      this.$message.success('设置已保存')
      this.showSettings = false
      
      window.location.reload()
    }
  },
  mounted() {
    const savedApiUrl = localStorage.getItem('chroma-api-url')
    const savedToken = localStorage.getItem('chroma-token')
    
    if (savedApiUrl) {
      this.settings.apiUrl = savedApiUrl
    }
    if (savedToken) {
      this.settings.token = savedToken
    }
  }
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
}

.sidebar {
  background-color: #304156;
}

.logo {
  padding: 20px;
  text-align: center;
  color: white;
  border-bottom: 1px solid #434a50;
}

.logo h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}

.sidebar-menu {
  border: none;
  height: calc(100vh - 80px);
}

.header {
  background-color: #fff;
  border-bottom: 1px solid #e6e6e6;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}

.header-left {
  flex: 1;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 15px;
}

.user-dropdown {
  cursor: pointer;
  color: #606266;
  display: flex;
  align-items: center;
  gap: 5px;
}

.user-dropdown:hover {
  color: #409EFF;
}

.main-content {
  background-color: #f5f5f5;
  padding: 0;
}

.dialog-footer {
  text-align: right;
}
</style>
