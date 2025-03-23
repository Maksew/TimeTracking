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

  // Créer une nouvelle feuille de temps
  createTimeSheet(timeSheet) {
    return doAjaxRequestWithAuth('/api/timesheets', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(timeSheet)
    })
  }
}
