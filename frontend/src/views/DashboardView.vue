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
const timeSheets = ref([]);
const userGroups = ref([]);

onMounted(async () => {
  try {
    // Chargement initial des données
    timeSheets.value = await timeSheetService.getUserTimeSheets();
    userGroups.value = await groupService.getUserGroups();
  } catch (error) {
    console.error("Erreur lors du chargement des données:", error);
  } finally {
    loading.value = false;
  }
});
</script>

<template>
  <div class="dashboard">
    <WelcomeMessage :username="user?.pseudo" />

    <v-overlay v-if="loading" class="d-flex align-center justify-center">
      <v-progress-circular indeterminate color="primary"></v-progress-circular>
    </v-overlay>

    <template v-else>
      <StatsOverview />

      <v-row>
        <v-col cols="12" md="7">
          <DetailedStats />
        </v-col>

        <v-col cols="12" md="5">
          <TimeSheetComponent :tasks="timeSheets" :groups="userGroups" />
        </v-col>
      </v-row>
    </template>
  </div>
</template>
