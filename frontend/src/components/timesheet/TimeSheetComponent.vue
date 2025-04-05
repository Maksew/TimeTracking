<script setup>
import { ref, watch, onMounted, computed } from 'vue';
import { useAuthStore } from '@/stores/auth';
import timeSheetService from '@/services/timeSheetService';
import groupService from '@/services/groupService';

// États
const selectedGroup = ref(null);
const currentTime = ref('00:00:00');
const loading = ref(true);
const error = ref(null);

// Données
const timeSheets = ref([]);
const timeSheetTasks = ref([]);
const userGroups = ref([]);
const authStore = useAuthStore();

// Transforme les groupes pour l'affichage dans le select
const taskGroups = computed(() => {
  return [
    { title: 'Tous', value: null },
    ...userGroups.value.map(group => ({
      title: group.name,
      value: group.id
    }))
  ];
});

// Calcule le pourcentage de complétion
const completion = computed(() => {
  if (!timeSheetTasks.value.length) return 0;

  const completed = timeSheetTasks.value.filter(task => task.completed).length;
  return Math.round((completed / timeSheetTasks.value.length) * 100);
});

// Format la durée totale
const formatTotalTime = () => {
  // Calcule le temps total à partir des timeSheetTasks
  const totalMinutes = timeSheetTasks.value.reduce((total, task) => {
    return total + (task.duration || 0);
  }, 0);

  const hours = Math.floor(totalMinutes / 60);
  const minutes = totalMinutes % 60;
  const seconds = 0;

  return `${String(hours).padStart(2, '0')}:${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`;
};

// Charge les feuilles de temps de l'utilisateur
const loadTimeSheets = async () => {
  try {
    loading.value = true;

    // Récupérer les feuilles de temps et les groupes en parallèle
    const [sheets, groups] = await Promise.all([
      timeSheetService.getUserTimeSheets(),
      groupService.getUserGroups()
    ]);

    timeSheets.value = sheets;
    userGroups.value = groups;

    // Filtrer par groupe si nécessaire
    filterTimeSheetsByGroup();

    // Mettre à jour le temps total
    currentTime.value = formatTotalTime();
  } catch (err) {
    console.error('Erreur lors du chargement des feuilles de temps:', err);
    error.value = 'Impossible de charger les feuilles de temps';
  } finally {
    loading.value = false;
  }
};

// Filtre les feuilles de temps par groupe
const filterTimeSheetsByGroup = () => {
  // Si aucun groupe n'est sélectionné, prendre la première feuille de temps
  let filteredSheets = [...timeSheets.value];

  if (selectedGroup.value) {
    // Filtrer les feuilles de temps partagées avec ce groupe
    filteredSheets = filteredSheets.filter(sheet =>
      sheet.sharedWithGroups?.some(g => g.groupId === selectedGroup.value)
    );
  }

  // S'il y a des feuilles de temps après filtrage, prendre la première
  if (filteredSheets.length > 0) {
    // Convertir les tâches de la feuille de temps pour l'affichage
    const firstSheet = filteredSheets[0];

    // Format requis pour l'affichage
    timeSheetTasks.value = firstSheet.timeSheetTasks.map(tst => ({
      id: tst.taskId,
      name: tst.task?.name || `Tâche #${tst.taskId}`,
      duration: tst.duration,
      time: formatTime(tst.duration),
      completed: tst.duration > 0 // Une tâche avec durée > 0 est considérée comme complétée
    }));
  } else {
    timeSheetTasks.value = [];
  }
};

// Formate le temps en minutes vers "HH:MM:SS" ou "XX min"
const formatTime = (minutes) => {
  if (!minutes) return '';

  if (minutes < 60) {
    return `${minutes} min`;
  }

  const hours = Math.floor(minutes / 60);
  const mins = minutes % 60;
  return `${String(hours).padStart(2, '0')}:${String(mins).padStart(2, '0')}:00`;
};

// Mettre à jour le statut de complétion d'une tâche
const toggleTaskComplete = async (task) => {
  // Cette fonction devrait appeler l'API pour mettre à jour le statut de la tâche
  // Mais pour l'instant, nous mettons simplement à jour l'état local
  task.completed = !task.completed;

  // Mettre à jour le temps total
  currentTime.value = formatTotalTime();
};

// Regarder les changements de groupe pour filtrer les feuilles de temps
watch(selectedGroup, filterTimeSheetsByGroup);

// Initialisation
onMounted(() => {
  loadTimeSheets();
});
</script>

<template>
  <v-card color="#283593" dark class="rounded-lg timesheet-card">
    <v-card-title class="d-flex align-center">
      <v-icon size="small" class="mr-2">mdi-file-document-outline</v-icon>
      Feuille de temps
    </v-card-title>

    <v-divider></v-divider>

    <!-- Indicateur de chargement -->
    <div v-if="loading" class="d-flex justify-center align-center my-5">
      <v-progress-circular indeterminate color="white"></v-progress-circular>
    </div>

    <!-- Message d'erreur -->
    <div v-else-if="error" class="pa-4">
      <v-alert type="error" text dense>{{ error }}</v-alert>
    </div>

    <!-- Contenu principal -->
    <template v-else>
      <div class="d-flex justify-center align-center py-4">
        <div class="text-h3 font-weight-bold timer">{{ currentTime }}</div>
        <v-btn icon class="ml-4 stop-btn" color="purple">
          <v-icon>mdi-stop</v-icon>
        </v-btn>
      </div>

      <v-card-text>
        <!-- Sélecteur de groupe -->
        <v-select
          v-model="selectedGroup"
          :items="taskGroups"
          item-title="title"
          item-value="value"
          variant="outlined"
          density="compact"
          label="Groupe"
          bg-color="#1a237e"
          hide-details
          class="group-select mb-3"
        ></v-select>

        <!-- Nombre de tâches incomplètes -->
        <div v-if="timeSheets.length > 0 && timeSheetTasks.length > 0" class="task-counter mb-2">
          Personnel - {{ timeSheetTasks.filter(t => !t.completed).length }} tâches incomplètes
        </div>

        <!-- Liste des tâches -->
        <div class="task-container">
          <template v-if="timeSheets.length > 0 && timeSheetTasks.length > 0">
            <v-expansion-panels>
              <v-expansion-panel
                bg-color="#1a237e"
              >
                <v-expansion-panel-title>
                  <v-icon size="small" class="mr-2">mdi-home</v-icon>
                  Rangement Maison
                </v-expansion-panel-title>

                <v-expansion-panel-text>
                  <v-list bg-color="transparent">
                    <v-list-item
                      v-for="task in timeSheetTasks"
                      :key="task.id"
                      :class="{ 'completed-task': task.completed }"
                    >
                      <template v-slot:prepend>
                        <v-checkbox-btn
                          v-model="task.completed"
                          color="white"
                          hide-details
                          @change="toggleTaskComplete(task)"
                        ></v-checkbox-btn>
                      </template>

                      <v-list-item-title>{{ task.name }}</v-list-item-title>

                      <template v-slot:append>
                        <span>{{ task.time }}</span>
                      </template>
                    </v-list-item>
                  </v-list>
                </v-expansion-panel-text>
              </v-expansion-panel>
            </v-expansion-panels>

            <!-- Cercle de progression -->
            <div class="d-flex align-center justify-center mt-6">
              <div class="progress-circle-container">
                <v-progress-circular
                  :model-value="completion"
                  :color="completion < 50 ? '#9c27b0' : '#e040fb'"
                  size="120"
                  width="8"
                  class="progress-circle"
                >
                  <span class="percentage">{{ completion }}%</span>
                </v-progress-circular>
              </div>
            </div>
          </template>

          <!-- Message si aucune feuille de temps n'est disponible -->
          <template v-else>
            <div class="text-center py-8">
              <v-icon size="large" class="mb-2">mdi-file-document-outline</v-icon>
              <p>Aucune feuille de temps disponible</p>
              <v-btn color="primary" to="/editTimesheet" class="mt-3">
                Créer une feuille de temps
              </v-btn>
            </div>
          </template>
        </div>
      </v-card-text>
    </template>
  </v-card>
</template>

<style scoped>
.timesheet-card {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.group-select {
  max-width: 180px;
}

.task-counter {
  font-size: 0.9rem;
  color: rgba(255, 255, 255, 0.8);
}

.task-container {
  flex-grow: 1;
  display: flex;
  flex-direction: column;
}

.completed-task {
  opacity: 0.7;
}

:deep(.v-expansion-panel-text__wrapper) {
  padding: 0;
}

.timer {
  font-size: 2.5rem;
  font-weight: 700;
  letter-spacing: 2px;
}

.stop-btn {
  background-color: rgba(156, 39, 176, 0.3);
  transition: background-color 0.2s;
}

.stop-btn:hover {
  background-color: rgba(156, 39, 176, 0.5);
}

.progress-circle-container {
  position: relative;
  margin: 16px 0;
}

.progress-circle {
  filter: drop-shadow(0 0 8px rgba(156, 39, 176, 0.4));
}

.percentage {
  font-size: 1.5rem;
  font-weight: bold;
  color: white;
}
</style>
