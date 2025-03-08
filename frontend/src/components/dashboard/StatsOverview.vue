<script setup>
defineProps({
  tasksToDo: {
    type: Number,
    default: 0
  },
  tasksCompleted: {
    type: Number,
    default: 0
  },
  timeWorkedToday: {
    type: String,
    default: '00:00:00'
  }
});
</script>

<template>
  <v-row class="mx-1 my-4">
    <v-col cols="12" sm="4">
      <v-card color="#283593" dark class="pa-3 rounded-lg">
        <div class="d-flex">
          <v-avatar color="#1a237e" size="48" class="mr-4">
            <v-icon>mdi-close-box-outline</v-icon>
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
            <v-icon>mdi-checkbox-outline</v-icon>
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
