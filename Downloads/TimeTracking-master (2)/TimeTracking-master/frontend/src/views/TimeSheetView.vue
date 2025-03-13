<template>
  <!--  v-container  -->
  <v-container
    fluid
    class="pa-0 d-flex flex-column"
    style="min-height: 100vh;"
  >
    <!-- Title text  -->
    <h3 class="white--text px-6 pt-6">7 modèles</h3>

    <!-- Boxes row -->
    <v-row class="px-6" align="center" justify="start" dense>
      <!-- Box 1: Feuille de temps vierge (plus icon) -->
      <v-col cols="12" sm="6" md="3" class="d-flex justify-center mb-6">
        <!-- Wrap the box with a router-link to navigate on click -->
        <router-link to="/editTimeSheet" style="text-decoration: none;">
          <FeuilleTempsBox
            title="Feuille de temps vierge"
            icon="mdi-plus"
          />
        </router-link>
      </v-col>

      <!-- Box 2: Révision - Étude -->
      <v-col cols="12" sm="6" md="3" class="d-flex justify-center mb-6">
        <FeuilleTempsBox
          title="Révision - Étude"
          icon="mdi-book-open-page-variant"
        />
      </v-col>

      <!-- Box 3: Tâches -->
      <v-col cols="12" sm="6" md="3" class="d-flex justify-center mb-6">
        <FeuilleTempsBox
          title="Tâches"
          icon="mdi-check"
        />
      </v-col>

      <!-- Box 4: Routine -->
      <v-col cols="12" sm="6" md="3" class="d-flex justify-center mb-6">
        <FeuilleTempsBox
          title="Routine"
          icon="mdi-run"
        />
      </v-col>
    </v-row>

    <!-- Second row of additional boxes -->
    <v-row class="px-6" align="center" justify="start" dense>
      <!-- Box 5: Projets -->
      <v-col cols="12" sm="6" md="3" class="d-flex justify-center mb-6">
        <FeuilleTempsBox
          title="Projets"
          icon="mdi-briefcase-check"
        />
      </v-col>

      <!-- Box 6: Meeting -->
      <v-col cols="12" sm="6" md="3" class="d-flex justify-center mb-6">
        <FeuilleTempsBox
          title="Meeting"
          icon="mdi-account-group"
        />
      </v-col>

      <!-- Box 7: Pause -->
      <v-col cols="12" sm="6" md="3" class="d-flex justify-center mb-6">
        <FeuilleTempsBox
          title="Pause"
          icon="mdi-coffee"
        />
      </v-col>
    </v-row>
  </v-container>
</template>

<script setup>
import FeuilleTempsBox from '@/components/timesheet/FeuilleTempsBox.vue'
</script>

<style scoped>
/* Add any additional styles if needed */
</style>
