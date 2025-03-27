// src/stores/auth.js
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
        const userData = await doAjaxRequest('/api/auth/login', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify(credentials)
        })

        this.user = userData
        this.token = userData.token
        this.isAuthenticated = true

        // Stocker les informations dans localStorage
        localStorage.setItem('user', JSON.stringify(userData))
        localStorage.setItem('token', userData.token)

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
        this.token = registeredUser.token
        this.isAuthenticated = true

        // Stocker les informations dans localStorage
        localStorage.setItem('user', JSON.stringify(registeredUser))
        localStorage.setItem('token', registeredUser.token)

        return registeredUser
      } catch (error) {
        this.error = error.message || 'Échec d\'inscription'
        throw error
      }
    },

    async refreshToken() {
      try {
        if (!this.token) {
          throw new Error('Aucun token à rafraîchir')
        }

        const response = await doAjaxRequest('/api/auth/refresh', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${this.token}`
          },
          body: JSON.stringify({ token: this.token })
        })

        // Mettre à jour le token
        this.token = response.token
        localStorage.setItem('token', response.token)

        return response.token
      } catch (error) {
        // Si le rafraîchissement échoue, déconnecter l'utilisateur
        this.logout()
        throw error
      }
    },

    logout() {
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
        } catch (error) {
          localStorage.removeItem('user')
          localStorage.removeItem('token')
        }
      }
    },

    // Vérifier si le token est expiré
    isTokenExpired() {
      if (!this.token) return true

      // Décoder le token JWT (sans bibliothèque)
      try {
        const payload = JSON.parse(atob(this.token.split('.')[1]))
        const expiry = payload.exp * 1000 // Convertir en millisecondes
        return Date.now() >= expiry
      } catch (e) {
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
