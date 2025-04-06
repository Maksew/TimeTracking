<script setup>
import { ref, watch, onMounted, computed } from 'vue';
import statisticsService from '@/services/statisticsService';
import groupService from '@/services/groupService';

// États de l'interface
const selectedTab = ref('global');
const selectedGroup = ref(null);
const loading = ref(true);
const error = ref(null);
const expandedPanels = ref([]);  // Pour contrôler les panneaux d'expansion

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
      // Garder les catégories telles quelles, sans transformation
      taskStats.value = response.categories.map(category => ({
        id: category.category,  // Identifiant unique pour le panneau
        title: category.category || 'Catégorie par défaut',
        tasks: category.tasks.map(task => ({
          id: task.taskId,
          name: task.taskName,
          time: `${task.totalTimeInMinutes} min`,
          value: task.totalTimeInMinutes,
          completion: task.percentageOfTotal || 0,
          color: task.completion >= 100 ? 'green' : 'purple',
          icon: task.icon || 'mdi-clipboard-check-outline'
        }))
      }));
    } else {
      // Données par défaut si aucune catégorie n'existe
      taskStats.value = [{
        id: 'default',
        title: 'Aucune catégorie',
        tasks: [{
          id: 'no-task',
          name: 'Aucune tâche disponible',
          time: '0 min',
          value: 0,
          completion: 0,
          color: 'grey',
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
      const categories = [];
      Object.entries(response).forEach(([userId, userStats]) => {
        categories.push({
          id: `user-${userId}`,
          title: userStats.userName || `Utilisateur #${userId}`,
          tasks: [
            {
              id: `tasks-${userId}`,
              name: 'Total tâches',
              time: `${userStats.totalTasks} tâches`,
              value: userStats.totalTasks,
              completion: userStats.completionRate || 0,
              color: 'purple',
              icon: 'mdi-clipboard-list-outline'
            },
            {
              id: `time-${userId}`,
              name: 'Temps total',
              time: `${userStats.totalTimeInMinutes} min`,
              value: userStats.totalTimeInMinutes,
              completion: 100,
              color: 'blue',
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
  // Par défaut, ouvrir le premier panneau
  expandedPanels.value = [0];
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
          <!-- Utilisation de v-expansion-panels pour organiser les statistiques comme une feuille de temps -->
          <v-expansion-panels v-model="expandedPanels" multiple>
            <v-expansion-panel
              v-for="(category, index) in taskStats"
              :key="category.id || index"
              :value="index"
              bg-color="#1a237e"
            >
              <v-expansion-panel-title>
                <div class="d-flex align-center">
                  <v-icon class="mr-2">mdi-folder-outline</v-icon>
                  <span>{{ category.title }}</span>
                  <v-chip class="ml-2" size="small" color="primary">
                    {{ category.tasks.length }} tâches
                  </v-chip>
                </div>
              </v-expansion-panel-title>

              <v-expansion-panel-text>
                <v-list bg-color="transparent">
                  <v-list-item
                    v-for="(task, taskIndex) in category.tasks"
                    :key="task.id || taskIndex"
                    class="mb-3"
                  >
                    <template v-slot:prepend>
                      <v-icon size="small" class="mr-2">{{ task.icon }}</v-icon>
                    </template>

                    <v-list-item-title>{{ task.name }}</v-list-item-title>

                    <template v-slot:append>
                      <span>{{ task.time }}</span>
                    </template>

                    <v-list-item-subtitle class="mt-2">
                      <v-progress-linear
                        :model-value="task.completion"
                        height="10"
                        rounded
                        :color="task.color"
                      >
                        <template #default>
                          <span class="text-caption">{{ Math.round(task.completion) }}%</span>
                        </template>
                      </v-progress-linear>
                    </v-list-item-subtitle>
                  </v-list-item>
                </v-list>
              </v-expansion-panel-text>
            </v-expansion-panel>
          </v-expansion-panels>
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

:deep(.v-expansion-panel-text__wrapper) {
  padding: 0;
}

.task-list-item {
  padding: 8px 12px;
  margin-bottom: 8px;
  border-radius: 4px;
  transition: background-color 0.2s;
}

.task-list-item:hover {
  background-color: rgba(255, 255, 255, 0.05);
}
</style>
