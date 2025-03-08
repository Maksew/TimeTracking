<script setup>
import { ref } from 'vue';

const selectedTab = ref('global');
const selectedGroup = ref(null);

const groups = ref([]);

const taskStats = ref([]);

defineProps({
  categories: {
    type: Array,
    default: () => []
  }
});
</script>

<template>
  <v-card color="#283593" dark class="rounded-lg">
    <v-card-title class="d-flex align-center">
      <v-icon size="small" class="mr-2">mdi-chart-bar</v-icon>
      Statistique
      <v-spacer></v-spacer>
      <v-btn variant="text" icon="mdi-download" class="ml-2">
        <v-tooltip activator="parent" location="top">Exporter</v-tooltip>
      </v-btn>
    </v-card-title>

    <v-divider></v-divider>

    <v-card-text>
      <div class="d-flex align-center">
        <div class="tabs-container">
          <v-btn
            variant="text"
            :color="selectedTab === 'global' ? 'white' : 'grey'"
            @click="selectedTab = 'global'"
          >
            Global
          </v-btn>
          <v-btn
            variant="text"
            :color="selectedTab === 'detailed' ? 'white' : 'grey'"
            @click="selectedTab = 'detailed'"
          >
            Détaillé
          </v-btn>
        </div>

        <v-spacer></v-spacer>

        <v-select
          v-model="selectedGroup"
          :items="groups"
          item-title="title"
          item-value="value"
          variant="outlined"
          density="compact"
          label="Groupe"
          bg-color="#1a237e"
          hide-details
          class="max-width-180"
        ></v-select>
      </div>

      <div class="mt-4">
        <template v-if="taskStats.length > 0">
          <div v-for="(category, index) in taskStats" :key="index" class="mb-4">
            <div class="text-subtitle-1 mb-2">{{ category.category }}</div>

            <div v-for="(task, taskIndex) in category.tasks" :key="taskIndex" class="mb-3">
              <div class="d-flex align-center mb-1">
                <v-icon size="small" class="mr-2">{{ task.icon }}</v-icon>
                <span>{{ task.name }}</span>
                <v-spacer></v-spacer>
                <span>{{ task.time }}</span>
              </div>

              <v-progress-linear
                :model-value="task.completion"
                height="10"
                rounded
                :color="task.completion >= 100 ? 'green' : 'purple'"
              >
                <template v-slot:default>
                  <span class="text-caption">{{ task.completion }}%</span>
                </template>
              </v-progress-linear>

              <div class="d-flex justify-end mt-1">
                <v-btn size="x-small" variant="text" color="purple-lighten-3">
                  Détail
                </v-btn>
              </div>
            </div>
          </div>
        </template>

        <template v-else>
          <div class="text-center py-8">
            <v-icon size="large" class="mb-2">mdi-chart-bar</v-icon>
            <p>Aucune donnée statistique disponible</p>
          </div>
        </template>
      </div>
    </v-card-text>
  </v-card>
</template>

<style scoped>
.max-width-180 {
  max-width: 180px;
}

.tabs-container {
  border-bottom: 1px solid rgba(255, 255, 255, 0.12);
}
</style>
