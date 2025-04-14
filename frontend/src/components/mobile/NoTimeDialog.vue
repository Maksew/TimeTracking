<template>
  <v-dialog v-model="internalVisible" max-width="400">
    <v-card color="#283593" dark>
      <v-card-title class="headline">
        <v-icon start class="mr-2" color="warning">mdi-alert-circle</v-icon>
        Aucun temps enregistré
      </v-card-title>
      <v-card-text>
        <p>Cette tâche n'a pas de temps enregistré.</p>
        <p class="mt-2">
          Vous devez d'abord enregistrer du temps avec le chronomètre ou éditer manuellement la durée pour pouvoir la marquer comme complétée.
        </p>
        <p class="text-subtitle-2 mt-3" v-if="task">
          <strong>{{ task.name }}</strong>
        </p>
      </v-card-text>
      <v-divider></v-divider>
      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn color="grey" variant="text" @click="$emit('close')">
          Fermer
        </v-btn>
        <v-btn color="primary" @click="$emit('edit')">
          <v-icon start class="mr-1">mdi-pencil</v-icon>
          Éditer le temps
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script setup>
import { computed } from 'vue';

// Define props
const props = defineProps({
  visible: Boolean,
  task: Object,
});

// Define events that this component emits
const emit = defineEmits(['update:visible', 'close', 'edit']);

// Create a computed property as a proxy for the "visible" prop
const internalVisible = computed({
  get() {
    return props.visible;
  },
  set(newValue) {
    // Emit an event so that the parent component can update its state
    emit('update:visible', newValue);
  }
});
</script>
