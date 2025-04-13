<template>
  <v-container fluid class="py-6 px-6" style="background-color: #2C2C5E;">
    <!-- Main Card -->
    <v-card
      class="mx-auto pa-4"
      max-width="800"
      elevation="3"
      style="background-color: rgb(40, 53, 147); color: #FFF; caret-color: #FFF;"
    >
      <!-- Headers avec actions -->
      <v-card-item>
        <template v-slot:prepend>
          <v-btn icon variant="text" @click="goBack">
            <v-icon>mdi-arrow-left</v-icon>
          </v-btn>
        </template>

        <v-card-title class="text-h5">
          <v-icon left class="mr-2">mdi-file-document-edit</v-icon>
          {{ isEditing ? 'Modifier la feuille de temps' : 'Nouvelle feuille de temps' }}
        </v-card-title>
      </v-card-item>

      <v-divider class="mb-4"></v-divider>

      <v-card-text>
        <!-- Alertes -->
        <v-alert v-if="errorMessage" type="error" class="mb-4" closable>
          {{ errorMessage }}
        </v-alert>

        <v-alert v-if="successMessage" type="success" class="mb-4" closable>
          {{ successMessage }}
        </v-alert>

        <!-- Title Field -->
        <v-text-field
          v-model="titre"
          label="Titre de la feuille de temps"
          placeholder="Saisissez votre titre..."
          variant="outlined"
          class="mb-4"
          hide-details="auto"
          style="background-color: rgba(255,255,255,0.1); color: #FFF;"
          prepend-inner-icon="mdi-format-title"
        />

        <!-- Icon selection -->
        <div class="mb-4">
          <label class="text-body-2 mb-2 d-block">Icône</label>
          <v-chip-group v-model="selectedIcon" mandatory>
            <v-chip
              v-for="icon in availableIcons"
              :key="icon"
              :value="icon"
              filter
              variant="outlined"
              :color="selectedIcon === icon ? 'primary' : 'default'"
            >
              <v-icon left>{{ icon }}</v-icon>
            </v-chip>
          </v-chip-group>
        </div>

        <ValidityPeriodSelector
          :initial-start-date="startDate"
          :initial-end-date="endDate"
          @update:period="onPeriodUpdate"
        />

        <!-- Tasks (Draggable) -->
        <div class="mb-4">
          <div class="d-flex align-center mb-2">
            <label class="text-body-2">Tâches</label>
            <v-spacer></v-spacer>
            <v-btn
              variant="text"
              color="primary"
              size="small"
              class="d-inline-flex align-center"
              @click="addTask"
            >
              <v-icon left size="small" class="mr-1">mdi-plus</v-icon>
              Ajouter tâche
            </v-btn>
          </div>

          <v-card class="mb-4" color="#1A237E">
            <div ref="tasksContainer" class="pa-2">
              <div
                v-for="(task, index) in tasks"
                :key="task.id"
                class="d-flex align-center mb-2 task-item pa-2 rounded"
              >
                <!-- Drag handle -->
                <v-btn icon class="drag-handle mr-2" size="small">
                  <v-icon>mdi-drag</v-icon>
                </v-btn>

                <!-- Task Field -->
                <v-text-field
                  v-model="task.name"
                  :label="`Tâche #${index + 1}`"
                  placeholder="Nom de la tâche..."
                  variant="outlined"
                  density="compact"
                  hide-details="auto"
                  class="flex-grow-1 mr-2"
                  style="background-color: rgba(255,255,255,0.05); color: #FFF;"
                  @change="unsavedChanges = true"
                />

                <!-- Remove Task -->
                <v-btn icon color="error" size="small" @click="removeTask(index)">
                  <v-icon>mdi-close</v-icon>
                </v-btn>
              </div>

              <div v-if="tasks.length === 0" class="text-center py-4 text-subtitle-2">
                Aucune tâche ajoutée. Cliquez sur "Ajouter tâche" pour commencer.
              </div>
            </div>
          </v-card>
        </div>

        <!-- Partage avec groupe (optionnel) -->
        <div class="mb-4">
          <label class="text-body-2 mb-2 d-block">Partager avec un groupe (optionnel)</label>
          <v-select
            v-model="selectedGroup"
            :items="userGroups"
            item-title="name"
            item-value="id"
            label="Sélectionner un groupe"
            variant="outlined"
            hide-details="auto"
            style="background-color: rgba(255,255,255,0.1); color: #FFF;"
            prepend-inner-icon="mdi-account-group"
            clearable
            :loading="loading"
          ></v-select>
          <div class="text-caption mt-1">{{ getGroupHelperText }}</div>
        </div>
      </v-card-text>

      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn variant="text" color="grey" @click="goBack">
          Annuler
        </v-btn>
        <v-btn
          color="primary"
          @click="submit"
          :loading="isSubmitting"
          :disabled="isSubmitting || tasks.every(task => !task.name.trim())"
        >
          {{ isEditing ? 'Mettre à jour' : 'Créer' }}
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-container>
</template>

<script setup>
import { ref, computed, onMounted, nextTick, watch, onUnmounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import Sortable from 'sortablejs';
import timeSheetService from '@/services/timeSheetService';
import taskService from '@/services/taskService';
import groupService from '@/services/groupService';
import ValidityPeriodSelector from '@/components/timesheet/ValidityPeriodSelector.vue';

const router = useRouter();
const route = useRoute();

// État d'édition
const isEditing = ref(false);
const templateId = ref(null);
const unsavedChanges = ref(false);
const showSavePrompt = ref(false);

// État de soumission et messages
const isSubmitting = ref(false);
const errorMessage = ref('');
const successMessage = ref('');

// Informations du formulaire
const titre = ref('');
const selectedIcon = ref('mdi-file-document');
const tasks = ref([]);
const nextTaskId = ref(1);

// Dates de validité
const startDate = ref(new Date().toISOString().split('T')[0]);
const endDate = ref((() => {
  const date = new Date();
  date.setDate(date.getDate() + 14); // Par défaut, 2 semaines de validité
  return date.toISOString().split('T')[0];
})());

// Informations avancées sur la période
const periodInfo = ref({
  startTime: '08:00',
  endTime: '18:00'
});

// Partage de groupe
const userGroups = ref([]);
const selectedGroup = ref(null);
const personalGroupId = ref('personnel');

// Icônes disponibles
const availableIcons = [
  'mdi-file-document',
  'mdi-briefcase',
  'mdi-school',
  'mdi-home',
  'mdi-run',
  'mdi-food',
  'mdi-brain',
  'mdi-heart',
  'mdi-leaf',
  'mdi-palette'
];

// Reference pour SortableJS container
const tasksContainer = ref(null);

// Fonction pour avertir l'utilisateur quand il tente de quitter avec des modifications non enregistrées
const handleBeforeUnload = (e) => {
  if (unsavedChanges.value) {
    e.preventDefault();
    e.returnValue = '';
  }
};

// Récupérer les groupes pour le partage
onMounted(async () => {
  // Ajouter l'écouteur d'événement beforeunload
  window.addEventListener('beforeunload', handleBeforeUnload);

  // Initialiser le drag-and-drop
  nextTick(() => {
    if (tasksContainer.value) {
      Sortable.create(tasksContainer.value, {
        handle: '.drag-handle',
        animation: 150,
        onEnd: (evt) => {
          // Reordonner les tâches
          if (evt.oldIndex !== evt.newIndex) {
            const movedItem = tasks.value.splice(evt.oldIndex, 1)[0];
            tasks.value.splice(evt.newIndex, 0, movedItem);
            // Forcer la réactivité Vue
            tasks.value = [...tasks.value];
            unsavedChanges.value = true;
          }
        }
      });
    }

    // Vérifier s'il s'agit d'une édition (templateId dans l'URL)
    const template = route.query.template;
    if (template) {
      templateId.value = parseInt(template);
      isEditing.value = true;
      loadTemplate(templateId.value);
    } else {
      // Nouvelle feuille, ajouter une tâche vide
      addTask();
    }

    // Récupérer les groupes de l'utilisateur
    loadUserGroups();
  });
});

// Cleanup quand le composant est détruit
onUnmounted(() => {
  // Supprimer l'écouteur d'événement beforeunload
  window.removeEventListener('beforeunload', handleBeforeUnload);
});

// Gestionnaire de mise à jour de la période
const onPeriodUpdate = (periodData) => {
  // Mettre à jour les dates
  startDate.value = periodData.startDate;
  endDate.value = periodData.endDate;

  // Stocker les informations avancées
  periodInfo.value = {
    startTime: periodData.startTime,
    endTime: periodData.endTime,
    startDateTime: periodData.startDateTime,
    endDateTime: periodData.endDateTime
  };

  unsavedChanges.value = true;
};

// Méthodes
async function loadTemplate(id) {
  try {
    const [template, allTasks] = await Promise.all([
      timeSheetService.getTimeSheetById(id),
      taskService.getAllTasks()
    ]);

    // Créer un map des tâches par ID pour un accès rapide
    const tasksMap = {};
    allTasks.forEach(task => {
      tasksMap[task.id] = task;
    });

    console.log("Template chargé:", template);
    console.log("Toutes les tâches:", allTasks);
    console.log("Map des tâches:", tasksMap);

    // Remplir les champs du formulaire
    titre.value = template.title || '';
    selectedIcon.value = template.icon || 'mdi-file-document';

    // Dates de validité
    if (template.startDate) {
      startDate.value = template.startDate;
    }
    if (template.endDate) {
      endDate.value = template.endDate;
    }

    // Charger les tâches
    if (template.timeSheetTasks && template.timeSheetTasks.length > 0) {
      tasks.value = template.timeSheetTasks.map(tst => {
        // Récupérer les détails complets de la tâche
        const taskDetails = tasksMap[tst.taskId];
        console.log("Détails de la tâche", tst.taskId, ":", taskDetails);

        return {
          id: nextTaskId.value++,
          name: taskDetails ? taskDetails.name : `Tâche #${tst.taskId}`,
          taskId: tst.taskId,
          duration: tst.duration || 0
        };
      });

      console.log("Tâches chargées:", tasks.value);
    } else {
      addTask(); // Au moins une tâche vide
    }

    // Groupe associé
    if (template.sharedWithGroups && template.sharedWithGroups.length > 0) {
      selectedGroup.value = template.sharedWithGroups[0].groupId;
    } else {
      // Si pas de groupe associé, utiliser le groupe Personnel par défaut
      selectedGroup.value = personalGroupId.value;
    }

    // Réinitialiser l'état des modifications
    unsavedChanges.value = false;
  } catch (error) {
    console.error('Erreur lors du chargement du modèle:', error);
    errorMessage.value = "Impossible de charger le modèle de feuille de temps.";
  }
}

const loadUserGroups = async () => {
  try {
    console.log("Chargement des groupes...");
    // Récupérer tous les groupes de l'utilisateur
    const groups = await groupService.getUserGroups();
    console.log("Groupes récupérés:", groups);

    // Filtrer pour ne garder que les groupes dont l'utilisateur est propriétaire
    const ownedGroups = groups.filter(group => {
      // Trouver l'entrée UserGroup pour l'utilisateur actuel
      const userGroup = group.userGroups?.find(ug => ug.userId === authStore.user.id);
      // Ne garder que les groupes où l'utilisateur a le rôle OWNER
      return userGroup && userGroup.role === 'OWNER';
    }).map(group => ({
      id: group.id,
      name: group.name
    }));

    console.log("Groupes filtrés (propriétaire uniquement):", ownedGroups);

    // S'assurer que l'option "Personnel" est toujours présente
    userGroups.value = [
      { id: 'personnel', name: 'Personnel' },
      ...ownedGroups
    ];

    console.log("Groupes disponibles finaux:", userGroups.value);
  } catch (error) {
    console.error('Erreur lors du chargement des groupes:', error);
    // S'assurer qu'au moins l'option Personnel est disponible même en cas d'erreur
    userGroups.value = [{ id: 'personnel', name: 'Personnel' }];
  }
};

const getGroupHelperText = computed(() => {
  if (selectedGroup.value === 'personnel') {
    return "Cette feuille sera visible uniquement par vous.";
  } else if (selectedGroup.value) {
    return "Cette feuille sera partagée avec tous les membres du groupe sélectionné.";
  }
  return "Sélectionnez un groupe pour partager cette feuille de temps.";
});



function addTask() {
  tasks.value.push({ id: nextTaskId.value++, name: '', duration: 0 }); // Toujours commencer à 0
  unsavedChanges.value = true;
}

function removeTask(index) {
  tasks.value.splice(index, 1);
  unsavedChanges.value = true;
}

function goBack() {
  if (unsavedChanges.value) {
    if (confirm("Vous avez des modifications non sauvegardées. Voulez-vous vraiment quitter ?")) {
      router.back();
    }
  } else {
    router.back();
  }
}

function goBackWithoutSaving() {
  showSavePrompt.value = false;
  unsavedChanges.value = false;
  router.back();
}

async function submit() {
  // Valider le formulaire
  if (!titre.value.trim()) {
    errorMessage.value = "Le titre est obligatoire.";
    return;
  }

  if (tasks.value.length === 0 || tasks.value.every(task => !task.name.trim())) {
    errorMessage.value = "Veuillez ajouter au moins une tâche.";
    return;
  }

  try {
    isSubmitting.value = true;
    errorMessage.value = '';

    // Préparer les données
    const timeSheetData = {
      entryDate: new Date().toISOString().split('T')[0],
      title: titre.value,
      icon: selectedIcon.value,
      startDate: startDate.value,
      endDate: endDate.value,
      // Ajouter les infos de temps si disponibles
      startTime: periodInfo.value.startTime,
      endTime: periodInfo.value.endTime
    }

    // Stockage des durées existantes (pour les préserver)
    const existingTaskDurations = {};
    let originalTimeSheet = null;

    // Si c'est une mise à jour, récupérer les durées existantes
    if (isEditing.value) {
      try {
        // Récupérer la feuille de temps existante avec ses tâches
        originalTimeSheet = await timeSheetService.getTimeSheetById(templateId.value);

        // Stocker les durées existantes par ID de tâche
        if (originalTimeSheet.timeSheetTasks && originalTimeSheet.timeSheetTasks.length > 0) {
          originalTimeSheet.timeSheetTasks.forEach(taskRelation => {
            existingTaskDurations[taskRelation.taskId] = taskRelation.duration || 0;
          });
        }
        console.log('Durées existantes récupérées:', existingTaskDurations);
      } catch (error) {
        console.warn("Impossible de récupérer les durées existantes:", error);
      }
    }

    // Créer ou mettre à jour la feuille de temps
    let timeSheet;
    if (isEditing.value) {
      timeSheetData.id = templateId.value;
      timeSheet = await timeSheetService.updateTimeSheet(templateId.value, timeSheetData);
    } else {
      timeSheet = await timeSheetService.createTimeSheet(timeSheetData);
    }

    // Si c'est une mise à jour, supprimer d'abord toutes les tâches existantes
    if (isEditing.value && originalTimeSheet && originalTimeSheet.timeSheetTasks) {
      try {
        for (const task of originalTimeSheet.timeSheetTasks) {
          // Supprimer la relation tâche-feuille
          await timeSheetService.removeTaskFromTimeSheet(timeSheet.id, task.taskId);
        }
        console.log('Anciennes tâches supprimées avec succès');
      } catch (error) {
        console.warn("Erreur lors de la suppression des tâches existantes:", error);
      }
    }

    // Ajouter ou mettre à jour les tâches
    for (const task of tasks.value) {
      if (task.name.trim()) {
        // Créer la tâche si nécessaire
        let taskId = task.taskId;
        if (!taskId) {
          const taskData = await taskService.createTask({
            name: task.name,
            repetition: 'NONE'
          });
          taskId = taskData.id;
        }

        // Déterminer la durée à utiliser (préserver l'ancienne si elle existe)
        let duration = 0;
        if (existingTaskDurations[taskId] !== undefined) {
          duration = existingTaskDurations[taskId];
        }

        // Ajouter la tâche à la feuille de temps avec la durée appropriée
        await timeSheetService.addTaskToTimeSheet(timeSheet.id, taskId, duration);
      }
    }

    // Partager avec le groupe si sélectionné (et que ce n'est pas le groupe Personnel virtuel)
    if (selectedGroup.value && selectedGroup.value !== 'personnel') {
      await timeSheetService.shareTimeSheetWithGroup(timeSheet.id, selectedGroup.value, 'READ');
    }

    // Succès
    successMessage.value = `Feuille de temps ${isEditing.value ? 'mise à jour' : 'créée'} avec succès!`;
    unsavedChanges.value = false;

    // Forcer une attente pour laisser le temps au serveur de traiter les modifications
    await new Promise(resolve => setTimeout(resolve, 500));

    // Rediriger après un court délai avec un paramètre force_refresh
    setTimeout(() => {
      router.push('/?force_refresh=true');
    }, 1000);
  } catch (error) {
    console.error('Erreur lors de la soumission:', error);
    errorMessage.value = `Une erreur est survenue: ${error.message || 'Veuillez réessayer.'}`;
  } finally {
    isSubmitting.value = false;
  }
}
</script>

<style scoped>
.drag-handle {
  cursor: move;
}

.task-item {
  transition: background-color 0.2s;
}

.task-item:hover {
  background-color: rgba(255, 255, 255, 0.05);
}
</style>
