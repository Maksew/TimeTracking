<template>
  <v-dialog v-model="internalVisible" max-width="400" persistent>
    <v-card color="#283593" dark>
      <v-card-title class="headline">
        <v-icon start class="mr-2">mdi-timer-off</v-icon>
        Réinitialiser le temps
      </v-card-title>
      <v-card-text>
        <p>Voulez-vous réinitialiser le temps passé sur cette tâche ?</p>
        <p class="text-subtitle-2 mt-2" v-if="task">
          <strong>{{ task.name }}</strong> - Temps actuel : {{ formatTime(task.duration) }}
        </p>
        <p class="text-caption mt-3">
          <v-icon size="small" color="warning" class="mr-1">mdi-alert-circle</v-icon>
          Cette action ne peut pas être annulée.
        </p>
      </v-card-text>
      <v-divider></v-divider>
      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn color="grey" variant="text" @click="$emit('cancel')">
          Annuler
        </v-btn>
        <v-btn color="error" @click="$emit('confirm')">
          Réinitialiser
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script setup>
import { computed } from 'vue';
import { defineProps, defineEmits } from 'vue';

// Define incoming props
const props = defineProps({
  visible: Boolean,
  task: Object,
  timeSheet: Object,
});

// Define events to emit (including update for the visible prop)
const emit = defineEmits(['update:visible', 'cancel', 'confirm']);

// Create a computed property that acts as a proxy for the "visible" prop
const internalVisible = computed({
  get() {
    return props.visible;
  },
  set(newValue) {
    emit('update:visible', newValue);
  }
});

// Optionally, you can either define or import a helper function like formatTime
// For instance, here's a simple formatTime function:
const formatTime = (timeValue) => {
  if (timeValue == null) return '00:00:00';
  const totalSeconds = timeValue;
  const hrs = Math.floor(totalSeconds / 3600);
  const mins = Math.floor((totalSeconds % 3600) / 60);
  const secs = totalSeconds % 60;
  return `${String(hrs).padStart(2, '0')}:${String(mins).padStart(2, '0')}:${String(secs).padStart(2, '0')}`;
};
</script>
