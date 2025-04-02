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
  <v-app-bar color="#1a237e" dark flat>
    <v-app-bar-title class="font-weight-bold">Time Tracking</v-app-bar-title>

    <template v-if="isAuthenticated">
      <div class="tabs">
        <div
          :class="['tab', { active: activeTab === 'dashboard' }]"
          @click="router.push('/')"
        >
          Dashboard
        </div>

        <div
          :class="['tab', { active: activeTab === 'timesheet' }]"
          @click="router.push('/timesheet')"
        >
          Feuille de temps
        </div>

        <div
          :class="['tab', { active: activeTab === 'groups' }]"
          @click="router.push('/groups')"
        >
          Groupes
        </div>
      </div>
    </template>

    <v-spacer></v-spacer>

    <div v-if="isAuthenticated" class="d-flex align-center">
      <v-btn text to="/profile" class="d-flex align-center">
        <span class="mr-2">{{ user.pseudo }}</span>
        <v-avatar size="36" color="#7986cb" class="mr-2">
          <span class="text-h6" v-if="user.pseudo">{{ user.pseudo.charAt(0).toUpperCase() }}</span>
        </v-avatar>
      </v-btn>

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

<style scoped>
.tabs {
  display: flex;
  gap: 16px;
  align-items: center;
}

.tab {
  padding: 8px 16px;
  color: white;
  font-weight: 600;
  transition: all 0.3s ease;
  text-transform: none;
  border-radius: 12px;
}

.tab:hover {
  background: linear-gradient(to top, rgba(18, 29, 93, 0.5) 0%, rgba(153, 89, 231, 0.5) 77%);
}

.tab.active {
  font-weight: bold;
  background: linear-gradient(to top, rgba(18, 29, 93, 0.5) 0%, rgba(153, 89, 231, 0.5) 77%);
}

.tab:not(.active) {
  font-weight: 600;
}
</style>
