<template>
  <div class="left-sidebar">
    <!-- Top header (optional) -->
    <div class="sidebar-header d-flex align-center mb-4">
      <v-icon color="white" size="36">mdi-file-document</v-icon>
      <h3 class="sidebar-title ml-2">Feuille de temps</h3>
    </div>

    <!-- Navigation List -->
    <v-list dense>
      <!-- Feuille de temps -->
      <v-list-item
        :active="activeItem === 'feuille'"
        @click="setActive('feuille')"
      >
        <v-list-item-icon>
          <v-icon color="white">mdi-file-document</v-icon>
        </v-list-item-icon>
        <v-list-item-title class="text-white">Feuille de temps</v-list-item-title>
      </v-list-item>

      <!-- Choisir modèle -->
      <v-list-item
        :active="activeItem === 'modele'"
        @click="setActive('modele')"
      >
        <v-list-item-icon>
          <v-icon color="white">mdi-file-document-edit</v-icon>
        </v-list-item-icon>
        <v-list-item-title class="text-white">Choisir modèle</v-list-item-title>
      </v-list-item>

      <!-- Partager -->
      <v-list-item
        :active="activeItem === 'partager'"
        @click="setActive('partager')"
      >
        <v-list-item-icon>
          <v-icon color="white">mdi-share-variant</v-icon>
        </v-list-item-icon>
        <v-list-item-title class="text-white">Partager</v-list-item-title>
      </v-list-item>
    </v-list>
  </div>
</template>

<script setup>
import { ref } from 'vue'

// Track which item is active
const activeItem = ref('feuille')

function setActive(item) {
  activeItem.value = item
  // Optionally navigate or emit an event here
}
</script>

<style scoped>
.left-sidebar {
  background-color: #2C2C5E; /* Dark purple/blue background */
  color: #FFF;
  width: 240px;       /* Adjust as needed */
  min-height: 100vh;  /* Full vertical space */
  padding: 1rem;
}

.sidebar-header {
  /* Header area at the top */
}

.sidebar-title {
  color: #FFF;
  font-size: 1.2rem;
  margin: 0;
}

/* Highlight active items with a rounded background */
.v-list-item--active {
  background-color: rgba(255, 255, 255, 0.15) !important;
  border-radius: 8px;
}
</style>
