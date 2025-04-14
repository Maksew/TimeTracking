<template>
  <div class="export-buttons">
    <v-text-field
      v-model="startDate"
      label="Start Date"
      type="date"
      dense
    ></v-text-field>
    <v-text-field
      v-model="endDate"
      label="End Date"
      type="date"
      dense
    ></v-text-field>
    <v-btn color="primary" @click="handleExportCSV">
      Export CSV
    </v-btn>
    <v-btn color="secondary" @click="handleExportPDF">
      Export PDF
    </v-btn>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { exportTimeSheetsCSV, exportTimeSheetsPDF } from '@/services/exportService';
import timeSheetService from '@/services/timeSheetService';

/**
 * Formats a Date object to YYYY-MM-DD.
 */
function formatDateForInput(date) {
  return date.toISOString().substring(0, 10);
}

// Reactive variables for date range
const startDate = ref('');
const endDate = ref('');

/**
 * Loads current user's timesheets and computes default export dates.
 * Uses the earliest entryDate as startDate and latest as endDate.
 */
async function loadDateRange() {
  try {
    const sheets = await timeSheetService.getUserTimeSheets();
    if (!sheets || !sheets.length) {
      // Fallback to defaults if none available.
      startDate.value = '2025-01-01';
      endDate.value = '2025-01-31';
      return;
    }
    const dates = sheets
      .map(sheet => new Date(sheet.entryDate))
      .filter(date => !isNaN(date)); // ensure valid dates

    const minDate = new Date(Math.min(...dates));
    const maxDate = new Date(Math.max(...dates));

    startDate.value = formatDateForInput(minDate);
    endDate.value = formatDateForInput(maxDate);
  } catch (error) {
    console.error('Error fetching date range:', error);
    // Use fallback defaults if error occurs
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
