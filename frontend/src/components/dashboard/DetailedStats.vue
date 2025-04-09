<script setup>
import { ref, watch, onMounted, computed, onUnmounted } from 'vue';
import statisticsService from '@/services/statisticsService';
import timeSheetService from '@/services/timeSheetService';
import taskService from '@/services/taskService';
import groupService from '@/services/groupService';

// États de l'interface
const selectedTab = ref('global');
const selectedGroup = ref(null);
const loading = ref(true);
const error = ref(null);
const expandedPanels = ref([]);  // Pour contrôler les panneaux d'expansion
const refreshInterval = ref(null);

// Données
const groups = ref([]);
const statistics = ref(null);
const organizedStats = ref([]);
const timeSheets = ref([]);

// Fonction pour formater le temps correctement (en considérant que c'est des secondes)
const formatTime = (timeValue) => {
  if (timeValue === null || timeValue === undefined) return '00:00:00';

  // La valeur est toujours en secondes
  const totalSeconds = timeValue;

  const hours = Math.floor(totalSeconds / 3600);
  const mins = Math.floor((totalSeconds % 3600) / 60);
  const secs = totalSeconds % 60;

  return `${String(hours).padStart(2, '0')}:${String(mins).padStart(2, '0')}:${String(secs).padStart(2, '0')}`;
};

// Charger les statistiques globales de l'utilisateur
const fetchGlobalStats = async (silent = false) => {
  try {
    if (!silent) loading.value = true;

    // Charger parallèlement les statistiques et les feuilles de temps
    const [response, allTimeSheets, allTasks] = await Promise.all([
      statisticsService.getCurrentUserStatistics(),
      timeSheetService.getUserTimeSheets(),
      taskService.getAllTasks()
    ]);

    statistics.value = response;
    timeSheets.value = allTimeSheets;

    // Créer un map des tâches par ID
    const tasksMap = {};
    allTasks.forEach(task => {
      tasksMap[task.id] = task;
    });

    // Regrouper les statistiques par feuille de temps
    if (response.categories && response.categories.length > 0) {
      // Structure pour regrouper par feuille de temps
      const timeSheetGroups = {};

      // Associer chaque tâche à sa feuille de temps
      allTimeSheets.forEach(ts => {
        if (ts.timeSheetTasks && ts.timeSheetTasks.length > 0) {
          const title = ts.title || `Feuille du ${new Date(ts.entryDate).toLocaleDateString()}`;

          // Initialiser le groupe de cette feuille si nécessaire
          if (!timeSheetGroups[ts.id]) {
            timeSheetGroups[ts.id] = {
              id: ts.id,
              title: title,
              icon: ts.icon || 'mdi-file-document-outline',
              tasks: []
            };
          }

          // Ajouter chaque tâche au groupe
          ts.timeSheetTasks.forEach(tst => {
            // Rechercher dans les catégories pour trouver les stats de cette tâche
            response.categories.forEach(category => {
              const matchingTask = category.tasks.find(t => t.taskId === tst.taskId);
              if (matchingTask) {
                let taskName = matchingTask.taskName;
                // Utiliser le nom complet de la tâche depuis le tasksMap si disponible
                if (tasksMap[tst.taskId]) {
                  taskName = tasksMap[tst.taskId].name;
                }

                timeSheetGroups[ts.id].tasks.push({
                  id: matchingTask.taskId,
                  name: taskName,
                  time: `${matchingTask.totalTimeInMinutes} min`,
                  value: matchingTask.totalTimeInMinutes,
                  completion: matchingTask.percentageOfTotal || 0,
                  color: matchingTask.totalTimeInMinutes > 0 ? 'green' : 'purple', // Utiliser seulement la durée
                  icon: matchingTask.icon || 'mdi-clipboard-check-outline',
                  completed: matchingTask.totalTimeInMinutes > 0 // Considérer comme complété si durée > 0
                });
              }
            });
          });
        }
      });

      // Convertir l'objet en tableau et trier par titre
      organizedStats.value = Object.values(timeSheetGroups).sort((a, b) =>
        a.title.localeCompare(b.title)
      );

      // Si aucune feuille n'est trouvée, utiliser une structure adaptée
      if (organizedStats.value.length === 0) {
        organizedStats.value = [{
          id: 'default',
          title: 'Aucune feuille de temps',
          icon: 'mdi-file-document-outline',
          tasks: []
        }];
      }
    } else {
      // Données par défaut si aucune catégorie n'existe
      organizedStats.value = [{
        id: 'default',
        title: 'Aucune donnée',
        icon: 'mdi-file-document-outline',
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
    if (!silent) error.value = 'Impossible de charger les statistiques';
  } finally {
    if (!silent) loading.value = false;
  }
};

// Charger les statistiques détaillées (par groupe)
const fetchDetailedStats = async (silent = false) => {
  try {
    if (!silent) loading.value = true;

    if (selectedGroup.value) {
      // Si un groupe est sélectionné, charger les stats de ce groupe
      const response = await statisticsService.getGroupStatistics(selectedGroup.value);

      // Transformation des données pour l'affichage
      const categories = [];
      Object.entries(response).forEach(([userId, userStats]) => {
        categories.push({
          id: `user-${userId}`,
          title: userStats.userName || `Utilisateur #${userId}`,
          icon: 'mdi-account',
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

      organizedStats.value = categories;
    } else {
      // Sinon, charger les statistiques de l'utilisateur
      await fetchGlobalStats(silent);
    }
  } catch (err) {
    console.error('Erreur lors du chargement des statistiques détaillées:', err);
    if (!silent) error.value = 'Impossible de charger les statistiques';
  } finally {
    if (!silent) loading.value = false;
  }
};

// Fonction centralisée pour charger les données
const refreshData = async (silent = false) => {
  // Si mode silencieux, ne pas afficher le loader
  if (!silent) loading.value = true;

  try {
    if (selectedTab.value === 'global') {
      await fetchGlobalStats(silent);
    } else if (selectedTab.value === 'detailed') {
      await fetchDetailedStats(silent);
    }
  } catch (err) {
    console.error('Erreur lors du rafraîchissement des statistiques:', err);
    if (!silent) error.value = 'Impossible de charger les statistiques';
  } finally {
    if (!silent) loading.value = false;
  }
};

// Charger les groupes pour le sélecteur
const loadGroups = async () => {
  try {
    const userGroups = await groupService.getUserGroups();
    groups.value = [
      { title: 'Tous', value: null },
      { title: 'Personnel', value: 'personnel' },
      ...userGroups.map(group => ({
        title: group.name,
        value: group.id
      }))
    ];
  } catch (err) {
    console.error('Erreur lors du chargement des groupes:', err);
  }
};

// Exposer la méthode de rafraîchissement pour l'appel externe
defineExpose({
  refreshData
});

// Charger les données en fonction de l'onglet et du groupe sélectionnés
const loadStats = () => {
  refreshData();
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

  // Rafraîchissement périodique des données toutes les 30 secondes
  refreshInterval.value = setInterval(() => {
    refreshData(true); // Mode silencieux pour ne pas perturber l'interface
  }, 30000);

  // Par défaut, ouvrir le premier panneau
  expandedPanels.value = [0];
});

// Nettoyage à la destruction du composant
onUnmounted(() => {
  if (refreshInterval.value) {
    clearInterval(refreshInterval.value);
    refreshInterval.value = null;
  }
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
        <template v-if="organizedStats && organizedStats.length > 0">
          <!-- Utilisation de v-expansion-panels pour organiser les statistiques comme une feuille de temps -->
          <v-expansion-panels v-model="expandedPanels" multiple>
            <v-expansion-panel
              v-for="(timeSheet, index) in organizedStats"
              :key="timeSheet.id || index"
              :value="index"
              bg-color="#1a237e"
            >
              <v-expansion-panel-title>
                <div class="d-flex align-center">
                  <v-icon class="mr-2">{{ timeSheet.icon }}</v-icon>
                  <span>{{ timeSheet.title }}</span>
                  <v-chip class="ml-2" size="small" color="primary">
                    {{ timeSheet.tasks.length }} tâches
                  </v-chip>
                </div>
              </v-expansion-panel-title>

              <v-expansion-panel-text>
                <v-list bg-color="transparent">
                  <v-list-item
                    v-for="(task, taskIndex) in timeSheet.tasks"
                    :key="task.id || taskIndex"
                    class="mb-3"
                  >
                    <template v-slot:prepend>
                      <v-icon size="small" class="mr-2">{{ task.icon }}</v-icon>
                    </template>

                    <v-list-item-title>{{ task.name }}</v-list-item-title>

                    <template v-slot:append>
                      <span>{{ formatTime(task.value) }}</span>
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
