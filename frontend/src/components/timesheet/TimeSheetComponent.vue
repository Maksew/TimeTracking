<script setup>
import { ref, watch, onMounted, computed, onUnmounted } from 'vue';
import { useAuthStore } from '@/stores/auth';
import timeSheetService from '@/services/timeSheetService';
import groupService from '@/services/groupService';

// États
const selectedGroup = ref(null);
const currentTime = ref('00:00:00');
const loading = ref(true);
const error = ref(null);
const successMessage = ref('');

// Données
const timeSheets = ref([]);
const timeSheetTasks = ref([]);
const userGroups = ref([]);
const authStore = useAuthStore();

// États pour le chronomètre
const selectedTask = ref(null);
const timerRunning = ref(false);
const timerSeconds = ref(0);
const timerInterval = ref(null);
const activeTimeSheetId = ref(null);

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
    activeTimeSheetId.value = firstSheet.id;

    // Format requis pour l'affichage
    timeSheetTasks.value = firstSheet.timeSheetTasks.map(tst => ({
      id: tst.taskId,
      timeSheetId: firstSheet.id,
      name: tst.task?.name || `Tâche #${tst.taskId}`,
      duration: tst.duration || 0,
      time: formatTime(tst.duration || 0),
      completed: tst.duration > 0 // Une tâche avec durée > 0 est considérée comme complétée
    }));
  } else {
    timeSheetTasks.value = [];
    activeTimeSheetId.value = null;
  }
};

// Formate le temps en minutes vers "HH:MM:SS" ou "XX min"
const formatTime = (minutes) => {
  if (!minutes) return '00:00';

  if (minutes < 60) {
    return `${minutes} min`;
  }

  const hours = Math.floor(minutes / 60);
  const mins = minutes % 60;
  return `${String(hours).padStart(2, '0')}:${String(mins).padStart(2, '0')}`;
};

// Format le temps du chronomètre (en secondes) pour l'affichage
const formatTimerDisplay = () => {
  const hours = Math.floor(timerSeconds.value / 3600);
  const minutes = Math.floor((timerSeconds.value % 3600) / 60);
  const seconds = timerSeconds.value % 60;

  return `${String(hours).padStart(2, '0')}:${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`;
};

// Démarre le chronomètre pour une tâche spécifique
const startTimer = (task) => {
  if (timerRunning.value) {
    stopTimer(); // Arrêter tout timer en cours
  }

  selectedTask.value = task;
  timerRunning.value = true;
  timerSeconds.value = 0;

  timerInterval.value = setInterval(() => {
    timerSeconds.value++;
  }, 1000);

  successMessage.value = `Chronométrage démarré pour "${task.name}"`;
  setTimeout(() => { successMessage.value = ''; }, 3000);
};

// Arrête le chronomètre et enregistre le temps passé
const stopTimer = async () => {
  if (!timerRunning.value || !selectedTask.value) return;

  clearInterval(timerInterval.value);

  // Calculer les minutes passées et sauvegarder
  const minutesSpent = Math.round(timerSeconds.value / 60);
  if (minutesSpent > 0) {
    await saveTaskTime(selectedTask.value, minutesSpent);
  }

  timerRunning.value = false;
  timerSeconds.value = 0;

  successMessage.value = `Chronométrage arrêté pour "${selectedTask.value.name}"`;
  setTimeout(() => { successMessage.value = ''; }, 3000);
};

// Sauvegarde le temps passé sur une tâche
const saveTaskTime = async (task, additionalMinutes) => {
  try {
    if (!task || !task.id || !activeTimeSheetId.value) return;

    // Calculer la nouvelle durée totale
    const newDuration = (task.duration || 0) + additionalMinutes;

    // Mettre à jour sur le serveur
    await timeSheetService.updateTaskDuration(activeTimeSheetId.value, task.id, newDuration);

    // Mettre à jour localement
    const updatedTask = timeSheetTasks.value.find(t => t.id === task.id);
    if (updatedTask) {
      updatedTask.duration = newDuration;
      updatedTask.time = formatTime(newDuration);
      updatedTask.completed = true;
    }

    // Mettre à jour le temps total affiché
    currentTime.value = formatTotalTime();

  } catch (err) {
    console.error('Erreur lors de l\'enregistrement du temps:', err);
    error.value = 'Impossible d\'enregistrer le temps passé';
  }
};

// Regarder les changements de groupe pour filtrer les feuilles de temps
watch(selectedGroup, filterTimeSheetsByGroup);

// Nettoyer l'intervalle du chronomètre lors du démontage du composant
onUnmounted(() => {
  if (timerInterval.value) {
    clearInterval(timerInterval.value);
  }
});

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
      <v-spacer></v-spacer>
      <v-btn icon small to="/editTimesheet" class="ml-2">
        <v-icon>mdi-plus</v-icon>
        <v-tooltip activator="parent" location="bottom">Nouvelle feuille</v-tooltip>
      </v-btn>
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

    <!-- Message de succès -->
    <div v-if="successMessage" class="pa-2">
      <v-alert type="success" text dense>{{ successMessage }}</v-alert>
    </div>

    <!-- Contenu principal -->
    <template v-else>
      <!-- Chronomètre -->
      <div class="d-flex justify-center align-center py-4">
        <div class="text-h3 font-weight-bold timer">
          {{ timerRunning ? formatTimerDisplay() : currentTime }}
        </div>

        <template v-if="!timerRunning">
          <v-btn
            icon
            class="ml-4 play-btn"
            color="green"
            :disabled="!selectedTask"
            @click="startTimer(selectedTask)"
          >
            <v-icon>mdi-play</v-icon>
            <v-tooltip activator="parent" location="bottom">Démarrer</v-tooltip>
          </v-btn>
        </template>

        <template v-else>
          <v-btn
            icon
            class="ml-4 stop-btn"
            color="red"
            @click="stopTimer"
          >
            <v-icon>mdi-stop</v-icon>
            <v-tooltip activator="parent" location="bottom">Arrêter</v-tooltip>
          </v-btn>
        </template>
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
                  Tâches disponibles
                </v-expansion-panel-title>

                <v-expansion-panel-text>
                  <v-list bg-color="transparent">
                    <v-list-item
                      v-for="task in timeSheetTasks"
                      :key="task.id"
                      :class="{
                        'completed-task': task.completed,
                        'selected-task': selectedTask && selectedTask.id === task.id
                      }"
                      @click="selectedTask = task"
                    >
                      <template v-slot:prepend>
                        <v-checkbox-btn
                          v-model="task.completed"
                          color="white"
                          hide-details
                          @click.stop
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

.selected-task {
  background-color: rgba(156, 39, 176, 0.3) !important;
  border-left: 3px solid #e040fb;
}

:deep(.v-expansion-panel-text__wrapper) {
  padding: 0;
}

.timer {
  font-size: 2.5rem;
  font-weight: 700;
  letter-spacing: 2px;
}

.play-btn {
  background-color: rgba(76, 175, 80, 0.3);
  transition: background-color 0.2s;
}

.play-btn:hover {
  background-color: rgba(76, 175, 80, 0.5);
}

.stop-btn {
  background-color: rgba(244, 67, 54, 0.3);
  transition: background-color 0.2s;
}

.stop-btn:hover {
  background-color: rgba(244, 67, 54, 0.5);
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
