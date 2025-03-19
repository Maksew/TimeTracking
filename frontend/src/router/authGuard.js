// src/router/authGuard.js
import { useAuthStore } from '@/stores/auth'
import { isTokenExpired } from '@/util/util'

/**
 * Garde de navigation pour protéger les routes qui nécessitent une authentification
 */
export function authGuard(to, from, next) {
  const authStore = useAuthStore()

  // Vérifier si la route nécessite une authentification
  if (to.matched.some(record => record.meta.requiresAuth)) {
    // Vérifier si l'utilisateur est authentifié
    if (!authStore.isAuthenticated) {
      // Rediriger vers la page de connexion
      return next({
        path: '/login',
        query: { redirect: to.fullPath } // Garder l'URL pour rediriger après connexion
      })
    }

    // Vérifier si le token est expiré
    const token = authStore.getToken
    if (token && isTokenExpired(token)) {
      // Essayer de rafraîchir le token
      authStore.refreshToken().catch(() => {
        // Si le rafraîchissement échoue, déconnecter et rediriger
        authStore.logout()
        return next({
          path: '/login',
          query: {
            redirect: to.fullPath,
            expired: 'true'
          }
        })
      })
    }

    // Si la route nécessite un rôle spécifique
    if (to.meta.requiredRole && authStore.getUserRole !== to.meta.requiredRole) {
      // Rediriger vers une page d'accès refusé
      return next({ path: '/access-denied' })
    }
  }

  // Autoriser la navigation
  next()
}
