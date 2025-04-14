<template>
  <v-menu>
    <template v-slot:activator="{ props }">
      <v-btn icon v-bind="props" class="mr-2">
        <v-icon>mdi-download</v-icon>
        <v-tooltip activator="parent" location="bottom">Exporter</v-tooltip>
      </v-btn>
    </template>
    <v-card min-width="300" color="#283593">
      <v-card-text>
        <p class="text-subtitle-2 mb-2">Export your time sheets</p>
        <div class="d-flex flex-column gap-2">
          <div class="d-flex flex-wrap align-center gap-2 mb-2">
            <v-text-field
              v-model="startDate"
              label="Start Date"
              type="date"
              density="compact"
              hide-details
              bg-color="#1a237e"
              class="mb-2 mr-2"
              style="max-width: 140px;"
            ></v-text-field>
            <v-text-field
              v-model="endDate"
              label="End Date"
              type="date"
              density="compact"
              hide-details
              bg-color="#1a237e"
              class="mb-2"
              style="max-width: 140px;"
            ></v-text-field>
          </div>
          <div class="d-flex gap-2">
            <v-btn color="primary" block @click="handleExportCSV">
              <v-icon start>mdi-file-delimited</v-icon>
              CSV
            </v-btn>
            <v-btn color="secondary" block @click="handleExportPDF">
              <v-icon start>mdi-file-pdf-box</v-icon>
              PDF
            </v-btn>
          </div>
        </div>
      </v-card-text>
    </v-card>
  </v-menu>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { exportTimeSheetsCSV, exportTimeSheetsPDF } from '@/services/exportService';
import timeSheetService from '@/services/timeSheetService';

function formatDateForInput(date) {
  return date.toISOString().substring(0, 10);
}

const startDate = ref('');
const endDate = ref('');

async function loadDateRange() {
  try {
    const sheets = await timeSheetService.getUserTimeSheets();
    if (!sheets || !sheets.length) {
      startDate.value = '2025-01-01';
      endDate.value = '2025-01-31';
      return;
    }
    const dates = sheets
      .map(sheet => new Date(sheet.entryDate))
      .filter(date => !isNaN(date));

    const minDate = new Date(Math.min(...dates));
    const maxDate = new Date(Math.max(...dates));

    startDate.value = formatDateForInput(minDate);
    endDate.value = formatDateForInput(maxDate);
  } catch (error) {
    console.error('Error fetching date range:', error);
    startDate.value = '2025-01-01';
    endDate.value = '2025-01-31';
  }
}

const handleExportCSV = () => {
  exportTimeSheetsCSV(startDate.value, endDate.value);
};

const handleExportPDF = () => {
  exportTimeSheetsPDF(startDate.value, endDate.value);
};

onMounted(() => {
  loadDateRange();
});
</script>

<style scoped>
.export-buttons {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}
</style>
