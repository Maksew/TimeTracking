<script setup>
import { ref, computed } from 'vue';
import { useRoute } from 'vue-router';

const route = useRoute();
const activeTab = computed(() => route.name || 'dashboard');

defineProps({
  user: {
    type: Object,
    default: () => ({
      name: '',
      avatar: ''
    })
  }
});
</script>

<template>
  <v-app-bar color="#1a237e" dark flat>
    <v-app-bar-title class="font-weight-bold">Time Tracking</v-app-bar-title>

    <v-btn
      :active="activeTab === 'dashboard'"
      variant="flat"
      color="white"
      class="ml-4"
      to="/"
    >
      Dashboard
    </v-btn>

    <v-btn
      :active="activeTab === 'timesheet'"
      variant="flat"
      color="white"
      to="/timesheet"
    >
      Feuille de temps
    </v-btn>

    <v-spacer></v-spacer>

    <div v-if="user && user.name" class="d-flex align-center">
      <span class="mr-2">{{ user.name }}</span>
      <v-avatar size="36">
        <v-img v-if="user.avatar" :src="user.avatar" alt="User Avatar"></v-img>
        <v-icon v-else>mdi-account</v-icon>
      </v-avatar>
    </div>

    <v-btn v-else variant="outlined" color="white">
      Connexion
    </v-btn>
  </v-app-bar>
</template>
