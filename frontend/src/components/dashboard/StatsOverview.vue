<script setup>
import { ref, onMounted, computed } from 'vue';
import statisticsService from '@/services/statisticsService';
import timeSheetService from '@/services/timeSheetService';

// États pour le chargement
const loading = ref(true);
const error = ref(null);

// Statistiques
const statistics = ref(null);
const timeSheets = ref([]);

// Compteurs calculés
const tasksToDo = computed(() => {
  if (!statistics.value) return 0;
  return statistics.value.summary ?
    (statistics.value.summary.totalTasks - statistics.value.summary.completedTasks) : 0;
});

const tasksCompleted = computed(() => {
  if (!statistics.value) return 0;
  return statistics.value.summary ? statistics.value.summary.completedTasks : 0;
});

// Formatage du temps total
const formatTimeWorked = (minutes) => {
  if (!minutes) return '00:00:00';

  const hours = Math.floor(minutes / 60);
  const mins = minutes % 60;
  // Ajout des secondes (même si elles sont à 00 pour l'instant)
  return `${String(hours).padStart(2, '0')}:${String(mins).padStart(2, '0')}:00`;
};

const timeWorked = computed(() => {
  if (!statistics.value) return '00:00:00';
  return formatTimeWorked(statistics.value.summary ? statistics.value.summary.totalTimeInMinutes : 0);
});

// Chargement des données au montage du composant
onMounted(async () => {
  try {
    // Charger les statistiques de l'utilisateur connecté
    const statsData = await statisticsService.getCurrentUserStatistics();
    statistics.value = statsData;

    // Charger les feuilles de temps pour avoir des données supplémentaires si nécessaire
    const timeSheetsData = await timeSheetService.getUserTimeSheets();
    timeSheets.value = timeSheetsData;
  } catch (err) {
    console.error('Erreur lors du chargement des statistiques:', err);
    error.value = err.message || 'Erreur lors du chargement des données';
  } finally {
    loading.value = false;
  }
});
</script>

<template>
  <!-- Indicateur de chargement -->
  <div v-if="loading" class="d-flex justify-center align-center mt-3">
    <v-progress-circular indeterminate color="primary"></v-progress-circular>
  </div>

  <!-- Message d'erreur si applicable -->
  <div v-else-if="error" class="mt-3">
    <v-alert type="error" text dense>{{ error }}</v-alert>
  </div>

  <!-- Cartes de statistiques -->
  <div v-else class="stats-cards">
    <!-- Tâches à effectuer -->
    <v-card color="#283593" dark class="metric-card">
      <div class="d-flex align-center">
        <div class="metric-icon">
          <v-icon>mdi-clipboard-text-outline</v-icon>
        </div>
        <div class="metric-content">
          <div class="metric-value">{{ tasksToDo }}</div>
          <div class="metric-label">Tâches à effectuer</div>
        </div>
      </div>
    </v-card>

    <!-- Tâches effectuées -->
    <v-card color="#283593" dark class="metric-card">
      <div class="d-flex align-center">
        <div class="metric-icon">
          <v-icon>mdi-check-circle-outline</v-icon>
        </div>
        <div class="metric-content">
          <div class="metric-value">{{ tasksCompleted }}</div>
          <div class="metric-label">Tâches effectuées</div>
        </div>
      </div>
    </v-card>

    <!-- Temps travaillé -->
    <v-card color="#283593" dark class="metric-card">
      <div class="d-flex align-center">
        <div class="metric-icon">
          <v-icon>mdi-clock-outline</v-icon>
        </div>
        <div class="metric-content">
          <div class="metric-value">{{ timeWorked }}</div>
          <div class="metric-label">Travaillé aujourd'hui</div>
        </div>
      </div>
    </v-card>
  </div>
</template>

<style scoped>
.stats-cards {
  display: flex;
  flex-direction: row;
  gap: 12px;
  width: 100%;
}

.metric-card {
  flex: 1;
  padding: 16px;
  border-radius: 12px;
  transition: transform 0.2s;
}

.metric-card:hover {
  transform: translateY(-4px);
}

.metric-icon {
  background-color: #1a237e;
  border-radius: 50%;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
}

.metric-content {
  display: flex;
  flex-direction: column;
}

.metric-value {
  font-size: 1.25rem;
  font-weight: bold;
  line-height: 1.2;
}

.metric-label {
  font-size: 0.75rem;
  opacity: 0.8;
}

/* Pour les écrans très petits, passer en colonne */
@media (max-width: 500px) {
  .stats-cards {
    flex-direction: column;
  }
}
</style>
