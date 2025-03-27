<template>
  <v-container
    fluid
    class="pa-0 d-flex flex-column"
    style="min-height: 100vh; background-color: #1a237e;"
  >
    <div class="px-6 pt-6 d-flex align-center">
      <h2 class="text-h4 font-weight-medium text-white">Mes feuilles de temps</h2>
      <v-spacer></v-spacer>
      <v-btn color="white" variant="outlined" class="mr-2" @click="showFilters = !showFilters">
        <v-icon class="mr-2">mdi-filter</v-icon>
        Filtres
      </v-btn>
    </div>

    <!-- Filtres (collapsible) -->
    <v-expand-transition>
      <div v-if="showFilters" class="px-6 py-4">
        <v-card color="#283593" class="mb-6">
          <v-card-text>
            <v-row>
              <v-col cols="12" md="6">
                <v-select
                  v-model="selectedGroup"
                  :items="groups"
                  item-title="name"
                  item-value="id"
                  label="Filtrer par groupe"
                  variant="outlined"
                  density="comfortable"
                  bg-color="#1a237e"
                ></v-select>
              </v-col>
              <v-col cols="12" md="6">
                <v-menu
                  v-model="dateMenu"
                  :close-on-content-click="false"
                >
                  <template v-slot:activator="{ props }">
                    <v-text-field
                      v-model="dateRangeText"
                      label="Période"
                      variant="outlined"
                      bg-color="#1a237e"
                      readonly
                      v-bind="props"
                      prepend-inner-icon="mdi-calendar-range"
                    ></v-text-field>
                  </template>
                  <v-date-picker
                    v-model="dateRange"
                    range
                    color="primary"
                    elevation="10"
                  ></v-date-picker>
                </v-menu>
              </v-col>
            </v-row>
            <v-row>
              <v-col cols="12" class="d-flex justify-end">
                <v-btn color="error" variant="text" class="mr-2" @click="clearFilters">
                  Réinitialiser
                </v-btn>
                <v-btn color="primary" @click="applyFilters">
                  Appliquer
                </v-btn>
              </v-col>
            </v-row>
          </v-card-text>
        </v-card>
      </div>
    </v-expand-transition>

    <!-- Subtitle avec le nombre de modèles -->
    <h3 class="white--text px-6">{{ filteredTemplates.length + 1 }} modèles</h3>

    <!-- Feedback de chargement -->
    <div v-if="loading" class="d-flex justify-center align-center py-12">
      <v-progress-circular indeterminate size="64" color="white"></v-progress-circular>
    </div>

    <!-- Boxe grid -->
    <v-row v-else class="px-6" align="start" justify="start" dense>
      <!-- Box 1: Feuille de temps vierge (toujours présent) -->
      <v-col cols="12" sm="6" md="4" lg="3" class="mb-6">
        <v-hover v-slot="{ isHovering, props }">
          <v-card
            v-bind="props"
            :elevation="isHovering ? 8 : 2"
            class="rounded-lg transition-swing"
            height="220"
            to="/editTimeSheet"
            color="#283593"
          >
            <div class="d-flex flex-column align-center justify-center h-100 pa-4">
              <v-icon color="white" size="64" class="mb-4">mdi-plus</v-icon>
              <div class="text-subtitle-1 text-center white--text">
                Feuille de temps vierge
              </div>
            </div>
          </v-card>
        </v-hover>
      </v-col>

      <!-- Templates générés à partir des feuilles existantes (historique) -->
      <v-col
        v-for="template in filteredTemplates"
        :key="template.id"
        cols="12" sm="6" md="4" lg="3"
        class="mb-6"
      >
        <v-hover v-slot="{ isHovering, props }">
          <v-card
            v-bind="props"
            :elevation="isHovering ? 8 : 2"
            class="rounded-lg transition-swing"
            height="220"
            :to="`/editTimeSheet?template=${template.id}`"
            color="#283593"
          >
            <div class="d-flex flex-column align-center justify-center h-100 pa-4">
              <v-icon color="white" size="64" class="mb-4">{{ template.icon || 'mdi-file-document' }}</v-icon>
              <div class="text-subtitle-1 text-center white--text">
                {{ template.title || `Feuille du ${formatDate(template.entryDate)}` }}
              </div>
              <div class="mt-2 text-caption text-center white--text">
                {{ template.timeSheetTasks ? `${template.timeSheetTasks.length} tâches` : 'Aucune tâche' }}
              </div>
            </div>
          </v-card>
        </v-hover>
      </v-col>
    </v-row>

    <!-- Message si aucun résultat après filtrage -->
    <div v-if="!loading && filteredTemplates.length === 0 && hasFilters" class="d-flex justify-center py-8">
      <v-card color="#283593" max-width="500" class="pa-4">
        <v-card-text class="text-center">
          <v-icon size="large" color="white" class="mb-2">mdi-file-search</v-icon>
          <p class="text-h6 white--text">Aucune feuille de temps ne correspond à vos critères</p>
          <v-btn color="primary" class="mt-4" @click="clearFilters">Réinitialiser les filtres</v-btn>
        </v-card-text>
      </v-card>
    </div>
  </v-container>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue';
import timeSheetService from '@/services/timeSheetService';
import groupService from '@/services/groupService';

// Référence aux templates existants (historique des feuilles)
const existingTemplates = ref([]);
const filteredTemplates = ref([]);
const loading = ref(true);

// Groupes pour filtrage
const groups = ref([]);
const selectedGroup = ref(null);

// Filtres date
const dateRange = ref([]);
const dateMenu = ref(false);
const showFilters = ref(false);

// Calcul dynamique du nombre total de modèles
const totalModels = computed(() => {
  // 1 pour le modèle vierge + nombre de templates existants
  return 1 + filteredTemplates.value.length;
});

// Texte pour l'affichage des dates
const dateRangeText = computed(() => {
  if (dateRange.value.length === 0) return 'Toutes les dates';
  if (dateRange.value.length === 1) return `À partir du ${formatDate(dateRange.value[0])}`;
  return `Du ${formatDate(dateRange.value[0])} au ${formatDate(dateRange.value[1])}`;
});

// Détecter si des filtres sont appliqués
const hasFilters = computed(() => {
  return selectedGroup.value !== null || dateRange.value.length > 0;
});

// Charger les données initiales
onMounted(async () => {
  try {
    // Chargement parallèle pour optimisation
    const [timeSheets, userGroups] = await Promise.all([
      timeSheetService.getUserTimeSheets(),
      groupService.getUserGroups()
    ]);

    // Stocker les données
    existingTemplates.value = timeSheets;
    filteredTemplates.value = timeSheets;

    // Préparer les options de filtre pour les groupes
    groups.value = [
      { id: null, name: 'Tous les groupes' },
      ...userGroups
    ];
  } catch (error) {
    console.error('Erreur lors du chargement des templates:', error);
  } finally {
    loading.value = false;
  }
});

// Appliquer les filtres
function applyFilters() {
  let filtered = [...existingTemplates.value];

  // Filtre par groupe
  if (selectedGroup.value) {
    filtered = filtered.filter(template => {
      return template.sharedWithGroups?.some(g => g.groupId === selectedGroup.value);
    });
  }

  // Filtre par date
  if (dateRange.value.length === 2) {
    const startDate = new Date(dateRange.value[0]);
    const endDate = new Date(dateRange.value[1]);

    filtered = filtered.filter(template => {
      const templateDate = new Date(template.entryDate);
      return templateDate >= startDate && templateDate <= endDate;
    });
  } else if (dateRange.value.length === 1) {
    const startDate = new Date(dateRange.value[0]);

    filtered = filtered.filter(template => {
      const templateDate = new Date(template.entryDate);
      return templateDate >= startDate;
    });
  }

  filteredTemplates.value = filtered;
  showFilters.value = false;
}

// Réinitialiser les filtres
function clearFilters() {
  selectedGroup.value = null;
  dateRange.value = [];
  filteredTemplates.value = existingTemplates.value;
}

// Formater une date pour l'affichage
function formatDate(dateString) {
  if (!dateString) return '';
  const date = new Date(dateString);
  return date.toLocaleDateString('fr-FR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric'
  });
}

// Fermer le sélecteur de date quand la date est sélectionnée (pour le range)
watch(dateRange, (newVal) => {
  if (newVal.length === 2) {
    dateMenu.value = false;
  }
});
</script>

<style scoped>
.transition-swing {
  transition: box-shadow 0.3s, transform 0.3s;
}

.v-card:hover {
  transform: translateY(-5px);
}
</style>
