<script setup>
import { ref, watch, onMounted, computed, onUnmounted, nextTick } from 'vue';
import { useAuthStore } from '@/stores/auth';
import timeSheetService from '@/services/timeSheetService';
import groupService from '@/services/groupService';

const authStore = useAuthStore();

// États
const loading = ref(true);
const error = ref(null);
const statusMessage = ref('');

// Données principales
const timeSheets = ref([]);
const userGroups = ref([]);
const selectedGroup = ref(null);

// Organisation hiérarchique
const expandedTimeSheets = ref({});
const expandedPanels = ref([]);

// État du chronomètre
const timerRunning = ref(false);
const timerSeconds = ref(0);
const timerInterval = ref(null);
const selectedTimeSheet = ref(null);
const selectedTask = ref(null);
const autoChainTasks = ref(false);

// Transforme les groupes pour l'affichage dans le select
const groupOptions = computed(() => {
  // Ajouter l'option "Personnel" en plus des groupes réels
  return [
    { title: 'Tous', value: null },
    { title: 'Personnel', value: 'personnel' },
    ...userGroups.value.map(group => ({
      title: group.name,
      value: group.id
    }))
  ];
});

// Format le temps du chronomètre (en secondes) pour l'affichage
const formattedTimerDisplay = computed(() => {
  const hours = Math.floor(timerSeconds.value / 3600);
  const minutes = Math.floor((timerSeconds.value % 3600) / 60);
  const seconds = timerSeconds.value % 60;

  return `${String(hours).padStart(2, '0')}:${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`;
});

// Charge les feuilles de temps de l'utilisateur
const loadTimeSheets = async () => {
  try {
    loading.value = true;
    error.value = null;

    // Récupérer les feuilles de temps et les groupes en parallèle
    const [sheets, groups] = await Promise.all([
      timeSheetService.getUserTimeSheets(),
      groupService.getUserGroups()
    ]);

    userGroups.value = groups;

    // Traiter les feuilles de temps pour s'assurer que les propriétés nécessaires sont présentes
    timeSheets.value = sheets.map(sheet => {
      // Si pas de titre, utiliser la date formatée
      if (!sheet.title) {
        sheet.title = `Feuille du ${new Date(sheet.entryDate).toLocaleDateString()}`;
      }

      // S'assurer que les tâches ont les propriétés nécessaires
      if (sheet.timeSheetTasks) {
        sheet.timeSheetTasks = sheet.timeSheetTasks.map(task => {
          // Assurer que chaque tâche a un nom (depuis l'objet task.task si possible)
          if (task.task && task.task.name) {
            task.name = task.task.name;
          } else if (!task.name) {
            task.name = `Tâche non nommée`;
          }

          return task;
        });
      }

      return sheet;
    });

    // Par défaut, sélectionner "Tous" (null)
    if (!selectedGroup.value) {
      selectedGroup.value = null;
    }

    // Par défaut, ouvrir la première feuille de temps si aucune n'est sélectionnée
    if (sheets.length > 0 && !selectedTimeSheet.value) {
      setSelectedTimeSheet(sheets[0]);
    }
  } catch (err) {
    console.error('Erreur lors du chargement des feuilles de temps:', err);
    error.value = 'Impossible de charger les feuilles de temps';
  } finally {
    loading.value = false;
  }
};

// Filtre les feuilles de temps par groupe
const filteredTimeSheets = computed(() => {
  if (selectedGroup.value === 'personnel') {
    // Afficher uniquement les feuilles personnelles (non partagées)
    return timeSheets.value.filter(sheet =>
      !sheet.sharedWithGroups || sheet.sharedWithGroups.length === 0
    );
  } else if (!selectedGroup.value) {
    // Afficher toutes les feuilles
    return timeSheets.value;
  } else {
    // Filtrer par groupe spécifique
    return timeSheets.value.filter(sheet =>
      sheet.sharedWithGroups?.some(g => g.groupId === selectedGroup.value)
    );
  }
});

// Sélectionne une feuille de temps et déploie ses tâches
const setSelectedTimeSheet = (timeSheet) => {
  if (!timeSheet) return;

  selectedTimeSheet.value = timeSheet;

  // Trouver l'index de la feuille dans les feuilles filtrées et l'ouvrir
  const index = filteredTimeSheets.value.findIndex(ts => ts.id === timeSheet.id);
  if (index !== -1 && !expandedPanels.value.includes(index)) {
    expandedPanels.value.push(index);
  }
};

// Sélectionne une tâche pour chronométrage (ne marque pas comme complétée)
const selectTask = (timeSheet, task) => {
  // Si un timer est en cours pour une autre tâche, demander confirmation
  if (timerRunning.value && selectedTask.value &&
    (selectedTask.value.taskId !== task.taskId || selectedTimeSheet.value?.id !== timeSheet.id)) {
    const confirm = window.confirm("Un chronométrage est en cours. Voulez-vous l'arrêter et passer à cette tâche?");
    if (!confirm) return;
    stopTimer();
  }

  // Sélectionner la tâche (sans changer son statut)
  selectedTimeSheet.value = timeSheet;
  selectedTask.value = task;

  // Feedback visuel
  statusMessage.value = `Tâche sélectionnée: ${task.name}`;
  setTimeout(() => {
    if (statusMessage.value === `Tâche sélectionnée: ${task.name}`) {
      statusMessage.value = '';
    }
  }, 2000);
};

// Démarre le chronomètre pour la tâche sélectionnée
const startTimer = () => {
  if (!selectedTask.value) {
    statusMessage.value = 'Veuillez sélectionner une tâche';
    setTimeout(() => { statusMessage.value = ''; }, 2000);
    return;
  }

  if (timerRunning.value) return;

  timerRunning.value = true;
  timerSeconds.value = 0;

  timerInterval.value = setInterval(() => {
    timerSeconds.value++;
  }, 1000);

  // Message discret
  statusMessage.value = `Chronométrage démarré: ${selectedTask.value.name}`;
  setTimeout(() => { statusMessage.value = ''; }, 2000);
};

// Arrête le chronomètre et enregistre le temps passé
const stopTimer = async () => {
  if (!timerRunning.value || !selectedTask.value || !selectedTimeSheet.value) return;

  clearInterval(timerInterval.value);
  timerInterval.value = null;

  // Calculer les minutes passées et sauvegarder
  const minutesSpent = Math.round(timerSeconds.value / 60);
  if (minutesSpent > 0) {
    try {
      await saveTaskTime(selectedTask.value, minutesSpent);

      // Message discret
      statusMessage.value = `${minutesSpent} minutes ajoutées à "${selectedTask.value.name}"`;
      setTimeout(() => { statusMessage.value = ''; }, 2000);

      // Si mode enchaînement automatique activé
      if (autoChainTasks.value) {
        await nextTick(); // Attendre la mise à jour du DOM
        moveToNextTask();
      }
    } catch (err) {
      console.error('Erreur lors de la sauvegarde du temps:', err);
      statusMessage.value = 'Erreur lors de la sauvegarde';
      setTimeout(() => { statusMessage.value = ''; }, 2000);
    }
  }

  timerRunning.value = false;
  timerSeconds.value = 0;
};

// Sauvegarde le temps passé sur une tâche
const saveTaskTime = async (task, additionalMinutes) => {
  if (!task || !task.taskId || !selectedTimeSheet.value?.id) return;

  // Calculer la nouvelle durée totale
  const newDuration = (task.duration || 0) + additionalMinutes;

  // Mettre à jour sur le serveur
  await timeSheetService.updateTaskDuration(selectedTimeSheet.value.id, task.taskId, newDuration);

  // Mettre à jour localement la durée sans changer le statut completed
  task.duration = newDuration;

  // Recharger les données pour s'assurer que tout est à jour
  await loadTimeSheets();
};

// Marque explicitement une tâche comme complétée ou non
const toggleTaskCompleted = async (task, completed) => {
  // Ce clic est indépendant de la sélection de tâche pour chronométrage
  task.completed = completed;

  // On pourrait avoir une API dédiée pour mettre à jour le statut, si elle existe
  // Pour l'instant, on simule localement ce comportement
  console.log(`Tâche "${task.name}" marquée comme ${completed ? 'complétée' : 'non complétée'}`);
};

// Passe à la tâche suivante non complétée dans la feuille de temps actuelle
const moveToNextTask = () => {
  if (!selectedTimeSheet.value) return;

  const tasks = selectedTimeSheet.value.timeSheetTasks || [];
  if (tasks.length === 0) return;

  // Trouver l'index de la tâche actuelle
  const currentIndex = tasks.findIndex(t => t.taskId === selectedTask.value?.taskId);

  // Trouver la prochaine tâche
  let nextTaskIndex = (currentIndex + 1) % tasks.length;

  // Sélectionner la tâche suivante
  selectTask(selectedTimeSheet.value, tasks[nextTaskIndex]);

  // Message discret
  statusMessage.value = `Passé à la tâche: ${tasks[nextTaskIndex].name}`;
  setTimeout(() => { statusMessage.value = ''; }, 2000);
};

// Formatage du temps (minutes -> HH:MM)
const formatTime = (minutes) => {
  if (!minutes) return '00:00';
  const hours = Math.floor(minutes / 60);
  const mins = minutes % 60;
  return `${String(hours).padStart(2, '0')}:${String(mins).padStart(2, '0')}`;
};

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

// Observer les changements de groupe pour filtrer les feuilles de temps
watch(selectedGroup, () => {
  // Si le groupe sélectionné change et qu'il n'y a plus de feuilles correspondantes
  if (filteredTimeSheets.value.length === 0) {
    selectedTimeSheet.value = null;
    selectedTask.value = null;
  }
  // Si le groupe change et la feuille sélectionnée n'est plus dans le filtre
  else if (selectedTimeSheet.value &&
    !filteredTimeSheets.value.some(ts => ts.id === selectedTimeSheet.value.id)) {
    // Sélectionner la première feuille du nouveau filtre
    setSelectedTimeSheet(filteredTimeSheets.value[0]);
  }
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

    <!-- Message de statut discret -->
    <div v-if="statusMessage" class="status-message">
      {{ statusMessage }}
    </div>

    <!-- Contenu principal -->
    <template v-else>
      <!-- Chronomètre -->
      <div class="d-flex justify-center align-center py-4">
        <div class="text-h3 font-weight-bold timer">
          {{ formattedTimerDisplay }}
        </div>

        <template v-if="!timerRunning">
          <v-btn
            icon
            class="ml-4 play-btn"
            color="green"
            :disabled="!selectedTask"
            @click="startTimer"
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

      <!-- Information sur tâche sélectionnée -->
      <div v-if="selectedTask" class="selected-task-info pa-2 mb-3">
        <div class="d-flex align-center">
          <v-icon :icon="selectedTimeSheet?.icon || 'mdi-file-document'" class="mr-2"></v-icon>
          <span class="font-weight-medium">{{ selectedTimeSheet?.title || "Feuille" }}</span>
          <v-icon class="mx-2">mdi-chevron-right</v-icon>
          <span class="selected-task-name">{{ selectedTask.name }}</span>
        </div>
      </div>

      <v-card-text>
        <!-- Option d'enchaînement automatique -->
        <div class="auto-chain-option mb-3">
          <v-checkbox
            v-model="autoChainTasks"
            label="Enchaîner automatiquement les tâches"
            color="white"
            hide-details
          >
            <template v-slot:label>
              <div @click.stop>Enchaîner automatiquement les tâches</div>
            </template>
          </v-checkbox>
        </div>

        <!-- Sélecteur de groupe -->
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
          class="group-select mb-3"
        ></v-select>

        <!-- Liste des feuilles de temps -->
        <div class="timesheet-container">
          <template v-if="filteredTimeSheets.length > 0">
            <v-expansion-panels v-model="expandedPanels" multiple>
              <v-expansion-panel
                v-for="(timeSheet, index) in filteredTimeSheets"
                :key="timeSheet.id"
                :value="index"
                bg-color="#1a237e"
              >
                <v-expansion-panel-title>
                  <div class="d-flex align-center">
                    <v-icon :icon="timeSheet.icon || 'mdi-file-document'" class="mr-2"></v-icon>
                    <span>{{ timeSheet.title }}</span>
                    <v-chip class="ml-2" size="small" color="primary">
                      {{ timeSheet.timeSheetTasks?.length || 0 }} tâches
                    </v-chip>
                  </div>
                </v-expansion-panel-title>

                <v-expansion-panel-text>
                  <v-list bg-color="transparent">
                    <v-list-item
                      v-for="task in timeSheet.timeSheetTasks"
                      :key="task.taskId"
                      :class="{
                        'selected-task': selectedTask && selectedTask.taskId === task.taskId && selectedTimeSheet.id === timeSheet.id
                      }"
                      @click="selectTask(timeSheet, task)"
                    >
                      <template v-slot:prepend>
                        <!-- La case à cocher contrôle seulement l'apparence visuelle,
                             mais n'affecte pas la sélection de la tâche pour le timer -->
                        <v-checkbox-btn
                          :model-value="task.completed"
                          color="white"
                          hide-details
                          @click.stop="toggleTaskCompleted(task, !task.completed)"
                        ></v-checkbox-btn>
                      </template>

                      <v-list-item-title
                        :class="{ 'text-decoration-line-through': task.completed }"
                      >
                        {{ task.name }}
                      </v-list-item-title>

                      <template v-slot:append>
                        <span>{{ formatTime(task.duration || 0) }}</span>
                      </template>
                    </v-list-item>
                  </v-list>
                </v-expansion-panel-text>
              </v-expansion-panel>
            </v-expansion-panels>
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

.timesheet-container {
  flex-grow: 1;
  display: flex;
  flex-direction: column;
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

.selected-task-info {
  background-color: rgba(156, 39, 176, 0.2);
  border-left: 3px solid #e040fb;
  padding: 8px 12px;
  margin: 0 16px;
  border-radius: 4px;
}

.selected-task-name {
  font-weight: 500;
}

.status-message {
  position: absolute;
  bottom: 10px;
  right: 10px;
  background-color: rgba(0, 0, 0, 0.6);
  padding: 6px 10px;
  border-radius: 4px;
  font-size: 0.85rem;
  opacity: 0.9;
  transition: opacity 0.3s;
  max-width: 80%;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  z-index: 10;
}
</style>
