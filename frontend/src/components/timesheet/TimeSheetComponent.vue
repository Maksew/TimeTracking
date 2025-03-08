<script setup>
import { ref } from 'vue';

const selectedGroup = ref(null);
const currentTime = ref('00:00:00');
const completion = ref(0);

const timesheetTasks = ref([]);
const taskGroups = ref([]);

defineProps({
  tasks: {
    type: Array,
    default: () => []
  },
  groups: {
    type: Array,
    default: () => []
  }
});
</script>

<template>
  <v-card color="#283593" dark class="rounded-lg">
    <v-card-title class="d-flex align-center">
      <v-icon size="small" class="mr-2">mdi-file-document-outline</v-icon>
      Feuille de temps
    </v-card-title>

    <v-divider></v-divider>

    <div class="d-flex justify-center align-center py-4">
      <div class="text-h3 font-weight-bold">{{ currentTime }}</div>
      <v-btn icon class="ml-4" color="purple">
        <v-icon>mdi-stop</v-icon>
      </v-btn>
    </div>

    <v-card-text>
      <v-select
        v-model="selectedGroup"
        :items="taskGroups"
        item-title="title"
        item-value="value"
        variant="outlined"
        density="compact"
        label="Groupe"
        bg-color="#1a237e"
        hide-details
        class="max-width-180 mb-3"
      ></v-select>

      <div class="mt-2">
        <template v-if="timesheetTasks.length > 0">
          <div v-for="(category, index) in timesheetTasks" :key="index">
            <div class="text-subtitle-1 d-flex align-center">
              {{ category.category }}
              <span class="text-caption ml-2" v-if="category.incompleteTasks > 0">
                - {{ category.incompleteTasks }} tâches incomplètes
              </span>
            </div>

            <v-expansion-panels>
              <v-expansion-panel
                bg-color="#1a237e"
              >
                <v-expansion-panel-title>
                  <v-icon size="small" class="mr-2">{{ category.icon || 'mdi-folder' }}</v-icon>
                  {{ category.category }}
                </v-expansion-panel-title>

                <v-expansion-panel-text>
                  <v-list bg-color="transparent">
                    <v-list-item
                      v-for="(task, i) in category.tasks"
                      :key="i"
                      :class="{ 'completed-task': task.completed }"
                    >
                      <template v-slot:prepend>
                        <v-checkbox-btn
                          v-model="task.completed"
                          color="white"
                          hide-details
                        ></v-checkbox-btn>
                      </template>

                      <v-list-item-title>{{ task.name }}</v-list-item-title>

                      <template v-slot:append>
                        <span>{{ task.time }}</span>
                      </template>
                    </v-list-item>
                  </v-list>
                </v-expansion-panel-text>
              </v-expansion-panel>
            </v-expansion-panels>
          </div>
        </template>

        <template v-else>
          <div class="text-center py-8">
            <v-icon size="large" class="mb-2">mdi-file-document-outline</v-icon>
            <p>Aucune feuille de temps disponible</p>
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

.completed-task {
  opacity: 0.7;
}

:deep(.v-expansion-panel-text__wrapper) {
  padding: 0;
}
</style>
