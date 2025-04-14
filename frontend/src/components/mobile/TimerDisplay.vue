<template>
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
        @click="$emit('startTimer')"
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
        @click="$emit('stopTimer')"
      >
        <v-icon>mdi-stop</v-icon>
        <v-tooltip activator="parent" location="bottom">Arrêter</v-tooltip>
      </v-btn>
    </template>
  </div>
</template>

<script setup>
import { defineProps } from 'vue';
const props = defineProps({
  timerRunning: Boolean,
  formattedTimerDisplay: String,
  selectedTask: Object,
});
</script>
