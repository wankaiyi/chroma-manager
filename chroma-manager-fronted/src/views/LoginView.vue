<template>
  <div class="login-container">
    <!-- GitHub 图标 -->
    <a href="https://github.com/wankaiyi/chroma-manager" target="_blank" class="github-link">
      <svg height="32" width="32" viewBox="0 0 16 16" version="1.1" aria-hidden="true">
        <path fill-rule="evenodd"
          d="M8 0C3.58 0 0 3.58 0 8c0 3.54 2.29 6.53 5.47 7.59.4.07.55-.17.55-.38
             0-.19-.01-.82-.01-1.49-2.01.37-2.53-.49-2.69-.94-.09-.23-.48-.94-.82-1.13-.28-.15-.68-.52
             -.01-.53.63-.01 1.08.58 1.23.82.72 1.21 1.87.87 2.33.66.07-.52.28-.87.51-1.07-1.78-.2-3.64
             -.89-3.64-3.95 0-.87.31-1.59.82-2.15-.08-.2-.36-1.02.08-2.12 0 0 .67-.21 2.2.82a7.56
             7.56 0 012-.27c.68 0 1.36.09 2 .27 1.53-1.04 2.2-.82 2.2-.82.44 1.1.16 1.92.08
             2.12.51.56.82 1.27.82 2.15 0 3.07-1.87 3.75-3.65 3.95.29.25.54.73.54 1.48 0 1.07-.01
             1.93-.01 2.19 0 .21.15.46.55.38A8.013 8.013 0 0016 8c0-4.42-3.58-8-8-8z">
        </path>
      </svg>
    </a>
    
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
  background: linear-gradient(135deg, #f5f7fa 0%, #e8f4fd 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  position: relative;
}

.login-container::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><defs><pattern id="grain" width="100" height="100" patternUnits="userSpaceOnUse"><circle cx="25" cy="25" r="1" fill="%23e0e0e0" opacity="0.1"/><circle cx="75" cy="75" r="1" fill="%23e0e0e0" opacity="0.1"/><circle cx="50" cy="10" r="0.5" fill="%23e0e0e0" opacity="0.1"/><circle cx="10" cy="60" r="0.5" fill="%23e0e0e0" opacity="0.1"/><circle cx="90" cy="40" r="0.5" fill="%23e0e0e0" opacity="0.1"/></pattern></defs><rect width="100" height="100" fill="url(%23grain)"/></svg>');
  pointer-events: none;
}

.github-link {
  position: absolute;
  top: 30px;
  right: 30px;
  width: 50px;
  height: 50px;
  background: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
  text-decoration: none;
  color: #333;
  transition: all 0.3s ease;
  z-index: 10;
}

.github-link svg {
  fill: currentColor;
}

.github-link:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.15);
  color: #409eff;
}

.login-box {
  background: white;
  border-radius: 16px;
  padding: 40px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12), 0 2px 8px rgba(0, 0, 0, 0.08);
  width: 100%;
  max-width: 400px;
  border: 1px solid rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
  position: relative;
  z-index: 1;
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.login-header h1 {
  font-size: 2.5rem;
  margin: 0 0 10px 0;
  color: #2c3e50;
  font-weight: 700;
}

.login-header p {
  color: #7f8c8d;
  margin: 0;
  font-size: 1rem;
  font-weight: 500;
}

.login-form {
  margin-bottom: 20px;
}

.login-form .el-form-item {
  margin-bottom: 20px;
}

.login-form .el-input__inner {
  border-radius: 10px;
  border: 2px solid #e8eaed;
  transition: all 0.3s ease;
}

.login-form .el-input__inner:focus {
  border-color: #409eff;
  box-shadow: 0 0 0 3px rgba(64, 158, 255, 0.1);
}

.login-button {
  width: 100%;
  height: 50px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 12px;
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
  border: none;
  box-shadow: 0 4px 15px rgba(64, 158, 255, 0.3);
  transition: all 0.3s ease;
}

.login-button:hover {
  background: linear-gradient(135deg, #66b1ff 0%, #409eff 100%);
  box-shadow: 0 6px 20px rgba(64, 158, 255, 0.4);
  transform: translateY(-2px);
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
