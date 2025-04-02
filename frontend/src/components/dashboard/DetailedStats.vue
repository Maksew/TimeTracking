<script setup>
import { ref, watch, onMounted } from 'vue';
import axios from 'axios';

const selectedTab = ref('global');
const selectedGroup = ref(null);
const groups = ref([]);
const taskStats = ref([]);

// Retrieve the token from localStorage (ensure it's stored after login)
const token = localStorage.getItem('token');
console.log('token:', token); // Verify token

// Function to fetch global statistics (current user) and transform the categories data
const fetchGlobalStats = async () => {
  try {
    const response = await axios.get('http://localhost:8989/api/statistics/user', {
      headers: {
        Authorization: `Bearer ${token}`
      }
    });
    console.log('Stats:', response.data);

    const rawCategories = response.data.categories;

    if (rawCategories && rawCategories.length > 0) {
      // Transform the structure into taskStats expected by the template
      taskStats.value = rawCategories.map(category => ({
        category: category.name || 'Catégorie inconnue',
        tasks: category.tasks.map(task => ({
          name: task.name,
          time: `${task.duration} min`,
          completion: task.completionRate ?? 100,
          icon: task.icon || 'mdi-clock-outline'
        }))
      }));
    } else {
      // Fallback if no categories exist
      taskStats.value = [{
        category: 'Statistiques',
        tasks: [{
          name: 'Aucune tâche',
          time: '0 min',
          completion: 0,
          icon: 'mdi-alert-circle-outline'
        }]
      }];
    }
  } catch (error) {
    console.error('Error fetching global statistics:', error);
  }
};

// Function to fetch detailed statistics
const fetchDetailedStats = async () => {
  try {
    let response;
    if (selectedGroup.value) {
      // Si un groupe est sélectionné, on récupère les statistiques du groupe
      response = await axios.get(`http://localhost:8989/api/statistics/group/${selectedGroup.value}`, {
        headers: { Authorization: `Bearer ${token}` }
      });
    } else {
      // Sinon, on récupère les statistiques d'un utilisateur (ici id=1)
      response = await axios.get(`http://localhost:8989/api/statistics/user/1`, {
        headers: { Authorization: `Bearer ${token}` }
      });
    }
    console.log('Stats:', response.data);

    // Si l'API ne renvoie pas de catégories, on construit un fallback en utilisant summary et dailyStats
    if (response.data.categories && response.data.categories.length > 0) {
      taskStats.value = response.data.categories.map(category => ({
        category: category.name || 'Catégorie inconnue',
        tasks: category.tasks.map(task => ({
          name: task.name,
          time: `${task.duration} min`,
          completion: task.completionRate ?? 100,
          icon: task.icon || 'mdi-clock-outline'
        }))
      }));
    } else {
      // Fallback : utiliser summary et dailyStats pour afficher quelques statistiques détaillées
      const { summary, dailyStats } = response.data;
      taskStats.value = [
        {
          category: 'Résumé Utilisateur',
          tasks: [
            {
              name: 'Feuilles de temps',
              time: `${summary.totalTimeSheets}`,
              completion: 100,
              icon: 'mdi-timer'
            },
            {
              name: 'Tâches totales',
              time: `${summary.totalTasks}`,
              completion: summary.totalTasks > 0 ? Math.round((summary.completedTasks / summary.totalTasks) * 100) : 0,
              icon: 'mdi-format-list-bulleted'
            }
          ]
        },
        {
          category: 'Statistiques Quotidiennes',
          tasks: [
            {
              name: 'Temps total',
              time: `${dailyStats.totalTimeInMinutes} min`,
              completion: 100,
              icon: 'mdi-clock-outline'
            },
            {
              name: 'Tâches complétées',
              time: `${dailyStats.completedTasks}`,
              completion: dailyStats.totalTasks > 0 ? Math.round((dailyStats.completedTasks / dailyStats.totalTasks) * 100) : 0,
              icon: 'mdi-check'
            }
          ]
        }
      ];
    }
  } catch (error) {
    console.error('Error fetching detailed statistics:', error);
  }
};


// Function to load groups for the v-select component
const loadGroups = async () => {
  try {
    const response = await axios.get('http://localhost:8989/api/groups', {
      headers: {
        Authorization: `Bearer ${token}`
      }
    });
    // Transform groups to match the v-select item structure
    groups.value = response.data.map(group => ({
      title: group.name,
      value: group.id
    }));
  } catch (error) {
    console.error('Error fetching groups:', error);
  }
};

// Load stats based on the selected tab and group
const loadStats = () => {
  if (selectedTab.value === 'global') {
    fetchGlobalStats();
  } else if (selectedTab.value === 'detailed') {
    fetchDetailedStats();
  }
};

// Watch for changes on the selected tab or group to refresh statistics
watch(selectedTab, loadStats);
watch(selectedGroup, () => {
  if (selectedTab.value === 'detailed') {
    loadStats();
  }
});

// On component mount, load groups and initial statistics
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

      <div class="mt-4">
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
                  <span class="text-caption">{{ task.completion }}%</span>
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
