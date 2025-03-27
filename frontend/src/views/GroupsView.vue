<script setup>
import { ref, onMounted } from 'vue';
import groupService from '@/services/groupService';
import { useAuthStore } from '@/stores/auth';

const authStore = useAuthStore();
const myGroups = ref([]);
const joinedGroups = ref([]);
const loading = ref(true);

// Modals
const createGroupDialog = ref(false);
const joinGroupDialog = ref(false);

// Form fields
const newGroupName = ref('');
const invitationCode = ref('');

// Errors
const error = ref('');

onMounted(async () => {
  try {
    const groups = await groupService.getUserGroups();

    // Séparer les groupes dont je suis propriétaire des autres
    myGroups.value = groups.filter(group => {
      const userGroup = group.userGroups.find(ug => ug.userId === authStore.user.id);
      return userGroup && userGroup.role === 'OWNER';
    });

    joinedGroups.value = groups.filter(group => {
      const userGroup = group.userGroups.find(ug => ug.userId === authStore.user.id);
      return userGroup && userGroup.role !== 'OWNER';
    });
  } catch (err) {
    console.error('Erreur lors du chargement des groupes:', err);
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
    await groupService.createGroup({ name: newGroupName.value }, authStore.user.id);
    newGroupName.value = '';
    createGroupDialog.value = false;

    // Recharger les groupes
    const groups = await groupService.getUserGroups();
    myGroups.value = groups.filter(group => {
      const userGroup = group.userGroups.find(ug => ug.userId === authStore.user.id);
      return userGroup && userGroup.role === 'OWNER';
    });
  } catch (err) {
    error.value = err.message || 'Erreur lors de la création du groupe';
  }
};

const joinGroup = async () => {
  if (!invitationCode.value.trim()) {
    error.value = 'Veuillez saisir un code d\'invitation';
    return;
  }

  try {
    error.value = '';
    await groupService.joinGroup(invitationCode.value, authStore.user.id);
    invitationCode.value = '';
    joinGroupDialog.value = false;

    // Recharger les groupes
    const groups = await groupService.getUserGroups();
    joinedGroups.value = groups.filter(group => {
      const userGroup = group.userGroups.find(ug => ug.userId === authStore.user.id);
      return userGroup && userGroup.role !== 'OWNER';
    });
  } catch (err) {
    error.value = err.message || 'Erreur lors de la jointure au groupe';
  }
};
</script>

<template>
  <div class="groups-container pa-6">
    <div class="d-flex align-center mb-6">
      <h2 class="text-h4 font-weight-medium text-white">Mes groupes</h2>
      <v-spacer></v-spacer>
      <v-btn color="primary" class="mr-2" @click="createGroupDialog = true">
        <v-icon class="mr-1">mdi-plus</v-icon>
        Créer un groupe
      </v-btn>
      <v-btn color="secondary" @click="joinGroupDialog = true">
        <v-icon class="mr-1">mdi-account-group</v-icon>
        Rejoindre un groupe
      </v-btn>
    </div>

    <v-progress-linear v-if="loading" indeterminate></v-progress-linear>

    <template v-else>
      <!-- Mes groupes -->
      <h3 class="text-h5 font-weight-medium text-white mb-3">Groupes que je gère</h3>
      <v-row>
        <template v-if="myGroups.length > 0">
          <v-col v-for="group in myGroups" :key="group.id" cols="12" sm="6" md="4">
            <v-card color="#283593" dark class="rounded-lg">
              <v-card-title class="d-flex align-center">
                <v-avatar color="#1a237e" size="40" class="mr-3">
                  <v-icon>mdi-account-group</v-icon>
                </v-avatar>
                {{ group.name }}
              </v-card-title>

              <v-card-text>
                <div class="d-flex align-center mb-2">
                  <v-icon size="small" class="mr-2">mdi-account-multiple</v-icon>
                  <span>{{ group.userGroups.length }} membres</span>
                </div>

                <div class="d-flex align-center mb-2">
                  <v-icon size="small" class="mr-2">mdi-key</v-icon>
                  <span>Code d'invitation: <b>{{ group.invitCode }}</b></span>
                </div>
              </v-card-text>

              <v-card-actions>
                <v-btn variant="text" color="white">
                  Gérer
                </v-btn>
                <v-spacer></v-spacer>
                <v-btn icon>
                  <v-icon>mdi-dots-vertical</v-icon>
                </v-btn>
              </v-card-actions>
            </v-card>
          </v-col>
        </template>

        <v-col v-else cols="12">
          <v-card color="#283593" dark class="rounded-lg">
            <v-card-text class="text-center py-5">
              <v-icon size="large" class="mb-2">mdi-account-group</v-icon>
              <p>Vous ne gérez aucun groupe</p>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>

      <!-- Groupes rejoints -->
      <h3 class="text-h5 font-weight-medium text-white mt-6 mb-3">Groupes rejoints</h3>
      <v-row>
        <template v-if="joinedGroups.length > 0">
          <v-col v-for="group in joinedGroups" :key="group.id" cols="12" sm="6" md="4">
            <v-card color="#283593" dark class="rounded-lg">
              <v-card-title class="d-flex align-center">
                <v-avatar color="#1a237e" size="40" class="mr-3">
                  <v-icon>mdi-account-group</v-icon>
                </v-avatar>
                {{ group.name }}
              </v-card-title>

              <v-card-text>
                <div class="d-flex align-center mb-2">
                  <v-icon size="small" class="mr-2">mdi-account-multiple</v-icon>
                  <span>{{ group.userGroups.length }} membres</span>
                </div>
              </v-card-text>

              <v-card-actions>
                <v-btn variant="text" color="white">
                  Voir
                </v-btn>
              </v-card-actions>
            </v-card>
          </v-col>
        </template>

        <v-col v-else cols="12">
          <v-card color="#283593" dark class="rounded-lg">
            <v-card-text class="text-center py-5">
              <v-icon size="large" class="mb-2">mdi-account-group</v-icon>
              <p>Vous n'avez rejoint aucun groupe</p>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>
    </template>

    <!-- Dialog: Créer un groupe -->
    <v-dialog v-model="createGroupDialog" max-width="500px">
      <v-card color="#283593" dark>
        <v-card-title>Créer un nouveau groupe</v-card-title>
        <v-card-text>
          <v-alert v-if="error" type="error" density="compact" class="mb-3">
            {{ error }}
          </v-alert>

          <v-text-field
            v-model="newGroupName"
            label="Nom du groupe"
            variant="outlined"
            bg-color="#1a237e"
          ></v-text-field>
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn text @click="createGroupDialog = false">Annuler</v-btn>
          <v-btn color="primary" @click="createGroup">Créer</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Dialog: Rejoindre un groupe -->
    <v-dialog v-model="joinGroupDialog" max-width="500px">
      <v-card color="#283593" dark>
        <v-card-title>Rejoindre un groupe</v-card-title>
        <v-card-text>
          <v-alert v-if="error" type="error" density="compact" class="mb-3">
            {{ error }}
          </v-alert>

          <v-text-field
            v-model="invitationCode"
            label="Code d'invitation"
            variant="outlined"
            bg-color="#1a237e"
          ></v-text-field>
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn text @click="joinGroupDialog = false">Annuler</v-btn>
          <v-btn color="primary" @click="joinGroup">Rejoindre</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<style scoped>
.groups-container {
  min-height: 80vh;
}
</style>
