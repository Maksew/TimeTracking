<template>
  <div v-if="selectedGroup || filteredCount > 0" class="group-label mb-3">
    <span class="font-weight-medium" :class="{'personal-label': selectedGroup === 'personnel'}">
      {{ currentGroupLabel }}
      <span v-if="filteredCount > 0">
        - {{ filteredCount }} {{ filteredCount > 1 ? 'feuilles' : 'feuille' }}
      </span>
    </span>
  </div>
</template>

<script setup>
import { defineProps } from 'vue';
const props = defineProps({
  selectedGroup: null,
  currentGroupLabel: String,
  filteredCount: Number,
});
</script>
