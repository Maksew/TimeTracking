import { doAjaxRequestWithAuth } from '@/util/httpInterceptor'
import { useAuthStore } from '@/stores/auth'


export default {
  // Récupérer tous les groupes
  getAllGroups() {
    return doAjaxRequestWithAuth('/api/groups')
  },

  // Récupérer les groupes de l'utilisateur connecté avec les membres complets
  getUserGroups() {
    const authStore = useAuthStore()
    // Ajout du paramètre includeMembers=true pour demander au backend de charger les détails des membres
    return doAjaxRequestWithAuth(`/api/groups/user/${authStore.user.id}?includeMembers=true`)
  },

  // Récupérer un groupe par son ID avec tous les détails des membres
  getGroupById(id) {
    return doAjaxRequestWithAuth(`/api/groups/${id}?includeMembers=true`)
  },

  // Charger les détails d'un membre du groupe par son ID
  getGroupMemberDetails(userId) {
    return doAjaxRequestWithAuth(`/api/users/${userId}`)
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
