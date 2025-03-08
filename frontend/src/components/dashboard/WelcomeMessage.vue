<script setup>
defineProps({
  username: {
    type: String,
    default: ''
  }
});
</script>

<template>
  <div class="welcome-message pa-4">
    <template v-if="username">
      <h2 class="text-h5 font-weight-medium">Bonjour {{ username }},</h2>
      <p class="text-subtitle-1 mt-1">Prêt à atteindre vos objectifs aujourd'hui? 🚀</p>
    </template>
    <template v-else>
      <h2 class="text-h5 font-weight-medium">Bienvenue sur Time Tracking</h2>
      <p class="text-subtitle-1 mt-1">L'application qui vous aide à organiser votre temps 🚀</p>
    </template>
  </div>
</template>

<style scoped>
.welcome-message {
  color: white;
}
</style>
