<script setup>
import { ref, watch, onMounted, computed, onUnmounted } from 'vue';
import statisticsService from '@/services/statisticsService';
import timeSheetService from '@/services/timeSheetService';
import taskService from '@/services/taskService';
import groupService from '@/services/groupService';
import VueApexCharts from 'vue3-apexcharts';

// États
const loading = ref(true);
const error = ref(null);
const expandedPanels = ref([]);
const refreshInterval = ref(null);


// Mode d'affichage (feuilles de temps ou groupes)
const viewMode = ref('timesheets'); // 'timesheets' ou 'groups'

// Données principales
const statistics = ref(null);
const timeSheets = ref([]);
const organizedStats = ref([]);
const tasksMap = ref({});
const groupStats = ref([]);

// Groupes
const userGroups = ref([]);
const selectedGroup = ref(null);

// Options du sélecteur de groupe
const groupOptions = computed(() => {
  return [
    { title: 'Tous', value: null },
    { title: 'Personnel', value: 'personnel' },
    ...userGroups.value.map(group => ({
      title: group.name,
      value: group.id
    }))
  ];
});

// Options de graphique pour les groupes
const groupChartOptions = ref({
  chart: {
    type: 'bar',
    foreColor: '#fff',
    background: 'transparent',
    toolbar: {
      show: false
    }
  },
  plotOptions: {
    bar: {
      horizontal: false,
      columnWidth: '55%',
      borderRadius: 4,
      distributed: true,
    }
  },
  dataLabels: {
    enabled: false
  },
  colors: ['#FF6B6B', '#4ECDC4', '#FFD166', '#6A8EAE', '#9F90CF', '#5D69B1', '#52BCA3', '#E76F51'],
  xaxis: {
    categories: [],
    labels: {
      style: {
        colors: '#fff',
        fontSize: '12px'
      }
    }
  },
  yaxis: {
    title: {
      text: 'Temps (heures)',
      style: {
        color: '#fff'
      }
    },
    labels: {
      formatter: function(val) {
        return (val / 3600).toFixed(1) + 'h';
      }
    }
  },
  tooltip: {
    y: {
      formatter: function(val) {
        return formatTime(val);
      }
    }
  },
  legend: {
    show: false
  }
});

// Options pour les graphiques circulaires
const pieChartOptions = ref({
  chart: {
    type: 'donut',
    foreColor: '#fff',
    background: 'transparent',
  },
  plotOptions: {
    pie: {
      donut: {
        size: '65%',
        labels: {
          show: true,
          name: {
            show: true,
            fontSize: '14px',
            color: '#fff',
          },
          value: {
            show: true,
            fontSize: '16px',
            color: '#fff',
            formatter: function(val) {
              return formatTime(val);
            }
          },
          total: {
            show: true,
            label: 'Total',
            color: '#fff',
            formatter: function(w) {
              const total = w.globals.seriesTotals.reduce((a, b) => a + b, 0);
              return formatTime(total);
            }
          }
        }
      }
    }
  },
  labels: [],
  dataLabels: {
    enabled: false
  },
  legend: {
    position: 'bottom',
    fontSize: '14px',
    labels: {
      colors: '#fff'
    },
    itemMargin: {
      horizontal: 8,
      vertical: 8
    }
  },
  tooltip: {
    y: {
      formatter: function(val) {
        return formatTime(val);
      }
    }
  },
  colors: ['#FF6B6B', '#4ECDC4', '#FFD166', '#6A8EAE', '#9F90CF', '#5D69B1', '#52BCA3', '#E76F51'],
  responsive: [
    {
      breakpoint: 480,
      options: {
        chart: {
          width: 200
        },
        legend: {
          position: 'bottom'
        }
      }
    }
  ]
});

// Format le temps correctement (en secondes)
const formatTime = (timeValue) => {
  if (timeValue === null || timeValue === undefined) return '00:00:00';

  // La valeur est toujours en secondes
  const totalSeconds = timeValue;

  const hours = Math.floor(totalSeconds / 3600);
  const mins = Math.floor((totalSeconds % 3600) / 60);
  const secs = totalSeconds % 60;

  return `${String(hours).padStart(2, '0')}:${String(mins).padStart(2, '0')}:${String(secs).padStart(2, '0')}`;
};

// Récupération de couleur pour un groupe
const getGroupColor = (groupId) => {
  // Palette de couleurs pour les groupes
  const groupColors = [
    '#673ab7', '#3f51b5', '#2196f3', '#9c27b0',
    '#5e35b1', '#3949ab', '#1e88e5',
    '#8e24aa', '#5e35b1', '#3949ab'
  ];

  if (!groupId) return '#9c27b0'; // Couleur par défaut

  // Convertir l'ID en nombre si ce n'est pas déjà le cas
  const numericId = typeof groupId === 'number' ? groupId : parseInt(groupId, 10);

  // Utiliser le modulo pour obtenir un index dans le tableau de couleurs
  const colorIndex = (numericId || 0) % groupColors.length;

  return groupColors[colorIndex];
};

// Charger les statistiques
const loadStatistics = async (silent = false) => {
  try {
    if (!silent) loading.value = true;
    error.value = null;

    // 1. Charger les groupes de l'utilisateur
    const groups = await groupService.getUserGroups();
    userGroups.value = groups;

    // 2. Charger toutes les tâches pour référence
    const allTasks = await taskService.getAllTasks();

    // Construire un map des tâches par ID pour accès rapide
    const tasksByIdMap = {};
    allTasks.forEach(task => {
      tasksByIdMap[task.id] = task;
    });
    tasksMap.value = tasksByIdMap;

    // 3. Récupérer les feuilles personnelles et partagées directement
    const [ownedTimeSheets, sharedTimeSheets] = await Promise.all([
      timeSheetService.getUserTimeSheets(),
      timeSheetService.getSharedTimeSheets()
    ]);

    // 4. Récupérer les feuilles partagées avec chaque groupe
    const groupSharedTimeSheetsPromises = [];
    for (const group of groups) {
      groupSharedTimeSheetsPromises.push(timeSheetService.getGroupTimeSheets(group.id));
    }

    const groupSharedTimeSheetsResults = await Promise.all(groupSharedTimeSheetsPromises);

    // 5. Fusionner toutes les feuilles de temps
    let allTimeSheets = [...ownedTimeSheets, ...sharedTimeSheets];

    groupSharedTimeSheetsResults.forEach(groupSheets => {
      if (Array.isArray(groupSheets)) {
        allTimeSheets = [...allTimeSheets, ...groupSheets];
      }
    });

    // 6. Dédupliquer pour éviter les doublons
    const uniqueTimeSheets = [...new Map(allTimeSheets.map(sheet => [sheet.id, sheet])).values()];

    // Stocker les feuilles de temps
    timeSheets.value = uniqueTimeSheets;

    // 7. Récupérer les statistiques globales depuis le backend
    statistics.value = await statisticsService.getCurrentUserStatistics();

    // 8. Organiser les données pour l'affichage
    processTimeSheetsData(uniqueTimeSheets, tasksByIdMap);
    processGroupData(uniqueTimeSheets, tasksByIdMap);

    console.log(`Statistiques chargées: ${uniqueTimeSheets.length} feuilles au total`);

    if (!silent) loading.value = false;
  } catch (err) {
    console.error('Erreur lors du chargement des statistiques:', err);
    if (!silent) error.value = 'Impossible de charger les statistiques';
    if (!silent) loading.value = false;
  }
};

// Traiter les données des feuilles de temps
const processTimeSheetsData = (allTimeSheets, tasksByIdMap) => {
  const timeSheetStats = {};

  // Traiter les feuilles de temps pour les statistiques
  allTimeSheets.forEach(timeSheet => {
    // Filtrer par groupe si sélectionné
    if (selectedGroup.value) {
      if (selectedGroup.value === 'personnel') {
        // Montrer uniquement les feuilles personnelles
        if (timeSheet.sharedWithGroups && timeSheet.sharedWithGroups.length > 0) {
          return;
        }
      } else {
        // Montrer uniquement les feuilles du groupe sélectionné
        const isInSelectedGroup = timeSheet.sharedWithGroups &&
          timeSheet.sharedWithGroups.some(share => share.groupId === selectedGroup.value);
        if (!isInSelectedGroup) {
          return;
        }
      }
    }

    // Initialiser les statistiques pour cette feuille
    const title = timeSheet.title || `Feuille du ${new Date(timeSheet.entryDate).toLocaleDateString()}`;
    const timeSheetId = timeSheet.id;

    timeSheetStats[timeSheetId] = {
      id: timeSheetId,
      title,
      icon: timeSheet.icon || 'mdi-file-document-outline',
      isPersonal: !timeSheet.sharedWithGroups || timeSheet.sharedWithGroups.length === 0,
      groupId: timeSheet.sharedWithGroups && timeSheet.sharedWithGroups.length > 0
        ? timeSheet.sharedWithGroups[0].groupId : null,
      groupName: timeSheet.sharedWithGroups && timeSheet.sharedWithGroups.length > 0
        ? userGroups.value.find(g => g.id === timeSheet.sharedWithGroups[0].groupId)?.name || 'Groupe'
        : 'Personnel',
      tasks: [],
      stats: {
        totalTasks: 0,
        completedTasks: 0,
        totalTime: 0,
        chartData: {
          series: [],
          labels: []
        }
      }
    };

    // Ajouter les tâches et calculer les statistiques
    if (timeSheet.timeSheetTasks && timeSheet.timeSheetTasks.length > 0) {
      timeSheet.timeSheetTasks.forEach(task => {
        const taskDetails = tasksByIdMap[task.taskId] || { name: `Tâche #${task.taskId}` };
        const taskName = taskDetails.name;
        const taskDuration = task.duration || 0;
        const isCompleted = task.completed || taskDuration > 0;

        // Ajouter la tâche aux statistiques
        timeSheetStats[timeSheetId].tasks.push({
          id: task.taskId,
          name: taskName,
          duration: taskDuration,
          completed: isCompleted
        });

        // Mettre à jour les statistiques globales de cette feuille
        timeSheetStats[timeSheetId].stats.totalTasks++;
        if (isCompleted) {
          timeSheetStats[timeSheetId].stats.completedTasks++;
        }
        timeSheetStats[timeSheetId].stats.totalTime += taskDuration;

        // Ajouter les données pour le graphique
        if (taskDuration > 0) {
          timeSheetStats[timeSheetId].stats.chartData.series.push(taskDuration);
          timeSheetStats[timeSheetId].stats.chartData.labels.push(taskName);
        }
      });
    }
  });

  // Convertir en tableau pour le template
  organizedStats.value = Object.values(timeSheetStats);
};

// Traiter les données pour les statistiques par groupe
const processGroupData = (allTimeSheets, tasksByIdMap) => {
  // Créer un objet pour stocker les statistiques par groupe
  const groups = {};

  // Ajouter une entrée pour "Personnel"
  groups['personnel'] = {
    id: 'personnel',
    name: 'Personnel',
    color: '#9c27b0',
    totalTasks: 0,
    completedTasks: 0,
    totalTime: 0,
    taskStats: {}, // Statistiques par tâche
    chartData: {
      series: [],
      labels: []
    }
  };

  // Préparer une entrée pour chaque groupe connu
  userGroups.value.forEach(group => {
    groups[group.id] = {
      id: group.id,
      name: group.name,
      color: getGroupColor(group.id),
      totalTasks: 0,
      completedTasks: 0,
      totalTime: 0,
      taskStats: {}, // Statistiques par tâche
      chartData: {
        series: [],
        labels: []
      }
    };
  });

  // Parcourir les feuilles de temps pour collecter les statistiques par groupe
  allTimeSheets.forEach(timeSheet => {
    // Déterminer le groupe de cette feuille de temps
    const groupId = timeSheet.sharedWithGroups && timeSheet.sharedWithGroups.length > 0
      ? timeSheet.sharedWithGroups[0].groupId
      : 'personnel';

    // Vérifier si ce groupe est dans notre liste
    if (!groups[groupId]) {
      // Si ce groupe n'est pas connu, l'ajouter
      groups[groupId] = {
        id: groupId,
        name: `Groupe #${groupId}`,
        color: getGroupColor(groupId),
        totalTasks: 0,
        completedTasks: 0,
        totalTime: 0,
        taskStats: {},
        chartData: {
          series: [],
          labels: []
        }
      };
    }

    // Ajouter les statistiques des tâches de cette feuille de temps au groupe
    if (timeSheet.timeSheetTasks && timeSheet.timeSheetTasks.length > 0) {
      timeSheet.timeSheetTasks.forEach(task => {
        const taskDetails = tasksByIdMap[task.taskId] || { name: `Tâche #${task.taskId}` };
        const taskName = taskDetails.name;
        const taskDuration = task.duration || 0;
        const isCompleted = task.completed || taskDuration > 0;

        // Mettre à jour les compteurs globaux du groupe
        groups[groupId].totalTasks++;
        if (isCompleted) {
          groups[groupId].completedTasks++;
        }
        groups[groupId].totalTime += taskDuration;

        // Mettre à jour les statistiques par tâche
        if (!groups[groupId].taskStats[task.taskId]) {
          groups[groupId].taskStats[task.taskId] = {
            id: task.taskId,
            name: taskName,
            totalDuration: 0,
            occurrences: 0,
            completedOccurrences: 0
          };
        }

        groups[groupId].taskStats[task.taskId].totalDuration += taskDuration;
        groups[groupId].taskStats[task.taskId].occurrences++;
        if (isCompleted) {
          groups[groupId].taskStats[task.taskId].completedOccurrences++;
        }
      });
    }
  });

  // Préparer les données des graphiques pour chaque groupe
  Object.values(groups).forEach(group => {
    // Convertir les statistiques par tâche en tableau
    const taskStatsArray = Object.values(group.taskStats);

    // Trier par durée totale (décroissant)
    taskStatsArray.sort((a, b) => b.totalDuration - a.totalDuration);

    // Prendre les 8 tâches les plus importantes pour le graphique (pour éviter la surcharge)
    const topTasks = taskStatsArray.slice(0, 8);

    // Préparer les données du graphique
    group.chartData.series = topTasks.map(task => task.totalDuration);
    group.chartData.labels = topTasks.map(task => task.name);

    // Préparer les données pour le graphique en barres
    group.barChartData = {
      categories: topTasks.map(task => task.name.length > 12 ? task.name.substring(0, 10) + '...' : task.name),
      series: [
        {
          name: 'Temps passé',
          data: topTasks.map(task => task.totalDuration)
        }
      ]
    };
  });

  // Filtrer les groupes si un groupe spécifique est sélectionné
  let filteredGroups = Object.values(groups);
  if (selectedGroup.value) {
    if (selectedGroup.value === 'personnel') {
      filteredGroups = [groups['personnel']];
    } else {
      filteredGroups = [groups[selectedGroup.value]].filter(Boolean);
    }
  }

  // Stocker les statistiques des groupes
  groupStats.value = filteredGroups;
};

// Charger les groupes pour le filtre
const loadGroups = async () => {
  try {
    const groups = await groupService.getUserGroups();
    userGroups.value = groups;
  } catch (err) {
    console.error('Erreur lors du chargement des groupes:', err);
  }
};

// Préparer les options du graphique pour une feuille de temps spécifique
const getChartOptionsForTimeSheet = (timeSheet) => {
  if (!timeSheet || !timeSheet.stats || !timeSheet.stats.chartData) {
    return { ...pieChartOptions.value, labels: [], series: [] };
  }

  return {
    ...pieChartOptions.value,
    labels: timeSheet.stats.chartData.labels,
    series: timeSheet.stats.chartData.series
  };
};

// Préparer les options du graphique pour un groupe spécifique
const getChartOptionsForGroup = (group) => {
  if (!group || !group.chartData) {
    return { ...pieChartOptions.value, labels: [], series: [] };
  }

  return {
    ...pieChartOptions.value,
    labels: group.chartData.labels,
    series: group.chartData.series
  };
};

// Préparer les options du graphique en barres pour un groupe
const getBarChartOptionsForGroup = (group) => {
  if (!group || !group.barChartData) {
    return { ...groupChartOptions.value, xaxis: { categories: [] } };
  }

  return {
    ...groupChartOptions.value,
    xaxis: {
      ...groupChartOptions.value.xaxis,
      categories: group.barChartData.categories
    }
  };
};

// Rafraîchir toutes les données
const refreshData = async (silent = false) => {
  await loadStatistics(silent);
};

// Exposer la méthode pour permettre au composant parent de rafraîchir les données
defineExpose({
  refreshData
});

// Observer les changements de groupe pour recharger les statistiques
watch(selectedGroup, () => {
  loadStatistics();
});

// Initialisation
onMounted(() => {
  // Charger les données au démarrage
  loadStatistics();

  // Configuration du rafraîchissement périodique
  refreshInterval.value = setInterval(() => {
    refreshData(true); // Mode silencieux
  }, 60000); // Rafraîchir toutes les 60 secondes
});

// Nettoyage à la destruction du composant
onUnmounted(() => {
  if (refreshInterval.value) {
    clearInterval(refreshInterval.value);
  }
});
</script>

<template>
  <v-card color="#283593" dark class="rounded-lg">
    <v-card-title class="d-flex align-center">
      <v-icon size="small" class="mr-2">mdi-chart-bar</v-icon>
      Statistiques
      <v-spacer></v-spacer>
      <v-btn variant="text" icon="mdi-download" class="ml-2">
        <v-tooltip activator="parent" location="top">Exporter</v-tooltip>
      </v-btn>
    </v-card-title>

    <v-divider></v-divider>

    <v-card-text>
      <!-- Contrôles de filtre et mode d'affichage -->
      <div class="d-flex align-center mb-4">
        <div class="mr-2">
          <v-btn-toggle
            v-model="viewMode"
            color="primary"
            mandatory
            variant="outlined"
            density="compact"
          >
            <v-btn value="timesheets">Feuilles</v-btn>
            <v-btn value="groups">Groupes</v-btn>
          </v-btn-toggle>
        </div>

        <v-select
          v-model="selectedGroup"
          :items="groupOptions"
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

      <!-- Affichage des statistiques par feuille de temps -->
      <div v-else-if="viewMode === 'timesheets'" class="stats-container">
        <template v-if="organizedStats.length > 0">
          <!-- Utilisation de v-expansion-panels pour organiser les statistiques -->
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

                  <!-- Indicateur personnel ou groupe -->
                  <v-chip
                    v-if="timeSheet.isPersonal"
                    class="ml-2"
                    size="small"
                    color="purple"
                  >
                    Personnel
                  </v-chip>
                  <v-chip
                    v-else-if="timeSheet.groupId"
                    class="ml-2"
                    size="small"
                    :color="getGroupColor(timeSheet.groupId)"
                  >
                    {{ timeSheet.groupName }}
                  </v-chip>

                  <v-chip class="ml-2" size="small" color="info">
                    {{ timeSheet.tasks.length }} tâches
                  </v-chip>
                </div>
              </v-expansion-panel-title>

              <v-expansion-panel-text>
                <!-- Statistiques récapitulatives -->
                <v-row>
                  <v-col cols="12" sm="4">
                    <v-card color="#1a237e" class="rounded-lg text-center pa-3">
                      <div class="text-h5 font-weight-bold">{{ timeSheet.stats.totalTasks }}</div>
                      <div class="text-caption">Tâches totales</div>
                    </v-card>
                  </v-col>
                  <v-col cols="12" sm="4">
                    <v-card color="#1a237e" class="rounded-lg text-center pa-3">
                      <div class="text-h5 font-weight-bold">{{ timeSheet.stats.completedTasks }}</div>
                      <div class="text-caption">Tâches complétées</div>
                    </v-card>
                  </v-col>
                  <v-col cols="12" sm="4">
                    <v-card color="#1a237e" class="rounded-lg text-center pa-3">
                      <div class="text-h5 font-weight-bold">{{ formatTime(timeSheet.stats.totalTime) }}</div>
                      <div class="text-caption">Temps total</div>
                    </v-card>
                  </v-col>
                </v-row>

                <!-- Graphique de répartition du temps -->
                <v-card color="#1a237e" class="mt-4 pa-4 rounded-lg" v-if="timeSheet.stats.chartData.series.length > 0">
                  <h3 class="text-subtitle-1 mb-3">Répartition du temps par tâche</h3>
                  <div style="height: 300px;">
                    <VueApexCharts
                      type="donut"
                      :options="getChartOptionsForTimeSheet(timeSheet)"
                      :series="timeSheet.stats.chartData.series"
                      height="300"
                    />
                  </div>
                </v-card>

                <!-- Liste détaillée des tâches -->
                <v-card color="#1a237e" class="mt-4 rounded-lg">
                  <v-list bg-color="transparent">
                    <v-list-item
                      v-for="task in timeSheet.tasks"
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
                        <span class="task-time">{{ formatTime(task.duration) }}</span>
                      </template>
                    </v-list-item>
                  </v-list>
                </v-card>
              </v-expansion-panel-text>
            </v-expansion-panel>
          </v-expansion-panels>
        </template>

        <!-- Message si aucune donnée disponible -->
        <template v-else>
          <div class="text-center py-8">
            <v-icon size="large" class="mb-2">mdi-file-document-outline</v-icon>
            <p>Aucune feuille de temps disponible</p>
          </div>
        </template>
      </div>

      <!-- Affichage des statistiques par groupe -->
      <div v-else-if="viewMode === 'groups'" class="stats-container">
        <template v-if="groupStats.length > 0">
          <v-expansion-panels v-model="expandedPanels" multiple>
            <v-expansion-panel
              v-for="(group, index) in groupStats"
              :key="group.id || index"
              :value="index"
              bg-color="#1a237e"
            >
              <v-expansion-panel-title>
                <div class="d-flex align-center">
                  <v-icon class="mr-2">
                    {{ group.id === 'personnel' ? 'mdi-account' : 'mdi-account-group' }}
                  </v-icon>
                  <span>{{ group.name }}</span>

                  <v-chip
                    class="ml-2"
                    size="small"
                    :color="group.color"
                  >
                    {{ group.totalTasks }} tâches
                  </v-chip>
                </div>
              </v-expansion-panel-title>

              <v-expansion-panel-text>
                <!-- Statistiques récapitulatives du groupe -->
                <v-row>
                  <v-col cols="12" sm="4">
                    <v-card :color="group.color" dark class="rounded-lg text-center pa-3">
                      <div class="text-h5 font-weight-bold">{{ group.totalTasks }}</div>
                      <div class="text-caption">Tâches totales</div>
                    </v-card>
                  </v-col>
                  <v-col cols="12" sm="4">
                    <v-card :color="group.color" dark class="rounded-lg text-center pa-3">
                      <div class="text-h5 font-weight-bold">{{ group.completedTasks }}</div>
                      <div class="text-caption">Tâches complétées</div>
                    </v-card>
                  </v-col>
                  <v-col cols="12" sm="4">
                    <v-card :color="group.color" dark class="rounded-lg text-center pa-3">
                      <div class="text-h5 font-weight-bold">{{ formatTime(group.totalTime) }}</div>
                      <div class="text-caption">Temps total</div>
                    </v-card>
                  </v-col>
                </v-row>

                <!-- Graphiques du groupe -->
                <div class="mt-4">
                  <v-row>
                    <!-- Graphique en donut montrant la répartition par tâche -->
                    <v-col cols="12" md="6" v-if="group.chartData.series.length > 0">
                      <v-card color="#1a237e" class="pa-4 rounded-lg">
                        <h3 class="text-subtitle-1 mb-3">Répartition du temps</h3>
                        <div style="height: 300px;">
                          <VueApexCharts
                            type="donut"
                            :options="getChartOptionsForGroup(group)"
                            :series="group.chartData.series"
                            height="300"
                          />
                        </div>
                      </v-card>
                    </v-col>

                    <!-- Graphique en barres montrant la répartition par tâche -->
                    <v-col cols="12" md="6" v-if="group.barChartData && group.barChartData.series[0].data.length > 0">
                      <v-card color="#1a237e" class="pa-4 rounded-lg">
                        <h3 class="text-subtitle-1 mb-3">Temps par tâche</h3>
                        <div style="height: 300px;">
                          <VueApexCharts
                            type="bar"
                            :options="getBarChartOptionsForGroup(group)"
                            :series="group.barChartData.series"
                            height="300"
                          />
                        </div>
                      </v-card>
                    </v-col>
                  </v-row>
                </div>

                <!-- Liste des tâches principales du groupe -->
                <v-card color="#1a237e" class="mt-4 rounded-lg">
                  <v-card-title class="text-subtitle-1">Principales tâches</v-card-title>
                  <v-list bg-color="transparent">
                    <v-list-item
                      v-for="task in Object.values(group.taskStats).sort((a, b) => b.totalDuration - a.totalDuration).slice(0, 10)"
                      :key="task.id"
                    >
                      <v-list-item-title>{{ task.name }}</v-list-item-title>
                      <template v-slot:append>
                        <div>
                          <span class="task-time">{{ formatTime(task.totalDuration) }}</span>
                          <span class="task-stat ml-2">({{ task.completedOccurrences }}/{{ task.occurrences }} complétées)</span>
                        </div>
                      </template>
                    </v-list-item>
                  </v-list>
                </v-card>
              </v-expansion-panel-text>
            </v-expansion-panel>
          </v-expansion-panels>
        </template>

        <!-- Message si aucun groupe disponible -->
        <template v-else>
          <div class="text-center py-8">
            <v-icon size="large" class="mb-2">mdi-account-group</v-icon>
            <p>Aucun groupe disponible</p>
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

.stats-container {
  max-height: calc(100vh - 320px);
  overflow-y: auto;
}

:deep(.v-expansion-panel-text__wrapper) {
  padding: 0;
}

.completed-task {
  opacity: 0.7;
}

.completed-task .v-list-item-title {
  text-decoration: line-through;
}

.task-time {
  font-family: monospace;
  font-weight: 600;
}

.task-stat {
  font-size: 0.8rem;
  opacity: 0.7;
}

/* Style pour les graphiques */
:deep(.apexcharts-legend-text) {
  color: white !important;
}
</style>
