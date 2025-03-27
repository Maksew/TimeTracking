// src/services/groupService.js
import { doAjaxRequestWithAuth } from '@/util/httpInterceptor'
import { useAuthStore } from '@/stores/auth'

export default {
  // Récupérer tous les groupes
  getAllGroups() {
    return doAjaxRequestWithAuth('/api/groups')
  },

  // Récupérer les groupes de l'utilisateur connecté
  getUserGroups() {
    const authStore = useAuthStore()
    return doAjaxRequestWithAuth(`/api/groups/user/${authStore.user.id}`)
  },

  // Récupérer un groupe par son ID
  getGroupById(id) {
    return doAjaxRequestWithAuth(`/api/groups/${id}`)
  },

  // Créer un nouveau groupe
  createGroup(group, ownerId) {
    return doAjaxRequestWithAuth(`/api/groups?ownerId=${ownerId}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(group)
    })
  },

  // Rejoindre un groupe avec un code d'invitation
  joinGroup(invitCode, userId) {
    return doAjaxRequestWithAuth(`/api/groups/join?invitCode=${invitCode}&userId=${userId}`, {
      method: 'POST'
    })
  }
}
