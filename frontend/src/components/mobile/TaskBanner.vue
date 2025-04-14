<template>
  <div v-if="selectedTask" class="selected-task-banner mx-4 mb-4">
    <div class="d-flex align-center">
      <v-icon size="small" class="mr-2">{{ selectedTimeSheet?.icon || 'mdi-file-document' }}</v-icon>
      <span class="font-weight-medium">{{ selectedTimeSheet?.title }}</span>
      <v-icon class="mx-2" size="small">mdi-chevron-right</v-icon>
      <span class="task-name">{{ selectedTask.name }}</span>
    </div>
  </div>
  <div v-if="selectedTask" class="task-selection-indicator">
    <span>Tâche sélectionnée : {{ selectedTask.name }}</span>
  </div>
</template>

<script setup>
import { defineProps } from 'vue';
const props = defineProps({
  selectedTask: Object,
  selectedTimeSheet: Object,
});
</script>
