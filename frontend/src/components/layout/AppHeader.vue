<script setup>
import { computed } from 'vue';
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
  <v-app-bar color="#1a237e" dark flat class="app-header">
    <v-app-bar-title class="font-weight-bold">Time Tracking</v-app-bar-title>

    <template v-if="isAuthenticated">
      <div class="nav-tabs">
        <div
          :class="['nav-tab', { active: activeTab === 'dashboard' }]"
          @click="router.push('/')"
        >
          Dashboard
        </div>

        <div
          :class="['nav-tab', { active: activeTab === 'timesheet' }]"
          @click="router.push('/timesheet')"
        >
          Feuille de temps
        </div>

        <div
          :class="['nav-tab', { active: activeTab === 'groups' }]"
          @click="router.push('/groups')"
        >
          Groupes
        </div>
      </div>
    </template>

    <v-spacer></v-spacer>

    <div v-if="isAuthenticated" class="d-flex align-center">
      <span class="user-name mr-2">{{ user && user.pseudo ? user.pseudo : 'TEST' }}</span>
      <v-avatar color="#7986cb" size="36" class="mr-3">
        <span class="text-h6" v-if="user && user.pseudo">{{ user.pseudo.charAt(0).toUpperCase() }}</span>
        <span class="text-h6" v-else>T</span>
      </v-avatar>

      <v-btn icon class="logout-btn" @click="logout">
        <v-icon>mdi-logout</v-icon>
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

<style scoped>
.app-header {
  border-bottom: none !important;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.4) !important;
  /* Assurer une hauteur fixe pour la barre de navigation */
  height: 64px !important;
}

.nav-tabs {
  display: flex;
  margin-left: 20px;
}

.nav-tab {
  padding: 8px 16px;
  color: white;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  margin: 0 4px;
  border-radius: 8px;
}

.nav-tab:hover {
  background: rgba(153, 89, 231, 0.2);
}

.nav-tab.active {
  font-weight: bold;
  background: linear-gradient(to top, rgba(18, 29, 93, 0.5) 0%, rgba(153, 89, 231, 0.5) 77%);
}

.user-name {
  font-weight: 500;
}

.logout-btn {
  opacity: 0.8;
  transition: opacity 0.2s;
}

.logout-btn:hover {
  opacity: 1;
}
</style>
