// src/util/httpInterceptor.js
import { useAuthStore } from '@/stores/auth'
import { useRouter } from 'vue-router'
import { API_BASE_URL } from '@/config/api.config';

/**
 * Intercepteur HTTP pour ajouter le token JWT aux requêtes
 * et gérer les réponses 401 (non autorisé)
 */
export async function httpInterceptor(url, options = {}) {
  const authStore = useAuthStore()
  const router = useRouter()

  console.log(`Envoi d'une requête à ${url}`);
  console.log('Options:', { ...options, headers: options.headers });

  // Si l'utilisateur est authentifié, ajouter le token aux en-têtes
  if (authStore.isAuthenticated) {
    options.headers = {
      ...options.headers,
      'Authorization': authStore.getAuthorizationHeader()
    }
  }

  try {
    const response = await fetch(url, options)

    console.log(`Réponse reçue de ${url}:`, {
      status: response.status,
      statusText: response.statusText,
      headers: Object.fromEntries([...response.headers])
    });

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

/**
 * Fonction pour faire des requêtes AJAX avec authentification
 * Traite les réponses et gère les erreurs courantes
 */
export async function doAjaxRequestWithAuth(url, options = {}) {
  const baseUrl = 'https://intermediate-pansie-maksew-0cc92781.koyeb.app';
  const fullUrl = new URL(url, API_BASE_URL).toString();

  console.log(`Envoi de requête à: ${fullUrl}`);
  console.log('Options:', JSON.stringify({
    method: options.method,
    headers: options.headers,
    body: options.body ? '(présent)' : '(absent)'
  }));

  try {
    const response = await httpInterceptor(fullUrl, options);

    console.log(`Réponse reçue de ${fullUrl}:`, {
      status: response.status,
      statusText: response.statusText
    });

    // Traitement spécial pour les erreurs 403 (forbidden) et 401 (unauthorized)
    if (response.status === 403 || response.status === 401) {
      const responseText = await response.clone().text();
      console.log(`Contenu de la réponse ${response.status}:`, responseText);
      console.log('Headers de réponse:', Object.fromEntries([...response.headers]));

      // Si on reçoit une erreur 403, il est possible que ce soit une erreur CORS ou un problème d'authentification
      if (response.status === 403) {
        throw new Error(`Accès refusé (403): ${responseText || 'Pas de détails disponibles'}`);
      }
    }

    // Vérifier si la réponse est vide ou non-JSON
    if (response.status === 204) {
      // 204 No Content - retourner un objet vide ou une valeur appropriée
      return {};
    }

    // Récupérer le contenu textuel de la réponse
    const text = await response.text();
    if (!text) {
      // Réponse vide - retourner un objet vide
      return {};
    }

    try {
      // Essayer de parser le JSON
      const result = JSON.parse(text);

      // Si la réponse n'est pas OK, lever une exception avec les détails
      if (!response.ok) {
        const errorMessage = result.error || result.message || JSON.stringify(result);
        throw new Error(`Erreur ${response.status}: ${errorMessage}`);
      }

      // Tout s'est bien passé, retourner le résultat
      console.log('Réponse JSON valide reçue');
      return result;
    } catch (e) {
      // Erreur de parsing JSON
      console.error('Erreur de parsing JSON:', e);
      console.error('Texte reçu:', text);

      // Si le texte semble être du HTML, informer clairement dans l'erreur
      if (text.includes('<html') || text.includes('<!DOCTYPE')) {
        throw new Error(`Réponse HTML reçue au lieu de JSON. Statut: ${response.status}. Vérifiez la configuration CORS.`);
      } else {
        throw new Error(`Réponse invalide du serveur (${response.status}): ${text.substring(0, 100)}...`);
      }
    }
  } catch (error) {
    // Capturer toutes les erreurs, y compris les erreurs réseau
    console.error('Erreur lors de la requête:', error);

    // Relancer l'erreur pour qu'elle puisse être traitée par l'appelant
    throw error;
  }
}
