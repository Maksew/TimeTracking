<script setup>
import { computed } from 'vue';
import { useAuthStore } from '@/stores/auth';
import WelcomeMessage from '@/components/dashboard/WelcomeMessage.vue';
import StatsOverview from '@/components/dashboard/StatsOverview.vue';
import DetailedStats from '@/components/dashboard/DetailedStats.vue';
import TimeSheetComponent from '@/components/timesheet/TimeSheetComponent.vue';

const authStore = useAuthStore();
const user = computed(() => authStore.user);
</script>

<template>
  <div class="dashboard">
    <WelcomeMessage :username="user?.pseudo" />

    <StatsOverview />

    <v-row>
      <v-col cols="12" md="7">
        <DetailedStats />
      </v-col>

      <v-col cols="12" md="5">
        <TimeSheetComponent />
      </v-col>
    </v-row>
  </div>
</template>

<style scoped>
.dashboard {
  padding: 0 16px;
}
</style>
