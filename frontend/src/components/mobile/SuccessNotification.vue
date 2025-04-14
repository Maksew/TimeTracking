<template>
  <transition name="fade">
    <div class="success-notification">
      <v-icon class="mr-2" color="white">mdi-check-circle</v-icon>
      {{ message }}
    </div>
  </transition>
</template>

<script setup>
import { defineProps } from 'vue';
const props = defineProps({
  message: String,
});
</script>
