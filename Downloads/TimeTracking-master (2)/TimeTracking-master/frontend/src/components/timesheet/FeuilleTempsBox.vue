<template>
  <!-- v-hover gives us a "hover" boolean to control elevation or transform -->
  <v-hover v-slot="{ hover }">
    <v-card
      :elevation="hover ? 6 : 2"
      class="feuille-box d-flex flex-column align-center justify-center"
    >
      <!-- Icon (white color, size=48) -->
      <v-icon color="white" size="48" class="mb-4">
        {{ icon }}
      </v-icon>

      <!-- Title -->
      <div class="white--text text-subtitle-1">
        {{ title }}
      </div>
    </v-card>
  </v-hover>
</template>

<script setup>
defineProps({
  title: {
    type: String,
    required: true,
  },
  icon: {
    type: String,
    required: true,
  },
})
</script>

<style scoped>
.feuille-box {
  background-color: rgb(40, 53, 147);
  color: rgb(255, 255, 255);
  caret-color: rgb(255, 255, 255);
  border-radius: 8px;
  width: 200px;
  height: 200px;
  cursor: pointer;
  transition: transform 0.3s ease-in-out;
}

.feuille-box:hover {
  transform: translateY(-5px) scale(1.03);
}
</style>
