import { defineStore } from 'pinia'
import doAjaxRequest from '@/util/util.js'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    user: null,
    isAuthenticated: false,
    error: null,
    token: null
  }),

  actions: {
    async login(credentials) {
      try {
        this.error = null
        console.log('[Auth] Attempting login with credentials:', credentials)
        const userData = await doAjaxRequest('/api/auth/login', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          credentials: 'include', // include cookies for cross-site requests
          body: JSON.stringify(credentials)
        })

        console.log('[Auth] Login response:', userData)
        this.user = userData
        this.token = userData.token
        this.isAuthenticated = true

        // Store user info and token in localStorage
        localStorage.setItem('user', JSON.stringify(userData))
        localStorage.setItem('token', userData.token)

        return userData
      } catch (error) {
        console.error('[Auth] Login error:', error)
        this.error = error.message || 'Échec de connexion'
        throw error
      }
    },

    async register(userData) {
      try {
        this.error = null
        console.log('[Auth] Attempting registration with data:', userData)
        const registeredUser = await doAjaxRequest('/api/auth/register', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          credentials: 'include', // include cookies for cross-site requests
          body: JSON.stringify(userData)
        })

        console.log('[Auth] Registration response:', registeredUser)
        this.user = registeredUser
        this.token = registeredUser.token
        this.isAuthenticated = true

        // Store user info and token in localStorage
        localStorage.setItem('user', JSON.stringify(registeredUser))
        localStorage.setItem('token', registeredUser.token)

        return registeredUser
      } catch (error) {
        console.error('[Auth] Registration error:', error)
        this.error = error.message || 'Échec d\'inscription'
        throw error
      }
    },

    async refreshToken() {
      try {
        if (!this.token) {
          throw new Error('Aucun token à rafraîchir')
        }
        console.log('[Auth] Refreshing token:', this.token)

        const response = await doAjaxRequest('/api/auth/refresh', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${this.token}`
          },
          credentials: 'include', // include cookies for cross-site requests
          body: JSON.stringify({ token: this.token })
        })

        console.log('[Auth] Token refresh response:', response)
        // Update the token
        this.token = response.token
        localStorage.setItem('token', response.token)

        return response.token
      } catch (error) {
        console.error('[Auth] Token refresh error:', error)
        // Logout the user on refresh failure
        this.logout()
        throw error
      }
    },

    logout() {
      console.log('[Auth] Logging out user:', this.user)
      this.user = null
      this.token = null
      this.isAuthenticated = false
      localStorage.removeItem('user')
      localStorage.removeItem('token')
    },

    initAuth() {
      const storedUser = localStorage.getItem('user')
      const storedToken = localStorage.getItem('token')

      if (storedUser && storedToken) {
        try {
          this.user = JSON.parse(storedUser)
          this.token = storedToken
          this.isAuthenticated = true
          console.log('[Auth] Initialized auth from localStorage.')
        } catch (error) {
          console.error('[Auth] Error parsing stored auth data:', error)
          localStorage.removeItem('user')
          localStorage.removeItem('token')
        }
      }
    },

    // Check if the token is expired
    isTokenExpired() {
      if (!this.token) return true

      try {
        const payload = JSON.parse(atob(this.token.split('.')[1]))
        const expiry = payload.exp * 1000 // convert to milliseconds
        const isExpired = Date.now() >= expiry
        console.log('[Auth] Token expiry check:', isExpired)
        return isExpired
      } catch (e) {
        console.error('[Auth] Error checking token expiry:', e)
        return true
      }
    },

    getAuthorizationHeader() {
      return this.token ? `Bearer ${this.token}` : ''
    }
  },

  getters: {
    getToken() {
      return this.token
    },

    getUserRole() {
      return this.user ? this.user.role : null
    }
  }
})
