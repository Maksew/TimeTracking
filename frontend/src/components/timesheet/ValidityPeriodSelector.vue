<template>
  <v-card color="#1a237e" class="pa-4 mb-4 rounded-lg">
    <v-card-title class="d-flex align-center mb-2">
      <v-icon left class="mr-2">mdi-calendar-clock</v-icon>
      Durée de validité
    </v-card-title>

    <v-card-text>
      <!-- Sélection rapide avec des boutons prédéfinis -->
      <div class="mb-4">
        <div class="d-flex flex-wrap">
          <v-btn
            v-for="option in quickOptions"
            :key="option.value"
            variant="outlined"
            :color="selectedQuickOption === option.value ? 'primary' : 'grey'"
            class="mr-2 mb-2"
            @click="selectQuickOption(option.value)"
          >
            {{ option.label }}
          </v-btn>
        </div>
      </div>

      <!-- Visualisation de la période -->
      <div class="mt-4">
        <div class="period-visualizer pa-3 rounded">
          <div class="d-flex justify-space-between align-center mb-2">
            <div>{{ formattedStartDate }}</div>
            <div class="period-duration">{{ durationText }}</div>
            <div>{{ formattedEndDate }}</div>
          </div>
          <div class="timeline-container">
            <div class="timeline-progress" style="width: 100%"></div>
            <div class="timeline-marker current-time" :style="{ left: progressPosition + '%' }"></div>
          </div>
        </div>
      </div>
    </v-card-text>
  </v-card>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';

// Options de départ
const props = defineProps({
  initialStartDate: {
    type: String,
    default: () => new Date().toISOString().split('T')[0]
  },
  initialEndDate: {
    type: String,
    default: () => {
      const date = new Date();
      date.setDate(date.getDate() + 14); // Par défaut: 2 semaines
      return date.toISOString().split('T')[0];
    }
  }
});

const emit = defineEmits(['update:period']);

// État
const selectedQuickOption = ref('two-weeks');
const startDate = ref(props.initialStartDate);
const endDate = ref(props.initialEndDate);
const durationText = ref('2 semaines');
const progressPosition = ref(0); // Position de la marque de temps actuel (0-100%)

// Options prédéfinies
const quickOptions = [
  { label: 'Aujourd\'hui', value: 'today' },
  { label: 'Cette semaine', value: 'week' },
  { label: 'Ce mois', value: 'month' },
  { label: '2 semaines', value: 'two-weeks' },
  { label: '1 mois', value: 'one-month' },
  { label: '3 mois', value: 'three-months' }
];

// Formatage des dates pour affichage
const formattedStartDate = computed(() => {
  return formatDateSimple(startDate.value);
});

const formattedEndDate = computed(() => {
  return formatDateSimple(endDate.value);
});

// Fonction de formatage simple
function formatDateSimple(dateStr) {
  if (!dateStr) return '';
  try {
    const date = new Date(dateStr);
    return date.toLocaleDateString('fr-FR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric'
    });
  } catch (e) {
    console.error('Erreur formatage date:', e);
    return dateStr;
  }
}

// Calcule la position du marqueur de temps actuel sur la timeline
function updateProgressPosition() {
  try {
    const now = new Date();
    const start = new Date(startDate.value);
    const end = new Date(endDate.value);

    if (now < start) {
      progressPosition.value = 0;
      return;
    }

    if (now > end) {
      progressPosition.value = 100;
      return;
    }

    const totalMs = end.getTime() - start.getTime();
    const elapsedMs = now.getTime() - start.getTime();
    progressPosition.value = (elapsedMs / totalMs) * 100;
  } catch (e) {
    console.error('Erreur calcul progression:', e);
    progressPosition.value = 0;
  }
}

// Sélection d'une option prédéfinie
function selectQuickOption(option) {
  selectedQuickOption.value = option;

  const today = new Date();
  let end = new Date();

  try {
    switch (option) {
      case 'today':
        startDate.value = today.toISOString().split('T')[0];
        endDate.value = today.toISOString().split('T')[0];
        durationText.value = "Aujourd'hui";
        break;

      case 'week':
        // Début: aujourd'hui, Fin: dimanche prochain
        const dayOfWeek = today.getDay();
        const daysToSunday = dayOfWeek === 0 ? 7 : 7 - dayOfWeek;
        end = new Date(today);
        end.setDate(today.getDate() + daysToSunday);
        startDate.value = today.toISOString().split('T')[0];
        endDate.value = end.toISOString().split('T')[0];
        durationText.value = "Cette semaine";
        break;

      case 'month':
        // Début: aujourd'hui, Fin: dernier jour du mois
        end = new Date(today.getFullYear(), today.getMonth() + 1, 0);
        startDate.value = today.toISOString().split('T')[0];
        endDate.value = end.toISOString().split('T')[0];
        durationText.value = "Ce mois";
        break;

      case 'two-weeks':
        end = new Date(today);
        end.setDate(today.getDate() + 14);
        startDate.value = today.toISOString().split('T')[0];
        endDate.value = end.toISOString().split('T')[0];
        durationText.value = "2 semaines";
        break;

      case 'one-month':
        end = new Date(today);
        end.setMonth(today.getMonth() + 1);
        startDate.value = today.toISOString().split('T')[0];
        endDate.value = end.toISOString().split('T')[0];
        durationText.value = "1 mois";
        break;

      case 'three-months':
        end = new Date(today);
        end.setMonth(today.getMonth() + 3);
        startDate.value = today.toISOString().split('T')[0];
        endDate.value = end.toISOString().split('T')[0];
        durationText.value = "3 mois";
        break;
    }

    // Mettre à jour la position du marqueur de temps
    updateProgressPosition();

    // Notifier le parent
    emitPeriodUpdate();
  } catch (e) {
    console.error('Erreur sélection option:', e);
  }
}

// Notifier le parent des changements de période
function emitPeriodUpdate() {
  emit('update:period', {
    startDate: startDate.value,
    endDate: endDate.value
  });
}

// Initialisation
onMounted(() => {
  // Initialiser avec l'option 2 semaines par défaut
  selectQuickOption('two-weeks');
});
</script>

<style scoped>
.period-visualizer {
  background-color: #142055;
  position: relative;
}

.timeline-container {
  height: 8px;
  background-color: rgba(255, 255, 255, 0.1);
  border-radius: 4px;
  position: relative;
  overflow: hidden;
}

.timeline-progress {
  height: 100%;
  background: linear-gradient(to right, #7e57c2, #9c27b0);
  border-radius: 4px;
  position: absolute;
  top: 0;
  left: 0;
}

.timeline-marker {
  position: absolute;
  width: 4px;
  height: 16px;
  background-color: #ffffff;
  top: -4px;
  transform: translateX(-50%);
  border-radius: 2px;
  z-index: 2;
}

.period-duration {
  background-color: rgba(156, 39, 176, 0.2);
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 0.85rem;
  border: 1px solid rgba(156, 39, 176, 0.4);
}
</style>
