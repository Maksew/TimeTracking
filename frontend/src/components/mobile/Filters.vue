<template>
  <div class="d-flex flex-wrap mb-3">
    <!-- Use the computed property as the v-model -->
    <v-select
      v-model="internalSelectedGroup"
      :items="groupOptions"
      item-title="title"
      item-value="value"
      variant="outlined"
      density="compact"
      label="Groupe"
      bg-color="#1a237e"
      hide-details
      class="group-select mr-2"
      @change="$emit('filterChange')"
    ></v-select>

    <v-select
      v-model="internalValidityFilter"
      :items="validityFilterOptions"
      variant="outlined"
      density="compact"
      label="Statut"
      bg-color="#1a237e"
      hide-details
      class="validity-select"
      @change="$emit('filterChange')"
    ></v-select>
  </div>
</template>

<script setup>
import { computed } from 'vue';

// Define the props
const props = defineProps({
  selectedGroup: {
    type: [String, Number, null],
    default: null,
  },
  validityFilter: {
    type: String,
    default: 'all'
  },
  groupOptions: Array,
  validityFilterOptions: Array,
});

// Define the events that will be emitted (for updating the parent's state)
const emit = defineEmits([
  'update:selectedGroup',
  'update:validityFilter',
  'filterChange'
]);

// Create computed properties to proxy the props.
// When updated locally, these computed properties emit events to update the parent's state.
const internalSelectedGroup = computed({
  get() {
    return props.selectedGroup;
  },
  set(newValue) {
    emit('update:selectedGroup', newValue);
  }
});

const internalValidityFilter = computed({
  get() {
    return props.validityFilter;
  },
  set(newValue) {
    emit('update:validityFilter', newValue);
  }
});
</script>
