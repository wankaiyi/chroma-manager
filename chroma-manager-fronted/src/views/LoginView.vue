<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <h1>Chroma Manager</h1>
        <p>Chroma向量数据库管理界面</p>
      </div>
      
      <el-form :model="loginForm" :rules="loginRules" ref="loginForm" class="login-form">
        <el-form-item prop="tenant">
          <el-input
            v-model="loginForm.tenant"
            placeholder="请输入租户名称"
            prefix-icon="el-icon-office-building"
            size="large"
            autocomplete="username"
            name="tenant">
          </el-input>
        </el-form-item>
        
        <el-form-item prop="apiUrl">
          <el-input
            v-model="loginForm.apiUrl"
            placeholder="请输入ChromaDB API地址"
            prefix-icon="el-icon-link"
            size="large"
            autocomplete="url"
            name="apiUrl">
          </el-input>
        </el-form-item>
        
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            @click="handleLogin"
            :loading="logging"
            class="login-button">
            登录
          </el-button>
        </el-form-item>
      </el-form>
      
      <div class="login-footer">
        <p>默认API地址: http://localhost:8000</p>
      </div>
    </div>
  </div>
</template>

<script>
import { mapActions } from 'vuex'

export default {
  name: 'LoginView',
  data() {
    return {
      logging: false,
      loginForm: {
        tenant: '',
        apiUrl: 'http://localhost:8000'
      },
      loginRules: {
        tenant: [
          { required: true, message: '请输入租户名称', trigger: 'blur' },
          { min: 1, max: 50, message: '长度在 1 到 50 个字符', trigger: 'blur' }
        ],
        apiUrl: [
          { required: true, message: '请输入API地址', trigger: 'blur' },
          {
            pattern: /^https?:\/\/.+/, 
            message: '请输入有效的URL地址',
            trigger: 'blur'
          }
        ]
      }
    }
  },
  mounted() {
    this.restoreLoginInfo()
  },
  methods: {
    ...mapActions(['login']),
    
    restoreLoginInfo() {
      const savedTenant = localStorage.getItem('last-login-tenant')
      const savedApiUrl = localStorage.getItem('last-login-apiUrl')
      
      if (savedTenant) {
        this.loginForm.tenant = savedTenant
      }
      if (savedApiUrl) {
        this.loginForm.apiUrl = savedApiUrl
      }
    },
    
    saveLoginInfo() {
      localStorage.setItem('last-login-tenant', this.loginForm.tenant)
      localStorage.setItem('last-login-apiUrl', this.loginForm.apiUrl)
    },
    
    async handleLogin() {
      try {
        await this.$refs.loginForm.validate()
        this.logging = true

        this.saveLoginInfo()

        await this.login({
          tenant: this.loginForm.tenant,
          apiUrl: this.loginForm.apiUrl
        })

        this.$message.success('登录成功')
        this.$router.push('/system')

      } catch (error) {
        if (error !== false) {
          console.error('Login error:', error)
          this.$message.error('登录失败，请检查租户名称和API地址')
        }
      } finally {
        this.logging = false
      }
    }
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.login-box {
  background: white;
  border-radius: 20px;
  padding: 40px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  width: 100%;
  max-width: 400px;
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.login-header h1 {
  font-size: 2.5rem;
  margin: 0 0 10px 0;
  color: #333;
  font-weight: 700;
}

.login-header p {
  color: #666;
  margin: 0;
  font-size: 1rem;
}

.login-form {
  margin-bottom: 20px;
}

.login-form .el-form-item {
  margin-bottom: 20px;
}

.login-button {
  width: 100%;
  height: 50px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 25px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
}

.login-button:hover {
  background: linear-gradient(135deg, #5a6fd8 0%, #6a4190 100%);
}

.login-footer {
  text-align: center;
  color: #999;
  font-size: 14px;
}

.login-footer p {
  margin: 0;
}
</style>
