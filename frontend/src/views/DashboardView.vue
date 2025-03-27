<script setup>
import { ref, onMounted, computed } from 'vue';
import { useAuthStore } from '@/stores/auth';
import WelcomeMessage from '@/components/dashboard/WelcomeMessage.vue';
import StatsOverview from '@/components/dashboard/StatsOverview.vue';
import DetailedStats from '@/components/dashboard/DetailedStats.vue';
import TimeSheetComponent from '@/components/timesheet/TimeSheetComponent.vue';

import timeSheetService from '@/services/timeSheetService';
import groupService from '@/services/groupService';

const authStore = useAuthStore();
const user = computed(() => authStore.user);

// États pour le chargement et les données
const loading = ref(true);
const error = ref(null);
const timeSheets = ref([]);
const userGroups = ref([]);
const statistics = ref(null);

onMounted(async () => {
  try {
    // Chargement parallèle des données pour optimiser le temps de chargement
    const [timeSheetsData, userGroupsData] = await Promise.all([
      timeSheetService.getUserTimeSheets(),
      groupService.getUserGroups()
    ]);

    timeSheets.value = timeSheetsData;
    userGroups.value = userGroupsData;
  } catch (err) {
    console.error("Erreur lors du chargement des données:", err);
    error.value = "Une erreur est survenue lors du chargement des données.";
  } finally {
    loading.value = false;
  }
});
</script>

<template>
  <div class="dashboard px-6 py-4">
    <WelcomeMessage :username="user?.pseudo" />

    <v-overlay v-if="loading" class="d-flex align-center justify-center">
      <v-progress-circular indeterminate color="primary" size="64"></v-progress-circular>
    </v-overlay>

    <v-alert v-if="error && !loading" type="error" class="mb-4">
      {{ error }}
      <template v-slot:append>
        <v-btn variant="text" @click="error = null">Fermer</v-btn>
      </template>
    </v-alert>

    <template v-if="!loading && !error">
      <StatsOverview />

      <v-row>
        <v-col cols="12" lg="7" md="7">
          <DetailedStats />
        </v-col>

        <v-col cols="12" lg="5" md="5">
          <TimeSheetComponent :tasks="timeSheets" :groups="userGroups" />
        </v-col>
      </v-row>
    </template>
  </div>
</template>

<style scoped>
.dashboard {
  background-color: #1a237e;
  min-height: calc(100vh - 64px); /* Full height minus app bar */
}
</style>
