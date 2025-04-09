import { doAjaxRequestWithAuth } from '@/util/httpInterceptor'

export default {
  // Récupérer toutes les feuilles de temps de l'utilisateur connecté
  getUserTimeSheets() {
    return doAjaxRequestWithAuth('/api/timesheets')
  },

  // Récupérer une feuille de temps par son ID
  getTimeSheetById(id) {
    return doAjaxRequestWithAuth(`/api/timesheets/${id}`)
  },

  // Récupérer les feuilles de temps par date
  getTimeSheetsByDate(date) {
    return doAjaxRequestWithAuth(`/api/timesheets/byDate?date=${date}`)
  },

  // Récupérer les feuilles de temps partagées avec l'utilisateur
  getSharedTimeSheets() {
    return doAjaxRequestWithAuth('/api/timesheets/shared')
  },

  // Récupérer les feuilles de temps partagées avec un groupe
  getGroupTimeSheets(groupId) {
    return doAjaxRequestWithAuth(`/api/timesheets/group/${groupId}`)
  },

  // Créer une nouvelle feuille de temps
  createTimeSheet(timeSheet) {
    return doAjaxRequestWithAuth('/api/timesheets', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(timeSheet)
    })
  },

  // Mettre à jour une feuille de temps
  updateTimeSheet(id, timeSheet) {
    return doAjaxRequestWithAuth(`/api/timesheets/${id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(timeSheet)
    })
  },

  // Supprimer une feuille de temps
  deleteTimeSheet(id) {
    return doAjaxRequestWithAuth(`/api/timesheets/${id}`, {
      method: 'DELETE'
    })
  },

  // Ajouter une tâche à une feuille de temps
  addTaskToTimeSheet(timeSheetId, taskId, duration) {
    return doAjaxRequestWithAuth(`/api/timesheets/${timeSheetId}/tasks`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ taskId, duration })
    })
  },

  updateTaskDuration(timeSheetId, taskId, durationInSeconds) {
    return doAjaxRequestWithAuth(`/api/timesheets/${timeSheetId}/tasks/${taskId}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ duration: durationInSeconds })
    })
  },

  // Partager une feuille de temps avec un utilisateur
  shareTimeSheetWithUser(timeSheetId, userId, accessLevel) {
    return doAjaxRequestWithAuth(`/api/timesheets/${timeSheetId}/share/user/${userId}?accessLevel=${accessLevel}`, {
      method: 'POST'
    })
  },

  // Partager une feuille de temps avec un groupe
  shareTimeSheetWithGroup(timeSheetId, groupId, accessLevel) {
    return doAjaxRequestWithAuth(`/api/timesheets/${timeSheetId}/share/group/${groupId}?accessLevel=${accessLevel}`, {
      method: 'POST'
    })
  },

  removeTaskFromTimeSheet(timeSheetId, taskId) {
    return doAjaxRequestWithAuth(`/api/timesheets/${timeSheetId}/tasks/${taskId}`, {
      method: 'DELETE'
    })
  },

  updateTaskCompletionState(timeSheetId, taskId, completed) {
    return doAjaxRequestWithAuth(`/api/timesheets/${timeSheetId}/tasks/${taskId}/complete`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ completed })
    })
  },

  // Exporter les feuilles de temps au format CSV
  exportTimeSheetsToCsv(startDate, endDate) {
    let url = '/api/timesheets/export/csv';
    if (startDate || endDate) {
      const params = [];
      if (startDate) params.push(`startDate=${startDate}`);
      if (endDate) params.push(`endDate=${endDate}`);
      url += `?${params.join('&')}`;
    }

    return doAjaxRequestWithAuth(url, {
      method: 'GET',
      headers: {
        'Accept': 'text/csv'
      }
    })
  }
}
