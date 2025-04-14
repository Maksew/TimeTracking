<template>
  <div class="auto-chain-option mb-3">
    <v-checkbox
      v-model="internalValue"
      label="Enchaîner automatiquement les tâches"
      color="white"
      hide-details
    >
      <template v-slot:label>
        <div @click.stop>Enchaîner automatiquement les tâches</div>
      </template>
    </v-checkbox>
  </div>
</template>

<script setup>
import { defineProps, defineEmits, computed } from 'vue';

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false,
  },
});

// Define the event emitter
const emit = defineEmits(['update:modelValue']);

// Create a computed property as a proxy for modelValue
const internalValue = computed({
  get() {
    return props.modelValue;
  },
  set(newValue) {
    emit('update:modelValue', newValue);
  }
});
</script>
