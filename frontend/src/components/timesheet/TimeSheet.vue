<template>
  <v-container fluid class="py-6 px-6" style="background-color: #2C2C5E;">
    <!-- Main Card -->
    <v-card
      class="mx-auto pa-4"
      max-width="600"
      elevation="2"
      style="background-color: rgb(40, 53, 147); color: #FFF; caret-color: #FFF;"
    >
      <!-- Title -->
      <v-card-title class="d-flex align-center">
        <v-icon left class="mr-2">mdi-file-document</v-icon>
        <span>Feuille de temps</span>
      </v-card-title>

      <v-card-text>
        <!-- Title Field -->
        <v-text-field
          v-model="titre"
          label="Saisissez votre titre..."
          outlined
          class="mb-4"
          style="background-color: rgba(255,255,255,0.1); color: #FFF;"
        />

        <!-- Tasks (Draggable) -->
        <div class="mb-4">
          <label class="text-body-2 mb-2 d-block">Tâches</label>
          <div ref="tasksContainer">
            <div
              v-for="(task, index) in tasks"
              :key="task.id"
              class="d-flex align-center mb-2"
            >
              <!-- Drag handle -->
              <v-btn icon class="drag-handle mr-2">
                <v-icon>mdi-drag</v-icon>
              </v-btn>
              <!-- Task Field -->
              <v-text-field
                v-model="task.text"
                :label="`Tâche #${index + 1}`"
                outlined
                class="flex-grow-1 mr-2"
                style="background-color: rgba(255,255,255,0.1); color: #FFF;"
              />
              <!-- Remove Task -->
              <v-btn icon color="error" @click="removeTask(index)">
                <v-icon>mdi-close</v-icon>
              </v-btn>
            </div>
          </div>
          <!-- Add Task -->
          <v-btn
            variant="text"
            color="primary"
            class="d-inline-flex align-center"
            @click="addTask"
          >
            <v-icon left>mdi-plus</v-icon>
            Ajouter tâche
          </v-btn>
        </div>

        <!-- Choose: Either "Fixer la date de fin" or "Jour / Heure / Minutes" -->
        <label class="text-body-2 mb-2 d-block">Durée</label>

        <!-- Updated radio group with flex -->
        <v-radio-group
          v-model="mode"
          row
          class="d-flex align-center mb-4"
          style="gap: 1rem; color: #FFF;"
        >
          <v-radio
            label="Fixer la date de fin"
            value="date"
            hide-details
            color="primary"
          />
          <v-radio
            label="Jour / Heure / Minutes"
            value="dhm"
            hide-details
            color="primary"
          />
        </v-radio-group>

        <!-- If "Fixer la date de fin" -->
        <div v-if="mode === 'date'" class="mb-4">
          <v-btn
            variant="outlined"
            color="primary"
            class="mr-2"
            @click="openDateDialog"
          >
            Fixer la date de fin
          </v-btn>
          <div v-if="selectedDate" class="mt-3">
            <strong>Date de fin :</strong>
            <span>{{ formatDate(selectedDate) }}</span>
          </div>
        </div>

        <!-- If "Jour / Heure / Minutes" -->
        <div v-else class="mb-4">
          <v-row dense>
            <v-col cols="4">
              <v-text-field
                label="Jours"
                type="number"
                v-model="days"
                outlined
                style="background-color: rgba(255,255,255,0.1); color: #FFF;"
              />
            </v-col>
            <v-col cols="4">
              <v-text-field
                label="Heures"
                type="number"
                v-model="hours"
                outlined
                style="background-color: rgba(255,255,255,0.1); color: #FFF;"
              />
            </v-col>
            <v-col cols="4">
              <v-text-field
                label="Minutes"
                type="number"
                v-model="minutes"
                outlined
                style="background-color: rgba(255,255,255,0.1); color: #FFF;"
              />
            </v-col>
          </v-row>
        </div>
      </v-card-text>

      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn variant="text" color="grey" @click="cancel">Annuler</v-btn>
        <v-btn color="primary" @click="submit">Continuer</v-btn>
      </v-card-actions>
    </v-card>

    <!-- Date Picker Dialog (Landscape Design) -->
    <v-dialog v-model="dateDialog" persistent max-width="500">
      <v-card style="background-color: #2C2C5E; color: #FFF; overflow: hidden;">
        <v-card-title class="py-2">Choisissez la date</v-card-title>
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
          <v-btn text color="white" @click="cancelDate">Annuler</v-btn>
          <v-btn text color="white" @click="saveDate">Continuer</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </v-container>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import Sortable from 'sortablejs'

// Title & Tasks
const titre = ref('')
let nextId = 1
const tasks = ref([{ id: nextId++, text: '' }])

// "mode" to choose between "date" or "days/hours/minutes"
const mode = ref('date')

// For the date dialog
const dateDialog = ref(false)
const selectedDate = ref(null)
const tempDate = ref(null)

// Days/Hours/Minutes
const days = ref(0)
const hours = ref(0)
const minutes = ref(0)

// Reference for SortableJS container
const tasksContainer = ref(null)

// Initialize SortableJS on mount
onMounted(() => {
  nextTick(() => {
    Sortable.create(tasksContainer.value, {
      handle: '.drag-handle',
      animation: 150,
      onEnd: (evt) => {
        // Reorder tasks
        if (evt.oldIndex !== evt.newIndex) {
          const movedItem = tasks.value.splice(evt.oldIndex, 1)[0]
          tasks.value.splice(evt.newIndex, 0, movedItem)
          // Force Vue reactivity
          tasks.value = [...tasks.value]
        }
      }
    })
  })
})

// Methods
function addTask() {
  tasks.value.push({ id: nextId++, text: '' })
}

function removeTask(index) {
  tasks.value.splice(index, 1)
  tasks.value = [...tasks.value]
}

function openDateDialog() {
  // Pre-fill the dialog with the currently selected date
  tempDate.value = selectedDate.value
  dateDialog.value = true
}

function cancelDate() {
  dateDialog.value = false
}

function saveDate() {
  selectedDate.value = tempDate.value
  dateDialog.value = false
  console.log('Date choisie:', selectedDate.value)
}

function cancel() {
  window.history.back()
}

async function submit() {
  try {
    const payload = {
      entryDate: mode.value === 'date' ? selectedDate.value : new Date().toISOString().split('T')[0],
      icon: 'mdi-file-document', // Icône par défaut
      tasks: tasks.value.map(task => ({
        name: task.text,
        repetition: 'NONE'
      }))
    };

    // Afficher un indicateur de chargement
    isSubmitting.value = true;

    // Créer la feuille de temps
    const timeSheet = await timeSheetService.createTimeSheet(payload);

    // Ajouter chaque tâche
    for (const task of tasks.value) {
      if (task.text.trim()) {
        // Créer la tâche si elle n'existe pas déjà
        const taskData = await taskService.createTask({ name: task.text, repetition: 'NONE' });

        // Calculer la durée en minutes
        let duration = 0;
        if (mode.value === 'dhm') {
          duration = (parseInt(days.value) * 24 * 60) +
            (parseInt(hours.value) * 60) +
            parseInt(minutes.value);
        } else {
          // Calculer la différence entre la date actuelle et la date sélectionnée
          const diffTime = Math.abs(new Date(selectedDate.value) - new Date());
          duration = Math.ceil(diffTime / (1000 * 60)); // Convertir en minutes
        }

        // Ajouter la tâche à la feuille de temps
        await timeSheetService.addTaskToTimeSheet(timeSheet.id, taskData.id, duration);
      }
    }

    // Rediriger vers le dashboard après succès
    router.push('/');
  } catch (error) {
    // Gérer les erreurs
    console.error('Erreur lors de la création de la feuille de temps:', error);
  } finally {
    isSubmitting.value = false;
  }
}

function formatDate(date) {
  if (!date) return ''
  const d = new Date(date)
  return d.toLocaleDateString('fr-CA', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric'
  })
}
</script>

<style scoped>
.drag-handle {
  cursor: move;
}
</style>
