<template>
  <v-dialog v-model="internalVisible" max-width="400" persistent>
    <v-card color="#283593" dark>
      <v-card-title class="text-h5">
        <v-icon start class="mr-2">mdi-clock-check-outline</v-icon>
        Confirmer le temps
      </v-card-title>
      <v-card-text class="pb-4 pt-4">
        <p class="mb-2">Tâche : <strong>{{ selectedTaskName }}</strong></p>
        <p class="mb-4">Confirmez ou ajustez le temps passé sur cette tâche :</p>
        <div class="time-display text-center mb-6">
          <div class="text-h2 font-weight-bold">
            {{ paddedHours }}:{{ paddedMinutes }}:{{ paddedSeconds }}
          </div>
          <div class="text-caption">Heures:Minutes:Secondes</div>
        </div>
        <v-row>
          <v-col cols="12" sm="4">
            <v-text-field
              v-model.number="localHours"
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
              v-model.number="localMinutes"
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
              v-model.number="localSeconds"
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
        <v-btn color="grey" variant="text" @click="$emit('cancel')">
          Annuler
        </v-btn>
        <v-btn color="primary" @click="handleSave" :disabled="confirmedSeconds <= 0">
          Enregistrer
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script setup>
import { ref, computed, watch, defineProps, defineEmits } from 'vue';

// Define the props coming from the parent
const props = defineProps({
  visible: Boolean,
  hours: Number,
  minutes: Number,
  seconds: Number,
  selectedTaskName: {
    type: String,
    default: ''
  }
});

// Define events that this component can emit
const emit = defineEmits(['update:visible', 'cancel', 'save']);

// Create a computed property that acts as a proxy for the "visible" prop
const internalVisible = computed({
  get() {
    return props.visible;
  },
  set(newValue) {
    emit('update:visible', newValue);
  }
});

// Local values for time fields
const localHours = ref(props.hours);
const localMinutes = ref(props.minutes);
const localSeconds = ref(props.seconds);

// Watch the props to update local time values when changes occur
watch(() => props.hours, newVal => localHours.value = newVal);
watch(() => props.minutes, newVal => localMinutes.value = newVal);
watch(() => props.seconds, newVal => localSeconds.value = newVal);

// Format the time fields with padded zeros
const paddedHours = computed(() => String(localHours.value).padStart(2, '0'));
const paddedMinutes = computed(() => String(localMinutes.value).padStart(2, '0'));
const paddedSeconds = computed(() => String(localSeconds.value).padStart(2, '0'));

// Calculate the total confirmed seconds from the local fields
const confirmedSeconds = computed(() => (localHours.value * 3600) + (localMinutes.value * 60) + localSeconds.value);

// Emit the save event with the confirmed seconds when "Enregistrer" is clicked
const handleSave = () => {
  emit('save', confirmedSeconds.value);
};
</script>
