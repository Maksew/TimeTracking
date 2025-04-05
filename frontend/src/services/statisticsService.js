import { doAjaxRequestWithAuth } from '@/util/httpInterceptor';

/**
 * Service pour récupérer les statistiques de l'utilisateur et des groupes
 */
export default {
  /**
   * Récupère les statistiques de l'utilisateur connecté
   * @returns {Promise} Statistiques de l'utilisateur
   */
  async getCurrentUserStatistics() {
    return doAjaxRequestWithAuth('/api/statistics/user');
  },

  /**
   * Récupère les statistiques d'un utilisateur spécifique
   * @param {Number} userId ID de l'utilisateur
   * @returns {Promise} Statistiques de l'utilisateur
   */
  async getUserStatistics(userId) {
    return doAjaxRequestWithAuth(`/api/statistics/user/${userId}`);
  },

  /**
   * Récupère les statistiques d'un groupe
   * @param {Number} groupId ID du groupe
   * @returns {Promise} Statistiques du groupe
   */
  async getGroupStatistics(groupId) {
    return doAjaxRequestWithAuth(`/api/statistics/group/${groupId}`);
  },

  /**
   * Récupère les statistiques pour une période spécifique
   * @param {String} startDate Date de début (YYYY-MM-DD)
   * @param {String} endDate Date de fin (YYYY-MM-DD)
   * @returns {Promise} Statistiques pour la période
   */
  async getPeriodStatistics(startDate, endDate) {
    let url = '/api/statistics/period';
    const params = [];

    if (startDate) params.push(`startDate=${startDate}`);
    if (endDate) params.push(`endDate=${endDate}`);

    if (params.length > 0) {
      url += `?${params.join('&')}`;
    }

    return doAjaxRequestWithAuth(url);
  }
};
