<template>
  <div class="access-denied-container pa-4">
    <v-card class="mx-auto" max-width="600" color="#283593">
      <v-card-title class="text-h4 text-center pt-6">
        <v-icon size="x-large" class="mr-2">mdi-lock</v-icon>
        Accès refusé
      </v-card-title>

      <v-card-text class="text-center pa-6">
        <p class="text-subtitle-1 mb-4">
          Vous n'avez pas les autorisations nécessaires pour accéder à cette page.
        </p>
        <v-btn color="primary" to="/" class="mt-4">
          Retour à l'accueil
        </v-btn>
      </v-card-text>
    </v-card>
  </div>
</template>

<style scoped>
.access-denied-container {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 80vh;
}
</style>
