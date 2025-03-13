// src/stores/auth.js
import { defineStore } from 'pinia'
import doAjaxRequest from '@/util/util.js'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    user: null,
    isAuthenticated: false,
    error: null
  }),

  actions: {
    async login(credentials) {
      try {
        this.error = null
        const userData = await doAjaxRequest('/api/auth/login', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify(credentials)
        })

        this.user = userData
        this.isAuthenticated = true

        localStorage.setItem('user', JSON.stringify(userData))

        return userData
      } catch (error) {
        this.error = error.message || 'Échec de connexion'
        throw error
      }
    },

    async register(userData) {
      try {
        this.error = null
        const registeredUser = await doAjaxRequest('/api/auth/register', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify(userData)
        })

        this.user = registeredUser
        this.isAuthenticated = true

        localStorage.setItem('user', JSON.stringify(registeredUser))

        return registeredUser
      } catch (error) {
        this.error = error.message || 'Échec d\'inscription'
        throw error
      }
    },

    logout() {
      this.user = null
      this.isAuthenticated = false
      localStorage.removeItem('user')
    },

    initAuth() {
      const storedUser = localStorage.getItem('user')
      if (storedUser) {
        try {
          this.user = JSON.parse(storedUser)
          this.isAuthenticated = true
        } catch (error) {
          localStorage.removeItem('user')
        }
      }
    }
  }
})
