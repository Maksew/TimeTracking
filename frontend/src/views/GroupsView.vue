<script setup>
import { ref, onMounted, computed, watch } from 'vue';
import groupService from '@/services/groupService';
import { useAuthStore } from '@/stores/auth';

const authStore = useAuthStore();
const myGroups = ref([]);
const joinedGroups = ref([]);
const loading = ref(true);
const activeTab = ref(0);
const selectedGroup = ref(null);
const groupDetails = ref(null);
const showGroupDetailsDialog = ref(false); // Variable pour le dialog de détails

// Modals
const createGroupDialog = ref(false);
const joinGroupDialog = ref(false);
const shareCodeDialog = ref(false);
const confirmLeaveDialog = ref(false);
const memberDetailsDialog = ref(false);

// Form fields
const newGroupName = ref('');
const invitationCode = ref('');
const groupToLeave = ref(null);
const selectedMember = ref(null);

// Errors & messages
const error = ref('');
const successMessage = ref('');

// Constantes pour filtrage et tri
const sortOptions = ref([
  { title: 'Nom (A-Z)', value: 'name' },
  { title: 'Nom (Z-A)', value: '-name' },
  { title: 'Membres (croissant)', value: 'members' },
  { title: 'Membres (décroissant)', value: '-members' },
  { title: 'Dernière activité', value: 'activity' }
]);
const selectedSort = ref('name');
const searchQuery = ref('');

// Calcul de groupes filtrés et triés
const filteredMyGroups = computed(() => {
  return filterAndSortGroups(myGroups.value);
});

const filteredJoinedGroups = computed(() => {
  return filterAndSortGroups(joinedGroups.value);
});

// Format a date for display in French format
function formatDate(date) {
  return new Date(date).toLocaleDateString('fr-FR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric'
  });
}

function filterAndSortGroups(groups) {
  // Filtrer par recherche
  let filtered = groups;
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase();
    filtered = groups.filter(group =>
      group.name.toLowerCase().includes(query)
    );
  }

  // Trier
  return [...filtered].sort((a, b) => {
    const sortBy = selectedSort.value;
    const direction = sortBy.startsWith('-') ? -1 : 1;
    const field = sortBy.replace(/^-/, '');

    if (field === 'name') {
      return direction * a.name.localeCompare(b.name);
    } else if (field === 'members') {
      return direction * (a.userGroups.length - b.userGroups.length);
    } else {
      return 0;
    }
  });
}

const loadGroupMembers = async (group) => {
  if (!group || !group.userGroups) return;

  console.log('Chargement des membres pour le groupe:', group.name);
  console.log('UserGroups actuels:', group.userGroups);

  // Pour chaque UserGroup qui n'a pas d'information utilisateur complète
  for (const userGroup of group.userGroups) {
    if (!userGroup.user || !userGroup.user.pseudo) {
      try {
        // Charger les détails de l'utilisateur
        console.log('Chargement des détails pour l\'utilisateur:', userGroup.userId);
        const userData = await groupService.getGroupMemberDetails(userGroup.userId);
        console.log('Données utilisateur récupérées:', userData);

        // Mettre à jour l'objet userGroup avec les données complètes
        userGroup.user = userData;
      } catch (err) {
        console.error(`Erreur lors du chargement des détails pour l'utilisateur #${userGroup.userId}:`, err);
      }
    }
  }
};

onMounted(async () => {
  try {
    const groups = await groupService.getUserGroups();
    console.log('Groupes récupérés:', groups);

    // Séparer les groupes dont je suis propriétaire des autres
    myGroups.value = groups.filter(group => {
      const userGroup = group.userGroups?.find(ug => ug.userId === authStore.user.id);
      return userGroup && userGroup.role === 'OWNER';
    });

    joinedGroups.value = groups.filter(group => {
      const userGroup = group.userGroups?.find(ug => ug.userId === authStore.user.id);
      return userGroup && userGroup.role !== 'OWNER';
    });

    // Précharger les détails des membres pour tous les groupes
    for (const group of [...myGroups.value, ...joinedGroups.value]) {
      await loadGroupMembers(group);
    }

    // Force le rafraîchissement de l'interface après chargement complet
    myGroups.value = [...myGroups.value];
    joinedGroups.value = [...joinedGroups.value];

  } catch (err) {
    console.error('Erreur lors du chargement des groupes:', err);
    error.value = 'Erreur lors du chargement des groupes.';
  } finally {
    loading.value = false;
  }
});

const createGroup = async () => {
  if (!newGroupName.value.trim()) {
    error.value = 'Veuillez saisir un nom de groupe';
    return;
  }

  try {
    error.value = '';
    const createdGroup = await groupService.createGroup({ name: newGroupName.value }, authStore.user.id);
    myGroups.value.push(createdGroup);
    newGroupName.value = '';
    createGroupDialog.value = false;
    successMessage.value = 'Groupe créé avec succès!';

    // Réinitialiser le message après 3 secondes
    setTimeout(() => {
      successMessage.value = '';
    }, 3000);
  } catch (err) {
    error.value = err.message || 'Erreur lors de la création du groupe';
  }
};

const getCorrectMemberCount = (group) => {
  console.log('Comptage des membres pour le groupe:', group?.name);
  console.log('userGroups:', group?.userGroups);

  // Si userGroups existe et est un tableau, retourner sa longueur
  if (group && Array.isArray(group.userGroups)) {
    return group.userGroups.length;
  }

  // Valeur par défaut
  return 1;
}

const joinGroup = async () => {
  if (!invitationCode.value.trim()) {
    error.value = 'Veuillez saisir un code d\'invitation';
    return;
  }

  try {
    error.value = '';
    const joinedGroup = await groupService.joinGroup(invitationCode.value, authStore.user.id);
    joinedGroups.value.push(joinedGroup);
    invitationCode.value = '';
    joinGroupDialog.value = false;
    successMessage.value = 'Vous avez rejoint le groupe avec succès!';

    // Passer à l'onglet des groupes rejoints
    activeTab.value = 1;

    // Réinitialiser le message après 3 secondes
    setTimeout(() => {
      successMessage.value = '';
    }, 3000);
  } catch (err) {
    error.value = err.message || 'Erreur lors de la jointure au groupe';
  }
};

const showGroupDetails = async (group) => {
  selectedGroup.value = group;
  // Charger les détails complets du groupe
  try {
    // Charger les détails des membres manquants
    await loadGroupMembers(group);
    // Mettre à jour les détails du groupe
    groupDetails.value = { ...group };
    showGroupDetailsDialog.value = true;
  } catch (err) {
    console.error('Erreur lors du chargement des détails du groupe:', err);
    error.value = 'Impossible de charger tous les détails du groupe';
  }
};

const showShareCode = (group) => {
  selectedGroup.value = group;
  shareCodeDialog.value = true;
};

const confirmLeaveGroup = (group) => {
  groupToLeave.value = group;
  confirmLeaveDialog.value = true;
};

const leaveGroup = async () => {
  if (!groupToLeave.value) return;

  try {
    // Appel au service pour quitter le groupe
    // Note: Ce service doit être implémenté
    // await groupService.leaveGroup(groupToLeave.value.id, authStore.user.id);

    // Retirer le groupe de la liste
    joinedGroups.value = joinedGroups.value.filter(g => g.id !== groupToLeave.value.id);
    confirmLeaveDialog.value = false;
    groupToLeave.value = null;
    successMessage.value = 'Vous avez quitté le groupe.';

    // Réinitialiser le message après 3 secondes
    setTimeout(() => {
      successMessage.value = '';
    }, 3000);
  } catch (err) {
    error.value = err.message || 'Erreur lors du départ du groupe';
  }
};

const showMemberDetails = (member) => {
  selectedMember.value = member;
  memberDetailsDialog.value = true;
};

const copyInviteCode = () => {
  if (selectedGroup.value) {
    navigator.clipboard.writeText(selectedGroup.value.invitCode)
      .then(() => {
        successMessage.value = 'Code d\'invitation copié dans le presse-papier!';
        setTimeout(() => {
          successMessage.value = '';
        }, 3000);
      })
      .catch(() => {
        error.value = 'Impossible de copier le code. Veuillez le sélectionner manuellement.';
      });
  }
};

// Watcher pour s'assurer que showGroupDetailsDialog reste cohérent avec selectedGroup
watch(selectedGroup, (newVal) => {
  if (newVal === null) {
    showGroupDetailsDialog.value = false;
  }
});

// Fonction pour fermer la dialogue des détails
const closeGroupDetails = () => {
  selectedGroup.value = null;
  showGroupDetailsDialog.value = false;
};
</script>

<template>
  <div class="groups-container pa-6">
    <!-- En-tête avec titre et actions -->
    <div class="d-flex align-center mb-4">
      <h2 class="text-h4 font-weight-medium text-white">Mes groupes</h2>
      <v-spacer></v-spacer>

      <div class="search-container mr-4">
        <v-text-field
          v-model="searchQuery"
          density="compact"
          variant="outlined"
          label="Rechercher un groupe"
          append-inner-icon="mdi-magnify"
          hide-details
          bg-color="rgb(26, 35, 126)"
          style="max-width: 250px;"
        ></v-text-field>
      </div>

      <v-select
        v-model="selectedSort"
        :items="sortOptions"
        density="compact"
        variant="outlined"
        label="Trier par"
        hide-details
        bg-color="rgb(26, 35, 126)"
        style="max-width: 180px;"
        class="mr-4"
      ></v-select>

      <v-btn color="primary" class="mr-2" prepend-icon="mdi-plus" @click="createGroupDialog = true">
        Créer
      </v-btn>
      <v-btn color="secondary" prepend-icon="mdi-account-group" @click="joinGroupDialog = true">
        Rejoindre
      </v-btn>
    </div>

    <!-- Messages de succès et d'erreur -->
    <v-alert
      v-if="successMessage"
      type="success"
      class="mb-4"
      closable
      @click:close="successMessage = ''"
    >
      {{ successMessage }}
    </v-alert>

    <v-alert
      v-if="error"
      type="error"
      class="mb-4"
      closable
      @click:close="error = ''"
    >
      {{ error }}
    </v-alert>

    <v-progress-linear v-if="loading" indeterminate color="primary"></v-progress-linear>

    <template v-else>
      <!-- Onglets pour naviguer entre "Mes Groupes" et "Groupes rejoints" -->
      <v-tabs
        v-model="activeTab"
        color="primary"
        align-tabs="start"
        class="mb-4"
        bg-color="rgb(26, 35, 126)"
      >
        <v-tab value="0">
          <v-icon start>mdi-account-multiple-plus</v-icon>
          Mes groupes ({{ myGroups.length }})
        </v-tab>
        <v-tab value="1">
          <v-icon start>mdi-account-group</v-icon>
          Groupes rejoints ({{ joinedGroups.length }})
        </v-tab>
      </v-tabs>

      <v-window v-model="activeTab">
        <!-- Mes groupes -->
        <v-window-item value="0">
          <v-row>
            <template v-if="filteredMyGroups.length > 0">
              <v-col v-for="group in filteredMyGroups" :key="group.id" cols="12" sm="6" md="4" lg="3">
                <v-hover v-slot="{ isHovering, props }">
                  <v-card
                    v-bind="props"
                    color="#283593"
                    dark
                    class="rounded-lg group-card h-100"
                    :elevation="isHovering ? 8 : 2"
                  >
                    <v-card-title class="d-flex align-center">
                      <v-avatar color="#1a237e" size="40" class="mr-3">
                        <v-icon>mdi-account-group</v-icon>
                      </v-avatar>
                      <div class="text-truncate">{{ group.name }}</div>
                    </v-card-title>

                    <v-card-text>
                      <div class="d-flex align-center mb-2">
                        <v-icon size="small" class="mr-2">mdi-account-multiple</v-icon>
                        <span>{{ group.userGroups && group.userGroups.length > 0 ? group.userGroups.length : 1 }}
                          membre{{ group.userGroups && group.userGroups.length !== 1 ? 's' : '' }}</span>
                      </div>

                      <div class="d-flex align-center mb-2">
                        <v-icon size="small" class="mr-2">mdi-key</v-icon>
                        <span>Code: <b>{{ group.invitCode }}</b></span>
                        <v-btn icon size="small" class="ml-1" @click.stop="showShareCode(group)">
                          <v-icon size="small">mdi-content-copy</v-icon>
                          <v-tooltip activator="parent" location="top">Copier le code</v-tooltip>
                        </v-btn>
                      </div>
                    </v-card-text>

                    <v-divider></v-divider>

                    <v-card-actions>
                      <v-btn variant="text" color="white" @click="showGroupDetails(group)">
                        <v-icon start>mdi-eye</v-icon>
                        Détails
                      </v-btn>
                      <v-spacer></v-spacer>
                      <v-menu>
                        <template v-slot:activator="{ props }">
                          <v-btn icon v-bind="props">
                            <v-icon>mdi-dots-vertical</v-icon>
                          </v-btn>
                        </template>
                        <v-list bg-color="#283593">
                          <v-list-item @click="showShareCode(group)">
                            <template v-slot:prepend>
                              <v-icon>mdi-share-variant</v-icon>
                            </template>
                            <v-list-item-title>Partager</v-list-item-title>
                          </v-list-item>
                          <v-list-item href="#edit">
                            <template v-slot:prepend>
                              <v-icon>mdi-pencil</v-icon>
                            </template>
                            <v-list-item-title>Modifier</v-list-item-title>
                          </v-list-item>
                          <v-list-item href="#delete" color="error">
                            <template v-slot:prepend>
                              <v-icon color="error">mdi-delete</v-icon>
                            </template>
                            <v-list-item-title class="text-error">Supprimer</v-list-item-title>
                          </v-list-item>
                        </v-list>
                      </v-menu>
                    </v-card-actions>
                  </v-card>
                </v-hover>
              </v-col>
            </template>

            <v-col v-else-if="searchQuery" cols="12">
              <v-card color="#283593" dark class="rounded-lg">
                <v-card-text class="text-center py-5">
                  <v-icon size="large" class="mb-2">mdi-file-search</v-icon>
                  <p>Aucun groupe ne correspond à votre recherche "{{ searchQuery }}"</p>
                  <v-btn color="primary" class="mt-3" @click="searchQuery = ''">
                    Réinitialiser la recherche
                  </v-btn>
                </v-card-text>
              </v-card>
            </v-col>

            <v-col v-else cols="12">
              <v-card color="#283593" dark class="rounded-lg">
                <v-card-text class="text-center py-5">
                  <v-icon size="large" class="mb-2">mdi-account-group</v-icon>
                  <p>Vous ne gérez aucun groupe</p>
                  <v-btn color="primary" class="mt-3" @click="createGroupDialog = true">
                    <v-icon start>mdi-plus</v-icon>
                    Créer un groupe
                  </v-btn>
                </v-card-text>
              </v-card>
            </v-col>
          </v-row>
        </v-window-item>

        <!-- Groupes rejoints -->
        <v-window-item value="1">
          <v-row>
            <template v-if="filteredJoinedGroups.length > 0">
              <v-col v-for="group in filteredJoinedGroups" :key="group.id" cols="12" sm="6" md="4" lg="3">
                <v-hover v-slot="{ isHovering, props }">
                  <v-card
                    v-bind="props"
                    color="#283593"
                    dark
                    class="rounded-lg group-card h-100"
                    :elevation="isHovering ? 8 : 2"
                  >
                    <v-card-title class="d-flex align-center">
                      <v-avatar color="#1a237e" size="40" class="mr-3">
                        <v-icon>mdi-account-group</v-icon>
                      </v-avatar>
                      <div class="text-truncate">{{ group.name }}</div>
                    </v-card-title>

                    <v-card-text>
                      <div class="d-flex align-center mb-2">
                        <v-icon size="small" class="mr-2">mdi-account-multiple</v-icon>
                        <span>{{ getCorrectMemberCount(group) }} membre{{ getCorrectMemberCount(group) !== 1 ? 's' : '' }}</span>
                      </div>

                      <div class="d-flex align-center mb-2">
                        <v-icon size="small" class="mr-2">mdi-calendar-check</v-icon>
                        <span>Rejoint le {{ formatDate(new Date()) }}</span>
                      </div>
                    </v-card-text>

                    <v-divider></v-divider>

                    <v-card-actions>
                      <v-btn variant="text" color="white" @click="showGroupDetails(group)">
                        <v-icon start>mdi-eye</v-icon>
                        Détails
                      </v-btn>
                      <v-spacer></v-spacer>
                      <v-btn color="error" variant="text" @click="confirmLeaveGroup(group)">
                        <v-icon start>mdi-exit-to-app</v-icon>
                        Quitter
                      </v-btn>
                    </v-card-actions>
                  </v-card>
                </v-hover>
              </v-col>
            </template>

            <v-col v-else-if="searchQuery" cols="12">
              <v-card color="#283593" dark class="rounded-lg">
                <v-card-text class="text-center py-5">
                  <v-icon size="large" class="mb-2">mdi-file-search</v-icon>
                  <p>Aucun groupe ne correspond à votre recherche "{{ searchQuery }}"</p>
                  <v-btn color="primary" class="mt-3" @click="searchQuery = ''">
                    Réinitialiser la recherche
                  </v-btn>
                </v-card-text>
              </v-card>
            </v-col>

            <v-col v-else cols="12">
              <v-card color="#283593" dark class="rounded-lg">
                <v-card-text class="text-center py-5">
                  <v-icon size="large" class="mb-2">mdi-account-group</v-icon>
                  <p>Vous n'avez rejoint aucun groupe</p>
                  <v-btn color="primary" class="mt-3" @click="joinGroupDialog = true">
                    <v-icon start>mdi-account-plus</v-icon>
                    Rejoindre un groupe
                  </v-btn>
                </v-card-text>
              </v-card>
            </v-col>
          </v-row>
        </v-window-item>
      </v-window>
    </template>

    <!-- Dialog: Créer un groupe -->
    <v-dialog v-model="createGroupDialog" max-width="500px">
      <v-card color="#283593" dark>
        <v-card-title>
          <v-icon start class="mr-2">mdi-plus</v-icon>
          Créer un nouveau groupe
        </v-card-title>
        <v-card-text>
          <v-alert v-if="error" type="error" density="compact" class="mb-3">
            {{ error }}
          </v-alert>

          <v-text-field
            v-model="newGroupName"
            label="Nom du groupe"
            variant="outlined"
            bg-color="#1a237e"
            placeholder="Ex: Mon équipe de projet"
            hint="Choisissez un nom descriptif"
            persistent-hint
            autofocus
          ></v-text-field>
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn text @click="createGroupDialog = false">Annuler</v-btn>
          <v-btn
            color="primary"
            @click="createGroup"
            :disabled="!newGroupName.trim()"
          >
            Créer
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Dialog: Rejoindre un groupe -->
    <v-dialog v-model="joinGroupDialog" max-width="500px">
      <v-card color="#283593" dark>
        <v-card-title>
          <v-icon start class="mr-2">mdi-account-plus</v-icon>
          Rejoindre un groupe
        </v-card-title>
        <v-card-text>
          <v-alert v-if="error" type="error" density="compact" class="mb-3">
            {{ error }}
          </v-alert>

          <v-text-field
            v-model="invitationCode"
            label="Code d'invitation"
            variant="outlined"
            bg-color="#1a237e"
            placeholder="Ex: ABC12345"
            hint="Demandez le code d'invitation à l'administrateur du groupe"
            persistent-hint
            autofocus
            autocapitalize="characters"
          ></v-text-field>
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn text @click="joinGroupDialog = false">Annuler</v-btn>
          <v-btn
            color="primary"
            @click="joinGroup"
            :disabled="!invitationCode.trim()"
          >
            Rejoindre
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Dialog: Partager le code -->
    <v-dialog v-model="shareCodeDialog" max-width="500px">
      <v-card color="#283593" dark>
        <v-card-title>
          <v-icon start class="mr-2">mdi-share-variant</v-icon>
          Partager le groupe
        </v-card-title>
        <v-card-text>
          <p class="mb-4">Partagez ce code avec les personnes que vous souhaitez inviter à rejoindre votre groupe.</p>

          <v-alert type="info" variant="tonal" class="mb-4">
            <div class="text-center">
              <div class="text-h5 font-weight-bold my-2">{{ selectedGroup?.invitCode }}</div>
              <v-btn color="primary" prepend-icon="mdi-content-copy" @click="copyInviteCode">
                Copier
              </v-btn>
            </div>
          </v-alert>

          <p class="text-caption">Les membres qui rejoignent votre groupe pourront voir toutes les feuilles de temps partagées avec le groupe.</p>
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn text @click="shareCodeDialog = false">Fermer</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Dialog: Confirmer quitter un groupe -->
    <v-dialog v-model="confirmLeaveDialog" max-width="400px">
      <v-card color="#283593" dark>
        <v-card-title>Quitter le groupe?</v-card-title>
        <v-card-text>
          <p>Êtes-vous sûr de vouloir quitter le groupe "{{ groupToLeave?.name }}"?</p>
          <p class="text-caption mt-2">Vous devrez être invité à nouveau pour rejoindre ce groupe.</p>
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn text @click="confirmLeaveDialog = false">Annuler</v-btn>
          <v-btn color="error" @click="leaveGroup">
            Quitter le groupe
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Dialog: Détails du groupe (modal plus large) -->
    <v-dialog v-model="showGroupDetailsDialog" max-width="800px" @click:outside="closeGroupDetails">
      <v-card v-if="selectedGroup" color="#283593" dark>
        <v-card-title class="d-flex align-center">
          <v-avatar color="#1a237e" size="40" class="mr-3">
            <v-icon>mdi-account-group</v-icon>
          </v-avatar>
          <div>{{ selectedGroup.name }}</div>
          <v-spacer></v-spacer>
          <v-btn icon @click="closeGroupDetails">
            <v-icon>mdi-close</v-icon>
          </v-btn>
        </v-card-title>

        <v-divider></v-divider>

        <v-card-text class="mt-3">
          <v-row>
            <v-col cols="12" md="6">
              <h3 class="text-h6 mb-3">
                <v-icon start>mdi-information</v-icon>
                Informations
              </h3>

              <v-list bg-color="transparent" class="py-0">
                <v-list-item>
                  <template v-slot:prepend>
                    <v-icon>mdi-account-multiple</v-icon>
                  </template>
                  <v-list-item-title>{{ getCorrectMemberCount(selectedGroup) }} membre{{ getCorrectMemberCount(selectedGroup) !== 1 ? 's' : '' }}</v-list-item-title>
                </v-list-item>

                <v-list-item v-if="selectedGroup.invitCode">
                  <template v-slot:prepend>
                    <v-icon>mdi-key</v-icon>
                  </template>
                  <v-list-item-title>
                    Code d'invitation: {{ selectedGroup.invitCode }}
                    <v-btn icon size="small" class="ml-1" @click="copyInviteCode">
                      <v-icon size="small">mdi-content-copy</v-icon>
                    </v-btn>
                  </v-list-item-title>
                </v-list-item>

                <v-list-item>
                  <template v-slot:prepend>
                    <v-icon>mdi-shield-account</v-icon>
                  </template>
                  <v-list-item-title>
                    Rôle: {{
                      selectedGroup.userGroups.find(ug => ug.userId === authStore.user.id)?.role === 'OWNER'
                        ? 'Administrateur'
                        : 'Membre'
                    }}
                  </v-list-item-title>
                </v-list-item>
              </v-list>

              <h3 class="text-h6 mb-3 mt-6">
                <v-icon start>mdi-file-document-multiple</v-icon>
                Feuilles de temps partagées
              </h3>

              <v-list bg-color="transparent" class="py-0">
                <v-list-item v-if="selectedGroup.sharedTimeSheets && selectedGroup.sharedTimeSheets.length > 0">
                  <v-list-item-title>
                    {{ selectedGroup.sharedTimeSheets.length }} feuilles de temps partagées
                  </v-list-item-title>
                </v-list-item>
                <v-list-item v-else>
                  <v-list-item-title>
                    Aucune feuille de temps partagée
                  </v-list-item-title>
                </v-list-item>
              </v-list>
            </v-col>

            <v-col cols="12" md="6">
              <h3 class="text-h6 mb-3">
                <v-icon start>mdi-account-group</v-icon>
                Membres
              </h3>

              <v-list bg-color="#1a237e" class="rounded">
                <v-list-item
                  v-for="(userGroup, index) in selectedGroup.userGroups"
                  :key="index"
                  :active="userGroup.userId === authStore.user.id"
                  @click="showMemberDetails(userGroup)"
                >
                  <template v-slot:prepend>
                    <v-avatar color="#5c6bc0" size="36">
                      <v-icon v-if="userGroup.user && userGroup.user.pseudo">
                        {{ userGroup.user.pseudo.charAt(0).toUpperCase() }}
                      </v-icon>
                      <v-icon v-else>mdi-account</v-icon>
                    </v-avatar>
                  </template>

                  <v-list-item-title>
                    {{ userGroup.user && userGroup.user.pseudo ? userGroup.user.pseudo :
                    (userGroup.userId === authStore.user.id ? authStore.user.pseudo : `Utilisateur #${userGroup.userId}`) }}
                    <v-chip
                      v-if="userGroup.role === 'OWNER'"
                      size="x-small"
                      color="primary"
                      class="ml-2"
                    >
                      Admin
                    </v-chip>
                  </v-list-item-title>

                  <template v-slot:append>
                    <v-btn
                      v-if="userGroup.userId === authStore.user.id && userGroup.role !== 'OWNER'"
                      icon
                      size="small"
                      color="error"
                      variant="text"
                      @click.stop="confirmLeaveGroup(selectedGroup)"
                    >
                      <v-icon>mdi-exit-to-app</v-icon>
                      <v-tooltip activator="parent" location="top">Quitter le groupe</v-tooltip>
                    </v-btn>
                  </template>
                </v-list-item>
              </v-list>
            </v-col>
          </v-row>
        </v-card-text>

        <v-divider></v-divider>

        <v-card-actions>
          <v-spacer></v-spacer>

          <template v-if="selectedGroup.userGroups.find(ug => ug.userId === authStore.user.id)?.role === 'OWNER'">
            <v-btn color="primary" prepend-icon="mdi-share-variant" @click="showShareCode(selectedGroup)">
              Partager
            </v-btn>
            <v-btn color="primary" variant="outlined" prepend-icon="mdi-pencil" class="ml-2">
              Modifier
            </v-btn>
          </template>

          <template v-else>
            <v-btn color="error" prepend-icon="mdi-exit-to-app" @click="confirmLeaveGroup(selectedGroup)">
              Quitter le groupe
            </v-btn>
          </template>

          <v-btn variant="text" @click="closeGroupDetails" class="ml-2">
            Fermer
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<style scoped>
.groups-container {
  min-height: 80vh;
  background-color: #1a237e;
}

.group-card {
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.group-card:hover {
  transform: translateY(-5px);
}
</style>
