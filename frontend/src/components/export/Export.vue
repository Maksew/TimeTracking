<template>
  <div>
    <h3 style="margin-top: 0.5rem;">Export your time sheets</h3>
    <div class="export-buttons">
      <v-text-field
        v-model="startDate"
        label="Start Date"
        type="date"
        dense
        style="max-width: 150px;"
      ></v-text-field>
      <v-text-field
        v-model="endDate"
        label="End Date"
        type="date"
        dense
        style="max-width: 150px;"
      ></v-text-field>
      <v-btn color="primary" @click="handleExportCSV" small style="min-width: 70px;">
        CSV
      </v-btn>
      <v-btn color="secondary" @click="handleExportPDF" small style="min-width: 70px;">
        PDF
      </v-btn>
    </div>
  </div>
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
  gap: 1rem;
  margin: 1rem 0;
}
</style>
