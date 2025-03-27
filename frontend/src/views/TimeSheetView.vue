<template>
  <v-container
    fluid
    class="pa-0 d-flex flex-column"
    style="min-height: 100vh; background-color: #1a237e;"
  >
    <!-- Title text dynamique -->
    <h3 class="white--text px-6 pt-6">{{ totalModels }} modèles</h3>

    <!-- Boxes row -->
    <v-row class="px-6" align="start" justify="start" dense>
      <!-- Box 1: Feuille de temps vierge (toujours présent) -->
      <v-col cols="12" sm="6" md="3" class="mb-6">
        <router-link to="/editTimeSheet" style="text-decoration: none;">
          <v-card
            class="rounded-lg d-flex flex-column align-center justify-center py-12 px-4"
            height="200"
            color="#283593"
            elevation="1"
          >
            <v-icon color="white" size="48" class="mb-4">mdi-plus</v-icon>
            <div class="text-subtitle-1 text-center white--text">
              Feuille de temps vierge
            </div>
          </v-card>
        </router-link>
      </v-col>

      <!-- Templates générés à partir des feuilles existantes (historique) -->
      <v-col
        v-for="template in existingTemplates"
        :key="template.id"
        cols="12" sm="6" md="3"
        class="mb-6"
      >
        <router-link :to="`/editTimeSheet?template=${template.id}`" style="text-decoration: none;">
          <v-card
            class="rounded-lg d-flex flex-column align-center justify-center py-12 px-4"
            height="200"
            color="#283593"
            elevation="1"
          >
            <v-icon color="white" size="48" class="mb-4">{{ template.icon || 'mdi-file-document' }}</v-icon>
            <div class="text-subtitle-1 text-center white--text">
              {{ template.title || `Feuille du ${formatDate(template.entryDate)}` }}
            </div>
          </v-card>
        </router-link>
      </v-col>
    </v-row>
  </v-container>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import timeSheetService from '@/services/timeSheetService';

// Référence aux templates existants (historique des feuilles)
const existingTemplates = ref([]);
const loading = ref(true);

// Calcul dynamique du nombre total de modèles
const totalModels = computed(() => {
  // 1 pour le modèle vierge + nombre de templates existants
  return 1 + existingTemplates.value.length;
});

// Charger les templates existants
onMounted(async () => {
  try {
    // Récupérer les feuilles de temps existantes
    const timeSheets = await timeSheetService.getUserTimeSheets();

    // Utiliser ces feuilles comme templates
    existingTemplates.value = timeSheets;
  } catch (error) {
    console.error('Erreur lors du chargement des templates:', error);
  } finally {
    loading.value = false;
  }
});

// Formatage de la date pour affichage
function formatDate(dateString) {
  if (!dateString) return '';
  const date = new Date(dateString);
  return date.toLocaleDateString('fr-FR');
}
</script>

<style scoped>
</style>
