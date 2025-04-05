<script setup>
import { ref, watch, onMounted } from 'vue';
import statisticsService from '@/services/statisticsService';
import groupService from '@/services/groupService';

// États de l'interface
const selectedTab = ref('global');
const selectedGroup = ref(null);
const loading = ref(true);
const error = ref(null);

// Données
const groups = ref([]);
const statistics = ref(null);
const taskStats = ref([]);

// Charger les statistiques globales de l'utilisateur
const fetchGlobalStats = async () => {
  try {
    loading.value = true;
    const response = await statisticsService.getCurrentUserStatistics();
    statistics.value = response;

    if (response.categories && response.categories.length > 0) {
      // Transformer les catégories en format pour l'affichage
      taskStats.value = response.categories.map(category => ({
        category: category.category || 'Catégorie par défaut',
        tasks: category.tasks.map(task => ({
          name: task.taskName,
          time: `${task.totalTimeInMinutes} min`,
          completion: task.percentageOfTotal || 0,
          icon: task.icon || 'mdi-clipboard-check-outline'
        }))
      }));
    } else {
      // Données par défaut si aucune catégorie n'existe
      taskStats.value = [{
        category: 'Aucune catégorie',
        tasks: [{
          name: 'Aucune tâche disponible',
          time: '0 min',
          completion: 0,
          icon: 'mdi-information-outline'
        }]
      }];
    }
  } catch (err) {
    console.error('Erreur lors du chargement des statistiques globales:', err);
    error.value = 'Impossible de charger les statistiques';
  } finally {
    loading.value = false;
  }
};

// Charger les statistiques détaillées (par groupe)
const fetchDetailedStats = async () => {
  try {
    loading.value = true;

    if (selectedGroup.value) {
      // Si un groupe est sélectionné, charger les stats de ce groupe
      const response = await statisticsService.getGroupStatistics(selectedGroup.value);

      // Transformation des données pour l'affichage
      // Note: La structure exacte dépend de la réponse API
      const categories = [];
      Object.entries(response).forEach(([userId, userStats]) => {
        categories.push({
          category: userStats.userName || `Utilisateur #${userId}`,
          tasks: [
            {
              name: 'Total tâches',
              time: `${userStats.totalTasks} tâches`,
              completion: userStats.completionRate || 0,
              icon: 'mdi-clipboard-list-outline'
            },
            {
              name: 'Temps total',
              time: `${userStats.totalTimeInMinutes} min`,
              completion: 100,
              icon: 'mdi-clock-outline'
            }
          ]
        });
      });

      taskStats.value = categories;
    } else {
      // Sinon, charger les statistiques de l'utilisateur
      await fetchGlobalStats();
    }
  } catch (err) {
    console.error('Erreur lors du chargement des statistiques détaillées:', err);
    error.value = 'Impossible de charger les statistiques';
  } finally {
    loading.value = false;
  }
};

// Charger les groupes pour le sélecteur
const loadGroups = async () => {
  try {
    const userGroups = await groupService.getUserGroups();
    groups.value = [
      { title: 'Tous', value: null },
      ...userGroups.map(group => ({
        title: group.name,
        value: group.id
      }))
    ];
  } catch (err) {
    console.error('Erreur lors du chargement des groupes:', err);
  }
};

// Charger les données en fonction de l'onglet et du groupe sélectionnés
const loadStats = () => {
  if (selectedTab.value === 'global') {
    fetchGlobalStats();
  } else if (selectedTab.value === 'detailed') {
    fetchDetailedStats();
  }
};

// Observer les changements pour rafraîchir les données
watch(selectedTab, loadStats);
watch(selectedGroup, () => {
  if (selectedTab.value === 'detailed') {
    loadStats();
  }
});

// Initialisation
onMounted(() => {
  loadGroups();
  loadStats();
});
</script>

<template>
  <v-card color="#283593" dark class="rounded-lg">
    <v-card-title class="d-flex align-center">
      <v-icon size="small" class="mr-2">mdi-chart-bar</v-icon>
      Statistique
      <v-spacer></v-spacer>
      <v-btn variant="text" icon="mdi-download" class="ml-2">
        <v-tooltip activator="parent" location="top">Exporter</v-tooltip>
      </v-btn>
    </v-card-title>

    <v-divider></v-divider>

    <v-card-text>
      <div class="d-flex align-center">
        <div class="tabs-container">
          <v-btn
            variant="text"
            :color="selectedTab === 'global' ? 'white' : 'grey'"
            @click="selectedTab = 'global'"
          >
            Global
          </v-btn>
          <v-btn
            variant="text"
            :color="selectedTab === 'detailed' ? 'white' : 'grey'"
            @click="selectedTab = 'detailed'"
          >
            Détaillé
          </v-btn>
        </div>

        <v-spacer></v-spacer>

        <v-select
          v-model="selectedGroup"
          :items="groups"
          item-title="title"
          item-value="value"
          variant="outlined"
          density="compact"
          label="Groupe"
          bg-color="#1a237e"
          hide-details
          class="max-width-180"
        ></v-select>
      </div>

      <!-- Indicateur de chargement -->
      <div v-if="loading" class="d-flex justify-center align-center my-8">
        <v-progress-circular indeterminate color="white"></v-progress-circular>
      </div>

      <!-- Message d'erreur -->
      <div v-else-if="error" class="my-4">
        <v-alert type="error" text dense>{{ error }}</v-alert>
      </div>

      <!-- Affichage des statistiques -->
      <div v-else class="mt-4">
        <template v-if="taskStats && taskStats.length > 0">
          <div v-for="(category, index) in taskStats" :key="index" class="mb-4">
            <div class="text-subtitle-1 mb-2">{{ category.category }}</div>

            <div v-for="(task, taskIndex) in category.tasks" :key="taskIndex" class="mb-3">
              <div class="d-flex align-center mb-1">
                <v-icon size="small" class="mr-2">{{ task.icon }}</v-icon>
                <span>{{ task.name }}</span>
                <v-spacer></v-spacer>
                <span>{{ task.time }}</span>
              </div>

              <v-progress-linear
                :model-value="task.completion"
                height="10"
                rounded
                :color="task.completion >= 100 ? 'green' : 'purple'"
              >
                <template #default>
                  <span class="text-caption">{{ Math.round(task.completion) }}%</span>
                </template>
              </v-progress-linear>

              <div class="d-flex justify-end mt-1">
                <v-btn size="x-small" variant="text" color="purple-lighten-3">
                  Détail
                </v-btn>
              </div>
            </div>
          </div>
        </template>

        <template v-else>
          <div class="text-center py-8">
            <v-icon size="large" class="mb-2">mdi-chart-bar</v-icon>
            <p>Aucune donnée statistique disponible</p>
          </div>
        </template>
      </div>
    </v-card-text>
  </v-card>
</template>

<style scoped>
.max-width-180 {
  max-width: 180px;
}

.tabs-container {
  border-bottom: 1px solid rgba(255, 255, 255, 0.12);
}
</style>
