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

        <template v-slot:append>
          <v-badge v-if="unsavedChanges" color="warning" dot>
            <v-btn icon @click="showSavePrompt = true">
              <v-icon>mdi-content-save</v-icon>
              <v-tooltip activator="parent" location="bottom">Enregistrer</v-tooltip>
            </v-btn>
          </v-badge>
        </template>
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
                  v-model="task.text"
                  :label="`Tâche #${index + 1}`"
                  variant="outlined"
                  density="compact"
                  hide-details="auto"
                  class="flex-grow-1 mr-2"
                  style="background-color: rgba(255,255,255,0.05); color: #FFF;"
                  @change="unsavedChanges = true"
                />

                <!-- Task Duration -->
                <v-text-field
                  v-model="task.duration"
                  label="Durée (min)"
                  variant="outlined"
                  density="compact"
                  hide-details="auto"
                  type="number"
                  style="max-width: 110px; background-color: rgba(255,255,255,0.05); color: #FFF;"
                  class="mr-2"
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

        <!-- Choose: Either "Fixer la date d'échéance" or "Jour / Heure / Minutes" -->
        <div class="mb-4">
          <label class="text-body-2 mb-2 d-block">Période</label>

          <v-tabs v-model="modeTab" bg-color="#1A237E" align-tabs="center">
            <v-tab value="date">
              <v-icon start>mdi-calendar-end</v-icon>
              Date d'échéance
            </v-tab>
            <v-tab value="dhm">
              <v-icon start>mdi-clock-time-four</v-icon>
              Durée manuelle
            </v-tab>
          </v-tabs>

          <v-window v-model="modeTab" class="mt-4">
            <!-- If "Fixer la date d'échéance" -->
            <v-window-item value="date">
              <v-card flat>
                <div class="pa-4">
                  <div v-if="selectedDate" class="mb-4">
                    <v-chip color="primary" size="large" class="pa-4">
                      <v-icon start>mdi-calendar-check</v-icon>
                      Date d'échéance: {{ formatDate(selectedDate) }}
                    </v-chip>
                    <v-btn icon class="ml-2" color="error" variant="outlined" @click="selectedDate = null">
                      <v-icon>mdi-close</v-icon>
                    </v-btn>
                  </div>

                  <v-btn
                    variant="outlined"
                    color="primary"
                    prepend-icon="mdi-calendar"
                    @click="openDateDialog"
                  >
                    {{ selectedDate ? 'Modifier la date' : 'Fixer la date d\'échéance' }}
                  </v-btn>
                </div>
              </v-card>
            </v-window-item>

            <!-- If "Jour / Heure / Minutes" -->
            <v-window-item value="dhm">
              <v-card flat>
                <div class="pa-4">
                  <v-row dense>
                    <v-col cols="4">
                      <v-text-field
                        label="Jours"
                        type="number"
                        v-model="days"
                        variant="outlined"
                        min="0"
                        hide-details="auto"
                        style="background-color: rgba(255,255,255,0.1); color: #FFF;"
                      />
                    </v-col>
                    <v-col cols="4">
                      <v-text-field
                        label="Heures"
                        type="number"
                        v-model="hours"
                        variant="outlined"
                        min="0"
                        max="23"
                        hide-details="auto"
                        style="background-color: rgba(255,255,255,0.1); color: #FFF;"
                      />
                    </v-col>
                    <v-col cols="4">
                      <v-text-field
                        label="Minutes"
                        type="number"
                        v-model="minutes"
                        variant="outlined"
                        min="0"
                        max="59"
                        hide-details="auto"
                        style="background-color: rgba(255,255,255,0.1); color: #FFF;"
                      />
                    </v-col>
                  </v-row>

                  <div class="text-center mt-4">
                    <v-chip color="success" size="large">
                      <v-icon start>mdi-timer</v-icon>
                      Durée totale: {{ calculateTotalTime() }}
                    </v-chip>
                  </div>
                </div>
              </v-card>
            </v-window-item>
          </v-window>
        </div>

        <!-- Partage avec groupe (optionnel) -->
        <div v-if="userGroups.length > 0" class="mb-4">
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
          ></v-select>
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
          :disabled="isSubmitting || tasks.every(task => !task.text.trim())"
        >
          {{ isEditing ? 'Mettre à jour' : 'Créer' }}
        </v-btn>
      </v-card-actions>
    </v-card>

    <!-- Date Picker Dialog -->
    <v-dialog v-model="dateDialog" persistent max-width="500">
      <v-card style="background-color: #2C2C5E; color: #FFF; overflow: hidden;">
        <v-card-title class="py-2">Choisissez la date d'échéance</v-card-title>
        <v-card-text style="padding-top: 0;">
          <v-date-picker
            v-model="tempDate"
            color="primary"
            theme="dark"
            landscape
            show-current
          />
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn text color="grey" @click="cancelDate">Annuler</v-btn>
          <v-btn text color="primary" @click="saveDate">Confirmer</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Prompt sauvegarde non enregistrée -->
    <v-dialog v-model="showSavePrompt" max-width="400">
      <v-card>
        <v-card-title class="text-h5">Enregistrer les modifications?</v-card-title>
        <v-card-text>
          Vous avez des modifications non enregistrées. Voulez-vous les sauvegarder avant de quitter?
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn color="grey" variant="text" @click="showSavePrompt = false">
            Annuler
          </v-btn>
          <v-btn color="error" variant="text" @click="goBackWithoutSaving">
            Ne pas enregistrer
          </v-btn>
          <v-btn color="primary" @click="submit">
            Enregistrer
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </v-container>
</template>

<script setup>
import { ref, onMounted, nextTick, computed, watch, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import Sortable from 'sortablejs'
import timeSheetService from '@/services/timeSheetService'
import taskService from '@/services/taskService'
import groupService from '@/services/groupService'

const router = useRouter()
const route = useRoute()

// État d'édition
const isEditing = ref(false)
const templateId = ref(null)
const unsavedChanges = ref(false)
const showSavePrompt = ref(false)

// État de soumission et messages
const isSubmitting = ref(false)
const errorMessage = ref('')
const successMessage = ref('')

// Informations du formulaire
const titre = ref('')
const selectedIcon = ref('mdi-file-document')
const modeTab = ref('dhm') // 'date' or 'dhm'
const tasks = ref([])
const nextTaskId = ref(1)

// Pour la date d'échéance
const dateDialog = ref(false)
const selectedDate = ref(null)
const tempDate = ref(null)

// Pour la durée manuelle
const days = ref(0)
const hours = ref(0)
const minutes = ref(0)

// Partage de groupe
const userGroups = ref([])
const selectedGroup = ref(null)

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
]

// Reference pour SortableJS container
const tasksContainer = ref(null)

// Fonction pour avertir l'utilisateur quand il tente de quitter avec des modifications non enregistrées
const handleBeforeUnload = (e) => {
  if (unsavedChanges.value) {
    e.preventDefault()
    e.returnValue = ''
  }
}

// Récupérer les groupes pour le partage
onMounted(async () => {
  // Ajouter l'écouteur d'événement beforeunload
  window.addEventListener('beforeunload', handleBeforeUnload)

  // Initialiser le drag-and-drop
  nextTick(() => {
    if (tasksContainer.value) {
      Sortable.create(tasksContainer.value, {
        handle: '.drag-handle',
        animation: 150,
        onEnd: (evt) => {
          // Reordonner les tâches
          if (evt.oldIndex !== evt.newIndex) {
            const movedItem = tasks.value.splice(evt.oldIndex, 1)[0]
            tasks.value.splice(evt.newIndex, 0, movedItem)
            // Forcer la réactivité Vue
            tasks.value = [...tasks.value]
            unsavedChanges.value = true
          }
        }
      })
    }

    // Vérifier s'il s'agit d'une édition (templateId dans l'URL)
    const template = route.query.template
    if (template) {
      templateId.value = parseInt(template)
      isEditing.value = true
      loadTemplate(templateId.value)
    } else {
      // Nouvelle feuille, ajouter une tâche vide
      addTask()
    }

    // Récupérer les groupes de l'utilisateur
    loadUserGroups()
  })
})

// Cleanup quand le composant est détruit
onUnmounted(() => {
  // Supprimer l'écouteur d'événement beforeunload
  window.removeEventListener('beforeunload', handleBeforeUnload)
})

// Méthodes
async function loadTemplate(id) {
  try {
    const template = await timeSheetService.getTimeSheetById(id)

    // Remplir les champs du formulaire
    titre.value = template.title || ''
    selectedIcon.value = template.icon || 'mdi-file-document'

    // Charger les tâches
    if (template.timeSheetTasks && template.timeSheetTasks.length > 0) {
      tasks.value = template.timeSheetTasks.map(tst => ({
        id: nextTaskId.value++,
        text: tst.task?.name || `Tâche ${tst.taskId}`,
        taskId: tst.taskId,
        duration: tst.duration || 0
      }))
    } else {
      addTask() // Au moins une tâche vide
    }

    // Date d'échéance ou durée
    if (template.dueDate) {
      selectedDate.value = template.dueDate
      modeTab.value = 'date'
    } else {
      // Calculer jours/heures/minutes à partir de la durée totale
      const totalMinutes = template.timeSheetTasks?.reduce((sum, tst) => sum + (tst.duration || 0), 0) || 0
      days.value = Math.floor(totalMinutes / (60 * 24))
      hours.value = Math.floor((totalMinutes % (60 * 24)) / 60)
      minutes.value = totalMinutes % 60
      modeTab.value = 'dhm'
    }

    // Groupe associé
    if (template.sharedWithGroups && template.sharedWithGroups.length > 0) {
      selectedGroup.value = template.sharedWithGroups[0].groupId
    }

    // Réinitialiser l'état des modifications
    unsavedChanges.value = false
  } catch (error) {
    console.error('Erreur lors du chargement du modèle:', error)
    errorMessage.value = "Impossible de charger le modèle de feuille de temps."
  }
}

async function loadUserGroups() {
  try {
    const groups = await groupService.getUserGroups()
    userGroups.value = groups
  } catch (error) {
    console.error('Erreur lors du chargement des groupes:', error)
  }
}

function addTask() {
  tasks.value.push({ id: nextTaskId.value++, text: '', duration: 30 })
  unsavedChanges.value = true
}

function removeTask(index) {
  tasks.value.splice(index, 1)
  unsavedChanges.value = true
}

function openDateDialog() {
  tempDate.value = selectedDate.value
  dateDialog.value = true
}

function cancelDate() {
  dateDialog.value = false
}

function saveDate() {
  selectedDate.value = tempDate.value
  dateDialog.value = false
  unsavedChanges.value = true
}

function goBack() {
  if (unsavedChanges.value) {
    showSavePrompt.value = true
  } else {
    router.back()
  }
}

function goBackWithoutSaving() {
  showSavePrompt.value = false
  unsavedChanges.value = false
  router.back()
}

function calculateTotalTime() {
  const totalMinutes = parseInt(days.value || 0) * 24 * 60 +
    parseInt(hours.value || 0) * 60 +
    parseInt(minutes.value || 0)

  const displayDays = Math.floor(totalMinutes / (60 * 24))
  const displayHours = Math.floor((totalMinutes % (60 * 24)) / 60)
  const displayMinutes = totalMinutes % 60

  let result = ''
  if (displayDays > 0) result += `${displayDays}j `
  if (displayHours > 0) result += `${displayHours}h `
  result += `${displayMinutes}min`

  return result.trim()
}

async function submit() {
  // Valider le formulaire
  if (!titre.value.trim()) {
    errorMessage.value = "Le titre est obligatoire."
    return
  }

  if (tasks.value.length === 0 || tasks.value.every(task => !task.text.trim())) {
    errorMessage.value = "Veuillez ajouter au moins une tâche."
    return
  }

  try {
    isSubmitting.value = true
    errorMessage.value = ''

    // Préparer les données
    const timeSheetData = {
      entryDate: new Date().toISOString().split('T')[0],
      title: titre.value,
      icon: selectedIcon.value,
      dueDate: modeTab.value === 'date' ? selectedDate.value : null
    }

    // Créer ou mettre à jour la feuille de temps
    let timeSheet
    if (isEditing.value) {
      timeSheetData.id = templateId.value
      timeSheet = await timeSheetService.updateTimeSheet(templateId.value, timeSheetData)
    } else {
      timeSheet = await timeSheetService.createTimeSheet(timeSheetData)
    }

    // Ajouter ou mettre à jour les tâches
    for (const task of tasks.value) {
      if (task.text.trim()) {
        // Créer la tâche si nécessaire
        let taskId = task.taskId
        if (!taskId) {
          const taskData = await taskService.createTask({
            name: task.text,
            repetition: 'NONE'
          })
          taskId = taskData.id
        }

        // Calculer la durée en minutes
        let duration = parseInt(task.duration || 0)
        if (modeTab.value === 'dhm' && !duration) {
          // Répartir la durée totale entre les tâches
          const totalTaskMinutes = parseInt(days.value || 0) * 24 * 60 +
            parseInt(hours.value || 0) * 60 +
            parseInt(minutes.value || 0)
          duration = Math.floor(totalTaskMinutes / tasks.value.filter(t => t.text.trim()).length)
        }

        // Ajouter la tâche à la feuille de temps
        await timeSheetService.addTaskToTimeSheet(timeSheet.id, taskId, duration)
      }
    }

    // Partager avec le groupe si sélectionné
    if (selectedGroup.value) {
      await timeSheetService.shareTimeSheetWithGroup(timeSheet.id, selectedGroup.value, 'READ')
    }

    // Succès
    successMessage.value = `Feuille de temps ${isEditing.value ? 'mise à jour' : 'créée'} avec succès!`
    unsavedChanges.value = false

    // Rediriger après un court délai
    setTimeout(() => {
      router.push('/')
    }, 1500)
  } catch (error) {
    console.error('Erreur lors de la soumission:', error)
    errorMessage.value = `Une erreur est survenue: ${error.message || 'Veuillez réessayer.'}`
  } finally {
    isSubmitting.value = false
  }
}

function formatDate(date) {
  if (!date) return ''
  const d = new Date(date)
  return d.toLocaleDateString('fr-FR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric'
  })
}

// Observer les changements pour détecter les modifications non enregistrées
watch([titre, selectedIcon, tasks, selectedDate, days, hours, minutes, selectedGroup], () => {
  unsavedChanges.value = true
})
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
