<template>
  <div class="timesheet-container">
    <template v-if="timeSheets.length > 0">
      <v-expansion-panels v-model="localExpandedPanels" multiple>
        <v-expansion-panel
          v-for="(timeSheet, index) in timeSheets"
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
                @click="$emit('selectTask', timeSheet, task)"
                :disabled="processingTask"
              >
                <template v-slot:prepend>
                  <v-checkbox-btn
                    :model-value="task.completed || task.duration > 0"
                    color="success"
                    hide-details
                    @click.stop="$emit('toggleTaskCompleted', task, !(task.completed || task.duration > 0))"
                  ></v-checkbox-btn>
                </template>
                <v-list-item-title :class="{ 'text-decoration-line-through': task.completed || task.duration > 0 }">
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
                      @click.stop="$emit('editTaskTime', timeSheet, task)"
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
</template>

<script setup>
import { ref } from 'vue';
import { defineProps } from 'vue';
const props = defineProps({
  timeSheets: Array,
  processingTask: Boolean,
  getGroupColor: Function,
  getGroupName: Function,
  getExpirationColor: Function,
  getExpirationLabel: Function,
  getTimeRemainingText: Function,
  getTimeRemainingClass: Function,
  getProgressPercentage: Function,
  getProgressColor: Function,
  formatDate: Function,
  formatTime: Function,
  isTaskSelected: Function,
});
const localExpandedPanels = ref([]);
</script>
