<script setup>
import { ref, onMounted, computed } from 'vue';
import { useAuthStore } from '@/stores/auth';
import StatsOverview from '@/components/dashboard/StatsOverview.vue';
import DetailedStats from '@/components/dashboard/DetailedStats.vue';
import TimeSheetComponent from '@/components/timesheet/TimeSheetComponent.vue';
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
const loading = ref(false);
const error = ref(null);

// Date actuelle formatée
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

onMounted(() => {
  if (route.query.force_refresh === 'true') {
    console.log('Rafraîchissement forcé demandé après édition de feuille de temps');
    // Supprimer le paramètre de l'URL sans recharger la page
    router.replace({ query: {} });
    // Attendre un peu pour s'assurer que les composants sont montés
    setTimeout(() => {
      refreshAllStats();
      if (timeSheetComponentRef.value) {
        timeSheetComponentRef.value.refreshData();
      }
    }, 200);
  }
});

</script>

<template>
  <div class="dashboard">
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
          <DetailedStats ref="detailedStatsRef" class="mt-4" />
        </div>

        <!-- Colonne de droite: Feuilles de temps -->
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

<style scoped>
.dashboard {
  background-color: #1a237e;
  min-height: calc(100vh - 64px); /* Hauteur totale moins la barre de navigation */
  padding: 0;
  /* Ajouter un padding-top pour créer de l'espace sous la barre de navigation */
  padding-top: 24px;
}

.date-header {
  color: white;
  font-weight: 500;
  font-size: 1.25rem;
  margin: 0;
  padding: 16px 24px;
  /* Ajuster le padding du header de date */
  padding-top: 0;
  margin-bottom: 16px;
}

.dashboard-container {
  display: grid;
  grid-template-columns: minmax(0, 2fr) minmax(0, 1.5fr);
  gap: 16px;
  padding: 0 16px 16px 16px;
  height: calc(100vh - 180px); /* Ajuster cette valeur pour laisser plus d'espace */
  /* Ajouter une marge en haut pour séparer du header de date */
  margin-top: 16px;
}

/* En dessous de 960px, passer en une seule colonne */
@media (max-width: 960px) {
  .dashboard-container {
    grid-template-columns: 1fr;
    gap: 16px;
    height: auto;
  }

  .dashboard-left, .dashboard-right {
    width: 100%;
  }
}

.dashboard-left {
  display: flex;
  flex-direction: column;
}

.dashboard-right {
  display: flex;
  flex-direction: column;
}
</style>
