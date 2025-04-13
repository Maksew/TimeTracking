<template>
  <div class="timesheet-view" style="background-color: #1a237e; min-height: 100vh;">
    <!-- En-tête avec titre principal -->
    <div class="px-4 py-3">
      <h1 class="text-h4 font-weight-bold white--text mb-3">Mes feuilles de temps</h1>

      <!-- Barre de filtres -->
      <div class="filter-bar mb-6">
        <div class="d-flex flex-wrap align-center">
          <div class="filter-group mr-2 mb-2">
            <v-select
              v-model="selectedGroupFilter"
              :items="groupFilterOptions"
              item-title="name"
              item-value="id"
              label="Filtrer par groupe"
              variant="outlined"
              density="compact"
              bg-color="#1e2a82"
              class="filter-select"
              hide-details
            ></v-select>
          </div>

          <div class="filter-date mr-2 mb-2">
            <v-menu
              v-model="dateMenu"
              :close-on-content-click="false"
            >
              <template v-slot:activator="{ props }">
                <v-text-field
                  v-model="dateRangeText"
                  label="Période"
                  variant="outlined"
                  bg-color="#1e2a82"
                  density="compact"
                  readonly
                  v-bind="props"
                  prepend-inner-icon="mdi-calendar-range"
                  hide-details
                  class="filter-select"
                ></v-text-field>
              </template>
              <v-date-picker
                v-model="dateRange"
                range
                color="primary"
                elevation="10"
              ></v-date-picker>
            </v-menu>
          </div>

          <v-btn
            variant="tonal"
            color="error"
            size="small"
            class="mr-2 mb-2"
            @click="clearFilters"
          >
            RÉINITIALISER
          </v-btn>

          <v-btn
            color="primary"
            size="small"
            class="mb-2"
            @click="applyFilters"
          >
            APPLIQUER
          </v-btn>
        </div>
      </div>
    </div>

    <!-- Compteur de feuilles filtrées -->
    <div class="px-4 mb-4">
      <p class="text-body-1 font-weight-medium white--text">
        {{ totalTemplatesCount }} feuilles de temps filtrées
      </p>
    </div>

    <!-- Bouton Créer une nouvelle feuille -->
    <div class="px-4 mb-4">
      <div class="new-sheet-button mb-4" @click="$router.push('/editTimeSheet')">
        <v-icon color="white" size="24" class="mr-2">mdi-plus-circle</v-icon>
        <span class="white--text font-weight-medium">Créer une nouvelle feuille</span>
      </div>
    </div>

    <!-- Feedback de chargement -->
    <div v-if="loading" class="d-flex justify-center align-center py-12">
      <v-progress-circular indeterminate size="64" color="white"></v-progress-circular>
    </div>

    <!-- Vue organisée des feuilles de temps par groupe -->
    <div v-else>
      <!-- Feuille de temps vierge -->
      <div class="px-4">
        <v-row dense>
          <v-col cols="12" sm="6" md="4" lg="3" class="mb-4">
            <v-card
              class="timesheet-card"
              to="/editTimeSheet"
              color="#283593"
              elevation="2"
              height="180"
            >
              <div class="d-flex flex-column align-center justify-center h-100 pa-4">
                <v-icon color="white" size="56" class="mb-2">mdi-plus</v-icon>
                <div class="text-h6 text-center white--text font-weight-medium">
                  Feuille de temps vierge
                </div>
              </div>
            </v-card>
          </v-col>
        </v-row>
      </div>

      <!-- Feuilles Personnelles -->
      <div v-if="personalTemplates.length > 0" class="timesheet-section mb-4">
        <div class="section-title px-4 py-2 d-flex align-center mb-3">
          <v-icon color="white" size="small" class="mr-2">mdi-account</v-icon>
          <span class="text-h6 white--text">Feuilles Personnelles</span>
          <span class="count-badge ml-2">({{ personalTemplates.length }})</span>
        </div>

        <div class="px-4">
          <v-row dense>
            <v-col
              v-for="template in personalTemplates"
              :key="`personal-${template.id}`"
              cols="12" sm="6" md="4" lg="3"
              class="mb-4"
            >
              <v-card
                class="timesheet-card"
                height="180"
                :to="`/editTimeSheet?template=${template.id}`"
                elevation="2"
                color="#283593"
              >
                <div class="card-content">
                  <div class="d-flex justify-space-between align-center mb-2">
                    <v-icon :icon="template.icon || 'mdi-file-document'" size="32" color="white"></v-icon>
                    <v-chip size="small" color="purple" variant="elevated" class="personal-chip">Personnel</v-chip>
                  </div>

                  <h3 class="text-h6 white--text mb-2">{{ template.title || `Feuille du ${formatDate(template.entryDate)}` }}</h3>

                  <div class="d-flex align-center mt-3">
                    <v-icon size="small" color="white" class="mr-1">mdi-checkbox-marked-circle-outline</v-icon>
                    <span class="text-caption white--text">
                      {{ template.timeSheetTasks ? `${template.timeSheetTasks.length} tâches` : 'Aucune tâche' }}
                    </span>
                  </div>

                  <div class="d-flex align-center mt-1">
                    <v-icon size="small" color="grey-lighten-1" class="mr-1">mdi-calendar</v-icon>
                    <span class="text-caption grey--text text--lighten-1">
                      Créée le {{ formatDate(template.entryDate) }}
                    </span>
                  </div>
                </div>
              </v-card>
            </v-col>
          </v-row>
        </div>
      </div>

      <!-- Feuilles par Groupe -->
      <div
        v-for="(groupData, index) in groupedTemplates"
        :key="`group-${index}`"
        class="timesheet-section mb-6"
      >
        <div
          class="section-title px-4 py-2 d-flex align-center mb-3"
          :style="`background-color: ${getGroupColor(groupData.groupId)}`"
        >
          <v-icon color="white" size="small" class="mr-2">mdi-account-group</v-icon>
          <span class="text-h6 white--text">{{ groupData.name }}</span>
          <span class="count-badge ml-2">({{ groupData.templates.length }})</span>
        </div>

        <div class="px-4">
          <v-row dense>
            <v-col
              v-for="template in groupData.templates"
              :key="`group-${groupData.groupId}-template-${template.id}`"
              cols="12" sm="6" md="4" lg="3"
              class="mb-4"
            >
              <v-card
                class="timesheet-card"
                height="180"
                :to="`/editTimeSheet?template=${template.id}`"
                elevation="2"
                color="#283593"
              >
                <div class="card-content">
                  <div class="d-flex justify-space-between align-center mb-2">
                    <v-icon :icon="template.icon || 'mdi-file-document'" size="32" color="white"></v-icon>
                    <v-chip
                      size="small"
                      :color="getGroupColor(groupData.groupId)"
                      variant="elevated"
                      class="group-chip"
                    >
                      {{ groupData.name }}
                    </v-chip>
                  </div>

                  <h3 class="text-h6 white--text mb-2">{{ template.title || `Feuille du ${formatDate(template.entryDate)}` }}</h3>

                  <div class="d-flex align-center mt-3">
                    <v-icon size="small" color="white" class="mr-1">mdi-checkbox-marked-circle-outline</v-icon>
                    <span class="text-caption white--text">
                      {{ template.timeSheetTasks ? `${template.timeSheetTasks.length} tâches` : 'Aucune tâche' }}
                    </span>
                  </div>

                  <div class="d-flex align-center mt-1">
                    <v-icon size="small" color="grey-lighten-1" class="mr-1">mdi-calendar</v-icon>
                    <span class="text-caption grey--text text--lighten-1">
                      Créée le {{ formatDate(template.entryDate) }}
                    </span>
                  </div>
                </div>
              </v-card>
            </v-col>
          </v-row>
        </div>
      </div>

      <!-- Message si aucun résultat après filtrage -->
      <div v-if="totalTemplatesCount === 0 && hasFilters" class="d-flex justify-center py-8">
        <v-card color="#283593" max-width="500" class="pa-4">
          <v-card-text class="text-center">
            <v-icon size="large" color="white" class="mb-2">mdi-file-search</v-icon>
            <p class="text-h6 white--text">Aucune feuille de temps ne correspond à vos critères</p>
            <v-btn color="primary" class="mt-4" @click="clearFilters">Réinitialiser les filtres</v-btn>
          </v-card-text>
        </v-card>
      </div>

      <!-- Message si aucune feuille de temps n'est disponible -->
      <div v-if="existingTemplates.length === 0 && !hasFilters" class="d-flex justify-center py-8">
        <v-card color="#283593" max-width="500" class="pa-4">
          <v-card-text class="text-center">
            <v-icon size="large" color="white" class="mb-2">mdi-file-document-outline</v-icon>
            <p class="text-h6 white--text">Vous n'avez pas encore créé de feuilles de temps</p>
            <v-btn color="primary" class="mt-4" to="/editTimeSheet">
              <v-icon start>mdi-plus</v-icon>
              Créer ma première feuille
            </v-btn>
          </v-card-text>
        </v-card>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import timeSheetService from '@/services/timeSheetService';
import groupService from '@/services/groupService';

const router = useRouter();

// Référence aux templates existants (historique des feuilles)
const existingTemplates = ref([]);
const loading = ref(true);
const error = ref(null);

// Groupes pour filtrage
const groups = ref([]);
const selectedGroupFilter = ref(null);

// Filtres date
const dateRange = ref([]);
const dateMenu = ref(false);

// Palette de couleurs pour les groupes
const groupColors = [
  '#673ab7', '#3f51b5', '#2196f3', '#9c27b0',
  '#5e35b1', '#3949ab', '#1e88e5',
  '#8e24aa', '#5e35b1', '#3949ab'
];

// Attribuer une couleur en fonction de l'ID du groupe
const getGroupColor = (groupId) => {
  // Convertir l'ID en nombre si ce n'est pas déjà le cas
  const numericId = typeof groupId === 'number' ? groupId : parseInt(groupId, 10);
  // Utiliser le modulo pour obtenir un index dans le tableau de couleurs
  const colorIndex = (numericId || 0) % groupColors.length;
  return groupColors[colorIndex];
};

// Options de filtre pour les groupes (mis à jour dynamiquement)
const groupFilterOptions = computed(() => {
  return [
    { id: null, name: 'Tous les groupes' },
    { id: 'personal', name: 'Personnel uniquement' },
    ...groups.value.map(g => ({ id: g.id, name: g.name }))
  ];
});

// Texte pour l'affichage des dates
const dateRangeText = computed(() => {
  if (dateRange.value.length === 0) return 'Toutes les dates';
  if (dateRange.value.length === 1) return `À partir du ${formatDate(dateRange.value[0])}`;
  return `Du ${formatDate(dateRange.value[0])} au ${formatDate(dateRange.value[1])}`;
});

// Détecter si des filtres sont appliqués
const hasFilters = computed(() => {
  return selectedGroupFilter.value !== null || dateRange.value.length > 0;
});

// Feuilles de temps personnelles (pas partagées avec un groupe)
const personalTemplates = computed(() => {
  // Si on a sélectionné un groupe spécifique (qui n'est pas "personnel")
  if (selectedGroupFilter.value !== null && selectedGroupFilter.value !== 'personal') {
    return [];
  }

  return existingTemplates.value.filter(template => {
    // Vérifier si la feuille a des sharedWithGroups et si elle est vide
    const isPersonal = !template.sharedWithGroups || template.sharedWithGroups.length === 0;

    // Appliquer filtre de date si nécessaire
    if (dateRange.value.length > 0) {
      return isPersonal && isInDateRange(template.entryDate);
    }

    return isPersonal;
  });
});

// Feuilles de temps regroupées par groupe
const groupedTemplates = computed(() => {
  // Créer une map pour regrouper les templates par groupe
  const groupMap = new Map();

  // Remplir la map avec les informations des groupes connus
  groups.value.forEach(group => {
    groupMap.set(group.id, {
      groupId: group.id,
      name: group.name,
      templates: []
    });
  });

  // Parcourir les templates et les ajouter aux groupes appropriés
  existingTemplates.value.forEach(template => {
    if (template.sharedWithGroups && template.sharedWithGroups.length > 0) {
      // Pour chaque groupe associé à ce template
      template.sharedWithGroups.forEach(shareInfo => {
        const groupId = shareInfo.groupId;

        // Si filtre de groupe spécifique activé et ce n'est pas "personnel"
        if (selectedGroupFilter.value !== null &&
          selectedGroupFilter.value !== 'personal' &&
          selectedGroupFilter.value !== groupId) {
          return;
        }

        // Vérifier le filtre de date si nécessaire
        if (dateRange.value.length > 0 && !isInDateRange(template.entryDate)) {
          return;
        }

        // Si le groupe existe dans notre map, ajouter le template
        if (groupMap.has(groupId)) {
          groupMap.get(groupId).templates.push(template);
        } else {
          // Si le groupe n'est pas connu, créer une entrée avec un nom par défaut
          groupMap.set(groupId, {
            groupId: groupId,
            name: `Groupe #${groupId}`,
            templates: [template]
          });
        }
      });
    }
  });

  // Convertir la map en tableau et ne garder que les groupes qui ont des templates
  return Array.from(groupMap.values())
    .filter(group => group.templates.length > 0)
    .sort((a, b) => a.name.localeCompare(b.name));
});

// Nombre total de templates filtrés
const totalTemplatesCount = computed(() => {
  const personalCount = personalTemplates.value.length;
  const groupCount = groupedTemplates.value.reduce((sum, group) => sum + group.templates.length, 0);
  return personalCount + groupCount;
});

// Vérifier si une date est dans la plage de dates sélectionnée
function isInDateRange(dateString) {
  if (!dateString || dateRange.value.length === 0) return true;

  const date = new Date(dateString);

  if (dateRange.value.length === 1) {
    // Date de début uniquement
    return date >= new Date(dateRange.value[0]);
  } else if (dateRange.value.length === 2) {
    // Plage complète
    return date >= new Date(dateRange.value[0]) && date <= new Date(dateRange.value[1]);
  }

  return true;
}

// Charger les données initiales
onMounted(async () => {
  try {
    loading.value = true;

    // 1. Charger les groupes de l'utilisateur
    const userGroups = await groupService.getUserGroups();
    groups.value = userGroups;

    // 2. Récupérer les feuilles personnelles et partagées directement
    const [ownedTimeSheets, sharedWithUserTimeSheets] = await Promise.all([
      timeSheetService.getUserTimeSheets(),
      timeSheetService.getSharedTimeSheets()
    ]);

    // 3. Récupérer les feuilles partagées avec chaque groupe dont l'utilisateur est membre
    const groupSharedTimeSheetsPromises = [];
    for (const group of userGroups) {
      groupSharedTimeSheetsPromises.push(timeSheetService.getGroupTimeSheets(group.id));
    }

    // Attendre que toutes les requêtes soient terminées
    const groupSharedTimeSheetsResults = await Promise.all(groupSharedTimeSheetsPromises);

    // 4. Fusionner toutes les feuilles de temps récupérées
    let allTimeSheets = [...ownedTimeSheets, ...sharedWithUserTimeSheets];

    // Ajouter les feuilles partagées avec les groupes
    groupSharedTimeSheetsResults.forEach(groupSheets => {
      if (Array.isArray(groupSheets)) {
        allTimeSheets = [...allTimeSheets, ...groupSheets];
      }
    });

    // 5. Dédupliquer en cas de doublons (sur la base de l'ID)
    const uniqueSheets = [...new Map(allTimeSheets.map(sheet => [sheet.id, sheet])).values()];

    // Mettre à jour la liste des feuilles
    existingTemplates.value = uniqueSheets;

    console.log(`Chargé ${uniqueSheets.length} feuilles au total (${ownedTimeSheets.length} personnelles, ${sharedWithUserTimeSheets.length} partagées directement, plus les feuilles via groupes)`);

  } catch (err) {
    console.error('Erreur lors du chargement des données:', err);
    error.value = 'Erreur lors du chargement des données: ' + err.message;
  } finally {
    loading.value = false;
  }
});

// Appliquer les filtres
function applyFilters() {
  console.log("Filtres appliqués:", {
    groupe: selectedGroupFilter.value,
    dates: dateRange.value
  });
}

// Réinitialiser les filtres
function clearFilters() {
  selectedGroupFilter.value = null;
  dateRange.value = [];
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
.timesheet-view {
  padding-bottom: 40px;
}

.filter-bar {
  background-color: #1e2a82;
  border-radius: 8px;
  padding: 16px;
}

.filter-select {
  min-width: 200px;
  max-width: 300px;
}

.new-sheet-button {
  display: inline-flex;
  align-items: center;
  padding: 8px 16px;
  background-color: rgba(255, 255, 255, 0.1);
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.new-sheet-button:hover {
  background-color: rgba(255, 255, 255, 0.15);
}

.timesheet-section {
  margin-bottom: 24px;
}

.section-title {
  background-color: #512da8; /* Couleur par défaut pour les sections */
  border-radius: 4px 4px 0 0;
  font-weight: bold;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.count-badge {
  color: rgba(255, 255, 255, 0.7);
  font-size: 14px;
  font-weight: normal;
}

.timesheet-card {
  border-radius: 8px;
  transition: transform 0.3s, box-shadow 0.3s;
  overflow: hidden;
  position: relative;
}

.timesheet-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.2) !important;
}

.card-content {
  padding: 16px;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.personal-chip {
  font-weight: bold;
  letter-spacing: 0.5px;
}

.group-chip {
  font-weight: bold;
  letter-spacing: 0.5px;
}
</style>
