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
      <!-- Onglets et filtres -->
      <div class="d-flex align-center mb-4">
        <div class="tabs-container">
          <v-btn
            variant="text"
            :color="selectedTab === 'global' ? 'white' : 'grey'"
            @click="selectedTab = 'global'"
          >
            GLOBAL
          </v-btn>
          <v-btn
            variant="text"
            :color="selectedTab === 'detailed' ? 'white' : 'grey'"
            @click="selectedTab = 'detailed'"
          >
            DÉTAILLÉ
          </v-btn>
        </div>

        <v-spacer></v-spacer>

        <div class="filter-group">
          <v-select
            v-model="selectedGroup"
            :items="groups"
            item-title="title"
            item-value="value"
            variant="outlined"
            density="compact"
            label="Groupe"
            hide-details
            bg-color="#1a237e"
            class="max-width-180"
          ></v-select>
        </div>
      </div>

      <!-- Indicateur de chargement -->
      <div v-if="loading" class="d-flex justify-center align-center my-8">
        <v-progress-circular indeterminate color="white"></v-progress-circular>
      </div>

      <!-- Message d'erreur -->
      <div v-else-if="error" class="my-4">
        <v-alert type="error" text dense>{{ error }}</v-alert>
      </div>

      <!-- ONGLET GLOBAL -->
      <div v-else-if="selectedTab === 'global'" class="mt-4">
        <v-row>
          <v-col cols="12" sm="6" md="4">
            <v-card color="#1a237e" class="rounded-lg">
              <v-card-text class="text-center">
                <div class="text-h4 font-weight-bold">{{ totalTasks || 0 }}</div>
                <div class="text-subtitle-2">Tâches totales</div>
              </v-card-text>
            </v-card>
          </v-col>
          <v-col cols="12" sm="6" md="4">
            <v-card color="#1a237e" class="rounded-lg">
              <v-card-text class="text-center">
                <div class="text-h4 font-weight-bold">{{ completedTasks || 0 }}</div>
                <div class="text-subtitle-2">Tâches complétées</div>
              </v-card-text>
            </v-card>
          </v-col>
          <v-col cols="12" sm="6" md="4">
            <v-card color="#1a237e" class="rounded-lg">
              <v-card-text class="text-center">
                <div class="text-h4 font-weight-bold">{{ formatTime(totalTime) }}</div>
                <div class="text-subtitle-2">Temps total</div>
              </v-card-text>
            </v-card>
          </v-col>
        </v-row>

        <!-- Statistiques par catégorie -->
        <div class="mt-6">
          <h3 class="text-subtitle-1 font-weight-medium mb-3">Répartition par catégorie</h3>
          <v-sheet color="#1a237e" class="rounded-lg pa-4">
            <div v-for="(cat, index) in categories" :key="index" class="mb-3">
              <div class="d-flex justify-space-between mb-1">
                <span class="font-weight-medium">{{ cat.name }}</span>
                <span>{{ formatTime(cat.time) }}</span>
              </div>
              <v-progress-linear
                :model-value="cat.percentage"
                height="8"
                rounded
                :color="getCategoryColor(index)"
              ></v-progress-linear>
            </div>
          </v-sheet>
        </div>
      </div>

      <!-- ONGLET DÉTAILLÉ - Vue simple par liste comme dans votre image -->
      <div v-else-if="selectedTab === 'detailed'" class="mt-2">
        <!-- Option pour enchaîner les tâches -->
        <div class="mb-3">
          <v-checkbox
            v-model="autoChainTasks"
            label="Enchaîner automatiquement les tâches"
            density="compact"
            hide-details
          ></v-checkbox>
        </div>

        <!-- Toutes les feuilles - info -->
        <div class="d-flex align-center mb-2">
          <span class="text-body-2">Toutes les feuilles - {{ timeSheets.length }} feuilles</span>
        </div>

        <!-- Liste des statistiques par groupe (style similaire à l'image) -->
        <!-- Section: Feuilles Personnelles -->
        <div v-if="personalStats.length > 0" class="stats-group mb-4">
          <!-- Titre du groupe avec bouton de détail -->
          <div class="group-header d-flex align-center px-3 py-2 mb-2">
            <v-icon size="small" color="white" class="mr-2">mdi-account</v-icon>
            <span class="font-weight-medium">Feuilles Personnelles</span>
            <v-spacer></v-spacer>
            <v-btn
              variant="text"
              size="small"
              color="white"
              @click="showGroupDetails('personal')"
            >
              Détail
            </v-btn>
          </div>

          <!-- Liste des feuilles personnelles -->
          <div
            v-for="item in personalStats"
            :key="'personal-'+item.id"
            class="stat-item d-flex align-center px-3 py-2 mb-1"
          >
            <v-icon size="small" class="mr-2">{{ item.icon }}</v-icon>
            <span>{{ item.title }}</span>
            <span class="ml-2 text-caption">{{ item.tasks.length }} tâches</span>

            <v-chip
              size="small"
              color="purple"
              class="ml-2"
            >
              Personnel
            </v-chip>

            <v-spacer></v-spacer>

            <v-btn
              variant="text"
              size="small"
              color="white"
              @click="showTimeSheetDetails(item)"
            >
              Détail
            </v-btn>
          </div>
        </div>

        <!-- Autres groupes -->
        <div
          v-for="group in groupedStats"
          :key="`group-${group.groupId}`"
          class="stats-group mb-4"
        >
          <!-- Titre du groupe avec bouton de détail -->
          <div
            class="group-header d-flex align-center px-3 py-2 mb-2"
            :style="`background-color: ${getGroupColor(group.groupId)}`"
          >
            <v-icon size="small" color="white" class="mr-2">mdi-account-group</v-icon>
            <span class="font-weight-medium">{{ group.name }}</span>
            <v-spacer></v-spacer>
            <v-btn
              variant="text"
              size="small"
              color="white"
              @click="showGroupDetails(group.groupId)"
            >
              Détail
            </v-btn>
          </div>

          <!-- Liste des feuilles du groupe -->
          <div
            v-for="item in group.items"
            :key="`${group.groupId}-${item.id}`"
            class="stat-item d-flex align-center px-3 py-2 mb-1"
          >
            <v-icon size="small" class="mr-2">{{ item.icon }}</v-icon>
            <span>{{ item.title }}</span>
            <span class="ml-2 text-caption">{{ item.tasks.length }} tâches</span>

            <v-chip
              size="small"
              :color="getGroupColor(group.groupId)"
              class="ml-2"
            >
              {{ group.name }}
            </v-chip>

            <v-spacer></v-spacer>

            <v-btn
              variant="text"
              size="small"
              color="white"
              @click="showTimeSheetDetails(item)"
            >
              Détail
            </v-btn>
          </div>
        </div>

        <!-- Message si aucune donnée -->
        <div v-if="timeSheets.length === 0" class="text-center py-8">
          <v-icon size="large" class="mb-2">mdi-chart-bar</v-icon>
          <p>Aucune donnée statistique disponible</p>
        </div>
      </div>
    </v-card-text>

    <!-- Dialog pour les détails d'une feuille de temps -->
    <v-dialog
      v-model="timeSheetDetailDialog"
      max-width="800"
    >
      <v-card v-if="selectedTimeSheet" color="#283593" dark>
        <v-card-title class="d-flex align-center">
          <v-icon :icon="selectedTimeSheet.icon" class="mr-2"></v-icon>
          {{ selectedTimeSheet.title }}
          <v-chip
            size="small"
            :color="selectedTimeSheet.groupId ? getGroupColor(selectedTimeSheet.groupId) : 'purple'"
            class="ml-2"
          >
            {{ selectedTimeSheet.groupName || 'Personnel' }}
          </v-chip>
          <v-spacer></v-spacer>
          <v-btn icon @click="timeSheetDetailDialog = false">
            <v-icon>mdi-close</v-icon>
          </v-btn>
        </v-card-title>

        <v-divider></v-divider>

        <v-card-text class="pa-4">
          <!-- Statistiques détaillées avec graphiques -->
          <div class="mb-4">
            <h3 class="text-h6 mb-3">Résumé des tâches</h3>

            <v-row>
              <v-col cols="12" sm="4">
                <v-card color="#1a237e" class="rounded-lg text-center pa-3">
                  <div class="text-h5 font-weight-bold">{{ selectedTimeSheet.tasks.length }}</div>
                  <div class="text-caption">Tâches totales</div>
                </v-card>
              </v-col>
              <v-col cols="12" sm="4">
                <v-card color="#1a237e" class="rounded-lg text-center pa-3">
                  <div class="text-h5 font-weight-bold">
                    {{ selectedTimeSheet.tasks.filter(t => t.completed).length }}
                  </div>
                  <div class="text-caption">Tâches complétées</div>
                </v-card>
              </v-col>
              <v-col cols="12" sm="4">
                <v-card color="#1a237e" class="rounded-lg text-center pa-3">
                  <div class="text-h5 font-weight-bold">
                    {{ formatTime(getTotalTimeForSheet(selectedTimeSheet)) }}
                  </div>
                  <div class="text-caption">Temps total</div>
                </v-card>
              </v-col>
            </v-row>
          </div>

          <!-- Graphique circulaire -->
          <div class="mb-4">
            <h3 class="text-h6 mb-3">Répartition du temps par tâche</h3>
            <div class="chart-container pa-4 rounded-lg" style="background-color: #1a237e; min-height: 250px;">
              <!-- Visualisation du graphique (à implémenter avec une librairie) -->
              <div class="text-center py-4">
                <p>Graphique de répartition du temps</p>
                <!-- Ici, vous intégreriez une bibliothèque comme Chart.js ou D3.js -->
              </div>
            </div>
          </div>

          <!-- Liste des tâches avec temps -->
          <div>
            <h3 class="text-h6 mb-3">Détail des tâches</h3>
            <v-list bg-color="#1a237e" class="rounded-lg">
              <v-list-item
                v-for="task in selectedTimeSheet.tasks"
                :key="task.id"
                :class="{ 'completed-task': task.completed }"
              >
                <template v-slot:prepend>
                  <v-icon :color="task.completed ? 'success' : 'grey'">
                    {{ task.completed ? 'mdi-checkbox-marked-circle' : 'mdi-checkbox-blank-circle-outline' }}
                  </v-icon>
                </template>

                <v-list-item-title>{{ task.name }}</v-list-item-title>

                <template v-slot:append>
                  <span class="task-time">{{ formatTime(task.value) }}</span>
                </template>
              </v-list-item>
            </v-list>
          </div>
        </v-card-text>
      </v-card>
    </v-dialog>

    <!-- Dialog pour les détails d'un groupe -->
    <v-dialog
      v-model="groupDetailDialog"
      max-width="800"
    >
      <v-card v-if="selectedGroupId !== null" color="#283593" dark>
        <v-card-title class="d-flex align-center">
          <v-icon class="mr-2">
            {{ selectedGroupId === 'personal' ? 'mdi-account' : 'mdi-account-group' }}
          </v-icon>
          {{ getGroupName(selectedGroupId) }}
          <v-spacer></v-spacer>
          <v-btn icon @click="groupDetailDialog = false">
            <v-icon>mdi-close</v-icon>
          </v-btn>
        </v-card-title>

        <v-divider></v-divider>

        <v-card-text class="pa-4">
          <!-- Statistiques agrégées du groupe -->
          <div class="mb-4">
            <h3 class="text-h6 mb-3">Résumé du groupe</h3>

            <v-row>
              <v-col cols="12" sm="4">
                <v-card color="#1a237e" class="rounded-lg text-center pa-3">
                  <div class="text-h5 font-weight-bold">{{ getGroupTotalTasks() }}</div>
                  <div class="text-caption">Tâches totales</div>
                </v-card>
              </v-col>
              <v-col cols="12" sm="4">
                <v-card color="#1a237e" class="rounded-lg text-center pa-3">
                  <div class="text-h5 font-weight-bold">{{ getGroupCompletedTasks() }}</div>
                  <div class="text-caption">Tâches complétées</div>
                </v-card>
              </v-col>
              <v-col cols="12" sm="4">
                <v-card color="#1a237e" class="rounded-lg text-center pa-3">
                  <div class="text-h5 font-weight-bold">{{ formatTime(getGroupTotalTime()) }}</div>
                  <div class="text-caption">Temps total</div>
                </v-card>
              </v-col>
            </v-row>
          </div>

          <!-- Graphique d'évolution dans le temps -->
          <div class="mb-4">
            <h3 class="text-h6 mb-3">Évolution du temps par jour</h3>
            <div class="chart-container pa-4 rounded-lg" style="background-color: #1a237e; min-height: 250px;">
              <!-- Visualisation du graphique (à implémenter avec une librairie) -->
              <div class="text-center py-4">
                <p>Graphique d'évolution du temps</p>
                <!-- Ici, vous intégreriez une bibliothèque comme Chart.js ou D3.js -->
              </div>
            </div>
          </div>

          <!-- Tableau des feuilles de temps du groupe -->
          <div>
            <h3 class="text-h6 mb-3">Feuilles de temps du groupe</h3>
            <v-table class="rounded-lg">
              <thead>
              <tr>
                <th>Feuille</th>
                <th>Tâches</th>
                <th>Complétées</th>
                <th>Temps total</th>
              </tr>
              </thead>
              <tbody>
              <tr
                v-for="item in getGroupTimeSheets()"
                :key="item.id"
              >
                <td>{{ item.title }}</td>
                <td>{{ item.tasks.length }}</td>
                <td>{{ item.tasks.filter(t => t.completed).length }}</td>
                <td>{{ formatTime(getTotalTimeForSheet(item)) }}</td>
              </tr>
              </tbody>
            </v-table>
          </div>
        </v-card-text>
      </v-card>
    </v-dialog>
  </v-card>
</template>

<script setup>
import { ref, onMounted, computed, onUnmounted, watch } from 'vue';
import statisticsService from '@/services/statisticsService';
import timeSheetService from '@/services/timeSheetService';
import taskService from '@/services/taskService';
import groupService from '@/services/groupService';

// États
const loading = ref(true);
const error = ref(null);
const refreshInterval = ref(null);
const selectedTab = ref('global');
const autoChainTasks = ref(false);

// Données principales
const statistics = ref(null);
const timeSheets = ref([]);
const expandedPanels = ref([]);

// Dialogues
const timeSheetDetailDialog = ref(false);
const groupDetailDialog = ref(false);
const selectedTimeSheet = ref(null);
const selectedGroupId = ref(null);

// Groupes et filtrage
const userGroups = ref([]);
const selectedGroup = ref(null);

// Palette de couleurs pour les groupes
const groupColors = [
  '#673ab7', '#3f51b5', '#2196f3', '#9c27b0',
  '#5e35b1', '#3949ab', '#1e88e5',
  '#8e24aa', '#5e35b1', '#3949ab'
];

// Palette pour les catégories
const categoryColors = ['#ff7043', '#26a69a', '#5c6bc0', '#ec407a', '#ab47bc', '#7e57c2'];

// Données pour l'onglet global
const totalTasks = ref(0);
const completedTasks = ref(0);
const totalTime = ref(0);
const categories = ref([
  { name: 'Développement', time: 12600, percentage: 40 },
  { name: 'Réunions', time: 7200, percentage: 25 },
  { name: 'Recherche', time: 5400, percentage: 15 },
  { name: 'Documentation', time: 3600, percentage: 10 },
  { name: 'Tests', time: 1800, percentage: 5 },
]);

// Options de filtrage de groupe
const groups = computed(() => {
  return [
    { title: 'Tous', value: null },
    { title: 'Personnel', value: 'personnel' },
    ...userGroups.value.map(group => ({
      title: group.name,
      value: group.id
    }))
  ];
});

// Stats personnelles
const personalStats = computed(() => {
  if (selectedGroup.value && selectedGroup.value !== 'personnel') {
    return [];
  }
  return timeSheets.value.filter(sheet => !sheet.groupId);
});

// Stats regroupées par groupe
const groupedStats = computed(() => {
  // Créer une map pour regrouper les templates par groupe
  const groupMap = new Map();

  // Pré-remplir avec les groupes connus
  userGroups.value.forEach(group => {
    groupMap.set(group.id, {
      groupId: group.id,
      name: group.name,
      items: []
    });
  });

  // Parcourir les stats et les regrouper
  for (const stat of timeSheets.value) {
    if (stat.groupId) {
      // Si on a un filtre de groupe et que ce n'est pas ce groupe, ignorer
      if (selectedGroup.value && selectedGroup.value !== 'personal' && selectedGroup.value !== stat.groupId) {
        continue;
      }

      // Si le groupe existe, ajouter le stat
      if (groupMap.has(stat.groupId)) {
        groupMap.get(stat.groupId).items.push(stat);
      } else {
        // Si le groupe n'est pas connu, créer une entrée
        groupMap.set(stat.groupId, {
          groupId: stat.groupId,
          name: `Groupe #${stat.groupId}`,
          items: [stat]
        });
      }
    }
  }

  // Convertir la map en tableau, filtrer les groupes vides et trier
  return Array.from(groupMap.values())
    .filter(group => group.items.length > 0)
    .sort((a, b) => a.name.localeCompare(b.name));
});

// Attribuer une couleur en fonction de l'ID du groupe
const getGroupColor = (groupId) => {
  if (!groupId) return '#9c27b0'; // Couleur par défaut pour Personnel

  // Convertir l'ID en nombre si ce n'est pas déjà le cas
  const numericId = typeof groupId === 'number' ? groupId : parseInt(groupId, 10);
  // Utiliser le modulo pour obtenir un index dans le tableau de couleurs
  const colorIndex = (numericId || 0) % groupColors.length;
  return groupColors[colorIndex];
};

// Obtenir une couleur pour une catégorie
const getCategoryColor = (index) => {
  return categoryColors[index % categoryColors.length];
};

// Format le temps (en secondes) pour l'affichage
const formatTime = (seconds) => {
  if (!seconds) return '00:00:00';

  const hrs = Math.floor(seconds / 3600);
  const mins = Math.floor((seconds % 3600) / 60);
  const secs = seconds % 60;

  return `${String(hrs).padStart(2, '0')}:${String(mins).padStart(2, '0')}:${String(secs).padStart(2, '0')}`;
};

// Obtenir le nom d'un groupe par son ID
const getGroupName = (groupId) => {
  if (groupId === 'personal') return 'Feuilles Personnelles';
  const group = userGroups.value.find(g => g.id === groupId);
  return group ? group.name : `Groupe #${groupId}`;
};

// Calculer le temps total pour une feuille de temps
const getTotalTimeForSheet = (sheet) => {
  if (!sheet || !sheet.tasks) return 0;
  return sheet.tasks.reduce((sum, task) => sum + (task.value || 0), 0);
};

// Obtenir toutes les feuilles d'un groupe
const getGroupTimeSheets = () => {
  if (selectedGroupId.value === 'personal') {
    return personalStats.value;
  }

  const group = groupedStats.value.find(g => g.groupId === selectedGroupId.value);
  return group ? group.items : [];
};

// Obtenir le nombre total de tâches pour un groupe
const getGroupTotalTasks = () => {
  const sheets = getGroupTimeSheets();
  return sheets.reduce((sum, sheet) => sum + sheet.tasks.length, 0);
};

// Obtenir le nombre de tâches complétées pour un groupe
const getGroupCompletedTasks = () => {
  const sheets = getGroupTimeSheets();
  return sheets.reduce((sum, sheet) => {
    return sum + sheet.tasks.filter(t => t.completed).length;
  }, 0);
};

// Obtenir le temps total pour un groupe
const getGroupTotalTime = () => {
  const sheets = getGroupTimeSheets();
  return sheets.reduce((sum, sheet) => sum + getTotalTimeForSheet(sheet), 0);
};

// Afficher les détails d'une feuille de temps
const showTimeSheetDetails = (timeSheet) => {
  selectedTimeSheet.value = timeSheet;
  timeSheetDetailDialog.value = true;
};

// Afficher les détails d'un groupe
const showGroupDetails = (groupId) => {
  selectedGroupId.value = groupId;
  groupDetailDialog.value = true;
};

// Charger les données initiales
const loadData = async () => {
  try {
    loading.value = true;
    error.value = null;

    // Chargement parallèle
    const [stats, sheets, groups] = await Promise.all([
      statisticsService.getCurrentUserStatistics(),
      timeSheetService.getUserTimeSheets(),
      groupService.getUserGroups()
    ]);

    // Stocker les données
    statistics.value = stats;
    userGroups.value = groups;

    // Traitement des statistiques globales
    if (stats && stats.summary) {
      totalTasks.value = stats.summary.totalTasks || 0;
      completedTasks.value = stats.summary.completedTasks || 0;
      totalTime.value = stats.summary.totalTimeInMinutes || 0;
    }

    // Transformation des données pour la vue simplifiée
    processTimeSheetData(sheets);
  } catch (err) {
    console.error('Erreur lors du chargement des statistiques:', err);
    error.value = 'Impossible de charger les statistiques';
  } finally {
    loading.value = false;
  }
};

// Traiter les données des feuilles de temps
const processTimeSheetData = (sheets) => {
  // Si pas de données, sortir
  if (!sheets || sheets.length === 0) {
    timeSheets.value = [];
    return;
  }

  // Transformation en structure simplifiée
  const processed = sheets.map(sheet => {
    // Déterminer si c'est une feuille personnelle ou de groupe
    const groupId = sheet.sharedWithGroups && sheet.sharedWithGroups.length > 0
      ? sheet.sharedWithGroups[0].groupId
      : null;

    // Trouver le nom du groupe si applicable
    let groupName = null;
    if (groupId) {
      const group = userGroups.value.find(g => g.id === groupId);
      groupName = group ? group.name : `Groupe ${groupId}`;
    }

    // Construire les données des tâches
    const tasks = sheet.timeSheetTasks ? sheet.timeSheetTasks.map(task => {
      // Valeurs par défaut
      return {
        id: task.taskId,
        name: task.task ? task.task.name : `Tâche #${task.taskId}`,
        icon: 'mdi-clipboard-check-outline',
        value: task.duration || 0,
        completed: task.completed || false
      };
    }) : [];

    return {
      id: sheet.id,
      title: sheet.title || `Feuille du ${formatDate(sheet.entryDate)}`,
      icon: sheet.icon || 'mdi-file-document-outline',
      groupId,
      groupName,
      tasks
    };
  });

  timeSheets.value = processed;
};

// Formater une date pour l'affichage
const formatDate = (dateString) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  return date.toLocaleDateString('fr-FR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric'
  });
};

// Charger les données au montage
onMounted(() => {
  loadData();

  // Rafraîchissement périodique
  refreshInterval.value = setInterval(() => {
    loadData();
  }, 60000); // Toutes les 60 secondes
});

// Nettoyage au démontage
onUnmounted(() => {
  if (refreshInterval.value) {
    clearInterval(refreshInterval.value);
  }
});

// Exposer méthode de rafraîchissement
const refreshData = async (silent = false) => {
  if (!silent) loading.value = true;
  await loadData();
  if (!silent) loading.value = false;
};

// Exposer pour le parent
defineExpose({
  refreshData
});

// Observer les changements de groupe pour filtrer
watch(selectedGroup, () => {
  console.log('Groupe sélectionné:', selectedGroup.value);
});
</script>

<style scoped>
.max-width-180 {
  max-width: 180px;
}

.tabs-container {
  border-bottom: 1px solid rgba(255, 255, 255, 0.12);
  margin-bottom: 8px;
}

.group-header {
  background-color: #512da8; /* Couleur par défaut */
  border-radius: 4px;
  font-weight: bold;
}

.stat-item {
  background-color: #1e2a82;
  border-radius: 4px;
  transition: background-color 0.2s;
}

.stat-item:hover {
  background-color: #2a377f;
}

.completed-task {
  opacity: 0.7;
}

.task-time {
  font-family: monospace;
  font-weight: 600;
}

.chart-container {
  display: flex;
  align-items: center;
  justify-content: center;
}

/* Pour la table dans le dialog de groupe */
:deep(.v-table) {
  background-color: #1a237e !important;
  color: white !important;
}

:deep(.v-table th) {
  background-color: #000a43 !important;
  color: white !important;
}
</style>
