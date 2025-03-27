// src/util/httpInterceptor.js
import { useAuthStore } from '@/stores/auth'
import { useRouter } from 'vue-router'

/**
 * Intercepteur HTTP pour ajouter le token JWT aux requêtes
 * et gérer les réponses 401 (non autorisé)
 */
export async function httpInterceptor(url, options = {}) {
  const authStore = useAuthStore()
  const router = useRouter()

  // Si l'utilisateur est authentifié, ajouter le token aux en-têtes
  if (authStore.isAuthenticated) {
    options.headers = {
      ...options.headers,
      'Authorization': authStore.getAuthorizationHeader()
    }
  }

  try {
    const response = await fetch(url, options)

    // Si le token est expiré (401), tenter de le rafraîchir
    if (response.status === 401 && authStore.isAuthenticated) {
      try {
        // Tenter de rafraîchir le token
        await authStore.refreshToken()

        // Réessayer la requête avec le nouveau token
        const retryOptions = {
          ...options,
          headers: {
            ...options.headers,
            'Authorization': authStore.getAuthorizationHeader()
          }
        }

        return fetch(url, retryOptions)
      } catch (refreshError) {
        // Si le rafraîchissement échoue, déconnecter l'utilisateur
        authStore.logout()
        router.push('/login?expired=true')
        throw new Error('Session expirée, veuillez vous reconnecter')
      }
    }

    return response
  } catch (error) {
    // Gestion des erreurs réseau
    console.error('Erreur réseau:', error)
    throw error
  }
}


export async function doAjaxRequestWithAuth(url, options = {}) {
  const response = await httpInterceptor(url, options);

  // Vérifier si la réponse est vide ou non-JSON
  if (response.status === 204) {
    // 204 No Content - retourner un objet vide ou une valeur appropriée
    return {};
  }

  // Vérifier si la réponse a du contenu
  const text = await response.text();
  if (!text) {
    // Réponse vide - retourner un objet vide
    return {};
  }

  try {
    // Essayer de parser le JSON
    const result = JSON.parse(text);

    // Si la réponse n'est pas OK, lever une exception
    if (!response.ok) throw result;

    // Tout s'est bien passé, retourner le résultat
    return result;
  } catch (e) {
    // Erreur de parsing JSON
    console.error('Erreur de parsing JSON:', text);
    throw new Error('Réponse invalide du serveur: ' + text);
  }
}
