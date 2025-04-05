<script setup>
import { RouterView } from 'vue-router';
import AppHeader from '@/components/layout/AppHeader.vue';
</script>

<template>
  <v-app theme="dark" class="app-container">
    <AppHeader />
    <v-main class="main-content pa-0 ma-0">
      <RouterView />
    </v-main>
  </v-app>
</template>

<style>
:root {
  --background-color: #1a237e; /* Indigo darken-4 */
  --card-color: #283593; /* Indigo darken-3 */
  --accent-color-primary: #7e57c2; /* Deep purple accent */
  --accent-color-secondary: #9c27b0; /* Purple */
}

/* Force les couleurs pour éviter les écrasements */
.app-container {
  background-color: var(--background-color) !important;
  overflow: hidden;
}

.v-application__wrap {
  min-height: 100vh !important;
}

.main-content {
  background-color: var(--background-color) !important;
  padding: 0 !important;
  margin: 0 !important;
}

/* Suppression complète des marges de v-main */
.v-main {
  padding-top: 0 !important;
}

/* Éliminer le padding du content wrapper */
.v-main__wrap {
  padding: 0 !important;
}

/* Style pour les cartes */
.v-card {
  border-radius: 12px !important;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.v-card:hover {
  box-shadow: 0 6px 12px rgba(0, 0, 0, 0.2) !important;
}

/* Style pour les barres de progression */
.v-progress-linear__determinate {
  background: linear-gradient(to right, var(--accent-color-primary), var(--accent-color-secondary)) !important;
}

/* Style pour la date */
.date-header {
  color: white;
  font-weight: 500;
  font-size: 1.25rem;
  margin: 0;
  padding: 16px 24px;
}

/* Mise en surbrillance des éléments spécifiques */
.highlight-purple {
  background-color: rgba(153, 89, 231, 0.15);
  transition: background-color 0.2s;
}

.highlight-purple:hover {
  background-color: rgba(153, 89, 231, 0.25);
}

/* Styles pour les statistiques et les tâches */
.task-item {
  border-radius: 8px;
  margin-bottom: 8px;
  transition: background-color 0.2s;
}

.task-item:hover {
  background-color: rgba(255, 255, 255, 0.05);
}

.task-complete .task-name {
  text-decoration: line-through;
  opacity: 0.7;
}

.progress-bar-container {
  height: 10px;
  border-radius: 5px;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.1);
}

.progress-bar {
  height: 100%;
  background: linear-gradient(to right, #7e57c2, #9c27b0);
}

/* Ajustement des espaces dans les cartes */
.v-card-title {
  padding: 16px 20px !important;
}

.v-card-text {
  padding: 0 20px 16px 20px !important;
}
</style>
