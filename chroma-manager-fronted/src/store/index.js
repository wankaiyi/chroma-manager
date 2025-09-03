import Vue from 'vue'
import Vuex from 'vuex'

Vue.use(Vuex)

export default new Vuex.Store({
    state: {
        user: null,
        apiConfig: {
            baseURL: '',
            token: ''
        }
    },
    mutations: {
        setUser(state, user) {
            state.user = user
        },
        setApiConfig(state, config) {
            state.apiConfig = {...state.apiConfig, ...config}
        },
        clearUser(state) {
            state.user = null
            state.apiConfig = {
                baseURL: '',
                token: ''
            }
        }
    },
    actions: {
        async login({commit}, loginData) {
            const {authAPI} = await import('@/services/api')
            const response = await authAPI.login({
                tenant: loginData.tenant,
                url: loginData.apiUrl
            })

            const token = response.data.token

            const userInfo = {
                tenant: loginData.tenant,
                apiUrl: loginData.apiUrl,
                token: token,
                loginTime: new Date().toISOString()
            }

            commit('setUser', userInfo)
            commit('setApiConfig', {
                baseURL: loginData.apiUrl,
                token: token
            })

            localStorage.setItem('user-info', JSON.stringify(userInfo))
            localStorage.setItem('chroma-api-url', loginData.apiUrl)
            localStorage.setItem('token', token)

            return response.data
        },
        logout({commit}) {
            commit('clearUser')
            localStorage.removeItem('user-info')
            localStorage.removeItem('chroma-api-url')
            localStorage.removeItem('token')
        },
        restoreUser({commit}) {
            const userInfo = localStorage.getItem('user-info')
            if (userInfo) {
                try {
                    const user = JSON.parse(userInfo)
                    commit('setUser', user)
                    commit('setApiConfig', {
                        baseURL: user.apiUrl,
                        token: user.token
                    })
                } catch (error) {
                    localStorage.removeItem('user-info')
                    localStorage.removeItem('chroma-api-url')
                    localStorage.removeItem('token')
                }
            }
        }
    },
    getters: {
        isLoggedIn: state => !!state.user,
        currentUser: state => state.user,
        apiConfig: state => state.apiConfig
    }
})
