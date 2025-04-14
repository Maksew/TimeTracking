<template>
  <v-card color="#283593" dark class="rounded-lg timesheet-card">
    <!-- Card Header -->
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

    <!-- Loading indicator -->
    <div v-if="loading" class="d-flex justify-center align-center my-5">
      <v-progress-circular indeterminate color="white"></v-progress-circular>
    </div>

    <!-- Error message -->
    <div v-else-if="error" class="pa-4">
      <v-alert type="error" text dense>{{ error }}</v-alert>
    </div>

    <!-- Main content -->
    <template v-else>
      <!-- Chronometer Section -->
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

      <!-- Selected Task Information (inline) -->
      <div v-if="selectedTask" class="selected-task-banner mx-4 mb-4">
        <div class="d-flex align-center">
          <v-icon size="small" class="mr-2">
            {{ selectedTimeSheet?.icon || 'mdi-file-document' }}
          </v-icon>
          <span class="font-weight-medium">{{ selectedTimeSheet?.title }}</span>
          <v-icon class="mx-2" size="small">mdi-chevron-right</v-icon>
          <span class="task-name">{{ selectedTask.name }}</span>
        </div>
      </div>

      <!-- Fixed-position indicator for selected task -->
      <div v-if="selectedTask" class="task-selection-indicator">
        <span>Tâche sélectionnée : {{ selectedTask.name }}</span>
      </div>

      <v-card-text>
        <!-- Auto-chain tasks option -->
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

        <!-- Filters -->
        <div class="d-flex flex-wrap mb-3">
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
            class="group-select mr-2"
          ></v-select>

          <v-select
            v-model="validityFilter"
            :items="validityFilterOptions"
            variant="outlined"
            density="compact"
            label="Statut"
            bg-color="#1a237e"
            hide-details
            class="validity-select"
          ></v-select>
        </div>

        <!-- Current Group Label -->
        <div v-if="selectedGroup || filteredTimeSheets.length > 0" class="group-label mb-3">
          <span class="font-weight-medium" :class="{'personal-label': selectedGroup === 'personnel'}">
            {{ currentGroupLabel }}
            <span v-if="filteredTimeSheets.length > 0"> - {{ filteredTimeSheets.length }}
              {{ filteredTimeSheets.length > 1 ? 'feuilles' : 'feuille' }}
            </span>
          </span>
        </div>

        <!-- Timesheet List -->
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
                    <v-icon size="small" class="mr-2">
                      {{ timeSheet.icon || 'mdi-file-document' }}
                    </v-icon>
                    <span>{{ timeSheet.title }}</span>
                    <v-chip class="ml-2" size="small" color="primary">
                      {{ timeSheet.timeSheetTasks?.length || 0 }} tâches
                    </v-chip>
                    <v-chip
                      v-if="timeSheet.isPersonal"
                      size="small"
                      class="ml-2 category-chip personal-indicator"
                      color="purple"
                      variant="flat"
                    >
                      Personnel
                    </v-chip>
                    <v-chip
                      v-else-if="timeSheet.sharedWithGroups && timeSheet.sharedWithGroups.length > 0"
                      size="small"
                      class="ml-2 category-chip group-indicator"
                      :color="getGroupColor(timeSheet.sharedWithGroups[0].groupId)"
                      variant="flat"
                    >
                      {{ getGroupName(timeSheet.sharedWithGroups[0].groupId) }}
                    </v-chip>
                    <v-chip
                      v-if="timeSheet.endDate"
                      size="x-small"
                      :color="getExpirationColor(timeSheet)"
                      class="ml-2"
                      variant="flat"
                    >
                      {{ getExpirationLabel(timeSheet) }}
                    </v-chip>
                  </div>
                </v-expansion-panel-title>
                <v-expansion-panel-text>
                  <template v-if="timeSheet.startDate && timeSheet.endDate">
                    <div class="validity-indicator mb-3">
                      <div class="d-flex align-center justify-space-between mb-1">
                        <span class="text-caption">
                          Validité : {{ formatDate(timeSheet.startDate) }} - {{ formatDate(timeSheet.endDate) }}
                        </span>
                        <span class="text-caption" :class="getTimeRemainingClass(timeSheet)">
                          {{ getTimeRemainingText(timeSheet) }}
                        </span>
                      </div>
                      <v-progress-linear
                        :model-value="getProgressPercentage(timeSheet)"
                        height="4"
                        rounded
                        :color="getProgressColor(timeSheet)"
                      ></v-progress-linear>
                    </div>
                  </template>
                  <v-list bg-color="transparent">
                    <v-list-item
                      v-for="task in timeSheet.timeSheetTasks"
                      :key="task.taskId"
                      :class="{
                        'selected-task': isTaskSelected(timeSheet.id, task.taskId),
                        'task-completed': task.completed || task.duration > 0
                      }"
                      @click="selectTask(timeSheet, task)"
                      :disabled="processingTask"
                    >
                      <template v-slot:prepend>
                        <v-checkbox-btn
                          :model-value="task.completed || task.duration > 0"
                          color="success"
                          hide-details
                          @click.stop="toggleTaskCompleted($event, task, !(task.completed || task.duration > 0))"
                        ></v-checkbox-btn>
                      </template>
                      <v-list-item-title
                        :class="{ 'text-decoration-line-through': task.completed || task.duration > 0 }"
                      >
                        {{ task.name }}
                      </v-list-item-title>
                      <template v-slot:append>
                        <div class="d-flex align-center">
                          <span>{{ formatTime(task.duration || 0) }}</span>
                          <v-btn
                            icon
                            size="x-small"
                            variant="text"
                            color="grey-lighten-1"
                            class="ml-2 edit-time-btn"
                            @click.stop="editTaskTime(timeSheet, task)"
                          >
                            <v-icon size="small">mdi-pencil</v-icon>
                            <v-tooltip activator="parent" location="top">Modifier le temps</v-tooltip>
                          </v-btn>
                        </div>
                      </template>
                    </v-list-item>
                  </v-list>
                </v-expansion-panel-text>
              </v-expansion-panel>
            </v-expansion-panels>
          </template>
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

    <!-- Confirmation Dialog for Time -->
    <v-dialog v-model="showConfirmDialog" max-width="400" persistent>
      <v-card color="#283593" dark>
        <v-card-title class="text-h5">
          <v-icon start class="mr-2">mdi-clock-check-outline</v-icon>
          Confirmer le temps
        </v-card-title>
        <v-card-text class="pb-4 pt-4">
          <p class="mb-2">Tâche : <strong>{{ selectedTask?.name }}</strong></p>
          <p class="mb-4">Confirmez ou ajustez le temps passé sur cette tâche :</p>
          <div class="time-display text-center mb-6">
            <div class="text-h2 font-weight-bold">
              {{ hours.toString().padStart(2, '0') }}:{{ minutes.toString().padStart(2, '0') }}:{{ seconds.toString().padStart(2, '0') }}
            </div>
            <div class="text-caption">Heures:Minutes:Secondes</div>
          </div>
          <v-row>
            <v-col cols="12" sm="4">
              <v-text-field
                v-model.number="hours"
                label="Heures"
                type="number"
                variant="outlined"
                bg-color="#1a237e"
                min="0"
                density="compact"
              ></v-text-field>
            </v-col>
            <v-col cols="12" sm="4">
              <v-text-field
                v-model.number="minutes"
                label="Minutes"
                type="number"
                variant="outlined"
                bg-color="#1a237e"
                min="0"
                max="59"
                density="compact"
              ></v-text-field>
            </v-col>
            <v-col cols="12" sm="4">
              <v-text-field
                v-model.number="seconds"
                label="Secondes"
                type="number"
                variant="outlined"
                bg-color="#1a237e"
                min="0"
                max="59"
                density="compact"
              ></v-text-field>
            </v-col>
          </v-row>
        </v-card-text>
        <v-divider></v-divider>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn color="grey" variant="text" @click="cancelConfirmation">
            Annuler
          </v-btn>
          <v-btn color="primary" @click="saveConfirmedTime" :disabled="confirmedSeconds <= 0">
            Enregistrer
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Asynchronous Success Notification -->
    <transition name="fade">
      <div v-if="statusMessage" class="success-notification">
        <v-icon class="mr-2" color="white">mdi-check-circle</v-icon>
        {{ statusMessage }}
      </div>
    </transition>

    <!-- Reset Time Confirmation Dialog -->
    <v-dialog v-model="resetTimeDialog" max-width="400" persistent>
      <v-card color="#283593" dark>
        <v-card-title class="headline">
          <v-icon start class="mr-2">mdi-timer-off</v-icon>
          Réinitialiser le temps
        </v-card-title>
        <v-card-text>
          <p>Voulez-vous réinitialiser le temps passé sur cette tâche ?</p>
          <p class="text-subtitle-2 mt-2" v-if="taskToReset">
            <strong>{{ taskToReset.name }}</strong> - Temps actuel : {{ formatTime(taskToReset.duration) }}
          </p>
          <p class="text-caption mt-3">
            <v-icon size="small" color="warning" class="mr-1">mdi-alert-circle</v-icon>
            Cette action ne peut pas être annulée.
          </p>
        </v-card-text>
        <v-divider></v-divider>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn color="grey" variant="text" @click="cancelResetTime">
            Annuler
          </v-btn>
          <v-btn color="error" @click="confirmResetTime">
            Réinitialiser
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Warning Dialog for Task without Time -->
    <v-dialog v-model="noTimeDialog" max-width="400">
      <v-card color="#283593" dark>
        <v-card-title class="headline">
          <v-icon start class="mr-2" color="warning">mdi-alert-circle</v-icon>
          Aucun temps enregistré
        </v-card-title>
        <v-card-text>
          <p>Cette tâche n'a pas de temps enregistré.</p>
          <p class="mt-2">Vous devez d'abord enregistrer du temps avec le chronomètre ou éditer manuellement la durée pour pouvoir la marquer comme complétée.</p>
          <p class="text-subtitle-2 mt-3" v-if="taskToToggle">
            <strong>{{ taskToToggle.name }}</strong>
          </p>
        </v-card-text>
        <v-divider></v-divider>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn color="grey" variant="text" @click="closeNoTimeDialog">
            Fermer
          </v-btn>
          <v-btn color="primary" @click="editTaskTimeFromDialog">
            <v-icon start class="mr-1">mdi-pencil</v-icon>
            Éditer le temps
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </v-card>
</template>

<script setup>
import { ref, watch, onMounted, computed, onUnmounted, nextTick } from 'vue';
import { defineEmits } from 'vue';
import { useAuthStore } from '@/stores/auth';
import timeSheetService from '@/services/timeSheetService';
import taskService from '@/services/taskService';
import groupService from '@/services/groupService';

const authStore = useAuthStore();
const emit = defineEmits(['task-updated', 'data-changed']);

/* =========
   State Variables
   ========= */
const loading = ref(true);
const error = ref(null);
const statusMessage = ref('');
const processingTask = ref(false);
const tasksMap = ref({});

const timeSheets = ref([]);
const allTasks = ref([]);
const userGroups = ref([]);
const selectedGroup = ref(null);
const expandedPanels = ref([]);

const timerRunning = ref(false);
const timerSeconds = ref(0);
const timerInterval = ref(null);
const selectedTimeSheet = ref(null);
const selectedTask = ref(null);
const autoChainTasks = ref(false);

const showConfirmDialog = ref(false);
const confirmedSeconds = ref(0);
const hours = ref(0);
const minutes = ref(0);
const seconds = ref(0);

const resetTimeDialog = ref(false);
const noTimeDialog = ref(false);
const taskToReset = ref(null);
const taskToToggle = ref(null);

const validityFilter = ref('all');
const validityFilterOptions = [
  { title: 'Toutes', value: 'all' },
  { title: 'Actives', value: 'active' },
  { title: 'Expirées', value: 'expired' },
  { title: 'À venir', value: 'upcoming' }
];

/* =========
   Computed Properties
   ========= */
const groupColors = [
  'purple', 'deep-purple', 'indigo', 'blue',
  'deep-purple-darken-1', 'indigo-darken-1', 'blue-darken-1',
  'purple-lighten-1', 'deep-purple-lighten-1', 'indigo-lighten-1'
];

const getGroupColor = (groupId) => {
  const numericId = typeof groupId === 'number' ? groupId : parseInt(groupId, 10);
  const colorIndex = (numericId || 0) % groupColors.length;
  return groupColors[colorIndex];
};

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

const formattedTimerDisplay = computed(() => {
  const hrs = Math.floor(timerSeconds.value / 3600);
  const mins = Math.floor((timerSeconds.value % 3600) / 60);
  const secs = timerSeconds.value % 60;
  return `${String(hrs).padStart(2, '0')}:${String(mins).padStart(2, '0')}:${String(secs).padStart(2, '0')}`;
});

const filteredTimeSheets = computed(() => {
  let filtered = timeSheets.value;
  if (selectedGroup.value === 'personnel') {
    filtered = filtered.filter(sheet => sheet.isPersonal);
  } else if (selectedGroup.value) {
    filtered = filtered.filter(sheet =>
      sheet.sharedWithGroups?.some(g => g.groupId === selectedGroup.value)
    );
  }
  if (validityFilter.value !== 'all') {
    const now = new Date();
    if (validityFilter.value === 'active') {
      filtered = filtered.filter(sheet => {
        if (!sheet.startDate || !sheet.endDate) return true;
        const start = new Date(sheet.startDate);
        const end = new Date(sheet.endDate);
        return now >= start && now <= end;
      });
    } else if (validityFilter.value === 'expired') {
      filtered = filtered.filter(sheet => sheet.endDate && new Date(sheet.endDate) < now);
    } else if (validityFilter.value === 'upcoming') {
      filtered = filtered.filter(sheet => sheet.startDate && new Date(sheet.startDate) > now);
    }
  }
  return filtered;
});

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

/* =========
   Functions: Loading & Processing Data
   ========= */
const loadTimeSheets = async () => {
  try {
    loading.value = true;
    error.value = null;

    // Load user groups
    const groups = await groupService.getUserGroups();
    userGroups.value = groups;

    // Load all tasks for fast reference
    const allTasksFetched = await taskService.getAllTasks();
    const tasksByIdMap = {};
    allTasksFetched.forEach(task => {
      tasksByIdMap[task.id] = task;
    });
    tasksMap.value = tasksByIdMap;

    // Retrieve personal and directly shared timesheets
    const [ownedTimeSheets, sharedTimeSheets] = await Promise.all([
      timeSheetService.getUserTimeSheets(),
      timeSheetService.getSharedTimeSheets()
    ]);

    // Retrieve group shared timesheets
    const groupSharedTimeSheetsPromises = [];
    for (const group of groups) {
      groupSharedTimeSheetsPromises.push(timeSheetService.getGroupTimeSheets(group.id));
    }
    const groupSharedTimeSheetsResults = await Promise.all(groupSharedTimeSheetsPromises);

    let allTimeSheetsArr = [...ownedTimeSheets, ...sharedTimeSheets];
    groupSharedTimeSheetsResults.forEach(groupSheets => {
      if (Array.isArray(groupSheets)) {
        allTimeSheetsArr = [...allTimeSheetsArr, ...groupSheets];
      }
    });
    const uniqueTimeSheets = [...new Map(allTimeSheetsArr.map(sheet => [sheet.id, sheet])).values()];
    const processedTimeSheets = uniqueTimeSheets.map(sheet => {
      if (!sheet.title) {
        sheet.title = `Feuille du ${new Date(sheet.entryDate).toLocaleDateString()}`;
      }
      if (sheet.timeSheetTasks) {
        sheet.timeSheetTasks = sheet.timeSheetTasks.map(task => {
          const taskDetails = tasksMap.value[task.taskId];
          if (taskDetails) {
            task.name = taskDetails.name;
            task.repetition = taskDetails.repetition;
          } else {
            task.name = `Tâche #${task.taskId}`;
          }
          return task;
        });
      }
      sheet.isPersonal = !sheet.sharedWithGroups || sheet.sharedWithGroups.length === 0;
      return sheet;
    });
    timeSheets.value = processedTimeSheets;
  } catch (err) {
    console.error('Erreur lors du chargement des feuilles de temps:', err);
    error.value = 'Impossible de charger les feuilles de temps';
  } finally {
    loading.value = false;
  }
};

const getGroupName = (groupId) => {
  const group = userGroups.value.find(g => g.id === groupId);
  return group ? group.name : 'Groupe';
};

const setSelectedTimeSheet = (timeSheet) => {
  if (!timeSheet) return;
  selectedTimeSheet.value = timeSheet;
  const index = filteredTimeSheets.value.findIndex(ts => ts.id === timeSheet.id);
  if (index !== -1 && !expandedPanels.value.includes(index)) {
    expandedPanels.value.push(index);
  }
};

const selectTask = (timeSheet, task) => {
  if (processingTask.value) return;
  processingTask.value = true;
  setTimeout(() => {
    try {
      if (selectedTask.value && selectedTask.value.taskId === task.taskId &&
        selectedTimeSheet.value && selectedTimeSheet.value.id === timeSheet.id) {
        if (timerRunning.value) {
          const confirmStop = confirm("Un chronométrage est en cours. Voulez-vous l'arrêter et désélectionner cette tâche?");
          if (!confirmStop) {
            processingTask.value = false;
            return;
          }
          clearInterval(timerInterval.value);
          timerInterval.value = null;
          timerRunning.value = false;
          timerSeconds.value = 0;
        }
        selectedTask.value = null;
        selectedTimeSheet.value = null;
      } else {
        if (timerRunning.value) {
          const confirmSwitch = confirm("Un chronométrage est en cours. Voulez-vous l'arrêter et passer à cette tâche?");
          if (!confirmSwitch) {
            processingTask.value = false;
            return;
          }
          clearInterval(timerInterval.value);
          timerInterval.value = null;
          timerRunning.value = false;
          timerSeconds.value = 0;
        }
        selectedTimeSheet.value = timeSheet;
        selectedTask.value = task;
      }
    } catch (err) {
      console.error('Erreur lors de la sélection/désélection de tâche:', err);
    } finally {
      setTimeout(() => {
        processingTask.value = false;
      }, 300);
    }
  }, 0);
};

const isTaskSelected = (timeSheetId, taskId) => {
  return selectedTask.value &&
    selectedTask.value.taskId === taskId &&
    selectedTimeSheet.value &&
    selectedTimeSheet.value.id === timeSheetId;
};

const updateTotalSeconds = () => {
  const h = Math.max(0, parseInt(hours.value) || 0);
  const m = Math.max(0, parseInt(minutes.value) || 0);
  const s = Math.max(0, parseInt(seconds.value) || 0);
  const adjustedSeconds = s % 60;
  const additionalMinutes = Math.floor(s / 60);
  const adjustedMinutes = (m + additionalMinutes) % 60;
  const additionalHours = Math.floor((m + additionalMinutes) / 60);
  if (s !== adjustedSeconds || m !== adjustedMinutes) {
    seconds.value = adjustedSeconds;
    minutes.value = adjustedMinutes;
    hours.value = h + additionalHours;
  }
  confirmedSeconds.value = (h * 3600) + (adjustedMinutes * 60) + adjustedSeconds;
};

const updateTimeFields = () => {
  const totalSeconds = confirmedSeconds.value || 0;
  hours.value = Math.floor(totalSeconds / 3600);
  minutes.value = Math.floor((totalSeconds % 3600) / 60);
  seconds.value = totalSeconds % 60;
};

const startTimer = () => {
  if (!selectedTask.value || timerRunning.value) return;
  timerRunning.value = true;
  timerSeconds.value = 0;
  timerInterval.value = setInterval(() => {
    timerSeconds.value++;
  }, 1000);
};

const stopTimer = () => {
  if (!timerRunning.value || !selectedTask.value || !selectedTimeSheet.value) return;
  clearInterval(timerInterval.value);
  timerInterval.value = null;
  confirmedSeconds.value = timerSeconds.value;
  updateTimeFields();
  showConfirmDialog.value = true;
};

const saveConfirmedTime = async () => {
  try {
    if (confirmedSeconds.value <= 0) return;
    showConfirmDialog.value = false;
    setTimeout(async () => {
      try {
        await timeSheetService.updateTaskDuration(
          selectedTimeSheet.value.id,
          selectedTask.value.taskId,
          confirmedSeconds.value
        );
        if (selectedTask.value) {
          selectedTask.value.duration = confirmedSeconds.value;
        }
        emit('task-updated', {
          timeSheetId: selectedTimeSheet.value.id,
          taskId: selectedTask.value.taskId,
          duration: confirmedSeconds.value
        });
        emit('data-changed');
        statusMessage.value = 'Temps enregistré avec succès!';
        timerRunning.value = false;
        timerSeconds.value = 0;
        confirmedSeconds.value = 0;
        if (autoChainTasks.value) {
          moveToNextTask();
        } else {
          selectedTask.value = null;
          selectedTimeSheet.value = null;
        }
        setTimeout(() => {
          statusMessage.value = '';
        }, 3000);
        await loadTimeSheets();
        emit('data-changed');
      } catch (innerErr) {
        console.error('Erreur lors de la mise à jour de la durée:', innerErr);
        error.value = 'Erreur lors de la mise à jour: ' + (innerErr.message || 'Erreur inconnue');
      }
    }, 100);
  } catch (err) {
    console.error('Erreur lors de la sauvegarde du temps:', err);
    error.value = 'Erreur lors de la sauvegarde: ' + (err.message || 'Erreur inconnue');
  }
};

const refreshData = async (silent = false) => {
  if (!silent) loading.value = true;
  await loadTimeSheets();
  if (!silent) loading.value = false;
};

defineExpose({ refreshData });

const cancelConfirmation = () => {
  showConfirmDialog.value = false;
  timerRunning.value = false;
  timerSeconds.value = 0;
  confirmedSeconds.value = 0;
  hours.value = 0;
  minutes.value = 0;
  seconds.value = 0;
};

const toggleTaskCompleted = async (event, task, completed) => {
  event.stopPropagation();
  try {
    if (completed) {
      if (task.duration <= 0) {
        taskToToggle.value = task;
        noTimeDialog.value = true;
        return;
      }
      task.completed = true;
    } else {
      if (task.duration > 0) {
        taskToReset.value = task;
        resetTimeDialog.value = true;
        return;
      }
      task.completed = false;
    }
    emit('task-updated', {
      timeSheetId: selectedTimeSheet.value.id,
      taskId: task.taskId,
      duration: task.duration,
      completed: task.completed
    });
    emit('data-changed');
    await refreshData(true);
  } catch (err) {
    console.error('Erreur lors de la mise à jour de l\'état de la tâche:', err);
    error.value = 'Erreur lors de la mise à jour de l\'état de la tâche';
  }
};

const confirmResetTime = async () => {
  try {
    taskToReset.value.duration = 0;
    taskToReset.value.completed = false;
    await timeSheetService.updateTaskDuration(
      selectedTimeSheet.value.id,
      taskToReset.value.taskId,
      0
    );
    emit('task-updated', {
      timeSheetId: selectedTimeSheet.value.id,
      taskId: taskToReset.value.taskId,
      duration: 0,
      completed: false
    });
    emit('data-changed');
    await refreshData(true);
    resetTimeDialog.value = false;
    taskToReset.value = null;
  } catch (err) {
    console.error('Erreur lors de la réinitialisation du temps:', err);
    error.value = 'Erreur lors de la réinitialisation du temps';
  }
};

const cancelResetTime = () => {
  if (taskToReset.value) {
    taskToReset.value.completed = true;
    nextTick(() => {
      emit('task-updated', {
        timeSheetId: selectedTimeSheet.value.id,
        taskId: taskToReset.value.taskId,
        duration: taskToReset.value.duration,
        completed: true
      });
    });
  }
  resetTimeDialog.value = false;
  taskToReset.value = null;
};

const closeNoTimeDialog = () => {
  noTimeDialog.value = false;
  taskToToggle.value = null;
};

const editTaskTimeFromDialog = () => {
  if (taskToToggle.value) {
    editTaskTime(selectedTimeSheet.value, taskToToggle.value);
    closeNoTimeDialog();
  }
};

const editTaskTime = (timeSheet, task) => {
  event.stopPropagation();
  selectedTimeSheet.value = timeSheet;
  selectedTask.value = task;
  confirmedSeconds.value = task.duration || 0;
  updateTimeFields();
  showConfirmDialog.value = true;
};

const moveToNextTask = () => {
  if (!selectedTimeSheet.value) return;
  const tasks = selectedTimeSheet.value.timeSheetTasks || [];
  if (tasks.length === 0) return;
  const currentIndex = tasks.findIndex(t => t.taskId === selectedTask.value?.taskId);
  let nextTaskIndex = (currentIndex + 1) % tasks.length;
  setTimeout(() => {
    selectTask(selectedTimeSheet.value, tasks[nextTaskIndex]);
  }, 0);
};

const formatTime = (timeValue) => {
  if (timeValue === null || timeValue === undefined) return '00:00:00';
  const totalSeconds = timeValue;
  const hrs = Math.floor(totalSeconds / 3600);
  const mins = Math.floor((totalSeconds % 3600) / 60);
  const secs = totalSeconds % 60;
  return `${String(hrs).padStart(2, '0')}:${String(mins).padStart(2, '0')}:${String(secs).padStart(2, '0')}`;
};

const formatDate = (dateString) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  return date.toLocaleDateString('fr-FR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric'
  });
};

const getProgressPercentage = (timeSheet) => {
  if (!timeSheet.startDate || !timeSheet.endDate) return 0;
  const start = new Date(timeSheet.startDate);
  const end = new Date(timeSheet.endDate);
  const now = new Date();
  const totalDuration = end - start;
  if (totalDuration <= 0) return 100;
  if (now < start) return 0;
  if (now > end) return 100;
  const elapsed = now - start;
  return Math.min(100, Math.floor((elapsed / totalDuration) * 100));
};

const getTimeRemainingText = (timeSheet) => {
  if (!timeSheet.startDate || !timeSheet.endDate) return 'Sans limite';
  const end = new Date(timeSheet.endDate);
  const now = new Date();
  if (now > end) return 'Expirée';
  const start = new Date(timeSheet.startDate);
  if (now < start) return 'À venir';
  const daysRemaining = Math.ceil((end - now) / (1000 * 60 * 60 * 24));
  return daysRemaining === 1 ? '1 jour restant' : `${daysRemaining} jours restants`;
};

const getProgressColor = (timeSheet) => {
  if (!timeSheet.startDate || !timeSheet.endDate) return 'grey';
  const end = new Date(timeSheet.endDate);
  const now = new Date();
  if (now > end) return 'error';
  const daysRemaining = Math.ceil((end - now) / (1000 * 60 * 60 * 24));
  if (daysRemaining <= 2) return 'orange-darken-1';
  if (daysRemaining <= 7) return 'amber-lighten-1';
  return 'success';
};

const getTimeRemainingClass = (timeSheet) => {
  if (!timeSheet.startDate || !timeSheet.endDate) return '';
  const end = new Date(timeSheet.endDate);
  const now = new Date();
  if (now > end) return 'text-error';
  const daysRemaining = Math.ceil((end - now) / (1000 * 60 * 60 * 24));
  if (daysRemaining <= 2) return 'text-orange';
  return '';
};

const getExpirationColor = (timeSheet) => {
  if (!timeSheet.startDate || !timeSheet.endDate) return 'grey';
  const start = new Date(timeSheet.startDate);
  const end = new Date(timeSheet.endDate);
  const now = new Date();
  if (now > end) return 'error';
  if (now < start) return 'info';
  const daysRemaining = Math.ceil((end - now) / (1000 * 60 * 60 * 24));
  if (daysRemaining <= 2) return 'warning';
  return 'success';
};

const getExpirationLabel = (timeSheet) => {
  if (!timeSheet.startDate || !timeSheet.endDate) return '';
  const start = new Date(timeSheet.startDate);
  const end = new Date(timeSheet.endDate);
  const now = new Date();
  if (now > end) return 'Expirée';
  if (now < start) return 'À venir';
  const daysRemaining = Math.ceil((end - now) / (1000 * 60 * 60 * 24));
  if (daysRemaining <= 2) return `${daysRemaining}j`;
  return 'Active';
};

onUnmounted(() => {
  if (timerInterval.value) {
    clearInterval(timerInterval.value);
  }
});

onMounted(() => {
  loadTimeSheets();
});

watch(selectedGroup, () => {
  if (filteredTimeSheets.value.length === 0) {
    selectedTimeSheet.value = null;
    selectedTask.value = null;
  } else if (selectedTimeSheet.value &&
    !filteredTimeSheets.value.some(ts => ts.id === selectedTimeSheet.value.id)) {
    setSelectedTimeSheet(filteredTimeSheets.value[0]);
  }
});

watch(validityFilter, () => {
  if (filteredTimeSheets.value.length === 0) {
    selectedTimeSheet.value = null;
    selectedTask.value = null;
  } else if (selectedTimeSheet.value &&
    !filteredTimeSheets.value.some(ts => ts.id === selectedTimeSheet.value.id)) {
    setSelectedTimeSheet(filteredTimeSheets.value[0]);
  }
});

watch(confirmedSeconds, updateTimeFields);
watch([hours, minutes, seconds], updateTotalSeconds);
</script>

<style scoped>
.timesheet-card {
  height: 100%;
  display: flex;
  flex-direction: column;
}

/* Adjust select inputs for mobile */
.group-select {
  max-width: 180px;
}
.validity-select {
  max-width: 150px;
}

.timesheet-container {
  flex-grow: 1;
  display: flex;
  flex-direction: column;
  max-height: calc(100vh - 320px);
  overflow-y: auto;
  scrollbar-width: thin;
  padding-right: 8px;
}

.timesheet-container::-webkit-scrollbar {
  width: 8px;
}
.timesheet-container::-webkit-scrollbar-track {
  background: rgba(0, 0, 0, 0.1);
  border-radius: 4px;
}
.timesheet-container::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.3);
  border-radius: 4px;
}
.timesheet-container::-webkit-scrollbar-thumb:hover {
  background: rgba(255, 255, 255, 0.5);
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
  background-color: rgba(76, 175, 80, 0.1) !important;
  transition: background-color 0.3s ease;
}
.task-completed:hover {
  background-color: rgba(76, 175, 80, 0.2) !important;
}
.task-completed .v-list-item-title {
  opacity: 0.7;
}
.selected-task.task-completed {
  background-color: rgba(156, 39, 176, 0.3) !important;
  border-left: 3px solid #e040fb;
}
.selected-task.task-completed:hover {
  background-color: rgba(156, 39, 176, 0.4) !important;
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

.time-display {
  background-color: rgba(26, 35, 126, 0.8);
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
}

.success-notification {
  position: fixed;
  top: 80px;
  right: 20px;
  z-index: 2000;
  max-width: 90%;
  background-color: rgba(76, 175, 80, 0.9);
  color: white;
  padding: 12px 20px;
  border-radius: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
  animation: fadeInOut 3s ease;
}

@keyframes fadeInOut {
  0% { opacity: 0; transform: translateY(-20px); }
  15% { opacity: 1; transform: translateY(0); }
  85% { opacity: 1; transform: translateY(0); }
  100% { opacity: 0; transform: translateY(-20px); }
}

.edit-time-btn {
  opacity: 0.6;
  transition: opacity 0.2s;
}
.edit-time-btn:hover {
  opacity: 1;
}

.fade-enter-active, .fade-leave-active {
  transition: opacity 0.5s;
}
.fade-enter-from, .fade-leave-to {
  opacity: 0;
}

:deep(.v-checkbox-btn) {
  margin-right: 8px;
}
:deep(.v-checkbox-btn--selected .v-selection-control__input) {
  color: #4CAF50 !important;
}

.category-chip {
  font-weight: 600 !important;
  letter-spacing: 0.3px;
  opacity: 0.95;
  box-shadow: 0 1px 3px rgba(0,0,0,0.2);
  text-shadow: 0 1px 2px rgba(0,0,0,0.1);
  padding: 0 8px !important;
}

.personal-indicator {
  background-color: rgba(156, 39, 176, 0.3) !important;
  border: 1px solid rgba(156, 39, 176, 0.7) !important;
  color: white !important;
}

.group-indicator {
  background-color: rgba(124, 77, 255, 0.2) !important;
  color: white !important;
  border: 1px solid rgba(124, 77, 255, 0.5) !important;
}

:deep(.v-chip__content) {
  font-weight: 600 !important;
  color: white !important;
}

.validity-indicator {
  background-color: rgba(0, 0, 0, 0.1);
  border-radius: 4px;
  padding: 8px;
}

.text-error {
  color: #f44336 !important;
}

.text-orange {
  color: #ff9800 !important;
}

/* Mobile-friendly adjustments */
@media (max-width: 600px) {
  .timesheet-card {
    margin: 8px;
    border-radius: 8px;
  }
  .timer {
    font-size: 2rem;
  }
  .selected-task-banner {
    margin-left: 8px;
    padding: 8px 12px;
  }
  .task-selection-indicator {
    right: 10px;
    bottom: 10px;
    font-size: 12px;
    padding: 6px 12px;
  }
}
</style>
