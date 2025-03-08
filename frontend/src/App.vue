<script setup>
import { RouterView } from 'vue-router';
import AppHeader from '@/components/layout/AppHeader.vue';
</script>

<template>
  <v-app theme="dark">
    <AppHeader />

    <v-main class="bg-indigo-darken-4">
      <RouterView />
    </v-main>
  </v-app>
</template>

<style>

</style>
