import axios from 'axios';

const BASE_URL = 'https://intermediate-pansie-maksew-0cc92781.koyeb.app';

function getAuthToken() {
  const token = localStorage.getItem('token');
  console.log('[Auth] Retrieved token from localStorage:', token);
  return token;
}


/**
 * Export timesheets as CSV.
 * @param {string} startDate - Format: YYYY-MM-DD.
 * @param {string} endDate - Format: YYYY-MM-DD.
 */
export async function exportTimeSheetsCSV(startDate, endDate) {
  try {
    const token = getAuthToken();
    const response = await axios.get(`${BASE_URL}/api/timesheets/export/csv`, {
      params: { startDate, endDate },
      responseType: 'blob', // Expecting binary data
      headers: { Authorization: `Bearer ${token}` }
    });

    if (!response || !response.data) {
      console.error('No data received for CSV export.');
      return;
    }

    const blob = new Blob([response.data], { type: 'text/csv' });
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', `timesheets_${startDate}_${endDate}.csv`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);
  } catch (error) {
    console.error('Error exporting CSV:', error);
    throw error;
  }
}

/**
 * Export timesheets as PDF.
 * @param {string} startDate - Format: YYYY-MM-DD.
 * @param {string} endDate - Format: YYYY-MM-DD.
 */
export async function exportTimeSheetsPDF(startDate, endDate) {
  try {
    const token = getAuthToken();
    console.log("token is: ", token);

    console.log('Exporting PDF with token:', token);
    console.log(`Requesting PDF from: ${BASE_URL}/api/timesheets/export/pdf`);

    const response = await axios.get(`${BASE_URL}/api/timesheets/export/pdf`, {
      params: { startDate, endDate },
      responseType: 'blob',
      headers: { Authorization: `Bearer ${token}` }
    });

    // For debugging: Check if the response is in JSON format (which indicates an error)
    if (response.data && response.data.type && response.data.type.includes('application/json')) {
      const reader = new FileReader();
      reader.onload = () => {
        console.error('PDF export error response:', reader.result);
      };
      reader.readAsText(response.data);
      throw new Error('PDF export endpoint returned JSON (likely unauthorized).');
    }

    const pdfBlob = new Blob([response.data], { type: 'application/pdf' });
    const url = window.URL.createObjectURL(pdfBlob);
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', `timesheets_${startDate}_${endDate}.pdf`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);
  } catch (error) {
    console.error('Error exporting PDF:', error);
    throw error;
  }
}
