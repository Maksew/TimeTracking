<script setup>
import { ref, onMounted, computed } from 'vue';
import timeSheetService from '@/services/timeSheetService';

const timeSheets = ref([]);
const loading = ref(true);

onMounted(async () => {
  try {
    timeSheets.value = await timeSheetService.getUserTimeSheets();
  } catch (error) {
    console.error('Erreur lors du chargement des feuilles de temps:', error);
  } finally {
    loading.value = false;
  }
});

// Calcul des statistiques
const tasksToDo = computed(() => {
  if (!timeSheets.value.length) return 0;
  return timeSheets.value.reduce((total, sheet) => {
    return total + (sheet.timeSheetTasks?.filter(task => !task.completed)?.length || 0);
  }, 0);
});

const tasksCompleted = computed(() => {
  if (!timeSheets.value.length) return 0;
  return timeSheets.value.reduce((total, sheet) => {
    return total + (sheet.timeSheetTasks?.filter(task => task.completed)?.length || 0);
  }, 0);
});

const timeWorkedToday = computed(() => {
  if (!timeSheets.value.length) return '00:00:00';

  // Filtrer les feuilles d'aujourd'hui
  const today = new Date().toISOString().split('T')[0];
  const todaySheets = timeSheets.value.filter(sheet =>
    sheet.entryDate.startsWith(today)
  );

  // Calculer le temps total en minutes
  const totalMinutes = todaySheets.reduce((total, sheet) => {
    return total + sheet.timeSheetTasks.reduce((sum, task) => sum + (task.duration || 0), 0);
  }, 0);

  // Convertir en format hh:mm:ss
  const hours = Math.floor(totalMinutes / 60);
  const minutes = totalMinutes % 60;
  return `${String(hours).padStart(2, '0')}:${String(minutes).padStart(2, '0')}:00`;
});
</script>

<template>
  <v-row class="mx-1 my-4">
    <v-col cols="12" sm="4">
      <v-card color="#283593" dark class="pa-3 rounded-lg">
        <div class="d-flex">
          <v-avatar color="#1a237e" size="48" class="mr-4">
            <v-icon>mdi-clipboard-text-outline</v-icon>
          </v-avatar>
          <div>
            <div class="text-h5 font-weight-bold">{{ tasksToDo }}</div>
            <div class="text-caption">Tâches à effectuer</div>
          </div>
        </div>
      </v-card>
    </v-col>

    <v-col cols="12" sm="4">
      <v-card color="#283593" dark class="pa-3 rounded-lg">
        <div class="d-flex">
          <v-avatar color="#1a237e" size="48" class="mr-4">
            <v-icon>mdi-check-circle-outline</v-icon>
          </v-avatar>
          <div>
            <div class="text-h5 font-weight-bold">{{ tasksCompleted }}</div>
            <div class="text-caption">Tâches effectuées</div>
          </div>
        </div>
      </v-card>
    </v-col>

    <v-col cols="12" sm="4">
      <v-card color="#283593" dark class="pa-3 rounded-lg">
        <div class="d-flex">
          <v-avatar color="#1a237e" size="48" class="mr-4">
            <v-icon>mdi-clock-outline</v-icon>
          </v-avatar>
          <div>
            <div class="text-h5 font-weight-bold">{{ timeWorkedToday }}</div>
            <div class="text-caption">Temps total aujourd'hui</div>
          </div>
        </div>
      </v-card>
    </v-col>
  </v-row>
</template>
