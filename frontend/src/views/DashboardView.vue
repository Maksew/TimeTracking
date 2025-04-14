<template>
  <div class="dashboard">
    <!-- Message de bienvenue personnalisé -->
    <WelcomeMessage :username="user ? user.pseudo : ''" />

    <!-- Date -->
    <h2 class="date-header">Aujourd'hui, {{ currentDate }}</h2>

    <!-- État de chargement -->
    <v-overlay v-if="loading" class="d-flex align-center justify-center">
      <v-progress-circular indeterminate color="primary" size="64"></v-progress-circular>
    </v-overlay>

    <!-- Message d'erreur -->
    <v-alert v-if="error && !loading" type="error" class="mb-4">
      {{ error }}
      <template v-slot:append>
        <v-btn variant="text" @click="error = null">Fermer</v-btn>
      </template>
    </v-alert>

    <!-- Contenu principal -->
    <template v-if="!loading">
      <!-- Métriques et feuilles de temps -->
      <div class="dashboard-container">
        <!-- Colonne de gauche: Métriques et statistiques -->
        <div class="dashboard-left">
          <!-- Métriques -->
          <StatsOverview ref="statsOverviewRef" />

          <!-- Statistiques détaillées -->
          <ImprovedDetailedStats ref="detailedStatsRef" class="mt-4" />
        </div>

        <!-- Colonne de droite: Feuilles de temps - MAINTENANT FIXÉE EN HAUTEUR -->
        <div class="dashboard-right">
          <TimeSheetComponent
            ref="timeSheetComponentRef"
            @task-updated="handleTaskUpdated"
            @data-changed="handleDataChanged"
          />
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useAuthStore } from '@/stores/auth';
import StatsOverview from '@/components/dashboard/StatsOverview.vue';
import ImprovedDetailedStats from '@/components/dashboard/ImprovedDetailedStats.vue';
import TimeSheetComponent from '@/components/timesheet/TimeSheetComponent.vue';
import WelcomeMessage from '@/components/dashboard/WelcomeMessage.vue';
import timeSheetService from '@/services/timeSheetService';
import groupService from '@/services/groupService';
import { useRouter, useRoute } from 'vue-router';

const router = useRouter();
const route = useRoute();

const authStore = useAuthStore();
const user = computed(() => authStore.user);

// Références vers les composants enfants pour pouvoir appeler leurs méthodes
const statsOverviewRef = ref(null);
const detailedStatsRef = ref(null);
const timeSheetComponentRef = ref(null);

// États pour le chargement
const timeSheets = ref([]);
const loading = ref(false);
const error = ref(null);

const currentDate = computed(() => {
  const days = ['Dimanche', 'Lundi', 'Mardi', 'Mercredi', 'Jeudi', 'Vendredi', 'Samedi'];
  const months = [
    'Janvier', 'Février', 'Mars', 'Avril', 'Mai', 'Juin',
    'Juillet', 'Août', 'Septembre', 'Octobre', 'Novembre', 'Décembre'
  ];

  const now = new Date();
  return `${days[now.getDay()]} ${now.getDate()} ${months[now.getMonth()]} ${now.getFullYear()}`;
});

// Méthode pour rafraîchir toutes les statistiques
const refreshAllStats = () => {
  console.log('Rafraîchissement des statistiques...');
  if (statsOverviewRef.value) {
    statsOverviewRef.value.refreshData();
  }
  if (detailedStatsRef.value) {
    detailedStatsRef.value.refreshData();
  }
};

// Méthode pour gérer le signal de mise à jour des tâches
const handleTaskUpdated = (taskData) => {
  console.log('Tâche mise à jour:', taskData);
  refreshAllStats();
};

// Méthode pour gérer le signal de changement de données
const handleDataChanged = () => {
  console.log('Données modifiées, rafraîchissement des statistiques...');
  refreshAllStats();
};

onMounted(async () => {
  try {
    loading.value = true;

    const userGroups = await groupService.getUserGroups();

    const [ownedTimeSheets, sharedWithUserTimeSheets] = await Promise.all([
      timeSheetService.getUserTimeSheets(),
      timeSheetService.getSharedTimeSheets()
    ]);

    const groupSharedTimeSheetsPromises = [];
    for (const group of userGroups) {
      groupSharedTimeSheetsPromises.push(timeSheetService.getGroupTimeSheets(group.id));
    }

    const groupSharedTimeSheetsResults = await Promise.all(groupSharedTimeSheetsPromises);
    let allTimeSheets = [...ownedTimeSheets, ...sharedWithUserTimeSheets];

    groupSharedTimeSheetsResults.forEach(groupSheets => {
      if (Array.isArray(groupSheets)) {
        allTimeSheets = [...allTimeSheets, ...groupSheets];
      }
    });

    const uniqueSheets = [...new Map(allTimeSheets.map(sheet => [sheet.id, sheet])).values()];

    timeSheets.value = uniqueSheets;

    loading.value = false;
  } catch (err) {
    console.error('Erreur lors du chargement des feuilles de temps:', err);
    error.value = 'Impossible de charger les feuilles de temps';
    loading.value = false;
  }
});
</script>

<style scoped>
.dashboard {
  background-color: #1a237e;
  min-height: calc(100vh - 64px);
  padding: 0;
  padding-top: 24px;
  display: flex;
  flex-direction: column;
}

.date-header {
  color: white;
  font-weight: 500;
  font-size: 1.25rem;
  margin: 0;
  padding: 16px 24px;
  padding-top: 0;
  margin-bottom: 16px;
}

.dashboard-container {
  display: grid;
  /* Ajuster les proportions pour mieux correspondre à la maquette */
  grid-template-columns: minmax(0, 1.7fr) minmax(0, 1.3fr);
  gap: 16px;
  padding: 0 16px 16px 16px;
  flex: 1;
  min-height: 0; /* Important pour éviter le débordement */
}

/* En dessous de 960px, passer en une seule colonne */
@media (max-width: 960px) {
  .dashboard-container {
    grid-template-columns: 1fr;
    gap: 16px;
    overflow-y: auto;
  }

  .dashboard-left, .dashboard-right {
    width: 100%;
    overflow: visible;
  }
}

.dashboard-left {
  display: flex;
  flex-direction: column;
  overflow-y: auto;
  max-height: calc(100vh - 150px); /* Ajusté pour tenir compte de l'en-tête */
}

.dashboard-right {
  display: flex;
  flex-direction: column;
  max-height: calc(100vh - 150px); /* Même hauteur que dashboard-left */
  overflow: hidden;
}

/* Personnalisation des scrollbars pour une meilleure intégration */
.dashboard-left::-webkit-scrollbar,
.dashboard-right::-webkit-scrollbar {
  width: 8px;
}

.dashboard-left::-webkit-scrollbar-track,
.dashboard-right::-webkit-scrollbar-track {
  background: rgba(0, 0, 0, 0.1);
  border-radius: 4px;
}

.dashboard-left::-webkit-scrollbar-thumb,
.dashboard-right::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.3);
  border-radius: 4px;
}

.dashboard-left::-webkit-scrollbar-thumb:hover,
.dashboard-right::-webkit-scrollbar-thumb:hover {
  background: rgba(255, 255, 255, 0.5);
}
</style>
