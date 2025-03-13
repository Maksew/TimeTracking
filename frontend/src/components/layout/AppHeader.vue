<script setup>
import { ref, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();

const activeTab = computed(() => route.name || 'dashboard');
const isAuthenticated = computed(() => authStore.isAuthenticated);
const user = computed(() => authStore.user);

const logout = () => {
  authStore.logout();
  router.push('/login');
};
</script>

<template>
  <v-app-bar color="#1a237e" dark flat>
    <v-app-bar-title class="font-weight-bold">Time Tracking</v-app-bar-title>

    <template v-if="isAuthenticated">
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
    </template>

    <v-spacer></v-spacer>

    <div v-if="isAuthenticated" class="d-flex align-center">
      <span class="mr-2">{{ user.pseudo }}</span>
      <v-avatar size="36" color="#7986cb" class="mr-2">
        <span class="text-h6" v-if="user.pseudo">{{ user.pseudo.charAt(0).toUpperCase() }}</span>
      </v-avatar>

      <v-btn icon @click="logout">
        <v-icon>mdi-logout</v-icon>
        <v-tooltip activator="parent" location="bottom">Se déconnecter</v-tooltip>
      </v-btn>
    </div>

    <div v-else class="d-flex align-center">
      <v-btn variant="text" color="white" to="/login">
        Connexion
      </v-btn>
      <v-btn variant="outlined" color="white" to="/register" class="ml-2">
        Inscription
      </v-btn>
    </div>
  </v-app-bar>
</template>
