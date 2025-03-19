/**
 * Faire un appel AJAX avec l'API fetch
 * Permet de récupérer erreur réseau et erreur de l'API
 * usage :
 * doAjaxRequest(url, options).then(showResult).catch(showError);
 * @param {string} url L'URL de l'API
 * @param {object} options Les options de la requête AJAX
 * @returns {Promise} Une promesse qui sera résolue avec le résultat de l'appel AJAX
 * @throws {object} Une exception qui sera levée si l'API a signalé une erreur
 */
export default async function doAjaxRequest(url, options) {
  // On fait l'appel AJAX
  const response = await fetch(url, options);
  // On récupère le résultat transmis en format JSON
  const result = await response.json();
  // L'API a signalé une erreur, on lève une exception
  if (!response.ok) throw result;
  // Tout s'est bien passé, on renvoie le résultat
  return result;
}

/**
 * Vérifie si un token JWT est expiré
 * @param {string} token Le token JWT à vérifier
 * @returns {boolean} True si le token est expiré, sinon false
 */
export function isTokenExpired(token) {
  if (!token) return true;

  try {
    // Diviser le token en ses trois parties
    const parts = token.split('.');
    if (parts.length !== 3) return true;

    // Décoder la partie payload (deuxième partie)
    const payload = JSON.parse(atob(parts[1]));

    // Vérifier si le token a une date d'expiration
    if (!payload.exp) return false;

    // Comparer avec l'heure actuelle
    const now = Date.now() / 1000;
    return payload.exp < now;
  } catch (e) {
    console.error('Erreur lors de la vérification du token:', e);
    return true;
  }
}
