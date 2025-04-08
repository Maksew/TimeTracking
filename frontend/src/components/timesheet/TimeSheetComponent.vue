<script setup>
import { ref, watch, onMounted, computed, onUnmounted, nextTick } from 'vue';
import { useAuthStore } from '@/stores/auth';
import timeSheetService from '@/services/timeSheetService';
import taskService from '@/services/taskService';
import groupService from '@/services/groupService';

const authStore = useAuthStore();

// États
const loading = ref(true);
const error = ref(null);
const statusMessage = ref('');
const processingTask = ref(false); // Indicateur de traitement en cours

// Données principales
const timeSheets = ref([]);
const allTasks = ref([]);
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

// Référence à la tâche actuellement sélectionnée (pour l'affichage)
const taskSelectionText = computed(() => {
  if (!selectedTask.value) return '';
  return `Tâche sélectionnée: ${selectedTask.value.name}`;
});

// Charge les feuilles de temps de l'utilisateur
const loadTimeSheets = async () => {
  try {
    loading.value = true;
    error.value = null;

    // Récupérer les feuilles de temps, les groupes et toutes les tâches en parallèle
    const [sheets, groups, tasks] = await Promise.all([
      timeSheetService.getUserTimeSheets(),
      groupService.getUserGroups(),
      taskService.getAllTasks()
    ]);

    // Stocker toutes les tâches
    allTasks.value = tasks;

    // Créer un map des tâches par ID pour un accès rapide
    const tasksMap = {};
    tasks.forEach(task => {
      tasksMap[task.id] = task;
    });

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
          // Récupérer le nom de la tâche depuis le tasksMap
          const taskDetails = tasksMap[task.taskId];

          if (taskDetails) {
            // Assurer que chaque tâche a un nom à partir de la tâche trouvée
            task.name = taskDetails.name;
            task.repetition = taskDetails.repetition;
          } else {
            // Si la tâche n'est pas trouvée, utiliser un nom par défaut
            task.name = `Tâche #${task.taskId}`;
          }

          return task;
        });
      }

      // Ajouter une propriété pour indiquer si c'est une feuille "personnelle"
      sheet.isPersonal = !sheet.sharedWithGroups || sheet.sharedWithGroups.length === 0;

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
    return timeSheets.value.filter(sheet => sheet.isPersonal);
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

// Retourne le label du groupe actuel pour l'affichage
const currentGroupLabel = computed(() => {
  if (selectedGroup.value === 'personnel') {
    return 'Personnel';
  } else if (!selectedGroup.value) {
    return 'Toutes les feuilles';
  } else {
    const group = userGroups.value.find(g => g.id === selectedGroup.value);
    return group ? group.name : 'Groupe sélectionné';
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

// Sélectionne une tâche pour chronométrage (ou désélectionne si c'est la même)
const selectTask = (timeSheet, task) => {
  // Prévenir les sélections multiples trop rapides
  if (processingTask.value) return;

  // Marquer comme étant en traitement
  processingTask.value = true;

  // Utilisation de setTimeout pour rendre l'opération non-bloquante
  setTimeout(() => {
    try {
      // Si on clique sur la même tâche déjà sélectionnée, on la désélectionne
      if (selectedTask.value && selectedTask.value.taskId === task.taskId &&
        selectedTimeSheet.value && selectedTimeSheet.value.id === timeSheet.id) {

        // Si un timer est en cours, demander confirmation (mais en mode non-bloquant)
        if (timerRunning.value) {
          const confirmStop = confirm("Un chronométrage est en cours. Voulez-vous l'arrêter et désélectionner cette tâche?");
          if (!confirmStop) {
            processingTask.value = false;
            return;
          }

          // Arrêter le timer de manière non-bloquante
          clearInterval(timerInterval.value);
          timerInterval.value = null;
          timerRunning.value = false;
          timerSeconds.value = 0;
        }

        // Désélectionner la tâche
        selectedTask.value = null;
        selectedTimeSheet.value = null;
      } else {
        // Si un timer est en cours pour une autre tâche, gérer de manière non-bloquante
        if (timerRunning.value) {
          const confirmSwitch = confirm("Un chronométrage est en cours. Voulez-vous l'arrêter et passer à cette tâche?");
          if (!confirmSwitch) {
            processingTask.value = false;
            return;
          }

          // Arrêter le timer
          clearInterval(timerInterval.value);
          timerInterval.value = null;
          timerRunning.value = false;
          timerSeconds.value = 0;
        }

        // Sélectionner la nouvelle tâche
        selectedTimeSheet.value = timeSheet;
        selectedTask.value = task;
      }
    } catch (err) {
      console.error('Erreur lors de la sélection/désélection de tâche:', err);
    } finally {
      // Libérer le flag de traitement après un court délai pour éviter les clics frénétiques
      setTimeout(() => {
        processingTask.value = false;
      }, 300);
    }
  }, 0); // setTimeout avec délai de 0ms pour rendre l'opération asynchrone
};

// Vérifie si une tâche est sélectionnée
const isTaskSelected = (timeSheetId, taskId) => {
  return selectedTask.value &&
    selectedTask.value.taskId === taskId &&
    selectedTimeSheet.value &&
    selectedTimeSheet.value.id === timeSheetId;
};

// Démarre le chronomètre pour la tâche sélectionnée
const startTimer = () => {
  if (!selectedTask.value) return;
  if (timerRunning.value) return;

  timerRunning.value = true;
  timerSeconds.value = 0;

  timerInterval.value = setInterval(() => {
    timerSeconds.value++;
  }, 1000);
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

      // Si mode enchaînement automatique activé
      if (autoChainTasks.value) {
        await nextTick(); // Attendre la mise à jour du DOM
        moveToNextTask();
      }
    } catch (err) {
      console.error('Erreur lors de la sauvegarde du temps:', err);
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
const toggleTaskCompleted = (event, task, completed) => {
  // Arrêter la propagation pour ne pas interférer avec la sélection
  event.stopPropagation();

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

  // Sélectionner la tâche suivante de manière non-bloquante
  setTimeout(() => {
    selectTask(selectedTimeSheet.value, tasks[nextTaskIndex]);
  }, 0);
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

      <!-- Information sur tâche sélectionnée (style intégré) -->
      <div v-if="selectedTask" class="selected-task-banner mx-4 mb-4">
        <div class="d-flex align-center">
          <v-icon :icon="selectedTimeSheet?.icon || 'mdi-file-document'" class="mr-2" size="small"></v-icon>
          <span class="font-weight-medium">{{ selectedTimeSheet?.title }}</span>
          <v-icon class="mx-2" size="small">mdi-chevron-right</v-icon>
          <span class="task-name">{{ selectedTask.name }}</span>
        </div>
      </div>

      <!-- Information sur tâche sélectionnée (fixe en bas d'écran) -->
      <div v-if="selectedTask" class="task-selection-indicator">
        <span>Tâche sélectionnée: {{ selectedTask.name }}</span>
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

        <!-- Afficher le label du groupe actuel -->
        <div v-if="selectedGroup || filteredTimeSheets.length > 0" class="group-label mb-3">
          <span class="font-weight-medium" :class="{'personal-label': selectedGroup === 'personnel'}">
            {{ currentGroupLabel }}
            <span v-if="filteredTimeSheets.length > 0">- {{ filteredTimeSheets.length }}
              {{ filteredTimeSheets.length > 1 ? 'feuilles' : 'feuille' }}
            </span>
          </span>
        </div>

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
                    <!-- Indicateur pour les feuilles personnelles -->
                    <v-chip v-if="timeSheet.isPersonal" size="x-small" class="ml-2" color="secondary">
                      Personnel
                    </v-chip>
                  </div>
                </v-expansion-panel-title>

                <v-expansion-panel-text>
                  <v-list bg-color="transparent">
                    <v-list-item
                      v-for="task in timeSheet.timeSheetTasks"
                      :key="task.taskId"
                      :class="{
                        'selected-task': isTaskSelected(timeSheet.id, task.taskId),
                        'task-completed': task.completed
                      }"
                      @click="selectTask(timeSheet, task)"
                      :disabled="processingTask"
                    >
                      <template v-slot:prepend>
                        <!-- La case à cocher pour marquer comme complété/non complété -->
                        <v-checkbox-btn
                          :model-value="task.completed"
                          color="white"
                          hide-details
                          @click="(e) => toggleTaskCompleted(e, task, !task.completed)"
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

.selected-task-banner {
  background-color: rgba(156, 39, 176, 0.3);
  border-left: 3px solid #e040fb;
  padding: 10px 15px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  transition: all 0.2s ease;
  text-align: left;
  width: auto;
  margin-left: 20px;
}

.selected-task-banner .task-name {
  font-weight: 500;
  color: #fafafa;
}

.task-completed {
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

.task-selection-indicator {
  position: fixed;
  bottom: 20px;
  right: 20px;
  padding: 8px 16px;
  background-color: rgba(0, 0, 0, 0.7);
  border-radius: 8px;
  color: white;
  z-index: 100;
  font-size: 14px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
  max-width: 80%;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.group-label {
  font-size: 0.9rem;
  padding: 4px 8px;
  background-color: rgba(255, 255, 255, 0.05);
  border-radius: 4px;
}

.personal-label {
  color: #e040fb;
}
</style>
